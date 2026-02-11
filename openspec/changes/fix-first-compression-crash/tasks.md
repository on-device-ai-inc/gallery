# Tasks: fix-first-compression-crash

## Implementation Tasks

- [ ] **Task 1: Reset conversation BEFORE compression in LlmChatViewModel**
  - File: app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
  - Location: Before line 147 (checkAndCompact call)
  - Steps:
    1. Get existing conversation state and summary
    2. Format as Message with system prompt
    3. Call resetConversation with existingSummary, supportImage=false, supportAudio=false
  - Acceptance: Conversation KV-cache is cleared before summarization

- [ ] **Task 2: Keep existing reset AFTER compression**
  - File: app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
  - Location: Lines 154-174 (existing code)
  - Keep this reset to prepare for next user message
  - Acceptance: Conversation is reset with new summary after compression

## Testing Tasks

- [ ] Clear logcat: `adb logcat -c`
- [ ] Fresh conversation: Delete app data, reinstall
- [ ] Generate 35-40 messages to trigger first compression
- [ ] Monitor logs: `adb logcat | grep -E "Compaction|FATAL|SIGSEGV"`
- [ ] Verify: No crash, compression completes successfully
- [ ] Verify: Second compression also works
- [ ] Verify: Conversation continues normally

## Documentation Tasks

- [ ] Update LESSONS_LEARNED.md with pattern:
  - "Reset conversation before reusing for different task"
  - "Native LiteRT lib doesn't handle dirty KV-cache well"
