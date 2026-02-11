# Privacy Lock Enhancement + Web Search Toggle

## Context

The current privacy indicator implementation (Story 10, commit acd0e18) has UI refinement needs based on user feedback:

1. **Lock badge is too large** - The "🔒 On-device" badge with text takes up too much space and overlaps with the model config button (gear icon)
2. **"Works offline" badge is overkill** - The OfflineCapabilityBadge appears when network is unavailable, but user considers this redundant and unnecessary
3. **Web search capability needed** - App currently doesn't have web search, but users need access to current information (weather, news, etc.) that on-device models can't provide
4. **Privacy transparency required** - When web search is enabled, users need visual indication that data is leaving the device

## Scope

### In Scope
1. **Remove OfflineCapabilityBadge** entirely from codebase
2. **Simplify OnDeviceIndicator** to icon-only (no "On-device" text), smaller bubble size
3. **Add web search toggle** to existing + menu (ModalBottomSheet in MessageInputText.kt)
4. **Color-code lock based on state**:
   - 🟢 Green lock when web search disabled (100% on-device, private)
   - 🟠 Amber lock when web search enabled (hybrid mode, data leaves device)
5. **State persistence** - Web search preference survives app restarts
6. **Google Custom Search API integration** - Functional web search when toggle is ON

### Out of Scope
- Advanced search features (filters, date ranges, etc.)
- Search history or caching
- Other privacy indicators beyond the lock
- API key management UI (will use hardcoded key initially, can be enhanced later)
- Multi-turn web search context (each query is independent)

## BMM Context

- **Related Story:** Story 10 - Privacy Differentiation Indicators (completed in commit acd0e18)
- **Enhancement Type:** UI refinement + feature addition
- **User Feedback:** Lock too large, offline badge unnecessary, need web search capability

## Design Decisions

### Lock Color Scheme
- **Green (#4CAF50)**: Pure on-device mode - no data leaves device, maximum privacy
- **Amber (#FF9800)**: Hybrid mode - web search enabled, queries sent to Google API
- **Why amber instead of red?** Amber signals "caution" not "danger" - web search is opt-in and useful, not inherently bad

### Web Search Toggle Placement
- **Location:** Bottom of existing + menu (after Camera/Gallery/Files buttons)
- **Rationale:** Follows Claude's UX pattern (user provided reference screenshot), familiar to users
- **Visual separation:** HorizontalDivider separates action buttons from toggle settings

### API Choice
- **Google Custom Search JSON API** selected because:
  - Free tier: 100 queries/day (sufficient for MVP testing)
  - Structured JSON responses easy to parse
  - Well-documented, stable API
- **Alternative considered:** Brave Search API, DuckDuckGo (rejected: worse docs, rate limits)

## User Impact

### Before
- Lock badge: Large blue bubble with "🔒 On-device" text (clutters UI)
- Offline badge: Appears when WiFi off (user finds annoying)
- No web search: Can't get current information

### After
- Lock badge: Small icon-only (32dp bubble), dynamically colored
- Offline badge: Removed
- Web search: Available via + menu toggle, clear privacy indication via lock color

## Success Criteria

1. Lock icon visible and smaller (no overlap with settings button)
2. Lock color changes within 100ms of toggle change
3. Web search returns relevant results when enabled
4. Lock stays green when web search disabled
5. State persists across app restarts
6. No "Works offline" badge appears anywhere in app

## Risks & Mitigations

**Risk:** API costs could exceed free tier
**Mitigation:** Monitor usage, implement rate limiting, consider API key configuration in settings later

**Risk:** Users don't notice lock color change
**Mitigation:** Add tooltip or first-time dialog explaining colors (future enhancement)

**Risk:** Web search results quality varies
**Mitigation:** Use top 3 snippets, clearly attribute to Google Search

## Dependencies

- Google Cloud account with Custom Search API enabled
- API key (can use project-level key initially)
- DataStore or SharedPreferences for toggle state persistence
