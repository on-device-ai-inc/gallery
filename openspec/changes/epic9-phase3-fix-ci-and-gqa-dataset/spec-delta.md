# Spec Delta: epic9-phase3-fix-ci-and-gqa-dataset

## ADDED

### gqa-evaluation-spec.md

```markdown
# GQA-006: Golden QA Dataset for Context Compression

## Overview
Comprehensive test dataset for validating context compression quality across different conversation scenarios.

## Dataset Structure

### Directory Layout
```
app/src/test/resources/gqa/
├── dataset_metadata.yaml         # Dataset version, creation date, targets
├── long_conversation/            # 50-100 message conversations
├── code_discussion/              # Technical code-related conversations
├── multi_topic/                  # Conversations with topic transitions
├── importance_preservation/      # Starred, system, first messages
└── summarization_quality/        # Summarization accuracy tests
```

### Test Case Format (YAML)
```yaml
test_case_id: "lc_001_general_chat"
category: "LONG_CONVERSATION"
description: "50-message general chat, verify coherence after compression"
min_overall_score: 0.75

original_context:
  messages:
    - role: "user" | "assistant" | "system"
      content: "Message text here"
      is_starred: false | true
      is_system_generated: false | true
      is_first_in_conversation: false | true

evaluation_queries:
  - query: "What was discussed about X?"
    min_acceptable_score: 0.70
    expected_keywords: ["keyword1", "keyword2"]
```

## Test Categories

### 1. Long Conversation (LC)
- **Purpose**: Verify coherence in 50-100 message conversations
- **Test Cases**: 10+
- **Min Score**: 0.75
- **Scenarios**:
  - General chat
  - Technical support
  - Project planning
  - Feature discussions

### 2. Code Discussion (CD)
- **Purpose**: Verify technical accuracy with code blocks
- **Test Cases**: 10+
- **Min Score**: 0.80 (higher for code)
- **Scenarios**:
  - Debugging sessions
  - Code reviews
  - Architecture decisions
  - Implementation discussions

### 3. Multi-Topic (MT)
- **Purpose**: Verify topic boundary preservation
- **Test Cases**: 10+
- **Min Score**: 0.70
- **Scenarios**:
  - 3-5 distinct topics
  - Topic transitions
  - Context switching

### 4. Importance Preservation (IP)
- **Purpose**: Ensure critical messages never compressed
- **Test Cases**: 10+
- **Min Score**: 0.95 (critical)
- **Scenarios**:
  - Starred messages
  - System messages
  - First messages in conversation

### 5. Summarization Quality (SQ)
- **Purpose**: Verify active summarization accuracy
- **Test Cases**: 10+
- **Min Score**: 0.75
- **Scenarios**:
  - Conversation summaries
  - Key point extraction
  - Context distillation

## Evaluation Metrics

### Similarity Score
- **Method**: Jaccard word overlap (MVP)
- **Formula**: `|A ∩ B| / |A ∪ B|`
- **Future**: Semantic similarity via embeddings

### Keyword Match Score
- **Method**: Expected keywords present in response
- **Formula**: `matched_keywords / total_expected_keywords`

### Combined Score
- **Formula**: `(similarity * 0.7) + (keyword_match * 0.3)`
- **Threshold**: Per-query `min_acceptable_score`

### Pass Criteria
- Query passes if: `combined_score >= min_acceptable_score`
- Test case passes if: `all_queries_pass AND avg_score >= min_overall_score`
- Dataset passes if: `pass_rate >= 0.90` (90%)

## Integration Points

### CompressionQualityMonitor
```kotlin
fun evaluateAgainstGQA(): GQAEvaluationReport {
    val dataset = GQADatasetLoader(context).loadDataset()
    val evaluator = GQAEvaluator(compressor, null, tokenCounter, logger)
    return evaluator.evaluate(dataset, skipInference = true)
}
```

### CompressionMetrics
```kotlin
data class CompressionMetrics(
    // ... existing fields
    val gqaPassRate: Double // 0.0-1.0, from latest GQA run
)
```

## Test Execution

### Local (via CI)
```bash
# Run GQA tests
./gradlew test --tests GoldenQARunnerTest

# Or run all tests
./gradlew test
```

### CI Pipeline
```yaml
# .github/workflows/ci.yml already includes test step
- name: Run Tests
  run: ./gradlew test
```

## Reporting

### Test Output
```
GQA-006 Evaluation Report
Date: 2025-01-12 22:00:00
Dataset Version: 1.0.0

Overall Results
| Metric       | Value      |
|--------------|------------|
| Total Tests  | 50         |
| Passed       | 47         |
| Failed       | 3          |
| Pass Rate    | 94.0%      |
| Average Score| 0.823      |

Results by Category
| Category              | Passed | Total | Pass Rate | Avg Score |
|-----------------------|--------|-------|-----------|-----------|
| LONG_CONVERSATION     | 10     | 10    | 100.0%    | 0.851     |
| CODE_DISCUSSION       | 9      | 10    | 90.0%     | 0.789     |
| MULTI_TOPIC           | 10     | 10    | 100.0%    | 0.812     |
| IMPORTANCE_PRESERVATION| 10    | 10    | 100.0%    | 0.967     |
| SUMMARIZATION_QUALITY | 8      | 10    | 80.0%     | 0.754     |
```

## Maintenance

### Adding New Test Cases
1. Create YAML file in appropriate category folder
2. Follow naming: `{category}_{number}_{description}.yaml`
3. Ensure min_overall_score is set appropriately
4. Add 3-5 evaluation queries per test case
5. Run test suite to validate

### Updating Test Cases
- Increment dataset version in metadata
- Document changes in changelog
- Re-run full suite to ensure no regressions

### Dataset Versioning
```yaml
# dataset_metadata.yaml
version: "1.0.0"
created_date: "2025-01-12"
total_test_cases: 50
categories: 5
target_pass_rate: 0.90
changelog:
  - version: "1.0.0"
    date: "2025-01-12"
    changes: "Initial GQA-006 dataset creation"
