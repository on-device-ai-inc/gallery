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

package ai.ondevice.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ai.ondevice.app.ui.theme.customColors
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle

/** Composable function to display Markdown-formatted text. */
@Composable
fun MarkdownText(
  text: String,
  modifier: Modifier = Modifier,
  smallFontSize: Boolean = false,
  textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
  val fontSize =
    if (smallFontSize) MaterialTheme.typography.bodyMedium.fontSize
    else MaterialTheme.typography.bodyLarge.fontSize
  CompositionLocalProvider {
    ProvideTextStyle(
      value = TextStyle(fontSize = fontSize, lineHeight = fontSize * 1.3, color = textColor)
    ) {
      RichText(
        modifier = modifier,
        style =
          RichTextStyle(
            codeBlockStyle =
              CodeBlockStyle(
                textStyle =
                  TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontFamily = FontFamily.Monospace,
                  )
              ),
            stringStyle =
              RichTextStringStyle(
                linkStyle =
                  TextLinkStyles(style = SpanStyle(color = MaterialTheme.customColors.linkColor))
              ),
          ),
      ) {
        Markdown(content = text)
      }
    }
  }
}

/**
 * A code block component with a copy button.
 * Can be used for standalone code display with one-tap copy.
 */
@Composable
fun CodeBlock(
  code: String,
  language: String? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
  ) {
    // Code content with horizontal scroll
    Text(
      text = code,
      style = TextStyle(
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(12.dp)
        .padding(end = 40.dp) // Make room for copy button
    )

    // Copy button in top-right corner
    IconButton(
      onClick = { copyToClipboard(context, code, "Code") },
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(4.dp)
        .size(32.dp),
      colors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
      )
    ) {
      Icon(
        imageVector = Icons.Rounded.ContentCopy,
        contentDescription = "Copy code",
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

// @Preview(showBackground = true)
// @Composable
// fun MarkdownTextPreview() {
//   GalleryTheme {
//     MarkdownText(text = "*Hello World*\n**Good morning!!**")
//   }
// }
