# Spec Delta: infinite-conversation-minimal

## ADDED

### conversation-memory.md
```markdown
# Conversation Memory Management

## Overview
Enables infinite conversations by automatically managing context window limits through self-summarization. Based on LangChain ConversationSummaryBufferMemory pattern.

## Problem
- Gemma 2B has 8,192 token context limit
- Long conversations exceed this limit
- Users must start new chats and re-explain context
- Wastes tokens and creates frustration

## Solution
- Track token count in real-time
- Trigger compaction at 75% (6,144 tokens)
- Summarize oldest messages using LLM itself
- Keep recent messages verbatim
- Reset session with summary injected

## Architecture

### Components

#### TokenEstimator
**Purpose**: Estimate token count for messages
**Location**: `app/src/main/java/ai/ondevice/app/conversation/TokenEstimator.kt`

```kotlin
object TokenEstimator {
    private const val AVG_CHARS_PER_TOKEN = 4

    fun estimate(text: String): Int =
        (text.length / AVG_CHARS_PER_TOKEN).coerceAtLeast(1)

    fun estimate(messages: List<ConversationMessage>): Int =
        messages.sumOf { estimate(it.content) }
}
```

**Accuracy**: ±15% (good enough for MVP with 75% buffer)

#### CompactionTrigger
**Purpose**: Determine when to compact and what to evict
**Location**: `app/src/main/java/ai/ondevice/app/conversation/CompactionTrigger.kt`

```kotlin
class CompactionTrigger(
    private val maxTokens: Int = 8192,
    private val triggerPercent: Float = 0.75f,  // 6,144 tokens
    private val targetPercent: Float = 0.40f    // 3,276 tokens
) {
    fun shouldCompact(currentTokens: Int): Boolean =
        currentTokens >= (maxTokens * triggerPercent).toInt()

    fun turnsToEvict(messages: List<ConversationMessage>): List<ConversationMessage> {
        val targetTokens = (maxTokens * targetPercent).toInt()
        var accumulated = 0
        return messages.takeWhile { msg ->
            accumulated += TokenEstimator.estimate(msg.content)
            TokenEstimator.estimate(messages) - accumulated > targetTokens
        }
    }
}
```

#### ConversationSummarizer
**Purpose**: Summarize messages using existing LLM
**Location**: `app/src/main/java/ai/ondevice/app/conversation/ConversationSummarizer.kt`

```kotlin
class ConversationSummarizer(
    private val llmHelper: LlmChatModelHelper
) {
    suspend fun summarize(messages: List<ConversationMessage>): String {
        val conversationText = messages.joinToString("\n") {
            "${if (it.isUser) "User" else "Assistant"}: ${it.content}"
        }

        val prompt = """
<instruction>
Summarize this conversation into key facts, decisions, and commitments in under 150 words.
Focus on: names mentioned, decisions made, tasks agreed, preferences stated, important context.
</instruction>

<conversation>
$conversationText
</conversation>

<summary>
        """.trimIndent()

        return llmHelper.generateResponse(prompt).trimEnd()
    }
}
```

**Key Insight**: Use the SAME LLM that's already loaded. No new model needed!

#### ConversationState (Room Entity)
**Purpose**: Persist running summary across sessions
**Location**: `app/src/main/java/ai/ondevice/app/data/ConversationState.kt`

```kotlin
@Entity(tableName = "conversation_state")
data class ConversationState(
    @PrimaryKey val threadId: String,
    val runningSummary: String,
    val turnsSummarized: Int,
    val lastCompactionTime: Long
)

@Dao
interface ConversationStateDao {
    @Query("SELECT * FROM conversation_state WHERE threadId = :threadId")
    suspend fun getState(threadId: String): ConversationState?

