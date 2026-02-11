# OnDevice AI - Chat System Architecture

**Version:** 1.0
**Date:** November 26, 2025
**Branch:** feature/chat-history-phase1

---

## Overview

The OnDevice AI chat system provides a unified chat interface for three task types (AI Chat, Ask Image, Audio Scribe) with persistent conversation history using Room database.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           UI Layer (Compose)                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │  AI Chat     │  │  Ask Image   │  │ Audio Scribe │                  │
│  │  Screen      │  │  Screen      │  │  Screen      │                  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                  │
│         │                 │                 │                          │
│         └─────────────────┼─────────────────┘                          │
│                           ▼                                            │
│                   ┌───────────────┐                                    │
│                   │   ChatPanel   │  ◄── Shared chat UI component      │
│                   └───────┬───────┘                                    │
│                           │                                            │
│  ┌────────────────────────┼────────────────────────┐                   │
│  │                        ▼                        │                   │
│  │               ┌─────────────────┐               │                   │
│  │               │  ChatMenuSheet  │               │                   │
│  │               │  (+ button menu)│               │                   │
│  │               └────────┬────────┘               │                   │
│  │                        │                        │                   │
│  │         ┌──────────────┴──────────────┐        │                   │
│  │         ▼                             ▼        │                   │
│  │  ┌──────────────┐           ┌─────────────────┐│                   │
│  │  │ Conversation │           │ Conversation    ││                   │
│  │  │ ListScreen   │◄─────────►│ DetailScreen    ││                   │
│  │  └──────────────┘           └─────────────────┘│                   │
│  │         │                             │        │                   │
│  └─────────┼─────────────────────────────┼────────┘                   │
│            ▼                             ▼                             │
└────────────────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────────────┐
│                     ViewModel Layer                                     │
├──────────────────────────┼──────────────────────────────────────────────┤
│                          │                                              │
│  ┌───────────────────────┴───────────────────────┐                     │
│  │                                               │                     │
│  │           ChatViewModel (Abstract)            │                     │
│  │  ┌─────────────────────────────────────────┐  │                     │
│  │  │ • uiState: ChatUiState                  │  │                     │
│  │  │ • currentThreadId: Long?                │  │                     │
│  │  │ • threadMutex: Mutex                    │  │                     │
│  │  │ • addMessage()                          │  │                     │
│  │  │ • clearAllMessages()                    │  │                     │
│  │  │ • saveMessageToDatabase() [private]     │  │                     │
│  │  └─────────────────────────────────────────┘  │                     │
│  │                       │                       │                     │
│  │     ┌─────────────────┼─────────────────┐     │                     │
│  │     ▼                 ▼                 ▼     │                     │
│  │ ┌─────────┐     ┌─────────┐     ┌─────────┐   │                     │
│  │ │LlmChat  │     │LlmAsk   │     │LlmAsk   │   │                     │
│  │ │ViewModel│     │ImageVM  │     │AudioVM  │   │                     │
│  │ └─────────┘     └─────────┘     └─────────┘   │                     │
│  │                                               │                     │
│  └───────────────────────────────────────────────┘                     │
│                                                                        │
│  ┌─────────────────────────┐  ┌─────────────────────────┐              │
│  │ ConversationListVM      │  │ ConversationDetailVM    │              │
│  │ • uiState: ListUiState  │  │ • uiState: DetailState  │              │
│  │ • loadConversations()   │  │ • loadConversation()    │              │
│  │ • deleteConversation()  │  │                         │              │
│  └─────────────────────────┘  └─────────────────────────┘              │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────────────┐
│                      Data Layer                                         │
├──────────────────────────┼──────────────────────────────────────────────┤
│                          │                                              │
│  ┌───────────────────────┴───────────────────────┐                     │
│  │                                               │                     │
│  │              ConversationDao                  │                     │
│  │  ┌─────────────────────────────────────────┐  │                     │
│  │  │ + insertThread(): Long                  │  │                     │
│  │  │ + insertMessage()                       │  │                     │
│  │  │ + updateThread()                        │  │                     │
│  │  │ + getAllThreads(): List                 │  │                     │
│  │  │ + getAllThreadsFlow(): Flow             │  │                     │
│  │  │ + getMessagesForThread(): List          │  │                     │
│  │  │ + getMessagesForThreadFlow(): Flow      │  │                     │
│  │  │ + getThreadById(): Thread?              │  │                     │
│  │  │ + deleteThread()                        │  │                     │
│  │  └─────────────────────────────────────────┘  │                     │
│  │                       │                       │                     │
│  └───────────────────────┼───────────────────────┘                     │
│                          │                                              │
│  ┌───────────────────────┴───────────────────────┐                     │
│  │                                               │                     │
│  │              AppDatabase (Room)               │                     │
│  │  ┌─────────────────────────────────────────┐  │                     │
│  │  │ Database: ondevice_database             │  │                     │
│  │  │ Version: 1                              │  │                     │
│  │  │ Entities:                               │  │                     │
│  │  │   - ConversationThread                  │  │                     │
│  │  │   - ConversationMessage                 │  │                     │
│  │  └─────────────────────────────────────────┘  │                     │
│  │                                               │                     │
│  └───────────────────────────────────────────────┘                     │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Package Structure

