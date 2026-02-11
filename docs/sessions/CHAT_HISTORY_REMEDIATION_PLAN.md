# Chat History Feature - Remediation Plan

**Date:** November 26, 2025
**Project:** OnDevice AI
**Branch:** feature/chat-history-phase1
**Priority:** High

---

## Current Status

| Component | Status | Notes |
|-----------|--------|-------|
| Database Layer | WORKING | Room tables created, data persists |
| Silent Saves | WORKING | Messages auto-save during chat |
| ConversationListScreen | WORKING | Shows threads, delete works |
| ConversationDetailScreen | WORKING | Shows messages read-only |
| Navigation from Menu | PARTIALLY | Needs verification across all tasks |
| Continue Conversation | NOT IMPLEMENTED | Cannot resume old conversations |

---

## Phase 1: Cleanup and Verification (IMMEDIATE)

### 1.1 Verify Navigation Paths

**Files to Check:**
- `ui/common/ChatMenuSheet.kt`
- `ui/common/chat/ChatView.kt`
- `ui/llmchat/LlmChatScreen.kt`
- `ui/llmaskimage/LlmAskImageScreen.kt`
- `ui/llmaskaudio/LlmAskAudioScreen.kt`

**Actions:**
```kotlin
// Ensure onNavigateToConversationHistory is wired in ALL three task screens
// Check ChatMenuSheet has "View conversation history" button
// Verify navigation callback is passed through all layers
```

**Test Cases:**
- [ ] AI Chat → Menu → "View conversation history" → Opens list
- [ ] Ask Image → Menu → "View conversation history" → Opens list
- [ ] Audio Scribe → Menu → "View conversation history" → Opens list
- [ ] Back navigation from list returns to correct screen

### 1.2 Remove Dead Code

**Search for and remove:**
```kotlin
// Old textInputHistory references (if any remain)
grep -r "textInputHistory" app/src/main/java/

// Old conversation-threading patterns
grep -r "getThreadsForTask" app/src/main/java/
grep -r "ConversationWithMessages" app/src/main/java/
```

**Files likely affected:**
- `ChatMenuSheet.kt` - May have remnant history code
- `MessageInputText.kt` - May have old menu items
- `TextInputHistorySheet.kt` - Evaluate if still needed

### 1.3 Fix Any Import Issues

Check all conversation-related files for correct imports:
```kotlin
// Required imports in ViewModels
import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ConversationThread
import ai.ondevice.app.data.ConversationMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
```

---

## Phase 2: Continue Conversation Feature (SHORT-TERM)

### 2.1 Add "Continue" Button to ConversationDetailScreen

**File:** `ui/conversationdetail/ConversationDetailScreen.kt`

**Changes:**
```kotlin
@Composable
fun ConversationDetailScreen(
    threadId: Long,
    onNavigateBack: () -> Unit,
    onContinueConversation: (Long) -> Unit  // NEW PARAMETER
) {
    // ... existing UI ...

    // Add FAB or button
    FloatingActionButton(
        onClick = { onContinueConversation(threadId) }
    ) {
        Icon(Icons.Default.PlayArrow, "Continue conversation")
    }
}
```

### 2.2 Wire Navigation for Continue

**File:** `ui/navigation/GalleryNavGraph.kt`

**Changes:**
```kotlin
composable(route = ROUTE_CONVERSATION_DETAIL, ...) {
    ConversationDetailScreen(
        threadId = threadId,
        onNavigateBack = { navController.navigateUp() },
        onContinueConversation = { threadId ->
            // Navigate to chat with thread ID
            navController.navigate("$ROUTE_MODEL/llm_chat/{modelName}?threadId=$threadId")
        }
    )
}
```

### 2.3 Load Conversation into ChatViewModel

**File:** `ui/common/chat/ChatViewModel.kt`

**Add methods:**
```kotlin
fun loadExistingThread(threadId: Long) {
    viewModelScope.launch(Dispatchers.IO) {
        val thread = conversationDao.getThreadById(threadId)
        if (thread != null) {
            currentThreadId = threadId
            val messages = conversationDao.getMessagesForThread(threadId)
            // Convert to ChatMessages and add to UI state
            for (msg in messages) {
                val chatMsg = ChatMessageText(
                    content = msg.content,
                    side = if (msg.isUser) ChatSide.USER else ChatSide.AGENT
                )
                // Add to UI without re-saving to DB
                addMessageToUiOnly(chatMsg)
            }
        }
    }
}

private fun addMessageToUiOnly(model: Model, message: ChatMessage) {
    // Same as addMessage but without saveMessageToDatabase call
}
```

### 2.4 Pass Thread ID Through Navigation

**File:** `customtasks/common/CustomTaskData.kt`

**Changes:**
```kotlin
data class CustomTaskDataForBuiltinTask(
    val modelManagerViewModel: ModelManagerViewModel,
    val onNavUp: () -> Unit,
    val onNavigateToConversationHistory: () -> Unit,
    val initialThreadId: Long? = null  // NEW: For continuing conversations
)
```

---

