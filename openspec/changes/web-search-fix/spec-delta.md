# Spec Delta: web-search-fix

This document describes the specification changes resulting from fixing the web search bug.

---

## MODIFIED

### web-search.md (Error Handling & Debugging)

**Location**: `openspec/specs/web-search.md` (to be created if doesn't exist, or modified if exists)

```diff
# Web Search Integration Specification

## Overview
Integrate Brave Search API to enhance AI responses with current information while maintaining privacy and rate limiting.

## Architecture

### Components
- **SearchRepository**: Orchestrates search requests, rate limiting, error handling
- **BraveSearchService**: Retrofit interface for Brave Search API
- **WebSearchPreferencesDataStore**: Manages toggle state and rate limiting counter

## Functional Requirements

### FR-1: Search Execution
- When web search enabled AND user sends query
- System fetches top 3 results from Brave Search API
- Results formatted and appended to conversation context
- LLM generates response with web search context

### FR-2: Rate Limiting
- Maximum 5 searches per day per user
- Counter resets at midnight local time
- 6th search blocked with message: "Daily search limit reached (5/day). Resets at midnight."

### FR-3: Error Handling

+ **NEW: Comprehensive error handling with user feedback**
+
+ #### Error Types
+ - **RateLimitExceeded**: User exceeded 5 searches/day
+ - **NetworkError**: Device offline or network unreachable
+ - **ApiError**: Brave API returned error or is unavailable
+ - **ConfigurationError**: API key missing or invalid
+
+ #### Error Responses
+ ```kotlin
+ sealed class SearchError : Exception() {
+     object RateLimitExceeded : SearchError() {
+         override val message = "Daily search limit reached (5/day). Resets at midnight."
+     }
+     object NetworkError : SearchError() {
+         override val message = "Unable to search. Check internet connection."
+     }
+     object ApiError : SearchError() {
+         override val message = "Search temporarily unavailable. Using AI knowledge."
+     }
+     object ConfigurationError : SearchError() {
+         override val message = "Search not configured. Please contact support."
+     }
+ }
+ ```
+
+ #### Error Display
+ - Show subtle banner above chat input
+ - Banner auto-dismisses after 5 seconds
+ - User can still send messages (fallback to AI knowledge)
+ - Error logged to Logcat with full context

### FR-4: Logging & Debugging

+ **NEW: Comprehensive logging for troubleshooting**
+
+ #### Log Points
+ 1. **Search Trigger**: When LlmChatViewModel decides to search
+    - Log: "Web search enabled: $isEnabled, shouldSearch: $shouldSearch"
+
+ 2. **Rate Limit Check**: Before API call
+    - Log: "Search rate limit check: canSearch=$canSearch, dailyCount=$count, lastReset=$date"
+
+ 3. **API Request**: Before calling Brave API
+    - Log: "Calling Brave Search API: query='$query', count=3"
+
+ 4. **API Response**: After receiving results
+    - Log: "Brave API response: ${response.web.results.size} results, status=${response.statusCode}"
+
+ 5. **Results Formatting**: After formatting for LLM
+    - Log: "Formatted search results: ${formatted.length} chars"
+
+ 6. **Error Occurrence**: On any failure
+    - Log: "Search failed: ${exception.javaClass.simpleName}: ${exception.message}" with full stack trace
+
+ #### Log Tag Convention
+ - SearchRepository: `"SearchRepo"`
+ - LlmChatViewModel: `"LlmChat"`
+ - WebSearchPreferences: `"WebSearchPrefs"`

## Non-Functional Requirements

### NFR-1: Performance
- Search adds <2s latency to response (p95)
- Results cached for 5 minutes (same query)
- Timeout: 5 seconds for API call

### NFR-2: Reliability
+ - Success rate >95% when internet available
- Graceful degradation when API unavailable
+ - No crashes on network errors
+ - All errors logged for debugging

### NFR-3: Privacy
- Search queries NOT logged to remote servers
- Only query hash logged locally for rate limiting
- Results not stored permanently

## API Specification

### Brave Search API
**Endpoint**: `https://api.search.brave.com/res/v1/web/search`

**Request**:
```
GET /res/v1/web/search?q={query}&count=3
Headers:
  X-Subscription-Token: {BRAVE_API_KEY}
```

**Response** (Success):
```json
{
  "web": {
    "results": [
      {
        "title": "Result title",
        "url": "https://example.com",
        "description": "Result snippet"
      }
    ]
  }
}
```

+ **Response** (Error):
+ ```json
+ {
+   "error": {
+     "code": "rate_limit_exceeded",
+     "message": "Rate limit exceeded"
+   }
+ }
+ ```
+
+ **HTTP Status Codes**:
+ - 200: Success
+ - 401: Invalid API key
+ - 429: Rate limit exceeded (Brave-side, separate from app limit)
+ - 500: Server error

## Testing Requirements

### Unit Tests
+ - SearchRepository.search() success case
+ - SearchRepository.search() with rate limit exceeded
+ - SearchRepository.search() with API error
+ - SearchRepository.search() with network error
- WebSearchPreferences rate limit logic
- Results formatting logic

### Integration Tests
+ - End-to-end search flow with MockWebServer
- Rate limit reset on date change

### Manual Tests
- Enable toggle → send query → verify results in response
- 6 searches → verify 7th blocked
- Offline → verify error message
- Next day → verify counter reset

## Acceptance Criteria

+ - [ ] AC-1: Search fetches results from Brave API when toggle enabled
+ - [ ] AC-2: Results formatted and visible in AI response
+ - [ ] AC-3: Rate limiting blocks 6th search with clear message
+ - [ ] AC-4: Network errors display: "Unable to search. Check internet connection."
+ - [ ] AC-5: API errors display: "Search temporarily unavailable. Using AI knowledge."
+ - [ ] AC-6: Logging shows full execution path for debugging
+ - [ ] AC-7: No crashes when API unreachable or rate limited
+ - [ ] AC-8: Response latency <2s additional (p95)
+ - [ ] AC-9: Success rate >95% in manual testing
```

---

## ADDED

### error-handling-patterns.md

**Location**: `openspec/specs/error-handling-patterns.md`

```markdown
# Error Handling Patterns Specification

## Overview
Standard patterns for error handling across OnDevice AI to ensure consistent user experience and debuggability.

## Principles

1. **No Silent Failures**: Every error visible to user OR logged
2. **Graceful Degradation**: Features fail without blocking core functionality
3. **User-Friendly Messages**: Technical errors translated to actionable messages
4. **Debug Context**: All errors logged with full context for troubleshooting

## Error Types

### Network Errors
**Scenario**: API calls, web requests fail due to connectivity

**User Message**: "Unable to [action]. Check internet connection."

**Logging**:
```kotlin
Log.e(TAG, "Network error during [operation]: ${e.message}", e)
```

**UI Pattern**: Subtle banner above input, auto-dismiss after 5s

### API Errors
**Scenario**: External API returns error or is unavailable

**User Message**: "[Feature] temporarily unavailable. [Fallback action]."

**Example**: "Search temporarily unavailable. Using AI knowledge."

**Logging**:
```kotlin
Log.e(TAG, "API error: endpoint=$endpoint, status=${response.code()}, message=${response.message()}")
```

### Configuration Errors
**Scenario**: Missing API keys, invalid settings

**User Message**: "[Feature] not configured. Please contact support."

**Logging**:
```kotlin
Log.e(TAG, "Configuration error: [specific missing config]")
```

**Action**: Disable feature UI, show error only on attempted use

### Rate Limit Errors
**Scenario**: User-side or API-side rate limiting

**User Message**: "[Feature] limit reached ([X]/day). Resets at [time]."

**Example**: "Daily search limit reached (5/day). Resets at midnight."

**Logging**:
```kotlin
Log.w(TAG, "Rate limit reached: currentCount=$count, limit=$limit, resetTime=$resetTime")
```

## Error Display Patterns

### Banner (Non-Blocking)
Use for errors that don't prevent core functionality:
- Network errors during optional features
- API errors with fallback
- Rate limiting

**Implementation**:
```kotlin
@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
    LaunchedEffect(message) {
        delay(5000)
        onDismiss()
    }
}
```

### Dialog (Blocking)
Use for errors requiring user action:
- Permissions required
- Critical configuration missing
- Data loss warnings

### Snackbar (Transient)
Use for operation confirmations or minor errors:
- "Message deleted"
- "Export failed"

## Logging Standards

### Log Levels
- **ERROR**: Exceptions, API failures, unexpected states
- **WARN**: Rate limits, deprecated features, recoverable issues
- **INFO**: Feature usage, significant state changes
- **DEBUG**: Flow execution, variable values (debug builds only)

### Log Format
```kotlin
Log.e(TAG, "[Operation] failed: [reason]. Context: key1=$value1, key2=$value2", exception)
```

**Example**:
```kotlin
Log.e("SearchRepo", "Search failed: API timeout. Context: query='$query', timeout=5s, attempt=$retryCount", e)
```

### Sensitive Data
**NEVER log**:
- User messages/conversations
- API keys
- Personally identifiable information

**CAN log**:
- Query hashes (for rate limiting)
- Error codes/types
- Performance metrics

## Testing Error Handling

### Unit Tests
- Test each error type returns correct exception
- Verify error messages are user-friendly
- Assert logging occurs

### Integration Tests
- Simulate network failures (MockWebServer)
- Test API errors (mock 500 responses)
- Verify UI shows error banner

### Manual Tests
- Disable WiFi → trigger feature → verify message
- Invalid API key → trigger feature → verify message
- Exceed rate limit → verify message

## Acceptance Criteria

- [ ] All errors have user-friendly messages (no technical jargon)
- [ ] No crashes on expected error conditions
- [ ] All errors logged with sufficient context for debugging
- [ ] Error UI doesn't block core functionality (chat always works)
- [ ] Error messages provide actionable guidance
```

---

## REMOVED

**None** - This change only adds/modifies specs, no specs are removed.

---

## Summary of Changes

| Spec File | Type | Changes |
|-----------|------|---------|
| `web-search.md` | MODIFIED | Added comprehensive error handling (FR-3), logging & debugging (FR-4), API error responses, updated acceptance criteria |
| `error-handling-patterns.md` | ADDED | New spec defining error handling standards for entire app |

## Rationale

### Why Modify web-search.md?
The existing spec (or lack thereof) didn't specify:
- How to handle network errors gracefully
- What error messages to show users
- What logging to add for debugging
- API error response formats

This change adds **operational readiness** to the spec.

### Why Add error-handling-patterns.md?
This bug revealed a gap: no standardized error handling patterns across the app. This new spec:
- Prevents future similar bugs
- Ensures consistent UX for errors
- Establishes logging standards
- Guides developers on error handling decisions

## Migration Path

1. **For web-search-fix**: Implement error handling per modified web-search.md spec
2. **For future features**: Follow error-handling-patterns.md from start
3. **For existing features**: Gradually refactor to match patterns (no rush, opportunistic)

## Validation

After implementation:
- [ ] SearchRepository.kt matches error handling in web-search.md
- [ ] Error messages match specified text exactly
- [ ] Logging includes all specified log points
- [ ] Error banner UI matches pattern in error-handling-patterns.md
- [ ] All acceptance criteria in web-search.md are met
