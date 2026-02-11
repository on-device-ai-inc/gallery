# Tasks: enhance-topic-extraction-multilingual

## Phase 0: Detection Layer (Long vs Short Form)

- [ ] **Task 0.1**: Create DetectionResult data class with confidence scoring
  - Acceptance: DetectionResult has fields: confidence (Float), signals (List), shouldShowStatus (Boolean), reasoning (String)
  - Acceptance: DetectionSignal enum has all signal types (BREVITY_BLOCKER, STRONG_ACTION, WEAK_ACTION, LENGTH_MODIFIER, EXPLICIT_REQUEST, QUESTION_PENALTY)
  - Estimated: 20 minutes

- [ ] **Task 0.2**: Implement brevity blocker detection (veto logic)
  - Acceptance: hasBrevityModifier() detects: "brief", "briefly", "short", "shortly", "quick", "quickly", "concise", "concisely", "one sentence", "single sentence"
  - Acceptance: Returns immediate 0.0 confidence when brevity detected
  - Estimated: 30 minutes

- [ ] **Task 0.3**: Implement strong action detection (verb + content type)
  - Acceptance: hasStrongAction() matches: "write thesis", "create essay", "generate analysis", etc.
  - Acceptance: Covers English and Swahili patterns
  - Acceptance: Returns +0.50 confidence weight
  - Estimated: 45 minutes

- [ ] **Task 0.4**: Implement weak action detection (verb without content type)
  - Acceptance: hasWeakAction() matches: "do", "make", "give", "provide" (without content type)
  - Acceptance: Returns +0.25 confidence weight (lower than strong action)
  - Estimated: 30 minutes

- [ ] **Task 0.5**: Implement length modifier detection
  - Acceptance: hasLengthModifier() detects: "comprehensive", "detailed", "thorough", "in-depth", "extensive", "elaborate", "step by step"
  - Acceptance: Covers English and Swahili keywords
  - Acceptance: Returns +0.30 confidence weight
  - Estimated: 30 minutes

- [ ] **Task 0.6**: Implement explicit request detection
  - Acceptance: hasExplicitRequest() matches: "step by step", "explain everything", "tell me everything", "in detail"
  - Acceptance: Returns +0.40 confidence weight
  - Estimated: 20 minutes

- [ ] **Task 0.7**: Implement question penalty logic
  - Acceptance: isPureQuestion() detects questions starting with: what, how, why, when, where, who (without follow-up)
  - Acceptance: Returns -0.20 confidence penalty
  - Acceptance: Multi-sentence prompts with questions don't get penalty
  - Estimated: 30 minutes

- [ ] **Task 0.8**: Implement main detectLongRequestWithConfidence() function
  - Acceptance: Returns DetectionResult with accumulated confidence score
  - Acceptance: Brevity blockers veto all other signals (immediate 0.0)
  - Acceptance: Positive signals accumulate up to 1.0 max
  - Acceptance: Confidence clamped to [0.0, 1.0] range
  - Acceptance: shouldShowStatus = (confidence >= 0.70)
  - Estimated: 1 hour

- [ ] **Task 0.9**: Maintain backward compatibility with detectLongRequest()
  - Acceptance: Old detectLongRequest() calls detectLongRequestWithConfidence() internally
  - Acceptance: Returns boolean (result.shouldShowStatus)
  - Acceptance: Existing call sites work without modification
  - Estimated: 15 minutes

- [ ] **Task 0.10**: Add detection layer tests to LongResponseDetectorTest.kt
  - Acceptance: Test "write one sentence" → 0.0 confidence (brevity veto)
  - Acceptance: Test "quick comprehensive analysis" → 0.0 confidence (brevity wins)
  - Acceptance: Test "write thesis" → 0.50 confidence (SHORT, below threshold)
  - Acceptance: Test "write comprehensive thesis" → 0.80 confidence (LONG)
  - Acceptance: Test "What is AI?" → 0.0 confidence (question penalty)
  - Acceptance: Test "do an analysis" → 0.25 confidence (weak action, SHORT)
  - Acceptance: Test "detailed step-by-step analysis" → 0.95 confidence (LONG)
  - Estimated: 1 hour

## Phase 1: Extraction Engine (TopicExtractor.kt)

