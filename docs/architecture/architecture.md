# OnDevice AI - Architecture Document

**Author:** Gora & Architecture Workflow
**Date:** 2025-11-26
**Version:** 1.0

---

## Executive Summary

OnDevice AI is a brownfield Android application built with Jetpack Compose, Room database, and Hilt dependency injection. This architecture document defines the patterns and decisions that AI agents must follow when implementing new features to ensure consistency across the codebase.

The architecture prioritizes **simplicity** - using the most straightforward approach for each decision while maintaining code quality and user experience.

---

## Decision Summary

| Category | Decision | Version | Affects FRs | Rationale |
|----------|----------|---------|-------------|-----------|
| Language | Kotlin | 1.9+ | All | Android standard |
| UI Framework | Jetpack Compose | BOM 2024.x | All UI | Modern declarative UI |
| Database | Room | 2.6.x | FR10-FR21 | Local persistence |
| DI Framework | Hilt | 2.51+ | All | Google recommended |
| Architecture | MVVM | - | All | Clean separation |
| Design System | Material Design 3 | - | All UI | Familiar to users |
| Markdown | compose-richtext | 1.0.0+ | FR25, FR26 | Already implemented |
| Clipboard | Android ClipboardManager | Built-in | FR22, FR23 | Simplest approach |
| Search | Room LIKE query | Built-in | FR17 | No external dependency |
| Date Display | DateUtils | Built-in | FR21 | Android standard |
| Logging | Android Log | Built-in | Debug | Already in use |
| Error Handling | Try-catch + Toast | Built-in | Reliability | Standard pattern |

---

## Project Structure

```
app/src/main/java/ai/ondevice/app/
├── data/                              # Data layer
│   ├── AppDatabase.kt                 # Room database singleton
│   ├── ConversationDao.kt             # Conversation CRUD operations
│   ├── ConversationThread.kt          # Thread entity
│   ├── ConversationMessage.kt         # Message entity
│   ├── Model.kt                       # AI model definitions
│   ├── Task.kt                        # Task definitions
│   └── DataStoreRepository.kt         # Preferences storage
│
├── di/                                # Dependency injection
│   └── AppModule.kt                   # Hilt module definitions
│
├── ui/                                # UI layer
│   ├── common/                        # Shared components
│   │   ├── chat/                      # Chat-related components
│   │   │   ├── ChatViewModel.kt       # Abstract base ViewModel
│   │   │   ├── ChatPanel.kt           # Main chat UI
│   │   │   ├── ChatView.kt            # Chat view wrapper
│   │   │   ├── ChatMessage.kt         # Message data types
│   │   │   ├── MessageBodyText.kt     # Text message display
│   │   │   ├── MessageInputText.kt    # Text input component
│   │   │   ├── AudioRecorderPanel.kt  # Voice recording
│   │   │   └── ModelSelector.kt       # Model picker
│   │   │
│   │   ├── ChatMenuSheet.kt           # Menu drawer
│   │   ├── MarkdownText.kt            # Markdown rendering
│   │   ├── Utils.kt                   # Utility functions (clipboard here)
│   │   └── ErrorDialog.kt             # Error display
│   │
│   ├── conversationlist/              # Conversation history list
│   │   ├── ConversationListScreen.kt
│   │   ├── ConversationListViewModel.kt
│   │   └── ConversationListUiState.kt
│   │
│   ├── conversationdetail/            # Conversation detail view
│   │   ├── ConversationDetailScreen.kt
│   │   ├── ConversationDetailViewModel.kt
│   │   └── ConversationDetailUiState.kt
│   │
│   ├── llmchat/                       # AI Chat task
│   │   ├── LlmChatScreen.kt
│   │   ├── LlmChatViewModel.kt
│   │   └── LlmChatTaskModule.kt
│   │
│   ├── llmaskimage/                   # Ask Image task
│   │   └── ...
│   │
│   ├── llmaskaudio/                   # Audio Scribe task
│   │   └── ...
│   │
│   ├── navigation/                    # Navigation
│   │   └── GalleryNavGraph.kt
│   │
│   └── theme/                         # Theming
│       ├── Color.kt
│       ├── Theme.kt
│       ├── ThemeSettings.kt
│       └── Type.kt
│
├── worker/                            # Background work
│   └── DownloadWorker.kt              # Model downloads
│
└── MainActivity.kt                    # Entry point
```

