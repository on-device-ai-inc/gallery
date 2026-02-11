# Specification Delta - Web Search Fresh (Perplexica Pattern)

## ADDED

### SearchPromptTemplate.kt
```kotlin
package ai.ondevice.app.data

/**
 * Prompt template based on Perplexica (27.7k⭐) pattern for web search.
 * Ensures LLM prioritizes search results over training data.
 *
 * Reference: https://github.com/ItzCrazyKns/Perplexica
 */
object SearchPromptTemplate {
  fun format(
    query: String,
    currentDate: String,
    searchResults: List<SearchResult>
  ): String {
    val resultsSection = searchResults.mapIndexed { i, result ->
      """[${i + 1}] ${result.title}
      ${result.description}
      Source: ${result.url}
      """
    }.joinToString("\n\n")

    return """
<context>
Current date: $currentDate
Web search results for: "$query"

$resultsSection
</context>

<instructions>
CRITICAL SEARCH RESULT USAGE:
1. Your training data is from before $currentDate - the search results are CURRENT
2. ALWAYS prioritize information from the search results over your training data
3. When stating facts from search results, cite them using [1], [2], etc.
4. If search results contradict your training, trust the search results
5. Format dates consistently using the current date as reference
6. Include a "Sources:" section at the end listing all [1], [2] citations with URLs
</instructions>

<user_query>
$query
</user_query>
"""
  }
}
```

### DuckDuckGoClient.kt (Fallback Provider)
```kotlin
package ai.ondevice.app.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * DuckDuckGo search client for fallback when Brave fails.
 * Based on duckduckgo-search library pattern (4.8k⭐).
 */
@Singleton
class DuckDuckGoClient @Inject constructor(
  // TBD: HTTP client implementation
) {
  suspend fun search(query: String, count: Int = 3): Result<List<SearchResult>> {
    // Implementation: Call DuckDuckGo HTML API
    // Parse results to SearchResult data class
    // Return top N results
  }
}
```

### CircuitBreaker.kt (Resilience Pattern)
```kotlin
package ai.ondevice.app.data

/**
 * Circuit breaker for search providers.
 * Pattern from production research: 5-failure threshold, 60s timeout, 3-success recovery.
 */
class CircuitBreaker {
  private val providerStates = mutableMapOf<SearchProvider, ProviderState>()

  enum class State { CLOSED, OPEN, HALF_OPEN }

  data class ProviderState(
    var failureCount: Int = 0,
    var state: State = State.CLOSED,
    var lastFailureTime: Long = 0,
    var successCount: Int = 0
  )

  suspend fun <T> execute(
    provider: SearchProvider,
    block: suspend () -> T
  ): Result<T> {
    val state = providerStates.getOrPut(provider) { ProviderState() }

    if (state.state == State.OPEN) {
      val elapsed = System.currentTimeMillis() - state.lastFailureTime
      if (elapsed < 60_000) { // 60 second timeout
        return Result.failure(CircuitOpenException(provider))
      }
      state.state = State.HALF_OPEN
      state.successCount = 0
    }

    return try {
      val result = block()

      // Success
      state.successCount++
      if (state.state == State.HALF_OPEN && state.successCount >= 3) {
        state.state = State.CLOSED
        state.failureCount = 0
      }
      Result.success(result)

    } catch (e: Exception) {
      state.failureCount++
      state.lastFailureTime = System.currentTimeMillis()

      if (state.failureCount >= 5) { // 5-failure threshold
        state.state = State.OPEN
      }
      Result.failure(e)
    }
  }
}

sealed class SearchProvider {
  object Brave : SearchProvider()
  object DuckDuckGo : SearchProvider()
}

class CircuitOpenException(val provider: SearchProvider) :
  Exception("Circuit breaker open for $provider")

class SearchUnavailableException :
  Exception("All search providers unavailable")
```

### CitationFormatter.kt
```kotlin
package ai.ondevice.app.util

/**
 * Formats LLM responses with citation sources.
 * Based on Perplexica citation pattern.
 */
object CitationFormatter {
  /**
   * Append "Sources:" section to LLM response with citations.
   *
   * @param response LLM generated text with [1], [2] citations
   * @param searchResults Original search results to match citations
   * @return Response with appended sources section
   */
  fun appendSources(
    response: String,
    searchResults: List<SearchResult>
  ): String {
    val citationPattern = "\\[(\\d+)\\]".toRegex()
    val citedIndices = citationPattern.findAll(response)
      .map { it.groupValues[1].toInt() }
      .distinct()
      .sorted()

    if (citedIndices.isEmpty()) {
      return response // No citations found
    }

    val sourcesSection = buildString {
      appendLine()
      appendLine()
      appendLine("Sources:")
      citedIndices.forEach { index ->
        val result = searchResults.getOrNull(index - 1) // 1-indexed
        if (result != null) {
          appendLine("[$index] ${result.title}")
          appendLine("    ${result.url}")
        }
      }
    }

    return response + sourcesSection
  }
}
```

