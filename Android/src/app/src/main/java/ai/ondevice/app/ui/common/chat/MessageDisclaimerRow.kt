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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ondevice.app.R

/**
 * Disclaimer row that appears below AI responses, similar to Claude mobile app.
 * Shows app logo + message: "OnDevice can make mistakes. Please double check responses."
 */
@Composable
fun MessageDisclaimerRow() {
  Row(
    modifier = Modifier.padding(top = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // App logo - 48dp in full color (3x original 16dp size)
    Image(
      painter = painterResource(id = R.mipmap.ic_launcher_foreground),
      contentDescription = "OnDevice Logo",
      modifier = Modifier
        .size(48.dp)
        .padding(0.dp)
    )

    // 16dp spacer (proportional increase for visual balance)
    Spacer(modifier = Modifier.width(16.dp))

    // Disclaimer text
    Text(
      text = "OnDevice can make mistakes. Please double check responses.",
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
      color = Color(0xFF888888)
    )
  }
}
