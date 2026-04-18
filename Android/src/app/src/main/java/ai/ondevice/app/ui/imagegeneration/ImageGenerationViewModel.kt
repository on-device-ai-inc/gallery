/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Modifications are proprietary and confidential.
 *
 * Originally Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.ondevice.app.ui.imagegeneration

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.helper.GenerationResult
import ai.ondevice.app.helper.ImageGenerationHelper
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ai.ondevice.app.firebaseAnalytics
import androidx.core.os.bundleOf

private const val TAG = "ImageGenerationViewModel"

/**
 * ViewModel for image generation with progress tracking and cancellation support
 */
@HiltViewModel
class ImageGenerationViewModel @Inject constructor() : ViewModel() {

  private val _uiState = MutableStateFlow(ImageGenerationUiState())
  val uiState = _uiState.asStateFlow()

  private var generationJob: Job? = null

  /**
   * Start image generation with progress tracking
   *
   * @param context Android context for resource access
   * @param modelPath Path to the model directory (from model.getPath(context))
   * @param prompt Text description of the image to generate
   * @param iterations Number of diffusion steps (5-50, default 20)
   * @param seed Random seed for reproducibility
   */
  fun startGeneration(
    context: Context,
    modelPath: String,
    prompt: String,
    iterations: Int,
    seed: Int
  ) {
    // Cancel any existing generation
    generationJob?.cancel()

    // Reset UI state
    _uiState.update {
      ImageGenerationUiState(
        isGenerating = true,
        currentStep = 0,
        totalSteps = iterations
      )
    }

    // Start generation coroutine
    generationJob = viewModelScope.launch {
      val startTime = System.currentTimeMillis()
      val trace = safePerformanceTrace("image_generation")
      trace.putAttribute("model_name", modelPath.substringAfterLast("/"))
      trace.putAttribute("resolution", "512x512")
      trace.safePutMetric("steps", iterations.toLong())
      trace.safeStart()
      try {
        ImageGenerationHelper.generateImage(
          context = context,
          modelPath = modelPath,
          prompt = prompt,
          iterations = iterations,
          seed = seed
        ).collect { result ->
          when (result) {
            is GenerationResult.Progress -> {
              // Update progress state
              _uiState.update { state ->
                state.copy(
                  currentStep = result.step,
                  totalSteps = result.total,
                  intermediateBitmap = result.intermediateBitmap ?: state.intermediateBitmap
                )
              }
              Log.d(TAG, "Progress: ${result.step}/${result.total}")
            }

            is GenerationResult.Success -> {
              // Generation completed successfully
              val generationTimeMs = System.currentTimeMillis() - startTime
              trace.safeStop()

              // Track successful image generation
              firebaseAnalytics?.logEvent(
                "image_generated",
                bundleOf(
                  "model_name" to modelPath.substringAfterLast("/"),
                  "iterations" to iterations,
                  "seed" to seed,
                  "generation_time_ms" to generationTimeMs
                )
              )

              _uiState.update { state ->
                state.copy(
                  isGenerating = false,
                  finalBitmap = result.bitmap,
                  currentStep = state.totalSteps, // Ensure we show 100% completion
                  errorMessage = null
                )
              }
              Log.d(TAG, "Generation completed successfully")
            }

            is GenerationResult.Error -> {
              // Generation failed
              trace.safeStop()
              _uiState.update { state ->
                state.copy(
                  isGenerating = false,
                  errorMessage = result.message
                )
              }
              Log.e(TAG, "Generation failed: ${result.message}")
            }
          }
        }
      } catch (e: Exception) {
        trace.safeStop()
        if (e is CancellationException) throw e
        firebaseAnalytics?.logEvent(
          "error_occurred",
          bundleOf(
            "error_type" to e::class.simpleName,
            "source_class" to "ImageGenerationViewModel",
            "error_message" to e.message.orEmpty()
          )
        )
        _uiState.update { state ->
          state.copy(
            isGenerating = false,
            errorMessage = "Unexpected error: ${e.message}"
          )
        }
        Log.e(TAG, "Unexpected error during generation", e)
      }
    }
  }

  /**
   * Cancel the ongoing image generation
   *
   * Cancellation should complete within 2 seconds (cooperative cancellation)
   */
  fun cancelGeneration() {
    Log.d(TAG, "Cancellation requested")
    generationJob?.cancel()
    generationJob = null

    _uiState.update { state ->
      state.copy(
        isGenerating = false,
        cancelled = true,
        errorMessage = "Generation cancelled"
      )
    }
  }

