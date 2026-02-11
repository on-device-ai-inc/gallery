# Spec Delta - Web Search Perplexica Service

## REMOVED

### SearchPromptTemplate
```kotlin
// REMOVED: app/src/main/java/ai/ondevice/app/data/SearchPromptTemplate.kt
// Reason: Replaced by Perplexica service (handles prompt engineering internally)

object SearchPromptTemplate {
  enum class Mode { SPEED, BALANCED, QUALITY }

  fun getWriterPrompt(context: String, systemInstructions: String, mode: Mode): String
  fun formatSearchContext(query: String, searchResults: List<SearchResult>): String
  fun format(query: String, searchResults: List<SearchResult>, mode: Mode): String
}
```

### SearchRepository
```kotlin
// REMOVED: app/src/main/java/ai/ondevice/app/data/SearchRepository.kt
// Reason: Replaced by PerplexicaClient (direct API calls instead of repository pattern)

class SearchRepository @Inject constructor(
  private val braveService: BraveSearchService,
  private val duckDuckGoClient: DuckDuckGoClient,
  private val circuitBreaker: CircuitBreaker
) {
  suspend fun search(query: String): Result<SearchResponse>
  fun formatResults(query: String, results: List<SearchResult>): String
}
```

### BraveSearchService
```kotlin
// REMOVED: app/src/main/java/ai/ondevice/app/data/BraveSearchService.kt
// Reason: Perplexica uses SearxNG (aggregates 210+ engines including Brave)

interface BraveSearchService {
  @GET("/v1/web/search")
  suspend fun search(
    @Query("q") query: String,
    @Query("count") count: Int = 5
  ): BraveSearchResponse
}

data class BraveSearchResponse(
  val web: WebResults
)

data class WebResults(
  val results: List<SearchResult>
)
```

### CircuitBreaker
```kotlin
// REMOVED: app/src/main/java/ai/ondevice/app/data/CircuitBreaker.kt (if exists)
// Reason: Perplexica handles resilience internally

class CircuitBreaker {
  enum class State { CLOSED, OPEN, HALF_OPEN }
  suspend fun <T> execute(provider: SearchProvider, block: suspend () -> T): Result<T>
}
```

### DuckDuckGoClient
```kotlin
// REMOVED: app/src/main/java/ai/ondevice/app/data/DuckDuckGoClient.kt (if exists)
// Reason: Perplexica uses SearxNG (includes DuckDuckGo)

class DuckDuckGoClient @Inject constructor() {
  suspend fun search(query: String, count: Int): List<SearchResult>
}
```

### WebSearchPreferences (Modified - Remove Limit)
```kotlin
// MODIFIED: app/src/main/java/ai/ondevice/app/ui/preferences/WebSearchPreferences.kt
// Remove: 5-query limit tracking
// Remove: Usage counter UI
// Update: Toggle description to "Powered by Perplexica"

// REMOVED FIELDS:
private const val MAX_WEB_SEARCH_QUERIES = 5
private var queriesUsedToday: Int = 0
private var lastResetDate: String = ""

// REMOVED METHODS:
fun canUseWebSearch(): Boolean
fun incrementUsageCount()
fun resetDailyCounter()
```

---

## ADDED

