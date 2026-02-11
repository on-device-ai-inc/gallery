# Brave Search API Integration (5/day Shared Limit)

## Context

Following the privacy lock enhancement (commit 676affe), users now have a web search toggle in the + menu. However, the toggle is currently non-functional - it doesn't actually perform web searches. This change implements the backend integration with Brave Search API to make web search functional.

**User feedback:**
- "I want web search to get current information (weather, news, etc.)"
- "5 searches per day is acceptable for now"
- Privacy is paramount - queries leaving device must be clearly communicated

**Previous discussion:** Team evaluated Google Custom Search (100/day, requires billing), Brave Search (2,000/month, requires credit card even for free tier), and DuckDuckGo (no official API).

**Decision:** Use Brave Search API with shared app-level key, 5/day per-user rate limit, NO user API key option to avoid "free but needs credit card" trust issues.

## Scope

### In Scope
1. **Brave Search API integration** with hardcoded app key (BuildConfig)
2. **Rate limiting:** 5 searches per user per day (resets at midnight)
3. **Usage counter:** Display "3/5 searches used today" in + menu
4. **First-time warning dialog** when enabling web search:
   - Privacy notice: queries leave device
   - 5/day limit clearly stated
   - Options: "Enable Web Search" or "Cancel"
5. **Limit reached handling:** Show dialog when user hits 5/5 with options to disable web search or wait until tomorrow
6. **Search result integration:** Prepend top 3 Brave results to LLM prompt
7. **Error handling:** Network failures, API errors, graceful degradation
8. **DataStore persistence:** dailyCount, lastResetDate for rate limiting

### Out of Scope
- User-provided API keys (avoided due to credit card requirement trust issue)
- Settings screen for API key management
- Paid in-app purchase for unlimited searches (future phase)
- SearXNG self-hosted implementation (future migration path)
- Search history or caching
- Advanced search filters

### Future Phases Documented
- **Phase 2:** SearXNG self-hosted solution ($5-10/month VPS, unlimited queries)
- **Phase 3:** Paid in-app purchase option (if user demand warrants)

## BMM Context

- **Related Story:** Web search toggle UI (commit 676affe)
- **Enhancement Type:** Backend integration + rate limiting
- **User Value:** Access to current information (weather, news, facts) via web search

## Design Decisions

### API Choice: Brave Search
- **Why Brave?** Privacy-focused (aligns with our brand), 2,000/month free tier
- **Why NOT user API keys?** Brave requires credit card even for free tier - creates "bait and switch" perception
- **Why NOT Google?** Only 100/day free, also requires billing setup
- **Why NOT DuckDuckGo?** No official API (HTML scraping violates ToS)

### Rate Limiting: 5 per day
- **Math:** 2,000/month ÷ 30 days = 66/day ÷ 13 users = ~5/day sustainable
- **User impact:** Prevents 1-2 power users from consuming entire quota
- **Known constraint:** Limits app to ~13-17 active web search users
- **Mitigation:** Acceptable for MVP/beta, plan SearXNG migration for scale

### Usage Counter Visibility
- **Placement:** In + menu ModalBottomSheet, below web search toggle
- **Format:** "3/5 searches used today" (updates in real-time)
- **Rationale:** Transparency prevents user surprise when hitting limit

### Dialog Copy
**First-time enable:**
```
🌐 Enable Web Search?

Web search sends your queries to Brave Search
to retrieve current information like weather and news.

⚠️ Privacy notice: Your queries will leave your device

📊 Limit: 5 searches per day (shared across the app)

[Enable Web Search]  [Cancel]
```

**Limit reached (5/5):**
```
Daily Limit Reached

You've used all 5 web searches for today.

Your limit will reset at midnight.

[Disable Web Search]  [OK]
```

## User Impact

### Before
- Web search toggle exists but does nothing
- No way to get current information (weather, news, etc.)

### After
- Web search toggle functional with Brave Search API
- Top 3 search results prepended to LLM prompt
- Usage counter shows remaining searches
- Clear privacy communication
- 5/day limit enforced transparently

## Success Criteria

1. User enables web search → Dialog explains privacy/limit clearly
2. User sends message with web search ON → Top 3 Brave results prepended to prompt
3. Usage counter updates after each search (shows 4/5, 3/5, etc.)
4. 6th search attempt shows "Daily limit reached" dialog
5. Counter resets at midnight (daily)
6. Network failures handled gracefully (show error, don't crash)
7. Lock color reflects web search state (green=off, amber=on) from previous commit

## Risks & Mitigations

**Risk:** 13-17 user ceiling due to 5/day limit
**Mitigation:** Acceptable for MVP. Analytics will inform Phase 2 (SearXNG migration)

**Risk:** Brave API service disruption
**Mitigation:** Graceful error handling, inform user "Web search temporarily unavailable"

**Risk:** Users frustrated by 5/day limit
**Mitigation:** Clear communication upfront, usage counter prevents surprise

**Risk:** Query quality/relevance varies
**Mitigation:** Use top 3 results, clearly attribute to "Web search"

## Dependencies

- Brave Search API key (obtain from https://brave.com/search/api/)
- Retrofit HTTP client library (likely already in project)
- DataStore for usage tracking persistence

## Analytics to Track (Post-Launch)

1. % of users who enable web search
2. Average searches per user per day
3. % of users hitting 5/day limit
4. Web search success rate (API uptime)

This data informs when to migrate to SearXNG (Phase 2).
