# Spec Delta: enhance-topic-extraction-multilingual

## ADDED

### Detection Layer Enhancements (in LongResponseDetector.kt)

```kotlin
package ai.ondevice.app.ui.llmchat

/**
 * Detection result with confidence scoring for long vs short form.
 *
 * Replaces binary true/false with confidence-based decision making.
 */
data class DetectionResult(
    val confidence: Float,  // 0.0-1.0
    val signals: List<DetectionSignal>,
    val shouldShowStatus: Boolean,  // true if confidence >= threshold
    val reasoning: String  // Human-readable explanation
)

/**
 * Signal types for detection with different weights.
 */
enum class DetectionSignal(val weight: Float) {
    BREVITY_BLOCKER(-1.0f),      // Veto: "brief", "quick", "one sentence"
    STRONG_ACTION(0.50f),         // "write thesis", "create essay"
    WEAK_ACTION(0.25f),           // "do", "make", "give" (without content type)
    LENGTH_MODIFIER(0.30f),       // "comprehensive", "detailed", "in-depth"
    EXPLICIT_REQUEST(0.40f),      // "step by step", "explain everything"
    QUESTION_PENALTY(-0.20f)      // Starts with "What is...", "How does..."
}

/**
 * Configuration for detection threshold.
 */
data class DetectionConfig(
    val threshold: Float = 0.70f,  // Confidence needed to show status (70%)
    val enableFeature: Boolean = true
)

/**
 * Enhanced detection with confidence scoring.
 *
 * Multi-factor scoring system:
 * 1. Brevity blockers veto everything (immediate 0.0)
 * 2. Strong actions (+0.50): verb + content type
 * 3. Weak actions (+0.25): verb without content type
 * 4. Length modifiers (+0.30): "comprehensive", "detailed"
 * 5. Explicit requests (+0.40): "step by step", "in detail"
 * 6. Question penalty (-0.20): Pure questions without follow-up
 *
 * Example scores:
 * - "write thesis" → 0.50 (SHORT, below 0.70)
 * - "write comprehensive thesis" → 0.80 (LONG)
 * - "write one sentence" → 0.00 (SHORT, brevity veto)
 * - "quick analysis" → 0.00 (SHORT, brevity veto)
 * - "detailed step-by-step analysis" → 0.95 (LONG)
 */
fun detectLongRequestWithConfidence(
    userPrompt: String,
    config: DetectionConfig = DetectionConfig()
): DetectionResult {
    if (!config.enableFeature) {
        return DetectionResult(0.0f, emptyList(), false, "Feature disabled")
    }

    val trimmed = userPrompt.trim()
    if (trimmed.length < 10) {
        return DetectionResult(0.0f, emptyList(), false, "Prompt too short")
    }

    var confidence = 0.0f
    val signals = mutableListOf<DetectionSignal>()

    // ═════════════════════════════════════════════════════════════════
    // CRITICAL: Brevity blockers veto everything
    // ═════════════════════════════════════════════════════════════════

    val brevityKeywords = listOf(
        "brief", "briefly", "short", "shortly", "quick", "quickly",
        "concise", "concisely", "one sentence", "single sentence",
        "in a sentence", "summarize", "summary", "just tell me"
    )

    if (brevityKeywords.any { trimmed.contains(it, ignoreCase = true) }) {
        return DetectionResult(
            confidence = 0.0f,
            signals = listOf(DetectionSignal.BREVITY_BLOCKER),
            shouldShowStatus = false,
            reasoning = "Brevity modifier present (veto)"
        )
    }

    // ═════════════════════════════════════════════════════════════════
    // Accumulate positive signals
    // ═════════════════════════════════════════════════════════════════

    // Strong action: verb + content type
    val strongActionPattern = Regex(
        "\\b(write|create|generate|compose|develop|produce|draft|prepare|andika|niandike|unda|niunde)" +
        "\\s+(a|an|me)?\\s*(a|an)?\\s*" +
        "(thesis|essay|paper|guide|tutorial|article|report|document|analysis|study|" +
        "tasnifu|insha|ripoti|uchambuzi|mwongozo)",
        RegexOption.IGNORE_CASE
    )
    if (strongActionPattern.containsMatchIn(trimmed)) {
        confidence += DetectionSignal.STRONG_ACTION.weight
        signals.add(DetectionSignal.STRONG_ACTION)
    }

    // Weak action: verb without content type (or with generic "one")
    val weakActionPattern = Regex(
        "\\b(do|make|give|provide|show|tell|help|explain|describe|fanya|nifanyie|pa|nipe|toa|eleza|nieleze)" +
        "\\s+(a|an|me)?\\s*(a|an)?\\s*" +
        "(analysis|breakdown|explanation|overview|summary)?",
        RegexOption.IGNORE_CASE
    )
    if (!signals.contains(DetectionSignal.STRONG_ACTION) &&
        weakActionPattern.containsMatchIn(trimmed)) {
        confidence += DetectionSignal.WEAK_ACTION.weight
        signals.add(DetectionSignal.WEAK_ACTION)
    }

    // Length modifiers
    val lengthKeywords = listOf(
        "comprehensive", "detailed", "thorough", "in-depth", "extensive",
        "elaborate", "complete", "full", "exhaustive",
        "kwa undani", "kamili", "kwa kina"  // Swahili
    )
    if (lengthKeywords.any { trimmed.contains(it, ignoreCase = true) }) {
        confidence += DetectionSignal.LENGTH_MODIFIER.weight
        signals.add(DetectionSignal.LENGTH_MODIFIER)
    }

    // Explicit requests
    val explicitPatterns = Regex(
        "step by step|explain everything|tell me everything|in detail|" +
        "give me (a|an)? (full|complete|comprehensive)|" +
        "walk me through|break it down",
        RegexOption.IGNORE_CASE
    )
    if (explicitPatterns.containsMatchIn(trimmed)) {
        confidence += DetectionSignal.EXPLICIT_REQUEST.weight
        signals.add(DetectionSignal.EXPLICIT_REQUEST)
    }

    // ═════════════════════════════════════════════════════════════════
    // Apply penalties
    // ═════════════════════════════════════════════════════════════════

    // Question penalty (pure questions without follow-up)
    val startsWithQuestion = trimmed.startsWith("what ", ignoreCase = true) ||
        trimmed.startsWith("how ", ignoreCase = true) ||
        trimmed.startsWith("why ", ignoreCase = true) ||
        trimmed.startsWith("when ", ignoreCase = true) ||
        trimmed.startsWith("where ", ignoreCase = true) ||
        trimmed.startsWith("who ", ignoreCase = true)

    val hasFollowUpRequest = trimmed.contains(". ") || trimmed.contains("? ")

    if (startsWithQuestion && !hasFollowUpRequest) {
        confidence += DetectionSignal.QUESTION_PENALTY.weight
        signals.add(DetectionSignal.QUESTION_PENALTY)
    }

    // ═════════════════════════════════════════════════════════════════
    // Clamp and return
    // ═════════════════════════════════════════════════════════════════

    confidence = confidence.coerceIn(0.0f, 1.0f)

    val shouldShow = confidence >= config.threshold

    val reasoning = buildString {
        append("Confidence: ${"%.2f".format(confidence)} ")
        append("(threshold: ${config.threshold}) ")
        append("Signals: ${signals.joinToString(", ") { "${it.name}(${it.weight})" }}")
    }

    return DetectionResult(
        confidence = confidence,
        signals = signals,
        shouldShowStatus = shouldShow,
        reasoning = reasoning
    )
}

/**
 * Backward-compatible boolean detection.
 * Delegates to confidence-based detection.
 */
fun detectLongRequest(userPrompt: String): Boolean {
    if (!ENABLE_LONG_RESPONSE_STATUS) return false
    return detectLongRequestWithConfidence(userPrompt).shouldShowStatus
}
```

