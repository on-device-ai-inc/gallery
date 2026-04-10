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

package ai.ondevice.app.ui.common.chat

import ai.ondevice.app.data.AnalyticsTracker
import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ConversationThread
import ai.ondevice.app.data.ConversationMessage
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.common.processLlmResponse
import ai.ondevice.app.data.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val TAG = "AGChatViewModel"

data class ChatUiState(
  /** Indicates whether the runtime is currently processing a message. */
  val inProgress: Boolean = false,

  /** Indicates whether the session is being reset. */
  val isResettingSession: Boolean = false,

  /**
   * Indicates whether the model is preparing (before outputting any result and after initializing).
   */
  val preparing: Boolean = false,

  /**
   * Indicates whether the conversation is being compacted (summarizing old messages).
   */
  val isCompacting: Boolean = false,

  /** A map of model names to lists of chat messages. */
  val messagesByModel: Map<String, List<ChatMessage>> = mapOf(),

  /** A map of model names to the currently streaming chat message. */
  val streamingMessagesByModel: Map<String, ChatMessage> = mapOf(),

  /*
   * A map of model names to a map of chat messages to a boolean indicating whether the message is
   * showing the stats below it.
   */
  val showingStatsByModel: Map<String, Set<ChatMessage>> = mapOf(),
)

