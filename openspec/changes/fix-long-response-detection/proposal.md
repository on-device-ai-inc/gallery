# Proposal: Fix Long Response Detection

## Summary
Fix the broken long response detection feature by checking the user's input prompt for request patterns instead of waiting for LLM response tokens, eliminating timing issues and improving reliability.

## Motivation
**Current Implementation is Broken:**
- Checks LLM response for keywords ("comprehensive", "detailed", "thesis")
- Only checks FIRST token (e.g., "I" or "Here")
- Keyword arrives too late (token 3-8) after `firstRun = false`
- Feature never activates → responses stream normally
- No status box shown for long responses

**User Experience Impact:**
- User asks: "Write a thesis on AI"
- Expected: Brief summary + status box + full response after completion
- Actual: Response streams word-by-word (slow for 3000-word thesis)

**Root Cause:**
Timing issue - detection happens too early on first token before keywords appear.

**Solution:**
Check user's INPUT prompt BEFORE inference starts:
- "Write a thesis" → Detect immediately
- "Create a comprehensive guide" → Detect immediately
- Set `isLongResponse = true` BEFORE streaming begins
- Show status box right away

## Scope

### IN SCOPE
- Add `detectLongRequest(userPrompt: String)` function to LongResponseDetector
- Check user prompt BEFORE `runInference()` call in LlmChatViewModel
- Remove broken LLM response detection logic from streaming callback
- Add comprehensive keyword patterns for user prompts
- Add false positive filtering (avoid detecting questions like "What is a thesis?")
- Update topic extraction to work with user prompts
- Keep existing status box UI (no UI changes needed)

### OUT OF SCOPE
- Changing status box UI design
- Adding new UI components
- Modifying streaming behavior (that part works correctly)
- Changing accumulation logic
- Adding LLM-based intent detection

## Acceptance Criteria
- [ ] User asks "Write a thesis on AI" → Status box appears immediately
- [ ] User asks "Create a comprehensive guide" → Status box appears
- [ ] User asks "What is a thesis?" → NO status box (false positive filtered)
- [ ] User asks "Tell me about AI" → NO status box (normal short response)
- [ ] Status box shows correct topic extracted from user prompt
- [ ] No logs showing "Long response detected" from old LLM response check
- [ ] CI passes (lint + tests)
- [ ] Visual verification on device confirms feature works

## Technical Approach

### 1. Add User Prompt Detection Function

**File**: `LongResponseDetector.kt`

Add new function:
```kotlin
/**
 * Detects if user is requesting a long response based on their prompt.
 * Checks BEFORE inference starts (not during streaming).
 */
fun detectLongRequest(userPrompt: String): Boolean {
  if (!ENABLE_LONG_RESPONSE_STATUS) return false

  // Filter out questions (avoid false positives)
  val trimmed = userPrompt.trim()
  if (trimmed.startsWith("what ", ignoreCase = true) ||
      trimmed.startsWith("how ", ignoreCase = true) ||
      trimmed.startsWith("why ", ignoreCase = true) ||
      trimmed.startsWith("when ", ignoreCase = true) ||
      trimmed.startsWith("where ", ignoreCase = true)) {
    return false
  }

  // Check for action verbs + content types
  val actionPatterns = Regex(
    "\\b(write|create|generate|compose|develop|produce|draft|prepare)\\s+(a|an)?\\s*(thesis|essay|paper|guide|tutorial|article|report|document|analysis|study)",
    RegexOption.IGNORE_CASE
  )

  // Check for length modifiers
  val lengthKeywords = listOf(
    "detailed", "comprehensive", "complete", "full",
    "in-depth", "thorough", "extensive", "elaborate"
  )

  // Check for explicit instructions
  val explicitPatterns = Regex(
    "in detail|step by step|explain everything|tell me everything|give me (a|an) (full|complete|comprehensive)",
    RegexOption.IGNORE_CASE
  )

  return actionPatterns.containsMatchIn(userPrompt) ||
         lengthKeywords.any { userPrompt.contains(it, ignoreCase = true) } ||
         explicitPatterns.containsMatchIn(userPrompt)
}
```

### 2. Add User Prompt Topic Extraction

**File**: `LongResponseDetector.kt`

Add new function:
```kotlin
/**
 * Extracts topic from user's request prompt.
 */
fun extractTopicFromUserPrompt(userPrompt: String): String {
  // Pattern: "write a [TYPE] on [TOPIC]"
  val pattern1 = Regex(
    "(?:write|create|generate|develop)\\s+(?:a|an)?\\s*(.+?)\\s+(?:on|about|for)\\s+(.+)",
    RegexOption.IGNORE_CASE
  )

  pattern1.find(userPrompt)?.let { match ->
    val type = match.groupValues[1].trim()
    val topic = match.groupValues[2].trim().removeSuffix(".").removeSuffix("?")
    return "Creating $type on $topic"
  }

  // Pattern: "[ADJECTIVE] [TYPE] about [TOPIC]"
  val pattern2 = Regex(
    "(detailed|comprehensive|thorough)\\s+(.+?)\\s+(?:on|about|for)\\s+(.+)",
    RegexOption.IGNORE_CASE
  )

  pattern2.find(userPrompt)?.let { match ->
    val type = match.groupValues[2].trim()
    val topic = match.groupValues[3].trim().removeSuffix(".").removeSuffix("?")
    return "Creating $type on $topic"
  }

  // Fallback: Use first 60 chars of prompt
  return "Processing request: " + userPrompt.take(60).trim() + "..."
}
```