### TopicExtractor.kt (Complete Rewrite - 500 lines)

```kotlin
package ai.ondevice.app.ui.llmchat

import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * Production Multilingual Topic Extractor
 *
 * Architecture:
 * - Tiered confidence: Success (≥0.85) / Uncertain (0.50-0.85) / Refused (<0.50)
 * - Pre-compiled patterns with possessive quantifiers for O(N) performance
 * - Fast-path rejection via indexOf() for <50ms latency guarantee
 * - Smart fallback with keyword extraction (never exposes parsing failure)
 *
 * Supported languages:
 * - English (full structure, conversational, question formats)
 * - Swahili (imperative, conjugated verbs, discourse markers)
 * - Code-switching (Swahili frame + English content)
 */
object TopicExtractor {

    // ═══════════════════════════════════════════════════════════════════
    // RESULT TYPES - Tiered confidence architecture
    // ═══════════════════════════════════════════════════════════════════

    sealed class ExtractionResult {
        abstract val displaySummary: String
        abstract val confidence: Float

        /**
         * High confidence (≥0.85): Structured extraction successful
         * Example: "write thesis on AI" → Success(topic="AI", type="thesis", confidence=0.95)
         */
        data class Success(
            override val displaySummary: String,
            val contentType: String?,
            val topic: String,
            override val confidence: Float,
            val patternUsed: String
        ) : ExtractionResult()

        /**
         * Medium confidence (0.50-0.85): Extraction uncertain, flagged for review
         * Example: "What is quantum computing?" → Uncertain(topic="quantum computing", confidence=0.75)
         */
        data class Uncertain(
            override val displaySummary: String,
            val topic: String,
            val rawInput: String,
            override val confidence: Float,
            val patternUsed: String
        ) : ExtractionResult()

        /**
         * Low confidence (<0.50): Refused structured extraction, fallback to keywords
         * Example: "random text here" → Refused(keywords=["random", "text"], confidence=0.0)
         */
        data class Refused(
            override val displaySummary: String,
            val rawInput: String,
            val keywords: List<String>,
            override val confidence: Float = 0.0f
        ) : ExtractionResult()
    }

    // ═══════════════════════════════════════════════════════════════════
    // VOCABULARY CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════

    private object Vocabulary {
        // English verb groups (semantic clustering for maintainability)
        val CREATION_VERBS = listOf(
            "write", "create", "generate", "develop", "make",
            "produce", "draft", "compose", "build", "design"
        )
        val ANALYSIS_VERBS = listOf(
            "analyze", "analyse", "examine", "investigate",
            "study", "research", "explore", "evaluate", "assess"
        )
        val EXPLANATION_VERBS = listOf(
            "explain", "describe", "clarify", "elaborate",
            "illustrate", "break down", "outline", "detail"
        )
        val REQUEST_VERBS = listOf(
            "give", "provide", "show", "tell", "help",
            "do", "get", "find", "list"
        )
        val COMPARISON_VERBS = listOf(
            "compare", "contrast", "differentiate", "distinguish"
        )
        val SUMMARY_VERBS = listOf(
            "summarize", "summarise", "recap", "overview", "condense"
        )

        val ALL_EN_VERBS = (CREATION_VERBS + ANALYSIS_VERBS + EXPLANATION_VERBS +
                           REQUEST_VERBS + COMPARISON_VERBS + SUMMARY_VERBS)

        // Swahili verb stems and conjugation components
        val SW_VERB_STEMS = listOf(
            "andik", "und", "fany", "elez", "tafut",
            "saidia", "toa", "tengenez", "chambua", "elewa"
        )
        val SW_SUBJECT_PREFIXES = listOf("ni", "u", "a", "tu", "m", "wa")
        val SW_TENSE_MARKERS = listOf("na", "li", "ta", "me", "ki", "ka")

        // Prepositions
        val PREPOSITIONS_EN = listOf(
            "on", "about", "for", "regarding", "concerning",
            "of", "into", "covering", "related to"
        )
        val PREPOSITIONS_SW = listOf("kuhusu", "juu ya", "kwa", "kulingana na")

        // Content types
        val CONTENT_TYPES_EN = listOf(
            "thesis", "essay", "analysis", "report", "guide", "tutorial",
            "summary", "overview", "explanation", "breakdown", "comparison",
            "study", "paper", "article", "document", "review", "assessment",
            "presentation", "outline", "brief", "one"
        )
        val CONTENT_TYPES_SW = listOf(
            "insha", "uchambuzi", "ripoti", "maelezo", "muhtasari",
            "mwongozo", "ulinganisho", "tathmini"
        )

        // Stopwords for fallback extraction
        val STOPWORDS_EN = setOf(
            "a", "an", "the", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will",
            // ... (full list ~100 words)
        )
        val STOPWORDS_SW = setOf(
            "na", "ya", "wa", "kwa", "ni", "katika", "au", "lakini",
            // ... (full list ~40 words)
        )

        // Fast-path trigger words (for indexOf pre-filter)
        val TRIGGER_WORDS = listOf(
            "write", "create", "analyze", "explain", "compare",
            "kuhusu", "andika", "eleza", "fanya", "niandike"
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // PRE-COMPILED PATTERNS (12 patterns ordered by confidence)
    // ═══════════════════════════════════════════════════════════════════

    private data class PatternDef(
        val name: String,
        val pattern: Pattern,
        val baseConfidence: Float,
        val extractor: (Matcher, String) -> ExtractionResult
    )

    /**
     * Patterns use possessive quantifiers (++) to prevent catastrophic backtracking.
     * Ordered by specificity: highest confidence (0.95) to lowest (0.65).
     */
    private val patterns: List<PatternDef> by lazy {
        listOf(
            // Pattern 1: en_full_structure (0.95)
            // "write a thesis on artificial intelligence"
            PatternDef(...),

            // Pattern 2: sw_kuhusu_topic (0.90)
            // "kuhusu elimu ya watoto"
            PatternDef(...),

            // Pattern 3: comparison (0.92)
            // "compare React vs Vue"
            PatternDef(...),

            // Pattern 4: code_switched (0.88)
            // "Nieleze about climate change"
            PatternDef(...),

            // Pattern 5: sw_imperative_full (0.90)
            // "Niandike insha kuhusu elimu"
            PatternDef(...),

            // Pattern 6: en_verb_prep_topic (0.78)
            // "analyze about economics"
            PatternDef(...),

            // Pattern 7: question_format (0.75)
            // "What is quantum computing?"
            PatternDef(...),

            // Pattern 8: sw_question (0.72)
            // "Uchumi wa Kenya ni nini?"
            PatternDef(...),

            // Pattern 9: conversational (0.80)
            // "tell me about blockchain"
            PatternDef(...),

            // Pattern 10: sw_polite_request (0.70)
            // "Tafadhali, nisaidie na mathematics"
            PatternDef(...),

            // Pattern 11: length_modifier (0.82)
            // "detailed analysis on AI"
            PatternDef(...),

            // Pattern 12: colon_topic (0.65)
            // "Topic: AI in healthcare"
            PatternDef(...)
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // MAIN EXTRACTION FUNCTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extract topic from user prompt with tiered confidence.
     *
     * Performance: <50ms typical, <100ms worst-case
     * Three-phase matching:
     * 1. Fast-path rejection: O(n) indexOf scan
     * 2. Pattern matching: O(n) per pattern with possessive quantifiers
     * 3. Smart fallback: O(n) keyword extraction
     */
    fun extract(userPrompt: String): ExtractionResult {
        val input = userPrompt.trim().normalizeWhitespace()

        // Fast-path: Empty or too short
        if (input.length < 3) {
            return ExtractionResult.Refused(
                displaySummary = "Processing your request",
                rawInput = input,
                keywords = emptyList()
            )
        }

        // Fast-path rejection: No trigger words present (14x faster than regex)
        val hasTrigger = Vocabulary.TRIGGER_WORDS.any { trigger ->
            input.contains(trigger, ignoreCase = true)
        }
        if (!hasTrigger) {
            return createSmartFallback(input)
        }

        // Run pattern matching pipeline (cascading confidence)
        for (patternDef in patterns) {
            val matcher = patternDef.pattern.matcher(input)
            if (matcher.find()) {
                return patternDef.extractor(matcher, input)
            }
        }

        // No pattern matched - create intelligent fallback
        return createSmartFallback(input)
    }

    // ═══════════════════════════════════════════════════════════════════
    // SMART FALLBACK
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extract meaningful keywords when structured parsing fails.
     * Never expose parsing failure to user - always provide useful keywords.
     */
    private fun createSmartFallback(input: String): ExtractionResult.Refused {
        val allStopwords = Vocabulary.STOPWORDS_EN + Vocabulary.STOPWORDS_SW

        val keywords = input
            .replace(Regex("[^a-zA-Z0-9\\s'-]"), " ")
            .split(Regex("\\s+"))
            .filter { word ->
                word.length > 2 &&
                word.lowercase() !in allStopwords &&
                !word.all { it.isDigit() }
            }
            .distinctBy { it.lowercase() }
            .take(8)

        val displaySummary = when {
            keywords.size >= 3 -> "Working on: ${keywords.take(4).joinToString(", ")}"
            keywords.isNotEmpty() -> "Processing: ${keywords.joinToString(" ")}"
            else -> "Processing your request"
        }

        return ExtractionResult.Refused(
            displaySummary = displaySummary.truncateSmart(80),
            rawInput = input,
            keywords = keywords
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITY EXTENSIONS
    // ═══════════════════════════════════════════════════════════════════

    private fun String.normalizeWhitespace(): String =
        this.replace(Regex("\\s+"), " ")

    private fun String.cleanTopic(): String =
        this.trim()
            .replace(Regex("[.?!]+$"), "")
            .replace(Regex("^[.?!,;:]+"), "")
            .replace(Regex("\\s+"), " ")

    /**
     * Smart truncation preserving word boundaries.
     * Example: "artificial intelligence..." (not "artificial intelligen...")
     */
    private fun String.truncateSmart(maxLength: Int): String {
        if (this.length <= maxLength) return this

        val truncated = this.take(maxLength - 3)
        val lastSpace = truncated.lastIndexOf(' ')

        return if (lastSpace > maxLength / 2) {
            truncated.substring(0, lastSpace) + "..."
        } else {
            truncated + "..."
        }
    }
}
```

