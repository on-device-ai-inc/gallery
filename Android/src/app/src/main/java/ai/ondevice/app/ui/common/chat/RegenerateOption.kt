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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.ShortText
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing different regeneration styles for AI responses.
 */
enum class RegenerateStyle {
  /** Prioritize speed, shorter responses */
  FASTER,

  /** Prioritize thoroughness, longer responses with examples */
  MORE_DETAILED,

  /** Try alternative approach or perspective */
  DIFFERENT,

  /** Concise, 2-3 sentence maximum responses */
  SHORTER,

  /** Default regenerate with no prompt modification */
  STANDARD
}

/**
 * Data class representing a regenerate option with UI display information.
 */
data class RegenerateOption(
  val style: RegenerateStyle,
  val label: String,
  val description: String,
  val icon: ImageVector
)

/**
 * Predefined regenerate options for the UI.
 */
val REGENERATE_OPTIONS = listOf(
  RegenerateOption(
    style = RegenerateStyle.FASTER,
    label = "Faster Response",
    description = "Get a quick, concise answer",
    icon = Icons.Rounded.Speed
  ),
  RegenerateOption(
    style = RegenerateStyle.MORE_DETAILED,
    label = "More Detailed",
    description = "Get a thorough response with examples",
    icon = Icons.Rounded.Description
  ),
  RegenerateOption(
    style = RegenerateStyle.DIFFERENT,
    label = "Different Approach",
    description = "Try an alternative perspective",
    icon = Icons.Rounded.Lightbulb
  ),
  RegenerateOption(
    style = RegenerateStyle.SHORTER,
    label = "Shorter",
    description = "Get a brief, 2-3 sentence answer",
    icon = Icons.Rounded.ShortText
  )
)
