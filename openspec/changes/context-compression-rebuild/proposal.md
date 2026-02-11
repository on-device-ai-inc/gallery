# Proposal: context-compression-rebuild

## Summary
Delete broken context compression implementation and rebuild from scratch with proper testing and quality monitoring.

## Motivation

**User Report**: "CONTEXT COMPRESSION NOT WORKING PROPERLY, DELETE THE ENTIRE IMPLEMENTATION AND START FRESH"

**Current State**:
- ❌ **TokenMonitor.kt** (146 lines) - BROKEN, must delete
- ❌ **ContextManager.kt** (158 lines) - BROKEN, must delete
- ❌ **ConversationCompressor.kt** (152 lines) - BROKEN, must delete
- ❌ **QualityMonitor.kt** (64 lines) - BROKEN, must delete
- **Total**: 520+ lines of non-functional code

**What Was Attempted**:
```kotlin
// TokenMonitor.kt - Existing broken implementation
class TokenMonitor(private val modelCapability: ModelCapability) {
    companion object {
        const val MAX_CONTEXT_TOKENS = 4096
        const val APPROACHING_THRESHOLD_PERCENT = 84  // 3,010 tokens
        const val CHARS_PER_TOKEN = 4f
    }
    fun estimateTokens(text: String): Int = (text.length / CHARS_PER_TOKEN).toInt()
}

// ConversationCompressor.kt - Existing broken implementation
class ConversationCompressor(private val tokenMonitor: TokenMonitor) {
    companion object {
        private const val RECENT_MESSAGES_TO_KEEP = 10  // Sliding window
    }
    fun compress(messages: List<ConversationMessage>, targetTokenReduction: Int)
}
```

**Why It's Broken** (hypotheses):
1. Token estimation (chars/4) too crude → misjudges context length
2. Sliding window (keep last 10) loses important context
3. No summarization → just drops messages
4. No quality verification → can't detect when compression hurts responses
5. Integration incomplete → not actually called in chat flow

**Impact**:
- **P0 Critical**: Users hit 4096 token limit → chat breaks
- **User Experience**: Long conversations unusable (context lost)
- **Quality**: AI responses degrade without context
- **Trust**: Users confused why AI "forgets" earlier conversation

**Business Priority**: P0 (Critical) - Blocks long conversations, core use case

## Scope

### IN SCOPE
- ✅ DELETE all 4 broken files (520+ lines)
- ✅ Design compression v2 architecture from scratch
- ✅ Implement accurate token counting (use actual tokenizer)
- ✅ Implement smart compression strategies (sliding window + importance scoring + summarization)
- ✅ Integrate compression into LlmChatViewModel
- ✅ Add quality monitoring (drift detection)
- ✅ Create golden QA dataset (GQA-006) for compression testing
- ✅ Validate compression doesn't degrade response quality

### OUT OF SCOPE
- ❌ Multi-turn reasoning optimization (separate feature)
- ❌ RAG integration (separate feature)
- ❌ Model-specific context tuning (use generic 4096 limit)
- ❌ User-facing compression controls (fully automatic)

## Acceptance Criteria

### Functional Requirements
- [ ] **AC1**: Conversations support 100+ messages without breaking
- [ ] **AC2**: When context approaches 4096 tokens, compression automatically triggered
- [ ] **AC3**: Compression reduces context by ≥20% while preserving quality
- [ ] **AC4**: Last 10 messages always kept verbatim (sliding window)
- [ ] **AC5**: Important messages (user-starred, system messages) never compressed
- [ ] **AC6**: Compressed context uses summarization, not message dropping
- [ ] **AC7**: Token counting accurate within ±5% of actual model tokenizer

### Quality Requirements
- [ ] **AC8**: Response quality post-compression ≥90% of pre-compression (measured via GQA-006)
- [ ] **AC9**: Compression latency <500ms (non-blocking)
- [ ] **AC10**: No crashes when compressing large conversations (500+ messages)
- [ ] **AC11**: Drift detection alerts when compression quality drops below 85%

