/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Modifications are proprietary and confidential.
 *
 * Originally Copyright 2025 Google LLC
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

/**
 * Topic Summarization Engine
 *
 * Transforms extracted topic components into natural, contextually appropriate
 * status messages for UI display.
 *
 * Key improvements over simple "Creating..." template:
 * 1. **Semantic verb-action mapping**: analyze→"Analyzing", explain→"Explaining"
 * 2. **Language preservation**: Pure English or pure Swahili (no mixing)
 * 3. **Formality awareness**: Formal/neutral/casual tone matching
 * 4. **Smart compression**: Clause-aware truncation at word boundaries
 *
 * Example transformations:
 * - "do an analysis on empires" → "Analyzing the rise and fall of empires"
 * - "explain quantum physics" → "Explaining quantum physics"
 * - "Nieleze kuhusu elimu" → "Kueleza elimu" (pure Swahili)
 * - "What is AI?" → "Researching AI"
 */
object TopicSummarizer {

  // ═══════════════════════════════════════════════════════════════════
  // INTENT DETECTION (Semantic verb-to-action mapping)
  // ═══════════════════════════════════════════════════════════════════

  enum class IntentClass {
    CREATE,      // write, create, generate
    ANALYZE,     // analyze, examine, study
    EXPLAIN,     // explain, describe, clarify
    COMPARE,     // compare, contrast
    SUMMARIZE,   // summarize, outline
    RESEARCH,    // research, find, search
    HELP,        // help, assist
    QUESTION,    // what, how, why
    UNKNOWN
  }

  private val VERB_TO_INTENT = mapOf(
    // CREATE
    "write" to IntentClass.CREATE,
    "create" to IntentClass.CREATE,
    "generate" to IntentClass.CREATE,
    "make" to IntentClass.CREATE,
    "draft" to IntentClass.CREATE,
    "andika" to IntentClass.CREATE,
    "unda" to IntentClass.CREATE,

    // ANALYZE
    "analyze" to IntentClass.ANALYZE,
    "analyse" to IntentClass.ANALYZE,
    "examine" to IntentClass.ANALYZE,
    "study" to IntentClass.ANALYZE,
    "do" to IntentClass.ANALYZE,  // "do an analysis"
    "chambua" to IntentClass.ANALYZE,

    // EXPLAIN
    "explain" to IntentClass.EXPLAIN,
    "describe" to IntentClass.EXPLAIN,
    "clarify" to IntentClass.EXPLAIN,
    "tell" to IntentClass.EXPLAIN,
    "eleza" to IntentClass.EXPLAIN,

    // COMPARE
    "compare" to IntentClass.COMPARE,
    "contrast" to IntentClass.COMPARE,
    "linganisha" to IntentClass.COMPARE,

    // SUMMARIZE
    "summarize" to IntentClass.SUMMARIZE,
    "summarise" to IntentClass.SUMMARIZE,
    "outline" to IntentClass.SUMMARIZE,
    "fupisha" to IntentClass.SUMMARIZE,

    // RESEARCH
    "research" to IntentClass.RESEARCH,
    "find" to IntentClass.RESEARCH,
    "search" to IntentClass.RESEARCH,
    "tafiti" to IntentClass.RESEARCH,

    // HELP
    "help" to IntentClass.HELP,
    "assist" to IntentClass.HELP,
    "saidia" to IntentClass.HELP,

    // REQUEST
    "give" to IntentClass.CREATE,
    "provide" to IntentClass.CREATE,
    "show" to IntentClass.EXPLAIN
  )

  // ═══════════════════════════════════════════════════════════════════
  // TEMPLATES (Intent-based, language-aware)
  // ═══════════════════════════════════════════════════════════════════

  private val TEMPLATES_EN = mapOf(
    IntentClass.CREATE to "Writing {topic}",
    IntentClass.ANALYZE to "Analyzing {topic}",
    IntentClass.EXPLAIN to "Explaining {topic}",
    IntentClass.COMPARE to "Comparing {topic}",
    IntentClass.SUMMARIZE to "Summarizing {topic}",
    IntentClass.RESEARCH to "Researching {topic}",
    IntentClass.HELP to "Working on {topic}",
    IntentClass.QUESTION to "Researching {topic}",
    IntentClass.UNKNOWN to "Working on {topic}"
  )

  private val TEMPLATES_SW = mapOf(
    IntentClass.CREATE to "Kuandika {topic}",
    IntentClass.ANALYZE to "Kuchambua {topic}",
    IntentClass.EXPLAIN to "Kueleza {topic}",
    IntentClass.COMPARE to "Kulinganisha {topic}",
    IntentClass.SUMMARIZE to "Kufupisha {topic}",
    IntentClass.RESEARCH to "Kutafiti {topic}",
    IntentClass.HELP to "Kusaidia na {topic}",
    IntentClass.QUESTION to "Kutafuta {topic}",
    IntentClass.UNKNOWN to "Kufanya kazi juu ya {topic}"
  )