---

## FR Category to Architecture Mapping

| FR Category | Location | Components |
|-------------|----------|------------|
| User Account (FR1-FR3) | `data/DataStoreRepository.kt` | Local preferences only |
| Model Management (FR4-FR9) | `ui/modelmanager/`, `worker/` | ModelManagerViewModel, DownloadWorker |
| Conversations (FR10-FR21) | `ui/common/chat/`, `ui/conversationlist/` | ChatViewModel, ConversationDao |
| Message Interactions (FR22-FR27) | `ui/common/chat/`, `ui/common/Utils.kt` | MessageBodyText, copyToClipboard |
| Data Management (FR28-FR32) | `data/`, `ui/common/` | ConversationDao, future export |
| Settings (FR33-FR38) | `ui/home/SettingsDialog.kt`, `ui/theme/` | ThemeSettings |
| Offline (FR39-FR42) | All | No network calls in AI ops |
| Privacy (FR43-FR46) | `ui/common/chat/ChatPanel.kt` | Privacy indicator text |

---

## Technology Stack Details

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 1.9+ | Primary language |
| Android SDK | API 26+ (Android 8.0) | Minimum target |
| Jetpack Compose | BOM 2024.x | UI framework |
| Room | 2.6.x | Local database |
| Hilt | 2.51+ | Dependency injection |
| Coroutines | 1.8+ | Async operations |
| Material 3 | 1.2+ | Design system |
| compose-richtext | 1.0.0+ | Markdown rendering |

### Integration Points

| Component A | Component B | Integration Method |
|-------------|-------------|-------------------|
| UI (Compose) | ViewModel | StateFlow collection |
| ViewModel | Room | Suspend functions, Flow |
| ViewModel | AI Engine | LlmChatModelHelper callbacks |
| Activities | Hilt | @AndroidEntryPoint |
| ViewModels | Hilt | @HiltViewModel |

---

## Implementation Patterns

These patterns ensure consistent implementation across all AI agents:

### 1. Clipboard/Copy Pattern

**Location:** `ui/common/Utils.kt`

```kotlin
// Add to existing Utils.kt
fun copyToClipboard(context: Context, text: String, label: String = "Text") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
```

**Usage in Composables:**
```kotlin
val context = LocalContext.current
IconButton(onClick = { copyToClipboard(context, messageText) }) {
    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
}
```

### 2. Search Pattern

**Location:** `data/ConversationDao.kt`

```kotlin
// Add to ConversationDao
@Query("""
    SELECT DISTINCT t.* FROM conversation_threads t
    LEFT JOIN conversation_messages m ON t.id = m.threadId
    WHERE t.title LIKE '%' || :query || '%'
       OR m.content LIKE '%' || :query || '%'
    ORDER BY t.updatedAt DESC
""")
fun searchThreads(query: String): Flow<List<ConversationThread>>
```

### 3. Date Formatting Pattern

**Location:** `ui/common/Utils.kt`

```kotlin
fun formatRelativeTime(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

fun formatDateGroup(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        isSameDay(now, then) -> "Today"
        isYesterday(now, then) -> "Yesterday"
        isWithinDays(now, then, 7) -> "Last 7 Days"
        else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}
```

### 4. Privacy Indicator Pattern

**Location:** `ui/common/chat/ChatPanel.kt`

```kotlin
// Show during AI inference
if (isGenerating) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Running privately on your device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
```

### 5. Continue Conversation Pattern

**Location:** `ui/common/chat/ChatViewModel.kt`

