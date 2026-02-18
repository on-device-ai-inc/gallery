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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ai.ondevice.app.data.Task
import ai.ondevice.app.ui.theme.customColors

@Composable
fun getTaskBgColor(task: Task): Color {
  val size = MaterialTheme.customColors.taskBgColors.size
  val colorIndex: Int = ((task.index % size) + size) % size
  return MaterialTheme.customColors.taskBgColors[colorIndex]
}

@Composable
fun getTaskBgGradientColors(task: Task): List<Color> {
  val size = MaterialTheme.customColors.taskBgGradientColors.size
  val colorIndex: Int = ((task.index % size) + size) % size
  return MaterialTheme.customColors.taskBgGradientColors[colorIndex]
}

@Composable
fun getTaskIconColor(task: Task): Color {
  val size = MaterialTheme.customColors.taskIconColors.size
  val colorIndex: Int = ((task.index % size) + size) % size
  return MaterialTheme.customColors.taskIconColors[colorIndex]
}

@Composable
fun getTaskIconColor(index: Int): Color {
  val size = MaterialTheme.customColors.taskIconColors.size
  val colorIndex: Int = ((index % size) + size) % size
  return MaterialTheme.customColors.taskIconColors[colorIndex]
}
