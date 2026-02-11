# Proposal: enhance-topic-extraction-multilingual

## Summary

Replace basic topic extraction with production-grade multilingual system featuring semantic verb-action mapping, intelligent topic compression, template variation, full language preservation, and tone-appropriate output for English, Swahili, and code-switched inputs.

## Motivation

**Current Problems:**
1. **Semantic mismatch**: Always shows "Creating..." regardless of user intent (analyze → "Creating analysis", explain → "Creating explanation")
2. **Language mixing**: Swahili output broken with mixed English ("Creating uchambuzi on uchumi")
3. **Poor truncation**: Naive cutting breaks mid-word ("artificial intelligen...")
4. **Robotic repetition**: Same template for all requests feels mechanical
5. **No tone awareness**: Formal requests get casual responses and vice versa

**User Impact:**
- Confusing status messages that don't match what user asked for
- Unprofessional language mixing for Swahili speakers
- Important topic information lost in truncation
- Generic responses reduce perceived intelligence
- Tone mismatch reduces trust and engagement

**Evidence from Testing:**
- User feedback: "the topic summarisation still feels too simple"
- Example failure: "do an analysis on empires" → "Creating analysis on the rise and fall of empires..." (should be "Analyzing...")
- Swahili example: "Nieleze about climate change" → Mixed language output instead of pure Swahili

## Scope

### ✅ Included

**Detection Layer (NEW - Long vs Short Form):**
- Confidence-based detection (0.0-1.0 score) instead of binary true/false
- Multi-factor weighting system (5 signal types with different weights)
- Contradiction resolution (brevity modifiers override length modifiers)
- Configurable threshold (default: 0.70 for showing long response status)
- Integration with extraction confidence for validation
- False positive prevention (e.g., "write one sentence" → short form)

**Core Architecture:**
- Tiered confidence system (Success/Uncertain/Refused)
- Pre-compiled patterns with possessive quantifiers
- Fast-path rejection via indexOf() for <50ms latency
- Zero false positive guarantee

**Extraction Enhancements:**
- 12 comprehensive patterns (up from current 7)
- Semantic verb-action mapping (CREATE/ANALYZE/EXPLAIN/COMPARE/SUMMARIZE/RESEARCH/HELP/QUESTION)
- Full code-switching support (Swahili frame + English content)
- Improved Swahili conjugation handling
- Question format detection
- Conversational pattern support
- Polite request handling (tafadhali, naomba)

**Summarization System (NEW):**
- Intent-based template selection (not just pattern-based)
- Language-consistent output (no mixing)
- Formality detection (FORMAL/NEUTRAL/CASUAL/URGENT)
- Smart topic compression preserving clause boundaries
- Template variation to avoid repetition
- Content type translation for Swahili

**Performance Targets:**
- <50ms extraction latency
- <100KB memory footprint
- Zero regex catastrophic backtracking

### ❌ Not Included

- Sheng-specific templates (deferred to future iteration)
- Machine learning-based extraction (regex-only for edge deployment)
- Multi-turn context awareness
- User preference learning
- Runtime pattern compilation (build-time only)
- Dialect-specific variations beyond standard Swahili

## Acceptance Criteria

### Detection Layer (Long vs Short Form)

- [ ] **Confidence scoring**: detectLongRequest() returns confidence score (0.0-1.0), not boolean
- [ ] **False positive prevention**: "write one sentence about AI" → confidence <0.70 (SHORT form)
- [ ] **Contradiction resolution**: "quick comprehensive analysis" → brevity wins (SHORT form)
- [ ] **Multi-factor weighting**: Strong signals (action verb + content type) weighted higher than weak signals (length modifiers alone)
- [ ] **Threshold configurable**: Can adjust detection threshold via config (default: 0.70)
- [ ] **Integration validation**: High extraction confidence + high detection confidence = show status

### Extraction & Summarization Layer

