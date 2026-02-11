# Spec Delta: long-response-status-box

## ADDED

### app/src/main/java/ai/ondevice/app/ui/common/chat/ChatMessage.kt
```kotlin
// New message type enum value
enum class ChatMessageType {
  // ... existing types
  LONG_RESPONSE_STATUS,  // NEW: Status box for long responses
}

// New data class
data class ChatMessageLongResponseStatus(
  val topic: String,
  override val side: ChatSide = ChatSide.AGENT,
  override val latencyMs: Float = -1f
) : ChatMessage() {
  override val type = ChatMessageType.LONG_RESPONSE_STATUS
}
```

### app/src/main/java/ai/ondevice/app/ui/common/chat/LongResponseStatusBox.kt (NEW FILE)
```kotlin
@Composable
fun LongResponseStatusBox(topic: String) {
  // Pulsating animation
  val infiniteTransition = rememberInfiniteTransition(label = "long-response-pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha-pulse"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .alpha(alpha)
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 16.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Description,
        contentDescription = "Generating document",
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = topic,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
```

### app/src/main/java/ai/ondevice/app/ui/llmchat/LongResponseDetector.kt (NEW FILE)
```kotlin
object LongResponseDetector {
  private val LONG_RESPONSE_KEYWORDS = listOf(
    "comprehensive", "detailed", "thesis", "extensive",
    "in-depth", "thorough", "I'll create", "I'll write",
    "I'll develop", "scholarly", "academic", "complete guide"
  )

  fun detectLongResponse(firstChunk: String): Boolean {
    return LONG_RESPONSE_KEYWORDS.any {
      firstChunk.contains(it, ignoreCase = true)
    }
  }

  fun extractFirstSentence(text: String): String {
    val firstSentence = text.split(". ", ".\n").firstOrNull() ?: text
    return if (firstSentence.length > 150) {
      firstSentence.take(150) + "..."
    } else {
      firstSentence
    }
  }

  fun extractTopic(text: String): String {
    // Pattern 1: "create/write a [TYPE] on [SUBJECT]"
    val pattern1 = Regex(
      "(?:create|write|develop)\\s+a\\s+(.+?)\\s+(?:on|about)\\s+(.+?)(?:\\.|,|$)",
      RegexOption.IGNORE_CASE
    )
    pattern1.find(text)?.let { match ->
      val type = match.groupValues[1].trim()
      val subject = match.groupValues[2].trim().take(50)
      return "Creating $type on $subject"
    }

    // Pattern 2: "[ADJECTIVE] [TYPE] about [SUBJECT]"
    val pattern2 = Regex(
      "(comprehensive|detailed|thorough)\\s+(.+?)\\s+(?:on|about)\\s+(.+?)(?:\\.|,|$)",
      RegexOption.IGNORE_CASE
    )
    pattern2.find(text)?.let { match ->
      val type = match.groupValues[2].trim()
      val subject = match.groupValues[3].trim().take(50)
      return "Creating $type on $subject"
    }

    // Fallback: use first 60 chars
    return "Creating " + text.take(60).trim() + "..."
  }
}
```

## MODIFIED

### app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
```diff
   fun generateResponse(...) {
     viewModelScope.launch(Dispatchers.Default) {
       // ... existing code ...

       var firstRun = true
+      var isLongResponse = false
+      var briefSummary = ""
+      var accumulatedResponse = ""

       LlmChatModelHelper.runInference(
         resultListener = { partialResult, done ->
           if (firstRun) {
+            // Check if this will be a long response
+            val firstChunk = partialResult.take(200)
+            isLongResponse = LongResponseDetector.detectLongResponse(firstChunk)
+
+            if (isLongResponse) {
+              // Extract brief summary and topic
+              briefSummary = LongResponseDetector.extractFirstSentence(firstChunk)
+              val topic = LongResponseDetector.extractTopic(firstChunk)
+
+              // Remove loading indicator
+              val lastMessage = getLastMessage(model = model)
+              if (lastMessage?.type == ChatMessageType.LOADING) {
+                removeLastMessage(model = model)
+              }
+
+              // Add brief summary
+              addMessage(
+                model = model,
+                message = ChatMessageText(
+                  content = briefSummary,
+                  side = ChatSide.AGENT,
+                  accelerator = accelerator
+                )
+              )
+
+              // Add status box
+              addMessage(
+                model = model,
+                message = ChatMessageLongResponseStatus(topic = topic)
+              )
+            }

             firstTokenTs = System.currentTimeMillis()
             timeToFirstToken = (firstTokenTs - start) / 1000f
             prefillSpeed = prefillTokens / timeToFirstToken
             firstRun = false
             setPreparing(false)
-          } else {
+          }
+
+          if (isLongResponse) {
+            // Accumulate full response, don't stream
+            accumulatedResponse += partialResult
+          } else if (!firstRun) {
             decodeTokens++
+
+            // Normal streaming: Remove loading, add empty message
+            val lastMessage = getLastMessage(model = model)
+            if (lastMessage?.type == ChatMessageType.LOADING) {
+              removeLastMessage(model = model)
+              addMessage(
+                model = model,
+                message = ChatMessageText(content = "", side = ChatSide.AGENT, accelerator = accelerator)
+              )
+            }
+
+            // Stream incrementally
+            updateLastTextMessageContentIncrementally(
+              model = model,
+              partialContent = partialResult,
+              latencyMs = if (done) (System.currentTimeMillis() - start).toFloat() else -1f
+            )
           }

-          // Remove loading indicator if needed
-          val lastMessage = getLastMessage(model = model)
-          if (lastMessage?.type == ChatMessageType.LOADING) {
-            removeLastMessage(model = model)
-            addMessage(...)
-          }
-
-          // Update streaming content
-          updateLastTextMessageContentIncrementally(...)

           if (done) {
+            if (isLongResponse) {
+              // Replace status box with full response
+              removeLastMessage(model = model)  // Remove status box
+              addMessage(
+                model = model,
+                message = ChatMessageText(
+                  content = accumulatedResponse,
+                  side = ChatSide.AGENT,
+                  latencyMs = (System.currentTimeMillis() - start).toFloat(),
+                  accelerator = accelerator
+                )
+              )
+            }
+
             setInProgress(false)
             // ... existing benchmark code ...
           }
         }
       )
     }
   }
```

### app/src/main/java/ai/ondevice/app/ui/common/chat/ChatPanel.kt
```diff
   when (message) {
     is ChatMessageLoading -> {
       if (uiState.isCompacting) {
         CompactingStatusChip()
       } else {
         MessageBodyLoading()
       }
     }
+
+    // Long response status indicator
+    is ChatMessageLongResponseStatus -> {
+      LongResponseStatusBox(topic = message.topic)
+    }

     is ChatMessageInfo -> MessageBodyInfo(message = message)
     // ... other cases ...
   }
```

## REMOVED
None

## Rationale

**Why keyword detection?**
Works with existing responses without modifying prompts. Can be tuned by adding/removing keywords.

**Why show brief summary + status box?**
User knows immediately what's being generated. Status box provides clear progress indication during 2-3 minute wait.

**Why accumulate instead of stream?**
Avoids rapid scrolling that prevents reading. After completion, full text is available for reading at user's pace.

**Why reuse CompactingStatusChip design?**
Consistent UX pattern - user already familiar with grey pulsing boxes for background operations.