### PerplexicaClient
```kotlin
// ADDED: app/src/main/java/ai/ondevice/app/data/PerplexicaClient.kt
// Purpose: HTTP client for Perplexica service API

package ai.ondevice.app.data

import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Request data model for Perplexica API.
 *
 * @param query User's search query
 * @param focus_mode Search mode (webSearch, academicSearch, youtubeSearch, redditSearch)
 */
data class PerplexicaRequest(
  val query: String,
  val focus_mode: String = "webSearch"
)

/**
 * Response data model from Perplexica API.
 *
 * @param message LLM-generated response with inline citations [1], [2]
 * @param sources List of source URLs referenced in citations
 */
data class PerplexicaResponse(
  val message: String,
  val sources: List<Source>
)

/**
 * Source citation data model.
 *
 * @param title Source title (e.g., "Weather.com - New York Forecast")
 * @param url Source URL
 */
data class Source(
  val title: String,
  val url: String
)

/**
 * Retrofit interface for Perplexica API endpoints.
 */
interface PerplexicaService {
  /**
   * Search endpoint.
   *
   * POST http://localhost:3000/api/search
   * Body: { "query": "...", "focus_mode": "webSearch" }
   *
   * @return Response with LLM-generated answer + citations
   */
  @POST("/api/search")
  suspend fun search(@Body request: PerplexicaRequest): PerplexicaResponse
}

/**
 * Client wrapper for Perplexica service.
 *
 * Handles HTTP calls to local Perplexica instance (http://localhost:3000).
 * Includes error handling and timeout logic.
 */
@Singleton
class PerplexicaClient @Inject constructor(
  private val service: PerplexicaService
) {

  /**
   * Execute web search query via Perplexica.
   *
   * @param query User's search query
   * @param focusMode Search mode (default: webSearch)
   * @return Result<String> with LLM response (includes citations) or error
   *
   * Example response:
   * "As of February 2, 2026, New York is experiencing partly cloudy conditions
   *  with a high of 42°F[1]. The forecast shows possible rain tomorrow[2].
   *
   *  Sources:
   *  [1] Weather.com - New York Weather Forecast
   *      https://weather.com/weather/today/l/New+York+NY"
   */
  suspend fun search(
    query: String,
    focusMode: String = "webSearch"
  ): Result<String> {
    return try {
      val response = service.search(
        PerplexicaRequest(query = query, focus_mode = focusMode)
      )
      Result.success(response.message)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
```

---

## MODIFIED

### SearchModule.kt
```kotlin
// MODIFIED: app/src/main/java/ai/ondevice/app/di/SearchModule.kt
// Changes:
// - Remove BraveSearchService, CircuitBreaker providers
// - Add PerplexicaService, PerplexicaClient providers

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

  // REMOVED:
  // @Provides @Singleton fun provideBraveSearchService(): BraveSearchService
  // @Provides @Singleton fun provideCircuitBreaker(): CircuitBreaker
  // @Provides @Singleton fun provideSearchRepository(...): SearchRepository

  // ADDED:

  /**
   * Provide Retrofit service for Perplexica API.
   *
   * Base URL: http://localhost:3000 (DGX Spark local deployment)
   * Timeout: 10 seconds
   */
  @Provides
  @Singleton
  fun providePerplexicaService(): PerplexicaService {
    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .build()

    val retrofit = Retrofit.Builder()
      .baseUrl("http://localhost:3000") // DGX Spark local
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    return retrofit.create(PerplexicaService::class.java)
  }

  /**
   * Provide PerplexicaClient singleton.
   */
  @Provides
  @Singleton
  fun providePerplexicaClient(service: PerplexicaService): PerplexicaClient {
    return PerplexicaClient(service)
  }
}
```

### LlmChatViewModel.kt
```kotlin
// MODIFIED: app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
// Changes:
// - Replace SearchRepository with PerplexicaClient injection
// - Remove prompt template logic
// - Call Perplexica API when web search enabled
// - Display Perplexica response directly (includes citations)

class LlmChatViewModel @Inject constructor(
  // REMOVED: private val searchRepository: SearchRepository,
  // ADDED:
  private val perplexicaClient: PerplexicaClient,

  // ... other dependencies unchanged
) : ViewModel() {

  // MODIFIED METHOD:
  suspend fun sendMessage(
    userMessage: String,
    webSearchEnabled: Boolean = false
  ) {
    if (webSearchEnabled) {
      // REMOVED: Old prompt template approach
      // val searchResults = searchRepository.search(userMessage)
      // val formattedPrompt = SearchPromptTemplate.format(userMessage, searchResults)
      // runInference(formattedPrompt)

      // ADDED: Call Perplexica service directly
      val result = perplexicaClient.search(userMessage)

      result.onSuccess { perplexicaResponse ->
        // Display Perplexica response (already includes citations)
        addMessage(
          model = currentModel,
          message = ChatMessageText(
            content = perplexicaResponse,
            side = ChatSide.AGENT,
            accelerator = "Perplexica",
            isMarkdown = true
          )
        )
      }.onFailure { error ->
        // Fallback: Run query without web search
        Log.e(TAG, "Perplexica error: ${error.message}, falling back to on-device")
        runInference(userMessage, webSearchEnabled = false)
      }
    } else {
      // Normal on-device inference (unchanged)
      runInference(userMessage, webSearchEnabled = false)
    }
  }
}
```