---

## MODIFIED

### SearchRepository.kt
```kotlin
@Singleton
class SearchRepository @Inject constructor(
  private val braveService: BraveSearchService,
  private val duckDuckGoClient: DuckDuckGoClient,        // NEW
  private val circuitBreaker: CircuitBreaker,           // NEW
  private val preferences: WebSearchPreferencesDataStore
) {

  suspend fun search(query: String): Result<String> {
    // Check rate limit (unchanged)
    val canSearch = preferences.canSearch()
    if (!canSearch) {
      return Result.failure(RateLimitException())
    }

    // Try Brave first (PRIMARY)
    val braveResult = circuitBreaker.execute(SearchProvider.Brave) {
      braveService.search(
        query = query.trim(),
        count = 5,  // Increased from 3 to 5
        apiKey = BuildConfig.BRAVE_API_KEY
      )
    }

    if (braveResult.isSuccess) {
      val results = braveResult.getOrNull()?.web?.results?.filterNotNull() ?: emptyList()
      if (results.isNotEmpty()) {
        preferences.incrementDailyCount()
        return Result.success(formatWithTemplate(query, results))
      }
    }

    // Fallback to DuckDuckGo (SECONDARY)
    val ddgResult = circuitBreaker.execute(SearchProvider.DuckDuckGo) {
      duckDuckGoClient.search(query.trim(), count = 5)
    }

    if (ddgResult.isSuccess) {
      val results = ddgResult.getOrNull() ?: emptyList()
      if (results.isNotEmpty()) {
        preferences.incrementDailyCount()
        return Result.success(formatWithTemplate(query, results))
      }
    }

    // All providers failed
    return Result.failure(SearchUnavailableException())
  }

  /**
   * Format results using Perplexica template pattern.
   */
  private fun formatWithTemplate(query: String, results: List<SearchResult>): String {
    val currentDate = java.time.LocalDate.now().toString()
    return SearchPromptTemplate.format(
      query = query,
      currentDate = currentDate,
      searchResults = results.take(5)
    )
  }
}
```

### LlmChatViewModel.kt
```kotlin
// In sendMessage() method:

// Web Search: Use template-based prompt AFTER persona
if (searchRepository != null && webSearchPreferences != null) {
  try {
    val prefs = webSearchPreferences.getPreferences()
    if (prefs.enabled) {
      when (val result = searchRepository.search(input)) {
        is Result.Success -> {
          val searchPrompt = result.getOrNull()
          if (!searchPrompt.isNullOrBlank()) {
            // Append search prompt AFTER persona, BEFORE user message
            enhancedInput = enhancedInput + "\n\n" + searchPrompt

            // Store search results for citation formatting
            _searchResultsForCitation.value = extractSearchResults(searchPrompt)
          }
        }
        is Result.Failure -> {
          // Log failure but continue without search
          Log.w(TAG, "Web search failed: ${result.exceptionOrNull()}")
        }
      }
    }
  } catch (e: Exception) {
    Log.e(TAG, "Web search error", e)
  }
}

// ... (LLM inference) ...

// After receiving LLM response, append citations
if (_searchResultsForCitation.value.isNotEmpty()) {
  fullResponse = CitationFormatter.appendSources(
    response = fullResponse,
    searchResults = _searchResultsForCitation.value
  )
  _searchResultsForCitation.value = emptyList() // Clear
}
```

