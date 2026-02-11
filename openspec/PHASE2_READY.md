# Phase 2: context-compression-rebuild - Ready to Start

**Status**: ⏳ READY TO START (in new session)
**Phase 1**: ✅ COMPLETE (see PHASE1_COMPLETE.md)
**Deferred**: 2026-01-11
**Resume**: When ready for 48-hour implementation

---

## 🎯 Quick Start (New Session)

When ready to begin Phase 2, run:

```bash
/openspec-apply context-compression-rebuild
```

This will execute all 70 tasks autonomously across 5 phases.

---

## 📊 What Is Phase 2?

**Proposal**: context-compression-rebuild
**Priority**: P0 Critical
**Effort**: 48 hours (6 days)
**Complexity**: Very High

### Problem
- User report: "CONTEXT COMPRESSION NOT WORKING PROPERLY, DELETE THE ENTIRE IMPLEMENTATION AND START FRESH"
- 520+ lines of broken code across 4 files
- Long conversations (100+ messages) break when hitting 4096 token limit

### Solution
Complete rebuild from scratch with proper testing and quality monitoring.

---

## 📁 Proposal Location

All specs ready at: `openspec/changes/context-compression-rebuild/`

**Files**:
- ✅ `proposal.md` - Complete technical approach
- ✅ `tasks.md` - 70 tasks across 5 phases
- ✅ `spec-delta.md` - Specification changes

---

## 🗺️ Implementation Plan (70 Tasks)

### Phase 0: Delete Broken Code (1 hour)
**Tasks**: 5
- Search for all references to broken classes
- Remove usages from ChatViewModel.kt (5 locations found)
- Delete 4 broken files (520+ lines total)
- Verify app compiles
- Commit deletion

**Files to Delete**:
```
app/src/main/java/ai/ondevice/app/compression/TokenMonitor.kt (146 lines)
app/src/main/java/ai/ondevice/app/compression/ContextManager.kt (158 lines)
app/src/main/java/ai/ondevice/app/compression/ConversationCompressor.kt (152 lines)
app/src/main/java/ai/ondevice/app/compression/QualityMonitor.kt (64 lines)
```

**References in**: `ChatViewModel.kt` lines 24-25, 89-91, 564, 572, 574, 589, 599

---

### Phase 1: Design Architecture (4 hours)
**Tasks**: 7
- Research tokenizer integration options
- Design TokenCounter API
- Design CompressionStrategy sealed class
- Design ContextCompressor v2 API
- Design CompressionQualityMonitor API
- Design integration into LlmChatViewModel
- Design metrics and monitoring

---

### Phase 2: Core Implementation (16 hours)
**Tasks**: 16

**Day 1** (8 hours):
- Implement TokenCounter with fallback
- Unit test TokenCounter accuracy (±5%)
- Implement CompressionStrategy sealed class
- Implement ContextCompressor basic structure
- Implement sliding window strategy
- Implement importance scoring
- Implement summarization
- Implement context reconstruction

**Day 2** (8 hours):
- Unit test ContextCompressor (sliding window, importance, summarization)
- Unit test token reduction (≥20%)
- Implement CompressionQualityMonitor
- Implement quality measurement
- Integrate compression into LlmChatViewModel
- Add compression logging and metrics

---

### Phase 3: Golden QA Dataset (8 hours)
**Tasks**: 8
- Design GQA-006 dataset structure
- Create 10 long conversation test cases (50+ messages)
- Create 10 code discussion test cases
- Create 10 multi-topic test cases
- Create 10 important message preservation test cases
- Create 10 summarization quality test cases
- Implement GQA-006 evaluation script
- Run baseline evaluation

---

### Phase 4: Testing & Validation (16 hours)
**Tasks**: 16

**Day 1** (8 hours):
- Fix failing GQA-006 test cases (iterate until ≥90% pass)
- Integration test: 100-message conversation
- Integration test: Starred message preservation
- Integration test: System message preservation
- Integration test: Compression latency (<500ms)
- Integration test: Memory usage (<50MB)
- Manual test: Long conversation with compression
- Manual test: Starred message preservation

**Day 2** (8 hours):
- DroidRun visual verification: Compression trigger
- Regression test: Short conversations unaffected
- Regression test: Mid-length conversations
- Edge case test: Empty conversation
- Edge case test: Single message conversation
- Edge case test: All messages starred
- Error handling test: Summarization failure
- Performance test: 500-message conversation

---

### Phase 5: Monitoring & Alerting (8 hours)
**Tasks**: 8
- Implement CompressionMetrics storage (Room)
- Implement Firebase Analytics events
- Implement drift detection: Data collection
- Implement drift detection: Alert logic
- Implement drift detection: UI notification (optional)
- Create compression metrics dashboard
- Document monitoring and alerting
- Test drift detection with synthetic data

