# Implementation Tasks - Web Search Perplexica Service

## Phase 1: Perplexica Setup on DGX Spark (4 hours)

### 1.1 Check Models & Dependencies
- [ ] Verify gpt-oss:120b available: `ollama list | grep gpt-oss`
- [ ] Check if nomic-embed-text needed: `ollama list | grep nomic-embed-text`
- [ ] Download nomic-embed-text if missing: `ollama pull nomic-embed-text`
- [ ] Verify Docker installed: `docker --version`
- [ ] Verify docker-compose installed: `docker-compose --version`

### 1.2 Create Perplexica Docker Setup
- [ ] Create directory: `mkdir -p /home/nashie/perplexica`
- [ ] Create `docker-compose.yml` with Perplexica + SearxNG
- [ ] Create `searxng/settings.yml` with `limiter: false`
- [ ] Set environment variables: OLLAMA_API_URL, CHAT_MODEL
- [ ] Configure `host.docker.internal` for Ollama access

### 1.3 Start Perplexica Service
- [ ] Pull images: `docker-compose pull`
- [ ] Start containers: `docker-compose up -d`
- [ ] Verify containers running: `docker ps | grep perplexica`
- [ ] Check logs: `docker logs perplexica`
- [ ] Verify Ollama accessible from container

### 1.4 Test Perplexica API
- [ ] Test search endpoint: `curl -X POST http://localhost:3000/api/search`
- [ ] Verify weather query returns current data
- [ ] Verify citations format: `[1], [2]` present
- [ ] Verify source URLs in response
- [ ] Test edge cases: empty query, network error

---

## Phase 2: Android App Integration (6 hours)

### 2.1 Create PerplexicaClient.kt
- [ ] Create `app/src/main/java/ai/ondevice/app/data/PerplexicaClient.kt`
- [ ] Define data models: `PerplexicaRequest`, `PerplexicaResponse`, `Source`
- [ ] Create Retrofit interface: `PerplexicaService`
- [ ] Implement `PerplexicaClient` with error handling
- [ ] Add timeout: 10 seconds max
- [ ] Add network error handling

