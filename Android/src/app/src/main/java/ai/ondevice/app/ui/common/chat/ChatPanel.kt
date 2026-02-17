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

import android.content.ClipData
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import android.content.Intent
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import ai.ondevice.app.R
import androidx.compose.ui.text.style.TextAlign
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.Task
import ai.ondevice.app.ui.common.AudioAnimation
import ai.ondevice.app.ui.common.ErrorDialog
import ai.ondevice.app.ui.common.copyToClipboard
import ai.ondevice.app.ui.common.chat.CompactingStatusChip
import ai.ondevice.app.ui.modelmanager.ModelInitializationStatusType
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.theme.customColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Composable function for the main chat panel, displaying messages and handling user input. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPanel(
  modelManagerViewModel: ModelManagerViewModel,
  task: Task,
  selectedModel: Model,
  viewModel: ChatViewModel,
  innerPadding: PaddingValues,
  onSendMessage: (Model, List<ChatMessage>) -> Unit,
  onRunAgainClicked: (Model, ChatMessage, RegenerateStyle) -> Unit,
  onBenchmarkClicked: (Model, ChatMessage, warmUpIterations: Int, benchmarkIterations: Int) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  onStreamImageMessage: (Model, ChatMessageImage) -> Unit = { _, _ -> },
  onStreamEnd: (Int) -> Unit = {},
  onStopButtonClicked: () -> Unit = {},
  onImageSelected: (bitmaps: List<Bitmap>, selectedBitmapIndex: Int) -> Unit = { _, _ -> },
  showStopButtonInInputWhenInProgress: Boolean = false,
) {
  val uiState by viewModel.uiState.collectAsState()
  val modelManagerUiState by modelManagerViewModel.uiState.collectAsState()
  val messages = uiState.messagesByModel[selectedModel.name] ?: listOf()
  val streamingMessage = uiState.streamingMessagesByModel[selectedModel.name]
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val haptic = LocalHapticFeedback.current
  val imageCountToLastConfigChange =
    remember(messages) {
      var imageCount = 0
      for (message in messages.reversed()) {
        if (message is ChatMessageConfigValuesChange) {
          break
        }
        if (message is ChatMessageImage) {
          imageCount += message.bitmaps.size
        }
      }
      imageCount
    }
  val audioClipMesssageCountToLastconfigChange =
    remember(messages) {
      var audioClipMessageCount = 0
      for (message in messages.reversed()) {
        if (message is ChatMessageConfigValuesChange) {
          break
        }
        if (message is ChatMessageAudioClip) {
          audioClipMessageCount++
        }
      }
      audioClipMessageCount
    }

  var curMessage by remember { mutableStateOf("") } // Correct state
  val focusManager = LocalFocusManager.current

  // Remember the LazyListState to control scrolling
  val listState = rememberLazyListState()
  val density = LocalDensity.current

  // Track if user has manually scrolled away from bottom
  var userHasScrolledUp by remember { mutableStateOf(false) }

  // Detect manual user scrolling
  LaunchedEffect(listState.isScrollInProgress) {
    if (listState.isScrollInProgress) {
      // User is scrolling - check if they're scrolling up
      val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
      if (lastVisibleItem != null && messages.isNotEmpty()) {
        val isAtBottom = lastVisibleItem.index >= messages.size - 1
        if (!isAtBottom) {
          userHasScrolledUp = true
        }
      }
    }
  }
  var showBenchmarkConfigsDialog by remember { mutableStateOf(false) }
  val benchmarkMessage: MutableState<ChatMessage?> = remember { mutableStateOf(null) }

  var showMessageLongPressedSheet by remember { mutableStateOf(false) }
  val longPressedMessage: MutableState<ChatMessage?> = remember { mutableStateOf(null) }
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  var showErrorDialog by remember { mutableStateOf(false) }

  var showAudioRecorder by remember { mutableStateOf(false) }
  var curAmplitude by remember { mutableIntStateOf(0) }

  // Keep track of the last message and last message content.
  val lastMessage: MutableState<ChatMessage?> = remember { mutableStateOf(null) }
  val lastMessageContent: MutableState<String> = remember { mutableStateOf("") }
  if (messages.isNotEmpty()) {
    val tmpLastMessage = messages.last()
    lastMessage.value = tmpLastMessage
    if (tmpLastMessage is ChatMessageText) {
      lastMessageContent.value = tmpLastMessage.content
    }
  }
  val lastShowingStatsByModel: MutableState<Map<String, MutableSet<ChatMessage>>> = remember {
    mutableStateOf(mapOf())
  }

  // Scroll to bottom when IME is toggled.
  LaunchedEffect(WindowInsets.ime.getBottom(density)) {
    scrollToBottom(listState = listState, animate = true)
  }

  // Scroll the content to the bottom when any of these changes.
  LaunchedEffect(
    messages.size,
    lastMessage.value,
    lastMessageContent.value,
    lastMessage.value?.latencyMs,
  ) {
    if (messages.isNotEmpty()) {
      val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
      if (lastVisibleItem != null) {
        // Check if user is already at or near the bottom
        val isNearBottom = lastVisibleItem.index >= messages.size - 1 ||
          (lastVisibleItem.offset + lastVisibleItem.size - listState.layoutInfo.viewportEndOffset < 90)

        // Reset userHasScrolledUp flag if they're back at bottom
        if (isNearBottom) {
          userHasScrolledUp = false
        }

        // Check if this is a long response (>2000 chars) - user should read from top
        val lastMsg = lastMessage.value
        val isLongResponse = lastMsg is ChatMessageText &&
                             lastMsg.content.length > 2000 &&
                             lastMsg.latencyMs > 0 // Just completed

        // Only auto-scroll if:
        // 1. User hasn't manually scrolled up, OR
        // 2. A new message was added (messages.size changed)
        // 3. NOT a long response completion (let user read from top)
        val shouldAutoScroll = (!userHasScrolledUp || isNearBottom) && !isLongResponse

        // Determines if an automatic scroll is necessary
        val canScroll =
          lastVisibleItem.index < messages.size - 1 ||
            lastVisibleItem.offset + lastVisibleItem.size - listState.layoutInfo.viewportEndOffset < 90

        // Only scroll if conditions are met
        if (shouldAutoScroll &&
            uiState.showingStatsByModel === lastShowingStatsByModel.value &&
            canScroll) {
          scrollToBottom(listState = listState, animate = true)
        }
      }
    }
    lastShowingStatsByModel.value = uiState.showingStatsByModel
  }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // If downward scroll, clear the focus from any currently focused composable.
        // This is useful for dismissing software keyboards or hiding text input fields
        // when the user starts scrolling down a list.
        if (available.y > 0) {
          focusManager.clearFocus()
        }
        // Let LazyColumn handle the scroll
        return Offset.Zero
      }
    }
  }

  val modelInitializationStatus = modelManagerUiState.modelInitializationStatus[selectedModel.name]

  LaunchedEffect(modelInitializationStatus) {
    showErrorDialog = modelInitializationStatus?.status == ModelInitializationStatusType.ERROR
  }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
    // Audio record animation.
    AnimatedVisibility(
      showAudioRecorder,
      enter =
        slideInVertically(
          animationSpec =
            spring(
              stiffness = Spring.StiffnessLow,
              visibilityThreshold = IntOffset.VisibilityThreshold,
            )
        ) {
          it
        } + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
      exit = fadeOut(),
      modifier = Modifier.graphicsLayer { alpha = 0.8f },
    ) {
      AudioAnimation(bgColor = MaterialTheme.colorScheme.surface, amplitude = curAmplitude)
    }

    Column(
      modifier = modifier.padding(innerPadding).consumeWindowInsets(innerPadding).imePadding()
    ) {
      Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.weight(1f)) {
        // Show greeting when empty, LazyColumn when has messages
        if (task.id == BuiltInTaskId.LLM_CHAT && messages.isEmpty()) {
          // Personalized greeting for empty chat
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Image(
              painter = painterResource(id = R.mipmap.ic_launcher_foreground),
              contentDescription = "OnDevice Logo",
              modifier = Modifier.size(160.dp).padding(bottom = 4.dp)
            )

            val greeting = remember {
              val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
              when (hour) {
                in 5..11 -> "morning"
                in 12..17 -> "afternoon"
                else -> "evening"
              }
            }

            Text(
              text = "How can I help you this $greeting?",
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        } else {
          LazyColumn(
          modifier =
            Modifier.fillMaxSize().nestedScroll(nestedScrollConnection).semantics {
              contentDescription = "Chat panel"
            },
          state = listState,
          verticalArrangement = Arrangement.Top,
        ) {
          itemsIndexed(messages) { index, message ->
            val imageHistoryCurIndex = remember { mutableIntStateOf(0) }
            var hAlign: Alignment.Horizontal = Alignment.End
            var backgroundColor: Color = MaterialTheme.customColors.userBubbleBgColor
            var hardCornerAtLeftOrRight = false
            var extraPaddingStart = 48.dp
            var extraPaddingEnd = 0.dp
            if (message.side == ChatSide.AGENT) {
              hAlign = Alignment.Start
              backgroundColor = MaterialTheme.customColors.agentBubbleBgColor
              hardCornerAtLeftOrRight = true
              extraPaddingStart = 0.dp
              extraPaddingEnd = 48.dp
            } else if (message.side == ChatSide.SYSTEM) {
              extraPaddingStart = 24.dp
              extraPaddingEnd = 24.dp
              if (message.type == ChatMessageType.PROMPT_TEMPLATES) {
                extraPaddingStart = 12.dp
                extraPaddingEnd = 12.dp
              }
            }
            if (message.type == ChatMessageType.IMAGE) {
              backgroundColor = Color.Transparent
            }
            val bubbleBorderRadius = dimensionResource(R.dimen.chat_bubble_corner_radius)

            Column(
              modifier =
                Modifier.fillMaxWidth()
                  .padding(
                    start = 12.dp + extraPaddingStart,
                    end = 12.dp + extraPaddingEnd,
                    top = 6.dp,
                    bottom = 6.dp,
                  ),
              horizontalAlignment = hAlign,
            ) messageColumn@{
              // Sender row.
              var agentName = stringResource(task.agentNameRes)
              MessageSender(
                message = message,
                agentName = agentName,
                imageHistoryCurIndex = imageHistoryCurIndex.intValue,
              )

              // Message body.
              when (message) {
                // Loading - replace with compacting indicator if compacting.
                is ChatMessageLoading -> {
                  if (uiState.isCompacting) {
                    CompactingStatusChip()
                  } else {
                    MessageBodyLoading()
                  }
                }

                // Long response status box.
                is ChatMessageLongResponseStatus -> {
                  LongResponseStatusBox(topic = message.topic)
                }

                // Info.
                is ChatMessageInfo -> MessageBodyInfo(message = message)

                // Warning
                is ChatMessageWarning -> MessageBodyWarning(message = message)

                // Config values change.
                is ChatMessageConfigValuesChange -> MessageBodyConfigUpdate(message = message)

                // Prompt templates.
                is ChatMessagePromptTemplates ->
                  MessageBodyPromptTemplates(
                    message = message,
                    task = task,
                    onPromptClicked = { template ->
                      onSendMessage(
                        selectedModel,
                        listOf(ChatMessageText(content = template.prompt, side = ChatSide.USER)),
                      )
                    },
                  )

                // Non-system messages.
                else -> {
                  // The bubble shape around the message body.
                  var messageBubbleModifier: Modifier = Modifier
                  // Use a rounded rectangle clip for multi-image image message.
                  if (message is ChatMessageImage && message.bitmaps.size > 1) {
                    messageBubbleModifier = messageBubbleModifier.clip(RoundedCornerShape(6.dp))
                  }
                  // For other messages, use a bubble shape to clip.
                  else {
                    messageBubbleModifier =
                      messageBubbleModifier.clip(
                        MessageBubbleShape(
                          radius = bubbleBorderRadius,
                          hardCornerAtLeftOrRight = hardCornerAtLeftOrRight,
                        )
                      )
                  }
                  messageBubbleModifier = messageBubbleModifier.background(backgroundColor)
                  if (message is ChatMessageText) {
                    messageBubbleModifier =
                      messageBubbleModifier.pointerInput(Unit) {
                        detectTapGestures(
                          onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            longPressedMessage.value = message
                            showMessageLongPressedSheet = true
                          }
                        )
                      }
                  }
                  Box(modifier = messageBubbleModifier) {
                    when (message) {
                      // Text
                      is ChatMessageText -> MessageBodyText(message = message)

                      // Image
                      is ChatMessageImage -> {
                        MessageBodyImage(message = message, onImageClicked = onImageSelected)
                      }

                      // Image with history (for image gen)
                      is ChatMessageImageWithHistory ->
                        MessageBodyImageWithHistory(
                          message = message,
                          imageHistoryCurIndex = imageHistoryCurIndex,
                        )

                      // Audio clip.
                      is ChatMessageAudioClip -> MessageBodyAudioClip(message = message)

                      // Classification result
                      is ChatMessageClassification ->
                        MessageBodyClassification(
                          message = message,
                          modifier =
                            Modifier.width(message.maxBarWidth ?: CLASSIFICATION_BAR_MAX_WIDTH),
                        )

                      // Benchmark result.
                      is ChatMessageBenchmarkResult -> MessageBodyBenchmark(message = message)

                      // Benchmark LLM result.
                      is ChatMessageBenchmarkLlmResult ->
                        MessageBodyBenchmarkLlm(
                          message = message,
                          modifier = Modifier.wrapContentWidth(),
                        )

                      else -> {}
                    }
                  }

                  if (message.side == ChatSide.AGENT) {
                    // Action buttons row (icon-only: Copy, Regenerate, Share, Play)
                    if (message is ChatMessageText && message.latencyMs >= 0) {
                      val context = LocalContext.current
                      var showShareSheet by remember { mutableStateOf(false) }
                      var isPlaying by remember { mutableStateOf(false) }
                      var tts by remember { mutableStateOf<TextToSpeech?>(null) }

                      // Initialize TTS
                      DisposableEffect(Unit) {
                        tts = TextToSpeech(context) { status ->
                          if (status == TextToSpeech.SUCCESS) {
                            tts?.language = Locale.getDefault()
                          }
                        }
                        onDispose {
                          tts?.stop()
                          tts?.shutdown()
                        }
                      }

                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                      ) {
                        // Copy button (icon-only)
                        IconButton(
                          onClick = { copyToClipboard(context, message.content, "AI Response") },
                          modifier = Modifier.size(36.dp)
                        ) {
                          Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }

                        // Regenerate button (icon-only)
                        val messageIndex = viewModel.getMessageIndex(model = selectedModel, message = message)
                        val previousUserMessage = if (messageIndex > 0) {
                          messages.take(messageIndex).lastOrNull {
                            it is ChatMessageText && it.side == ChatSide.USER
                          }
                        } else null
                        if (previousUserMessage != null) {
                          var showRegenerateMenu by remember { mutableStateOf(false) }

                          IconButton(
                            onClick = { showRegenerateMenu = true },
                            enabled = !uiState.inProgress,
                            modifier = Modifier.size(36.dp)
                          ) {
                            Icon(
                              Icons.Rounded.Refresh,
                              contentDescription = "Regenerate",
                              modifier = Modifier.size(18.dp),
                              tint = if (!uiState.inProgress) MaterialTheme.colorScheme.onSurfaceVariant
                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                          }

                          if (showRegenerateMenu) {
                            RegenerateMenu(
                              onDismiss = { showRegenerateMenu = false },
                              onStyleSelected = { style ->
                                onRunAgainClicked(selectedModel, previousUserMessage, style)
                                showRegenerateMenu = false
                              }
                            )
                          }
                        }

                        // Share button (icon-only)
                        IconButton(
                          onClick = { showShareSheet = true },
                          modifier = Modifier.size(36.dp)
                        ) {
                          Icon(
                            Icons.Rounded.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }

                        // Play button (TTS - icon-only)
                        IconButton(
                          onClick = {
                            if (isPlaying) {
                              tts?.stop()
                              isPlaying = false
                            } else {
                              tts?.speak(message.content, TextToSpeech.QUEUE_FLUSH, null, "response_tts")
                              isPlaying = true
                            }
                          },
                          modifier = Modifier.size(36.dp)
                        ) {
                          Icon(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                      }

                      // Share bottom sheet
                      if (showShareSheet) {
                        var shareOption by remember { mutableStateOf("response") }
                        ModalBottomSheet(
                          onDismissRequest = { showShareSheet = false }
                        ) {
                          Column(
                            modifier = Modifier.padding(16.dp)
                          ) {
                            Text(
                              "Share as Markdown",
                              style = MaterialTheme.typography.titleMedium,
                              modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shareOption = "response" }
                                .padding(vertical = 12.dp)
                            ) {
                              RadioButton(
                                selected = shareOption == "response",
                                onClick = { shareOption = "response" }
                              )
                              Text("Last response only", modifier = Modifier.padding(start = 8.dp))
                            }

                            Row(
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shareOption = "conversation" }
                                .padding(vertical = 12.dp)
                            ) {
                              RadioButton(
                                selected = shareOption == "conversation",
                                onClick = { shareOption = "conversation" }
                              )
                              Text("Entire conversation", modifier = Modifier.padding(start = 8.dp))
                            }

                            Button(
                              onClick = {
                                val shareText = if (shareOption == "response") {
                                  "**AI Response:**\n\n${message.content}"
                                } else {
                                  messages.filterIsInstance<ChatMessageText>().joinToString("\n\n") { msg ->
                                    if (msg.side == ChatSide.USER) "**You:** ${msg.content}"
                                    else "**AI:** ${msg.content}"
                                  }
                                }
                                val sendIntent = Intent().apply {
                                  action = Intent.ACTION_SEND
                                  putExtra(Intent.EXTRA_TEXT, shareText)
                                  type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                                showShareSheet = false
                              },
                              modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                            ) {
                              Text("Share")
                            }
                          }
                        }
                      }
                    }
                  } else if (message.side == ChatSide.USER) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      // Run again button.
                      if (selectedModel.showRunAgainButton) {
                        MessageActionButton(
                          label = stringResource(R.string.run_again),
                          icon = Icons.Rounded.Refresh,
                          onClick = { onRunAgainClicked(selectedModel, message, RegenerateStyle.STANDARD) },
                          enabled = !uiState.inProgress,
                        )
                      }

                      // Benchmark button
                      if (selectedModel.showBenchmarkButton) {
                        MessageActionButton(
                          label = stringResource(R.string.benchmark),
                          icon = Icons.Outlined.Timer,
                          onClick = {
                            showBenchmarkConfigsDialog = true
                            benchmarkMessage.value = message
                          },
                          enabled = !uiState.inProgress,
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          // Show disclaimer below the most recent AI response only
          // Only when: last message is from AI, not currently generating, and messages exist
          if (messages.isNotEmpty() &&
              lastMessage.value?.side == ChatSide.AGENT &&
              lastMessage.value?.type == ChatMessageType.TEXT &&
              !uiState.inProgress) {
            item {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(start = 12.dp, end = 60.dp, top = 0.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.Start
              ) {
                MessageDisclaimerRow()
              }
            }
          }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(vertical = 4.dp))

        // Show an info message for ask image task to get users started.
        if (task.id == BuiltInTaskId.LLM_ASK_IMAGE && messages.isEmpty()) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            MessageBodyInfo(
              ChatMessageInfo(
                content =
                  "To get started, click + below to add images (up to 10 in a single session) and type a prompt to ask a question about it."
              ),
              smallFontSize = false,
            )
          }
        }
        // Show an info message for ask audio task to get users started.
        else if (task.id == BuiltInTaskId.LLM_ASK_AUDIO && messages.isEmpty()) {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            MessageBodyInfo(
              ChatMessageInfo(
                content =
                  "To get started, tap the + icon to add your audio clip. Limited to 1 clip up to 30 seconds long."
              ),
              smallFontSize = false,
            )
          }
        }
      }

      } // end Box

      Surface(
        shadowElevation = 8.dp,
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
      Column {
      MessageInputText(
        task = task,
        modelManagerViewModel = modelManagerViewModel,
        curMessage = curMessage,
        inProgress = uiState.inProgress,
        isResettingSession = uiState.isResettingSession,
        modelPreparing = uiState.preparing,
        imageCount = imageCountToLastConfigChange,
        audioClipMessageCount = audioClipMesssageCountToLastconfigChange,
        modelInitializing =
          modelInitializationStatus?.status == ModelInitializationStatusType.INITIALIZING,
        textFieldPlaceHolderRes = task.textInputPlaceHolderRes,
        onValueChanged = { curMessage = it },
        onSendMessage = {
          onSendMessage(selectedModel, it)
          curMessage = ""
          // Hide software keyboard.
          focusManager.clearFocus()
        },
        onOpenPromptTemplatesClicked = {
          onSendMessage(
            selectedModel,
            listOf(
              ChatMessagePromptTemplates(
                templates = selectedModel.llmPromptTemplates,
                showMakeYourOwn = false,
              )
            ),
          )
        },
        onStopButtonClicked = onStopButtonClicked,
        onSetAudioRecorderVisible = { start ->
          showAudioRecorder = start
          if (!showAudioRecorder) {
            curAmplitude = 0
          }
        },
        onAmplitudeChanged = { curAmplitude = it },
        showPromptTemplatesInMenu = false,
        showImagePickerInMenu =
          selectedModel.llmSupportImage,  // Show for any model with image support
        showAudioItemsInMenu =
          selectedModel.llmSupportAudio,  // Show for any model with audio support
        showStopButtonWhenInProgress = showStopButtonInInputWhenInProgress,
      )
      } // end Column
      } // end Surface
    } // close parent container
  }

  // Error dialog.
  if (showErrorDialog) {
    ErrorDialog(
      error = modelInitializationStatus?.error ?: "",
      onDismiss = { showErrorDialog = false },
    )
  }

  // Benchmark config dialog.
  if (showBenchmarkConfigsDialog) {
    BenchmarkConfigDialog(
      onDismissed = { showBenchmarkConfigsDialog = false },
      messageToBenchmark = benchmarkMessage.value,
      onBenchmarkClicked = { message, warmUpIterations, benchmarkIterations ->
        onBenchmarkClicked(selectedModel, message, warmUpIterations, benchmarkIterations)
      },
    )
  }

  // Sheet to show when a message is long-pressed.
  if (showMessageLongPressedSheet) {
    val message = longPressedMessage.value
    if (message != null && message is ChatMessageText) {
      val clipboard = LocalClipboard.current
      val context = LocalContext.current

      ModalBottomSheet(
        onDismissRequest = { showMessageLongPressedSheet = false },
        modifier = Modifier.wrapContentHeight(),
      ) {
        Column {
          // Copy text.
          Box(
            modifier =
              Modifier.fillMaxWidth().clickable {
                // Copy text.
                scope.launch {
                  val clipData = ClipData.newPlainText("message content", message.content)
                  val clipEntry = ClipEntry(clipData = clipData)
                  clipboard.setClipEntry(clipEntry = clipEntry)
                }

                // Hide sheet.
                showMessageLongPressedSheet = false

                // Show a snack bar.
                scope.launch { snackbarHostState.showSnackbar("Text copied to clipboard") }
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
              Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = "",
                modifier = Modifier.size(18.dp),
              )
              Text("Copy text")
            }
          }

          // Share text.
          Box(
            modifier =
              Modifier.fillMaxWidth().clickable {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, message.content)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share message"))
                showMessageLongPressedSheet = false
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
              Icon(
                Icons.Rounded.Share,
                contentDescription = "",
                modifier = Modifier.size(18.dp),
              )
              Text("Share")
            }
          }

          // Delete message.
          Box(
            modifier =
              Modifier.fillMaxWidth().clickable {
                showMessageLongPressedSheet = false
                showDeleteConfirmDialog = true
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
              Icon(
                Icons.Rounded.Delete,
                contentDescription = "",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
              )
              Text("Delete", color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }

  // Delete confirmation dialog
  if (showDeleteConfirmDialog) {
    val message = longPressedMessage.value
    if (message != null) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showDeleteConfirmDialog = false },
        title = { Text("Delete message?") },
        text = { Text("This message will be removed from this chat.") },
        confirmButton = {
          androidx.compose.material3.TextButton(
            onClick = {
              val messageIndex = viewModel.getMessageIndex(model = selectedModel, message = message)
              if (messageIndex >= 0) {
                viewModel.removeMessageAt(model = selectedModel, index = messageIndex)
              }
              showDeleteConfirmDialog = false
              longPressedMessage.value = null
              scope.launch { snackbarHostState.showSnackbar("Message deleted") }
            }
          ) {
            Text("Delete", color = MaterialTheme.colorScheme.error)
          }
        },
        dismissButton = {
          androidx.compose.material3.TextButton(
            onClick = { showDeleteConfirmDialog = false }
          ) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

private suspend fun scrollToBottom(listState: LazyListState, animate: Boolean = false) {
  val itemCount = listState.layoutInfo.totalItemsCount
  if (itemCount > 0) {
    if (animate) {
      listState.animateScrollToItem(itemCount - 1, scrollOffset = 10000)
    } else {
      listState.scrollToItem(itemCount - 1, scrollOffset = 10000)
    }
  }
}