---

### Phase 6: Documentation (3 hours)
**Tasks**: 4
- Update LESSONS_LEARNED.md
- Update CODE_INDEX.md
- Create architecture documentation
- Create GQA-006 dataset documentation

---

## ✅ Success Criteria

All 70 tasks checked ✅ AND:
- [ ] All 4 broken files deleted (520+ lines removed)
- [ ] TokenCounter implemented with ±5% accuracy
- [ ] ContextCompressor v2 implemented with 3 strategies
- [ ] Compression integrated into LlmChatViewModel
- [ ] GQA-006 golden dataset created with 50+ test cases
- [ ] ≥90% of GQA-006 test cases pass (quality ≥0.85)
- [ ] Manual test: 100-message conversation works correctly
- [ ] Compression latency < 500ms
- [ ] Memory usage < 50MB during compression
- [ ] Drift detection works (alerts when quality < 0.85)
- [ ] Firebase Analytics events logged
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Architecture and dataset documented

---

## 🔧 Technical Details

### New Components to Create

**1. TokenCounter.kt**
```kotlin
class TokenCounter(private val tokenizerPath: String?) {
    companion object {
        const val MAX_CONTEXT_TOKENS = 4096
        const val COMPRESSION_THRESHOLD = 3400  // 83% of max
        const val CHARS_PER_TOKEN_FALLBACK = 4f
    }

    fun countTokens(text: String): Int
}
```

**2. CompressionStrategy.kt**
```kotlin
sealed class CompressionStrategy {
    data class SlidingWindow(val keepLast: Int = 10)
    data class ImportanceScoring(val minScore: Float = 0.7f)
    data class Summarization(val summaryTokenBudget: Int = 200, val olderThanMessages: Int = 15)
}
```

**3. ContextCompressor.kt (v2)**
```kotlin
class ContextCompressor(
    private val tokenCounter: TokenCounter,
    private val inferenceEngine: InferenceEngine
) {
    suspend fun compress(
        messages: List<ConversationMessage>,
        maxTokens: Int = TokenCounter.MAX_CONTEXT_TOKENS
    ): CompressionResult
}
```

**4. CompressionQualityMonitor.kt**
```kotlin
class CompressionQualityMonitor(private val goldenDataset: GoldenQADataset) {
    suspend fun measureQuality(
        originalContext: List<ConversationMessage>,
        compressedContext: List<ConversationMessage>
    ): QualityScore
}
```

**5. CompressionMetrics.kt**
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

---

## 📝 Integration Points

### LlmChatViewModel.kt Modification
```kotlin
// Before calling inferenceEngine:
val currentTokens = tokenCounter.countTokens(messages.joinToString())
if (currentTokens > TokenCounter.COMPRESSION_THRESHOLD) {
    val compressed = contextCompressor.compress(messages)
    // Use compressed.messages for inference
}
```

### ChatViewModel.kt - Remove Old Integration
Lines 89-91, 564, 572, 574, 589, 599 reference old ContextManager - will be removed in Phase 0.

---

## 🧪 Testing Strategy

### Unit Tests (New Files)
- `TokenCounterTest.kt` - Test accuracy ±5%
- `ContextCompressorTest.kt` - Test each strategy
- `CompressionQualityMonitorTest.kt` - Test quality measurement

### Integration Tests (New Files)
- `ContextCompressionIntegrationTest.kt` - End-to-end compression

### Golden QA Dataset
- `GQA-006-context-compression.yaml` - 50+ test cases
- Categories: long_conversation, code_discussion, multi_topic, important_preservation, summarization_quality

### Manual Testing
- 100-message conversation test
- Starred message preservation test
- Visual verification with DroidRun

---

## ⚠️ Known Challenges

### 1. Tokenizer Availability
**Risk**: Actual tokenizer may not be available on device
**Mitigation**: Fallback to character-based estimation (chars/4)
**Target**: ±5% accuracy

### 2. Summarization Quality
**Risk**: Summarization may degrade quality
**Mitigation**: Test extensively with GQA-006, tune summarization prompt
**Target**: ≥90% quality retention

### 3. Performance
**Risk**: Compression may be slow (>500ms)
**Mitigation**: Optimize algorithm, run async
**Target**: <500ms for 100-message conversation

### 4. Importance Scoring
**Risk**: Scoring weights may need tuning
**Mitigation**: Start with proposed weights, adjust based on user feedback
**Target**: Important messages never lost

---

## 📦 Files Modified/Created

### Files to DELETE (Phase 0)
- ❌ `TokenMonitor.kt`
- ❌ `ContextManager.kt`
- ❌ `ConversationCompressor.kt`
- ❌ `QualityMonitor.kt`

