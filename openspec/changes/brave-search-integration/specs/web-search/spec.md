# Web Search Integration Specification

## Purpose
This specification covers the Brave Search API integration that enables web search functionality in the app, with rate limiting, usage tracking, and privacy-conscious UX.

## ADDED Requirements

### Requirement: Brave Search API Integration
The system SHALL integrate with Brave Search API to retrieve current web information when the web search toggle is enabled.

#### Scenario: User sends message with web search enabled
- WHEN web search toggle is ON
- AND user sends a message
- THEN the system SHALL call Brave Search API with the user's message as query
- AND it SHALL retrieve the top 3 search results
- AND it SHALL format results as: "Web search results:\n1. [title] - [description]\n2. [title] - [description]\n3. [title] - [description]"
- AND it SHALL prepend the formatted results to the user's message before sending to the LLM

#### Scenario: User sends message with web search disabled
- WHEN web search toggle is OFF
- AND user sends a message
- THEN the system SHALL NOT call Brave Search API
- AND the message SHALL be sent directly to the LLM without web results

#### Scenario: Brave API returns no results
- WHEN Brave Search API is called
- AND the API returns zero results for the query
- THEN the system SHALL send the user's message to the LLM without prepending web results
- AND it SHALL log "No web results found for query" for debugging

### Requirement: Rate Limiting (5 Searches Per Day)
The system SHALL enforce a limit of 5 web searches per user per day to conserve the shared API quota.

#### Scenario: User within daily limit
- WHEN user has performed fewer than 5 searches today
- AND user sends a message with web search enabled
- THEN the system SHALL process the web search normally
- AND it SHALL increment the daily usage counter by 1

#### Scenario: User exceeds daily limit
- WHEN user has already performed 5 searches today
- AND user attempts to send a message with web search enabled
- THEN the system SHALL display "Daily Limit Reached" dialog
- AND it SHALL NOT call the Brave Search API
- AND it SHALL NOT increment the usage counter
- AND the message SHALL be sent to the LLM without web results

#### Scenario: Daily counter resets at midnight
- WHEN the system detects that the current date is later than the lastResetDate
- THEN it SHALL reset dailyCount to 0
- AND it SHALL update lastResetDate to the current date
- AND subsequent searches SHALL be allowed (user gets fresh 5 searches)

#### Scenario: Counter persists across app restarts
- WHEN user performs 3 searches and closes the app
- AND user reopens the app later the same day
- THEN the usage counter SHALL show 3/5 (not reset to 0/5)
- AND the user SHALL have 2 remaining searches for that day

### Requirement: Usage Counter Display
The system SHALL display a real-time usage counter showing how many searches the user has performed out of their daily limit.

#### Scenario: Counter shown in + menu
- WHEN user opens the + menu (ModalBottomSheet)
- AND web search is enabled
- THEN the system SHALL display "📊 X/5 searches used today" below the web search toggle
- WHERE X is the current dailyCount value

#### Scenario: Counter updates after search
- WHEN user performs a web search
- THEN the usage counter SHALL update immediately
- AND it SHALL reflect the new count (e.g., 2/5 → 3/5)

#### Scenario: Limit reached warning
- WHEN user has used all 5 searches (dailyCount == 5)
- THEN the usage counter SHALL display "⚠️ 5/5 - Limit reached"
- AND the text SHALL use a warning color (e.g., amber or red)

### Requirement: First-Time Warning Dialog
The system SHALL display a privacy warning dialog when the user enables web search for the first time.

#### Scenario: User enables web search for first time
- WHEN user toggles web search ON
- AND the hasSeenWarning preference is false
- THEN the system SHALL display the "Enable Web Search?" dialog
- AND the dialog SHALL contain:
  - Privacy notice explaining queries leave the device
  - 5/day limit notice
  - Two buttons: "Enable Web Search" and "Cancel"

#### Scenario: User confirms web search enablement
- WHEN the "Enable Web Search?" dialog is shown
- AND user taps "Enable Web Search"
- THEN webSearchEnabled SHALL be set to true
- AND hasSeenWarning SHALL be set to true
- AND the dialog SHALL close
- AND the web search toggle SHALL remain ON

#### Scenario: User cancels web search enablement
- WHEN the "Enable Web Search?" dialog is shown
- AND user taps "Cancel"
- THEN webSearchEnabled SHALL remain false
- AND the web search toggle SHALL return to OFF position
- AND hasSeenWarning SHALL remain false (dialog will show again next time)

#### Scenario: User enables web search subsequently
- WHEN user has previously seen and accepted the warning (hasSeenWarning == true)
- AND user toggles web search ON again (after having turned it OFF)
- THEN the system SHALL NOT show the warning dialog
- AND web search SHALL enable immediately

### Requirement: Daily Limit Reached Dialog
The system SHALL display a dialog when the user attempts to use web search after reaching their daily limit.