### WebSearchPreferences.kt
```kotlin
// MODIFIED: app/src/main/java/ai/ondevice/app/ui/preferences/WebSearchPreferences.kt
// Changes:
// - Remove 5-query limit logic
// - Update toggle description

@Composable
fun WebSearchPreferences(
  webSearchEnabled: Boolean,
  onWebSearchToggle: (Boolean) -> Unit
) {
  // REMOVED: Query limit tracking UI
  // val queriesRemaining = MAX_WEB_SEARCH_QUERIES - queriesUsedToday
  // Text("Queries remaining today: $queriesRemaining")

  // MODIFIED: Toggle description
  PreferenceSwitch(
    title = "Web Search",
    // BEFORE: description = "Enable web search (5 queries/day)"
    // AFTER:
    description = "Enable web search (Powered by Perplexica)",
    checked = webSearchEnabled,
    onCheckedChange = onWebSearchToggle
  )
}
```

---

## KEPT (No Changes)

### SearchResult
```kotlin
// KEPT: app/src/main/java/ai/ondevice/app/data/SearchResult.kt
// Reason: Generic data class, may be useful for future features

data class SearchResult(
  val title: String?,
  val url: String?,
  val description: String?
)
```

---

## Infrastructure Changes

### Docker Setup (External to Android App)
```yaml
# ADDED: /home/nashie/perplexica/docker-compose.yml
# Purpose: Run Perplexica + SearxNG on DGX Spark

version: '3.8'

services:
  perplexica:
    image: ghcr.io/itzcrazyKns/perplexica:latest
    container_name: perplexica
    ports:
      - "3000:3000"
    environment:
      - SEARXNG_API_URL=http://searxng:8080
      - OLLAMA_API_URL=http://host.docker.internal:11434
      - CHAT_MODEL=gpt-oss:120b
      - EMBEDDING_MODEL=nomic-embed-text
    depends_on:
      - searxng
    extra_hosts:
      - "host.docker.internal:host-gateway"

  searxng:
    image: searxng/searxng:latest
    container_name: searxng
    ports:
      - "8080:8080"
    volumes:
      - ./searxng:/etc/searxng
    environment:
      - SEARXNG_BASE_URL=http://localhost:8080/
```

### Ollama Models
```bash
# EXISTING: gpt-oss:120b (65GB) - Already downloaded
ollama list
# NAME            ID              SIZE
# gpt-oss:120b    a951a23b46a1    65 GB

# POTENTIALLY NEEDED: nomic-embed-text (for Perplexica embeddings)
# Check if needed, download if missing:
ollama pull nomic-embed-text
```

---

## Summary of Changes

| Category | Action | Count |
|----------|--------|-------|
| **Files Removed** | SearchPromptTemplate, SearchRepository, BraveSearchService, CircuitBreaker, DuckDuckGoClient | 5 |
| **Files Added** | PerplexicaClient | 1 |
| **Files Modified** | SearchModule, LlmChatViewModel, WebSearchPreferences | 3 |
| **Files Kept** | SearchResult | 1 |
| **Infrastructure** | Docker Compose (Perplexica + SearxNG) | 1 |

**Net Code Change**: -4 files (simplification)

**Complexity Reduction**:
- Remove: 2,000-char prompt template management
- Remove: Multi-provider fallback logic (Brave → DuckDuckGo)
- Remove: Circuit breaker pattern
- Remove: Query limit tracking
- Add: Simple HTTP client (PerplexicaClient)

**Result**: Android app becomes **simpler** and **more reliable** by delegating complexity to battle-tested Perplexica service.
