# Proposal: web-search-fix

## Summary
Investigate and fix web search result fetching bug where toggle UI works but Brave Search API calls are NOT returning results.

## Motivation
**User Report**: "WEBSEARCH TOGGLE WORKING, APP DOESNT SEEM TO BE FETCHING ACTUAL RESULTS, INVESTIGATION NEEDED THEN FIX"

**Current State**:
- ✅ Settings toggle UI implemented and functional
- ✅ Backend code exists (SearchRepository.kt, WebSearchPreferencesDataStore.kt, BraveSearchService.kt)
- ❌ NOT fetching actual search results when toggle is enabled
- ❌ Users believe web search is working (toggle is on) but get no search data

**Impact**:
- Feature appears enabled but doesn't work (user trust issue)
- Users cannot access current information via web search
- Silent failure (no error message shown to user)

**Business Priority**: P0 (Critical) - Broken feature that users think is working

## Scope

### IN SCOPE
- ✅ Root cause investigation of fetch failure
- ✅ Fix the identified issue
- ✅ Add error handling and user feedback
- ✅ Validate search results are fetched correctly
- ✅ Ensure rate limiting UI feedback works
- ✅ Add logging for debugging

### OUT OF SCOPE
- ❌ Changing Brave Search API to different provider
- ❌ Adding new search features
- ❌ UI/UX redesign of search results display
- ❌ Citation format implementation (separate feature)

## Acceptance Criteria

### Functional Requirements
- [ ] **AC1**: When user enables web search toggle, searches actually fetch results from Brave API
- [ ] **AC2**: Search results are correctly formatted and appended to conversation
- [ ] **AC3**: Rate limiting (5/day) correctly blocks after 5 searches with clear message
- [ ] **AC4**: API errors display user-friendly error messages (not silent failures)
- [ ] **AC5**: Network errors are handled gracefully with retry suggestion
- [ ] **AC6**: Logging added for debugging (API calls, responses, errors)

### Testing Requirements
- [ ] **AC7**: Manual test: Enable toggle → send query → verify Brave API called
- [ ] **AC8**: Manual test: Verify search results appear in AI response
- [ ] **AC9**: Manual test: 6th search blocked with "Daily limit reached" message
- [ ] **AC10**: Unit test: SearchRepository.search() returns valid results
- [ ] **AC11**: Integration test: End-to-end search flow with mock API

### Quality Requirements
- [ ] **AC12**: No crashes when API unreachable
- [ ] **AC13**: No silent failures (user always knows search status)
- [ ] **AC14**: Response latency <2s additional for search

## Technical Approach

### Phase 1: Investigation (Day 1, 4 hours)

**Hypothesis Checklist**:
1. **API Key Issue**
   - Check if BRAVE_API_KEY is configured in BuildConfig
   - Verify API key is valid (test with curl)
   - Check if key is being passed to BraveSearchService

2. **Network Permission Issue**
   - Verify INTERNET permission in AndroidManifest.xml
   - Check if network security config blocks HTTP/HTTPS
   - Test if other network calls work (model downloads)

3. **Integration Not Triggered**
   - Add logs to LlmChatViewModel where search should be called
   - Verify toggle state is read correctly
   - Check if search is conditionally skipped

4. **API Endpoint Issue**
   - Verify Brave Search API endpoint is correct
   - Check if API changed and requires new parameters
   - Test endpoint with curl/Postman

5. **Rate Limiting Blocking All Requests**
   - Check if daily counter is stuck at 5
   - Verify resetCounterIfNeeded() is called
   - Test if date comparison logic is broken

6. **Results Not Displayed**
   - Verify formatResults() is working
   - Check if results are appended to prompt correctly
   - Ensure persona adaptation for web search is applied

**Investigation Tools**:
- Logcat with filters for "Search", "Brave", "WebSearch"
- Network inspector (Charles Proxy or similar)
- Breakpoints in SearchRepository.kt
- curl test of Brave API with actual API key

**Expected Output**: Root cause identified with evidence (logs, network traces)

### Phase 2: Fix Implementation (Day 2-3, 1-2 days)

**Based on most likely root causes**:

**If API Key Issue**:
```kotlin
// Check BuildConfig has key
if (BuildConfig.BRAVE_API_KEY.isBlank()) {
    Log.e("SearchRepository", "BRAVE_API_KEY not configured")
    return Result.failure(Exception("Search not configured"))
}
```

**If Integration Not Triggered**:
```kotlin
// Add logging in LlmChatViewModel
val searchEnabled = webSearchPreferences.isEnabled()
Log.d("LlmChat", "Web search enabled: $searchEnabled")
if (searchEnabled && shouldSearch(prompt)) {
    val searchResults = searchRepository.search(prompt)
    Log.d("LlmChat", "Search results: ${searchResults.getOrNull()}")
}
```

**If Error Handling Missing**:
```kotlin
// SearchRepository.kt
try {
    val response = braveService.search(query, count, apiKey)
    Log.d("SearchRepo", "API response: ${response.web.results.size} results")
    return Result.success(formatResults(response))
} catch (e: Exception) {
    Log.e("SearchRepo", "Search failed: ${e.message}", e)
    // Show user-friendly error
    return Result.failure(SearchException("Search unavailable. Check internet connection."))
}
```

