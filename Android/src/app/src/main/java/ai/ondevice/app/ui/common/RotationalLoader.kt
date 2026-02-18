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

package ai.ondevice.app.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import ai.ondevice.app.R

/**
 * Rotating neural circuit logo - System State Indicator
 *
 * Specifications:
 * - Single-piece rotation around central axis
 * - Transparent background (no visible square borders)
 * - Slow, steady rotation for downloading state (3s per revolution)
 * - Linear easing for mechanical, intentional feel
 * - NO scaling, NO pulsing, NO opacity changes
 *
 * @param size The size of the loader (diameter of the logo)
 */
@Composable
fun RotationalLoader(size: Dp) {
  val infiniteTransition = rememberInfiniteTransition(label = "logo_rotation")

  // Slow, steady rotation - 3 seconds per revolution for downloading state
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 3000,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_angle"
  )

  Box(
    modifier = Modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.neural_circuit_logo),
      contentDescription = "Loading",
      modifier = Modifier
        .size(size)
        .aspectRatio(1f)  // Force perfect square to prevent oval distortion
        .graphicsLayer {
          rotationZ = rotation
        },
      contentScale = ContentScale.Fit,
      colorFilter = ColorFilter.tint(Color(0xFF00D9FF))  // Cyan tint for color version
    )
  }
}