/** ViewModel responsible for managing the chat UI state and handling chat-related operations. */
abstract class ChatViewModel(
  protected val conversationDao: ConversationDao,
  val analyticsTracker: AnalyticsTracker
) : ViewModel() {
  companion object {
    private const val TAG = "ChatViewModel"
  }

  protected var currentThreadId: Long? = null
  private val threadMutex = Mutex()


  private val _uiState = MutableStateFlow(createUiState())
  val uiState = _uiState.asStateFlow()

  /** Flow of conversation thread count for storage display */
  val conversationCount = conversationDao.getAllThreadsFlow().map { it.size }

  /** Flow of recent conversations for drawer quick access (last 10) */
  val recentConversations = conversationDao.getAllThreadsFlow().map { threads ->
    threads.sortedByDescending { it.updatedAt }.take(10)
  }

  fun addMessage(model: Model, message: ChatMessage) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      // Remove prompt template message if it is the current last message.
      if (newMessages.size > 0 && newMessages.last().type == ChatMessageType.PROMPT_TEMPLATES) {
        newMessages.removeAt(newMessages.size - 1)
      }
      newMessages.add(message)
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
    
    // Phase 2: Silent save to database (fire-and-forget)
    saveMessageToDatabase(model, message)
  }

  fun insertMessageAfter(model: Model, anchorMessage: ChatMessage, messageToAdd: ChatMessage) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      // Find the index of the anchor message
      val anchorIndex = newMessages.indexOf(anchorMessage)
      if (anchorIndex != -1) {
        // Insert the new message after the anchor message
        newMessages.add(anchorIndex + 1, messageToAdd)
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun removeMessageAt(model: Model, index: Int) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList()
      if (newMessages != null) {
        if (index >= 0 && index < newMessages.size) {
          newMessages.removeAt(index)
        }
        newMessagesByModel[model.name] = newMessages.toList()
      }
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun removeLastMessage(model: Model) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      if (newMessages.size > 0) {
        newMessages.removeAt(newMessages.size - 1)
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun clearAllMessages(model: Model) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      newMessagesByModel[model.name] = emptyList()
      current.copy(messagesByModel = newMessagesByModel)
    }
    currentThreadId = null // Reset thread ID for new conversation
  }

  /**
   * Load an existing conversation from database into the chat view.
   * This allows users to continue a saved conversation.
   */
  fun loadConversation(threadId: Long, model: Model) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val thread = conversationDao.getThreadById(threadId) ?: return@launch
        val messages = conversationDao.getMessagesForThread(threadId)

        // Set the current thread ID so new messages are added to this conversation
        threadMutex.withLock {
          currentThreadId = threadId
        }

        // Convert database messages to ChatMessages
        // Use latencyMs = 0f for agent messages to enable action buttons/disclaimer
        val chatMessages: List<ChatMessage> = messages.map { msg ->
          ChatMessageText(
            content = msg.content,
            side = if (msg.isUser) ChatSide.USER else ChatSide.AGENT,
            latencyMs = if (msg.isUser) -1f else 0f // Agent messages need >= 0 for UI actions
          ) as ChatMessage
        }

        // Update UI state with loaded messages
        _uiState.update { current ->
          val newMessagesByModel = current.messagesByModel.toMutableMap()
          newMessagesByModel[model.name] = chatMessages
          current.copy(messagesByModel = newMessagesByModel)
        }

        Log.d(TAG, "Loaded conversation $threadId with ${messages.size} messages")
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e(TAG, "Failed to load conversation", e)
      }
    }
  }

  fun getLastMessage(model: Model): ChatMessage? {
    return (_uiState.value.messagesByModel[model.name] ?: listOf()).lastOrNull()
  }

  fun updateLastTextMessageContentIncrementally(
    model: Model,
    partialContent: String,
    latencyMs: Float,
  ) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      if (newMessages.size > 0) {
        val lastMessage = newMessages.last()
        if (lastMessage is ChatMessageText) {
          val newContent = processLlmResponse(response = "${lastMessage.content}${partialContent}")
          val newLastMessage =
            ChatMessageText(
              content = newContent,
              side = lastMessage.side,
              latencyMs = latencyMs,
              accelerator = lastMessage.accelerator,
            )
          newMessages.removeAt(newMessages.size - 1)
          newMessages.add(newLastMessage)
        }
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun updateLastTextMessageLlmBenchmarkResult(
    model: Model,
    llmBenchmarkResult: ChatMessageBenchmarkLlmResult,
  ) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      if (newMessages.size > 0) {
        val lastMessage = newMessages.last()
        if (lastMessage is ChatMessageText) {
          lastMessage.llmBenchmarkResult = llmBenchmarkResult
          newMessages.removeAt(newMessages.size - 1)
          newMessages.add(lastMessage)
        }
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun replaceLastMessage(model: Model, message: ChatMessage, type: ChatMessageType) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      if (newMessages.size > 0) {
        val index = newMessages.indexOfLast { it.type == type }
        if (index >= 0) {
          newMessages[index] = message
        }
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun replaceMessage(model: Model, index: Int, message: ChatMessage) {
    _uiState.update { current ->
      val newMessagesByModel = current.messagesByModel.toMutableMap()
      val newMessages = newMessagesByModel[model.name]?.toMutableList() ?: mutableListOf()
      if (index >= 0 && index < newMessages.size) {
        newMessages[index] = message
      }
      newMessagesByModel[model.name] = newMessages.toList()
      current.copy(messagesByModel = newMessagesByModel)
    }
  }

  fun updateStreamingMessage(model: Model, message: ChatMessage) {
    _uiState.update { current ->
      val newStreamingMessagesByModel = current.streamingMessagesByModel.toMutableMap()
      newStreamingMessagesByModel[model.name] = message
      current.copy(streamingMessagesByModel = newStreamingMessagesByModel)
    }
  }

  fun setInProgress(inProgress: Boolean) {
    _uiState.update { it.copy(inProgress = inProgress) }
  }

  fun setIsResettingSession(isResettingSession: Boolean) {
    _uiState.update { it.copy(isResettingSession = isResettingSession) }
  }

  fun setPreparing(preparing: Boolean) {
    _uiState.update { it.copy(preparing = preparing) }
  }

  fun setIsCompacting(isCompacting: Boolean) {
    _uiState.update { it.copy(isCompacting = isCompacting) }
  }

  fun addConfigChangedMessage(
    oldConfigValues: Map<String, Any>,
    newConfigValues: Map<String, Any>,
    model: Model,
  ) {
    Log.d(TAG, "Adding config changed message. Old: ${oldConfigValues}, new: $newConfigValues")
    val message =
      ChatMessageConfigValuesChange(
        model = model,
        oldValues = oldConfigValues,
        newValues = newConfigValues,
      )
    addMessage(message = message, model = model)
  }

  fun getMessageIndex(model: Model, message: ChatMessage): Int {
    return (_uiState.value.messagesByModel[model.name] ?: listOf()).indexOf(message)
  }

  fun isShowingStats(model: Model, message: ChatMessage): Boolean {
    return _uiState.value.showingStatsByModel[model.name]?.contains(message) ?: false
  }

  fun toggleShowingStats(model: Model, message: ChatMessage) {
    _uiState.update { current ->
      val newShowingStatsByModel = current.showingStatsByModel.toMutableMap()
      val newShowingStats = newShowingStatsByModel[model.name]?.toMutableSet() ?: mutableSetOf()
      if (newShowingStats.contains(message)) {
        newShowingStats.remove(message)
      } else {
        newShowingStats.add(message)
      }
      newShowingStatsByModel[model.name] = newShowingStats.toSet()
      current.copy(showingStatsByModel = newShowingStatsByModel)
    }
  }

  private fun createUiState(): ChatUiState {
    return ChatUiState()
  }

  /**
   * Phase 2: Saves message to database without blocking UI.
   * Fire-and-forget pattern - failures are logged but don't affect chat.
   */
    private fun saveMessageToDatabase(model: Model, message: ChatMessage) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        // Only save text messages
        if (message !is ChatMessageText) {
          return@launch
        }
        
        // Get or create conversation thread (thread-safe with mutex)
        threadMutex.withLock {
          if (currentThreadId == null) {
            currentThreadId = conversationDao.insertThread(
              ConversationThread(
                title = message.content.take(50).ifEmpty { "New Conversation" },
                modelId = model.name,
                taskId = "chat",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
              )
            )
            Log.d(TAG, "Created new thread: $currentThreadId")
          }
        }
        
        // Save the message
        val threadId = currentThreadId ?: return@launch
        conversationDao.insertMessage(
          ConversationMessage(
            threadId = threadId,
            content = message.content,
            isUser = message.side == ChatSide.USER,
            timestamp = System.currentTimeMillis()
          )
        )

        Log.d(TAG, "Saved message to thread $threadId")
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.e(TAG, "Failed to save message to database", e)
      }
    }
  }

}