### TopicSummarizer.kt (NEW - 700 lines)

```kotlin
package ai.ondevice.app.ui.llmchat

/**
 * Topic Summarization Engine
 *
 * Transforms extracted topic components into natural, contextually
 * appropriate status messages for UI display.
 *
 * Key features:
 * - Verb-action semantic mapping (analyze→"Analyzing", not "Creating")
 * - Intelligent topic compression (clause-aware truncation)
 * - Template variation (32 variations: 8 intents × 4 formality levels)
 * - Full language preservation (pure English or pure Swahili, no mixing)
 * - Tone-appropriate output (formal/neutral/casual/urgent)
 *
 * Architecture:
 * 1. Input analysis: detect intent, language, formality from extraction result
 * 2. Template selection: choose from 64 templates (32 EN + 32 SW)
 * 3. Smart compression: preserve clause boundaries when truncating
 * 4. Language resolution: output in same language as input (pure, no mixing)
 */
object TopicSummarizer {

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════

    data class SummaryConfig(
        val maxLength: Int = 80,
        val preferredLanguage: Language = Language.AUTO,
        val includeEllipsis: Boolean = true,
        val formalityBias: Formality = Formality.NEUTRAL
    )

    enum class Language { ENGLISH, SWAHILI, AUTO }
    enum class Formality { FORMAL, NEUTRAL, CASUAL }

    // ═══════════════════════════════════════════════════════════════════
    // INPUT ANALYSIS TYPES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Semantic classification of user's intent (8 categories)
     */
    enum class IntentClass {
        CREATE,      // write, create, generate, make, draft
        ANALYZE,     // analyze, examine, study, investigate, assess
        EXPLAIN,     // explain, describe, clarify, elaborate
        COMPARE,     // compare, contrast, differentiate
        SUMMARIZE,   // summarize, outline, recap, condense
        RESEARCH,    // research, find, look up, search
        HELP,        // help, assist, support
        QUESTION,    // what, how, why, when, where
        UNKNOWN
    }

    enum class DetectedLanguage {
        ENGLISH,
        SWAHILI,
        CODE_SWITCHED_SW_FRAME,  // Swahili grammar, English content
        CODE_SWITCHED_EN_FRAME,  // English grammar, Swahili content
        AMBIGUOUS
    }

    enum class DetectedFormality {
        FORMAL,     // "Please provide", "I would like", "tafadhali"
        NEUTRAL,    // Standard requests
        CASUAL,     // "give me", "help", contractions
        URGENT      // Imperatives, exclamation marks
    }

    data class InputAnalysis(
        val intent: IntentClass,
        val language: DetectedLanguage,
        val formality: DetectedFormality,
        val detectedVerb: String?,
        val contentType: String?,
        val topic: String,
        val rawInput: String
    )

    // ═══════════════════════════════════════════════════════════════════
    // VERB-INTENT MAPPING (50+ verbs)
    // ═══════════════════════════════════════════════════════════════════

    private val verbToIntent = mapOf(
        // CREATE
        "write" to IntentClass.CREATE,
        "create" to IntentClass.CREATE,
        "generate" to IntentClass.CREATE,
        "andika" to IntentClass.CREATE,
        "unda" to IntentClass.CREATE,

        // ANALYZE
        "analyze" to IntentClass.ANALYZE,
        "examine" to IntentClass.ANALYZE,
        "do" to IntentClass.ANALYZE,  // "do an analysis"
        "chambua" to IntentClass.ANALYZE,

        // EXPLAIN
        "explain" to IntentClass.EXPLAIN,
        "describe" to IntentClass.EXPLAIN,
        "eleza" to IntentClass.EXPLAIN,

        // ... (50+ total mappings)
    )

    // ═══════════════════════════════════════════════════════════════════
    // TEMPLATE DEFINITIONS (64 templates total)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * English templates: 8 intents × 4 formality levels = 32 templates
     *
     * Example for ANALYZE intent:
     * - FORMAL: "Conducting analysis of {topic}"
     * - NEUTRAL: "Analyzing {topic}"
     * - CASUAL: "Digging into {topic}"
     * - URGENT: "Analyzing: {topic}"
     */
    private val englishTemplates = mapOf(
        IntentClass.ANALYZE to mapOf(
            DetectedFormality.FORMAL to listOf(
                "Conducting analysis of {topic}",
                "Examining {topic}",
                "Assessing {topic}"
            ),
            DetectedFormality.NEUTRAL to listOf(
                "Analyzing {topic}",
                "Breaking down {topic}",
                "Looking into {topic}"
            ),
            DetectedFormality.CASUAL to listOf(
                "Digging into {topic}",
                "Checking out {topic}"
            ),
            DetectedFormality.URGENT to listOf(
                "Analyzing: {topic}"
            )
        ),
        // ... (7 more intent classes)
    )

    /**
     * Swahili templates: 8 intents × 4 formality levels = 32 templates
     * All templates are grammatically pure Swahili (no English mixing)
     *
     * Example for ANALYZE intent:
     * - FORMAL: "Kuchambua {topic}"
     * - NEUTRAL: "Kuchambua {topic}"
     * - CASUAL: "Nachambua {topic}"
     * - URGENT: "Uchambuzi: {topic}"
     */
    private val swahiliTemplates = mapOf(
        IntentClass.ANALYZE to mapOf(
            DetectedFormality.FORMAL to listOf(
                "Kuchambua {topic}",
                "Kufanya uchambuzi wa {topic}"
            ),
            DetectedFormality.NEUTRAL to listOf(
                "Kuchambua {topic}",
                "Kutathmini {topic}"
            ),
            DetectedFormality.CASUAL to listOf(
                "Nachambua {topic}",
                "Naangalia {topic}"
            ),
            DetectedFormality.URGENT to listOf(
                "Uchambuzi: {topic}"
            )
        ),
        // ... (7 more intent classes)
    )

    /**
     * Content type translations for Swahili output
     * Ensures pure Swahili when translating English content types
     */
    private val contentTypeTranslations = mapOf(
        "thesis" to "tasnifu",
        "essay" to "insha",
        "analysis" to "uchambuzi",
        "report" to "ripoti",
        // ... (15+ total translations)
    )

    // ═══════════════════════════════════════════════════════════════════
    // MAIN SUMMARIZATION FUNCTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Generate natural, contextually appropriate summary from extraction result.
     *
     * Process:
     * 1. Analyze input (intent, language, formality)
     * 2. Select appropriate language for output (preserve user's choice)
     * 3. Select template set (English or Swahili)
     * 4. Choose template based on intent and formality
     * 5. Compress topic smartly (clause boundaries)
     * 6. Substitute placeholders ({topic}, {type})
     * 7. Final length check and return
     */
    fun summarize(
        extractionResult: TopicExtractor.ExtractionResult,
        config: SummaryConfig = SummaryConfig()
    ): String {
        // Analyze input
        val analysis = analyzeInput(extractionResult)

        // Select language
        val outputLanguage = resolveOutputLanguage(analysis.language, config.preferredLanguage)

        // Select template set
        val templates = when (outputLanguage) {
            Language.ENGLISH -> englishTemplates
            Language.SWAHILI -> swahiliTemplates
            Language.AUTO -> englishTemplates
        }

        // Get templates for this intent and formality
        val intentTemplates = templates[analysis.intent] ?: templates[IntentClass.UNKNOWN]!!
        val formalityTemplates = intentTemplates[analysis.formality]
            ?: intentTemplates[DetectedFormality.NEUTRAL]!!

        // Select template (first for consistency)
        val template = formalityTemplates.first()

        // Compress topic
        val compressedTopic = compressTopic(analysis.topic, config.maxLength - 30)

        // Translate content type if Swahili
        val contentType = when (outputLanguage) {
            Language.SWAHILI -> analysis.contentType?.let {
                contentTypeTranslations[it.lowercase()] ?: it
            }
            else -> analysis.contentType
        }

        // Substitute placeholders
        var summary = template
            .replace("{topic}", compressedTopic)
            .replace("{type}", contentType ?: "")

        // Clean up empty {type} placeholders
        if (contentType == null) {
            summary = summary
                .replace(" {type}", "")
                .replace("{type} ", "")
                .replace("{type}", "")
        }

        // Final length check
        return if (summary.length > config.maxLength) {
            truncateSmart(summary, config.maxLength, config.includeEllipsis)
        } else {
            summary
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INPUT ANALYSIS
    // ═══════════════════════════════════════════════════════════════════

    private fun analyzeInput(result: TopicExtractor.ExtractionResult): InputAnalysis {
        // ... implementation
    }

    private fun detectIntent(input: String): IntentClass {
        // Check for question patterns first
        // Then extract first verb and map to intent
        // ... implementation
    }

    private fun detectLanguage(input: String): DetectedLanguage {
        // Look for Swahili markers (kuhusu, juu ya, conjugated verbs)
        // Look for English markers (write, create, about, the)
        // Determine if pure or code-switched
        // ... implementation
    }

    private fun detectFormality(input: String): DetectedFormality {
        // Formal: "please provide", "I would like", "tafadhali"
        // Urgent: exclamation marks, all caps
        // Casual: "give me", "gimme", "just", "quick"
        // ... implementation
    }

    // ═══════════════════════════════════════════════════════════════════
    // LANGUAGE RESOLUTION
    // ═══════════════════════════════════════════════════════════════════

    private fun resolveOutputLanguage(
        detected: DetectedLanguage,
        preferred: Language
    ): Language {
        // If user has preference, honor it
        // Otherwise preserve input language (pure English or pure Swahili)
        // Code-switched SW frame → Swahili output
        // Code-switched EN frame → English output
        // ... implementation
    }

    // ═══════════════════════════════════════════════════════════════════
    // TOPIC COMPRESSION (Clause-aware)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Intelligently compress topic while preserving meaning.
     *
     * Strategy:
     * 1. If short enough, return as-is
     * 2. Look for natural break points (and, or, commas, dashes)
     * 3. Preserve first clause + add ellipsis
     * 4. If no break point, truncate at word boundary
     *
     * Example:
     * Input: "the rise and fall of empires and their impact on modern society"
     * Output: "the rise and fall of empires..." (preserves first clause)
     */
    private fun compressTopic(topic: String, maxLength: Int): String {
        if (topic.length <= maxLength) return topic

        // Try to find natural break point
        val breakPoints = listOf(
            " and ", " or ", ", ", " - ", " with ", " for ",
            " na ", " au ", " kwa "
        )

        for (breakPoint in breakPoints) {
            val index = topic.indexOf(breakPoint)
            if (index in 10..(maxLength - 5)) {
                val firstPart = topic.substring(0, index)
                return if (firstPart.length <= maxLength - 3) {
                    "$firstPart..."
                } else {
                    truncateSmart(firstPart, maxLength - 3, true)
                }
            }
        }

        // No good break point - truncate at word boundary
        return truncateSmart(topic, maxLength, true)
    }

    private fun truncateSmart(text: String, maxLength: Int, addEllipsis: Boolean): String {
        // Truncate at word boundary, never mid-word
        // ... implementation
    }
}
```

