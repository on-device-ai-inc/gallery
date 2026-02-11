# Web Search - Fresh Implementation Using Perplexica Pattern

## Summary

Replace current web search implementation with battle-tested patterns from Perplexica (27.7k⭐), focusing on the **formatting challenge** (80% of effort) rather than API integration. Copy proven prompt engineering techniques to ensure LLM compliance with search results.

## Motivation

### Current State
- ✅ Brave Search API integration working (SearchRepository.kt, BraveSearchService.kt)
- ✅ Rate limiting, usage tracking, CI/CD all functional
- ❌ **LLM response formatting is inconsistent** (biggest challenge)
- ❌ LLM ignores search results, uses outdated training data
- ❌ Dates formatted inconsistently despite current date in prompt
- ❌ No citation support or source attribution

### Problem Statement
> **80% of web search implementation effort is prompt engineering for LLM compliance, NOT the API integration.**
> — LESSONS_LEARNED.md (2026-01-23)

**Research Validation**:
- "50-90% of LLM responses are not fully supported by cited sources" (Amazon research)
- Perplexity achieves 93% SimpleQA accuracy with 358ms p50 latency
- **Solution**: Structured prompts + citation verification (Perplexica pattern)

## Scope

### In Scope

1. **Copy Perplexica's Prompt Template System**
   - Structured XML-style prompts with clear sections
   - Explicit instructions: "prioritize search results", "cite sources", "use current date"
   - Citation format: `[1], [2]` with source list at end

2. **Implement Multi-Provider Fallback** (from research)
   - Primary: Brave Search (current, 2K/mo free)
   - Secondary: DuckDuckGo (community library, unlimited)
   - Tertiary: SearXNG (self-hosted fallback, optional)

3. **Add Citation Support**
   - Format: "According to [1], claim text here"
   - Source list appended after response
   - Based on Perplexica's citation implementation

4. **Result Formatting Pipeline** (copy from research)
   - Extract top 3-5 results
   - Add current date prominently
   - Strong LLM compliance instructions
   - Preserve source URLs for citations

5. **Simple Circuit Breaker** (production pattern from research)
   - 5-failure threshold → fallback to next provider
   - 60-second timeout before retry
   - Health monitoring per provider

### Out of Scope

**Do NOT implement** (avoid over-engineering):
- ❌ Query decomposition (adds complexity)
- ❌ Reranking (adds latency on mobile)
- ❌ Multi-agent orchestration (overkill)
- ❌ Hybrid retrieval (not needed for web search)
- ❌ Semantic caching (implement later if needed)
- ❌ LangChain/Haystack frameworks (too heavy for Android)

**Future Phases**:
- Phase 2: Semantic caching (68.8% API call reduction per research)
- Phase 3: SearXNG self-hosted option ($5-10/mo VPS, unlimited queries)

## Technical Approach

### 1. Copy Perplexica's Prompt Template

**Source**: https://github.com/ItzCrazyKns/Perplexica (27.7k stars)

**Pattern to Copy**:
```kotlin
val searchPromptTemplate = """
<context>
Current date: $currentDate
Web search results for: "$query"

${searchResults.mapIndexed { i, result ->
  """[${i + 1}] ${result.title}
  ${result.description}
  Source: ${result.url}
  """
}.joinToString("\n\n")}
</context>

<instructions>
CRITICAL SEARCH RESULT USAGE:
1. Your training data is from before $currentDate - the search results are CURRENT
2. ALWAYS prioritize information from the search results over your training data
3. When stating facts from search results, cite them using [1], [2], etc.
4. If search results contradict your training, trust the search results
5. Format dates consistently using the current date as reference
6. Include a "Sources:" section at the end listing all [1], [2] citations
</instructions>

<user_query>
$userMessage
</user_query>
"""
```

**Why This Works** (from research):
- In-context citation prompting improves grounding by 14% (Amazon research)
- XML-style markup ensures reliable parsing
- Explicit prioritization prevents LLM from defaulting to training data

### 2. Multi-Provider Fallback (Copy from Research)

**Pattern**: Cascading fallback chain with circuit breakers

```kotlin
sealed class SearchProvider {
  object Brave : SearchProvider()          // Primary (current)
  object DuckDuckGo : SearchProvider()     // Secondary (ddgs library)
  object SearXNG : SearchProvider()        // Tertiary (future)
}

class SearchRepository @Inject constructor(
  private val braveService: BraveSearchService,
  private val duckDuckGoClient: DuckDuckGoClient,  // NEW
  private val circuitBreaker: CircuitBreaker       // NEW
) {
  suspend fun search(query: String): Result<SearchResponse> {
    // Try Brave first
    circuitBreaker.execute(SearchProvider.Brave) {
      braveService.search(query)
    }.onSuccess { return it }

    // Fallback to DuckDuckGo
    circuitBreaker.execute(SearchProvider.DuckDuckGo) {
      duckDuckGoClient.search(query)
    }.onSuccess { return it }

    // All providers failed
    return Result.failure(SearchUnavailableException())
  }
}
```

**Library to Use**: `duckduckgo-search` (4.8k stars, MIT) - Python has this, need Kotlin equivalent or REST wrapper

### 3. Citation Support (Copy Perplexica Pattern)

**Format**:
```
User: "What's the weather in New York?"

LLM Response:
"As of January 23, 2026, New York is experiencing partly cloudy conditions
with a high of 42°F [1]. The forecast shows possible rain tomorrow [2].

Sources:
[1] Weather.com - New York Weather Forecast
    https://weather.com/weather/today/l/New+York+NY
[2] NOAA - National Weather Service
    https://www.weather.gov/okx/"
```

**Implementation**:
- Parse LLM response for `[1], [2]` citations
- Match to search result index
- Append "Sources:" section with titles and URLs

