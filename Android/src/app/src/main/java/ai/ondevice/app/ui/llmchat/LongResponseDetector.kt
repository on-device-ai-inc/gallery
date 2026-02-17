/*
 * Copyright 2025 OnDevice Inc.
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
 * Detects long responses and extracts topic information.
 * Used to show status box instead of streaming for long responses.
 *
 * DETECTION uses a scoring system for accuracy:
 * - Action verbs: +2, Content types: +3, Length modifiers: +2
 * - Elaboration keywords: +4 (dive deeper, elaborate, tell me more, etc.)
 * - Explicit patterns: +3, Multiple intents: +1, Long prompt: +1
 * - Threshold: 4+ points = long form detected
 *
 * Elaboration keywords trigger detection immediately to avoid jerky scrolling
 * during follow-up requests for more detail.
 *
 * TOPIC SUMMARISATION uses semantic parsing (parse → understand → generate):
 * 1. Parse prompt into structured components (intents, content type, modifiers, subject, comparison)
 * 2. Generate a clean, natural summary from those components
 * Instead of rearranging the user's raw words, we understand what they're asking
 * and produce a concise status message.
 *
 * FEATURE FLAG: Set ENABLE_LONG_RESPONSE_STATUS to false to revert to normal streaming.
 */
object LongResponseDetector {

  // FEATURE FLAG
  const val ENABLE_LONG_RESPONSE_STATUS = true
  private const val LONG_FORM_THRESHOLD = 4
  private const val MAX_TOPIC_LENGTH = 80

  // ═══════════════════════════════════════════════════════════════════════════
  // Keyword & Pattern Definitions (single source of truth)
  // ═══════════════════════════════════════════════════════════════════════════

  private val ACTION_VERBS = listOf(
    "write", "create", "generate", "compose", "develop", "produce",
    "draft", "prepare", "do", "make", "give", "provide", "build",
    "put together", "come up with"
  )

  private val ANALYSIS_VERBS = listOf(
    "analyze", "analyse", "examine", "evaluate", "assess",
    "compare", "contrast", "explain", "describe", "discuss",
    "explore", "investigate", "review", "break down", "outline"
  )

  private val CONTENT_TYPES = listOf(
    "thesis", "essay", "paper", "guide", "tutorial", "article",
    "report", "document", "analysis", "study", "overview",
    "breakdown", "summary", "review", "presentation", "plan",
    "strategy", "proposal", "memo", "brief", "white paper",
    "case study", "deep dive", "comparison"
  )

  private val LENGTH_MODIFIERS = listOf(
    "detailed", "comprehensive", "complete", "full", "in-depth",
    "thorough", "extensive", "elaborate", "exhaustive", "lengthy",
    "long", "in detail", "step by step"
  )

  private val ELABORATION_KEYWORDS = listOf(
    "dive deeper", "go deeper", "delve deeper", "dig deeper",
    "elaborate", "expand on", "tell me more", "say more",
    "more detail", "more information", "further", "in more depth",
    "unpack", "flesh out", "develop further", "continue"
  )

  private val EXPLICIT_PATTERNS = Regex(
    "explain everything|tell me everything|cover (all|every)|" +
    "give me (a |an )?(full|complete|comprehensive|detailed)|" +
    "don't leave anything out|be (very )?thorough|" +
    "go (deep|in-depth)|leave no .* unturned",
    RegexOption.IGNORE_CASE
  )

  private val STREAMING_LONG_RESPONSE_KEYWORDS = listOf(
    "comprehensive", "detailed", "thesis", "extensive",
    "in-depth", "thorough", "I'll create", "I'll write",
    "I'll develop", "scholarly", "academic", "complete guide",
    "I'll provide", "I'll analyze", "I'll examine", "Let me break down"
  )

  // ═══════════════════════════════════════════════════════════════════════════
  // Detection
  // ═══════════════════════════════════════════════════════════════════════════

  data class DetectionResult(
    val isLongForm: Boolean,
    val score: Int,
    val signals: List<String>
  )

  fun detectLongRequest(userPrompt: String): Boolean {
    if (!ENABLE_LONG_RESPONSE_STATUS) return false
    return scoreLongFormIntent(userPrompt).isLongForm
  }

