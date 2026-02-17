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

package ai.ondevice.app.ui.imagegeneration

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ondevice.app.data.MODELS_IMAGE_GENERATION
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import kotlin.random.Random

/**
 * Image Generation Screen
 *
 * Allows users to generate images from text prompts using on-device Stable Diffusion.
 * Features:
 * - Multi-line prompt input (500 char limit)
 * - Iteration slider (5-50, default 20)
 * - Progress display with cancellation
 * - Final image display with "Generate Again" option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenerationScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  imageGenerationViewModel: ImageGenerationViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by imageGenerationViewModel.uiState.collectAsState()

  // Local UI state
  var prompt by remember { mutableStateOf("") }
  var iterations by remember { mutableIntStateOf(20) }

  // Get the image generation model
  val model = remember { MODELS_IMAGE_GENERATION.first() }
  val modelPath = remember { model.getPath(context) }

  // Check if model is downloaded
  val isModelDownloaded = remember(modelPath) {
    val exists = java.io.File(modelPath).exists()
    Log.d("ImageGenerationScreen", "Model path: $modelPath, exists: $exists")
    exists
  }

  Log.d("ImageGenerationScreen", "TextField enabled: $isModelDownloaded")

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Image Generation") },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
    snackbarHost = {
      // Show error messages via Snackbar
      uiState.errorMessage?.let { error ->
        Snackbar(
          action = {
            TextButton(onClick = { imageGenerationViewModel.clearError() }) {
              Text("Dismiss")
            }
          }
        ) {
          Text(error)
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Model not downloaded message (AC6)
      if (!isModelDownloaded) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Generate images from text, completely offline.",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "Download the 1.9GB Stable Diffusion model to get started.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Button(
              onClick = { /* TODO: Navigate to Model Manager in Story 7.5 */ },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Go to Model Manager")
            }
          }
        }
      }

      // Show progress display if generating (AC3)
      if (uiState.isGenerating) {
        ImageGenerationProgressDisplay(
          currentStep = uiState.currentStep,
          totalSteps = uiState.totalSteps,
          intermediateBitmap = uiState.intermediateBitmap,
          onCancel = { imageGenerationViewModel.cancelGeneration() }
        )
      }
      // Show final result if available (AC4)
      else if (uiState.finalBitmap != null) {
        FinalImageDisplay(
          bitmap = uiState.finalBitmap!!,
          isSaving = uiState.isSaving,
          onGenerateAgain = {
            // Generate Again with new seed (AC5)
            val newSeed = Random.nextInt()
            imageGenerationViewModel.clearResult()
            imageGenerationViewModel.startGeneration(
              context = context,
              modelPath = modelPath,
              prompt = prompt,
              iterations = iterations,
              seed = newSeed
            )
          },
          onNewPrompt = {
            // Clear result to show input controls again
            imageGenerationViewModel.clearResult()
          },
          onSave = {
            imageGenerationViewModel.saveImage(
              context = context,
              bitmap = uiState.finalBitmap!!,
              onSuccess = {
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
              },
              onError = { error ->
                Toast.makeText(context, "Failed to save: $error", Toast.LENGTH_SHORT).show()
              }
            )
          },
          onShare = {
            imageGenerationViewModel.shareImage(
              context = context,
              bitmap = uiState.finalBitmap!!,
              onError = { error ->
                Toast.makeText(context, "Failed to share: $error", Toast.LENGTH_SHORT).show()
              }
            )
          }
        )
      }
      // Show input controls if not generating and no result (AC1, AC2)
      else {
        // Prompt input (AC1, AC2)
        OutlinedTextField(
          value = prompt,
          onValueChange = { if (it.length <= 500) prompt = it },
          label = { Text("Prompt") },
          placeholder = { Text("Describe the image you want to generate...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 5,
          supportingText = {
            Text(
              text = "${prompt.length}/500 characters",
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth()
            )
          },
          readOnly = false,
          enabled = true  // Always enabled to allow paste - Generate button will be disabled instead
        )

        // Iteration slider (AC2)
        Column(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Iterations: $iterations",
            style = MaterialTheme.typography.titleSmall
          )
          Text(
            text = "More iterations = higher quality, longer generation time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          Slider(
            value = iterations.toFloat(),
            onValueChange = { iterations = it.toInt() },
            valueRange = 5f..50f,
            steps = 44,
            enabled = true  // Always enabled
          )
        }

        // Generate button (AC1)
        Button(
          onClick = {
            val seed = Random.nextInt()
            imageGenerationViewModel.startGeneration(
              context = context,
              modelPath = modelPath,
              prompt = prompt,
              iterations = iterations,
              seed = seed
            )
          },
          modifier = Modifier.fillMaxWidth(),
          enabled = isModelDownloaded && prompt.isNotBlank()
        ) {
          Text("Generate Image")
        }

        // Helpful tips
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Tips for better results:",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = "• Be specific and descriptive\n" +
                     "• Include style keywords (e.g., \"photorealistic\", \"oil painting\")\n" +
                     "• Mention lighting and composition\n" +
                     "• Start with 20 iterations, increase if needed",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
      }
    }
  }
}

/**
 * Final image display with Generate Again, Save, and Share buttons (AC4, AC5, Story 7.5, Story 7.6)
 */
@Composable
private fun FinalImageDisplay(
  bitmap: Bitmap,
  isSaving: Boolean,
  onGenerateAgain: () -> Unit,
  onNewPrompt: () -> Unit,
  onSave: () -> Unit,
  onShare: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Generated Image",
      style = MaterialTheme.typography.titleLarge
    )

    // Image display at 512x512 (AC4)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      )
    ) {
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Generated image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
      )
    }

    // Action buttons row 1: Save and Share
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Save to Gallery button (Story 7.5)
      Button(
        onClick = onSave,
        modifier = Modifier.weight(1f),
        enabled = !isSaving
      ) {
        Icon(
          imageVector = Icons.Default.SaveAlt,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (isSaving) "Saving..." else "Save")
      }

      // Share button (Story 7.6)
      Button(
        onClick = onShare,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Share")
      }
    }

    // Action buttons row 2: Generate Again and New Prompt
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = onGenerateAgain,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Generate Again")
      }

      OutlinedButton(
        onClick = onNewPrompt,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("New Prompt")
      }
    }

    Text(
      text = "\"Generate Again\" creates a different image from the same prompt. \"New Prompt\" lets you enter a new prompt.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
  }
}