### 4. Circuit Breaker (Production Pattern)

**Copy from research recommendations**:
```kotlin
class CircuitBreaker {
  private var failureCount = 0
  private var state = State.CLOSED
  private var lastFailureTime: Long = 0

  enum class State { CLOSED, OPEN, HALF_OPEN }

  suspend fun <T> execute(
    provider: SearchProvider,
    block: suspend () -> T
  ): Result<T> {
    if (state == State.OPEN) {
      val elapsed = System.currentTimeMillis() - lastFailureTime
      if (elapsed < 60_000) { // 60 second timeout
        return Result.failure(CircuitOpenException())
      }
      state = State.HALF_OPEN
    }

    return try {
      val result = block()
      failureCount = 0
      state = State.CLOSED
      Result.success(result)
    } catch (e: Exception) {
      failureCount++
      lastFailureTime = System.currentTimeMillis()

      if (failureCount >= 5) { // 5-failure threshold
        state = State.OPEN
      }
      Result.failure(e)
    }
  }
}
```

## Battle-Tested References

### Primary Reference: Perplexica
- **GitHub**: https://github.com/ItzCrazyKns/Perplexica (27.7k stars)
- **License**: MIT
- **What to Copy**:
  - Prompt template structure (`src/prompts/`)
  - Citation format implementation
  - Multi-mode search logic (Academic, Web, etc.)
  - Result formatting pipeline

### Secondary References

1. **duckduckgo-search (ddgs)** - 4.8k stars, MIT
   - Fallback search provider
   - Python library: https://github.com/deedy5/ddgs
   - Need Kotlin equivalent or REST wrapper

2. **LangChain Search Patterns** - 125k stars, MIT
   - Tool definitions: `langchain.tools.tavily_search`
   - Structured output with Pydantic models
   - Don't import framework (too heavy), just copy patterns

3. **Production Patterns** (from research)
   - Circuit breaker: 5-failure threshold, 60s timeout, 3-success recovery
   - Fallback chain: Primary → Secondary → Cached
   - Citation verification: NLI models (future phase)

## Success Criteria

### Functional Requirements
1. ✅ User sends message with web search ON → Top 3-5 results in prompt
2. ✅ LLM response includes citations: `[1], [2]`
3. ✅ "Sources:" section appended with URLs
4. ✅ Current date prominently displayed in response
5. ✅ If Brave fails → DuckDuckGo fallback works
6. ✅ Circuit breaker prevents cascading failures

### Quality Requirements
1. ✅ **LLM compliance >80%** - Uses search results, not training data
2. ✅ **Citation accuracy >75%** - Citations support claims (manual review)
3. ✅ **Date formatting consistent** - All dates match current date format
4. ✅ **Latency <1s p95** - Search + LLM generation combined
5. ✅ **Fallback success rate >90%** - When primary fails, secondary works

### Evidence Required (Per LESSONS_LEARNED.md)
1. ✅ **Screenshot**: Chat with web search result + citations visible
2. ✅ **Test output**: Unit tests for prompt template formatting
3. ✅ **Manual verification**: 10+ test queries with citation accuracy check

## Implementation Strategy

### Minimize Changes
1. **Keep existing code**:
   - SearchRepository.kt (modify, don't replace)
   - BraveSearchService.kt (keep as-is)
   - WebSearchPreferences.kt (keep as-is)
   - SearchModule.kt (add DuckDuckGo provider only)

2. **Add new files**:
   - `SearchPromptTemplate.kt` (copy from Perplexica)
   - `DuckDuckGoClient.kt` (fallback provider)
   - `CircuitBreaker.kt` (resilience)
   - `CitationFormatter.kt` (source attribution)

3. **Modify LlmChatViewModel**:
   - Replace current prompt building with template
   - Add citation parsing logic
   - Use new fallback-aware SearchRepository

### Copy-Paste Strategy
1. Study Perplexica's `src/prompts/webSearchPrompt.ts`
2. Translate TypeScript prompt template to Kotlin string template
3. Copy circuit breaker pattern from research (no library needed, <100 lines)
4. Copy citation format exactly (don't invent new format)

## Risks & Mitigations

**Risk**: DuckDuckGo has no official API, community library may break
**Mitigation**: Wrap in circuit breaker, Brave is primary (2K/mo free sufficient)

**Risk**: Citation parsing brittle if LLM doesn't follow format
**Mitigation**: Strong prompt instructions + fallback to no-citation mode if parsing fails

**Risk**: Prompt template too long, exceeds context limit
**Mitigation**: Limit to top 3 results (current), each ~100 tokens = 300 tokens total

**Risk**: Over-engineering (adding features from research we don't need)
**Mitigation**: Strict scope adherence - ONLY prompt template + fallback + citations

## Dependencies

### Existing (No Changes)
- Brave Search API (working)
- Retrofit (working)
- Hilt (working)

### New (Add)
- DuckDuckGo client (TBD - find Kotlin library or build REST wrapper)
- Circuit breaker (implement, ~100 lines, no library)

## Analytics to Track

1. **Search provider usage**: Brave vs DuckDuckGo hit rate
2. **Circuit breaker trips**: How often fallback triggered
3. **Citation inclusion rate**: % of responses with `[1], [2]` format
4. **User satisfaction**: Track which responses get regenerated (proxy for quality)

---

## **Bottom Line**

**Goal**: Fix the formatting challenge (80% of effort) by copying Perplexica's battle-tested prompt template.

**Not Goal**: Build a complex multi-agent RAG system with query decomposition and reranking.

**Deliverable**: Working web search with consistent LLM compliance and citations.

**Effort**: ~4-6 hours implementation (prompt template + fallback + citations)

**Validation**: LESSONS_LEARNED.md documents the successful pattern for future reference.
