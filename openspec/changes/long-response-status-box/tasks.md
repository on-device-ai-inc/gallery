# Tasks: long-response-status-box

## Implementation Tasks

- [ ] **Task 1: Create ChatMessageLongResponseStatus data class**
  - File: app/src/main/java/ai/ondevice/app/ui/common/chat/ChatMessage.kt
  - Add new message type: LONG_RESPONSE_STATUS
  - Fields: topic (String), side (ChatSide), latencyMs (Float)
  - Acceptance: Compiles, can be added to message list

- [ ] **Task 2: Create LongResponseStatusBox composable**
  - File: app/src/main/java/ai/ondevice/app/ui/common/chat/LongResponseStatusBox.kt (NEW)
  - Similar design to CompactingStatusChip
  - Document icon + topic text
  - Pulsing animation, grey background, full width with padding
  - Acceptance: Renders correctly in preview

- [ ] **Task 3: Add helper functions for detection**
  - File: app/src/main/java/ai/ondevice/app/ui/llmchat/LongResponseDetector.kt (NEW)
  - detectLongResponse(firstChunk: String): Boolean
  - extractFirstSentence(text: String): String
  - extractTopic(text: String): String
  - Acceptance: Unit tests pass for various inputs

- [ ] **Task 4: Integrate detection in generateResponse()**
  - File: app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
  - On firstRun: check if long response
  - If yes: show summary + status box, skip streaming
  - If no: continue normal streaming
  - On done: if long response, replace status box with full text
  - Acceptance: Long responses show status box, short responses stream normally

- [ ] **Task 5: Add message rendering in ChatPanel**
  - File: app/src/main/java/ai/ondevice/app/ui/common/chat/ChatPanel.kt
  - Add case for ChatMessageLongResponseStatus
  - Render LongResponseStatusBox component
  - Acceptance: Status box appears in chat correctly

## Testing Tasks

- [ ] Unit tests: LongResponseDetector with various inputs
  - "I'll create a comprehensive thesis" → true
  - "Here's a quick answer" → false
  - Extract topic correctly from different phrasings

- [ ] Manual test: Ask for PhD thesis
  - Verify brief summary appears first
  - Verify status box shows with topic
  - Verify full response replaces status box when done

- [ ] Manual test: Ask short question
  - Verify normal streaming behavior (no status box)

- [ ] Visual test: Status box matches compacting box style
  - Grey background, pulsing, centered, full width

## Documentation Tasks

- [ ] Update LESSONS_LEARNED.md with pattern:
  - Conditional UX based on response content detection
  - Reusing design patterns (CompactingStatusChip → LongResponseStatusBox)
