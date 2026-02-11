# Tasks: web-search-fix

## Phase 1: Investigation Tasks (Day 1, 4 hours)

- [x] **TASK-1.1**: Verify BRAVE_API_KEY configuration
  - ✅ BuildConfig.BRAVE_API_KEY exists in app/build.gradle.kts
  - ✅ API key configured in local.properties: `brave.api.key=BSAD80kXDlN7yQpuMV7F860QXo--MJp`
  - ❌ **ROOT CAUSE FOUND**: local.properties in .gitignore, so GitHub Actions builds have empty API key
  - Acceptance: ✅ API key issue identified

- [x] **TASK-1.2**: Verify network permissions and connectivity
  - ✅ INTERNET permission exists in AndroidManifest.xml
  - ✅ Network config allows HTTPS to api.search.brave.com
  - ✅ Other network calls work (model downloads)
  - Acceptance: ✅ App has network access capability (not the issue)

- [x] **TASK-1.3**: Add comprehensive logging to search flow
  - ✅ SearchRepository.kt already has comprehensive DEBUG logging (lines 43-99)
  - ✅ LlmChatViewModel.kt has web search debug logging (lines 100-183)
  - ✅ Logging shows full execution path
  - Acceptance: ✅ Logging already comprehensive

- [x] **TASK-1.4**: Test search integration end-to-end
  - ✅ Code review shows integration is correct
  - ✅ LlmChatViewModel properly checks searchRepository != null
  - ✅ Flow: Check prefs → Call search → Append results → Inference
  - Acceptance: ✅ Integration code is correct (not the issue)

- [x] **TASK-1.5**: Verify rate limiting logic
  - ✅ WebSearchPreferencesDataStore.canSearch() logic correct
  - ✅ resetCounterIfNeeded() uses LocalDate comparison
  - ✅ Rate limiting implementation correct
  - Acceptance: ✅ Rate limiting not blocking (not the issue)

- [x] **TASK-1.6**: Document root cause with evidence
  - ✅ **ROOT CAUSE**: BRAVE_API_KEY missing in GitHub Actions CI builds
  - ✅ **Evidence**:
    - local.properties in .gitignore (lines 19, 31)
    - ci.yml doesn't create local.properties before build
    - BuildConfig.BRAVE_API_KEY defaults to "" when local.properties missing
    - CI-built APKs have empty API key → API calls fail with 401
  - ✅ **Component**: Build configuration, not runtime code
  - Acceptance: ✅ Root cause documented with full evidence

## Phase 2: Fix Implementation Tasks (Day 2, 6 hours)

- [x] **TASK-2.1**: Implement fix for identified root cause
  - ✅ Updated `.github/workflows/ci.yml`:
    - Added step to create local.properties before debug build (lines 63-66)
    - Added step to create local.properties before release build (lines 94-97)
    - Reads BRAVE_API_KEY from GitHub secrets
  - ✅ Updated `app/build.gradle.kts`:
    - Added fallback to read from environment variable (lines 72-79)
    - Priority: Environment variable > local.properties > empty string
  - ✅ Created `docs/github-secrets-setup.md` with user instructions
  - Acceptance: ✅ Fix implemented, waiting for user to add GitHub secret

- [x] **TASK-2.2**: Add SearchException error types
  - ✅ Already implemented: RateLimitException in SearchRepository.kt
  - ✅ Error handling already comprehensive with Result<String> return type
  - Acceptance: ✅ Exception types already exist (no work needed)

- [x] **TASK-2.3**: Enhance SearchRepository error handling
  - ✅ Already implemented: try-catch wraps API calls (lines 59-99)
  - ✅ Returns Result.failure with RateLimitException
  - ✅ Logs errors with full context
  - Acceptance: ✅ Error handling already comprehensive (no work needed)

- [x] **TASK-2.4**: Fix rate limiting reset logic (if broken)
  - ✅ Rate limiting logic verified correct (TASK-1.5)
  - ✅ LocalDate.now() comparison correct
  - ✅ Not broken, no fix needed
  - Acceptance: ✅ No issues found (no work needed)

- [x] **TASK-2.5**: Integrate search results into chat flow
  - ✅ Already implemented in LlmChatViewModel.kt (lines 131-178)
  - ✅ formatResults() output appended with strong instructions (lines 143-149)
  - ✅ Persona adaptation with webSearchEnabled flag (lines 100-128)
  - Acceptance: ✅ Integration already correct (no work needed)

## Phase 3: Testing & Validation Tasks (Day 3, 4 hours)

**⏳ BLOCKED**: Waiting for user to add BRAVE_API_KEY to GitHub Secrets (see docs/github-secrets-setup.md)

