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

package ai.ondevice.app.ui.imagegeneration

import android.graphics.Bitmap

/**
 * UI state for image generation progress and results
 */
data class ImageGenerationUiState(
  /** Whether image generation is currently in progress */
  val isGenerating: Boolean = false,

  /** Current generation step (1-based) */
  val currentStep: Int = 0,

  /** Total number of generation steps */
  val totalSteps: Int = 0,

  /** Intermediate bitmap preview (updated every 5 steps) */
  val intermediateBitmap: Bitmap? = null,

  /** Final generated bitmap when generation completes */
  val finalBitmap: Bitmap? = null,

  /** Error message if generation failed */
  val errorMessage: String? = null,

  /** Whether generation was cancelled by user */
  val cancelled: Boolean = false,

  /** Whether image save is currently in progress */
  val isSaving: Boolean = false
)