- [ ] **Task 1.1**: Create TopicExtractor.kt with sealed class result types
  - Acceptance: File compiles with ExtractionResult.Success/Uncertain/Refused classes
  - Acceptance: Each result type has displaySummary, confidence fields
  - Estimated: 30 minutes

- [ ] **Task 1.2**: Implement vocabulary configuration (50+ verbs, prepositions, content types)
  - Acceptance: Vocabulary object contains all English and Swahili terms from spec
  - Acceptance: Organized by semantic categories (CREATE_VERBS, ANALYSIS_VERBS, etc.)
  - Estimated: 45 minutes

- [ ] **Task 1.3**: Implement 12 pre-compiled patterns with possessive quantifiers
  - Acceptance: All 12 patterns compile successfully
  - Acceptance: Patterns use possessive quantifiers (++) to prevent backtracking
  - Acceptance: Patterns ordered by confidence (0.95 → 0.65)
  - Estimated: 1.5 hours

- [ ] **Task 1.4**: Implement fast-path rejection with indexOf() trigger words
  - Acceptance: extract() checks trigger words before pattern matching
  - Acceptance: Benchmark shows indexOf() path is <5ms
  - Estimated: 30 minutes

- [ ] **Task 1.5**: Implement smart fallback with keyword extraction
  - Acceptance: Failed extraction produces meaningful keywords (not "Processing request")
  - Acceptance: Stopwords filtered from both English and Swahili
  - Acceptance: Keywords limited to 8 most relevant terms
  - Estimated: 45 minutes

- [ ] **Task 1.6**: Implement utility functions (cleanTopic, truncateSmart, normalizeWhitespace)
  - Acceptance: truncateSmart() preserves word boundaries
  - Acceptance: cleanTopic() removes trailing punctuation
  - Acceptance: normalizeWhitespace() collapses multiple spaces
  - Estimated: 30 minutes

## Phase 2: Summarization Engine (TopicSummarizer.kt)

- [ ] **Task 2.1**: Create TopicSummarizer.kt with input analysis types
  - Acceptance: IntentClass enum (8 values: CREATE/ANALYZE/EXPLAIN/COMPARE/SUMMARIZE/RESEARCH/HELP/QUESTION/UNKNOWN)
  - Acceptance: DetectedLanguage enum (5 values)
  - Acceptance: DetectedFormality enum (4 values)
  - Acceptance: InputAnalysis data class
  - Estimated: 30 minutes

- [ ] **Task 2.2**: Implement verb-to-intent semantic mapping (50+ verbs)
  - Acceptance: verbToIntent map covers all verbs from vocabulary
  - Acceptance: English and Swahili verbs both mapped
  - Acceptance: detectIntent() function uses map correctly
  - Estimated: 45 minutes

- [ ] **Task 2.3**: Implement English template system (8 intents × 4 formality levels)
  - Acceptance: englishTemplates map has entries for all 8 intent classes
  - Acceptance: Each intent has 4 formality levels (FORMAL/NEUTRAL/CASUAL/URGENT)
  - Acceptance: Templates use {topic} and {type} placeholders
  - Estimated: 1 hour

- [ ] **Task 2.4**: Implement Swahili template system (8 intents × 4 formality levels)
  - Acceptance: swahiliTemplates map has entries for all 8 intent classes
  - Acceptance: All templates are grammatically pure Swahili (no English mixing)
  - Acceptance: Templates use Swahili verbs (Kuchambua, Kueleza, etc.)
  - Estimated: 1 hour

- [ ] **Task 2.5**: Implement content type translation map (English → Swahili)
  - Acceptance: contentTypeTranslations map has 15+ entries
  - Acceptance: Common types covered (thesis→tasnifu, essay→insha, analysis→uchambuzi)
  - Estimated: 20 minutes

- [ ] **Task 2.6**: Implement input analysis functions (detectIntent, detectLanguage, detectFormality)
  - Acceptance: detectIntent() correctly identifies 8 intent classes from test inputs
  - Acceptance: detectLanguage() distinguishes ENGLISH/SWAHILI/CODE_SWITCHED variants
  - Acceptance: detectFormality() detects FORMAL/NEUTRAL/CASUAL/URGENT levels
  - Estimated: 1 hour

