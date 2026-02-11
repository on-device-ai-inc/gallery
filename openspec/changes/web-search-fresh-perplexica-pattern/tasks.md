# Implementation Tasks - Web Search Fresh (Perplexica Pattern)

## Phase 1: Copy Perplexica Prompt Template (2 hours)

### 1.1 Study Reference Implementation
- [ ] Read Perplexica's `src/prompts/webSearchPrompt.ts`
- [ ] Study citation format in responses
- [ ] Document key prompt engineering techniques
- [ ] Identify what to copy vs what to skip

### 1.2 Create SearchPromptTemplate.kt
- [ ] Create `app/src/main/java/ai/ondevice/app/data/SearchPromptTemplate.kt`
- [ ] Copy Perplexica's XML-style prompt structure
- [ ] Translate TypeScript template to Kotlin string template
- [ ] Add parameters: `query`, `currentDate`, `searchResults`
- [ ] Add "CRITICAL SEARCH RESULT USAGE" instructions section
- [ ] Add "Sources:" section requirements

### 1.3 Test Prompt Template
- [ ] Write unit test: `SearchPromptTemplateTest.kt`
- [ ] Test: Template renders with 3 search results
- [ ] Test: Current date inserted correctly
- [ ] Test: Instructions section present
- [ ] Test: Source list formatted correctly
- [ ] Verify: Output matches Perplexica pattern

---

## Phase 2: Add DuckDuckGo Fallback Provider (1.5 hours)

### 2.1 Research DuckDuckGo Client
- [ ] Find Kotlin library for DuckDuckGo search (or REST wrapper)
- [ ] Evaluate: `duckduckgo-search` Python library (4.8k stars)
- [ ] Decision: Use Kotlin library OR build REST wrapper to Python lib
- [ ] Document choice in LESSONS_LEARNED.md

### 2.2 Implement DuckDuckGoClient.kt
- [ ] Create `app/src/main/java/ai/ondevice/app/data/DuckDuckGoClient.kt`
- [ ] Interface: `suspend fun search(query: String, count: Int): List<SearchResult>`
- [ ] Parse results to match `SearchResult` data class
- [ ] Add error handling for network failures
- [ ] Add timeout (5 seconds max)

### 2.3 Test DuckDuckGo Client
- [ ] Write unit test: `DuckDuckGoClientTest.kt`
- [ ] Mock HTTP responses
- [ ] Test: Successful search returns results
- [ ] Test: Network error handled gracefully
- [ ] Test: Empty results handled correctly

---

## Phase 3: Implement Circuit Breaker (1 hour)

### 3.1 Create CircuitBreaker.kt
- [ ] Create `app/src/main/java/ai/ondevice/app/data/CircuitBreaker.kt`
- [ ] Implement states: CLOSED, OPEN, HALF_OPEN
- [ ] Add failure threshold: 5 failures → OPEN
- [ ] Add timeout: 60 seconds before HALF_OPEN
- [ ] Add recovery: 3 successes → CLOSED
- [ ] Track per provider (Brave, DuckDuckGo)

### 3.2 Test Circuit Breaker
- [ ] Write unit test: `CircuitBreakerTest.kt`
- [ ] Test: 5 failures opens circuit
- [ ] Test: Circuit stays open for 60 seconds
- [ ] Test: HALF_OPEN allows limited retries
- [ ] Test: 3 successes close circuit
- [ ] Test: Per-provider state isolation

---

## Phase 4: Update SearchRepository (1.5 hours)

### 4.1 Modify SearchRepository.kt
- [ ] Inject `DuckDuckGoClient` and `CircuitBreaker`
- [ ] Update `search()` to use fallback chain:
  - Primary: `braveService.search()`
  - Fallback: `duckDuckGoClient.search()`
- [ ] Wrap each provider call in `circuitBreaker.execute()`
- [ ] Return `SearchUnavailableException` if all fail
- [ ] Log which provider succeeded (for analytics)

### 4.2 Update Result Formatting
- [ ] Modify `formatResults()` to use `SearchPromptTemplate`
- [ ] Pass `query`, `currentDate`, `results` to template
- [ ] Return structured prompt (not just raw results)
- [ ] Keep top 3-5 results (not more)

### 4.3 Update SearchModule.kt (DI)
- [ ] Add `@Provides` for `DuckDuckGoClient`
- [ ] Add `@Provides` for `CircuitBreaker`
- [ ] Add `@Singleton` annotations
- [ ] Verify Hilt graph compiles

### 4.4 Test Updated Repository
- [ ] Write integration test: `SearchRepositoryIntegrationTest.kt`
- [ ] Test: Brave success → returns results
- [ ] Test: Brave fails → DuckDuckGo fallback works
- [ ] Test: Both fail → exception thrown
- [ ] Test: Circuit breaker prevents cascading failures

---

## Phase 5: Add Citation Support (1 hour)

### 5.1 Create CitationFormatter.kt
- [ ] Create `app/src/main/java/ai/ondevice/app/util/CitationFormatter.kt`
- [ ] Parse LLM response for `[1], [2]` citations
- [ ] Match citations to search result indices
- [ ] Generate "Sources:" section with titles + URLs
- [ ] Handle missing citations gracefully (no crash)

