# Proposal: fix-first-compression-crash

## Summary
Fix native lib crash on first compression by resetting conversation BEFORE summarization instead of after.

## Motivation
First compression crashes in `liblitertlm_jni.so` (native LiteRT library) because we're reusing a "dirty" conversation object that has existing KV-cache from 40 messages. The native lib can't handle using the same conversation for a different task (summarization) without resetting first.

Subsequent compressions work because we call `resetConversation()` AFTER each successful compression, leaving the conversation in a clean state for the next compression.

## Root Cause
**Conversation state pollution:**
- First compression: Conversation has 40 messages in KV-cache → try to use for summarization → native SEGFAULT
- After success OR restart: `resetConversation()` clears state → subsequent compressions work

## Scope

### In Scope
- Reset conversation BEFORE calling `summarizeAsync()` to ensure clean state
- Preserve existing summary when resetting for summarization
- After summarization succeeds, reset again with new summary

### Out of Scope
- Fixing the underlying LiteRT native library bug (requires Google fix)
- Creating separate model instance for summarization
- Changing compression thresholds

## Acceptance Criteria
- [ ] First compression completes without crash
- [ ] Subsequent compressions continue to work
- [ ] Summary quality remains the same (no degradation)
- [ ] Conversation continues normally after compression
- [ ] Manual test: Fresh conversation, generate 35-40 messages, verify no crash on first compression

## Technical Approach

### Current Flow (Buggy)
```kotlin
// LlmChatViewModel.kt line 147-174
val compactionResult = compactionManager.checkAndCompact(...)  // Calls summarizeAsync() with dirty conversation
if (compactionResult is Success) {
  resetConversation(systemMessage = summary)  // Too late! Already crashed
}
```

### Fixed Flow
```kotlin
// BEFORE compression: Reset to clean state
LlmChatModelHelper.resetConversation(
  model = model,
  supportImage = false,  // Summarization doesn't need images
  supportAudio = false,  // Summarization doesn't need audio
  systemMessage = existingSummaryMessage  // Preserve context
)

// NOW safe to use for summarization
val compactionResult = compactionManager.checkAndCompact(...)

// AFTER compression: Reset with new summary for user chat
if (compactionResult is Success) {
  LlmChatModelHelper.resetConversation(
    model = model,
    supportImage = model.llmSupportImage,
    supportAudio = model.llmSupportAudio,
    systemMessage = newSummaryMessage
  )
}
```

### Implementation Details

**Step 1: Get existing summary before compression**
```kotlin
val existingState = conversationDao.getConversationState(threadId)
val existingSummaryMessage = if (existingState != null && existingState.runningSummary.isNotBlank()) {
  Message.of(listOf(Content.Text("Previous conversation summary:\n${existingState.runningSummary}")))
} else {
  null
}
```

**Step 2: Reset conversation to clean state**
```kotlin
LlmChatModelHelper.resetConversation(
  model = model,
  supportImage = false,  // Summarization is text-only
  supportAudio = false,
  systemMessage = existingSummaryMessage
)
```

**Step 3: Run compression (now safe)**
```kotlin
val compactionResult = compactionManager.checkAndCompact(...)
```

**Step 4: After success, reset again with new summary**
```kotlin
if (compactionResult is CompactionResult.Success) {
  val state = conversationDao.getConversationState(threadId)
  val newSummaryMessage = if (state != null && state.runningSummary.isNotBlank()) {
    Message.of(listOf(Content.Text("Previous conversation summary:\n${state.runningSummary}")))
  } else {
    null
  }

  LlmChatModelHelper.resetConversation(
    model = model,
    supportImage = model.llmSupportImage,
    supportAudio = model.llmSupportAudio,
    systemMessage = newSummaryMessage
  )
}
```

## Why This Works

1. **First compression**: Reset conversation → clean KV-cache → summarization succeeds → reset with summary
2. **Second compression**: Conversation already clean from previous reset → summarization succeeds → reset with summary
3. **Every compression**: Always work with clean conversation state

## Trade-offs

**Pro:**
- Fixes the crash without waiting for Google to fix LiteRT
- Minimal code changes
- No impact on summary quality

**Con:**
- Two resets per compression (one before, one after) - adds ~100ms overhead
- Loses conversation context during summarization (but we provide existing summary as system message)

## References
- Crash logs: SIGSEGV in liblitertlm_jni.so at pc 0x87cd20
- User report: "app still crashing on first compression"
- Root cause: Conversation state pollution - reusing dirty KV-cache
- Related: LlmChatViewModel.kt:128-183, CompactionManager.kt:101