- [ ] **Task 2.7**: Implement language resolution (preserve user's language choice)
  - Acceptance: Pure English input → English output
  - Acceptance: Pure Swahili input → Swahili output
  - Acceptance: Code-switched (SW frame) → Swahili output
  - Acceptance: Code-switched (EN frame) → English output
  - Estimated: 30 minutes

- [ ] **Task 2.8**: Implement smart topic compression (clause-aware truncation)
  - Acceptance: compressTopic() preserves clause boundaries when truncating
  - Acceptance: Looks for natural break points (and, or, commas, dashes)
  - Acceptance: Falls back to word-boundary truncation if no break point
  - Acceptance: Never breaks mid-word
  - Estimated: 45 minutes

- [ ] **Task 2.9**: Implement main summarize() function with template substitution
  - Acceptance: Takes ExtractionResult and SummaryConfig
  - Acceptance: Returns natural language summary string
  - Acceptance: Substitutes {topic} and {type} placeholders correctly
  - Acceptance: Respects maxLength from config
  - Estimated: 1 hour

## Phase 3: Integration

- [ ] **Task 3.1**: Update LongResponseDetector.extractTopicFromUserPrompt()
  - Acceptance: Function now calls TopicExtractor.extract() then TopicSummarizer.summarize()
  - Acceptance: Returns String (summary) as before (backward compatible)
  - Acceptance: Uses default SummaryConfig (maxLength: 80, auto language)
  - Estimated: 30 minutes

- [ ] **Task 3.2**: Remove old TopicExtractor.kt implementation (if conflicts)
  - Acceptance: No compilation errors after removal
  - Acceptance: All references updated to new implementation
  - Estimated: 15 minutes

## Phase 4: Testing

- [ ] **Task 4.1**: Create TopicExtractorTest.kt with high-confidence pattern tests
  - Acceptance: 6 tests covering Success tier patterns (≥0.85 confidence)
  - Acceptance: Tests for: english_full_structure, swahili_kuhusu, comparison, code_switched, swahili_imperative_full
  - Estimated: 1 hour

- [ ] **Task 4.2**: Add medium-confidence pattern tests to TopicExtractorTest.kt
  - Acceptance: 6 tests covering Uncertain tier patterns (0.50-0.85 confidence)
  - Acceptance: Tests for: question_format, conversational, swahili_question, polite_request, length_modifier, colon_topic
  - Estimated: 45 minutes

- [ ] **Task 4.3**: Add fallback and edge case tests to TopicExtractorTest.kt
  - Acceptance: 3 tests for Refused tier (fallback with keywords)
  - Acceptance: 5 edge case tests (whitespace, punctuation, case insensitivity, long topics, pathological input)
  - Estimated: 45 minutes

- [ ] **Task 4.4**: Add performance tests to TopicExtractorTest.kt
  - Acceptance: Latency test: 100 iterations avg <50ms per typical input
  - Acceptance: Catastrophic backtracking test: pathological input completes <100ms
  - Estimated: 30 minutes

- [ ] **Task 4.5**: Create TopicSummarizerTest.kt with semantic accuracy tests
  - Acceptance: 8 tests covering all intent classes (CREATE/ANALYZE/EXPLAIN/COMPARE/SUMMARIZE/RESEARCH/HELP/QUESTION)
  - Acceptance: Verifies correct output verb for each intent (analyze→"Analyzing", not "Creating")
  - Estimated: 1 hour

- [ ] **Task 4.6**: Add language purity tests to TopicSummarizerTest.kt
  - Acceptance: 4 tests for English input → English output
  - Acceptance: 4 tests for Swahili input → pure Swahili output (no mixing)
  - Acceptance: 2 tests for code-switched input
  - Estimated: 45 minutes

- [ ] **Task 4.7**: Add formality detection tests to TopicSummarizerTest.kt
  - Acceptance: 4 tests covering FORMAL/NEUTRAL/CASUAL/URGENT detection
  - Acceptance: Verifies template selection matches detected formality
  - Estimated: 30 minutes

- [ ] **Task 4.8**: Add topic compression tests to TopicSummarizerTest.kt
  - Acceptance: Test long topic preserves clause boundary
  - Acceptance: Test long topic without clause uses word boundary
  - Acceptance: Test never breaks mid-word
  - Estimated: 30 minutes

- [ ] **Task 4.9**: Add integration tests to LongResponseDetectorTest.kt
  - Acceptance: 5 tests for extractTopicFromUserPrompt() with new implementation
  - Acceptance: Covers English, Swahili, code-switched, formal, casual inputs
  - Acceptance: Verifies semantic accuracy (intent matches output)
  - Estimated: 45 minutes

## Phase 5: Verification Loops

- [ ] **Task 5.1**: TEST LOOP - Run all unit tests locally
  - Acceptance: All TopicExtractorTest.kt tests pass (30+ tests)
  - Acceptance: All TopicSummarizerTest.kt tests pass (25+ tests)
  - Acceptance: All LongResponseDetectorTest.kt tests pass (updated tests)
  - Acceptance: No test failures, no skipped tests
  - If tests fail: Fix issues → Rerun → Repeat until all pass

- [ ] **Task 5.2**: CI LOOP - Push to GitHub Actions
  - Acceptance: CI build passes (green checkmark)
  - Acceptance: Lint (ktlint) passes
  - Acceptance: All tests pass in CI environment
  - Acceptance: APK artifact generated
  - If CI fails: Analyze logs → Fix → Push → Repeat until green

- [ ] **Task 5.3**: VISUAL LOOP - Verify on device with DroidRun
  - Acceptance: Install APK from CI artifacts
  - Acceptance: Test "analyze empires" → Shows "Analyzing..." (not "Creating...")
  - Acceptance: Test "explain quantum physics" → Shows "Explaining..."
  - Acceptance: Test "Nieleze kuhusu elimu" → Pure Swahili output (no mixing)
  - Acceptance: Test "What is AI?" → Shows "Researching AI"
  - Acceptance: Screenshots captured for all test cases
  - If visual wrong: Fix code → Rebuild → Reinstall → Repeat until correct

## Phase 6: Documentation

- [ ] **Task 6.1**: Update LESSONS_LEARNED.md with key discoveries
  - Acceptance: Document semantic verb-action mapping pattern
  - Acceptance: Document language preservation strategy (no mixing)
  - Acceptance: Document smart compression approach (clause boundaries)
  - Estimated: 30 minutes

- [ ] **Task 6.2**: Add inline documentation to TopicExtractor.kt
  - Acceptance: All patterns have descriptive comments
  - Acceptance: Performance characteristics documented (O(N) with possessive quantifiers)
  - Acceptance: Confidence scoring strategy explained
  - Estimated: 20 minutes

- [ ] **Task 6.3**: Add inline documentation to TopicSummarizer.kt
  - Acceptance: Template organization explained
  - Acceptance: Language resolution strategy documented
  - Acceptance: Topic compression algorithm explained
  - Estimated: 20 minutes

## Task Summary

**Total Tasks**: 48 (38 original + 10 detection layer)
**Estimated Time**: 9-13 hours (7-11 hours original + 2 hours detection)

**Critical Path**: Phase 0 → Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5

**Dependencies**:
- Phase 1 depends on Phase 0 (extraction may use detection confidence)
- Phase 2 depends on Phase 1 (TopicSummarizer uses TopicExtractor.ExtractionResult)
- Phase 3 depends on Phase 0 + 1 + 2 (integration requires all three layers)
- Phase 4 depends on Phase 0 + 1 + 2 + 3 (testing requires complete implementation)
- Phase 5 depends on Phase 4 (verification requires passing tests)
- Phase 6 can run in parallel with Phase 5 (documentation while waiting for CI)

**Phase Breakdown:**
- **Phase 0**: Detection Layer (10 tasks, 2 hours) - Confidence-based long/short detection
- **Phase 1**: Extraction Engine (6 tasks, 2 hours) - What to extract from prompt
- **Phase 2**: Summarization Engine (9 tasks, 4 hours) - How to display extracted content
- **Phase 3**: Integration (2 tasks, 0.75 hours) - Wire all three layers together
- **Phase 4**: Testing (9 tasks, 3 hours) - Comprehensive test coverage
- **Phase 5**: Verification (3 tasks, 1-3 hours) - TEST/CI/VISUAL loops
- **Phase 6**: Documentation (3 tasks, 0.5 hours) - Update lessons and inline docs
