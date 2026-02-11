# Web Search - Migrate to Perplexica Service (Option 3)

## Summary

Replace current on-device prompt template approach (Option 1) with Perplexica service running locally on DGX Spark. Android app calls Perplexica API for web search queries, eliminating prompt engineering complexity and achieving proven >80% LLM compliance.

## Motivation

### Current State (Option 1 - NOT WORKING)
- ✅ SearchPromptTemplate.kt with 2,000+ char Perplexica template
- ✅ Brave Search API integration
- ❌ **LLM not using search results for simple queries**
- ❌ Weather queries fail: returns outdated/incorrect info
- ❌ Date queries fail: "May 8, 2024" instead of current date
- ❌ Same issues as initial broken implementation
- 🔴 **ROOT CAUSE**: On-device LLM (MediaPipe/LiteRT) cannot reliably follow complex 2,000-char prompts

### Problem Statement

**On-device LLMs struggle with complex prompt templates.**
- Perplexica's 2,000+ character template works with GPT-4/Claude/Gemini (cloud models)
- MediaPipe/LiteRT models (on-device) have limited instruction-following capability
- Even with exact Perplexica template, on-device LLM ignores search results

**Option 1 Failure Evidence:**
- User query: "What's the weather?" → LLM ignores search results, gives outdated info
- User query: "What's today's date?" → LLM says old date, not current from search
- **Conclusion**: Prompt template approach CANNOT work with on-device LLM constraints

### Why Option 3 (Perplexica Service)

**Advantages:**
1. ✅ **Proven quality**: 27.7k⭐ GitHub, >80% LLM compliance, battle-tested
2. ✅ **No prompt engineering**: Perplexica handles all prompt complexity internally
3. ✅ **No query limits**: Self-hosted = unlimited searches (remove 5-query cap)
4. ✅ **Better models**: Use gpt-oss:120b (65GB) via Ollama for superior results
5. ✅ **Citation support**: Built-in `[1], [2]` citations with source URLs
6. ✅ **Multiple search modes**: Academic, YouTube, Reddit, Wolfram, etc.
7. ✅ **Zero infrastructure cost**: Run locally on DGX Spark (no VPS needed)

## Scope

### In Scope

#### Phase 1: Perplexica Setup (Local on DGX Spark)
1. **Install Perplexica via Docker**
   - Pull `ghcr.io/itzcrazykns/perplexica` image
   - Configure with Ollama backend (existing gpt-oss:120b model)
   - Configure SearxNG (bundled search engine)
   - Expose API on `http://localhost:3000`

2. **Verify Perplexica Installation**
   - Test search query via curl: `POST http://localhost:3000/api/search`
   - Verify citations format: `[1], [2]` in response
   - Confirm source URLs appended

#### Phase 2: Android App Integration
3. **Create PerplexicaClient.kt**
   - Retrofit HTTP client pointing to `http://localhost:3000/api/search`
   - Data models: `PerplexicaRequest`, `PerplexicaResponse`
   - Support search modes: web, academic, youtube, reddit
   - Error handling: network failures, timeout, malformed responses

4. **Remove Option 1 Code**
   - Delete `SearchPromptTemplate.kt` (no longer needed)
   - Remove Brave Search integration (SearchRepository, BraveSearchService)
   - Remove web search preferences (5-query limit)
   - Keep SearchResult data class (may be useful for future)

5. **Update LlmChatViewModel**
   - Replace `SearchRepository` injection with `PerplexicaClient`
   - Remove prompt template logic
   - Call Perplexica API when web search enabled
   - Display Perplexica response directly (includes citations)

6. **Update UI**
   - Remove 5-query limit warning
   - Update web search toggle description: "Powered by Perplexica"
   - Add search mode selector (optional): Web/Academic/YouTube/Reddit

#### Phase 3: Testing & Validation
7. **Test Cases**
   - ✅ Weather query: "What's the weather in New York?" → Current weather with citations
   - ✅ Date query: "What's today's date?" → Correct current date
   - ✅ News query: "Latest AI news" → Recent articles with sources
   - ✅ Citation accuracy: Manual review of 10 queries
   - ✅ Multiple queries: Verify no limit enforced

### Out of Scope

**Do NOT implement:**
- ❌ VPS deployment (using local DGX Spark, not cloud)
- ❌ Authentication/API keys (local deployment, no auth needed)
- ❌ Query caching (Perplexica handles internally)
- ❌ Multi-agent orchestration (Perplexica handles internally)
- ❌ SearxNG configuration (use Perplexica defaults)
- ❌ Advanced search modes (start with basic web search only)

**Future Phases:**
- Phase 4: Remote access via VPN/tunnel (if needed for device testing away from DGX)
- Phase 5: Search mode selector UI (Academic, YouTube, Reddit)
- Phase 6: Perplexica response caching for offline use

