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
 * Unit tests for TopicExtractor v2
 * Tests enhanced pattern matching with confidence ranking
 *
 * NOTE: TopicExtractor is not currently used in production code.
 * LongResponseDetector is used instead in LlmChatViewModel.
 * These tests are disabled until TopicExtractor is integrated.
 */
@org.junit.Ignore("TopicExtractor not used in production - LongResponseDetector is used instead")
class TopicExtractorTest {

  // ══════════════════════════════════════════════════════════════
  // PATTERN 1: Full Structure (confidence 0.95)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_writeThesisOnAI_usesFullStructure() {
    val result = TopicExtractor.extract("write a thesis on AI")
    assertEquals("Creating thesis on AI", result.summary)
    assertEquals("thesis", result.contentType)
    assertEquals("AI", result.topic)
    assertEquals(0.95f, result.confidence, 0.01f)
    assertEquals("full_structure", result.patternUsed)
  }

  @Test
  fun extract_doAnalysisOnEmpires_usesFullStructure() {
    val result = TopicExtractor.extract("do an analysis on the rise and fall of empires")
    assertEquals("Creating analysis on the rise and fall of empires", result.summary)
    assertEquals("analysis", result.contentType)
    assertEquals(0.95f, result.confidence, 0.01f)
  }

  @Test
  fun extract_createGuideAboutPython_usesFullStructure() {
    val result = TopicExtractor.extract("create a guide about Python programming")
    assertTrue(result.summary.startsWith("Creating guide about"))
    assertEquals("guide", result.contentType)
    assertEquals(0.95f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 2: Comparison (confidence 0.90)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_compareReactVsVue_usesComparison() {
    val result = TopicExtractor.extract("Compare React vs Vue")
    assertEquals("Comparing React vs Vue", result.summary)
    assertEquals("comparison", result.contentType)
    assertEquals("React vs Vue", result.topic)
    assertEquals(0.90f, result.confidence, 0.01f)
    assertEquals("comparison", result.patternUsed)
  }

  @Test
  fun extract_differenceBetween_usesComparison() {
    val result = TopicExtractor.extract("difference between capitalism and socialism")
    assertTrue(result.summary.contains("capitalism"))
    assertTrue(result.summary.contains("socialism"))
    assertEquals(0.90f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 3: Length Modifier (confidence 0.88)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_detailedAnalysis_usesLengthModifier() {
    val result = TopicExtractor.extract("detailed analysis on machine learning")
    assertTrue(result.summary.startsWith("Creating analysis"))
    assertEquals("analysis", result.contentType)
    assertEquals(0.88f, result.confidence, 0.01f)
    assertEquals("length_modifier", result.patternUsed)
  }

  @Test
  fun extract_comprehensiveGuide_usesLengthModifier() {
    val result = TopicExtractor.extract("comprehensive guide about data structures")
    assertTrue(result.summary.contains("guide"))
    assertEquals(0.88f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 4: Verb + Prep + Topic (confidence 0.85)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_explainAbout_usesVerbPrepTopic() {
    val result = TopicExtractor.extract("explain about quantum computing")
    assertEquals("Analyzing quantum computing", result.summary)
    assertNull(result.contentType)
    assertEquals("quantum computing", result.topic)
    assertEquals(0.85f, result.confidence, 0.01f)
    assertEquals("verb_prep_topic", result.patternUsed)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 5: Swahili (confidence 0.85)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_swahili_niandikeTasnifu_usesSwahili() {
    val result = TopicExtractor.extract("Niandike tasnifu kuhusu teknolojia")
    assertTrue(result.summary.startsWith("Kuunda"))
    assertEquals("tasnifu", result.contentType)
    assertEquals(0.85f, result.confidence, 0.01f)
    assertEquals("swahili", result.patternUsed)
  }

  @Test
  fun extract_swahili_nifanyieUchambuzi_usesSwahili() {
    val result = TopicExtractor.extract("Nifanyie uchambuzi kuhusu uchumi")
    assertTrue(result.summary.contains("uchambuzi"))
    assertEquals(0.85f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 6: Conversational (confidence 0.82)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_tellMeAbout_usesConversational() {
    val result = TopicExtractor.extract("tell me about blockchain technology")
    assertEquals("Exploring blockchain technology", result.summary)
    assertNull(result.contentType)
    assertEquals("blockchain technology", result.topic)
    assertEquals(0.82f, result.confidence, 0.01f)
    assertEquals("conversational", result.patternUsed)
  }

  @Test
  fun extract_helpMeUnderstand_usesConversational() {
    val result = TopicExtractor.extract("help me understand neural networks")
    assertTrue(result.summary.startsWith("Exploring"))
    assertEquals(0.82f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // PATTERN 7: Question Format (confidence 0.80)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_whatIsQuantum_usesQuestionFormat() {
    val result = TopicExtractor.extract("What is quantum computing?")
    assertEquals("Researching quantum computing", result.summary)
    assertNull(result.contentType)
    assertEquals("quantum computing", result.topic)
    assertEquals(0.80f, result.confidence, 0.01f)
    assertEquals("question_format", result.patternUsed)
  }

  @Test
  fun extract_howDoesAIWork_usesQuestionFormat() {
    val result = TopicExtractor.extract("How does artificial intelligence work")
    assertTrue(result.summary.startsWith("Researching"))
    assertEquals(0.80f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // FALLBACK: Smart fallback (confidence 0.50)
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_randomText_usesFallback() {
    val result = TopicExtractor.extract("Just some random text without structure")
    assertTrue(result.summary.startsWith("Working on:"))
    assertNull(result.contentType)
    assertEquals(0.50f, result.confidence, 0.01f)
    assertEquals("fallback", result.patternUsed)
  }

  @Test
  fun extract_emptyPrompt_usesFallback() {
    val result = TopicExtractor.extract("   ")
    assertEquals("Processing your request", result.summary)
    assertEquals(0.50f, result.confidence, 0.01f)
  }

  // ══════════════════════════════════════════════════════════════
  // EDGE CASES
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_veryLongTopic_truncates() {
    val longTopic = "a".repeat(200)
    val result = TopicExtractor.extract("write a thesis on $longTopic")
    assertTrue(result.summary.length < 150) // "Creating thesis on " + 80 chars + "..."
    assertTrue(result.summary.endsWith("..."))
  }

  @Test
  fun extract_punctuationCleaning_removesTrailing() {
    val result = TopicExtractor.extract("write a thesis on AI ethics?!")
    assertEquals("Creating thesis on AI ethics", result.summary)
  }

  @Test
  fun extract_multipleSpaces_normalized() {
    val result = TopicExtractor.extract("write    a    thesis    on    AI")
    assertTrue(result.summary.contains("thesis on AI"))
    assertFalse(result.summary.contains("    "))
  }

  // ══════════════════════════════════════════════════════════════
  // CONFIDENCE RANKING
  // ══════════════════════════════════════════════════════════════

  @Test
  fun extract_multipleMatches_highestConfidenceWins() {
    // This could match both full_structure (0.95) and verb_prep_topic (0.85)
    val result = TopicExtractor.extract("write a thesis on AI")
    // Should pick full_structure (higher confidence)
    assertEquals("full_structure", result.patternUsed)
    assertEquals(0.95f, result.confidence, 0.01f)
  }
}