    @Upsert
    suspend fun saveState(state: ConversationState)
}
```

**Database**: Add to existing AppDatabase (no new database)

#### CompactionManager
**Purpose**: Orchestrate compaction process
**Location**: `app/src/main/java/ai/ondevice/app/conversation/CompactionManager.kt`

```kotlin
class CompactionManager(
    private val llmHelper: LlmChatModelHelper,
    private val messageDao: ConversationMessageDao,
    private val stateDao: ConversationStateDao,
    private val trigger: CompactionTrigger = CompactionTrigger()
) {
    private val summarizer = ConversationSummarizer(llmHelper)

    suspend fun checkAndCompact(
        threadId: String,
        messages: List<ConversationMessage>
    ): CompactionResult {
        val currentTokens = TokenEstimator.estimate(messages)

        if (!trigger.shouldCompact(currentTokens)) {
            return CompactionResult.NotNeeded
        }

        return executeCompaction(threadId, messages)
    }

    private suspend fun executeCompaction(
        threadId: String,
        messages: List<ConversationMessage>
    ): CompactionResult {
        // 1. Determine what to evict
        val toEvict = trigger.turnsToEvict(messages)
        if (toEvict.isEmpty()) return CompactionResult.NotNeeded

        // 2. Summarize evicted using EXISTING LLM
        val newSummary = summarizer.summarize(toEvict)

        // 3. Merge with existing summary
        val existingState = stateDao.getState(threadId)
        val mergedSummary = if (existingState != null) {
            "${existingState.runningSummary}\n\nUpdate: $newSummary".take(1000)
        } else {
            newSummary
        }

        // 4. Save updated state
        stateDao.saveState(ConversationState(
            threadId = threadId,
            runningSummary = mergedSummary,
            turnsSummarized = (existingState?.turnsSummarized ?: 0) + toEvict.size,
            lastCompactionTime = System.currentTimeMillis()
        ))

        // 5. Mark evicted as archived
        toEvict.forEach { msg ->
            messageDao.markArchived(msg.id)
        }

        return CompactionResult.Success(
            evictedCount = toEvict.size,
            summaryLength = mergedSummary.length
        )
    }
}

sealed class CompactionResult {
    object NotNeeded : CompactionResult()
    data class Success(val evictedCount: Int, val summaryLength: Int) : CompactionResult()
}
```

#### ContextBuilder
**Purpose**: Inject summary into inference context
**Location**: `app/src/main/java/ai/ondevice/app/conversation/ContextBuilder.kt`

```kotlin
class ContextBuilder(
    private val stateDao: ConversationStateDao
) {
    suspend fun buildContext(
        threadId: String,
        recentMessages: List<ConversationMessage>,
        systemPrompt: String
    ): String {
        val state = stateDao.getState(threadId)

        return buildString {
            append(systemPrompt)

            // Inject summary if exists
            state?.runningSummary?.let { summary ->
                append("\n\n<previous_context>\n")
                append(summary)
                append("\n</previous_context>")
            }

            // Recent messages
            append("\n\n<conversation>\n")
            recentMessages.forEach { msg ->
                append("${if (msg.isUser) "User" else "Assistant"}: ${msg.content}\n")
            }
            append("</conversation>")
        }
    }
}
```

### Integration Flow

```
User sends message
    ↓
ChatViewModel.sendMessage()
    ↓
Load active messages from database
    ↓
CompactionManager.checkAndCompact(threadId, messages)
    ↓
    ├─ <75% full → CompactionResult.NotNeeded
    └─ ≥75% full → CompactionResult.Success
            ↓
       Summarize oldest messages
            ↓
       Save ConversationState
            ↓
       Mark messages as archived
    ↓
Reload active messages (after compaction)
    ↓
ContextBuilder.buildContext(threadId, messages, systemPrompt)
    ↓
    ├─ No summary: Just systemPrompt + messages
    └─ Has summary: systemPrompt + <previous_context> + messages
    ↓
LlmChatModelHelper.generateResponse(context)
    ↓
Response returned to user
    ↓