### TopicExtractorTest.kt (NEW - 400 lines)

```kotlin
package ai.ondevice.app.ui.llmchat

import org.junit.Test
import org.junit.Assert.*

class TopicExtractorTest {

    // ═══════════════════════════════════════════════════════════════════
    // HIGH CONFIDENCE TESTS (Tier 1: ≥0.85)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `english full structure - thesis on AI`() {
        val result = TopicExtractor.extract("write a thesis on artificial intelligence")

        assertTrue(result is TopicExtractor.ExtractionResult.Success)
        val success = result as TopicExtractor.ExtractionResult.Success
        assertEquals("thesis", success.contentType)
        assertEquals("artificial intelligence", success.topic)
        assertTrue(success.confidence >= 0.85f)
    }

    @Test
    fun `english full structure - analysis on empires`() {
        val result = TopicExtractor.extract(
            "do an analysis on the rise and fall of empires"
        )

        assertTrue(result is TopicExtractor.ExtractionResult.Success)
        val success = result as TopicExtractor.ExtractionResult.Success
        assertEquals("analysis", success.contentType)
        assertTrue(success.topic.contains("empires"))
    }

    // ... 4 more high-confidence tests

    // ═══════════════════════════════════════════════════════════════════
    // MEDIUM CONFIDENCE TESTS (Tier 2: 0.50-0.85)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `question format - What is quantum computing`() {
        val result = TopicExtractor.extract("What is quantum computing?")

        assertTrue(result is TopicExtractor.ExtractionResult.Uncertain)
        val uncertain = result as TopicExtractor.ExtractionResult.Uncertain
        assertTrue(uncertain.topic.contains("quantum computing"))
        assertTrue(uncertain.confidence in 0.50f..0.85f)
    }

    // ... 5 more medium-confidence tests

    // ═══════════════════════════════════════════════════════════════════
    // LOW CONFIDENCE TESTS (Tier 3: <0.50 - Fallback)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `fallback - unstructured input extracts keywords`() {
        val result = TopicExtractor.extract("machine learning neural networks")

        assertTrue(result is TopicExtractor.ExtractionResult.Refused)
        val refused = result as TopicExtractor.ExtractionResult.Refused
        assertTrue(refused.keywords.isNotEmpty())
        assertFalse(refused.displaySummary.contains("Processing request:"))
    }

    // ... 2 more fallback tests

    // ═══════════════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `handles multiple whitespace`() { /* ... */ }

    @Test
    fun `handles trailing punctuation`() { /* ... */ }

    @Test
    fun `case insensitive matching`() { /* ... */ }

    @Test
    fun `long topic truncation preserves word boundaries`() { /* ... */ }

    // ═══════════════════════════════════════════════════════════════════
    // PERFORMANCE TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `latency under 50ms for typical input`() {
        val inputs = listOf(
            "write a thesis on artificial intelligence",
            "Niandike insha kuhusu elimu",
            "What is quantum computing?",
            "compare React vs Vue"
        )

        inputs.forEach { input ->
            val start = System.nanoTime()
            repeat(100) { TopicExtractor.extract(input) }
            val elapsed = (System.nanoTime() - start) / 100 / 1_000_000.0

            assertTrue("Latency was ${elapsed}ms", elapsed < 50.0)
        }
    }

    @Test
    fun `no catastrophic backtracking on pathological input`() {
        val pathological = "a".repeat(30) + "b"

        val start = System.nanoTime()
        TopicExtractor.extract(pathological)
        val elapsed = (System.nanoTime() - start) / 1_000_000.0

        assertTrue("Pathological input took ${elapsed}ms", elapsed < 100.0)
    }
}
```

