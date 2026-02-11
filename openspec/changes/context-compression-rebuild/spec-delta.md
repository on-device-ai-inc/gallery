# Spec Delta: context-compression-rebuild

This document describes the specification changes resulting from rebuilding the context compression system from scratch.

---

## REMOVED

### context-compression-v1.md (Broken Implementation)

**Location**: `openspec/specs/context-compression-v1.md` (if it existed)

**Rationale**: The v1 implementation (TokenMonitor, ContextManager, ConversationCompressor, QualityMonitor) was broken and has been deleted. Any associated spec is now obsolete.

---

## ADDED

### context-compression-v2.md (Complete Rebuild)

**Location**: `openspec/specs/context-compression-v2.md`

```markdown
# Context Compression v2 Specification

## Overview
Automatic compression of conversation context when approaching model token limits, preserving response quality while enabling 100+ message conversations.

## Problem Statement

**Without Compression**:
- Model context window: 4096 tokens
- Average message: ~40 tokens
- Max conversation length: ~100 messages
- Beyond 100 messages: Context overflow → chat breaks

**With Compression**:
- Compress context when approaching limit (>3400 tokens)
- Reduce tokens by ≥20% while preserving quality
- Enable indefinite conversation length
- Maintain response quality ≥90%

## Architecture

### Components

#### 1. TokenCounter
Accurate token counting using model's actual tokenizer, with fallback to character-based estimation.

```kotlin
class TokenCounter(private val tokenizerPath: String?) {
    companion object {
        const val MAX_CONTEXT_TOKENS = 4096
        const val COMPRESSION_THRESHOLD = 3400  // 83% of max
        const val CHARS_PER_TOKEN_FALLBACK = 4f
    }

    fun countTokens(text: String): Int {
        return if (tokenizerPath != null) {
            loadTokenizer(tokenizerPath).encode(text).size
        } else {
            (text.length / CHARS_PER_TOKEN_FALLBACK).toInt()
        }
    }
}
```

**Requirements**:
- Accuracy: ±5% of actual tokenizer
- Fallback: Character-based estimation if tokenizer unavailable
- Performance: <100ms for 100-message conversation

#### 2. CompressionStrategy
Defines how to compress messages.

```kotlin
sealed class CompressionStrategy {
    /**
     * Keep last N messages verbatim (recent context is most important)
     */
    data class SlidingWindow(val keepLast: Int = 10) : CompressionStrategy()

    /**
     * Score messages by importance, keep high-scoring ones
     * Importance factors:
     * - Starred by user (+0.5)
     * - System messages (+0.3)
     * - Long messages >500 chars (+0.2)
     * - Contains code blocks (+0.1)
     * - First message (+0.2)
     */
    data class ImportanceScoring(val minScore: Float = 0.7f) : CompressionStrategy()

    /**
     * Summarize old messages into single summary message
     */
    data class Summarization(
        val summaryTokenBudget: Int = 200,
        val olderThanMessages: Int = 15
    ) : CompressionStrategy()
}
```

**Strategy Application Order**:
1. **SlidingWindow**: Always keep last 10 messages verbatim
2. **ImportanceScoring**: From older messages, keep important ones (score >0.7)
3. **Summarization**: Summarize remaining old messages into 200-token summary

#### 3. ContextCompressor
Core compression logic.

```kotlin
class ContextCompressor(
    private val tokenCounter: TokenCounter,
    private val inferenceEngine: InferenceEngine
) {
    suspend fun compress(
        messages: List<ConversationMessage>,
        maxTokens: Int = TokenCounter.MAX_CONTEXT_TOKENS
    ): CompressionResult {
        val currentTokens = tokenCounter.countTokens(messages.joinToString())

        if (currentTokens <= maxTokens) {
            return CompressionResult.NoCompressionNeeded(messages)
        }

        // 1. Keep last 10 messages (sliding window)
        val recentMessages = messages.takeLast(10)
        val oldMessages = messages.dropLast(10)

        // 2. Score old messages by importance
        val importantOldMessages = oldMessages.filter { scoreImportance(it) > 0.7f }

        // 3. Summarize remaining old messages
        val toSummarize = oldMessages - importantOldMessages.toSet()
        val summary = if (toSummarize.isNotEmpty()) {
            summarize(toSummarize, tokenBudget = 200)
        } else null

        // 4. Reconstruct context
        val compressedMessages = buildList {
            if (summary != null) add(summary)
            addAll(importantOldMessages)
            addAll(recentMessages)
        }

        val finalTokens = tokenCounter.countTokens(compressedMessages.joinToString())

        return CompressionResult.Compressed(
            messages = compressedMessages,
            originalTokens = currentTokens,
            finalTokens = finalTokens,
            reductionPercent = ((currentTokens - finalTokens) * 100f / currentTokens)
        )
    }

    private fun scoreImportance(message: ConversationMessage): Float {
        var score = 0f
        if (message.isStarred) score += 0.5f
        if (message.role == "system") score += 0.3f
        if (message.content.length > 500) score += 0.2f
        if (message.content.contains("```")) score += 0.1f
        if (message.isFirstInConversation) score += 0.2f
        return score.coerceAtMost(1.0f)
    }

    private suspend fun summarize(
        messages: List<ConversationMessage>,
        tokenBudget: Int
    ): ConversationMessage? {
        if (messages.isEmpty()) return null

        val prompt = """
            Summarize the following conversation in $tokenBudget tokens or less.
            Focus on key topics, decisions, and important context.

            ${messages.joinToString("\n") { "${it.role}: ${it.content}" }}

            Summary:
        """.trimIndent()

        val summary = try {
            inferenceEngine.generate(prompt, maxTokens = tokenBudget)
        } catch (e: Exception) {
            Log.e("ContextCompressor", "Summarization failed: ${e.message}", e)
            return null
        }

        return ConversationMessage(
            role = "system",
            content = "**Previous conversation summary**: $summary",
            timestamp = System.currentTimeMillis(),
            isSystemGenerated = true
        )
    }
}