## Technical Approach

### Architecture Shift

**Before (Option 1):**
```
User → Android App → LlmChatViewModel
                    ↓
                SearchRepository → Brave API → Search Results
                    ↓
                SearchPromptTemplate.format() → 2,000 char prompt
                    ↓
                MediaPipe LLM (on-device) → Response (ignores search results ❌)
```

**After (Option 3):**
```
User → Android App → LlmChatViewModel
                    ↓
                PerplexicaClient → HTTP POST http://localhost:3000/api/search
                                 ↓
                            Perplexica Service (DGX Spark)
                                 ├─ SearxNG → Aggregate search results
                                 ├─ Ollama (gpt-oss:120b) → Generate response
                                 └─ Writer Agent → Add citations [1], [2]
                                 ↓
                            Response with citations ✅
                    ↓
                Display in Android UI
```

### Perplexica Setup (Docker on DGX Spark)

**Step 1: Docker Compose**

Create `/home/nashie/perplexica/docker-compose.yml`:

```yaml
version: '3.8'

services:
  perplexica:
    image: ghcr.io/itzcrazykns/perplexica:latest
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

**Step 2: Verify Ollama Access**

```bash
# Test Ollama is accessible from Docker
curl http://localhost:11434/api/tags
```

**Step 3: Start Perplexica**

```bash
cd /home/nashie/perplexica
docker-compose up -d
```

**Step 4: Test Perplexica API**

```bash
curl -X POST http://localhost:3000/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What is the weather in New York?",
    "focus_mode": "webSearch"
  }'
```

Expected response:
```json
{
  "message": "As of February 2, 2026, New York is experiencing partly cloudy conditions with a high of 42°F[1]. The forecast shows possible rain tomorrow[2].\n\nSources:\n[1] Weather.com - New York Weather Forecast\n    https://weather.com/weather/today/l/New+York+NY\n[2] NOAA - National Weather Service\n    https://www.weather.gov/okx/",
  "sources": [
    {
      "title": "Weather.com - New York Weather Forecast",
      "url": "https://weather.com/weather/today/l/New+York+NY"
    }
  ]
}
```

### Android Integration

**PerplexicaClient.kt:**

```kotlin
package ai.ondevice.app.data

import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

data class PerplexicaRequest(
  val query: String,
  val focus_mode: String = "webSearch" // webSearch, academicSearch, youtubeSearch, redditSearch
)

data class PerplexicaResponse(
  val message: String,
  val sources: List<Source>
)

data class Source(
  val title: String,
  val url: String
)

interface PerplexicaService {
  @POST("/api/search")
  suspend fun search(@Body request: PerplexicaRequest): PerplexicaResponse
}

