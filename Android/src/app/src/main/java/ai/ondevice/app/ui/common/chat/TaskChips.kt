/*
 * Copyright 2025 On Device AI Inc.
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class TaskChipItem(
  val label: String,
  val promptTemplate: String,
)

private val taskChipItems = listOf(
  TaskChipItem(label = "Translate", promptTemplate = "Translate the following to English:\n"),
  TaskChipItem(label = "Rewrite", promptTemplate = "Rewrite this more clearly:\n"),
  TaskChipItem(label = "Summarize", promptTemplate = "Summarize the following:\n"),
)

/**
 * Displays a row of suggestion chips above the chat input field.
 * Chips are visible only when the input field is empty.
 * Tapping a chip pre-fills the input with a prompt template.
 */
@Composable
fun TaskChips(
  currentInput: String,
  onChipSelected: (String) -> Unit,
  onChipTapped: (String) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = currentInput.isEmpty(),
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      taskChipItems.forEach { chip ->
        SuggestionChip(
          onClick = {
            onChipTapped(chip.label)
            onChipSelected(chip.promptTemplate)
          },
          label = { Text(text = chip.label) },
        )
      }
    }
  }
}