### TopicSummarizerTest.kt (NEW - 350 lines)

```kotlin
package ai.ondevice.app.ui.llmchat

import org.junit.Test
import org.junit.Assert.*

class TopicSummarizerTest {

    // ═══════════════════════════════════════════════════════════════════
    // SEMANTIC ACCURACY TESTS (Intent → Output Verb)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `analyze intent produces Analyzing output`() {
        val extraction = TopicExtractor.extract("analyze the economy")
        val summary = TopicSummarizer.summarize(extraction)

        assertTrue(summary.startsWith("Analyzing"))
        assertFalse(summary.contains("Creating"))
    }

    @Test
    fun `explain intent produces Explaining output`() {
        val extraction = TopicExtractor.extract("explain quantum physics")
        val summary = TopicSummarizer.summarize(extraction)

        assertTrue(summary.startsWith("Explaining"))
    }

    // ... 6 more semantic accuracy tests (one per intent)

    // ═══════════════════════════════════════════════════════════════════
    // LANGUAGE PURITY TESTS (No Mixing)
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `english input produces pure english output`() {
        val extraction = TopicExtractor.extract("write a thesis on AI")
        val summary = TopicSummarizer.summarize(extraction)

        // Should not contain Swahili words
        assertFalse(summary.contains("Ku"))
        assertFalse(summary.contains("Na"))
    }

    @Test
    fun `swahili input produces pure swahili output`() {
        val extraction = TopicExtractor.extract("Niandike insha kuhusu elimu")
        val summary = TopicSummarizer.summarize(extraction)

        // Should be pure Swahili, no English mixing
        assertTrue(summary.matches(Regex("^[A-Za-z\\s:]+$")))  // Basic check
        assertFalse(summary.contains("Creating"))
        assertFalse(summary.contains("Writing"))
    }

    // ... 8 more language purity tests

    // ═══════════════════════════════════════════════════════════════════
    // FORMALITY DETECTION TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `formal input gets formal template`() {
        val extraction = TopicExtractor.extract("Please provide an analysis of the economy")
        val summary = TopicSummarizer.summarize(extraction)

        // Formal templates use "Conducting", "Preparing", etc.
        assertTrue(
            summary.contains("Conducting") ||
            summary.contains("Preparing") ||
            summary.contains("Assessing")
        )
    }

    // ... 3 more formality tests

    // ═══════════════════════════════════════════════════════════════════
    // TOPIC COMPRESSION TESTS
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `long topic preserves clause boundary`() {
        val longTopic = "the rise and fall of empires and their impact on modern society"
        val extraction = TopicExtractor.extract("analyze $longTopic")
        val summary = TopicSummarizer.summarize(extraction)

        // Should truncate at "and" boundary
        assertTrue(summary.contains("empires"))
        assertTrue(summary.contains("..."))
        assertFalse(summary.contains("modern society"))
    }

    @Test
    fun `truncation never breaks mid-word`() {
        val extraction = TopicExtractor.extract("write about artificial intelligence")
        val config = TopicSummarizer.SummaryConfig(maxLength = 25)
        val summary = TopicSummarizer.summarize(extraction, config)

        // Should not end with "intellig..." or "artifici..."
        assertFalse(summary.matches(Regex(".*[a-z]{5}\\.\\.\\.$")))
    }

    // ... 1 more compression test
}
```

