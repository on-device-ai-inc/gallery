# Tasks: fix-compaction-ux-bugs

## Implementation Tasks

- [ ] **Task 1: Fix first-compression crash in CompactionManager**
  - File: app/src/main/java/ai/ondevice/app/conversation/CompactionManager.kt
  - Change line 119: `deleteMessagesForThread(msg.id)` → correct method to delete single message
  - Check ConversationDao for correct API method name
  - Acceptance: First compression completes without crash, verify with `adb logcat`

- [ ] **Task 2: Fix compacting indicator positioning in ChatPanel**
  - File: app/src/main/java/ai/ondevice/app/ui/common/chat/ChatPanel.kt
  - Remove standalone `if (uiState.isCompacting)` item from LazyColumn
  - Add conditional in message rendering: if ChatMessageLoading && isCompacting, render CompactingStatusChip
  - Acceptance: Compacting indicator replaces rotating icon during compression

- [ ] **Task 3: Change compacting indicator color to subtle grey**
  - File: app/src/main/java/ai/ondevice/app/ui/common/chat/CompactingStatusChip.kt
  - Change line 69: `tertiaryContainer` → `surfaceVariant`
  - Change text color: `onTertiaryContainer` → `onSurfaceVariant`
  - Acceptance: Grey background in both dark/light themes, subtle contrast visible

## Testing Tasks

- [ ] Unit test: CompactionManager deletes correct message IDs
- [ ] Manual test: Trigger first compression (30-40 messages), verify no crash
- [ ] Visual test: Verify compacting indicator appears in place of rotating icon
- [ ] Visual test: Verify grey color in dark theme (via screenshot)
- [ ] Visual test: Verify grey color in light theme (via screenshot)

## Documentation Tasks

- [ ] Update LESSONS_LEARNED.md with database API fix pattern
