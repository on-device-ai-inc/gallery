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

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import ai.ondevice.app.R
import ai.ondevice.app.customtasks.common.CustomTask
import ai.ondevice.app.customtasks.common.CustomTaskDataForBuiltinTask
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Category
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.MODELS_IMAGE_GENERATION
import ai.ondevice.app.data.Task
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

////////////////////////////////////////////////////////////////////////////////////////////////////
// Image Generation.

class ImageGenerationTask @Inject constructor() : CustomTask {
  override val task: Task =
    Task(
      id = BuiltInTaskId.IMAGE_GENERATION,
      label = "Image Generation",
      category = Category.LLM,
      icon = Icons.Outlined.Image,
      models = MODELS_IMAGE_GENERATION,
      description = "Generate images from text prompts using on-device Stable Diffusion model. Privacy-first AI art creation with no internet required.",
      docUrl = "https://ai.google.dev/edge/mediapipe/solutions/vision/image_generator/android",
      sourceCodeUrl = "https://github.com/google-ai-edge/mediapipe/blob/master/mediapipe/tasks/android/vision/imagegenerator/src/main/java/com/google/mediapipe/tasks/vision/imagegenerator/ImageGenerator.java",
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    // ImageGenerator initialization happens on-demand in generateImage()
    // Just verify model path exists
    val modelPath = model.getPath(context)
    val modelDir = java.io.File(modelPath)

    if (!modelDir.exists()) {
      onDone("Model not downloaded. Please download the Stable Diffusion model first.")
    } else {
      onDone("") // Empty string indicates success
    }
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    // ImageGenerator cleanup happens automatically in ImageGenerationHelper.generateImage()'s finally block
    // No persistent resources to clean up here
    onDone()
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    ImageGenerationScreen(
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class)
internal object ImageGenerationTaskModule {
  @Provides
  @IntoSet
  fun provideTask(): CustomTask {
    return ImageGenerationTask()
  }
}
