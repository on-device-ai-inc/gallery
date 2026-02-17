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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ai.ondevice.app.R

/**
 * Rotating neural circuit logo icon - Model Thinking Indicator
 *
 * Specifications:
 * - Single-piece rotation around central axis
 * - Transparent background (no visible square borders)
 * - Faster rotation for thinking state (1.2s per revolution)
 * - Linear easing for controlled, mechanical feel
 * - NO scaling, NO pulsing, NO opacity changes
 * - Full color logo (no tint) for brand consistency
 *
 * @param modifier Modifier for the icon
 * @param size Size of the icon (default 20dp)
 * @param tint Color tint for the icon (default: Color.Unspecified = full color)
 * @param contentDescription Accessibility description
 */
@Composable
fun RotatingLogoIcon(
  modifier: Modifier = Modifier,
  size: Dp = 20.dp,
  tint: Color = Color.Unspecified, // Changed from MaterialTheme.colorScheme.primary
  contentDescription: String = "Loading"
) {
  val infiniteTransition = rememberInfiniteTransition(label = "logo_rotation")

  // Faster rotation for "thinking" state - 1.2 seconds per revolution
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 1200,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_angle"
  )

  Image(
    painter = painterResource(id = R.drawable.neural_circuit_logo),
    contentDescription = contentDescription,
    modifier = modifier
      .graphicsLayer {
        rotationZ = rotation
      },
    contentScale = ContentScale.Fit,
    colorFilter = ColorFilter.tint(Color(0xFF00D9FF))  // Cyan tint - matches RotationalLoader
  )
}