  fun scoreLongFormIntent(userPrompt: String): DetectionResult {
    val prompt = userPrompt.lowercase().trim()
    var score = 0
    val signals = mutableListOf<String>()

    val hasActionVerb = ACTION_VERBS.any { prompt.contains(Regex("\\b${Regex.escape(it)}\\b")) }
    if (hasActionVerb) { score += 2; signals.add("action_verb") }

    val matchedAnalysisVerbs = ANALYSIS_VERBS.filter { prompt.contains(Regex("\\b${Regex.escape(it)}\\b")) }
    if (matchedAnalysisVerbs.isNotEmpty()) { score += 2; signals.add("analysis_verb(${matchedAnalysisVerbs.first()})") }

    val matchedContentType = CONTENT_TYPES.firstOrNull { prompt.contains(Regex("\\b${Regex.escape(it)}\\b")) }
    if (matchedContentType != null) { score += 3; signals.add("content_type($matchedContentType)") }

    if (LENGTH_MODIFIERS.any { prompt.contains(it, ignoreCase = true) }) { score += 2; signals.add("length_modifier") }

    val matchedElaboration = ELABORATION_KEYWORDS.firstOrNull { prompt.contains(it) }
    if (matchedElaboration != null) { score += 4; signals.add("elaboration($matchedElaboration)") }

    if (EXPLICIT_PATTERNS.containsMatchIn(prompt)) { score += 3; signals.add("explicit_pattern") }
    if (matchedAnalysisVerbs.size >= 2) { score += 1; signals.add("multi_intent(${matchedAnalysisVerbs.size} verbs)") }
    if (prompt.length > 80) { score += 1; signals.add("long_prompt(${prompt.length} chars)") }

    return DetectionResult(score >= LONG_FORM_THRESHOLD, score, signals)
  }

