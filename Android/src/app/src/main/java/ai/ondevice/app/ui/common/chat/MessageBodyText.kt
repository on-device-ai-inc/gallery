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

package ai.ondevice.app.ui.common.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ai.ondevice.app.ui.common.MarkdownText
import ai.ondevice.app.ui.common.RotationalLoader

/** Composable function to display the text content of a ChatMessageText. */
@Composable
fun MessageBodyText(message: ChatMessageText) {
  if (message.side == ChatSide.USER) {
    if (message.isVoiceInput) {
      // Voice input message: show mic icon prefix
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(12.dp)
      ) {
        Icon(
          Icons.Rounded.Mic,
          contentDescription = "Voice input",
          modifier = Modifier.size(16.dp).padding(end = 4.dp),
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        MarkdownText(
          text = message.content,
          textColor = MaterialTheme.colorScheme.onSurface,
        )
      }
    } else {
      // Regular typed message
      MarkdownText(
        text = message.content,
        modifier = Modifier.padding(12.dp),
        textColor = MaterialTheme.colorScheme.onSurface,
      )
    }
  } else if (message.side == ChatSide.AGENT) {
    val isGenerating = message.latencyMs < 0
    val showThinking = isGenerating && message.content.isEmpty()

    // Thinking indicator with fade transitions (does NOT wrap content)
    AnimatedVisibility(
        visible = showThinking,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        RotationalLoader(size = 32.dp)
    }

    // Content with fade-in on first appearance (does NOT re-animate during streaming)
    AnimatedVisibility(
        visible = !showThinking,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        // Render streaming/complete content directly (no wrapper that restarts on updates)
        if (message.isMarkdown) {
            MarkdownText(
                text = message.content,
                modifier = Modifier
                    .padding(12.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Chat message content text markdown"
                    },
            )
        } else {
            Text(
                message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
  }
}
