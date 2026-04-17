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

import ai.ondevice.app.R
import ai.ondevice.app.data.AnalyticsTracker
import ai.ondevice.app.data.ConversationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Message
import ai.ondevice.app.data.ConfigKeys
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.ModelRuntimeStateManager
import ai.ondevice.app.data.Task
import ai.ondevice.app.ui.common.chat.ChatMessageAudioClip
import ai.ondevice.app.ui.common.chat.ChatMessageBenchmarkLlmResult
import ai.ondevice.app.ui.common.chat.ChatMessageImage
import ai.ondevice.app.ui.common.chat.ChatMessageLoading
import ai.ondevice.app.ui.common.chat.ChatMessageLongResponseStatus
import ai.ondevice.app.ui.common.chat.ChatMessageText
import ai.ondevice.app.ui.common.chat.ChatMessageType
import ai.ondevice.app.ui.common.chat.ChatMessageWarning
import ai.ondevice.app.ui.common.chat.ChatSide
import ai.ondevice.app.ui.common.chat.ChatViewModel
import ai.ondevice.app.ui.common.chat.Stat
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ai.ondevice.app.firebaseAnalytics
import androidx.core.os.bundleOf

private const val TAG = "AGLlmChatViewModel"
private val STATS =
  listOf(
    Stat(id = "time_to_first_token", label = "1st token", unit = "sec"),
    Stat(id = "prefill_speed", label = "Prefill speed", unit = "tokens/s"),
    Stat(id = "decode_speed", label = "Decode speed", unit = "tokens/s"),
    Stat(id = "latency", label = "Latency", unit = "sec"),
  )

