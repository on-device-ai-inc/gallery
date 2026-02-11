# Spec Delta: fix-compaction-ux-bugs

## MODIFIED

### app/src/main/java/ai/ondevice/app/conversation/CompactionManager.kt
```diff
         // Delete evicted messages
         toEvict.forEach { msg ->
-            conversationDao.deleteMessagesForThread(msg.id)
+            conversationDao.deleteMessage(msg.id)
         }
```

**Rationale**: `deleteMessagesForThread()` expects a threadId parameter, but we're passing message ID. This causes crash on first compression. Correct method is `deleteMessage(id)` which deletes a single message by its ID.

### app/src/main/java/ai/ondevice/app/ui/common/chat/ChatPanel.kt
```diff
     LazyColumn(...) {
-      if (uiState.isCompacting) {
-        item {
-          CompactingStatusChip()
-        }
-      }
       itemsIndexed(messages) { index, message ->
         Row(...) {
-          ChatMessage(message = message, ...)
+          // Replace rotating icon with compacting chip during compression
+          if (message.type == ChatMessageType.LOADING && uiState.isCompacting) {
+            CompactingStatusChip()
+          } else {
+            ChatMessage(message = message, ...)
+          }
         }
       }
     }
```

**Rationale**: CompactingStatusChip was added as a separate item at the top of LazyColumn, causing it to appear disconnected from the conversation flow. Instead, we conditionally replace the ChatMessageLoading (rotating icon) with CompactingStatusChip when compression is active. This makes the indicator appear as a natural continuation of the chat.

### app/src/main/java/ai/ondevice/app/ui/common/chat/CompactingStatusChip.kt
```diff
           Box(
             modifier =
               Modifier
                 .padding(8.dp)
                 .alpha(alpha)
                 .clip(RoundedCornerShape(16.dp))
-                .background(MaterialTheme.colorScheme.tertiaryContainer)
+                .background(MaterialTheme.colorScheme.surfaceVariant)
           ) {
             Row(...) {
               // ...
               Text(
                 "Compacting conversation so we can continue…",
                 style = MaterialTheme.typography.bodyMedium,
-                color = MaterialTheme.colorScheme.onTertiaryContainer,
+                color = MaterialTheme.colorScheme.onSurfaceVariant,
               )
             }
           }
```

**Rationale**: `tertiaryContainer` is typically green in Material 3 themes, making the compacting indicator too prominent. `surfaceVariant` provides a subtle grey background with slight contrast in both dark and light themes, appropriate for a temporary status indicator.

## ADDED
None

## REMOVED
None