sealed class CompressionResult {
    data class NoCompressionNeeded(val messages: List<ConversationMessage>) : CompressionResult()

    data class Compressed(
        val messages: List<ConversationMessage>,
        val originalTokens: Int,
        val finalTokens: Int,
        val reductionPercent: Float
    ) : CompressionResult()

    data class Failed(val error: Exception, val originalMessages: List<ConversationMessage>) : CompressionResult()
}
```

#### 4. CompressionQualityMonitor
Measures compression quality using golden QA dataset.

```kotlin
class CompressionQualityMonitor(
    private val goldenDataset: GoldenQADataset,
    private val inferenceEngine: InferenceEngine
) {
    suspend fun measureQuality(
        originalContext: List<ConversationMessage>,
        compressedContext: List<ConversationMessage>
    ): QualityScore {
        val scores = goldenDataset.testCases.map { testCase ->
            val responseOriginal = inferenceEngine.generate(
                context = originalContext,
                query = testCase.query
            )

            val responseCompressed = inferenceEngine.generate(
                context = compressedContext,
                query = testCase.query
            )

            semanticSimilarity(responseOriginal, responseCompressed)
        }

        return QualityScore(
            averageScore = scores.average(),
            minScore = scores.minOrNull() ?: 0.0,
            maxScore = scores.maxOrNull() ?: 0.0,
            testCaseCount = scores.size
        )
    }

    private fun semanticSimilarity(text1: String, text2: String): Double {
        // Simple word overlap similarity (Jaccard index)
        val words1 = text1.lowercase().split(Regex("\\W+")).toSet()
        val words2 = text2.lowercase().split(Regex("\\W+")).toSet()

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return if (union > 0) intersection.toDouble() / union else 0.0
    }
}

