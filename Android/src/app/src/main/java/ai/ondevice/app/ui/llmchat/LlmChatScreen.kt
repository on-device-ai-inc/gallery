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

package ai.ondevice.app.ui.llmchat

import androidx.hilt.navigation.compose.hiltViewModel

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.firebaseAnalytics
import ai.ondevice.app.ui.common.chat.ChatMessageAudioClip
import ai.ondevice.app.ui.common.chat.ChatMessageImage
import ai.ondevice.app.ui.common.chat.ChatMessageText
import ai.ondevice.app.ui.common.chat.ChatView
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel

@Composable
fun LlmChatScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  onNavigateToConversationHistory: () -> Unit,
  onNavigateToSettings: () -> Unit = {},  // Epic 5: Settings navigation
  loadConversationId: Long? = null,
  onConversationLoaded: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: LlmChatViewModel = hiltViewModel(),
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    taskId = BuiltInTaskId.LLM_CHAT,
    navigateUp = navigateUp,
    onNavigateToConversationHistory = onNavigateToConversationHistory,
    onNavigateToSettings = onNavigateToSettings,
    loadConversationId = loadConversationId,
    onConversationLoaded = onConversationLoaded,
    modifier = modifier,
  )
}

@Composable
fun LlmAskImageScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  onNavigateToConversationHistory: () -> Unit,
  onNavigateToSettings: () -> Unit = {},  // Epic 5: Settings navigation
  loadConversationId: Long? = null,
  onConversationLoaded: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: LlmAskImageViewModel = hiltViewModel(),
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    taskId = BuiltInTaskId.LLM_ASK_IMAGE,
    navigateUp = navigateUp,
    onNavigateToConversationHistory = onNavigateToConversationHistory,
    onNavigateToSettings = onNavigateToSettings,
    loadConversationId = loadConversationId,
    onConversationLoaded = onConversationLoaded,
    modifier = modifier,
  )
}

@Composable
fun LlmAskAudioScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  onNavigateToConversationHistory: () -> Unit,
  onNavigateToSettings: () -> Unit = {},  // Epic 5: Settings navigation
  loadConversationId: Long? = null,
  onConversationLoaded: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: LlmAskAudioViewModel = hiltViewModel(),
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    taskId = BuiltInTaskId.LLM_ASK_AUDIO,
    navigateUp = navigateUp,
    onNavigateToConversationHistory = onNavigateToConversationHistory,
    onNavigateToSettings = onNavigateToSettings,
    loadConversationId = loadConversationId,
    onConversationLoaded = onConversationLoaded,
    modifier = modifier,
  )
}

@Composable
fun ChatViewWrapper(
  viewModel: LlmChatViewModelBase,
  modelManagerViewModel: ModelManagerViewModel,
  taskId: String,
  navigateUp: () -> Unit,
  onNavigateToConversationHistory: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},  // Epic 5: Settings navigation
  loadConversationId: Long? = null,
  onConversationLoaded: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val task = modelManagerViewModel.getTaskById(id = taskId)!!
  val modelManagerUiState = modelManagerViewModel.uiState.collectAsState()
  val selectedModel = modelManagerUiState.value.selectedModel

  // Update #8: Load conversation when returning from history list
  androidx.compose.runtime.LaunchedEffect(loadConversationId) {
    if (loadConversationId != null) {
      viewModel.loadConversation(loadConversationId, selectedModel)
      onConversationLoaded()
    }
  }

  ChatView(
    task = task,
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    onSendMessage = { model, messages ->
      for (message in messages) {
        viewModel.addMessage(model = model, message = message)
      }

      var text = ""
      val images: MutableList<Bitmap> = mutableListOf()
      val audioMessages: MutableList<ChatMessageAudioClip> = mutableListOf()
      var chatMessageText: ChatMessageText? = null
      for (message in messages) {
        if (message is ChatMessageText) {
          chatMessageText = message
          text = message.content
        } else if (message is ChatMessageImage) {
          images.addAll(message.bitmaps)
        } else if (message is ChatMessageAudioClip) {
          audioMessages.add(message)
        }
      }
      if ((text.isNotEmpty() && chatMessageText != null) || audioMessages.isNotEmpty()) {
        modelManagerViewModel.addTextInputHistory(text)
        viewModel.generateResponse(
          model = model,
          input = text,
          images = images,
          audioMessages = audioMessages,
          onError = {
            viewModel.handleError(
              context = context,
              task = task,
              model = model,
              modelManagerViewModel = modelManagerViewModel,
              triggeredMessage = chatMessageText,
            )
          },
        )

        firebaseAnalytics?.logEvent(
          "generate_action",
          bundleOf("capability_name" to task.id, "model_id" to model.name),
        )
      }
    },
    onRunAgainClicked = { model, message, style ->
      if (message is ChatMessageText) {
        viewModel.runAgain(
          model = model,
          message = message,
          style = style,
          onError = {
            viewModel.handleError(
              context = context,
              task = task,
              model = model,
              modelManagerViewModel = modelManagerViewModel,
              triggeredMessage = message,
            )
          },
        )
      }
    },
    onBenchmarkClicked = { _, _, _, _ -> },
    onResetSessionClicked = { model -> viewModel.resetSession(task = task, model = model) },
    showStopButtonInInputWhenInProgress = true,
    onStopButtonClicked = { model -> viewModel.stopResponse(model = model) },
    navigateUp = navigateUp,
    onNavigateToConversationHistory = onNavigateToConversationHistory,
    onNavigateToSettings = onNavigateToSettings,  // Epic 5: Settings navigation
    modifier = modifier,
  )
}