### Files to CREATE
- ✅ `TokenCounter.kt`
- ✅ `CompressionStrategy.kt`
- ✅ `ContextCompressor.kt` (v2)
- ✅ `CompressionQualityMonitor.kt`
- ✅ `CompressionMetrics.kt`
- ✅ `GQA-006-context-compression.yaml`
- ✅ `TokenCounterTest.kt`
- ✅ `ContextCompressorTest.kt`
- ✅ `CompressionQualityMonitorTest.kt`
- ✅ `ContextCompressionIntegrationTest.kt`
- ✅ `docs/context-compression-architecture.md`

### Files to MODIFY
- 🔄 `LlmChatViewModel.kt` - Integrate compression before inference
- 🔄 `ChatViewModel.kt` - Remove old ContextManager references
- 🔄 `ConversationDao.kt` - Add isStarred, isFirstInConversation fields (if missing)
- 🔄 `FirebaseAnalytics.kt` - Add compression events (if Crashlytics enabled)

---

## 💡 Before Starting Phase 2

### Recommended: Enable Phase 1 Features First

**Why**: Test the foundation before building on it

**Actions** (15 minutes total):

1. **Add BRAVE_API_KEY to GitHub Secrets** (5 min)
   - Location: `docs/github-secrets-setup.md`
   - Benefit: Web search working in CI-built APKs

2. **Complete Firebase Crashlytics Setup** (10 min)
   - Location: `docs/firebase-setup.md`
   - Benefit: Crash reporting ready for Phase 2 testing

**Then**: Rebuild, test full Phase 1 functionality, verify stable baseline.

---

## 🚀 Starting Phase 2 (New Session)

### Step 1: Fresh Session
Open new Claude Code session with full context.

### Step 2: Review Phase 1 Status
```bash
cat openspec/PHASE1_COMPLETE.md
```

### Step 3: Load Phase 2 Context
```bash
cat openspec/PHASE2_READY.md  # This file
cat openspec/changes/context-compression-rebuild/proposal.md
cat openspec/changes/context-compression-rebuild/tasks.md
```

### Step 4: Start Implementation
```bash
/openspec-apply context-compression-rebuild
```

### Step 5: Execute Autonomously
Claude will:
- Execute all 70 tasks
- Run TDD loop (RED → GREEN → REFACTOR)
- Run CI loop (push → test → fix → repeat)
- Run visual verification loop
- Update tasks.md with progress
- **Stop only if critical blocker encountered**

### Step 6: Completion
When all tasks done:
```bash
/openspec-archive context-compression-rebuild
```

---

## 📈 Expected Timeline

**Optimistic**: 4-5 days (if no major blockers)
**Realistic**: 6 days (48 hours as estimated)
**Pessimistic**: 8 days (if quality tuning required)

**Session Breakdown**:
- Session 1: Phase 0-1 (Delete + Design) - 5 hours
- Session 2: Phase 2a (Core implementation) - 8 hours
- Session 3: Phase 2b (Integration + unit tests) - 8 hours
- Session 4: Phase 3 (Golden QA dataset) - 8 hours
- Session 5: Phase 4a (Integration tests) - 8 hours
- Session 6: Phase 4b (Manual + visual tests) - 8 hours
- Session 7: Phase 5-6 (Monitoring + docs) - 11 hours

**Total**: ~56 hours actual (vs 48h estimated) - expect 15-20% overrun

---

## ✅ Phase 1 Completion Checklist

Before starting Phase 2, verify:

- [x] Phase 1 proposals complete (3 of 4)
- [x] APK built and tested
- [x] Maestro e2e test PASSED
- [x] Documentation created
- [x] PHASE1_COMPLETE.md written
- [ ] **Optional**: Firebase setup complete (recommended)
- [ ] **Optional**: GitHub secret added (recommended)

---

## 📚 Reference Documents

**Phase 1 Summary**: `openspec/PHASE1_COMPLETE.md`
**Proposal**: `openspec/changes/context-compression-rebuild/proposal.md`
**Tasks**: `openspec/changes/context-compression-rebuild/tasks.md`
**Spec Delta**: `openspec/changes/context-compression-rebuild/spec-delta.md`
**Lessons**: `LESSONS_LEARNED.md`

---

## 🎯 Success Definition

Phase 2 is complete when:
- ✅ All 520+ lines of broken code deleted
- ✅ New compression system working
- ✅ 100+ message conversations supported
- ✅ ≥90% quality retention (measured via GQA-006)
- ✅ All tests passing (unit + integration)
- ✅ Visual verification complete
- ✅ Documentation complete
- ✅ Archived to `openspec/archive/`

---

**Status**: ⏳ READY TO START
**Next Command**: `/openspec-apply context-compression-rebuild`
**Estimated Effort**: 48 hours (6 days)