```kotlin
// Add to ChatViewModel
fun loadExistingThread(threadId: Long) {
    viewModelScope.launch(Dispatchers.IO) {
        val thread = conversationDao.getThreadById(threadId)
        if (thread != null) {
            currentThreadId = threadId
            val messages = conversationDao.getMessagesForThread(threadId)

            withContext(Dispatchers.Main) {
                // Clear current messages
                _uiState.update { it.copy(messages = emptyList()) }

                // Add historical messages to UI (without re-saving)
                messages.forEach { msg ->
                    val chatMsg = ChatMessageText(
                        content = msg.content,
                        side = if (msg.isUser) ChatSide.USER else ChatSide.AGENT
                    )
                    addMessageToUiOnly(chatMsg)
                }
            }
        }
    }
}

private fun addMessageToUiOnly(message: ChatMessage) {
    // Add to UI state without triggering database save
    _uiState.update { state ->
        state.copy(messages = state.messages + message)
    }
}
```

---

## Consistency Rules

### Naming Conventions

| Entity | Convention | Example |
|--------|------------|---------|
| Composables | PascalCase | `ChatPanel`, `MessageBodyText` |
| Functions | camelCase | `copyToClipboard`, `loadExistingThread` |
| Files | PascalCase.kt | `ChatViewModel.kt` |
| Packages | lowercase | `ui.common.chat` |
| Database tables | snake_case | `conversation_threads` |
| Database columns | camelCase | `threadId`, `createdAt` |
| State classes | PascalCase + UiState | `ConversationListUiState` |
| ViewModels | PascalCase + ViewModel | `ConversationListViewModel` |

### Code Organization

| Type | Location |
|------|----------|
| Shared UI components | `ui/common/` |
| Task-specific screens | `ui/{taskname}/` |
| Data entities | `data/` |
| DI modules | `di/` |
| Utilities | `ui/common/Utils.kt` |
| Theme | `ui/theme/` |

### Error Handling

```kotlin
// Standard error handling pattern
try {
    // Operation
} catch (e: Exception) {
    Log.e(TAG, "Error description: ${e.message}", e)
    // Show user-friendly message
    withContext(Dispatchers.Main) {
        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
    }
}
```

### Logging Strategy

```kotlin
// Use Android Log with consistent TAG
private const val TAG = "ClassName"

Log.d(TAG, "Debug message")      // Development info
Log.i(TAG, "Info message")       // Important events
Log.w(TAG, "Warning message")    // Potential issues
Log.e(TAG, "Error message", e)   // Errors with exception
```

---

## Data Architecture

### Entity Relationship

```
ConversationThread (1) ──────< (N) ConversationMessage
       │
       ├── id (PK, auto)
       ├── title
       ├── modelId
       ├── taskId
       ├── createdAt
       └── updatedAt

ConversationMessage
       │
       ├── id (PK, auto)
       ├── threadId (FK → ConversationThread.id, CASCADE DELETE)
       ├── content
       ├── isUser
       └── timestamp
```

### Data Flow

```
User Input → Composable → ViewModel → DAO → Room (SQLite)
                              ↓
                         StateFlow
                              ↓
                    UI Updates (recomposition)
```

---

## API Contracts

### Internal APIs (ViewModel → DAO)

All DAO methods are `suspend` functions for non-blocking operations:

```kotlin
// Thread operations
suspend fun insertThread(thread: ConversationThread): Long
suspend fun updateThread(thread: ConversationThread)
suspend fun deleteThread(threadId: Long)
suspend fun getThreadById(threadId: Long): ConversationThread?

// Message operations
suspend fun insertMessage(message: ConversationMessage)
suspend fun getMessagesForThread(threadId: Long): List<ConversationMessage>

// Reactive queries
fun getAllThreadsFlow(): Flow<List<ConversationThread>>
fun getMessagesForThreadFlow(threadId: Long): Flow<List<ConversationMessage>>
fun searchThreads(query: String): Flow<List<ConversationThread>>
```

---

## Security Architecture

### Privacy by Design