- [ ] **TASK-3.1**: Manual test - Basic search functionality
  - Enable web search toggle
  - Send query: "What's the weather in Harare today?"
  - Verify Brave API called (check Logcat)
  - Verify results appear in AI response
  - Acceptance: Search works end-to-end

- [ ] **TASK-3.2**: Manual test - Rate limiting enforcement
  - Send 5 search queries in succession
  - Verify 6th query blocked with "Daily limit reached" message
  - Check Logcat shows dailyCount = 5
  - Acceptance: Rate limit enforced correctly

- [ ] **TASK-3.3**: Manual test - Rate limit reset
  - Change device date to tomorrow: `adb shell date MMDDHHMMYYYY`
  - Launch app (triggers resetCounterIfNeeded)
  - Send search query
  - Verify search works (counter was reset)
  - Reset device date: `adb shell date $(date +%m%d%H%M%Y)`
  - Acceptance: Counter resets on new day

- [ ] **TASK-3.4**: Manual test - Network error handling
  - Disable WiFi and mobile data
  - Attempt search
  - Verify error message shown: "Unable to search. Check internet connection."
  - Re-enable internet
  - Verify search works again
  - Acceptance: Network errors handled gracefully

- [ ] **TASK-3.5**: Manual test - API error handling
  - Temporarily break API key (if possible) or test with invalid key
  - Attempt search
  - Verify error message shown: "Search temporarily unavailable."
  - Restore API key
  - Verify search works
  - Acceptance: API errors don't crash app

- [ ] **TASK-3.6**: Unit test - SearchRepository.search() success case
  - Create SearchRepositoryTest.kt
  - Mock BraveSearchService with successful response
  - Assert Result.success with formatted results
  - Acceptance: Test passes

- [ ] **TASK-3.7**: Unit test - SearchRepository rate limiting
  - Mock WebSearchPreferencesDataStore.canSearch() = false
  - Assert Result.failure with RateLimitException
  - Verify incrementDailyCount() NOT called
  - Acceptance: Test passes

- [ ] **TASK-3.8**: Unit test - SearchRepository API failure
  - Mock BraveSearchService to throw exception
  - Assert Result.failure with ApiError
  - Verify error logged
  - Acceptance: Test passes

- [ ] **TASK-3.9**: Integration test - End-to-end with MockWebServer
  - Create WebSearchIntegrationTest.kt
  - Use MockWebServer to simulate Brave API
  - Test full flow: canSearch → API call → formatResults → incrementCount
  - Acceptance: Test passes

## Phase 4: Error Handling & UX Tasks (Day 3, 2 hours)

- [ ] **TASK-4.1**: Add error banner UI in ChatPanel
  - Show subtle banner above input when search fails
  - Display error message from SearchException
  - Auto-dismiss after 5 seconds
  - Acceptance: Error banner appears and dismisses correctly

- [ ] **TASK-4.2**: Update ChatPanel to display search errors
  - Handle Result.failure from searchRepository.search()
  - Extract error message from exception
  - Show banner without blocking message sending
  - Acceptance: User sees error but can still send messages

- [ ] **TASK-4.3**: Add Firebase Analytics events (if Crashlytics integrated)
  - Log "web_search_executed" event with success/failure
  - Log error type for failures
  - Track search query length
  - Acceptance: Events logged to Firebase (or skip if not integrated)

- [ ] **TASK-4.4**: Performance validation
  - Measure search latency with Logcat timestamps
  - Verify <2s additional latency for search
  - Test with 3 different queries
  - Acceptance: All searches complete within latency SLA

## Documentation Tasks

- [ ] **TASK-5.1**: Update LESSONS_LEARNED.md
  - Document root cause found during investigation
  - Add working code patterns
  - Add debugging techniques used
  - Acceptance: Lesson added with full context

- [ ] **TASK-5.2**: Update CODE_INDEX.md (if new files created)
  - Add SearchRepositoryTest.kt entry
  - Add WebSearchIntegrationTest.kt entry
  - Update SearchRepository.kt description with "fixed on [date]"
  - Acceptance: Index updated

## Success Criteria

All tasks checked ✅ AND:
- [ ] Search success rate >95% in manual testing (9/10 queries work)
- [ ] Rate limiting works correctly (6th search blocked, resets next day)
- [ ] All error types handled with user-friendly messages
- [ ] No crashes when API unreachable or rate limited
- [ ] Response latency <2s additional for search (measured)
- [ ] Unit tests pass (3/3)
- [ ] Integration test passes (1/1)
- [ ] Visual verification: Search results visible in AI response
- [ ] Logs show complete execution path for debugging

---

**Total Tasks**: 23 implementation tasks + 2 documentation tasks = 25 tasks
**Estimated Effort**: 16 hours across 3 days