### 3. Move Detection to Before Inference

**File**: `LlmChatViewModel.kt`

**REMOVE** broken detection from streaming callback (lines 239-277):
```kotlin
// DELETE THIS BLOCK (currently at lines 239-277)
if (firstRun) {
  // ... old detection logic
  val firstChunk = partialResult.take(200)
  isLongResponse = LongResponseDetector.detectLongResponse(firstChunk)
  // ...
}
```

**ADD** detection before inference (around line 218):
```kotlin
// AFTER line 218: var firstRun = true

// Detect long response from USER PROMPT (not LLM response)
var isLongResponse = LongResponseDetector.detectLongRequest(input)
var accumulatedResponse = ""

if (isLongResponse) {
  // Extract topic from user's prompt
  val topic = LongResponseDetector.extractTopicFromUserPrompt(input)

  // Remove loading indicator
  val lastMessage = getLastMessage(model = model)
  if (lastMessage?.type == ChatMessageType.LOADING) {
    removeLastMessage(model = model)
  }

  // Show status box immediately (before inference starts)
  addMessage(
    model = model,
    message = ChatMessageLongResponseStatus(topic = topic)
  )

  Log.d(TAG, "Long response detected from user prompt: $topic")
}

// Continue with inference...
try {
  LlmChatModelHelper.runInference(...)
}
```

### 4. Simplify Streaming Callback

**File**: `LlmChatViewModel.kt`

The streaming callback becomes simpler (no detection logic):
```kotlin
resultListener = { partialResult, done ->
  if (firstRun) {
    firstTokenTs = System.currentTimeMillis()
    timeToFirstToken = (firstTokenTs - start) / 1000f
    prefillSpeed = prefillTokens / timeToFirstToken
    firstRun = false
    setPreparing(false)
  }

  // Simple branching: accumulate or stream
  if (isLongResponse) {
    accumulatedResponse += partialResult
  } else {
    updateLastTextMessageContentIncrementally(...)
  }

  // On completion
  if (done) {
    if (isLongResponse) {
      removeLastMessage(model = model)  // Remove status box
      addMessage(ChatMessageText(accumulatedResponse, ...))
    }
    // ... rest of completion logic
  }
}
```

### 5. Update Tests

**File**: `LongResponseDetectorTest.kt` (new)

Add unit tests:
```kotlin
@Test
fun detectLongRequest_thesis_returnsTrue() {
  assertTrue(LongResponseDetector.detectLongRequest("Write a thesis on AI"))
}

@Test
fun detectLongRequest_question_returnsFalse() {
  assertFalse(LongResponseDetector.detectLongRequest("What is a thesis?"))
}

@Test
fun detectLongRequest_detailed_returnsTrue() {
  assertTrue(LongResponseDetector.detectLongRequest("Give me a detailed explanation"))
}

@Test
fun extractTopic_parsesCorrectly() {
  val topic = LongResponseDetector.extractTopicFromUserPrompt(
    "Write a thesis on artificial intelligence"
  )
  assertEquals("Creating thesis on artificial intelligence", topic)
}
```

## Impact Analysis

### Files to Modify
1. **LongResponseDetector.kt** - Add `detectLongRequest()` and `extractTopicFromUserPrompt()`
2. **LlmChatViewModel.kt** - Move detection before inference, remove from callback
3. **LongResponseDetectorTest.kt** (new) - Add unit tests

### Lines Changed
- **Added**: ~80 lines (new functions + tests)
- **Removed**: ~40 lines (old broken detection logic)
- **Modified**: ~20 lines (refactor streaming callback)
- **Net**: +60 lines

### Breaking Changes
None - feature currently doesn't work, so fixing it has no breaking impact.

### Risk Assessment
- **Low risk**: Fixing broken feature
- **No UI changes**: Reuses existing status box component
- **Backward compatible**: Feature flag can disable if needed
- **Easy rollback**: Keep old function as fallback

## References
- Debug analysis: See conversation above (forensic analysis of timing issue)
- Current implementation: Commits 9bb2cd0 and c3189e0
- Status box UI: `LongResponseStatusBox.kt` (already working)
- Similar pattern: Persona detection in `PersonaManager` (checks input before inference)

## Success Metrics
- [ ] Test prompt "Write a thesis on AI" triggers status box immediately
- [ ] No "Long response detected" logs from old callback-based detection
- [ ] Zero timing-related failures
- [ ] False positive rate < 5% (questions don't trigger)
- [ ] False negative rate < 10% (catches most long requests)
- [ ] CI green (all tests pass)

## Follow-Up Work
After this fix, consider:
- Add more sophisticated intent detection (ML-based)
- Support other languages (currently English-only)
- Add user preference to disable feature
- Collect analytics on detection accuracy
