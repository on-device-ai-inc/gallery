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

package ai.ondevice.app.ui.llmchat

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Mms
import androidx.compose.runtime.Composable
import ai.ondevice.app.R
import ai.ondevice.app.customtasks.common.CustomTask
import ai.ondevice.app.customtasks.common.CustomTaskDataForBuiltinTask
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Category
import ai.ondevice.app.data.DataStoreRepository
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.Task
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Message
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

////////////////////////////////////////////////////////////////////////////////////////////////////
// AI Chat.

class LlmChatTask @Inject constructor(
  private val dataStoreRepository: DataStoreRepository
) : CustomTask {
  override val task: Task =
    Task(
      id = BuiltInTaskId.LLM_CHAT,
      label = "AI Chat",
      category = Category.LLM,
      icon = Icons.Outlined.Forum,
      models = mutableListOf(),
      description = "Chat with on-device AI models. Supports text and images based on model capabilities",
      docUrl = "https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android",
      sourceCodeUrl =
        "https://github.com/on-device-ai-inc/on-device-ai/blob/main/Android/src/app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatModelHelper.kt",
      textInputPlaceHolderRes = R.string.text_input_placeholder_llm_chat,
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    // Story 8: Read custom instructions from DataStore
    val customInstructions = dataStoreRepository.readCustomInstructions()
    val systemMessage = if (customInstructions.isNotEmpty()) {
      Message.of(listOf(Content.Text(customInstructions)))
    } else {
      null
    }

    LlmChatModelHelper.initialize(
      context = context,
      model = model,
      supportImage = model.llmSupportImage,  // Enable based on model capability
      supportAudio = model.llmSupportAudio,  // Enable based on model capability
      onDone = onDone,
      systemMessage = systemMessage,
    )
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    LlmChatModelHelper.cleanUp(model = model, onDone = onDone)
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    LlmChatScreen(
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
      onNavigateToConversationHistory = myData.onNavigateToConversationHistory,
      onNavigateToSettings = myData.onNavigateToSettings,  // Epic 5: Settings navigation
      loadConversationId = myData.loadConversationId,
      onConversationLoaded = myData.onConversationLoaded,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class) // Or another component that fits your scope
internal object LlmChatTaskModule {
  @Provides
  @IntoSet
  fun provideTask(dataStoreRepository: DataStoreRepository): CustomTask {
    return LlmChatTask(dataStoreRepository)
  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Ask image.

class LlmAskImageTask @Inject constructor(
  private val dataStoreRepository: DataStoreRepository
) : CustomTask {
  override val task: Task =
    Task(
      id = BuiltInTaskId.LLM_ASK_IMAGE,
      label = "Ask Image",
      category = Category.LLM,
      icon = Icons.Outlined.Mms,
      models = mutableListOf(),
      description = "Ask questions about images with on-device large language models",
      docUrl = "https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android",
      sourceCodeUrl =
        "https://github.com/on-device-ai-inc/on-device-ai/blob/main/Android/src/app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatModelHelper.kt",
      textInputPlaceHolderRes = R.string.text_input_placeholder_llm_chat,
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    // Story 8: Read custom instructions from DataStore
    val customInstructions = dataStoreRepository.readCustomInstructions()
    val systemMessage = if (customInstructions.isNotEmpty()) {
      Message.of(listOf(Content.Text(customInstructions)))
    } else {
      null
    }

    LlmChatModelHelper.initialize(
      context = context,
      model = model,
      supportImage = model.llmSupportImage,  // Enable based on model capability
      supportAudio = model.llmSupportAudio,  // Enable based on model capability
      onDone = onDone,
      systemMessage = systemMessage,
    )
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    LlmChatModelHelper.cleanUp(model = model, onDone = onDone)
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    LlmAskImageScreen(
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
      onNavigateToConversationHistory = myData.onNavigateToConversationHistory,
      onNavigateToSettings = myData.onNavigateToSettings,  // Epic 5: Settings navigation
      loadConversationId = myData.loadConversationId,
      onConversationLoaded = myData.onConversationLoaded,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class) // Or another component that fits your scope
internal object LlmAskImageModule {
  @Provides
  @IntoSet
  fun provideTask(dataStoreRepository: DataStoreRepository): CustomTask {
    return LlmAskImageTask(dataStoreRepository)
  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Ask audio.

class LlmAskAudioTask @Inject constructor(
  private val dataStoreRepository: DataStoreRepository
) : CustomTask {
  override val task: Task =
    Task(
      id = BuiltInTaskId.LLM_ASK_AUDIO,
      label = "Audio Scribe",
      category = Category.LLM,
      icon = Icons.Outlined.Mic,
      models = mutableListOf(),
      description =
        "Instantly transcribe and/or translate audio clips using on-device large language models",
      docUrl = "https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android",
      sourceCodeUrl =
        "https://github.com/on-device-ai-inc/on-device-ai/blob/main/Android/src/app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatModelHelper.kt",
      textInputPlaceHolderRes = R.string.text_input_placeholder_llm_chat,
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    // Story 8: Read custom instructions from DataStore
    val customInstructions = dataStoreRepository.readCustomInstructions()
    val systemMessage = if (customInstructions.isNotEmpty()) {
      Message.of(listOf(Content.Text(customInstructions)))
    } else {
      null
    }

    LlmChatModelHelper.initialize(
      context = context,
      model = model,
      supportImage = model.llmSupportImage,  // Enable based on model capability
      supportAudio = model.llmSupportAudio,  // Enable based on model capability
      onDone = onDone,
      systemMessage = systemMessage,
    )
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    LlmChatModelHelper.cleanUp(model = model, onDone = onDone)
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    LlmAskAudioScreen(
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
      onNavigateToConversationHistory = myData.onNavigateToConversationHistory,
      onNavigateToSettings = myData.onNavigateToSettings,  // Epic 5: Settings navigation
      loadConversationId = myData.loadConversationId,
      onConversationLoaded = myData.onConversationLoaded,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class) // Or another component that fits your scope
internal object LlmAskAudioModule {
  @Provides
  @IntoSet
  fun provideTask(dataStoreRepository: DataStoreRepository): CustomTask {
    return LlmAskAudioTask(dataStoreRepository)
  }
}