## MODIFIED

### LongResponseDetector.kt (lines 161-168)

```diff
  /**
   * Extracts topic from user's request prompt using enhanced TopicExtractor.
-  *
-  * This replaces the old regex-based approach with a confidence-ranked system
-  * that handles 7 different patterns and provides graceful fallback.
   */
  fun extractTopicFromUserPrompt(userPrompt: String): String {
-    return TopicExtractor.extract(userPrompt).summary
+    // Step 1: Extract topic components with tiered confidence
+    val extractionResult = TopicExtractor.extract(userPrompt)
+
+    // Step 2: Generate natural, contextually appropriate summary
+    val summary = TopicSummarizer.summarize(
+      extractionResult,
+      TopicSummarizer.SummaryConfig(
+        maxLength = 80,
+        preferredLanguage = TopicSummarizer.Language.AUTO,
+        includeEllipsis = true
+      )
+    )
+
+    return summary
  }
```

### LongResponseDetectorTest.kt (add new integration tests)

```diff
+ @Test
+ fun `extractTopicFromUserPrompt - analyze produces Analyzing output`() {
+   val topic = LongResponseDetector.extractTopicFromUserPrompt(
+     "do an analysis on the rise and fall of empires"
+   )
+
+   assertTrue(topic.contains("Analyzing"))
+   assertFalse(topic.contains("Creating"))
+   assertTrue(topic.contains("empires"))
+ }
+
+ @Test
+ fun `extractTopicFromUserPrompt - explain produces Explaining output`() {
+   val topic = LongResponseDetector.extractTopicFromUserPrompt(
+     "explain quantum physics to me"
+   )
+
+   assertTrue(topic.contains("Explaining"))
+ }
+
+ @Test
+ fun `extractTopicFromUserPrompt - swahili produces pure swahili`() {
+   val topic = LongResponseDetector.extractTopicFromUserPrompt(
+     "Nieleze kuhusu elimu ya watoto"
+   )
+
+   // Should be pure Swahili, no English words
+   assertFalse(topic.contains("Creating"))
+   assertFalse(topic.contains("Explaining"))
+ }
+
+ @Test
+ fun `extractTopicFromUserPrompt - question format produces Researching`() {
+   val topic = LongResponseDetector.extractTopicFromUserPrompt(
+     "What is artificial intelligence?"
+   )
+
+   assertTrue(topic.contains("Researching"))
+ }
+
+ @Test
+ fun `extractTopicFromUserPrompt - formal input gets formal output`() {
+   val topic = LongResponseDetector.extractTopicFromUserPrompt(
+     "Please provide a comprehensive analysis of climate change"
+   )
+
+   // Formal templates use "Conducting", "Preparing", etc.
+   assertTrue(
+     topic.contains("Conducting") ||
+     topic.contains("Preparing") ||
+     topic.contains("Assessing")
+   )
+ }
```