data class QualityScore(
    val averageScore: Double,
    val minScore: Double,
    val maxScore: Double,
    val testCaseCount: Int
)
```

### Integration into Chat Flow

```kotlin
// LlmChatViewModel.kt
class LlmChatViewModel(
    private val inferenceEngine: InferenceEngine,
    private val compressor: ContextCompressor,
    private val conversationDao: ConversationDao
) : ViewModel() {

    suspend fun sendMessage(userMessage: String) {
        // 1. Load conversation history
        val messages = conversationDao.getMessages(currentConversationId)

        // 2. Add user message
        val updatedMessages = messages + ConversationMessage(
            role = "user",
            content = userMessage,
            timestamp = System.currentTimeMillis()
        )

        // 3. Check if compression needed
        val context = if (shouldCompress(updatedMessages)) {
            Log.d("LlmChat", "Compression triggered (${updatedMessages.size} messages)")
            when (val result = compressor.compress(updatedMessages)) {
                is CompressionResult.Compressed -> {
                    Log.d("LlmChat", "Compressed: ${result.originalTokens} → ${result.finalTokens} (${result.reductionPercent}% reduction)")
                    result.messages
                }
                is CompressionResult.NoCompressionNeeded -> {
                    result.messages
                }
                is CompressionResult.Failed -> {
                    Log.e("LlmChat", "Compression failed: ${result.error.message}")
                    result.originalMessages
                }
            }
        } else {
            updatedMessages
        }

        // 4. Generate response with (possibly compressed) context
        val response = inferenceEngine.generate(context)

        // 5. Save response
        conversationDao.insertMessage(response)
    }

    private fun shouldCompress(messages: List<ConversationMessage>): Boolean {
        val tokenCount = compressor.tokenCounter.countTokens(messages.joinToString())
        return tokenCount > TokenCounter.COMPRESSION_THRESHOLD
    }
}
```

## Functional Requirements

### FR-1: Automatic Compression Trigger
- **When**: Context exceeds 3400 tokens (83% of 4096 max)
- **Action**: Automatically compress context before inference
- **User Visibility**: Transparent (user doesn't see compression)
- **Logging**: Log compression trigger, metrics

### FR-2: Quality Preservation
- **Target**: ≥90% response quality post-compression (measured via GQA-006)
- **Measurement**: Compare responses with original vs compressed context
- **Threshold**: If quality <90%, adjust compression parameters

### FR-3: Recent Context Priority
- **Rule**: Always keep last 10 messages verbatim
- **Rationale**: Recent context is most relevant for next response
- **Never Compress**: Last 10 messages, starred messages, system messages

### FR-4: Important Message Preservation
- **Starred Messages**: Never compress (user explicitly marked as important)
- **System Messages**: Never compress (errors, warnings, critical info)
- **First Message**: Never compress (establishes conversation context)
- **Long Messages** (>500 chars): Higher importance score (likely detailed explanations)
- **Code Blocks**: Higher importance score (technical discussions)

### FR-5: Summarization for Old Messages
- **Target**: Messages older than last 15 messages AND not important
- **Method**: LLM-based summarization
- **Budget**: 200 tokens for summary
- **Format**: "**Previous conversation summary**: [summary]"
- **Fallback**: If summarization fails, drop oldest messages

### FR-6: Token Reduction Target
- **Target**: ≥20% token reduction
- **Verify**: After compression, context should be <3400 tokens
- **Enforce**: If still too large, apply more aggressive compression

## Non-Functional Requirements

### NFR-1: Performance
- Token counting: <100ms for 100-message conversation
- Compression: <500ms for 100-message conversation
- Non-blocking: Run compression asynchronously (don't block UI)
- Memory: <50MB additional memory during compression

### NFR-2: Accuracy
- Token counting: ±5% of actual tokenizer
- Quality preservation: ≥90% of original response quality
- Compression reduction: Reliably achieve ≥20% reduction

### NFR-3: Reliability
- No crashes: Handle 500+ message conversations
- Graceful degradation: If compression fails, use original context
- Error handling: Log all errors, never block chat

### NFR-4: Observability
- Log compression trigger (when, why)
- Log compression metrics (tokens before/after, reduction %)
- Log compression time
- Log quality scores
- Monitor drift (7-day rolling average quality)
- Alert if quality <85%

## Quality Monitoring & Drift Detection

### Golden QA Dataset (GQA-006)

**Purpose**: Measure compression quality objectively

**Structure**:
```yaml
test_case_id: GQA-006-001
category: long_conversation
description: 50-message technical discussion about Android MVVM
original_context:
  - role: user
    content: "Explain MVVM architecture"
  - role: assistant
    content: "[detailed MVVM explanation]"
  # ... 48 more messages
query: "Can you summarize our discussion about MVVM and give me the key components?"
expected_quality:
  maintains_key_points: true
  references_original_context: true
  coherent: true
