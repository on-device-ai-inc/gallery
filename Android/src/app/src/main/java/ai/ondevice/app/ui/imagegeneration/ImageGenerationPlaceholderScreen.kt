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

import android.app.ActivityManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenerationPlaceholderScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  imageGenerationViewModel: ImageGenerationViewModel = viewModel()
) {
  val context = LocalContext.current
  val deviceInfo = remember {
    getDeviceCapabilities(context)
  }

  val uiState by imageGenerationViewModel.uiState.collectAsState()

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
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.Image,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Image Generation",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "On-device AI image generation from text prompts",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Device capability warning
      if (!deviceInfo.meetsMinimumRequirements) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Device may not support image generation",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = deviceInfo.warningMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      Spacer(modifier = Modifier.height(8.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp)
        ) {
          Text(
            text = "To get started:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "1. Go back to the home screen\n" +
                  "2. Navigate to Model Management\n" +
                  "3. Download the Stable Diffusion model (1.9GB)\n" +
                  "4. Return here to generate images\n\n" +
                  "Image generation UI coming in Story 7.4!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Show progress display if generation is in progress (Story 7.3)
      if (uiState.isGenerating) {
        ImageGenerationProgressDisplay(
          currentStep = uiState.currentStep,
          totalSteps = uiState.totalSteps,
          intermediateBitmap = uiState.intermediateBitmap,
          onCancel = { imageGenerationViewModel.cancelGeneration() }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Requirements: 8GB+ RAM, Android 12+",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Device info
      Text(
        text = "Your device: ${deviceInfo.ramGB}GB RAM, Android ${deviceInfo.androidVersion}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

data class DeviceCapabilities(
  val ramGB: Int,
  val androidVersion: Int,
  val meetsMinimumRequirements: Boolean,
  val warningMessage: String
)

private fun getDeviceCapabilities(context: android.content.Context): DeviceCapabilities {
  // Get RAM
  val activityManager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as ActivityManager
  val memoryInfo = ActivityManager.MemoryInfo()
  activityManager.getMemoryInfo(memoryInfo)
  val ramGB = (memoryInfo.totalMem / (1024 * 1024 * 1024)).toInt()

  // Get Android version
  val androidVersion = Build.VERSION.SDK_INT

  // Check requirements
  val meetsRamRequirement = ramGB >= 6
  val meetsAndroidRequirement = androidVersion >= 31 // Android 12

  val meetsMinimumRequirements = meetsRamRequirement && meetsAndroidRequirement

  val warningMessage = when {
    !meetsRamRequirement && !meetsAndroidRequirement ->
      "Your device has ${ramGB}GB RAM (requires 6GB+) and Android ${androidVersion} (requires Android 12+)"
    !meetsRamRequirement ->
      "Your device has ${ramGB}GB RAM (requires 6GB+ for optimal performance)"
    !meetsAndroidRequirement ->
      "Your device runs Android ${androidVersion} (requires Android 12+ for OpenCL support)"
    else -> ""
  }

  return DeviceCapabilities(
    ramGB = ramGB,
    androidVersion = androidVersion,
    meetsMinimumRequirements = meetsMinimumRequirements,
    warningMessage = warningMessage
  )
}