```

## Success Criteria
- ✅ 50+ test cases across 5 categories
- ✅ Pass rate ≥ 90% for baseline compression
- ✅ Execution time < 30 seconds
- ✅ Tests run automatically in CI
- ✅ Clear failure reporting with diffs
```

## MODIFIED

### compression-spec.md (Extends existing compression spec)

```diff
# Context Compression Specification

## Quality Monitoring

+ ### GQA-006 Integration
+
+ The compression system integrates with GQA-006 golden dataset for continuous quality validation.
+
+ ```kotlin
+ // app/src/main/java/ai/ondevice/app/compression/CompressionQualityMonitor.kt
+ class CompressionQualityMonitor {
+     // Existing quality check methods...
+
+     /**
+      * Evaluate compression quality against GQA-006 golden dataset.
+      *
+      * @return Evaluation report with pass/fail status and detailed metrics
+      */
+     suspend fun evaluateAgainstGQA(): GQAEvaluationReport {
+         val dataset = GQADatasetLoader(context).loadDataset()
+         val evaluator = GQAEvaluator(compressor, null, tokenCounter, logger)
+         return evaluator.evaluate(dataset, skipInference = true)
+     }
+ }
+ ```

### compression-metrics-spec.md

```diff
# Compression Metrics Specification

data class CompressionMetrics(
    val originalTokenCount: Int,
    val compressedTokenCount: Int,
    val reductionPercentage: Double,
    val compressionTimeMs: Long,
    val strategyUsed: String,
+
+   /**
+    * GQA-006 pass rate from latest evaluation run.
+    *
+    * Value: 0.0-1.0 (e.g., 0.94 = 94% pass rate)
+    * Updated: After each GQA evaluation
+    * Target: ≥ 0.90 (90%)
+    */
+   val gqaPassRate: Double = 0.0
)
```

### test-spec.md

```diff
# Testing Specification

## Unit Tests
- ✅ TokenCounterTest.kt (28 tests)
- ✅ ContextCompressorTest.kt (existing)
+
+ ## Golden QA Dataset Tests
+
+ ### GoldenQARunnerTest.kt
+
+ Executes all GQA-006 golden test cases to validate compression quality.
+
+ ```kotlin
+ @Test
+ fun `run all GQA golden tests`() {
+     val dataset = datasetLoader.loadDataset()
+     val report = runBlocking {
+         evaluator.evaluate(dataset, skipInference = true)
+     }
+
+     assertTrue(report.passRate >= 0.90,
+         "GQA pass rate too low: ${report.passRate}")
+
+     println(report.toMarkdownSummary())
+ }
+ ```
+
+ **Location**: `app/src/test/java/ai/ondevice/app/evaluation/GoldenQARunnerTest.kt`
+ **Resources**: `app/src/test/resources/gqa/`
+ **Execution**: Runs automatically in CI via `./gradlew test`
```

## REMOVED

None. This change is purely additive - no specs are being removed.

## Implementation Notes

### Files to Create
```
app/src/test/resources/gqa/
├── dataset_metadata.yaml
├── long_conversation/
│   ├── lc_001_general_chat.yaml
│   └── ... (9 more)
├── code_discussion/
│   ├── cd_001_kotlin_debug.yaml
│   └── ... (9 more)
├── multi_topic/
│   ├── mt_001_project_discussion.yaml
│   └── ... (9 more)
├── importance_preservation/
│   ├── ip_001_starred_messages.yaml
│   └── ... (9 more)
└── summarization_quality/
    ├── sq_001_conversation_summary.yaml
    └── ... (9 more)

app/src/test/java/ai/ondevice/app/evaluation/
└── GoldenQARunnerTest.kt
```

### Files to Modify
```
app/src/main/java/ai/ondevice/app/compression/
├── CompressionQualityMonitor.kt  (add evaluateAgainstGQA method)
└── CompressionMetrics.kt         (add gqaPassRate field)

app/src/main/java/ai/ondevice/app/evaluation/
├── GQAEvaluator.kt              (fix lint - remove InferenceEngine dependency)
├── GQADataset.kt                (fix lint if needed)
└── GQADatasetLoader.kt          (fix lint if needed)
```

### Build Configuration
```kotlin
// app/build.gradle.kts
android {
    sourceSets {
        getByName("test") {
            resources.srcDirs("src/test/resources")
        }
    }
}
```

## Validation

Before marking this change as complete, verify:

1. **CI is green**
   - Lint passes
   - All tests pass (including GQA)
   - Build succeeds

2. **Dataset complete**
   - 50+ YAML test cases
   - 5 categories populated
   - All test cases well-formed

3. **Tests passing**
   - GoldenQARunnerTest passes
   - Pass rate ≥ 90%
   - Execution time < 30s

4. **Integration working**
   - CompressionQualityMonitor.evaluateAgainstGQA() works
   - CompressionMetrics.gqaPassRate populated

5. **Documentation updated**
   - LESSONS_LEARNED.md has new entries
   - CODE_INDEX.md updated if needed

## Related Changes
- Epic 9 Phase 2: Compression framework (COMPLETE)
- Epic 9 Phase 1: Token counting (COMPLETE)