min_acceptable_score: 0.85
```

**Categories** (50+ test cases total):
1. **Long Conversations** (10 cases): 50+ messages, verify coherence maintained
2. **Code Discussions** (10 cases): Code blocks, verify code preserved/summarized correctly
3. **Multi-Topic** (10 cases): 3-5 topics, verify topic boundaries maintained
4. **Important Preservation** (10 cases): Starred/system messages, verify NEVER removed
5. **Summarization Quality** (10 cases): Old messages, verify summaries capture key points

**Evaluation**:
- For each test case, generate response with original context
- Generate response with compressed context
- Calculate semantic similarity (word overlap, embeddings, etc.)
- Pass if similarity ≥ min_acceptable_score
- Overall pass rate ≥90% required

### Drift Detection

**Metric**: 7-day rolling average quality score

**Process**:
1. Store compression metrics after each compression (timestamp, quality score)
2. Daily, calculate average quality score for last 7 days
3. If average <0.85, trigger alert

**Alert Actions**:
- Log warning: "Context compression quality degraded (7-day avg: X)"
- Fire Firebase event: "context_compression_quality_degraded"
- (Optional) Show warning in app settings

**Investigation**:
- Review recent compression logs
- Check if model version changed
- Check if average conversation length increased
- Adjust compression parameters if needed

## Testing Requirements

### Unit Tests
- [ ] TokenCounterTest: Verify token counting within ±5% accuracy
- [ ] ContextCompressorTest: Test sliding window, importance scoring, summarization
- [ ] CompressionQualityMonitorTest: Test quality measurement logic
- [ ] CompressionMetricsTest: Test metrics calculation

### Integration Tests
- [ ] 100-message conversation → compression triggered, context reduced
- [ ] Starred messages → preserved in compressed context
- [ ] System messages → preserved in compressed context
- [ ] Compression latency < 500ms
- [ ] Memory usage < 50MB during compression

### Golden QA Dataset Evaluation
- [ ] Run GQA-006 evaluation suite (50+ test cases)
- [ ] Verify ≥90% pass rate
- [ ] Verify quality scores ≥0.85 per category

### Manual Tests
- [ ] Long conversation (50+ messages) → verify AI responses coherent
- [ ] Star messages → continue conversation → verify AI remembers starred content
- [ ] Very long conversation (200+ messages) → verify no crashes, compression works

## Acceptance Criteria

- [ ] AC-1: Conversations support 100+ messages without breaking
- [ ] AC-2: Compression triggered when context >3400 tokens
- [ ] AC-3: Token reduction ≥20%
- [ ] AC-4: Last 10 messages always kept verbatim
- [ ] AC-5: Starred/system messages never compressed
- [ ] AC-6: Summarization generates coherent summaries
- [ ] AC-7: Token counting accuracy ±5%
- [ ] AC-8: Response quality ≥90% post-compression (GQA-006)
- [ ] AC-9: Compression latency <500ms
- [ ] AC-10: No crashes with 500+ message conversations
- [ ] AC-11: Drift detection alerts when quality <85%
- [ ] AC-12: All unit tests pass
- [ ] AC-13: All integration tests pass
- [ ] AC-14: GQA-006 pass rate ≥90%

## Monitoring & Metrics

### CompressionMetrics Data Model

```kotlin
data class CompressionMetrics(
    val id: Long = 0,
    val conversationId: Long,
    val timestamp: Long,
    val originalTokens: Int,
    val finalTokens: Int,
    val reductionPercent: Float,
    val qualityScore: Float,
    val compressionTimeMs: Long,
    val strategy: String,  // "sliding_window+importance+summarization"
    val messageCountBefore: Int,
    val messageCountAfter: Int
)
```

### Firebase Analytics Events

**Event**: `context_compression_triggered`
- `original_tokens` (int)
- `final_tokens` (int)
- `reduction_percent` (float)
- `message_count` (int)

**Event**: `context_compression_quality_degraded`
- `seven_day_avg_quality` (float)
- `threshold` (float): 0.85
- `test_case_failures` (int)

**Event**: `context_compression_failed`
- `error_type` (string)
- `message_count` (int)
- `fallback_used` (bool): true (used original context)

### Debug Dashboard (Optional)

Local debug screen showing:
- Recent compressions (last 10)
- 7-day quality trend (line chart)
- Token reduction distribution (histogram)
- Drift alert status

## References

- [Anthropic Context Windows](https://docs.anthropic.com/claude/docs/context-windows)
- [LongBench Benchmark](https://arxiv.org/abs/2308.14508)
- [Lost in the Middle](https://arxiv.org/abs/2307.03172)
- GQA-006 Golden QA Dataset
- error-handling-patterns.md (Compression failure handling)

## Known Issues & Limitations

### Tokenizer Availability
- **Issue**: Actual tokenizer may not be available on all devices
- **Mitigation**: Fallback to chars/4 estimation (±5% accuracy acceptable)

### Summarization Quality
- **Issue**: LLM summarization may lose nuance
- **Mitigation**: Extensive testing with GQA-006, tune prompts

### Compression Latency
- **Issue**: Summarization adds 200-500ms latency
- **Mitigation**: Run async, acceptable trade-off for long conversations

### First Compression in Conversation
- **Issue**: First time compression runs, user may notice slight delay
- **Mitigation**: Compression threshold set at 83% (not 100%) to avoid emergency compression

## Migration from v1

**v1 Implementation** (DELETED):
- TokenMonitor.kt (146 lines) - DELETED
- ContextManager.kt (158 lines) - DELETED
- ConversationCompressor.kt (152 lines) - DELETED
- QualityMonitor.kt (64 lines) - DELETED

**v2 Implementation** (NEW):
- TokenCounter.kt - NEW
- CompressionStrategy.kt - NEW
- ContextCompressor.kt - NEW (different implementation)
- CompressionQualityMonitor.kt - NEW
- GQA-006 dataset - NEW

**Migration Steps**:
1. Delete all v1 files
2. Remove all v1 references
3. Implement v2 from scratch
4. Test with GQA-006
5. Monitor quality with drift detection

**No Data Migration**: v1 never worked, so no data to migrate.
```

