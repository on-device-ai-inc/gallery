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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Modal bottom sheet menu for selecting a regeneration style.
 *
 * @param onDismiss Callback when the menu is dismissed
 * @param onStyleSelected Callback when a style is selected with the chosen RegenerateStyle
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegenerateMenu(
  onDismiss: () -> Unit,
  onStyleSelected: (RegenerateStyle) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedStyle by remember { mutableStateOf<RegenerateStyle?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
      Text(
        "Regenerate Response",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
      )

      // Display all regenerate options - compact spacing
      REGENERATE_OPTIONS.forEach { option ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedStyle = option.style }
            .padding(vertical = 6.dp)
        ) {
          RadioButton(
            selected = selectedStyle == option.style,
            onClick = { selectedStyle = option.style }
          )
          Icon(
            imageVector = option.icon,
            contentDescription = null,
            modifier = Modifier
              .padding(start = 8.dp)
              .size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Column(
            modifier = Modifier
              .padding(start = 8.dp)
              .weight(1f)
          ) {
            Text(
              text = option.label,
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = option.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Button(
        onClick = {
          selectedStyle?.let { style ->
            onStyleSelected(style)
          }
        },
        enabled = selectedStyle != null,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 48.dp) // Extra bottom padding to avoid nav bar overlap
      ) {
        Text("Regenerate")
      }
    }
  }
}