#### Scenario: User attempts 6th search of the day
- WHEN user has already performed 5 searches today (dailyCount == 5)
- AND user tries to send a message with web search enabled
- THEN the system SHALL display "Daily Limit Reached" dialog
- AND the dialog SHALL contain:
  - Message: "You've used all 5 web searches for today. Your limit will reset at midnight."
  - Two buttons: "Disable Web Search" and "OK"

#### Scenario: User disables web search from limit dialog
- WHEN "Daily Limit Reached" dialog is shown
- AND user taps "Disable Web Search"
- THEN webSearchEnabled SHALL be set to false
- AND the web search toggle SHALL turn OFF
- AND the dialog SHALL close

#### Scenario: User dismisses limit dialog
- WHEN "Daily Limit Reached" dialog is shown
- AND user taps "OK"
- THEN the dialog SHALL close
- AND webSearchEnabled SHALL remain true (toggle stays ON)
- AND subsequent message attempts SHALL continue to show the limit dialog

### Requirement: Error Handling
The system SHALL handle API errors, network failures, and edge cases gracefully without crashing or blocking the user.

#### Scenario: Network connection unavailable
- WHEN user sends a message with web search enabled
- AND the device has no internet connection
- THEN the system SHALL catch the IOException
- AND it SHALL display a Snackbar: "Network error. Continuing without web search."
- AND it SHALL send the user's message to the LLM without web results
- AND it SHALL NOT increment the usage counter

#### Scenario: Brave API returns HTTP error
- WHEN Brave Search API returns an HTTP error (400, 401, 500, etc.)
- THEN the system SHALL catch the HttpException
- AND it SHALL display a Snackbar: "Web search temporarily unavailable"
- AND it SHALL send the user's message to the LLM without web results
- AND it SHALL NOT increment the usage counter

#### Scenario: Invalid API key configured
- WHEN Brave Search API is called with an invalid API key
- AND the API returns a 401 Unauthorized response
- THEN the system SHALL display an error: "Web search configuration error"
- AND it SHALL log the error for debugging
- AND it SHALL send the user's message to the LLM without web results

#### Scenario: Rate limit exception occurs
- WHEN user attempts to search beyond their daily limit
- THEN the system SHALL throw RateLimitException
- AND it SHALL display "Daily Limit Reached" dialog
- AND it SHALL NOT make any API call
- AND the user's message SHALL be sent to the LLM without web results

### Requirement: DataStore Persistence
The system SHALL persist web search usage data across app sessions using DataStore.

#### Scenario: First app launch
- WHEN the app is launched for the first time
- THEN dailyCount SHALL default to 0
- AND lastResetDate SHALL default to current date
- AND hasSeenWarning SHALL default to false

#### Scenario: Usage data persists
- WHEN user performs 3 searches and has seen the warning
- AND the app is closed
- AND the app is reopened
- THEN dailyCount SHALL be 3 (not reset to 0)
- AND lastResetDate SHALL be the date of the searches
- AND hasSeenWarning SHALL be true

#### Scenario: Data updates saved immediately
- WHEN user performs a search
- THEN the updated dailyCount SHALL be written to DataStore immediately
- AND the write SHALL complete before returning to the UI

## Implementation Notes

**Brave Search API Endpoint:**
```
GET https://api.search.brave.com/res/v1/web/search
Query params: q=[query], count=3
Header: X-Subscription-Token: [API_KEY]
```

**Data Classes:**
```kotlin
data class BraveSearchResponse(
  val web: WebResults
)

data class WebResults(
  val results: List<SearchResult>
)

data class SearchResult(
  val title: String,
  val description: String,
  val url: String
)

data class WebSearchPreferences(
  val enabled: Boolean = false,
  val dailyCount: Int = 0,
  val lastResetDate: String = LocalDate.now().toString(),
  val hasSeenWarning: Boolean = false
)
```

**SearchRepository Integration:**
```kotlin
class SearchRepository @Inject constructor(
  private val braveService: BraveSearchService,
  private val preferences: WebSearchPreferencesDataStore
) {
  suspend fun search(query: String): Result<String> {
    // Check rate limit
    resetCounterIfNeeded()
    if (!canSearch()) {
      return Result.failure(RateLimitException())
    }

    // Call API
    try {
      val response = braveService.search(query, count = 3, BuildConfig.BRAVE_API_KEY)
      val formatted = formatResults(response.web.results)
      incrementUsageCounter()
      return Result.success(formatted)
    } catch (e: IOException) {
      // Network error - graceful degradation
      return Result.failure(e)
    } catch (e: HttpException) {
      // API error - graceful degradation
      return Result.failure(e)
    }
  }
}
```

## Related Specifications
- See `openspec/specs/privacy-indicators/spec.md` for lock color indication (green=off, amber=on)
- See `openspec/changes/privacy-lock-web-search/spec.md` for web search toggle UI

## Version History
- **v1.0** (PENDING): Brave Search API integration with 5/day limit
  - Related OpenSpec change: `openspec/changes/brave-search-integration/`
  - Commit: TBD