@Singleton
class PerplexicaClient @Inject constructor(
  private val service: PerplexicaService
) {
  suspend fun search(query: String): Result<String> {
    return try {
      val response = service.search(PerplexicaRequest(query = query))
      Result.success(response.message)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
```

**SearchModule.kt (Updated):**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

  @Provides
  @Singleton
  fun providePerplexicaService(): PerplexicaService {
    val retrofit = Retrofit.Builder()
      .baseUrl("http://localhost:3000") // DGX Spark local
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    return retrofit.create(PerplexicaService::class.java)
  }

  @Provides
  @Singleton
  fun providePerplexicaClient(service: PerplexicaService): PerplexicaClient {
    return PerplexicaClient(service)
  }
}
```

**LlmChatViewModel.kt (Modified):**

```kotlin
class LlmChatViewModel @Inject constructor(
  private val perplexicaClient: PerplexicaClient, // CHANGED: was SearchRepository
  // ... other dependencies
) : ViewModel() {

  suspend fun sendMessage(userMessage: String, webSearchEnabled: Boolean) {
    if (webSearchEnabled) {
      // NEW: Call Perplexica service directly
      val result = perplexicaClient.search(userMessage)

      result.onSuccess { perplexicaResponse ->
        // Display Perplexica response (already includes citations)
        addMessage(
          model = currentModel,
          message = ChatMessageText(
            content = perplexicaResponse,
            side = ChatSide.AGENT,
            accelerator = "Perplexica"
          )
        )
      }.onFailure { error ->
        // Fallback: Run query without web search
        runInference(userMessage, webSearchEnabled = false)
      }
    } else {
      // Normal on-device inference
      runInference(userMessage, webSearchEnabled = false)
    }
  }
}
```

### Files to Remove

**Delete (No longer needed):**
```
app/src/main/java/ai/ondevice/app/data/SearchPromptTemplate.kt
app/src/main/java/ai/ondevice/app/data/SearchRepository.kt
app/src/main/java/ai/ondevice/app/data/BraveSearchService.kt
app/src/main/java/ai/ondevice/app/ui/preferences/WebSearchPreferences.kt (query limit logic)
```

**Keep (May be useful):**
```
app/src/main/java/ai/ondevice/app/data/SearchResult.kt (data class)
```

## Success Criteria

### Functional Requirements
1. ✅ Perplexica service running on DGX Spark at `http://localhost:3000`
2. ✅ Android app calls Perplexica API when web search enabled
3. ✅ Weather query returns current, accurate weather with citations
4. ✅ Date query returns correct current date
5. ✅ News query returns recent articles with source URLs
6. ✅ No 5-query limit enforced (unlimited searches)
7. ✅ Citations visible in response: `[1], [2]` with source list

### Quality Requirements
1. ✅ **LLM compliance >80%** - Perplexica proven track record
2. ✅ **Citation accuracy >90%** - Perplexica's built-in verification
3. ✅ **Latency <3s p95** - Local service, no cloud latency
4. ✅ **Zero API costs** - Self-hosted, unlimited queries
5. ✅ **Error handling** - Graceful fallback to on-device if Perplexica fails

### Evidence Required
1. ✅ Screenshot: Perplexica running (`docker ps` showing containers)
2. ✅ Screenshot: Curl test showing Perplexica API response with citations
3. ✅ Screenshot: Android app showing weather query with correct current data
4. ✅ Screenshot: Android app showing date query with correct current date
5. ✅ Test report: 10 diverse queries with citation accuracy audit

## Implementation Strategy

### Phased Rollout

**Phase 1: Perplexica Setup (Day 1)**
1. Install Docker containers on DGX Spark
2. Configure Ollama backend (gpt-oss:120b)
3. Test Perplexica API with curl
4. Verify citations and source URLs

**Phase 2: Android Integration (Day 2)**
5. Create PerplexicaClient.kt
6. Update SearchModule.kt DI
7. Modify LlmChatViewModel.kt
8. Remove Option 1 code (SearchPromptTemplate, etc.)

**Phase 3: Testing & Validation (Day 3)**
9. Test weather, date, news queries
10. Citation accuracy audit (10 queries)
11. Performance testing (latency measurements)
12. Edge case testing (Perplexica offline, network errors)

**Phase 4: Documentation & Archive (Day 4)**
13. Update LESSONS_LEARNED.md
14. Archive Option 1 OpenSpec change
15. Document Perplexica setup for future reference

### Rollback Plan

If Perplexica service fails:
1. Keep on-device LLM as fallback (already implemented)
2. Graceful degradation: web search toggle disabled, show error message
3. User can continue using app without web search

## Risks & Mitigations

### Risk 1: Perplexica service downtime
**Mitigation:** Fallback to on-device LLM (no web search). App remains functional.

### Risk 2: gpt-oss:120b model incompatible with Perplexica
**Mitigation:** Download recommended model (llama3.1:8b or mistral:7b if needed)

### Risk 3: Docker networking issues on DGX Spark
**Mitigation:** Use `host.docker.internal` for Ollama access, test connectivity first

### Risk 4: Latency higher than expected
**Mitigation:** Use local Ollama (no cloud calls), measure p95 latency, optimize if >3s

### Risk 5: Android app cannot reach localhost:3000 when device is remote
**Mitigation:** Phase 1 uses localhost (device must be on same network as DGX). Future: VPN/tunnel setup

## Dependencies

### Existing (No Changes)
- Ollama (gpt-oss:120b already downloaded)
- Docker (installed on DGX Spark)
- Retrofit (HTTP client in Android app)
- Hilt (DI framework)

### New (Add)
- Perplexica Docker image: `ghcr.io/itzcrazykns/perplexica:latest`
- SearxNG Docker image: `searxng/searxng:latest`
- PerplexicaClient.kt (new Kotlin class)

### Models (Check/Download)
- ✅ gpt-oss:120b (65GB) - Already downloaded
- ❓ nomic-embed-text (embedding model for Perplexica) - Check if needed

```bash
# Check if nomic-embed-text is available
ollama list | grep nomic-embed-text || ollama pull nomic-embed-text
```

## Analytics to Track

1. **Perplexica usage rate**: % of queries using web search (should increase with no limit)
2. **Query success rate**: % of Perplexica API calls that succeed
3. **Latency distribution**: p50, p95, p99 for Perplexica API calls
4. **Citation inclusion rate**: % of responses with `[1], [2]` citations
5. **Fallback rate**: % of queries falling back to on-device LLM

---

## Bottom Line

**Goal:** Achieve reliable web search with >80% LLM compliance by using battle-tested Perplexica service instead of DIY prompt engineering.

**Not Goal:** Run cloud-based Perplexica (using local DGX Spark deployment).

**Deliverable:** Android app with unlimited web search powered by Perplexica, with citations and source URLs.

**Effort:** ~2-3 days (setup + integration + testing)

**Validation:** Weather and date queries return correct current information with citations.
