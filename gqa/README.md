# GQA-006: Context Compression Golden Dataset

## Overview

This dataset contains 50 carefully crafted test cases to validate the quality of context compression in long conversations. Each test case simulates a realistic conversation scenario and defines quality expectations.

## Dataset Structure

```yaml
test_cases:
  - test_case_id: "GQA-006-LC-001"  # LC = Long Conversation
    category: "long_conversation"
    description: "Technical discussion about Android development spanning 50 messages"
    original_context:
      messages:
        - role: "user"
          content: "..."
          is_starred: false
          is_system_generated: false
          is_first_in_conversation: true
        - role: "assistant"
          content: "..."
    evaluation_queries:
      - query: "What was discussed about view models?"
        min_acceptable_score: 0.85
        expected_keywords: ["ViewModel", "lifecycle", "state"]
    min_overall_score: 0.85
```

## Categories

### 1. Long Conversation (LC)
- 10 test cases with 50-100 messages
- Tests: Overall coherence preservation after compression
- Min score: 0.85

### 2. Code Discussion (CD)
- 10 test cases with code blocks
- Tests: Code snippet preservation and technical accuracy
- Min score: 0.90 (code is critical)

### 3. Multi-Topic (MT)
- 10 test cases covering 3-5 distinct topics
- Tests: Topic boundary preservation
- Min score: 0.85

### 4. Importance Preservation (IP)
- 10 test cases with starred/system messages
- Tests: Critical message retention
- Min score: 1.0 (must never drop important messages)

### 5. Summarization Quality (SQ)
- 10 test cases requiring active summarization
- Tests: Summary accuracy and coherence
- Min score: 0.80

## Test Case IDs

Format: `GQA-006-{CATEGORY}-{NUMBER}`

Examples:
- GQA-006-LC-001 (Long Conversation #1)
- GQA-006-CD-005 (Code Discussion #5)
- GQA-006-MT-010 (Multi-Topic #10)
- GQA-006-IP-003 (Importance Preservation #3)
- GQA-006-SQ-007 (Summarization Quality #7)

## Evaluation Metrics

### Quality Score Calculation

For each test case:
1. Compress the original context
2. For each evaluation query:
   - Generate response with original context (baseline)
   - Generate response with compressed context (test)
   - Calculate semantic similarity (MVP: Jaccard word overlap)
3. Aggregate scores: average, min, max

### Pass Criteria

- **Per Query**: Score ≥ min_acceptable_score for that query
- **Per Test Case**: Average score ≥ min_overall_score
- **Overall**: ≥90% of test cases pass (45/50)

## Usage

```kotlin
// Load dataset
val dataset = GQADatasetLoader.load("gqa/gqa-006-context-compression.yaml")

// Run evaluation
val results = GQAEvaluator.evaluate(
    dataset = dataset,
    compressor = contextCompressor
)

// Generate report
val report = GQAReporter.generate(results)
println(report.summary)
```

## Maintenance

- **Add new test cases** when edge cases discovered
- **Update min_acceptable_score** based on production data
- **Version dataset** when schema changes (GQA-006.1, GQA-006.2, etc.)

## Version History

- **GQA-006.0** (2026-01-13): Initial version with 50 test cases
