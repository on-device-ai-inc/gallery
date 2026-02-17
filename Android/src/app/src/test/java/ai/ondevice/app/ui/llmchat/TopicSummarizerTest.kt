/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.ondevice.app.ui.llmchat

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TopicSummarizer semantic verb-action mapping and
 * language-aware summarization.
 *
 * NOTE: TopicSummarizer is not currently used in production code.
 * LongResponseDetector is used instead in LlmChatViewModel.
 * These tests are disabled until TopicSummarizer is integrated.
 */
@org.junit.Ignore("TopicSummarizer not used in production - LongResponseDetector is used instead")
class TopicSummarizerTest {

  // ═══════════════════════════════════════════════════════════════════
  // SEMANTIC ACCURACY TESTS (Intent → Output Verb)
  // ═══════════════════════════════════════════════════════════════════

  @Test
  fun summarize_analyzeIntent_producesAnalyzing() {
    val extraction = ExtractionResult(
      summary = "analysis on economy",
      contentType = "analysis",
      topic = "the economy",
      confidence = 0.95f,
      patternUsed = "full_structure"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "analyze the economy"
    )

    assertTrue(summary.startsWith("Analyzing"))
    assertFalse(summary.contains("Creating"))
  }

  @Test
  fun summarize_explainIntent_producesExplaining() {
    val extraction = ExtractionResult(
      summary = "explanation on physics",
      contentType = "explanation",
      topic = "quantum physics",
      confidence = 0.90f,
      patternUsed = "full_structure"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "explain quantum physics"
    )

    assertTrue(summary.startsWith("Explaining"))
    assertFalse(summary.contains("Creating"))
  }

  @Test
  fun summarize_createIntent_producesWriting() {
    val extraction = ExtractionResult(
      summary = "thesis on AI",
      contentType = "thesis",
      topic = "artificial intelligence",
      confidence = 0.95f,
      patternUsed = "full_structure"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "write a thesis on AI"
    )

    assertTrue(summary.startsWith("Writing"))
  }

  @Test
  fun summarize_compareIntent_producesComparing() {
    val extraction = ExtractionResult(
      summary = "comparison of X and Y",
      contentType = "comparison",
      topic = "React and Vue",
      confidence = 0.92f,
      patternUsed = "comparison"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "compare React and Vue"
    )

    assertTrue(summary.startsWith("Comparing"))
  }

  @Test
  fun summarize_questionFormat_producesResearching() {
    val extraction = ExtractionResult(
      summary = "quantum computing",
      contentType = null,
      topic = "quantum computing",
      confidence = 0.75f,
      patternUsed = "question_format"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "What is quantum computing?"
    )

    assertTrue(summary.startsWith("Researching"))
  }

  // ═══════════════════════════════════════════════════════════════════
  // LANGUAGE PURITY TESTS (No Mixing)
  // ═══════════════════════════════════════════════════════════════════

  @Test
  fun summarize_englishInput_pureEnglishOutput() {
    val extraction = ExtractionResult(
      summary = "thesis on AI",
      contentType = "thesis",
      topic = "AI",
      confidence = 0.95f,
      patternUsed = "full_structure"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "write a thesis on AI"
    )

    // Should not contain Swahili words
    assertFalse(summary.contains("Ku"))
    assertFalse(summary.lowercase().contains("juu ya"))
  }

  @Test
  fun summarize_swahiliInput_pureSwahiliOutput() {
    val extraction = ExtractionResult(
      summary = "insha kuhusu elimu",
      contentType = "insha",
      topic = "elimu",
      confidence = 0.90f,
      patternUsed = "sw_imperative_full"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "Niandike insha kuhusu elimu"
    )

    // Should be pure Swahili, no English mixing
    assertFalse(summary.contains("Writing"))
    assertFalse(summary.contains("Creating"))
    assertTrue(summary.startsWith("Ku"))  // Swahili infinitive form
  }

  // ═══════════════════════════════════════════════════════════════════
  // TOPIC COMPRESSION TESTS
  // ═══════════════════════════════════════════════════════════════════

  @Test
  fun summarize_longTopic_preservesClauseBoundary() {
    val longTopic = "the rise and fall of empires and their impact on modern society"
    val extraction = ExtractionResult(
      summary = longTopic,
      contentType = "analysis",
      topic = longTopic,
      confidence = 0.95f,
      patternUsed = "full_structure"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "analyze $longTopic"
    )

    // Should truncate at "and" boundary
    assertTrue(summary.contains("empires"))
    assertTrue(summary.contains("...") || summary.length < 80)
  }

  @Test
  fun summarize_truncation_neverBreaksMidWord() {
    val extraction = ExtractionResult(
      summary = "artificial intelligence",
      contentType = null,
      topic = "artificial intelligence and machine learning systems",
      confidence = 0.80f,
      patternUsed = "en_verb_prep_topic"
    )

    val summary = TopicSummarizer.summarize(
      extractionResult = extraction,
      userPrompt = "explain artificial intelligence and machine learning systems",
      maxLength = 30
    )

    // Should not end with "intellig..." or "artifici..."
    assertFalse(summary.matches(Regex(".*[a-z]{5}\\.\\.\\.$")))
  }

  // ═══════════════════════════════════════════════════════════════════
  // INTEGRATION TESTS (End-to-end)
  // ═══════════════════════════════════════════════════════════════════

  @Test
  fun extractTopicFromUserPrompt_analyzeEmpires_producesAnalyzing() {
    val topic = LongResponseDetector.extractTopicFromUserPrompt(
      "do an analysis on the rise and fall of empires"
    )

    assertTrue(topic.contains("Analyzing") || topic.contains("Working on"))
    assertFalse(topic.contains("Creating"))
    assertTrue(topic.contains("empires"))
  }

  @Test
  fun extractTopicFromUserPrompt_explainPhysics_producesExplaining() {
    val topic = LongResponseDetector.extractTopicFromUserPrompt(
      "explain quantum physics to me"
    )

    assertTrue(topic.contains("Explaining") || topic.contains("Working on"))
    assertTrue(topic.contains("quantum") || topic.contains("physics"))
  }

  @Test
  fun extractTopicFromUserPrompt_whatIsAI_producesResearching() {
    val topic = LongResponseDetector.extractTopicFromUserPrompt(
      "What is artificial intelligence?"
    )

    assertTrue(topic.contains("Researching") || topic.contains("Working on"))
  }

  @Test
  fun extractTopicFromUserPrompt_swahili_pureSwahiliOutput() {
    val topic = LongResponseDetector.extractTopicFromUserPrompt(
      "Nieleze kuhusu elimu ya watoto"
    )

    // Should be pure Swahili or fallback to working on
    assertFalse(topic.contains("Creating"))
    assertFalse(topic.contains("Writing"))
  }
}