- [ ] **Semantic accuracy**: "analyze X" → "Analyzing X" (not "Creating analysis")
- [ ] **Language purity**: Swahili prompts → pure Swahili output (no mixing)
- [ ] **Smart truncation**: Long topics truncate at word/clause boundaries, never mid-word
- [ ] **Template variation**: Same intent with different formality levels produces different templates
- [ ] **Code-switching**: "Nieleze about AI" → Pure Swahili or English output (user's matrix language)
- [ ] **Question detection**: "What is X?" → "Researching X" (not "Creating...")
- [ ] **Tone matching**: Formal input → formal output, casual → casual

### Performance

- [ ] **Latency**: Average extraction <50ms on mid-range Android device
- [ ] **No backtracking**: Pathological inputs ("a"*30+"b") complete in <100ms
- [ ] **Memory**: Total memory footprint <100KB
- [ ] **Coverage**: 95%+ of test cases match expected intent

### Quality

- [ ] **Zero false positives**: Never show long response status for short queries
- [ ] **Graceful fallback**: Failed extraction produces meaningful keywords (not "Processing request")
- [ ] **Test coverage**: 100% of 12 patterns tested with positive/negative cases
- [ ] **Edge cases**: Handles empty input, excessive whitespace, mixed punctuation

## Technical Approach

### Architecture

```
USER PROMPT
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 1: Detection (Long vs Short Form)                       │
│  LongResponseDetector.detectLongRequestWithConfidence()        │
│  ├─ Multi-factor scoring (5 signals with weights)             │
│  ├─ Contradiction resolution (brevity > length modifiers)     │
│  ├─ Returns: DetectionResult(confidence, signals, reasoning)   │
│  └─ Threshold: 0.70 (configurable)                            │
└─────────────────────────────────────────────────────────────────┘
     │
     ├─ If confidence <0.70 → SHORT FORM (normal streaming)
     │
     ▼ If confidence ≥0.70 → LONG FORM (show status box)
     │
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 2: Extraction (What to say about)                       │
│  TopicExtractor.extract()                                      │
│  ├─ Fast-path rejection (indexOf trigger words)               │
│  ├─ 12 pre-compiled patterns with possessive quantifiers      │
│  ├─ Tiered confidence: Success (≥0.85) / Uncertain / Refused  │
│  └─ Smart fallback with keyword extraction                    │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│  LAYER 3: Summarization (How to say it)                        │
│  TopicSummarizer.summarize()                                   │
│  ├─ Input analysis: Intent / Language / Formality             │
│  ├─ Template selection: 8 intents × 4 formality × 2 languages │
│  ├─ Smart topic compression (clause-aware)                    │
│  └─ Language resolution (preserve user's choice)              │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
UI DISPLAY: "Analyzing the rise and fall of empires..."
```

### Key Innovations

**Detection Layer (NEW):**
1. **Multi-Factor Scoring**: 5 signal types with different weights instead of binary yes/no
2. **Contradiction Resolution**: Brevity modifiers ("brief", "quick") override length modifiers ("comprehensive")
3. **False Positive Prevention**: "write one sentence" analyzed as SHORT despite "write" trigger
4. **Configurable Threshold**: Adjust sensitivity (default: 0.70 = 70% confidence)
5. **Transparent Reasoning**: Returns signal breakdown for debugging

**Extraction & Summarization Layers:**
1. **Separation of Concerns**: Detection (long?) → Extraction (what?) → Summarization (how?)
2. **Semantic Verb Mapping**: `verbToIntent` map covers 50+ verbs across 8 intent classes
3. **Tiered Templates**: Different templates for FORMAL/NEUTRAL/CASUAL/URGENT
4. **Language Preservation**: Detect input language, output in same language (pure)
5. **Smart Compression**: Preserve first clause + key nouns from remainder

### Detection Layer: Multi-Factor Scoring System

**Problem with Current Binary Detection:**
- "write one sentence about AI" → TRUE (false positive)
- "quick comprehensive analysis" → TRUE (conflicting signals ignored)
- "give me a detailed breakdown" → FALSE (false negative)
- All patterns weighted equally (no prioritization)

**Proposed Confidence-Based Detection:**

| Signal Type | Weight | Example | Score |
|-------------|--------|---------|-------|
| **BREVITY_BLOCKER** | -1.0 (veto) | "brief", "quick", "one sentence" | Immediate 0.0 |
| **STRONG_ACTION** | +0.50 | "write thesis", "create essay" | High confidence |
| **WEAK_ACTION** | +0.25 | "do", "make", "give" (without content type) | Medium confidence |
| **LENGTH_MODIFIER** | +0.30 | "comprehensive", "detailed", "in-depth" | Medium confidence |
| **EXPLICIT_REQUEST** | +0.40 | "step by step", "explain everything" | High confidence |
| **QUESTION_PENALTY** | -0.20 | Starts with "What is..." | Reduce confidence |

**Confidence Calculation:**
```kotlin
fun detectLongRequestWithConfidence(userPrompt: String): DetectionResult {
    var confidence = 0.0f
    val signals = mutableListOf<DetectionSignal>()

    // CRITICAL: Brevity blockers veto everything
    if (hasBrevityModifier(userPrompt)) {
        return DetectionResult(
            confidence = 0.0f,
            signals = listOf(DetectionSignal.BREVITY_BLOCKER),
            shouldShowStatus = false,
            reasoning = "Brevity modifier present: brief/quick/short/one sentence"
        )
    }

    // Accumulate positive signals
    if (hasStrongAction(userPrompt)) {
        confidence += 0.50f
        signals.add(DetectionSignal.STRONG_ACTION)
    }

    if (hasLengthModifier(userPrompt)) {
        confidence += 0.30f
        signals.add(DetectionSignal.LENGTH_MODIFIER)
    }

    if (hasExplicitRequest(userPrompt)) {
        confidence += 0.40f
        signals.add(DetectionSignal.EXPLICIT_REQUEST)
    }

    // Apply penalties
    if (isPureQuestion(userPrompt)) {
        confidence -= 0.20f
        signals.add(DetectionSignal.QUESTION_PENALTY)
    }

    // Clamp to [0.0, 1.0]
    confidence = confidence.coerceIn(0.0f, 1.0f)

    return DetectionResult(
        confidence = confidence,
        signals = signals,
        shouldShowStatus = confidence >= DETECTION_THRESHOLD,  // 0.70
        reasoning = "Signals: ${signals.joinToString(", ")}"
    )
}
```

**Example Scoring:**

| Input | Signals | Confidence | Result |
|-------|---------|------------|--------|
| "write a thesis on AI" | STRONG_ACTION(+0.50) | 0.50 | ❌ SHORT (below 0.70) |
| "write a comprehensive thesis on AI" | STRONG_ACTION(+0.50), LENGTH_MODIFIER(+0.30) | 0.80 | ✅ LONG |
| "write one sentence about AI" | BREVITY_BLOCKER(-1.0) | 0.00 | ❌ SHORT (veto) |
| "quick comprehensive analysis" | BREVITY_BLOCKER(-1.0) | 0.00 | ❌ SHORT (brevity wins) |
| "What is AI?" | QUESTION_PENALTY(-0.20) | 0.00 | ❌ SHORT |
| "do an analysis on empires" | WEAK_ACTION(+0.25) | 0.25 | ❌ SHORT (below 0.70) |
| "do a comprehensive analysis on empires" | WEAK_ACTION(+0.25), LENGTH_MODIFIER(+0.30) | 0.55 | ❌ SHORT (below 0.70) |
| "do a detailed step-by-step analysis" | WEAK_ACTION(+0.25), LENGTH_MODIFIER(+0.30), EXPLICIT_REQUEST(+0.40) | 0.95 | ✅ LONG |

**Key Benefits:**
1. **Prevents false positives**: "write one sentence" → 0.0 confidence
2. **Resolves conflicts**: Brevity always wins over length
3. **Gradual degradation**: Weak signals accumulate but may not reach threshold
4. **Transparent reasoning**: Returns signal breakdown for debugging
5. **Tunable**: Adjust threshold or signal weights based on telemetry

### Integration Points

**Modified Files:**
- `LongResponseDetector.kt`: Update `extractTopicFromUserPrompt()` to call `TopicSummarizer.summarize()`
- Keep existing `detectLongRequest()` logic (user prompt detection)

**New Files:**
- `TopicExtractor.kt`: 500 lines, extraction engine
- `TopicSummarizer.kt`: 700 lines, summarization engine
- `TopicExtractorTest.kt`: 400 lines, 30+ tests
- `TopicSummarizerTest.kt`: 350 lines, 25+ tests

### Pattern Examples

**Pattern 1: Full English Structure (confidence: 0.95)**
```kotlin
"(?i)\\b(write|create|analyze|...)\\s++(a|an\\s++)?" +
"(thesis|essay|analysis|...)\\s++" +
"(on|about|regarding|...)\\s++(.++)$"
```

**Pattern 8: Code-Switched (confidence: 0.88)**
```kotlin
"(?i)\\b(ni|u|a|...)(na|li|ta|...)?(...)[aie]\\s++" +
"(?:on|about|kuhusu|...)\\s++(.++)$"
```

### Template Examples

**English - ANALYZE Intent:**
- FORMAL: "Conducting analysis of {topic}"
- NEUTRAL: "Analyzing {topic}"
- CASUAL: "Digging into {topic}"
- URGENT: "Analyzing: {topic}"

**Swahili - ANALYZE Intent:**
- FORMAL: "Kuchambua {topic}"
- NEUTRAL: "Kuchambua {topic}"
- CASUAL: "Nachambua {topic}"
- URGENT: "Uchambuzi: {topic}"

## References

### Related Analysis
- `/tmp/claude-1000/.../topic-extraction-analysis-report.md` - 14,000-word comprehensive analysis
- Meta-cognitive protocol applied to both extraction and summarization layers

### Related Code
- Current implementation: `LongResponseDetector.kt` (lines 59-90)
- Current implementation: `TopicExtractor.kt` (existing, to be replaced)
- UI display: `LongResponseStatusBox.kt` (calls extractTopicFromUserPrompt)

### Research Foundation
- Swahili morphology: Agglutinative verb structure enables deterministic parsing
- Code-switching: Matrix Language Frame model (96% Swahili-framed in Kenya)
- Performance: Possessive quantifiers prevent O(2^N) catastrophic backtracking
- UX writing: Semantic verb-action mapping, tone-appropriate language

### Related Changes
- This builds on: `fix-long-response-detection` (recently completed)
- Addresses user feedback: "topic summarisation still feels too simple"
- Supersedes: Current basic `TopicExtractor.kt` implementation

## Implementation Risks

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Pattern regex errors | Low | High | Comprehensive test suite (55+ tests) |
| Performance regression | Medium | Medium | Benchmark suite, pre-compilation |
| Language detection errors | Medium | Low | Graceful fallback, user preference override |
| Template selection wrong | Low | Low | Multiple templates per intent, variation |
| Memory footprint exceeds target | Low | Medium | Lazy-load extensions, vocabulary optimization |

## Success Metrics

**Before (Current State):**
- Binary detection (true/false only)
- False positives: "write one sentence" triggers long response status
- Always "Creating..." regardless of intent
- Mixed language output for Swahili
- Naive truncation breaks words
- Single template pattern

**After (Target State):**
- Confidence-based detection (0.0-1.0 score)
- Zero false positives: "write one sentence" → 0.0 confidence (SHORT)
- 95%+ semantic accuracy (intent matches output verb)
- 100% language purity (no mixing)
- 0% mid-word truncation
- 8 intent classes × 4 formality levels = 32 template variations

**User-Facing Improvement Examples:**

### Detection Layer

| Input | Before (Binary) | After (Confidence) |
|-------|-----------------|-------------------|
| "write one sentence about AI" | TRUE ❌ (false positive) | 0.00 → SHORT ✅ |
| "quick comprehensive analysis" | TRUE ❌ (conflicting) | 0.00 → SHORT ✅ |
| "write thesis" | TRUE ❌ (too aggressive) | 0.50 → SHORT ✅ |
| "write comprehensive thesis" | TRUE ✅ | 0.80 → LONG ✅ |
| "detailed step-by-step analysis" | TRUE ✅ | 0.95 → LONG ✅ |

### Extraction & Summarization Layer

| Input | Before | After |
|-------|--------|-------|
| "analyze empires" | Creating analysis on empires | **Analyzing the rise and fall of empires** |
| "explain quantum physics" | Creating explanation on quantum physics | **Explaining quantum physics** |
| "What is AI?" | Creating... (or no detection) | **Researching AI** |
| "Nieleze kuhusu elimu" | Creating uchambuzi on elimu | **Kueleza elimu** (pure Swahili) |
| "help me understand" | Creating... | **Helping with [topic]** |

## Open Questions

1. **Template Selection**: Should we randomize template selection for variety, or use first template for consistency? (Proposal: use first for consistency)
2. **User Preference**: Should we expose language preference in settings? (Proposal: auto-detect, add setting in future iteration)
3. **Sheng Support**: Should we add Sheng templates now or defer? (Proposal: defer to future iteration)
4. **Fallback Quality**: Is keyword extraction sufficient for refused cases? (Proposal: yes, with option to enhance later)

## Timeline Estimate

- **Detection Layer**: 2 hours (confidence scoring, multi-factor weighting)
- **Extraction Engine**: 2 hours (replace existing TopicExtractor.kt)
- **Summarization Engine**: 4 hours (new TopicSummarizer.kt)
- **Test Suite**: 3 hours (65+ tests across all three layers)
- **Integration**: 0.75 hours (update LongResponseDetector.kt)
- **CI/Visual Verification**: 1-3 hours (three loops)

**Total**: 9-13 hours implementation + testing + verification

**Critical Path Phases:**
1. Phase 0: Detection Layer (2 hours)
2. Phase 1: Extraction Engine (2 hours)
3. Phase 2: Summarization Engine (4 hours)
4. Phase 3: Integration (0.75 hours)
5. Phase 4: Testing (3 hours)
6. Phase 5: Verification (1-3 hours)