open class LlmChatViewModelBase(
  conversationDao: ConversationDao,
  analyticsTracker: AnalyticsTracker,
  private val personaManager: ai.ondevice.app.persona.PersonaManager? = null
) : ChatViewModel(conversationDao, analyticsTracker) {

  // Context compression (lazy initialization)
  private val compactionManager by lazy {
    ai.ondevice.app.conversation.CompactionManager(conversationDao)
  }

  fun generateResponse(
    model: Model,
    input: String,
    images: List<Bitmap> = listOf(),
    audioMessages: List<ChatMessageAudioClip> = listOf(),
    onError: () -> Unit,
  ) {
    val accelerator = model.getStringConfigValue(key = ConfigKeys.ACCELERATOR, defaultValue = "")

    viewModelScope.launch(Dispatchers.Default) {
      // Track chat message sent
      firebaseAnalytics?.logEvent(
        "chat_message_sent",
        bundleOf(
          "model_name" to model.name,
          "has_images" to (images.isNotEmpty()),
          "has_audio" to (audioMessages.isNotEmpty())
        )
      )

      setInProgress(true)
      setPreparing(true)

      // Save user images if provided
      if (images.isNotEmpty()) {
        val imageBitmaps = images.map { it.asImageBitmap() }
        val imageMessage = ChatMessageImage(
          bitmaps = images,
          imageBitMaps = imageBitmaps,
          side = ChatSide.USER,
          latencyMs = -1f
        )
        addMessage(model = model, message = imageMessage)
      }

      // Detect if user is requesting a long response (thesis, essay, comprehensive guide, etc.)
      Log.d(TAG, "🔍 DETECTION DEBUG: Input prompt = \"$input\"")
      val isLongRequest = LongResponseDetector.detectLongRequest(input)
      Log.d(TAG, "🔍 DETECTION DEBUG: isLongRequest = $isLongRequest")

      // Show appropriate status indicator
      if (isLongRequest) {
        // Long response detected - show status box with topic
        // For follow-up elaboration requests, provide conversation context
        val recentAgentMessages = uiState.value.messagesByModel[model.name]
          ?.filter { it.side == ChatSide.AGENT && it is ChatMessageText }
          ?.takeLast(3) // Last 3 agent messages for context
          ?.map { (it as ChatMessageText).content }
          ?: emptyList()

        val topic = LongResponseDetector.extractTopicFromUserPrompt(input, recentAgentMessages)
        Log.d(TAG, "🔍 DETECTION DEBUG: Long request detected! Topic = \"$topic\"")
        Log.d(TAG, "🔍 DETECTION DEBUG: Context messages: ${recentAgentMessages.size}")
        val statusMessage = ChatMessageLongResponseStatus(
          topic = topic,
          side = ChatSide.AGENT,
          latencyMs = -1f
        )
        addMessage(model = model, message = statusMessage)
      } else {
        // Normal response - show loading indicator
        Log.d(TAG, "🔍 DETECTION DEBUG: Short request, using loading indicator")
        addMessage(model = model, message = ChatMessageLoading(accelerator = accelerator))
      }

      // Wait for instance to be initialized (with 30s timeout).
      // Capture it locally right after the polling loop so a concurrent
      // cleanup/reset can't null it out from under us.
      val initDeadline = System.currentTimeMillis() + 30_000L
      var capturedInstance: LlmModelInstance? = null
      while (capturedInstance == null) {
        capturedInstance = ModelRuntimeStateManager.getValue(model.name).instance as? LlmModelInstance
        if (capturedInstance != null) break
        if (System.currentTimeMillis() > initDeadline) {
          Log.e(TAG, "Model initialization timed out after 30s for ${model.name}")
          setInProgress(false)
          setPreparing(false)
          onError()
          return@launch
        }
        delay(100)
      }
      delay(500)

      // Prompt Engineering: Inject persona FIRST on first message
      var enhancedInput = input
      if (personaManager != null) {
        try {
          // Get all messages for this model to check if this is the first user message
          val existingMessages = uiState.value.messagesByModel[model.name] ?: listOf()
          val userMessageCount = existingMessages.count { it.side == ChatSide.USER }

          // For Gemma (no system role support), prepend persona to first user message
          if (userMessageCount == 0) {
            enhancedInput = personaManager.formatSingleMessageWithPersona(
              input,
              ai.ondevice.app.persona.PersonaVariant.BALANCED
            )
          }
        } catch (e: Exception) {
          if (e is CancellationException) throw e
          Log.w(TAG, "Failed to inject persona, using input as-is", e)
        }
      }

      // NOTE: Token limit check now happens BEFORE loading indicator (lines 92-146)
      // This prevents showing rotating icon when resetting

      // Context Compression: Check if compaction needed
      try {
        val threadId = currentThreadId
        if (threadId != null) {
          val dbMessages = conversationDao.getMessagesForThread(threadId)

          // Check token count first (fast operation)
          val currentTokens = ai.ondevice.app.conversation.TokenEstimator.estimate(dbMessages)
          val triggerThreshold = (ai.ondevice.app.conversation.CompactionManager.MAX_TOKENS *
                                  ai.ondevice.app.conversation.CompactionManager.TRIGGER_PERCENT).toInt()

          if (currentTokens >= triggerThreshold) {
            // Show compacting indicator BEFORE starting slow operation
            setIsCompacting(true)
            // Small delay to ensure UI renders the indicator
            delay(100)

            Log.d(TAG, "Starting compaction: $currentTokens tokens (threshold: $triggerThreshold)")

            // CRITICAL FIX: Reset conversation BEFORE summarization to clear KV-cache
            // This prevents native lib crash when reusing conversation with large state (e.g., PhD thesis response)
            // The summarization gets context from the TEXT PROMPT (database messages), not from KV-cache
            val existingState = conversationDao.getConversationState(threadId)
            val existingSummaryMessage = if (existingState != null && existingState.runningSummary.isNotBlank()) {
              val formattedSummary = "Previous conversation summary:\n${existingState.runningSummary}"
              Message.of(listOf(Content.Text(formattedSummary)))
            } else {
              null
            }

            Log.d(TAG, "Resetting conversation BEFORE summarization to clear KV-cache")
            LlmChatModelHelper.resetConversation(
              model = model,
              supportImage = false,  // Summarization is text-only
              supportAudio = false,
              systemMessage = existingSummaryMessage
            )

            val compactionResult = compactionManager.checkAndCompact(
              threadId,
              dbMessages,
              LlmChatModelHelper,
              model
            )

            if (compactionResult is ai.ondevice.app.conversation.CompactionResult.Success) {
              Log.d(TAG, "Conversation compacted: evicted ${compactionResult.evictedCount} turns")

              // CRITICAL: Reset conversation with summary as system message
              val state = conversationDao.getConversationState(threadId)
              val summaryMessage = if (state != null && state.runningSummary.isNotBlank()) {
                // Format summary for LiteRT system message
                val formattedSummary = "Previous conversation summary:\n${state.runningSummary}"
                Message.of(listOf(Content.Text(formattedSummary)))
              } else {
                null
              }

              Log.d(TAG, "Resetting conversation AFTER compression with new summary (${state?.runningSummary?.length ?: 0} chars)")
              LlmChatModelHelper.resetConversation(
                model = model,
                supportImage = model.llmSupportImage,
                supportAudio = model.llmSupportAudio,
                systemMessage = summaryMessage
              )
            }

            // Hide compacting indicator after completion
            setIsCompacting(false)
          }
        }
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        firebaseAnalytics?.logEvent(
          "error_occurred",
          bundleOf(
            "error_type" to e::class.simpleName,
            "source_class" to "LlmChatViewModel",
            "error_message" to e.message.orEmpty()
          )
        )
        Log.w(TAG, "Compaction check failed, continuing without compression", e)
        setIsCompacting(false)  // Ensure indicator is hidden on error
      }

      // Run inference using the instance captured right after the polling loop.
      // This survives concurrent cleanup/reset races.
      val instance = capturedInstance
      // Note: sizeInTokens() not available in LiteRT-LM API, using estimation
      var prefillTokens = input.split(" ").size
      prefillTokens += images.size * 257
      val audioClips: MutableList<ByteArray> = mutableListOf()
      for (audioMessage in audioMessages) {
        audioClips.add(audioMessage.genByteArrayForWav())
        // 150ms = 1 audio token
        val duration = audioMessage.getDurationInSeconds()
        prefillTokens += (duration * 1000f / 150f).toInt()
      }

      var firstRun = true
      var timeToFirstToken = 0f
      var firstTokenTs = 0L
      var decodeTokens = 0
      var prefillSpeed = 0f
      var decodeSpeed: Float
      val start = System.currentTimeMillis()

      // Use the same detection result computed above to keep status box consistent.
      val isLongResponse = isLongRequest
      var accumulatedResponse = ""

      val trace = Firebase.performance.newTrace("llm_inference")
      trace.putAttribute("model_name", model.name)
      trace.start()

      try {
        LlmChatModelHelper.runInference(
          model = model,
          input = enhancedInput,  // Use persona-enhanced input for first message
          images = images,
          audioClips = audioClips,
          resultListener = { partialResult, done ->
            val curTs = System.currentTimeMillis()

            if (firstRun) {
              firstTokenTs = System.currentTimeMillis()
              timeToFirstToken = (firstTokenTs - start) / 1000f
              prefillSpeed = prefillTokens / timeToFirstToken
              firstRun = false
              setPreparing(false)
              // Record TTFT metric
              trace.putMetric("ttft_ms", (firstTokenTs - start))
            }

            if (isLongResponse) {
              // For long responses: accumulate full response, don't stream
              accumulatedResponse += partialResult
            } else {
              // Normal streaming behavior
              decodeTokens++

              // Remove the last message if it is a "loading" or "long response status" message.
              // This will only be done once.
              val lastMessage = getLastMessage(model = model)
              if (lastMessage?.type == ChatMessageType.LOADING ||
                  lastMessage?.type == ChatMessageType.LONG_RESPONSE_STATUS) {
                removeLastMessage(model = model)

                // Add an empty message that will receive streaming results.
                addMessage(
                  model = model,
                  message =
                    ChatMessageText(content = "", side = ChatSide.AGENT, accelerator = accelerator),
                )
              }

              // Incrementally update the streamed partial results.
              val latencyMs: Long = if (done) System.currentTimeMillis() - start else -1
              updateLastTextMessageContentIncrementally(
                model = model,
                partialContent = partialResult,
                latencyMs = latencyMs.toFloat(),
              )
            }

            if (done) {
              // For long responses: replace status box with full response
              if (isLongResponse) {
                removeLastMessage(model = model)  // Remove status box
                addMessage(
                  model = model,
                  message = ChatMessageText(
                    content = accumulatedResponse,
                    side = ChatSide.AGENT,
                    latencyMs = (curTs - start).toFloat(),
                    accelerator = accelerator
                  )
                )
                Log.d(TAG, "Long response completed: ${accumulatedResponse.length} chars")
              }

              setInProgress(false)

              decodeSpeed = decodeTokens / ((curTs - firstTokenTs) / 1000f)
              if (decodeSpeed.isNaN()) {
                decodeSpeed = 0f
              }

              // Track chat message received
              val responseTokenCount = if (isLongResponse) {
                accumulatedResponse.split(" ").size
              } else {
                decodeTokens
              }
              trace.putMetric("total_tokens", responseTokenCount.toLong())
              trace.stop()
              firebaseAnalytics?.logEvent(
                "chat_message_received",
                bundleOf(
                  "model_name" to model.name,
                  "token_count" to responseTokenCount,
                  "latency_ms" to (curTs - start)
                )
              )

              val lastMessageForBenchmark = getLastMessage(model = model)
              if (lastMessageForBenchmark is ChatMessageText) {
                updateLastTextMessageLlmBenchmarkResult(
                  model = model,
                  llmBenchmarkResult =
                    ChatMessageBenchmarkLlmResult(
                      orderedStats = STATS,
                      statValues =
                        mutableMapOf(
                          "prefill_speed" to prefillSpeed,
                          "decode_speed" to decodeSpeed,
                          "time_to_first_token" to timeToFirstToken,
                          "latency" to (curTs - start).toFloat() / 1000f,
                        ),
                      running = false,
                      latencyMs = -1f,
                      accelerator = accelerator,
                    ),
                )
              }

              // CRITICAL FIX: Save final agent message to database
              // The initial empty message was saved, but streaming updates were only in UI
              val finalMessage = getLastMessage(model = model)
              if (finalMessage is ChatMessageText && finalMessage.side == ChatSide.AGENT) {
                viewModelScope.launch(Dispatchers.IO) {
                  try {
                    // Update the message in database with final content
                    val threadId = currentThreadId
                    if (threadId != null) {
                      // Find the last agent message in DB and update it
                      val dbMessages = conversationDao.getMessagesForThread(threadId)
                      val lastAgentMessage = dbMessages.lastOrNull { !it.isUser }
                      if (lastAgentMessage != null) {
                        conversationDao.updateMessageContent(
                          lastAgentMessage.id,
                          finalMessage.content
                        )
                        Log.d(TAG, "Updated agent message in DB: ${finalMessage.content.take(50)}...")
                      }
                    }
                  } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    firebaseAnalytics?.logEvent(
                      "error_occurred",
                      bundleOf(
                        "error_type" to e::class.simpleName,
                        "source_class" to "LlmChatViewModel",
                        "error_message" to e.message.orEmpty()
                      )
                    )
                    Log.e(TAG, "Failed to update agent message in database", e)
                  }
                }
              }
            }
          },
          cleanUpListener = {
            setInProgress(false)
            setPreparing(false)
          },
          onError = { message ->
            trace.stop()
            // Handle error
          }
        )
      } catch (e: Exception) {
        trace.stop()
        if (e is CancellationException) throw e
        firebaseAnalytics?.logEvent(
          "error_occurred",
          bundleOf(
            "error_type" to e::class.simpleName,
            "source_class" to "LlmChatViewModel",
            "error_message" to e.message.orEmpty()
          )
        )
        Log.e(TAG, "Error occurred while running inference", e)
        setInProgress(false)
        setPreparing(false)
        onError()
      }
    }
  }

  fun stopResponse(model: Model) {
    Log.d(TAG, "Stopping response for model ${model.name}...")

    val lastMessage = getLastMessage(model = model)
    if (lastMessage is ChatMessageLoading || lastMessage is ChatMessageLongResponseStatus) {
      removeLastMessage(model = model)
    }
    viewModelScope.launch(Dispatchers.Default) {
      setInProgress(false)
      setPreparing(false)
      // Null-safe cast to prevent crash if model instance is not initialized
      val instance = ModelRuntimeStateManager.getValue(model.name).instance as? LlmModelInstance
      if (instance != null) {
        try {
          instance.conversation.cancelProcess()
        } catch (e: IllegalStateException) {
          // Conversation already stopped - this is fine during reset
          Log.d(TAG, "Conversation already stopped for ${model.name}")
        }
      } else {
        Log.w(TAG, "Cannot stop response - model instance is null for ${model.name}")
      }
    }
  }

  fun resetSession(task: Task, model: Model) {
    viewModelScope.launch(Dispatchers.Default) {
      setIsResettingSession(true)
      clearAllMessages(model = model)
      stopResponse(model = model)

      val resetDeadline = System.currentTimeMillis() + 30_000L
      var resetSucceeded = false
      while (!resetSucceeded) {
        try {
          // Enable features based on model capabilities, not task ID
          val supportImage = model.llmSupportImage
          val supportAudio = model.llmSupportAudio
          LlmChatModelHelper.resetConversation(
            model = model,
            supportImage = supportImage,
            supportAudio = supportAudio,
          )
          resetSucceeded = true
        } catch (e: Exception) {
          if (e is CancellationException) throw e
          if (System.currentTimeMillis() > resetDeadline) {
            Log.e(TAG, "Reset conversation timed out after 30s for ${model.name}", e)
            break
          }
          Log.d(TAG, "Failed to reset conversation. Trying again")
        }
        delay(200)
      }
      setIsResettingSession(false)
    }
  }

  fun runAgain(
    model: Model,
    message: ChatMessageText,
    style: ai.ondevice.app.ui.common.chat.RegenerateStyle = ai.ondevice.app.ui.common.chat.RegenerateStyle.STANDARD,
    onError: () -> Unit
  ) {
    viewModelScope.launch(Dispatchers.Default) {
      // Wait for model to be initialized (with 30s timeout).
      // Capture instance locally so a concurrent cleanup can't null it between
      // the polling loop exit and the generateResponse call below.
      val initDeadline = System.currentTimeMillis() + 30_000L
      var capturedForRunAgain: LlmModelInstance? = null
      while (capturedForRunAgain == null) {
        capturedForRunAgain = ModelRuntimeStateManager.getValue(model.name).instance as? LlmModelInstance
        if (capturedForRunAgain != null) break
        if (System.currentTimeMillis() > initDeadline) {
          Log.e(TAG, "Model initialization timed out after 30s for ${model.name} during runAgain")
          onError()
          return@launch
        }
        delay(100)
      }

      // Remove the last assistant response that we're regenerating.
      // This prevents duplicate user messages in chat history.
      val lastMessage = getLastMessage(model = model)
      if (lastMessage != null && lastMessage.side == ChatSide.AGENT) {
        removeLastMessage(model = model)
      }

      // Modify prompt based on regeneration style
      val modifiedInput = when (style) {
        ai.ondevice.app.ui.common.chat.RegenerateStyle.FASTER ->
          "${message.content}\n\n[Respond concisely and quickly]"
        ai.ondevice.app.ui.common.chat.RegenerateStyle.MORE_DETAILED ->
          "${message.content}\n\n[Provide a detailed, thorough response with examples]"
        ai.ondevice.app.ui.common.chat.RegenerateStyle.DIFFERENT ->
          "${message.content}\n\n[Take a different approach or perspective]"
        ai.ondevice.app.ui.common.chat.RegenerateStyle.SHORTER ->
          "${message.content}\n\n[Respond in 2-3 sentences maximum]"
        ai.ondevice.app.ui.common.chat.RegenerateStyle.STANDARD ->
          message.content
      }

      // Don't clone and re-add the user message - it's already in the chat!
      // Run inference with the modified input.
      generateResponse(model = model, input = modifiedInput, onError = onError)
    }
  }

  fun handleError(
    context: Context,
    task: Task,
    model: Model,
    modelManagerViewModel: ModelManagerViewModel,
    triggeredMessage: ChatMessageText?,
  ) {
    // Clean up.
    modelManagerViewModel.cleanupModel(context = context, task = task, model = model)

    // Remove the "loading" or "long response status" message.
    val cleanupMessage = getLastMessage(model = model)
    if (cleanupMessage is ChatMessageLoading || cleanupMessage is ChatMessageLongResponseStatus) {
      removeLastMessage(model = model)
    }

    // Remove the last Text message.
    if (getLastMessage(model = model) == triggeredMessage) {
      removeLastMessage(model = model)
    }

    // Add a warning message for re-initializing the session.
    addMessage(
      model = model,
      message = ChatMessageWarning(
        content = context.getString(R.string.error_inference_crashed) + "\n" +
          context.getString(R.string.error_inference_crashed_detail)
      ),
    )

    // Add the triggered message back.
    if (triggeredMessage != null) {
      addMessage(model = model, message = triggeredMessage)
    }

    // Re-initialize the session/engine.
    modelManagerViewModel.initializeModel(context = context, task = task, model = model)

    // Re-generate the response automatically.
    if (triggeredMessage != null) {
      generateResponse(
        model = model,
        input = triggeredMessage.content,
        onError = {
          // Surface the failure rather than silently swallowing it.
          addMessage(
            model = model,
            message = ChatMessageWarning(
              content = context.getString(R.string.error_inference_crashed)
            )
          )
        }
      )
    }
  }
}

@HiltViewModel class LlmChatViewModel @Inject constructor(
  conversationDao: ConversationDao,
  analyticsTracker: AnalyticsTracker,
  personaManager: ai.ondevice.app.persona.PersonaManager
) : LlmChatViewModelBase(conversationDao, analyticsTracker, personaManager)

@HiltViewModel class LlmAskImageViewModel @Inject constructor(
  conversationDao: ConversationDao,
  analyticsTracker: AnalyticsTracker,
  personaManager: ai.ondevice.app.persona.PersonaManager
) : LlmChatViewModelBase(conversationDao, analyticsTracker, personaManager)

@HiltViewModel class LlmAskAudioViewModel @Inject constructor(
  conversationDao: ConversationDao,
  analyticsTracker: AnalyticsTracker,
  personaManager: ai.ondevice.app.persona.PersonaManager
) : LlmChatViewModelBase(conversationDao, analyticsTracker, personaManager)