### 5.2 Test Citation Formatter
- [ ] Write unit test: `CitationFormatterTest.kt`
- [ ] Test: Response with `[1], [2]` → Sources section appended
- [ ] Test: Response without citations → No sources section
- [ ] Test: Invalid citation indices handled
- [ ] Test: Source formatting matches Perplexica pattern

---

## Phase 6: Update LlmChatViewModel (1 hour)

### 6.1 Modify LlmChatViewModel.kt
- [ ] Replace current web search prompt building with `SearchPromptTemplate`
- [ ] Pass formatted prompt to LLM (instead of raw results)
- [ ] After LLM response, call `CitationFormatter.appendSources()`
- [ ] Display response with citations in UI
- [ ] Keep existing persona injection BEFORE search results

### 6.2 Test ViewModel Changes
- [ ] Manual test: Send message with web search ON
- [ ] Verify: Prompt includes "CRITICAL SEARCH RESULT USAGE"
- [ ] Verify: LLM response includes citations `[1], [2]`
- [ ] Verify: Sources section appended at end
- [ ] Screenshot: Save as evidence

---

## Phase 7: Testing & Validation (2 hours)

### 7.1 Unit Tests (All Green)
- [ ] Run: `./gradlew test` (via GitHub Actions)
- [ ] Fix any failing tests
- [ ] Achieve 80%+ code coverage for new files

### 7.2 Manual Testing - Quality Checks
- [ ] **Test 1**: "What's the weather in New York?"
  - Verify: Current date in response
  - Verify: Citations present `[1], [2]`
  - Verify: Sources section with URLs
- [ ] **Test 2**: "Latest news on AI"
  - Verify: Recent news (not training data)
  - Verify: Multiple sources cited
- [ ] **Test 3**: "Who won the Super Bowl?" (date-sensitive)
  - Verify: Correct year based on current date
  - Verify: LLM doesn't use outdated training data
- [ ] **Test 4**: Brave API fails (disconnect network)
  - Verify: DuckDuckGo fallback works
  - Verify: Circuit breaker logs failure
- [ ] **Test 5**: Both providers fail
  - Verify: Error message displayed
  - Verify: App doesn't crash

### 7.3 Visual Verification (DroidRun or Screenshot)
- [ ] Take screenshot: Web search response with citations
- [ ] Take screenshot: Sources section visible
- [ ] Take screenshot: Fallback provider success
- [ ] Save screenshots as evidence

### 7.4 Citation Accuracy Audit (10 Queries)
- [ ] Run 10 diverse queries (weather, news, facts, code questions)
- [ ] For each response:
  - Count citations: `[1], [2]`, etc.
  - Verify citation supports claim (manual review)
  - Check sources section has correct URLs
- [ ] Calculate: Citation accuracy rate (target >75%)
- [ ] Document findings in test report

---

## Phase 8: CI/CD & Deployment (1 hour)

### 8.1 Build & Deploy
- [ ] Commit all changes
- [ ] Push to `feature/web-search-fresh-perplexica-pattern`
- [ ] Wait for CI to pass (GitHub Actions)
- [ ] Download APK artifact
- [ ] Install on device: `adb install -r app-debug.apk`

### 8.2 CI Monitoring
- [ ] If CI fails: `gh run view --log-failed`
- [ ] Fix errors (lint, build, test failures)
- [ ] Push again, repeat until green

### 8.3 Final Validation
- [ ] Launch app on device
- [ ] Test all 5 manual test cases
- [ ] Verify performance: Search + LLM <1s p95
- [ ] Confirm: No crashes, no ANRs

---

## Phase 9: Documentation & Cleanup (30 mins)

### 9.1 Update LESSONS_LEARNED.md
- [ ] Add: "Perplexica prompt template solves formatting challenge"
- [ ] Add: "Circuit breaker pattern prevents cascading failures"
- [ ] Add: "Citation format: `[1], [2]` with Sources section"
- [ ] Add: "DuckDuckGo fallback works reliably"
- [ ] Update change log with date and learnings

### 9.2 Archive OpenSpec Change
- [ ] Mark all tasks complete in this file
- [ ] Run: `/openspec-archive web-search-fresh-perplexica-pattern`
- [ ] Move to `openspec/archive/`
- [ ] Merge spec-delta.md to `openspec/specs/`

### 9.3 Clean Up Old Implementation (Optional)
- [ ] Decision: Keep or delete old web-search-fix OpenSpec change?
- [ ] If delete: Remove `openspec/changes/web-search-fix/`
- [ ] If keep: Mark as superseded in README

---

## Summary

**Total Estimated Time**: ~10 hours

**Critical Path**:
1. Prompt template (solves formatting challenge)
2. Fallback provider (resilience)
3. Citation support (quality)

**Success Metrics**:
- ✅ LLM compliance >80%
- ✅ Citation accuracy >75%
- ✅ Fallback success >90%
- ✅ Latency <1s p95

**Evidence**:
- Screenshots with citations
- Test report with 10-query audit
- CI green build
- LESSONS_LEARNED.md updated