  /**
   * Clear error message and reset cancelled state
   */
  fun clearError() {
    _uiState.update { state ->
      state.copy(
        errorMessage = null,
        cancelled = false
      )
    }
  }

  /**
   * Clear the final bitmap (for starting a new generation)
   */
  fun clearResult() {
    _uiState.update { state ->
      state.copy(
        finalBitmap = null,
        intermediateBitmap = null,
        currentStep = 0,
        errorMessage = null,
        cancelled = false
      )
    }
  }

  /**
   * Save the generated image to device gallery
   *
   * @param context Android context
   * @param bitmap The bitmap to save
   * @param onSuccess Callback invoked on successful save
   * @param onError Callback invoked on save failure with error message
   */
  fun saveImage(
    context: Context,
    bitmap: Bitmap,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      _uiState.update { it.copy(isSaving = true) }

      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          saveImageMediaStore(context, bitmap)
        } else {
          saveImageLegacy(context, bitmap)
        }

        withContext(Dispatchers.Main) {
          _uiState.update { it.copy(isSaving = false) }
          onSuccess()
        }
        Log.d(TAG, "Image saved to gallery successfully")
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        withContext(Dispatchers.Main) {
          _uiState.update { it.copy(isSaving = false) }
          onError(e.message ?: "Failed to save image")
        }
        Log.e(TAG, "Failed to save image to gallery", e)
      }
    }
  }

  /**
   * Save image using MediaStore API (Android 10+)
   */
  private fun saveImageMediaStore(context: Context, bitmap: Bitmap) {
    val filename = generateImageFilename()
    val contentValues = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, filename)
      put(MediaStore.Images.Media.MIME_TYPE, "image/png")
      put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      ?: throw Exception("Failed to create media store entry")

    resolver.openOutputStream(uri)?.use { outputStream ->
      if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
        throw Exception("Failed to compress bitmap")
      }
    } ?: throw Exception("Failed to open output stream")

    Log.d(TAG, "Image saved via MediaStore: $filename")
  }

  /**
   * Save image using legacy File API (Android 9 and below)
   */
  @Suppress("DEPRECATION")
  private fun saveImageLegacy(context: Context, bitmap: Bitmap) {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    if (!picturesDir.exists()) {
      picturesDir.mkdirs()
    }

    val filename = generateImageFilename()
    val file = File(picturesDir, filename)

    FileOutputStream(file).use { outputStream ->
      if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
        throw Exception("Failed to compress bitmap")
      }
    }

    // Notify media scanner to update gallery
    MediaScannerConnection.scanFile(
      context,
      arrayOf(file.absolutePath),
      arrayOf("image/png"),
      null
    )

    Log.d(TAG, "Image saved via legacy API: ${file.absolutePath}")
  }

  /**
   * Generate filename for saved image with timestamp
   *
   * @return Filename in format: ondevice_ai_yyyyMMdd_HHmmss.png
   */
  private fun generateImageFilename(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return "ondevice_ai_$timestamp.png"
  }

  /**
   * Share the generated image via system share sheet
   *
   * @param context Android context
   * @param bitmap The bitmap to share
   * @param onError Callback invoked on share failure with error message
   */
  fun shareImage(
    context: Context,
    bitmap: Bitmap,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        // Create temporary file in cache directory
        val cacheDir = File(context.cacheDir, "shared")
        if (!cacheDir.exists()) {
          cacheDir.mkdirs()
        }

        val filename = "shared_image_${System.currentTimeMillis()}.png"
        val file = File(cacheDir, filename)

        // Write bitmap to file
        FileOutputStream(file).use { outputStream ->
          if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
            throw Exception("Failed to compress bitmap for sharing")
          }
        }

        // Get content URI via FileProvider
        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.provider",
          file
        )

        // Create share intent
        val intent = Intent(Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_TEXT, "Generated with OnDevice AI")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Launch share chooser
        withContext(Dispatchers.Main) {
          val chooser = Intent.createChooser(intent, "Share Image")
          chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(chooser)
        }

        Log.d(TAG, "Image shared successfully: $filename")
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        withContext(Dispatchers.Main) {
          onError(e.message ?: "Failed to share image")
        }
        Log.e(TAG, "Failed to share image", e)
      }
    }
  }
}