### SearchModule.kt (Hilt DI)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

  @Provides
  @Singleton
  fun provideBraveSearchService(retrofit: Retrofit): BraveSearchService {
    return retrofit.create(BraveSearchService::class.java)
  }

  @Provides
  @Singleton
  fun provideDuckDuckGoClient(): DuckDuckGoClient {
    // TBD: Provide HTTP client
    return DuckDuckGoClient()
  }

  @Provides
  @Singleton
  fun provideCircuitBreaker(): CircuitBreaker {
    return CircuitBreaker()
  }
}
```

---

## REMOVED

None. Keeping all existing code, only adding new patterns.

---

## BEHAVIORAL CHANGES

### Before (Current State)
1. Brave Search API called
2. Top 3 results formatted as simple text list
3. Results appended to user message
4. **LLM often ignores results**, uses training data
5. No citations or source attribution
6. No fallback if Brave fails

### After (Perplexica Pattern)
1. **Circuit breaker** wraps Brave Search call
2. If Brave fails → **DuckDuckGo fallback**
3. Top 5 results formatted using **SearchPromptTemplate**
4. Template includes:
   - XML-style structure (`<context>`, `<instructions>`, `<user_query>`)
   - **CRITICAL instructions** forcing LLM to prioritize search results
   - Current date prominently displayed
   - Citation requirements (`[1], [2]`)
5. LLM generates response with citations
6. **CitationFormatter** appends "Sources:" section
7. User sees response with source attribution

### Quality Improvements
- **LLM compliance**: >80% (from ~30%)
- **Citation accuracy**: >75% (from 0%)
- **Resilience**: >90% fallback success
- **Date consistency**: Unified format using current date
- **User trust**: Source attribution visible

---

## ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────┐
│                    Web Search Pipeline                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  User Query                                                 │
│       │                                                     │
│       ▼                                                     │
│  SearchRepository                                           │
│       │                                                     │
│       ├─ Circuit Breaker → Brave Search (PRIMARY)          │
│       │     └─ Success? → Format with template             │
│       │     └─ Fail? → Fallback ↓                          │
│       │                                                     │
│       ├─ Circuit Breaker → DuckDuckGo (FALLBACK)           │
│       │     └─ Success? → Format with template             │
│       │     └─ Fail? → SearchUnavailableException          │
│       │                                                     │
│       ▼                                                     │
│  SearchPromptTemplate.format()                              │
│       │ (Perplexica pattern)                                │
│       │ - Current date                                      │
│       │ - Search results [1], [2], [3]                      │
│       │ - CRITICAL instructions                             │
│       │                                                     │
│       ▼                                                     │
│  LlmChatViewModel                                           │
│       │ Append prompt to user message                       │
│       │                                                     │
│       ▼                                                     │
│  LLM Inference                                              │
│       │ (uses search results, adds citations)               │
│       │                                                     │
│       ▼                                                     │
│  CitationFormatter.appendSources()                          │
│       │ Parse [1], [2] → Append "Sources:" section          │
│       │                                                     │
│       ▼                                                     │
│  Display to User                                            │
│       ├─ Response with citations                            │
│       └─ Sources section with URLs                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## TESTING SPECIFICATION

### Unit Tests (New)
- `SearchPromptTemplateTest.kt` - Template formatting
- `DuckDuckGoClientTest.kt` - Fallback provider
- `CircuitBreakerTest.kt` - Resilience logic
- `CitationFormatterTest.kt` - Source attribution

### Integration Tests (New)
- `SearchRepositoryIntegrationTest.kt` - Full pipeline with fallback

### Manual Tests (Quality Audit)
1. Weather query → Current date + citations
2. News query → Recent events + multiple sources
3. Date-sensitive query → Correct year
4. Network failure → Fallback success
5. Both providers fail → Error handling

### Success Metrics
- ✅ LLM compliance >80%
- ✅ Citation accuracy >75% (10-query audit)
- ✅ Fallback success >90%
- ✅ Latency <1s p95

---

## DEPENDENCIES

### Unchanged
- Brave Search API (working)
- Retrofit 2.x
- Hilt DI
- Kotlin Coroutines

### New
- **DuckDuckGo client** (TBD: Find library or build wrapper)
- **Circuit breaker** (implement in-house, ~100 lines)
- **Citation parser** (implement, ~50 lines)

---

## MIGRATION NOTES

### Backward Compatibility
- ✅ Existing SearchRepository interface unchanged
- ✅ WebSearchPreferences unchanged
- ✅ UI toggle unchanged
- ✅ Rate limiting unchanged

### Breaking Changes
- None. All changes additive.

### Rollback Plan
- Revert to `main` branch (before `feature/web-search-fresh-perplexica-pattern`)
- Old implementation still functional

---

## REFERENCE LINKS

### Primary
- **Perplexica**: https://github.com/ItzCrazyKns/Perplexica (27.7k⭐)
  - Prompt template: `src/prompts/webSearchPrompt.ts`
  - Citation format: `src/lib/outputParsers/citationFormatter.ts`

### Secondary
- **duckduckgo-search**: https://github.com/deedy5/ddgs (4.8k⭐, MIT)
- **LangChain Search Tools**: https://python.langchain.com/docs/integrations/tools/
- **Production Patterns** (Research doc): Circuit breakers, fallback chains

### Research
- Amazon research: In-context citation prompting (+14% grounding)
- Perplexity stats: 358ms p50 latency, 93% SimpleQA accuracy
- NLI citation verification (future phase)

---

## SUCCESS DEFINITION

**Feature Complete When:**
1. ✅ All 51 tasks checked in `tasks.md`
2. ✅ All unit tests passing (CI green)
3. ✅ Manual quality audit >75% citation accuracy
4. ✅ Screenshot evidence with citations
5. ✅ LESSONS_LEARNED.md updated
6. ✅ OpenSpec change archived

**User-Visible Changes:**
- Web search responses include `[1], [2]` citations
- "Sources:" section appears with URLs
- Current date referenced in responses
- More consistent, accurate answers
- Fallback works when Brave fails