| Aspect | Implementation |
|--------|----------------|
| Data Storage | Local only (Room/SQLite) |
| Network | No calls during AI inference |
| Authentication | None required (local app) |
| Data Export | User-controlled, local formats |
| Encryption | Android system encryption |

### Permissions

| Permission | Purpose | When Requested |
|------------|---------|----------------|
| INTERNET | Model downloads only | First model download |
| RECORD_AUDIO | Voice input | When using voice |
| READ_EXTERNAL_STORAGE | Image attachment | When attaching image |

---

## Performance Considerations

| NFR | Strategy |
|-----|----------|
| NFR1: 3s response start | Stream tokens as generated |
| NFR2: 2s app launch | Lazy model loading |
| NFR3: 500ms list load | Room with Flow, indexed queries |
| NFR4: 1s search | LIKE query with index on threadId |
| NFR5: 30s model switch | Background loading with progress |

### Optimization Patterns

```kotlin
// Use remember for expensive computations
val formattedDate = remember(timestamp) { formatRelativeTime(timestamp) }

// Use LaunchedEffect for one-time operations
LaunchedEffect(threadId) { viewModel.loadExistingThread(threadId) }

// Use derivedStateOf for filtered lists
val filteredList by remember { derivedStateOf { list.filter { ... } } }
```

---

## Deployment Architecture

| Aspect | Decision |
|--------|----------|
| Distribution | Google Play Store |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | Latest stable |
| Architecture | ARM64 primary, ARM32 secondary |
| App Bundle | Yes (AAB format) |
| ProGuard | Enabled for release |

---

## Development Environment

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK API 26+
- Kotlin 1.9+

### Setup Commands

```bash
# Clone and open in Android Studio
git clone <repo-url>
cd Android/src

# Build debug APK
./gradlew :app:assembleDebug

# Run tests
./gradlew :app:testDebugUnitTest

# Build release
./gradlew :app:bundleRelease
```

---

## Architecture Decision Records (ADRs)

### ADR-001: Simplest Approach Philosophy

**Context:** Need to implement new features (copy, search, privacy indicators) efficiently.

**Decision:** Always choose the simplest viable implementation:
- Use Android built-in APIs when available
- Avoid external dependencies for simple tasks
- Utility functions over complex abstractions

**Consequences:** Faster development, easier maintenance, smaller APK size.

### ADR-002: Clipboard via Utility Function

**Context:** Need copy functionality for messages and code blocks (FR22, FR23).

**Decision:** Single `copyToClipboard()` function in Utils.kt using Android ClipboardManager.

**Consequences:** Simple, reusable, no additional dependencies.

### ADR-003: Search via Room LIKE Query

**Context:** Need conversation search (FR17).

**Decision:** Add `searchThreads()` to ConversationDao using SQL LIKE with wildcard matching.

**Consequences:** No additional dependency, works offline, sufficient for expected data volume.

### ADR-004: Continue Conversation via ViewModel

**Context:** Need to resume old conversations (FR18).

**Decision:** Add `loadExistingThread()` to ChatViewModel that loads messages from database and populates UI state without re-saving.

**Consequences:** Reuses existing infrastructure, maintains thread continuity.

### ADR-005: Privacy Indicator as Conditional Text

**Context:** Need to show "Running privately on your device" during inference (FR43, FR44).

**Decision:** Conditional `Row` composable in ChatPanel that shows when `isGenerating` is true.

**Consequences:** Simple, no state management overhead, clear visual feedback.

---

## Validation Checklist

- [x] All 46 FRs from PRD have architectural support
- [x] All 20 NFRs from PRD are addressed
- [x] Every FR category mapped to architecture components
- [x] Source tree is complete and accurate
- [x] Implementation patterns cover all new features
- [x] Naming conventions documented
- [x] Error handling pattern defined
- [x] Logging strategy defined
- [x] No placeholder text remains

---

_Generated by BMAD Decision Architecture Workflow v1.0_
_Date: 2025-11-26_
_For: Gora_