## Phase 3: Enhanced Features (MEDIUM-TERM)

### 3.1 AI-Generated Titles

**Current:** Title = first 50 chars of first message
**Improved:** Generate title from AI response

**Implementation:**
```kotlin
// After first AI response, generate title
fun updateThreadTitle(threadId: Long, aiResponse: String) {
    viewModelScope.launch(Dispatchers.IO) {
        val title = generateTitle(aiResponse)  // Extract key topic
        conversationDao.updateThread(
            conversationDao.getThreadById(threadId)!!.copy(title = title)
        )
    }
}

private fun generateTitle(text: String): String {
    // Simple: First sentence or first 50 chars
    // Advanced: Use AI to summarize
    return text.split('.').first().take(50)
}
```

### 3.2 Search Conversations

**File:** `ui/conversationlist/ConversationListScreen.kt`

**Add search bar:**
```kotlin
@Composable
fun ConversationListScreen(...) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchConversations(it)
            },
            placeholder = { Text("Search conversations...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        // List
        // ...
    }
}
```

**File:** `data/ConversationDao.kt`

**Add query:**
```kotlin
@Query("""
    SELECT DISTINCT t.* FROM conversation_threads t
    LEFT JOIN conversation_messages m ON t.id = m.threadId
    WHERE t.title LIKE '%' || :query || '%'
       OR m.content LIKE '%' || :query || '%'
    ORDER BY t.updatedAt DESC
""")
fun searchThreads(query: String): Flow<List<ConversationThread>>
```

### 3.3 Swipe Actions Enhancement

**Current:** Swipe to delete
**Enhanced:** Swipe left = delete, swipe right = pin/star

```kotlin
SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> {
                // Delete (red background)
            }
            SwipeToDismissBoxValue.StartToEnd -> {
                // Pin/Star (blue background)
            }
        }
    }
)
```

---

## Phase 4: Production Polish (LONG-TERM)

### 4.1 Export Conversations

```kotlin
fun exportConversation(threadId: Long): String {
    val thread = conversationDao.getThreadById(threadId)
    val messages = conversationDao.getMessagesForThread(threadId)

    return buildString {
        appendLine("# ${thread.title}")
        appendLine("Date: ${formatDate(thread.createdAt)}")
        appendLine()
        messages.forEach { msg ->
            val role = if (msg.isUser) "You" else "AI"
            appendLine("**$role:** ${msg.content}")
            appendLine()
        }
    }
}
```

### 4.2 Conversation Settings

- Auto-delete conversations older than X days
- Maximum conversation count
- Per-model conversation filtering

### 4.3 Multi-Model Support

Track which model was used for each message:
```kotlin
@Entity
data class ConversationMessage(
    // ... existing fields ...
    val modelName: String? = null  // NEW: Track model per message
)
```

---

## Testing Checklist

### Unit Tests
- [ ] ConversationDao.insertThread returns valid ID
- [ ] ConversationDao.insertMessage associates with thread
- [ ] ConversationDao.deleteThread cascades to messages
- [ ] ChatViewModel.saveMessageToDatabase creates thread on first message
- [ ] ChatViewModel.clearAllMessages resets threadId

### Integration Tests
- [ ] Full conversation flow: send messages → check DB → view history
- [ ] Delete conversation → verify messages gone
- [ ] App restart → conversations persist

### UI Tests
- [ ] ConversationListScreen shows threads correctly
- [ ] ConversationDetailScreen shows messages in order
- [ ] Empty state displayed when no conversations
- [ ] Swipe to delete works with confirmation

### Navigation Tests
- [ ] All three tasks can access conversation history
- [ ] Back navigation works correctly
- [ ] Deep link to conversation detail (future)

---

## Risk Mitigation

### Data Migration
If schema changes are needed:
```kotlin
// AppDatabase.kt
@Database(
    version = 2,  // Increment version
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
```

For complex migrations:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE conversation_threads ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
    }
}
```

### Performance
- Add pagination for conversation list if > 100 threads
- Add message pagination for threads with > 1000 messages
- Consider LRU cache for frequently accessed threads

### Error Recovery
- Graceful handling if DB corrupted (offer to clear data)
- Retry logic for transient failures
- Logging for debugging production issues

---

## Implementation Priority

| Task | Priority | Effort | Impact |
|------|----------|--------|--------|
| Verify all navigation paths | P0 | Low | High |
| Remove dead code | P0 | Low | Medium |
| Add Continue Conversation | P1 | Medium | High |
| AI-generated titles | P2 | Low | Medium |
| Search conversations | P2 | Medium | Medium |
| Export conversations | P3 | Low | Low |
| Multi-model tracking | P3 | Medium | Low |

---

## Success Metrics

1. **Zero crashes** related to conversation history
2. **100% navigation coverage** - All tasks can access history
3. **< 100ms latency** for message saves (non-blocking)
4. **Data persistence** verified across 100+ app restarts
5. **User satisfaction** - Can find and continue old conversations

---

**Document Version:** 1.0
**Last Updated:** November 26, 2025
**Status:** Ready for Implementation