Update ConversationThread.estimatedTokens
```

## Database Schema Changes

### New Table: conversation_state
```sql
CREATE TABLE conversation_state (
    threadId TEXT PRIMARY KEY NOT NULL,
    runningSummary TEXT NOT NULL,
    turnsSummarized INTEGER NOT NULL,
    lastCompactionTime INTEGER NOT NULL
)
```

### Modified Table: conversation_messages
```sql
ALTER TABLE conversation_messages
ADD COLUMN archived INTEGER NOT NULL DEFAULT 0
```

**Migration**: MIGRATION_6_7 in DatabaseMigrations.kt

## Configuration

### Thresholds (Hard-Coded for MVP)
- **Context Limit**: 8,192 tokens (Gemma 2B spec)
- **Trigger**: 75% = 6,144 tokens
- **Target**: 40% = 3,276 tokens
- **Summary Max**: 1,000 chars (~250 tokens)

### Token Estimation
- **Heuristic**: chars / 4
- **Accuracy**: ±15% (acceptable with buffer)
- **Future**: Replace with BPE tokenizer in Phase 2

## Testing Strategy

### Unit Tests
- TokenEstimator: Verify char/token conversion
- CompactionTrigger: Verify thresholds and eviction logic
- ConversationStateDao: Verify CRUD operations

### Integration Tests
- End-to-end: 30-turn conversation → compaction → continuation
- Summary persistence: Verify across app restart
- Edge cases: Empty state, all messages evicted, etc.

### Manual Tests
- DroidRun: Send 30+ messages, verify no crash
- Database inspection: Verify conversation_state populated
- Context coherence: Verify responses remain relevant

## Performance Considerations

### Summarization Cost
- **When**: Only at 75% threshold (infrequent)
- **Where**: Background thread (Dispatchers.IO)
- **Impact**: 2-5 seconds per compaction (acceptable)

### Token Counting Cost
- **When**: After each message (frequent)
- **Cost**: O(n) where n = message count
- **Optimization**: Cache and update incrementally (Phase 2)

## Error Handling

### Summarization Failure
- **Fallback**: Use truncated message list (first 100 chars of each)
- **Log**: CrashlyticsLogger.recordNonFatalError()
- **Continue**: Don't block user interaction

### Database Failure
- **Fallback**: Continue without compaction
- **Log**: Error with thread ID
- **Retry**: Next compaction attempt

### Migration Failure
- **Strategy**: Fallback to destructive migration (rare)
- **Data loss**: Only affects new conversation_state (not existing messages)

## Future Enhancements (Phase 2+)

### Accuracy Improvements
- BPE tokenizer for precise token counting
- Prompt A/B testing for better summaries
- Entity extraction for fact preservation

### User Experience
- User-configurable thresholds (70/80/90%)
- Show "compressed N turns" badge
- Archive search/recall feature

### Performance
- Incremental token counting (cache + delta)
- Lazy summarization (background job)
- Parallel summarization (batch multiple evictions)

### Advanced Features
- Topic-based segmentation
- Commitment tracking
- Multi-level summaries (summary of summaries)

## References
- Pattern: [LangChain ConversationSummaryBufferMemory](https://www.pinecone.io/learn/series/langchain/langchain-conversational-memory/)
- Model: [Gemma 2B Specification](https://huggingface.co/google/gemma-2b)
- Existing: ConversationDao.kt, PersonaManager.kt, LlmChatModelHelper.kt
```

## MODIFIED

### app-database.md
```diff
# App Database Schema

## Entities

### ConversationThread
...existing fields...
+ estimatedTokens: Int (ALREADY EXISTS - now actively used)
+ lastTokenUpdate: Long (ALREADY EXISTS - now actively used)

+### ConversationState (NEW)
+Stores running summary for conversations that have been compacted.
+- threadId: String (PK)
+- runningSummary: String
+- turnsSummarized: Int
+- lastCompactionTime: Long

### ConversationMessage
...existing fields...
++ archived: Boolean (NEW - marks messages evicted during compaction)

## Migrations

+### MIGRATION_6_7
+- Add conversation_state table
+- Add archived column to conversation_messages
```

### conversationDao.md (if exists)
```diff
# ConversationDao

## Methods

...existing methods...

++ markArchived(messageId: Long)
+  Mark a message as archived (evicted during compaction)
+
++ getActiveMessages(threadId: Long): List<ConversationMessage>
+  Get non-archived messages for a thread
```

### llm-chat.md (if exists)
```diff
# LLM Chat

## Flow

1. User sends message
2. Save to database
+3. Check compaction (NEW)
+   - If >75% tokens, trigger compaction
+   - Summarize old messages
+   - Reset session with summary
4. Generate response
5. Display to user

## Components

...existing...

++ CompactionManager: Manages context window
++ ContextBuilder: Injects summary into prompts
```

## REMOVED

None. This is purely additive - no specs removed.

---

## Summary of Changes

**New Specifications**: 1 (conversation-memory.md)
**Modified Specifications**: 2-3 (app-database.md, conversationDao.md, llm-chat.md)
**Removed Specifications**: 0

**Total Spec Lines Added**: ~500
**Total Code Lines Added**: ~200
**New Dependencies**: 0
**Database Tables Added**: 1 (conversation_state)
**Database Columns Added**: 1 (archived flag)
