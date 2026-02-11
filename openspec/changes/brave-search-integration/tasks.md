# Implementation Tasks - Brave Search Integration

## Phase 1: Brave Search API Setup

- [ ] Add Retrofit dependency to build.gradle (if not present)
- [ ] Create BraveSearchService.kt interface with search() method
- [ ] Create SearchResult data class (title, description, url)
- [ ] Create BraveSearchResponse wrapper data class
- [ ] Add BRAVE_API_KEY to BuildConfig (local.properties or gradle)
- [ ] Create Hilt module for Retrofit + BraveSearchService
- [ ] Test API call manually (log results to verify integration)

## Phase 2: Rate Limiting & Usage Tracking

- [ ] Update WebSearchPreferences data class with:
  - dailyCount: Int = 0
  - lastResetDate: String (ISO date format)
- [ ] Create canSearch() method in SearchRepository:
  - Check if lastResetDate < today → reset counter to 0
  - Return dailyCount < 5
- [ ] Create incrementUsageCounter() method:
  - Increment dailyCount by 1
  - Save to DataStore
- [ ] Create getCurrentUsage() method:
  - Returns Pair<Int, Int> (used, total) e.g., (3, 5)
- [ ] Test: Do 5 searches, verify 6th is blocked

## Phase 3: SearchRepository Implementation

- [ ] Create SearchRepository class with @Inject constructor
- [ ] Inject BraveSearchService and WebSearchPreferences
- [ ] Implement search(query: String): Result<String> method:
  - Check canSearch() → if false, return RateLimitException
  - Call braveService.search(query, count=3, apiKey)
  - Format results: "Web search results:\n1. [title] - [desc]\n..."
  - Increment usage counter
  - Return formatted string
- [ ] Handle exceptions: IOException (network), HttpException (API error)
- [ ] Test with mock BraveSearchService

## Phase 4: First-Time Warning Dialog

- [ ] Create WebSearchWarningDialog composable in MessageInputText.kt
- [ ] Show dialog when user toggles web search ON for first time
- [ ] Store "hasSeenWarning" boolean in WebSearchPreferences
- [ ] Dialog content:
  - Title: "🌐 Enable Web Search?"
  - Body: Privacy notice + 5/day limit explanation
  - Buttons: "Enable Web Search" | "Cancel"
- [ ] "Enable Web Search" → Set webSearchEnabled = true, hasSeenWarning = true
- [ ] "Cancel" → Keep toggle OFF
- [ ] Test: First toggle shows dialog, subsequent toggles don't

## Phase 5: Usage Counter UI

- [ ] In MessageInputText.kt ModalBottomSheet, add usage counter Row
- [ ] Position: Below web search toggle, above buttons
- [ ] Format: "📊 3/5 searches used today"
- [ ] Use getCurrentUsage() to get current count
- [ ] Update counter with LaunchedEffect when showAddContentMenu changes
- [ ] Show different text when limit reached: "⚠️ 5/5 - Limit reached"
- [ ] Test: Counter updates after each search

## Phase 6: Limit Reached Dialog

- [ ] Create WebSearchLimitDialog composable
- [ ] Trigger when user tries to search with dailyCount >= 5
- [ ] Dialog content:
  - Title: "Daily Limit Reached"
  - Body: "You've used all 5 web searches for today. Your limit will reset at midnight."
  - Buttons: "Disable Web Search" | "OK"
- [ ] "Disable Web Search" → Set webSearchEnabled = false
- [ ] "OK" → Dismiss dialog, keep web search enabled but can't use until tomorrow
- [ ] Test: 6th search shows dialog

## Phase 7: LLM Integration

- [ ] Inject SearchRepository into LlmChatViewModel
- [ ] In generateResponse(), before calling model:
  - Check if webSearchEnabled == true
  - If yes: Call searchRepository.search(userMessage)
  - If success: Prepend results to userMessage
  - If failure (rate limit, network): Show error Snackbar, continue without web search
- [ ] Format combined prompt: "[Web results]\n\nUser: [original message]"
- [ ] Test: Send message with web search ON → Results appear in conversation

## Phase 8: Error Handling

- [ ] Handle RateLimitException:
  - Show "Daily limit reached" dialog
  - Don't call API
- [ ] Handle IOException (network failure):
  - Show Snackbar: "Network error. Continuing without web search."
  - Send message to LLM without web results
- [ ] Handle HttpException (API error):
  - Show Snackbar: "Web search temporarily unavailable"
  - Send message to LLM without web results
- [ ] Handle empty search results:
  - If Brave returns 0 results, continue without prepending
- [ ] Test all error paths manually

## Phase 9: DataStore Persistence

- [ ] Ensure WebSearchPreferences stored in DataStore
- [ ] On app launch: Load dailyCount and lastResetDate
- [ ] On each search: Save updated dailyCount
- [ ] On midnight rollover: Reset dailyCount to 0, update lastResetDate
- [ ] Test: Do 3 searches, restart app, counter shows 3/5

## Phase 10: Testing & Verification

- [ ] Manual test: Enable web search, see warning dialog
- [ ] Manual test: Send message with web search ON, verify results prepended
- [ ] Manual test: Usage counter shows 1/5, 2/5, 3/5, etc.
- [ ] Manual test: 6th search shows "Limit reached" dialog
- [ ] Manual test: Disable web search at limit, re-enable next day works
- [ ] Manual test: Network offline → Graceful error, message still sends
- [ ] Manual test: Invalid API key → Graceful error shown
- [ ] Manual test: Lock color green (web search off), amber (web search on)
- [ ] Test on different screen sizes (phone, tablet)

## Phase 11: Documentation & Cleanup

- [ ] Add inline comments explaining rate limiting logic
- [ ] Document Brave API key setup in README or docs/
- [ ] Update openspec/changes/brave-search-integration/spec.md
- [ ] Remove any debug logging
- [ ] Commit changes with descriptive message
- [ ] Update OpenSpec proposal with actual implementation notes

## Acceptance Criteria Checklist

- [ ] AC1: Web search toggle functional (sends queries to Brave API)
- [ ] AC2: First-time enable shows privacy warning dialog
- [ ] AC3: Usage counter visible in + menu ("3/5 searches used today")
- [ ] AC4: 5/day limit enforced (6th search blocked)
- [ ] AC5: Limit reached dialog shows options to disable or wait
- [ ] AC6: Daily counter resets at midnight
- [ ] AC7: Top 3 Brave results prepended to LLM prompt
- [ ] AC8: Network errors handled gracefully (no crashes)
- [ ] AC9: Lock color reflects web search state (from previous commit)
- [ ] AC10: Usage persists across app restarts

## Future Phase: SearXNG Migration (Documented for Later)

**When to migrate:** When app reaches 13-17 active web search users

**SearXNG Implementation Tasks (Future):**
- [ ] Research: Deploy SearXNG on DigitalOcean/Hetzner VPS
- [ ] Setup: Docker compose configuration with Nginx reverse proxy
- [ ] Domain: Configure searxng.yourdomain.com with SSL
- [ ] API: Create SearXNGService interface (similar to BraveSearchService)
- [ ] Migration: Replace BraveSearchService with SearXNGService
- [ ] Remove: All rate limiting logic (unlimited queries)
- [ ] Update: Usage counter to show "Unlimited searches"
- [ ] Test: Verify search quality matches or exceeds Brave
- [ ] Cost: Budget $5-10/month for VPS hosting

**Estimated migration time:** 4-6 hours (one-time setup + code changes)
