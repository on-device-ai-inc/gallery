# Proposal: fix-compaction-ux-bugs

## Summary
Fix three critical bugs in context compression UX: indicator positioning, first-compression crash, and color scheme.

## Motivation
Manual testing revealed three issues:
1. **Indicator positioning**: CompactingStatusChip appears at top of chat instead of replacing the rotating icon during compression
2. **First-compression crash**: App crashes on first compression attempt, subsequent compressions work
3. **Color scheme**: Green background (tertiaryContainer) is too prominent, needs subtle grey contrast

## Scope

### In Scope
- Replace rotating icon with compacting indicator when both compressing AND generating
- Fix database API call causing first-compression crash (deleteMessagesForThread vs deleteMessage)
- Change CompactingStatusChip color from tertiaryContainer to surfaceVariant for subtle grey

### Out of Scope
- Changing compaction thresholds or logic
- Adding progress percentage to indicator
- Modifying token estimation algorithm

## Acceptance Criteria
- [ ] When compression runs during generation, user sees "Compacting conversation..." instead of rotating icon
- [ ] When compression runs between messages (edge case), indicator appears as continuation in chat
- [ ] First compression completes without crash
- [ ] Compacting indicator has subtle grey background in both dark/light themes
- [ ] Manual test: Generate 30-40 messages, verify smooth compression UX on first and subsequent compressions

## Technical Approach

### Bug #1: Indicator Positioning
**Current flow:**
```
LazyColumn:
  - if (uiState.isCompacting) { item { CompactingStatusChip() } }
  - itemsIndexed(messages) { ... ChatMessageLoading ... }
```

**Problem**: CompactingStatusChip is added as separate item at top, ChatMessageLoading still shows

**Solution**: Conditional rendering in ChatPanel
- When `isCompacting && inProgress`: Show CompactingStatusChip in place of ChatMessageLoading
- When `!isCompacting && inProgress`: Show ChatMessageLoading (rotating icon)
- Logic location: ChatPanel.kt where messages are rendered

**Implementation:**
```kotlin
// In ChatPanel.kt LazyColumn
itemsIndexed(messages) { index, message ->
  if (message is ChatMessageLoading) {
    // Replace with compacting chip if compacting
    if (uiState.isCompacting) {
      CompactingStatusChip()
    } else {
      ChatMessage(message = message, ...)
    }
  } else {
    ChatMessage(message = message, ...)
  }
}
```

### Bug #2: First-Compression Crash
**Root cause**: Line 119 in CompactionManager.kt
```kotlin
conversationDao.deleteMessagesForThread(msg.id)  // WRONG: expects threadId, gets messageId
```

**Fix**:
```kotlin
conversationDao.deleteMessage(msg.id)  // CORRECT: delete single message by ID
```

**Alternative approach** (if deleteMessage doesn't exist): Check ConversationDao API and use correct method

### Bug #3: Color Scheme
**Current**: `MaterialTheme.colorScheme.tertiaryContainer` (green)
**Target**: `MaterialTheme.colorScheme.surfaceVariant` (subtle grey, theme-aware)

**Change location**: CompactingStatusChip.kt line 69

## References
- User bug report: "compression dialog appears at the very top of the chat"
- User feedback: "first compression app crashes then subsequent compressions work"
- User request: "green lets make it grey, just a slight contrast"
- Related: LlmChatViewModel.kt:128-183 (compression trigger logic)
- Related: ChatPanel.kt (message rendering)