```
app/src/main/java/ai/ondevice/app/
├── data/
│   ├── AppDatabase.kt              # Room database singleton
│   ├── ConversationDao.kt          # DAO interface for DB operations
│   ├── ConversationThread.kt       # Thread entity (table)
│   └── ConversationMessage.kt      # Message entity (table)
│
├── di/
│   └── AppModule.kt                # Hilt dependency injection module
│
└── ui/
    ├── common/
    │   ├── chat/
    │   │   ├── ChatViewModel.kt    # Abstract base chat ViewModel
    │   │   ├── ChatPanel.kt        # Shared chat UI component
    │   │   ├── ChatView.kt         # Chat view wrapper
    │   │   ├── ChatMessage.kt      # Message data types
    │   │   └── ...                 # Other chat components
    │   │
    │   └── ChatMenuSheet.kt        # Menu (+ button) component
    │
    ├── conversationlist/
    │   ├── ConversationListScreen.kt
    │   ├── ConversationListViewModel.kt
    │   └── ConversationListUiState.kt
    │
    ├── conversationdetail/
    │   ├── ConversationDetailScreen.kt
    │   ├── ConversationDetailViewModel.kt
    │   └── ConversationDetailUiState.kt
    │
    ├── llmchat/
    │   ├── LlmChatScreen.kt        # AI Chat task screen
    │   ├── LlmChatViewModel.kt     # AI Chat ViewModel
    │   └── LlmChatTaskModule.kt    # Task registration
    │
    ├── llmaskimage/
    │   └── ...                     # Ask Image task
    │
    ├── llmaskaudio/
    │   └── ...                     # Audio Scribe task
    │
    └── navigation/
        └── GalleryNavGraph.kt      # Navigation routes
```

---

## Data Flow

### 1. Message Saving Flow

```
User sends message
        │
        ▼
┌───────────────────┐
│ ChatPanel receives│
│ text input        │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ ViewModel.        │
│ addMessage()      │
│ called            │
└─────────┬─────────┘
          │
          ├─────────────────────────────┐
          │                             │
          ▼                             ▼
┌─────────────────┐           ┌─────────────────────┐
│ Update UI State │           │ saveMessageToDatabase│
│ (Synchronous)   │           │ (Background IO)     │
└─────────────────┘           └──────────┬──────────┘
                                         │
                                         ▼
                              ┌─────────────────────┐
                              │ Mutex.withLock {    │
                              │   if (threadId==null)│
                              │     create thread   │
                              │   save message      │
                              │ }                   │
                              └──────────┬──────────┘
                                         │
                                         ▼
                              ┌─────────────────────┐
                              │ Room Database       │
                              │ (SQLite)            │
                              └─────────────────────┘
```

### 2. Conversation List Loading Flow

```
User opens Conversation List
        │
        ▼
┌───────────────────────────┐
│ ConversationListScreen    │
│ renders                   │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ hiltViewModel<            │
│ ConversationListViewModel>│
│ injected                  │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ ViewModel.init {          │
│   loadConversations()     │
│ }                         │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ conversationDao           │
│ .getAllThreadsFlow()      │
│ .collect { ... }          │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Transform threads to      │
│ ConversationItem with:    │
│ - lastMessage preview     │
│ - messageCount            │
│ - formatted timestamp     │
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│ Update _uiState:          │
│ Loading → Success/Empty   │
└───────────────────────────┘
```

---

## Key Classes

### ConversationThread.kt
```kotlin
@Entity(tableName = "conversation_threads")
data class ConversationThread(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,           // First message content (truncated)
    val modelId: String,         // Which model was used
    val taskId: String,          // Which task (llm_chat, etc.)
    val createdAt: Long,         // Thread creation timestamp
    val updatedAt: Long          // Last message timestamp
)
```

