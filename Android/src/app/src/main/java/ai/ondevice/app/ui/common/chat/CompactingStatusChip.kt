/*
 * Copyright 2025 OnDevice Inc.
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

package ai.ondevice.app.ui.common.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ai.ondevice.app.ui.common.RotationalLoader

/**
 * Composable function to display a visual indicator for conversation compaction.
 *
 * This function renders a rounded box with a pulsating effect containing a rotating loader
 * and a message indicating that the conversation is being compacted. It provides a visual
 * cue to the user that context compression is in progress.
 */
@Composable
fun CompactingStatusChip() {
  // Subtle pulsation effect (alpha oscillates between 0.7 and 1.0)
  val infiniteTransition = rememberInfiniteTransition(label = "compacting-pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha-pulse"
  )

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .alpha(alpha)  // Apply pulsation effect
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
        // Rotating loader indicator
        Box(modifier = Modifier.size(16.dp)) {
          RotationalLoader(size = 16.dp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Text message
        Text(
          "Compacting conversation so we can continue…",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