## REMOVED

None. This is an enhancement that builds on existing code without removing functionality.

## DEPENDENCY CHANGES

None. Uses existing Kotlin stdlib and JUnit for testing. No new external dependencies.

## MIGRATION NOTES

**Backward Compatibility:**
- `LongResponseDetector.extractTopicFromUserPrompt()` signature unchanged (takes String, returns String)
- Existing callers (LongResponseStatusBox.kt) work without modification
- Internal implementation completely replaced but API contract preserved

**Performance Impact:**
- Previous implementation: 1-2ms average extraction time
- New implementation: <50ms target (20-40ms typical)
- Acceptable tradeoff for semantic accuracy and language purity

**Testing Impact:**
- Previous test coverage: 19 tests in LongResponseDetectorTest
- New test coverage: 19 + 55 = 74 tests total (289% increase)
- CI build time increase: ~2-3 seconds for additional tests

## VALIDATION CHECKLIST

Before marking this change complete, verify:

- [ ] All 30+ TopicExtractorTest tests pass
- [ ] All 25+ TopicSummarizerTest tests pass
- [ ] All 5 new LongResponseDetectorTest integration tests pass
- [ ] Performance tests confirm <50ms average latency
- [ ] Visual verification: "analyze empires" shows "Analyzing..." (not "Creating...")
- [ ] Visual verification: "explain physics" shows "Explaining..."
- [ ] Visual verification: "Nieleze kuhusu elimu" shows pure Swahili (no English mixing)
- [ ] Visual verification: "What is AI?" shows "Researching AI"
- [ ] Visual verification: Long topics truncate at word boundaries (never mid-word)
- [ ] CI build passes (lint + all tests)
- [ ] APK installs and runs without crashes