### Testing Requirements
- [ ] **AC12**: GQA-006 golden dataset created with 50+ compression test cases
- [ ] **AC13**: Unit tests for token counting (±5% accuracy)
- [ ] **AC14**: Unit tests for compression strategies (sliding window, importance, summarization)
- [ ] **AC15**: Integration test: 100-message conversation compressed successfully
- [ ] **AC16**: Manual test: Long conversation with compression remains coherent

### Performance Requirements
- [ ] **AC17**: Token counting <100ms for 100-message conversation
- [ ] **AC18**: Compression operation <500ms for 100-message conversation
- [ ] **AC19**: Memory usage <50MB during compression

## Technical Approach

### Phase 0: DELETE Broken Implementation (1 hour)

**Files to Delete**:
```bash
rm app/src/main/java/ai/ondevice/app/data/TokenMonitor.kt
rm app/src/main/java/ai/ondevice/app/data/ContextManager.kt
rm app/src/main/java/ai/ondevice/app/data/ConversationCompressor.kt
rm app/src/main/java/ai/ondevice/app/data/QualityMonitor.kt
```

**Remove References**:
- Search codebase for `TokenMonitor`, `ContextManager`, `ConversationCompressor`, `QualityMonitor`
- Remove imports, instantiations, calls
- Verify app compiles without these classes

### Phase 1: Design Compression v2 Architecture (4 hours)

**Component 1: TokenCounter**
```kotlin
/**
 * Accurate token counting using model's actual tokenizer.
 * Falls back to estimation (chars/4) if tokenizer unavailable.
 */
class TokenCounter(private val tokenizerPath: String?) {
    companion object {
        const val MAX_CONTEXT_TOKENS = 4096
        const val COMPRESSION_THRESHOLD = 3400  // 83% of max
        const val CHARS_PER_TOKEN_FALLBACK = 4f
    }

    fun countTokens(text: String): Int {
        return if (tokenizerPath != null) {
            // Use actual tokenizer (sentencepiece, tiktoken, etc.)
            loadTokenizer(tokenizerPath).encode(text).size
        } else {
            // Fallback: character-based estimation
            (text.length / CHARS_PER_TOKEN_FALLBACK).toInt()
        }
    }
}
```

**Component 2: CompressionStrategy**
```kotlin
sealed class CompressionStrategy {
    /**
     * Always keep last N messages verbatim (recent context is most important)
     */
    data class SlidingWindow(val keepLast: Int = 10) : CompressionStrategy()

    /**
     * Score messages by importance, keep high-scoring ones
     * Importance factors:
     * - User-starred messages
     * - System messages (errors, warnings)
     * - Messages with high engagement (long responses, code blocks)
     * - First message (conversation starter)
     */
    data class ImportanceScoring(val minScore: Float = 0.7f) : CompressionStrategy()

    /**
     * Summarize old messages into single summary message
     * "Previous conversation covered: [summary]"
     */
    data class Summarization(
        val summaryTokenBudget: Int = 200,
        val olderThanMessages: Int = 15
    ) : CompressionStrategy()
}
```

