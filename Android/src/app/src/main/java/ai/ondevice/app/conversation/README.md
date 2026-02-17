# Conversation Context Compression

Enable infinite conversations by automatically compressing context when approaching token limits.

## Overview

This package implements **LangChain ConversationSummaryBufferMemory** pattern for on-device LLMs, allowing conversations to exceed the model's context window without losing coherence.

**Pattern**: Progressive summarization with self-compaction
**Reference**: [LangChain ConversationSummaryBufferMemory](https://python.langchain.com/docs/modules/memory/types/summary_buffer)

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  User sends message                                         │
│     │                                                       │
│     ▼                                                       │
│  LlmChatViewModel.generateResponse()                       │
│     │                                                       │
│     ├─► TokenEstimator.estimate(messages)                  │
│     │   └─► Returns: ~2,500 tokens                         │
│     │                                                       │
│     ├─► CompactionManager.checkAndCompact()                │
│     │   ├─► if (tokens < 3,072) → NotNeeded               │
│     │   └─► if (tokens >= 3,072) → executeCompaction()    │
│     │       ├─► Identify oldest messages to evict          │
│     │       ├─► LLM.summarize(evicted messages)           │
│     │       ├─► Save ConversationState(summary)           │
│     │       └─► Delete evicted messages                    │
│     │                                                       │
│     ├─► ContextBuilder.buildContext()                      │
│     │   ├─► Inject: <previous_context>summary</...>       │
│     │   └─► Append: Recent messages                        │
│     │                                                       │
│     └─► LlmChatModelHelper.runInference(context)          │
│         └─► LLM generates response with full context      │
└─────────────────────────────────────────────────────────────┘
```

## Components

### TokenEstimator
- **Purpose**: Estimate token count from text
- **Algorithm**: `chars / 4` (good enough for MVP)
- **Accuracy**: ±10% compared to BPE tokenizer
- **Why not BPE**: Adds 100+ lines, dependencies, minimal accuracy gain

```kotlin
TokenEstimator.estimate("hello world") // Returns: 2 tokens
TokenEstimator.estimate(messages)      // Returns: sum of all message tokens
```

### CompactionManager
- **Purpose**: Orchestrate compaction workflow
- **Trigger**: 75% of context limit (3,072 tokens for 4,096 max)
- **Target**: 40% after compaction (1,638 tokens)
- **Strategy**: Evict oldest messages, summarize them, save summary

```kotlin
val result = compactionManager.checkAndCompact(
    threadId = threadId,
    messages = messages,
    llmHelper = LlmChatModelHelper,
    model = model
)

when (result) {
    is CompactionResult.NotNeeded -> { /* Continue normally */ }
    is CompactionResult.Success -> {
        Log.d(TAG, "Evicted ${result.evictedCount} turns")
    }
    is CompactionResult.Failed -> {
        Log.e(TAG, "Compaction failed: ${result.error}")
        // Continue without compression (fail-safe)
    }
}
```

### ContextBuilder
- **Purpose**: Build inference context with summary injection
- **Format**: `<previous_context>summary</previous_context>` + recent messages
- **Integration**: Called before every inference

```kotlin
val context = contextBuilder.buildContext(
    threadId = threadId,
    recentMessages = messages,
    systemPrompt = "" // Optional
)
// Result:
// <previous_context>
// User asked about features. Assistant explained benefits.
// </previous_context>
//
// User: What about pricing?
// Assistant: OnDevice AI costs $8 one-time.
```

### SummarizationPrompts
- **Purpose**: LangChain-style progressive summarization prompts
- **Pattern**: Merge new summary with existing summary
- **Style**: Concise, third-person, factual

```kotlin
val prompt = SummarizationPrompts.buildProgressiveSummary(
    existingSummary = "User asked about features.",
    messagesToSummarize = listOf(/* messages to evict */)
)
// Prompt instructs LLM to:
// 1. Read existing summary
// 2. Summarize new messages
// 3. Merge into coherent cumulative summary
```

### ConversationState (Room Entity)
- **Purpose**: Persist summary state across app restarts
- **Table**: `conversation_state`
- **Migration**: Database version 7 → 8

```kotlin
@Entity(tableName = "conversation_state")
data class ConversationState(
    @PrimaryKey val threadId: Long,
    val runningSummary: String,     // Max 1500 chars
    val turnsSummarized: Int,       // Cumulative count
    val lastCompactionTime: Long    // Timestamp
)
```

## Thresholds

| Metric | Value | Rationale |
|--------|-------|-----------|
| **Max context** | 4,096 tokens | Conservative (Gemma 2B actual: 8,192) |
| **Trigger** | 3,072 tokens (75%) | Early enough to avoid truncation |
| **Target** | 1,638 tokens (40%) | Balances freshness vs compression frequency |
| **Summary limit** | 1,500 chars (~375 tokens) | Prevents summary from dominating context |

## Usage in LlmChatViewModel

```kotlin
class LlmChatViewModelBase(
    conversationDao: ConversationDao,
    // ...
) : ChatViewModel(conversationDao) {

    // Lazy initialization (only created when needed)
    private val compactionManager by lazy {
        CompactionManager(conversationDao)
    }

    fun generateResponse(model: Model, input: String, ...) {
        viewModelScope.launch(Dispatchers.Default) {
            // ...

            // STEP 1: Check if compaction needed (before inference)
            try {
                val threadId = currentThreadId
                if (threadId != null) {
                    val messages = conversationDao.getMessagesForThread(threadId)
                    val result = compactionManager.checkAndCompact(
                        threadId, messages, LlmChatModelHelper, model
                    )
                    if (result is CompactionResult.Success) {
                        Log.d(TAG, "Compacted: evicted ${result.evictedCount} turns")
                    }
                }
            } catch (e: Exception) {
                // Fail-safe: Continue without compression
                Log.w(TAG, "Compaction failed, continuing without compression", e)
            }

            // STEP 2: Run inference (context with summary injected automatically)
            LlmChatModelHelper.runInference(model, input, ...)
        }
    }
}
```

## Progressive Summarization Example

### First Compaction (Turn 1-10 evicted)
```
Summary: "User asked about OnDevice AI features. Assistant explained on-device inference,
privacy benefits, and supported models (Gemma 2B, Phi-3)."
```

### Second Compaction (Turn 11-20 evicted)
```
Summary: "User asked about OnDevice AI features. Assistant explained on-device inference,
privacy benefits, and supported models (Gemma 2B, Phi-3). User then asked about pricing.
Assistant explained $8 one-time payment with no subscriptions or ads."
```

### Third Compaction (Turn 21-30 evicted)
```
Summary: "User explored OnDevice AI: features (on-device inference, privacy), models
(Gemma 2B, Phi-3), and pricing ($8 one-time). Then discussed technical specs: 4GB RAM
minimum, 8GB recommended, supports Android 12+."
```

**Notice**: Each summary is **cumulative** and **progressively condensed**.

## Testing

### Unit Tests
- `TokenEstimatorTest.kt` - Token counting accuracy
- `ConversationStateDaoTest.kt` - Database operations

### Integration Tests
- `CompactionIntegrationTest.kt` - End-to-end compaction workflow

### Manual Testing
1. Start fresh conversation
2. Send 30+ messages (simulate long conversation)
3. Verify app doesn't crash
4. Verify responses remain coherent
5. Check database: `conversation_state` table populated

```sql
SELECT * FROM conversation_state WHERE threadId = ?;
-- Should show runningSummary with cumulative context
```

## Performance

| Operation | Time | Notes |
|-----------|------|-------|
| Token estimation | <1ms | Simple string length / 4 |
| Compaction check | ~5ms | Database query + token sum |
| Summarization | 2-5s | LLM inference (async, non-blocking) |
| Context injection | <1ms | String concatenation |

**Impact on user**: ~3s pause when compaction triggers (every ~30-50 messages)

## Limitations

- **Token estimation accuracy**: ±10% (chars/4 heuristic vs BPE tokenizer)
- **Summary quality**: Depends on LLM's summarization ability
- **Summary length**: Hard-capped at 1,500 chars to prevent context bloat
- **Compaction frequency**: Every ~30-50 messages (depends on message length)

## Future Enhancements (Phase 2)

- [ ] Replace chars/4 with BPE tokenizer for accuracy
- [ ] User-configurable thresholds (70%, 80%, 90%)
- [ ] A/B test summarization prompts for quality
- [ ] Analytics: Track compaction frequency, summary quality
- [ ] UI: Show "compressed N turns" indicator in chat
- [ ] Archive search/recall feature

## References

- [LangChain ConversationSummaryBufferMemory](https://python.langchain.com/docs/modules/memory/types/summary_buffer)
- [OpenSpec Proposal](../../../../../openspec/changes/infinite-conversation-minimal/proposal.md)
- [Tasks Checklist](../../../../../openspec/changes/infinite-conversation-minimal/tasks.md)
