# Proposal: epic9-phase3-fix-ci-and-gqa-dataset

## Summary
Fix failing CI build and implement Golden QA Dataset (GQA-006) with 50+ test cases for validating context compression quality.

## Motivation
**Priority 1 - CI Failure**: CI is currently failing on lint checks, blocking all deployments. The GQA evaluation framework code (added in Phase 2) references `InferenceEngine` interface that doesn't exist in the codebase, causing compilation errors.

**Priority 2 - GQA Dataset**: Epic 9 Phase 2 delivered the compression framework and evaluation code structure, but lacks the actual golden test dataset needed to validate compression quality. Without these test cases, we cannot:
- Verify that compression preserves conversation coherence
- Catch regression bugs in compression logic
- Benchmark compression performance across different scenarios
- Ensure important messages (starred, system) are preserved

## Scope

### IN SCOPE
**Fix CI (Priority 1)**:
- Fix `InferenceEngine` interface reference in GQA evaluation code
- Resolve all lint errors causing CI failure
- Verify CI passes (lint + tests + build)

**GQA Dataset Implementation (Priority 2)**:
- Create `app/src/test/resources/gqa/` directory structure
- Generate 50+ golden test cases across 5 categories:
  - Long Conversation (LC): 50-100 messages, overall coherence
  - Code Discussion (CD): Code blocks, technical accuracy
  - Multi-Topic (MT): 3-5 topics, boundary preservation
  - Importance Preservation (IP): Starred/system messages must preserve
  - Summarization Quality (SQ): Active summarization accuracy
- Create `GoldenQARunnerTest.kt` to execute dataset tests
- Integrate with existing `CompressionQualityMonitor.kt`
- Add GQA tests to CI pipeline

### OUT OF SCOPE
- UI changes (this is backend testing infrastructure)
- New compression algorithms (use existing from Phase 2)
- Performance optimization (focus on correctness validation)
- Semantic similarity using LLMs (use Jaccard overlap MVP approach)

## Acceptance Criteria

### CI Fix
- [x] CI lint job passes
- [x] CI test job passes
- [x] CI build job passes (APK artifacts generated)
- [x] All GitHub Actions workflows green

### GQA Dataset
- [x] At least 50 golden test cases created
- [x] All 5 test categories have at least 8 test cases each
- [x] Test cases are in YAML format in `app/src/test/resources/gqa/`
- [x] `GoldenQARunnerTest.kt` loads and executes all test cases
- [x] All golden tests pass when run via `./gradlew test` (in CI)
- [x] GQA test results are logged with pass/fail status
- [x] Failed tests show diff between expected and actual

### Integration
- [x] `CompressionQualityMonitor.kt` hooks into GQA validation
- [x] `CompressionMetrics.kt` tracks GQA pass rate
- [x] GQA tests run automatically in CI on every PR
- [x] Documentation in `LESSONS_LEARNED.md` updated

### Visual Verification
- [x] APK installs successfully on device
- [x] App launches and shows welcome screen
- [x] No runtime crashes related to compression code

## Technical Approach

### Phase 1: Fix CI (2 hours)
1. **Identify root cause**: `InferenceEngine` interface doesn't exist
2. **Options**:
   - Option A: Create minimal `InferenceEngine` interface
   - Option B: Remove dependency on `InferenceEngine` from GQA evaluator
   - **RECOMMENDED: Option B** - Make `InferenceEngine` nullable and use mock responses for GQA testing (skipInference=true by default)
3. **Fix approach**:
   - Update `GQAEvaluator.kt` to not require `InferenceEngine` (already nullable)
   - Remove unused imports
   - Run lint locally (via git commit hook)
4. **Verify**: Push and check CI passes

### Phase 2: GQA Dataset Creation (6 hours)

#### 2.1 Directory Structure
```
app/src/test/resources/gqa/
├── long_conversation/
│   ├── lc_001_general_chat.yaml
│   ├── lc_002_technical_support.yaml
│   └── ... (10 test cases)
├── code_discussion/
│   ├── cd_001_kotlin_debug.yaml
│   ├── cd_002_architecture_review.yaml
│   └── ... (10 test cases)
├── multi_topic/
│   ├── mt_001_project_planning.yaml
│   └── ... (10 test cases)
├── importance_preservation/
│   ├── ip_001_starred_messages.yaml
│   └── ... (10 test cases)
└── summarization_quality/
    ├── sq_001_conversation_summary.yaml
    └── ... (10 test cases)
```

#### 2.2 YAML Test Case Format
```yaml
test_case_id: "lc_001_general_chat"
category: "LONG_CONVERSATION"
description: "50-message general chat, verify coherence after compression"
min_overall_score: 0.75

original_context:
  messages:
    - role: "user"
      content: "Hello, I need help with Android development"
      is_starred: false
    - role: "assistant"
      content: "I'd be happy to help! What specific area?"
    # ... 48 more messages

evaluation_queries:
  - query: "What was discussed about Jetpack Compose?"
    min_acceptable_score: 0.70
    expected_keywords: ["compose", "ui", "declarative"]
  - query: "What build tool was mentioned?"
    min_acceptable_score: 0.70
    expected_keywords: ["gradle", "build"]
```

#### 2.3 Test Runner Implementation
```kotlin
// app/src/test/java/ai/ondevice/app/evaluation/GoldenQARunnerTest.kt
class GoldenQARunnerTest {
    private val datasetLoader = GQADatasetLoader(context)
    private val evaluator = GQAEvaluator(compressor, null, tokenCounter, logger)

    @Test
    fun `run all GQA golden tests`() {
        val dataset = datasetLoader.loadDataset()
        val report = runBlocking {
            evaluator.evaluate(dataset, skipInference = true)
        }

        // Assert overall pass rate >= 90%
        assertTrue(report.passRate >= 0.90,
            "GQA pass rate too low: ${report.passRate}. Failed:\n${report.failedTestSummary()}")

        // Log detailed report
        println(report.toMarkdownSummary())
    }
}
```

#### 2.4 Integration Points
- `GQADatasetLoader.kt` - Already exists, loads YAML test cases
- `CompressionQualityMonitor.kt` - Add `evaluateAgainstGQA()` method
- `CompressionMetrics.kt` - Add `gqaPassRate: Double` field

### Phase 3: CI Integration (1 hour)
- Ensure `./gradlew test` runs GQA tests
- Verify GitHub Actions includes test step
- Add test resources to build.gradle if needed

## References
- Epic 9 Phase 2: Compression framework implementation (COMPLETE)
- `app/src/main/java/ai/ondevice/app/compression/` - Existing compression code
- `app/src/main/java/ai/ondevice/app/evaluation/` - GQA evaluation framework
- GQA-006 standard: Golden QA Dataset for Context Compression

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Lint failure cause unclear | Review recent commits, check GQA evaluation imports |
| 50+ test cases too time-consuming | Generate programmatically with templates |
| Test cases too simple/unrealistic | Base on real conversation patterns from LESSONS_LEARNED.md |
| CI still fails after fix | Test locally first via git hooks, iterate until pass |

## Dependencies
- Epic 9 Phase 2 MUST be complete (it is - 28 unit tests, 4 production files)
- YAML parsing library (snakeyaml) already added in Phase 2
- Android test context for loading resources

## Success Metrics
- CI build time: <10 minutes
- GQA test execution time: <30 seconds
- GQA pass rate: ≥90% for baseline compression
- Zero lint/compilation errors
- Zero CI failures on main branch
