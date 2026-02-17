/*
 * Copyright 2025 Google LLC
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

package ai.ondevice.app.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val TAG = "ImageGenerationHelper"

/**
 * Progress update emitted during image generation
 * @param step Current iteration step (1-based)
 * @param total Total number of iterations
 * @param intermediateBitmap Intermediate image result (available every 5 steps)
 */
data class GenerationProgress(
  val step: Int,
  val total: Int,
  val intermediateBitmap: Bitmap? = null
)

/**
 * Result of image generation
 */
sealed class GenerationResult {
  /**
   * Generation progress update
   * @param step Current generation step (1-based)
   * @param total Total number of steps
   * @param intermediateBitmap Intermediate preview image (available every 5 steps)
   */
  data class Progress(
    val step: Int,
    val total: Int,
    val intermediateBitmap: Bitmap? = null
  ) : GenerationResult()

  /**
   * Generation completed successfully
   * @param bitmap The generated image (512x512 for Stable Diffusion v1.5)
   */
  data class Success(val bitmap: Bitmap) : GenerationResult()

  /**
   * Generation failed with an error
   * @param message Error description
   */
  data class Error(val message: String) : GenerationResult()
}

/**
 * Helper class for MediaPipe Image Generator inference
 *
 * Wraps MediaPipe ImageGenerator API in a coroutine-based Flow interface for
 * non-blocking image generation with progress updates.
 *
 * Usage:
 * ```
 * ImageGenerationHelper.generateImage(
 *   context = context,
 *   modelPath = "/path/to/sd15/",
 *   prompt = "a photo of a cat",
 *   iterations = 20,
 *   seed = 12345
 * ).collect { result ->
 *   when (result) {
 *     is GenerationResult.Success -> displayImage(result.bitmap)
 *     is GenerationResult.Error -> showError(result.message)
 *   }
 * }
 * ```
 */
object ImageGenerationHelper {

  /**
   * Generate an image from a text prompt using Stable Diffusion
   *
   * This function runs on Dispatchers.Default (CPU-bound thread pool) and emits
   * progress updates via Flow as generation proceeds. Intermediate images are
   * emitted every 5 steps.
   *
   * @param context Android context for resource access
   * @param modelPath Absolute path to the model directory (e.g., "/data/.../sd15/")
   * @param prompt Text description of the image to generate
   * @param iterations Number of diffusion steps (5-50, default 20)
   * @param seed Random seed for reproducibility
   * @return Flow emitting GenerationProgress during generation and final GenerationResult
   */
  suspend fun generateImage(
    context: Context,
    modelPath: String,
    prompt: String,
    iterations: Int,
    seed: Int
  ): Flow<GenerationResult> = flow {
    Log.d(TAG, "Starting image generation: prompt='$prompt', iterations=$iterations, seed=$seed")
    Log.d(TAG, "Model path: $modelPath")

    // Validate inputs
    if (prompt.isBlank()) {
      emit(GenerationResult.Error("Prompt cannot be empty"))
      return@flow
    }

    if (iterations < 5 || iterations > 50) {
      emit(GenerationResult.Error("Iterations must be between 5 and 50"))
      return@flow
    }

    var imageGenerator: ImageGenerator? = null

    try {
      // Initialize ImageGenerator
      val options = ImageGenerator.ImageGeneratorOptions.builder()
        .setImageGeneratorModelDirectory(modelPath)
        .build()

      imageGenerator = ImageGenerator.createFromOptions(context, options)
      Log.d(TAG, "ImageGenerator initialized successfully")

      // Set generation parameters (prompt, iterations, seed)
      imageGenerator.setInputs(prompt, iterations, seed)
      Log.d(TAG, "Set inputs: prompt='$prompt', iterations=$iterations, seed=$seed")

      // Run inference loop with progress updates
      for (step in 1..iterations) {
        val showResult = (step % 5 == 0) || (step == iterations)
        val result = imageGenerator.execute(showResult)

        // Extract bitmap for intermediate and final results
        val generatedImage = result?.generatedImage()
        if (showResult && generatedImage != null) {
          val intermediateBitmap = BitmapExtractor.extract(generatedImage)
          Log.d(TAG, "Generated intermediate result at step $step/$iterations")

          // On final step, emit Success result
          if (step == iterations) {
            emit(GenerationResult.Success(intermediateBitmap))
            Log.d(TAG, "Image generation completed successfully")
          } else {
            // Emit progress with intermediate bitmap
            emit(GenerationResult.Progress(step, iterations, intermediateBitmap))
          }
        } else {
          // Emit progress without bitmap (for non-display steps)
          emit(GenerationResult.Progress(step, iterations, null))
        }
      }

    } catch (e: Exception) {
      Log.e(TAG, "Image generation failed", e)
      val errorMessage = when (e) {
        is IllegalArgumentException -> "Invalid input: ${e.message}"
        is IllegalStateException -> "Generation error: ${e.message}"
        else -> "Failed to generate image: ${e.message ?: e.javaClass.simpleName}"
      }
      emit(GenerationResult.Error(errorMessage))
    } finally {
      // Always release resources
      try {
        imageGenerator?.close()
        Log.d(TAG, "ImageGenerator resources released")
      } catch (e: Exception) {
        Log.e(TAG, "Error releasing ImageGenerator resources", e)
      }
    }
  }.flowOn(Dispatchers.Default) // Run flow operations on background thread
}