### 2.2 Update Dependency Injection
- [ ] Modify `app/src/main/java/ai/ondevice/app/di/SearchModule.kt`
- [ ] Add `@Provides` for `PerplexicaService` (baseUrl: http://localhost:3000)
- [ ] Add `@Provides` for `PerplexicaClient`
- [ ] Remove Brave Search providers (BraveSearchService)
- [ ] Remove Circuit Breaker (no longer needed)
- [ ] Verify Hilt graph compiles

### 2.3 Remove Option 1 Code
- [ ] Delete `SearchPromptTemplate.kt`
- [ ] Delete `SearchRepository.kt`
- [ ] Delete `BraveSearchService.kt`
- [ ] Remove `CircuitBreaker.kt` (if exists)
- [ ] Remove `DuckDuckGoClient.kt` (if exists)
- [ ] Keep `SearchResult.kt` (may be useful later)

### 2.4 Update LlmChatViewModel.kt
- [ ] Replace `SearchRepository` injection with `PerplexicaClient`
- [ ] Remove prompt template logic
- [ ] Update `sendMessage()` to call `perplexicaClient.search()`
- [ ] Display Perplexica response directly (includes citations)
- [ ] Add error handling: fallback to on-device if Perplexica fails
- [ ] Remove web search result formatting code

### 2.5 Update Web Search Preferences
- [ ] Remove 5-query limit from `WebSearchPreferences.kt`
- [ ] Update toggle description: "Powered by Perplexica"
- [ ] Remove usage tracking (no longer needed)
- [ ] Remove rate limiting UI

### 2.6 Unit Tests
- [ ] Write `PerplexicaClientTest.kt`
- [ ] Test: Successful search returns response
- [ ] Test: Network error handled gracefully
- [ ] Test: Empty response handled correctly
- [ ] Test: Malformed JSON handled
- [ ] Mock Retrofit responses

---

## Phase 3: Build & Deploy (2 hours)

### 3.1 Local Verification
- [ ] Run lint: `./gradlew lintDebug` (via GitHub Actions)
- [ ] Run tests: `./gradlew test` (via GitHub Actions)
- [ ] Fix any failing tests
- [ ] Fix any lint errors

### 3.2 Build APK
- [ ] Commit changes: `feat(web-search): Migrate to Perplexica service`
- [ ] Push to feature branch: `feature/web-search-perplexica-service`
- [ ] Wait for CI to build APK
- [ ] Download APK artifact: `gh run download <id> -n app-debug`

### 3.3 Install on Device
- [ ] Verify device connected: `adb devices`
- [ ] Install APK: `adb install -r app-debug.apk`
- [ ] Launch app: `adb shell am start -n ai.ondevice.app/.MainActivity`
- [ ] Verify app starts without crashes

---

## Phase 4: Testing & Validation (3 hours)

### 4.1 Functional Testing
- [ ] **Test 1**: Weather query - "What's the weather in New York?"
  - Verify: Current weather data (not outdated)
  - Verify: Citations `[1], [2]` present
  - Verify: Source URLs in response
- [ ] **Test 2**: Date query - "What's today's date?"
  - Verify: Correct current date (February 2, 2026)
  - Verify: Not outdated date from training data
- [ ] **Test 3**: News query - "Latest AI news"
  - Verify: Recent articles (not old news)
  - Verify: Multiple sources cited
- [ ] **Test 4**: Sports query - "Who won the Super Bowl?"
  - Verify: Current year result
  - Verify: Citations support answer
- [ ] **Test 5**: Tech query - "Latest iPhone release"
  - Verify: Recent product info
  - Verify: Sources section visible

### 4.2 Edge Case Testing
- [ ] **Test 6**: Perplexica service offline
  - Stop Docker: `docker stop perplexica`
  - Send query → Verify fallback to on-device LLM
  - Restart: `docker start perplexica`
- [ ] **Test 7**: Network timeout
  - Query during Perplexica startup delay
  - Verify error message displayed
- [ ] **Test 8**: Empty query
  - Send empty string → Verify handled gracefully
- [ ] **Test 9**: Very long query (>500 chars)
  - Verify truncation or proper handling
- [ ] **Test 10**: Special characters in query
  - Send query with emoji, quotes, etc.

### 4.3 Citation Accuracy Audit (10 Queries)
- [ ] Run 10 diverse queries (weather, news, facts, sports, tech)
- [ ] For each response:
  - Count citations: `[1], [2]`, etc.
  - Verify citation supports claim (manual click URL)
  - Check sources section has correct URLs
- [ ] Calculate: Citation accuracy rate (target >90%)
- [ ] Document findings in test report

### 4.4 Performance Testing
- [ ] Measure latency: Time from query → response (10 samples)
- [ ] Calculate: p50, p95, p99 latency
- [ ] Verify: p95 <3 seconds (target)
- [ ] Measure: Perplexica container memory usage
- [ ] Measure: Ollama memory usage during query

### 4.5 Visual Verification (DroidRun or Screenshot)
- [ ] Take screenshot: Weather query with citations visible
- [ ] Take screenshot: Date query showing correct current date
- [ ] Take screenshot: Sources section visible
- [ ] Take screenshot: Multiple queries (no 5-query limit message)
- [ ] Save screenshots as evidence

---

## Phase 5: Documentation & Cleanup (2 hours)

### 5.1 Update LESSONS_LEARNED.md
- [ ] Add: "Option 1 failed: On-device LLMs cannot follow complex 2K-char prompts"
- [ ] Add: "Option 3 works: Perplexica service achieves >80% compliance"
- [ ] Add: "Self-hosted Perplexica = unlimited queries, zero API costs"
- [ ] Add: "Fallback pattern: Perplexica fails → on-device LLM (graceful degradation)"
- [ ] Update change log with date and learnings

### 5.2 Create Perplexica Setup Guide
- [ ] Create `PERPLEXICA_SETUP.md` in project root
- [ ] Document Docker Compose setup
- [ ] Document Ollama model requirements
- [ ] Document Android app configuration (localhost:3000)
- [ ] Document troubleshooting common issues

### 5.3 Archive Option 1 OpenSpec Change
- [ ] Mark all tasks complete in `openspec/changes/web-search-fresh-perplexica-pattern/tasks.md`
- [ ] Move to `openspec/archive/web-search-fresh-perplexica-pattern/`
- [ ] Add note: "DEPRECATED - Replaced by Option 3 (Perplexica service)"
- [ ] Update README in archive folder

### 5.4 Archive Option 3 OpenSpec Change
- [ ] Mark all tasks complete in this file
- [ ] Run: `/openspec-archive web-search-perplexica-service`
- [ ] Move to `openspec/archive/`
- [ ] Merge spec-delta.md to `openspec/specs/`

---

## Summary

**Total Estimated Time**: ~17 hours (2-3 days)

**Critical Path**:
1. Perplexica setup (local Docker service)
2. Android integration (PerplexicaClient.kt)
3. Testing (weather/date queries)

**Success Metrics**:
- ✅ Weather query returns current data with citations
- ✅ Date query returns correct current date
- ✅ Citation accuracy >90%
- ✅ Latency <3s p95
- ✅ No query limits enforced

**Evidence**:
- Screenshots showing correct weather/date
- Test report with 10-query citation audit
- Performance metrics (p95 latency)
- CI green build
- LESSONS_LEARNED.md updated

**Deliverable**: Android app using Perplexica service for unlimited, high-quality web search with citations.