### ConversationMessage.kt
```kotlin
@Entity(
    tableName = "conversation_messages",
    foreignKeys = [ForeignKey(
        entity = ConversationThread::class,
        parentColumns = ["id"],
        childColumns = ["threadId"],
        onDelete = ForeignKey.CASCADE  // Delete messages when thread deleted
    )],
    indices = [Index("threadId")]       // Fast lookup by thread
)
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,          // Parent thread
    val content: String,         // Message text
    val isUser: Boolean,         // true = user, false = AI
    val timestamp: Long          // Message timestamp
)
```

### ChatViewModel.kt (Abstract)
```kotlin
abstract class ChatViewModel(
    private val conversationDao: ConversationDao  // REQUIRED (non-nullable)
) : ViewModel() {

    private var currentThreadId: Long? = null     // Current conversation
    private val threadMutex = Mutex()             // Race condition protection

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    fun addMessage(model: Model, message: ChatMessage) {
        // 1. Update UI state immediately
        // 2. Save to database in background
        saveMessageToDatabase(model, message)
    }

    fun clearAllMessages(model: Model) {
        // Clear UI state
        currentThreadId = null  // Reset for new conversation
    }

    private fun saveMessageToDatabase(model: Model, message: ChatMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            // Only save text messages
            if (message !is ChatMessageText) return@launch

            threadMutex.withLock {
                if (currentThreadId == null) {
                    // Create new thread
                    currentThreadId = conversationDao.insertThread(...)
                }
            }

            // Save message
            conversationDao.insertMessage(...)
        }
    }
}
```

---

## Navigation Routes

```kotlin
// GalleryNavGraph.kt

private const val ROUTE_CONVERSATION_LIST = "conversation_list"
private const val ROUTE_CONVERSATION_DETAIL = "conversation_detail/{threadId}"

NavHost(...) {
    // Conversation List
    composable(route = ROUTE_CONVERSATION_LIST) {
        ConversationListScreen(
            onNavigateToDetail = { threadId ->
                navController.navigate("conversation_detail/$threadId")
            },
            onNavigateToNewChat = {
                navController.navigateUp()
            }
        )
    }

    // Conversation Detail
    composable(
        route = ROUTE_CONVERSATION_DETAIL,
        arguments = listOf(navArgument("threadId") { type = NavType.LongType })
    ) { backStackEntry ->
        val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
        ConversationDetailScreen(
            threadId = threadId,
            onNavigateBack = { navController.navigateUp() }
        )
    }
}
```

---

## Dependency Injection (Hilt)

```kotlin
// AppModule.kt

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ondevice_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }
}
```

---

## State Management

### ConversationListUiState
```kotlin
sealed interface ConversationListUiState {
    data object Loading : ConversationListUiState
    data object Empty : ConversationListUiState
    data class Success(val conversations: List<ConversationItem>) : ConversationListUiState
    data class Error(val message: String) : ConversationListUiState
}

data class ConversationItem(
    val thread: ConversationThread,
    val lastMessage: String?,
    val messageCount: Int,
    val formattedTimestamp: String
)
```

### ConversationDetailUiState
```kotlin
sealed interface ConversationDetailUiState {
    data object Loading : ConversationDetailUiState
    data object Error : ConversationDetailUiState
    data class Success(
        val thread: ConversationThread,
        val messages: List<ConversationMessage>
    ) : ConversationDetailUiState
}
```

---

## Error Handling

1. **Database Errors**: Caught and logged, UI continues working
2. **Race Conditions**: Protected by Mutex in thread creation
3. **Null Safety**: DAO is required (non-nullable), eliminates null checks
4. **Cascade Delete**: Foreign key ensures orphan messages are cleaned up

---

## Performance Considerations

1. **Background IO**: All database operations on `Dispatchers.IO`
2. **Fire-and-Forget**: Message saving doesn't block UI
3. **Flow Subscriptions**: Reactive updates via `StateFlow` and `Flow`
4. **Index**: `threadId` indexed for fast message lookups
5. **Lazy Loading**: Messages loaded only when detail screen opened

---

## Future Enhancements

1. **Continue Conversation**: Load thread into active chat
2. **Search**: Full-text search across messages
3. **Export**: Share conversations as text/markdown
4. **Auto-Title**: AI-generated conversation titles
5. **Pagination**: Handle very long conversations
6. **Multi-Model**: Track which model for each message

---

**Document Version:** 1.0
**Last Updated:** November 26, 2025