**If Rate Limiting Broken**:
```kotlin
// Fix resetCounterIfNeeded logic
val today = LocalDate.now()
val lastReset = LocalDate.parse(prefs.lastResetDate)
Log.d("WebSearchPrefs", "Today: $today, Last reset: $lastReset")
if (lastReset.isBefore(today)) {
    Log.d("WebSearchPrefs", "Resetting counter (was ${prefs.dailyCount})")
    resetDailyCount()
}
```

### Phase 3: Testing & Validation (Day 3, 4 hours)

**Manual Testing Checklist**:
1. Enable web search toggle in settings
2. Send query: "What's the weather in Harare today?"
3. Verify Brave API called (Logcat)
4. Verify results appear in AI response
5. Send 5 more queries
6. Verify 6th query blocked with message
7. Change device date to tomorrow
8. Verify counter reset, search works again
9. Disable internet
10. Verify error message shown
11. Re-enable internet
12. Verify search works again

**Automated Testing**:
- Unit test: `SearchRepositoryTest.kt` - test search() with mock API
- Integration test: End-to-end with MockWebServer

### Phase 4: Error Handling & UX (Day 3, 2 hours)

**User-Facing Error Messages**:
```kotlin
sealed class SearchError : Exception() {
    object RateLimitExceeded : SearchError() {
        override val message = "Daily search limit reached (5/day). Resets at midnight."
    }
    object NetworkError : SearchError() {
        override val message = "Unable to search. Check internet connection."
    }
    object ApiError : SearchError() {
        override val message = "Search temporarily unavailable. Using AI knowledge."
    }
}
```

**Display in Chat**:
- Show subtle banner above input: "⚠️ Search unavailable: [reason]"
- Don't block message sending (fallback to AI knowledge)
- Log for debugging

## Implementation Files

### Files to Investigate
1. `SearchRepository.kt` (130 lines) - API call logic
2. `WebSearchPreferencesDataStore.kt` - Toggle state, rate limiting
3. `BraveSearchService.kt` - Retrofit interface
4. `LlmChatViewModel.kt` - Integration point (where search should be called)
5. `PersonaLibrary.kt` - Web search persona adaptation
6. `BuildConfig.kt` - Check if BRAVE_API_KEY exists
7. `AndroidManifest.xml` - Verify INTERNET permission

### Files to Potentially Modify
1. `SearchRepository.kt` - Add logging, fix bugs, improve error handling
2. `LlmChatViewModel.kt` - Ensure search is triggered when enabled
3. `WebSearchPreferencesDataStore.kt` - Fix rate limit reset logic if broken
4. `ChatPanel.kt` - Add error message UI for search failures

### Files to Create
1. `SearchRepositoryTest.kt` - Unit tests for search logic
2. `WebSearchIntegrationTest.kt` - End-to-end search flow test

## References

### Existing Implementation
- SearchRepository: `app/src/main/java/ai/ondevice/app/data/SearchRepository.kt`
- Preferences: `app/src/main/java/ai/ondevice/app/data/WebSearchPreferencesDataStore.kt`
- ViewModel integration: `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt`

### Related PRD Sections
- PRD v3.0 MASTER - Epic 5: Web Search (F11.1)
- Target: >85% factual accuracy, <2s additional latency
- Rate limiting: 5 searches/day

### Dependencies
- Brave Search API documentation
- Retrofit 2.x
- Kotlin Coroutines
- DataStore Proto

## Rollout Plan

### Stage 1: Investigation Complete
- Root cause identified with evidence
- Fix approach confirmed
- User update: "Found the issue: [root cause]"

### Stage 2: Fix Implemented
- Code changes merged
- Unit tests passing
- Manual testing complete
- User update: "Fix implemented, ready for testing"

### Stage 3: Validation
- 10+ manual test searches successful
- Rate limiting works correctly
- Error handling validated
- User update: "Web search now working correctly"

### Stage 4: Monitoring
- Add Firebase Analytics event: `web_search_executed`
- Monitor success rate
- Track error types
- Week 1 review: Validate no regressions

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Search success rate | >95% | Analytics (successful API calls / total attempts) |
| User-perceived functionality | 100% | Manual testing (search works when toggle on) |
| Error rate | <5% | Logcat analysis (API errors / total calls) |
| Rate limit accuracy | 100% | Manual test (6th search blocked) |
| Response latency | <2s added | Performance monitoring |

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| API key not configured | High | Critical | Document how to obtain/configure Brave API key |
| Brave API changed | Medium | High | Test with curl, contact Brave support if needed |
| Multiple bugs (not just one issue) | Medium | High | Systematic investigation checklist |
| Fix breaks other features | Low | Medium | Regression testing (run all Maestro flows) |

## Timeline

| Day | Tasks | Output |
|-----|-------|--------|
| **Day 1** (4h) | Investigation phase | Root cause identified with evidence |
| **Day 2** (6h) | Fix implementation | Code changes complete, unit tests passing |
| **Day 3** (6h) | Testing, error handling, validation | Manual testing complete, ready to ship |

**Total Effort**: 2-3 days (16 hours)

---

**Status**: PROPOSAL - Awaiting approval
**Next Step**: User reviews and approves → then `/openspec-apply web-search-fix`
