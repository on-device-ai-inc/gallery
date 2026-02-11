# Proposal: long-response-status-box

## Summary
Add conditional status box for long responses: show brief summary + progress box instead of streaming full text.

## Motivation
When LLM generates long responses (PhD thesis, detailed analysis), streaming thousands of tokens creates poor UX:
- User can't read while it's streaming (scrolls too fast)
- Takes 2-3 minutes to complete
- No clear indication of progress or topic

Better UX from user screenshot:
- Brief summary upfront: "Now I'll create a comprehensive PhD-level thesis..."
- Status box below: "📄 Creating a PhD-level thesis on artificial intelligence..."
- Similar to compaction box design
- After completion, show full response

## Scope

### In Scope
- Detect when response will be long (heuristic: first 50 tokens mention "detailed", "comprehensive", "thesis", etc.)
- Show brief summary (first 1-2 sentences from LLM)
- Replace streaming with status box showing topic
- After completion, replace status box with full response
- Status box design matches CompactingStatusChip (grey background, pulsing, centered)

### Out of Scope
- Progress percentage (LLM doesn't provide this)
- User cancellation of long responses (separate feature)
- Streaming short responses (keep current behavior)
- Estimating response length before generation

## Acceptance Criteria
- [ ] When LLM starts with "I'll create...", "comprehensive", "detailed", etc., trigger long-response mode
- [ ] Show first 1-2 sentences as brief summary
- [ ] Show status box with topic (e.g., "Creating a PhD-level thesis on artificial intelligence")
- [ ] Status box has document icon, pulsing animation, grey background
- [ ] After completion, replace status box with full text
- [ ] Short responses continue to stream normally (no change)
- [ ] Manual test: Ask for PhD thesis, verify status box appears with topic

## Technical Approach

### Detection Strategy

**Option 1: Keyword detection in first 50 tokens**
```kotlin
val firstChunk = partialResult.take(200)  // ~50 tokens
val longResponseKeywords = listOf(
  "comprehensive", "detailed", "thesis", "extensive",
  "in-depth", "thorough", "I'll create", "I'll write"
)
val isLongResponse = longResponseKeywords.any {
  firstChunk.contains(it, ignoreCase = true)
}
```

**Option 2: LLM self-declaration**
- Modify persona/prompt to include: "If generating >500 tokens, start with: [LONG]"
- Detect `[LONG]` prefix in first token

**Recommended: Option 1** (no prompt changes, works with existing responses)

### Implementation Flow

```kotlin
// In LlmChatViewModel.kt generateResponse()

var isLongResponse = false
var briefSummary = ""
var topicLine = ""

resultListener = { partialResult, done ->
  if (firstRun) {
    // Check if this looks like a long response
    val firstChunk = partialResult.take(200)
    isLongResponse = detectLongResponse(firstChunk)

    if (isLongResponse) {
      // Extract first sentence as brief summary
      briefSummary = extractFirstSentence(firstChunk)
      topicLine = extractTopic(firstChunk)  // e.g., "PhD-level thesis on artificial intelligence"

      // Replace loading indicator with status box
      removeLastMessage(model)
      addMessage(model, ChatMessageText(briefSummary, ...))
      addMessage(model, ChatMessageLongResponseStatus(topic = topicLine))

      // Don't stream incremental updates
      return@resultListener
    }
  }

  if (done && isLongResponse) {
    // Replace status box with full response
    removeLastMessage(model)  // Remove status box
    addMessage(model, ChatMessageText(fullResponse, ...))
  } else if (!isLongResponse) {
    // Normal streaming behavior
    updateLastTextMessageContentIncrementally(...)
  }
}
```

### New Message Type

```kotlin
// ChatMessage.kt
data class ChatMessageLongResponseStatus(
  val topic: String,
  override val side: ChatSide = ChatSide.AGENT,
  override val latencyMs: Float = -1f
) : ChatMessage() {
  override val type = ChatMessageType.LONG_RESPONSE_STATUS
}
```

### New UI Component

```kotlin
// LongResponseStatusBox.kt
@Composable
fun LongResponseStatusBox(topic: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .alpha(alpha)  // Pulsing animation
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
      Icon(Icons.Default.Description, contentDescription = "Document")
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        topic,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
```

### Helper Functions

```kotlin
fun detectLongResponse(firstChunk: String): Boolean {
  val keywords = listOf(
    "comprehensive", "detailed", "thesis", "extensive",
    "in-depth", "thorough", "I'll create", "I'll write",
    "I'll develop", "scholarly", "academic"
  )
  return keywords.any { firstChunk.contains(it, ignoreCase = true) }
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
  // Try to extract topic from patterns like:
  // "I'll create a [TOPIC] on [SUBJECT]"
  // "comprehensive [TOPIC] about [SUBJECT]"

  val patterns = listOf(
    Regex("(?:create|write|develop)\\s+a\\s+(.+?)\\s+on\\s+(.+?)(?:\\.|,|$)", RegexOption.IGNORE_CASE),
    Regex("comprehensive\\s+(.+?)\\s+about\\s+(.+?)(?:\\.|,|$)", RegexOption.IGNORE_CASE)
  )

  for (pattern in patterns) {
    val match = pattern.find(text)
    if (match != null) {
      val type = match.groupValues[1].trim()
      val subject = match.groupValues[2].trim().take(50)
      return "Creating $type on $subject"
    }
  }

  // Fallback: use first 50 chars
  return "Creating " + text.take(50).trim() + "..."
}
```

## Design Decisions

**Why keyword detection?**
- Works with existing prompts
- No need to modify persona
- Can be tuned with more keywords

**Why show brief summary + status box?**
- User knows what's being generated
- Clear progress indication
- Reduces anxiety from long wait

**Why replace after completion?**
- Clean transition
- User can read full response when ready
- No need to scroll back

## Trade-offs

**Pro:**
- Better UX for long responses
- Clear progress indication
- Reuses CompactingStatusChip design pattern

**Con:**
- Keyword detection may have false positives (could show status box for short "comprehensive" answers)
- Requires testing to tune keywords
- Adds complexity to streaming logic

## Alternatives Considered

1. **Always show status box for any response** - Too disruptive for short answers
2. **Show progress percentage** - LLM doesn't provide this info
3. **Token count threshold (>500 tokens)** - Can't detect until after generation

## References
- User screenshot showing desired UX
- Similar pattern: CompactingStatusChip (grey box, pulsing, centered)
- Related: ChatMessageLoading (rotating icon during generation)