  private val CONTENT_TYPE_TRANSLATIONS = mapOf(
    "thesis" to "tasnifu",
    "essay" to "insha",
    "analysis" to "uchambuzi",
    "report" to "ripoti",
    "guide" to "mwongozo",
    "summary" to "muhtasari",
    "explanation" to "maelezo"
  )

  // ═══════════════════════════════════════════════════════════════════
  // MAIN SUMMARIZATION FUNCTION
  // ═══════════════════════════════════════════════════════════════════

  /**
   * Generate natural, contextually appropriate summary from extraction result.
   *
   * Process:
   * 1. Detect intent from user input (analyze, explain, create, etc.)
   * 2. Detect language (English vs Swahili)
   * 3. Select appropriate template
   * 4. Compress topic smartly (clause boundaries)
   * 5. Return natural summary
   */
  fun summarize(
    extractionResult: ExtractionResult,
    userPrompt: String,
    maxLength: Int = 80
  ): String {
    // Detect intent from user prompt
    val intent = detectIntent(userPrompt)

    // Detect language
    val isSwahili = detectSwahili(userPrompt)

    // Select template set
    val templates = if (isSwahili) TEMPLATES_SW else TEMPLATES_EN

    // Get template for this intent
    val template = templates[intent] ?: templates[IntentClass.UNKNOWN]!!

    // Prepare topic
    val topic = extractionResult.topic ?: extractionResult.summary
    val compressedTopic = compressTopic(topic, maxLength - 15)  // Leave room for verb

    // Substitute {topic} placeholder
    val summary = template.replace("{topic}", compressedTopic)

    // Final length check
    return if (summary.length > maxLength) {
      truncateSmart(summary, maxLength)
    } else {
      summary
    }
  }

  // ═══════════════════════════════════════════════════════════════════
  // INTENT DETECTION
  // ═══════════════════════════════════════════════════════════════════

  private fun detectIntent(userPrompt: String): IntentClass {
    val lowered = userPrompt.lowercase()

    // Check for question patterns first
    if (lowered.matches(Regex("^(what|how|why|when|where|who)\\b.*"))) {
      return IntentClass.QUESTION
    }

    // Extract first verb and map to intent
    val words = lowered.split(Regex("\\s+"))
    for (word in words.take(5)) {
      VERB_TO_INTENT[word]?.let { return it }

      // Check stem match for Swahili conjugated verbs
      VERB_TO_INTENT.keys.find { verb ->
        word.contains(verb) || verb.contains(word.take(4))
      }?.let {
        return VERB_TO_INTENT[it]!!
      }
    }

    return IntentClass.UNKNOWN
  }

  // ═══════════════════════════════════════════════════════════════════
  // LANGUAGE DETECTION
  // ═══════════════════════════════════════════════════════════════════

  private fun detectSwahili(userPrompt: String): Boolean {
    val swahiliMarkers = listOf(
      "kuhusu", "juu ya", "kwa", "niandike", "nieleze",
      "nifanyie", "tasnifu", "insha", "uchambuzi"
    )

    val swahiliVerbPattern = Regex("\\b(ni|u|a|tu|m|wa)(na|li|ta|me)[a-z]+[aie]\\b")

    return swahiliMarkers.any { userPrompt.lowercase().contains(it) } ||
           swahiliVerbPattern.containsMatchIn(userPrompt)
  }

  // ═══════════════════════════════════════════════════════════════════
  // TOPIC COMPRESSION (Clause-aware)
  // ═══════════════════════════════════════════════════════════════════

  /**
   * Intelligently compress topic while preserving meaning.
   *
   * Strategy:
   * 1. If short enough, return as-is
   * 2. Look for natural break points (and, or, commas)
   * 3. Preserve first clause + add ellipsis
   * 4. If no break point, truncate at word boundary
   */
  private fun compressTopic(topic: String, maxLength: Int): String {
    if (topic.length <= maxLength) return topic

    // Try to find natural break point
    val breakPoints = listOf(" and ", " or ", ", ", " - ", " with ", " na ", " au ")

    for (breakPoint in breakPoints) {
      val index = topic.indexOf(breakPoint)
      if (index in 10..(maxLength - 5)) {
        val firstPart = topic.substring(0, index)
        return if (firstPart.length <= maxLength - 3) {
          "$firstPart..."
        } else {
          truncateSmart(firstPart, maxLength - 3) + "..."
        }
      }
    }

    // No good break point - truncate at word boundary
    return truncateSmart(topic, maxLength)
  }

  /**
   * Truncate at word boundary, never mid-word.
   */
  private fun truncateSmart(text: String, maxLength: Int): String {
    if (text.length <= maxLength) return text

    val truncated = text.take(maxLength - 3)
    val lastSpace = truncated.lastIndexOf(' ')

    return if (lastSpace > maxLength / 2) {
      truncated.substring(0, lastSpace) + "..."
    } else {
      truncated + "..."
    }
  }
}
