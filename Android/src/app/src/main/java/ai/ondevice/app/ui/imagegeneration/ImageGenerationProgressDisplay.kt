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

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Progress display for image generation
 *
 * Shows:
 * - Privacy indicator ("Running privately on your device")
 * - Progress text ("Generating... Step X of Y")
 * - Linear progress indicator
 * - Intermediate bitmap preview (when available)
 * - Cancel button
 */
@Composable
fun ImageGenerationProgressDisplay(
  currentStep: Int,
  totalSteps: Int,
  intermediateBitmap: Bitmap?,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Privacy indicator (AC5)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Running privately on your device",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.tertiary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Progress text (AC1)
      Text(
        text = "Generating... Step $currentStep of $totalSteps",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Linear progress indicator (AC1)
      LinearProgressIndicator(
        progress = { currentStep.toFloat() / totalSteps.toFloat() },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary,
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Intermediate bitmap preview (AC2)
      if (intermediateBitmap != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        ) {
          Image(
            bitmap = intermediateBitmap.asImageBitmap(),
            contentDescription = "Intermediate preview at step $currentStep",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      // Cancel button (AC3, AC4)
      OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.Cancel,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Cancel Generation")
      }
    }
  }
}