---

## MODIFIED

### evaluation-framework.md (Add GQA-006)

**Location**: `openspec/specs/evaluation-framework.md`

```diff
# Evaluation Framework Specification

## Overview
Comprehensive quality evaluation using golden QA datasets and automated LLM-judge evaluation.

## Golden QA Datasets

Golden QA (GQA) datasets are curated test cases with known-good responses for measuring specific quality dimensions.

### GQA-001 to GQA-005
(Existing datasets defined in evaluation-framework.md)

+ ### GQA-006: Context Compression Quality
+
+ **Purpose**: Measure compression quality by comparing responses before/after compression
+
+ **Structure**:
+ ```yaml
+ test_case_id: GQA-006-NNN
+ category: [long_conversation | code_discussion | multi_topic | important_preservation | summarization_quality]
+ description: Brief description
+ original_context:
+   - role: user
+     content: "Message content"
+   - role: assistant
+     content: "Response content"
+   # ... more messages
+ query: "Question to test comprehension of compressed context"
+ expected_quality:
+   maintains_key_points: true
+   references_original_context: true
+   coherent: true
+ min_acceptable_score: 0.85
+ ```
+
+ **Categories**:
+ 1. **Long Conversations** (10 cases): 50+ messages, technical discussions
+ 2. **Code Discussions** (10 cases): Code blocks, verify preservation
+ 3. **Multi-Topic** (10 cases): 3-5 topics, verify topic continuity
+ 4. **Important Preservation** (10 cases): Starred/system messages, MUST preserve
+ 5. **Summarization Quality** (10 cases): Old messages, verify summary captures key points
+
+ **Evaluation Method**:
+ - Generate response with original context → responseOriginal
+ - Compress context
+ - Generate response with compressed context → responseCompressed
+ - Calculate semantic similarity(responseOriginal, responseCompressed)
+ - Pass if similarity ≥ min_acceptable_score
+
+ **Pass Criteria**:
+ - ≥90% of test cases pass (45/50)
+ - Average quality score ≥0.90
+ - Per-category pass rate ≥85%
+
+ **Ownership**: Context compression feature
+ **Update Frequency**: Add new test cases when compression quality issues discovered

## Drift Detection

+ **GQA-006 Drift Monitoring**:
+ - Store compression quality scores after each compression
+ - Calculate 7-day rolling average
+ - Alert if average <0.85
+ - Investigate: model changes, conversation pattern shifts, compression parameter drift
```

---

## SUMMARY

| Spec File | Type | Changes |
|-----------|------|---------|
| `context-compression-v1.md` | REMOVED | Broken implementation spec deleted |
| `context-compression-v2.md` | ADDED | Complete rebuild spec with TokenCounter, ContextCompressor v2, quality monitoring |
| `evaluation-framework.md` | MODIFIED | Added GQA-006 dataset for compression quality measurement |

## Rationale

### Why Remove v1 Spec?
v1 implementation was broken (user-reported). Deleting the spec ensures no confusion about which approach is current.

### Why Add Comprehensive v2 Spec?
The rebuild is a significant feature (48 hours, 70 tasks). A detailed spec ensures:
- Clear architecture (4 components with interfaces)
- Testability (GQA-006 golden dataset)
- Quality gates (≥90% pass rate, drift detection)
- Maintainability (comprehensive logging, metrics)

### Why Modify evaluation-framework.md?
GQA-006 is part of the broader evaluation framework. Adding it to evaluation-framework.md:
- Centralizes all golden datasets
- Establishes compression quality as a measured metric
- Enables ongoing drift detection

## Validation

After implementation:
- [ ] TokenCounter.kt matches spec (±5% accuracy)
- [ ] ContextCompressor.kt implements all 3 strategies per spec
- [ ] CompressionQualityMonitor.kt uses GQA-006 per spec
- [ ] LlmChatViewModel integration matches spec
- [ ] All acceptance criteria met
- [ ] GQA-006 evaluation passes (≥90%)