**Component 3: ContextCompressor (v2)**
```kotlin
class ContextCompressor(
    private val tokenCounter: TokenCounter,
    private val inferenceEngine: InferenceEngine  // For summarization
) {
    /**
     * Compress conversation context to fit within token budget.
     * Returns compressed messages list.
     */
    suspend fun compress(
        messages: List<ConversationMessage>,
        maxTokens: Int = TokenCounter.MAX_CONTEXT_TOKENS
    ): CompressionResult {
        val currentTokens = tokenCounter.countTokens(messages.joinToString())

        if (currentTokens <= maxTokens) {
            return CompressionResult.NoCompressionNeeded(messages)
        }

        // Apply strategies in order:
        // 1. Keep last 10 messages (sliding window)
        val recentMessages = messages.takeLast(10)
        val oldMessages = messages.dropLast(10)

        // 2. Score old messages by importance
        val importantOldMessages = oldMessages.filter { scoreImportance(it) > 0.7f }

        // 3. Summarize remaining old messages
        val summarizedContext = summarize(
            messages = oldMessages - importantOldMessages.toSet(),
            tokenBudget = 200
        )

        // 4. Reconstruct context
        val compressedMessages = buildList {
            if (summarizedContext != null) add(summarizedContext)
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
        if (message.content.length > 500) score += 0.2f  // Long, detailed message
        if (message.content.contains("```")) score += 0.1f  // Code block
        if (message.isFirstInConversation) score += 0.2f
        return score.coerceAtMost(1.0f)
    }

    private suspend fun summarize(messages: List<ConversationMessage>, tokenBudget: Int): ConversationMessage? {
        if (messages.isEmpty()) return null

        val prompt = """
            Summarize the following conversation in $tokenBudget tokens or less:
            ${messages.joinToString("\n") { "${it.role}: ${it.content}" }}

            Summary:
        """.trimIndent()

        val summary = inferenceEngine.generate(prompt, maxTokens = tokenBudget)

        return ConversationMessage(
            role = "system",
            content = "**Previous conversation summary**: $summary",
            timestamp = System.currentTimeMillis(),
            isSystemGenerated = true
        )
    }
}
```

**Component 4: CompressionQualityMonitor**
```kotlin
class CompressionQualityMonitor(
    private val goldenDataset: GoldenQADataset
) {
    /**
     * Measure compression quality by comparing responses before/after compression.
     * Returns quality score 0.0-1.0
     */
    suspend fun measureQuality(
        originalContext: List<ConversationMessage>,
        compressedContext: List<ConversationMessage>
    ): QualityScore {
        val scores = goldenDataset.testCases.map { testCase ->
            val responseOriginal = generateResponse(originalContext, testCase.query)
            val responseCompressed = generateResponse(compressedContext, testCase.query)

            // Compare responses using semantic similarity
            semanticSimilarity(responseOriginal, responseCompressed)
        }

        return QualityScore(
            averageScore = scores.average(),
            minScore = scores.min(),
            maxScore = scores.max(),
            testCaseCount = scores.size
        )
    }
}
```

### Phase 2: Implementation (2 days)

**Day 1: Core Components**
1. Implement TokenCounter with actual tokenizer integration
2. Implement ContextCompressor with sliding window + importance scoring
3. Implement summarization logic
4. Add comprehensive logging

**Day 2: Integration & Quality Monitoring**
1. Integrate compression into LlmChatViewModel
2. Trigger compression when context > 3400 tokens
3. Implement CompressionQualityMonitor
4. Add drift detection alerts

### Phase 3: Golden QA Dataset Creation (1 day)

**GQA-006: Context Compression Quality Dataset**

Create 50+ test cases covering:
1. **Long conversations** (50+ messages) - verify compression maintains coherence
2. **Code discussions** - verify code blocks preserved or summarized correctly
3. **Multi-topic conversations** - verify topic continuity maintained
4. **Important message preservation** - verify starred messages never lost
5. **Summarization quality** - verify summaries capture key points

**Example Test Case**:
```yaml
test_case_id: GQA-006-001
category: long_conversation
description: 50-message technical discussion about Android architecture
original_context:
  - user: "Explain MVVM"
  - assistant: "[detailed explanation]"
  - user: "How does ViewModel survive config changes?"
  - assistant: "[detailed explanation]"
  # ... 46 more messages
query: "Can you summarize our discussion about MVVM?"
expected_quality:
  maintains_key_points: true
  references_original_context: true
  coherent: true