  fun detectLongResponse(firstChunk: String): Boolean {
    if (!ENABLE_LONG_RESPONSE_STATUS) return false
    return STREAMING_LONG_RESPONSE_KEYWORDS.any { firstChunk.contains(it, ignoreCase = true) }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Semantic Topic Summarisation
  //
  // Instead of regex substring extraction, we:
  // 1. Parse the prompt into structured components
  // 2. Generate a clean, natural summary from those components
  //
  // Examples:
  //   "Write a 5 page PhD level thesis on global warming"
  //     → Writing PhD-level 5-page thesis on global warming
  //   "Do an analysis on past empires rise and fall and compare to modern times"
  //     → Analyzing past empires rise and fall vs. modern times
  //   "Break down the differences between React and Angular"
  //     → Explaining React vs. Angular
  // ═══════════════════════════════════════════════════════════════════════════

  /** Parsed components of a user prompt */
  private data class PromptComponents(
    val intents: List<String>,
    val contentType: String?,
    val modifiers: List<String>,
    val subject: String?,
    val compareTarget: String?,
    val betweenSubject: String?
  )

  /**
   * Extracts a clean, natural topic summary from the user's prompt.
   * Uses semantic parsing: parse → understand → generate.
   *
   * For follow-up elaboration requests (e.g., "tell me more", "dive deeper"),
   * extracts the topic from recent agent messages if conversationContext is provided.
   *
   * @param userPrompt The current user message
   * @param conversationContext Optional list of recent agent messages for context-aware extraction
   */
  fun extractTopicFromUserPrompt(
    userPrompt: String,
    conversationContext: List<String> = emptyList()
  ): String {
    val lower = userPrompt.lowercase().trim()

    // Check if this is a follow-up elaboration request
    val isElaborationRequest = ELABORATION_KEYWORDS.any { lower.contains(it) }

    if (isElaborationRequest && conversationContext.isNotEmpty()) {
      // Extract topic from recent agent messages
      val contextTopic = extractTopicFromContext(conversationContext)
      if (contextTopic != null) {
        return contextTopic
      }
    }

    // Fallback to standard extraction from user prompt
    val components = parsePromptComponents(userPrompt)
    return generateTopicSummary(components)
  }

  /**
   * Extracts topic from recent agent messages for follow-up elaboration requests.
   * Looks for indicators of what the agent was discussing.
   */
  private fun extractTopicFromContext(agentMessages: List<String>): String? {
    if (agentMessages.isEmpty()) return null

    // Get the most recent agent message (what we're elaborating on)
    val recentMessage = agentMessages.firstOrNull() ?: return null

    // Try to extract what the agent was explaining/writing about
    // Strategy 1: Look for "about X", "on X", "regarding X"
    Regex(
      "(?:about|on|regarding|concerning)\\s+(.+?)(?:[.,!?]|\\s+(?:and|but|however|which)|$)",
      RegexOption.IGNORE_CASE
    ).find(recentMessage)?.let {
      val subject = it.groupValues[1].trim().take(50)
      if (subject.length > 3) return "Elaborating on $subject"
    }

    // Strategy 2: Look for action + subject patterns ("explaining X", "analyzing X")
    val actionVerbs = listOf(
      "explain", "analyz", "discuss", "examin", "explor", "compar",
      "describ", "outlin", "review", "break", "cover"
    )
    for (verb in actionVerbs) {
      Regex(
        "\\b${verb}\\w*\\s+(?:the\\s+)?(.+?)(?:[.,!?]|\\s+(?:and|but|however|which)|$)",
        RegexOption.IGNORE_CASE
      ).find(recentMessage)?.let {
        val subject = it.groupValues[1].trim().take(50)
        if (subject.length > 3) return "Elaborating on ${subject.lowercase()}"
      }
    }

    // Strategy 3: Look for sentence subjects (fallback)
    // Extract first meaningful noun phrase from the response
    val sentences = recentMessage.split(Regex("[.!?]"))
    if (sentences.isNotEmpty()) {
      val firstSentence = sentences[0].trim()
      // Remove common prefixes
      val cleaned = firstSentence
        .replace(Regex("^(Sure|Certainly|Of course|Let me|I'll|I will|I would|I can)\\s+", RegexOption.IGNORE_CASE), "")
        .replace(Regex("^(explain|analyze|discuss|describe)\\s+", RegexOption.IGNORE_CASE), "")
        .take(60)

      if (cleaned.length > 10) {
        return "Continuing on ${cleaned.lowercase()}..."
      }
    }

    // If all strategies fail, return generic elaboration message
    return "Elaborating on previous topic"
  }

  // ── Parsing ──────────────────────────────────────────────────────────────

  private fun parsePromptComponents(userPrompt: String): PromptComponents {
    val prompt = userPrompt.trim()
    val lower = prompt.lowercase()

    // 1. Intents
    val intents = mutableListOf<String>()
    if (lower.containsWord("write|create|generate|draft|compose|make|produce")) intents.add("write")
    if (lower.containsWord("analy[sz]e|examine|evaluate|assess") ||
        lower.contains(Regex("\\bdo\\s+(?:a|an)\\s+analysis\\b"))) intents.add("analyze")
    if (lower.containsWord("compare|contrast")) intents.add("compare")
    if (lower.containsWord("explain|describe|clarify|break\\s*down")) intents.add("explain")
    if (lower.containsWord("summari[sz]e|recap")) intents.add("summarize")
    if (lower.containsWord("review|investigate|explore|research")) intents.add("explore")
    if (lower.containsWord("discuss|outline")) intents.add("discuss")
    if (intents.isEmpty()) intents.add("general")

    // 2. Content type
    val contentType = CONTENT_TYPES.firstOrNull { type ->
      lower.contains(Regex("\\b${Regex.escape(type)}\\b"))
    }

    // 3. Modifiers
    val modifiers = mutableListOf<String>()
    if (lower.containsWord("phd")) modifiers.add("PhD-level")
    else if (lower.contains("master")) modifiers.add("master's-level")
    else if (lower.containsWord("academic|scholarly")) modifiers.add("academic")

    Regex("(\\d+)\\s*(?:page|pg)").find(lower)?.let {
      modifiers.add("${it.groupValues[1]}-page")
    }

    Regex("\\b(comprehensive|detailed|thorough|extensive|in-depth)\\b").find(lower)?.let {
      modifiers.add(it.groupValues[1])
    }

    // 4. Comparison target
    val compareTarget = extractCompareTarget(prompt)

    // 5. "between X and Y"
    val betweenSubject = extractBetweenSubject(prompt)

    // 6. Primary subject
    val subject = extractSubject(prompt, contentType, betweenSubject)

    return PromptComponents(intents, contentType, modifiers, subject, compareTarget, betweenSubject)
  }

  private fun extractCompareTarget(prompt: String): String? {
    // Direct: "compare to/with X"
    Regex(
      "(?:compare|contrast)\\s+(?:it\\s+|them\\s+)?(?:to|with|against|versus|vs\\.?)\\s+(.+?)(?:[.?!]|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let { return it.groupValues[1].trim() }

    // Trailing: "and compare to X"
    Regex(
      "and\\s+(?:compare|contrast)\\s+(?:it\\s+|them\\s+)?(?:to|with|against)?\\s*(.+?)(?:[.?!]|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let {
      val raw = it.groupValues[1].trim()
      if (raw.length > 1) return raw
    }

    return null
  }

  private fun extractBetweenSubject(prompt: String): String? {
    Regex(
      "\\bbetween\\s+(.+?)\\s+and\\s+(.+?)(?:[.?!,]|\\s+and\\s+(?:compare|contrast)|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let {
      return "${it.groupValues[1].trim()} vs. ${it.groupValues[2].trim()}"
    }
    return null
  }

  /**
   * Extracts the primary subject using multiple strategies (first match wins).
   *
   * KEY INSIGHT: "on" is only a reliable topic separator AFTER a content type.
   *   "thesis on global warming" → ✓ "on" separates content type from topic
   *   "impact of AI on jobs"     → ✗ "on" is part of the subject phrase
   * So "on" is only matched after a content type. "about/regarding/concerning"
   * are always safe as standalone topic separators.
   */
  private fun extractSubject(
    prompt: String,
    contentType: String?,
    betweenSubject: String?
  ): String? {
    val comparisonStop = "\\s+and\\s+(?:compare|contrast|discuss|explain|analy[sz]e)"

    // Strategy A: "about/regarding/concerning [SUBJECT]" (always safe)
    Regex(
      "(?:\\babout\\b|\\bregarding\\b|\\bconcerning\\b)\\s+(.+?)(?:$comparisonStop|[.?!]|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let {
      val raw = it.groupValues[1].trim().removeLeadingArticle()
      if (raw.length > 1) return raw
    }

    // Strategy B: "[content_type] on [SUBJECT]" — "on" only after content type
    if (contentType != null) {
      Regex(
        "\\b${Regex.escape(contentType)}\\b.*?\\bon\\b\\s+(.+?)(?:$comparisonStop|[.?!]|$)",
        RegexOption.IGNORE_CASE
      ).find(prompt)?.let {
        val raw = it.groupValues[1].trim().removeLeadingArticle()
        if (raw.length > 1) return raw
      }
    }

    // Strategy C: "[content_type] of/for [SUBJECT]"
    if (contentType != null) {
      Regex(
        "\\b${Regex.escape(contentType)}\\s+(?:of|for)\\s+(.+?)(?:$comparisonStop|[.?!]|$)",
        RegexOption.IGNORE_CASE
      ).find(prompt)?.let {
        val raw = it.groupValues[1].trim().removeLeadingArticle()
        if (raw.length > 1) return raw
      }
    }

    // Strategy D: Pre-parsed "between X and Y"
    if (betweenSubject != null) return betweenSubject

    // Strategy E: "pros and cons of [SUBJECT]"
    Regex(
      "pros\\s+and\\s+cons\\s+of\\s+(.+?)(?:$comparisonStop|[.?!]|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let {
      return it.groupValues[1].trim()
    }

    // Strategy F: Direct object after verb (fallback for "Explain quantum physics")
    val allVerbPattern = (ACTION_VERBS + ANALYSIS_VERBS + listOf("tell"))
      .joinToString("|") { Regex.escape(it) }
    Regex(
      "\\b(?:$allVerbPattern)\\s+(?:me\\s+)?(?:a\\s+|an\\s+|the\\s+)?(.+?)(?:$comparisonStop|[.?!]|$)",
      RegexOption.IGNORE_CASE
    ).find(prompt)?.let {
      var raw = it.groupValues[1].trim()
      // Strip modifiers and content type words (already captured separately)
      listOf("comprehensive", "detailed", "thorough", "extensive", "in-depth").forEach { mod ->
        raw = raw.replace(Regex("\\b$mod\\s+", RegexOption.IGNORE_CASE), "")
      }
      if (contentType != null) {
        raw = raw.replace(
          Regex("\\b${Regex.escape(contentType)}\\s*(?:of|on|about|for|regarding|between)?\\s*", RegexOption.IGNORE_CASE), ""
        )
      }
      raw = raw.removeLeadingArticle().trim()
      if (raw.length > 1) return raw
    }

    return null
  }

  // ── Summary Generation ───────────────────────────────────────────────────

  private fun generateTopicSummary(c: PromptComponents): String {
    val primaryIntent = pickPrimaryIntent(c)
    val gerund = intentToGerund(primaryIntent, c.contentType)

    // Skip content type when redundant with verb ("Analyzing analysis" → "Analyzing")
    val redundantPairs = mapOf(
      "analyze" to listOf("analysis"),
      "compare" to listOf("comparison"),
      "summarize" to listOf("summary"),
      "explore" to listOf("review")
    )
    val skipContentType = c.contentType in (redundantPairs[primaryIntent] ?: emptyList()) ||
        (c.contentType == "comparison" && c.betweenSubject != null)

    // Build parts
    val parts = mutableListOf<String>()

    // Content type with modifiers: "PhD-level 5-page thesis"
    if (c.contentType != null && !skipContentType) {
      val modStr = c.modifiers.joinToString(" ")
      parts.add(if (modStr.isNotEmpty()) "$modStr ${c.contentType}" else c.contentType)
    }

    // Subject
    c.subject?.let { subject ->
      val useConnector = parts.isNotEmpty() && "vs." !in subject
      parts.add(if (useConnector) "on ${subject.take(50)}" else subject.take(50))
    }

    // Comparison target (only if not already in betweenSubject)
    if (c.compareTarget != null && c.betweenSubject == null) {
      parts.add("vs. ${c.compareTarget.take(30)}")
    }

    // Assemble
    val body = parts.joinToString(" ").trim()
    var result = when {
      body.isNotEmpty() -> "$gerund $body"
      c.modifiers.isNotEmpty() -> "$gerund ${c.modifiers.joinToString(" ")} response"
      else -> "$gerund your request"
    }

    // Cleanup
    result = result.replace(Regex("\\s+"), " ").trim()
    if (result.length > MAX_TOPIC_LENGTH) {
      result = result.take(MAX_TOPIC_LENGTH - 3) + "..."
    }

    return result
  }

  private fun pickPrimaryIntent(c: PromptComponents): String {
    // Priority: most specific analytical intent first
    val priority = listOf("analyze", "explain", "discuss", "explore", "summarize", "write", "compare", "general")
    for (p in priority) {
      if (p in c.intents) {
        // Skip "compare" when expressed as "vs." suffix
        if (p == "compare" && c.compareTarget != null && c.intents.size > 1) continue
        return p
      }
    }
    // Promote to "compare" when content type implies it
    if (c.contentType == "comparison") return "compare"
    return "general"
  }

  private fun intentToGerund(intent: String, contentType: String?): String {
    // Promote "comparison" content type to compare gerund
    val effective = if (contentType == "comparison" && intent !in listOf("compare", "analyze", "explain")) {
      "compare"
    } else intent

    return when (effective) {
      "write" -> "Writing"
      "analyze" -> "Analyzing"
      "compare" -> "Comparing"
      "explain" -> "Explaining"
      "summarize" -> "Summarizing"
      "explore" -> "Exploring"
      "discuss" -> "Discussing"
      else -> "Working on"
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private fun String.containsWord(pattern: String): Boolean =
    this.contains(Regex("\\b($pattern)\\b"))

  private fun String.removeLeadingArticle(): String =
    this.replace(Regex("^(?:the|a|an)\\s+", RegexOption.IGNORE_CASE), "")

  // ═══════════════════════════════════════════════════════════════════════════
  // Streaming Helpers
  // ═══════════════════════════════════════════════════════════════════════════

  fun extractFirstSentence(text: String): String {
    val sentenceEnd = Regex("(?<=[.!?])\\s")
    val firstSentence = text.split(sentenceEnd).firstOrNull()?.trim() ?: text
    return if (firstSentence.length > 150) {
      val truncated = firstSentence.take(150)
      val lastSpace = truncated.lastIndexOf(' ')
      if (lastSpace > 100) truncated.take(lastSpace) + "..." else truncated + "..."
    } else {
      firstSentence
    }
  }

  fun extractTopic(text: String): String {
    Regex(
      "(?:create|write|develop|provide|analyze|examine)\\s+(?:a |an )?(.+?)\\s+(?:on|about)\\s+(.+?)(?:\\.|,|$)",
      RegexOption.IGNORE_CASE
    ).find(text)?.let {
      return "Creating ${it.groupValues[1].trim()} on ${it.groupValues[2].trim().take(50)}"
    }

    Regex(
      "(comprehensive|detailed|thorough)\\s+(.+?)\\s+(?:on|about)\\s+(.+?)(?:\\.|,|$)",
      RegexOption.IGNORE_CASE
    ).find(text)?.let {
      return "Creating ${it.groupValues[2].trim()} on ${it.groupValues[3].trim().take(50)}"
    }

    return "Creating " + text.take(60).trim() + "..."
  }
}