min_acceptable_score: 0.85
```

### Phase 4: Testing & Validation (2 days)

**Unit Tests**:
- TokenCounterTest: Verify token counting within ±5% of actual tokenizer
- ContextCompressorTest: Test each compression strategy independently
- CompressionQualityMonitorTest: Test quality measurement logic

**Integration Tests**:
- Create 100-message conversation
- Trigger compression
- Verify compressed context < 3400 tokens
- Verify response quality ≥90%

**Manual Tests**:
- Start new conversation
- Send 100 messages back and forth
- Verify compression triggers automatically
- Verify AI responses remain coherent
- Verify starred messages preserved
- Check logs for compression metrics

### Phase 5: Monitoring & Alerting (1 day)

**Compression Metrics**:
```kotlin
data class CompressionMetrics(
    val conversationId: Long,
    val originalTokens: Int,
    val finalTokens: Int,
    val reductionPercent: Float,
    val qualityScore: Float,
    val compressionTimeMs: Long,
    val strategy: String,
    val timestamp: Long
)
```

**Firebase Analytics Events**:
- `context_compression_triggered` - when compression runs
- `context_compression_quality_degraded` - when quality < 0.85
- `context_compression_failed` - on errors

**Drift Detection**:
- Monitor quality score over time (rolling 7-day average)
- Alert if average drops below 0.85
- Log detailed metrics for debugging

## Implementation Files

### Files to DELETE
1. ❌ `TokenMonitor.kt` (146 lines)
2. ❌ `ContextManager.kt` (158 lines)
3. ❌ `ConversationCompressor.kt` (152 lines)
4. ❌ `QualityMonitor.kt` (64 lines)

### Files to CREATE
1. ✅ `TokenCounter.kt` - Accurate token counting
2. ✅ `CompressionStrategy.kt` - Strategy definitions
3. ✅ `ContextCompressor.kt` - Compression logic v2
4. ✅ `CompressionQualityMonitor.kt` - Quality measurement
5. ✅ `CompressionMetrics.kt` - Metrics data classes
6. ✅ `GQA-006-context-compression.yaml` - Golden QA dataset

### Files to MODIFY
1. `LlmChatViewModel.kt` - Integrate compression before inference
2. `ConversationDao.kt` - Add isStarred, isFirstInConversation fields if missing
3. `FirebaseAnalytics.kt` - Add compression events

### Files to CREATE (Tests)
1. `TokenCounterTest.kt`
2. `ContextCompressorTest.kt`
3. `CompressionQualityMonitorTest.kt`
4. `ContextCompressionIntegrationTest.kt`

## References

### Research
- [Anthropic Context Windows](https://docs.anthropic.com/claude/docs/context-windows) - 200K context best practices
- [LongBench: Context Compression Benchmark](https://arxiv.org/abs/2308.14508)
- [Lost in the Middle](https://arxiv.org/abs/2307.03172) - Why middle context gets lost

### Tokenizer Integration
- SentencePiece for Gemma models
- tiktoken for GPT-style models
- Fallback: Character-based estimation

### PRD References
- PRD v3.0 MASTER - Epic 8: Context Compression (F8.2)
- Target: Support 100+ message conversations
- Quality: ≥90% response quality post-compression
- Performance: <500ms compression latency

## Timeline

| Phase | Duration | Output |
|-------|----------|--------|
| **Phase 0**: Delete broken code | 1 hour | Clean slate, app compiles |
| **Phase 1**: Design v2 architecture | 4 hours | Architecture doc, component design |
| **Phase 2**: Implementation | 2 days | Core compression working |
| **Phase 3**: Golden QA dataset | 1 day | GQA-006 with 50+ test cases |
| **Phase 4**: Testing & validation | 2 days | All tests passing, quality verified |
| **Phase 5**: Monitoring & alerting | 1 day | Metrics, drift detection |

**Total Effort**: 6 days (48 hours)

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Max conversation length | 100+ messages | Manual test (send 100 messages) |
| Compression triggered | When >3400 tokens | Logcat shows "Compression triggered" |
| Token reduction | ≥20% | Metrics: originalTokens vs finalTokens |
| Response quality | ≥90% | GQA-006 evaluation (50+ test cases) |
| Quality drift detection | <0.85 triggers alert | 7-day rolling average |
| Compression latency | <500ms | Performance monitoring |
| No crashes | 100% | Compress 500-message conversation |

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Actual tokenizer unavailable | High | Medium | Fallback to char/4 estimation, document ±5% accuracy |
| Summarization degrades quality | Medium | High | Test extensively with GQA-006, tune summarization prompt |
| Compression too slow (>500ms) | Low | Medium | Optimize compression algorithm, run async |
| Importance scoring inaccurate | Medium | Medium | Tune scoring weights with user feedback |
| Drift detection false positives | Low | Low | Use 7-day rolling average to smooth noise |

---

**Status**: PROPOSAL - Awaiting approval
**Next Step**: User reviews and approves → then `/openspec-apply context-compression-rebuild`
