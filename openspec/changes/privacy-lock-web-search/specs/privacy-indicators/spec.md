# Privacy Indicators Specification (Delta for Web Search Enhancement)

## MODIFIED Requirements

### Requirement: Persistent On-Device Indicator (Updated)
The system SHALL display a persistent on-device indicator on all AI inference screens with multiple visual variants and dynamic color coding based on privacy mode.

#### Scenario: User views chat screen with web search disabled
- WHEN user navigates to any LLM chat screen
- AND web search toggle is OFF
- THEN an icon-only lock badge SHALL appear in the top app bar
- AND the lock SHALL be colored green (#4CAF50) to indicate pure on-device processing
- AND the badge bubble SHALL be 32dp in diameter
- AND NO text SHALL be displayed alongside the lock icon

#### Scenario: User views chat screen with web search enabled
- WHEN user navigates to any LLM chat screen
- AND web search toggle is ON
- THEN an icon-only lock badge SHALL appear in the top app bar
- AND the lock SHALL be colored amber (#FF9800) to indicate hybrid mode
- AND the badge bubble SHALL be 32dp in diameter
- AND NO text SHALL be displayed alongside the lock icon

#### Scenario: Lock color updates in real-time
- WHEN user toggles web search ON or OFF
- THEN the lock color SHALL change within 100ms
- AND the transition SHALL be smooth (no jarring color flash)
- AND the color SHALL accurately reflect current privacy mode

#### Scenario: Lock does not overlap with config button
- WHEN lock indicator is displayed in ModelPageAppBar
- THEN it SHALL NOT overlap with the model configuration gear button
- AND there SHALL be at least 8dp spacing between lock and gear button
- AND both components SHALL remain fully interactive

### Requirement: Four Indicator Variants (Updated from Three)
The system SHALL provide four visual variants of the on-device indicator for different UI contexts.

#### Scenario: Icon-only variant (NEW)
- WHEN indicator is used in top app bars with space constraints
- THEN the ICON_ONLY variant SHALL be used
- AND it SHALL display only a lock icon (🔒) with no text
- AND the bubble SHALL be 32dp in diameter
- AND the color SHALL be dynamic (green or amber) based on web search state

#### Scenario: Compact variant (EXISTING - unchanged)
- WHEN indicator is used in contexts requiring small badge with text
- THEN the COMPACT variant SHALL be used
- AND it SHALL display lock icon with "On-device" text
- AND it SHALL use previous sizing (42dp bubble)

#### Scenario: Prominent variant (EXISTING - unchanged)
- WHEN indicator is used in feature introduction or marketing contexts
- THEN the PROMINENT variant SHALL be used
- AND it SHALL display as a card with "100% On-Device Processing" headline

#### Scenario: Subtle variant (EXISTING - unchanged)
- WHEN indicator is embedded within body text
- THEN the SUBTLE variant SHALL be used
- AND it SHALL display as minimal text-only indicator

## REMOVED Requirements

### Requirement: Offline Capability Badge (REMOVED)
**Reason for removal**: User feedback indicated this is "overkill" and redundant. The on-device indicator already communicates offline capability. Removing reduces UI clutter and prevents confusion.

**Previous requirement text**:
> The system SHALL display an "Works offline" badge when network connectivity is unavailable to emphasize offline functionality.

**Impact**: OfflineCapabilityBadge component removed from PrivacyIndicators.kt and all integration points (ModelPageAppBar.kt).

## ADDED Requirements

### Requirement: Web Search Toggle in Message Input Menu
The system SHALL provide a web search toggle in the message input "+" menu to allow users to optionally enhance responses with current web information.

#### Scenario: User opens + menu
- WHEN user taps the "+" button in the message input area
- THEN a ModalBottomSheet SHALL appear
- AND the sheet SHALL display existing buttons (Camera, Gallery, Files) in the top row
- AND a HorizontalDivider SHALL separate buttons from toggles
- AND a "Web search" toggle SHALL appear below the divider
- AND the toggle SHALL display a globe icon (🌐) and "Web search" label

#### Scenario: User enables web search
- WHEN user taps the web search toggle switch to ON
- THEN the switch SHALL animate to ON state
- AND the webSearchEnabled state SHALL update immediately
- AND the lock indicator color SHALL change to amber within 100ms
- AND the preference SHALL be saved to DataStore for persistence

#### Scenario: User disables web search
- WHEN user taps the web search toggle switch to OFF
- THEN the switch SHALL animate to OFF state
- AND the webSearchEnabled state SHALL update immediately
- AND the lock indicator color SHALL change to green within 100ms
- AND the preference SHALL be saved to DataStore for persistence

#### Scenario: Toggle state persists across app restarts
- WHEN user enables web search and restarts the app
- THEN the toggle SHALL remain in ON state after app launch
- AND the lock SHALL be amber on initial screen load
- AND the webSearchEnabled state SHALL be loaded from DataStore before first render

### Requirement: Lock Color Indicates Privacy Mode
The system SHALL use color coding to visually communicate the current privacy mode to users.

#### Scenario: Pure on-device mode (green lock)
- WHEN web search is disabled
- THEN the lock SHALL be colored green (#4CAF50)
- AND the color SHALL indicate "all processing is local, maximum privacy"
- AND the color SHALL meet WCAG 2.2 Level AA contrast requirements on dark backgrounds

#### Scenario: Hybrid mode with web search (amber lock)
- WHEN web search is enabled
- THEN the lock SHALL be colored amber (#FF9800)
- AND the color SHALL indicate "queries are sent to Google API, reduced privacy"
- AND the color SHALL meet WCAG 2.2 Level AA contrast requirements on dark backgrounds

#### Scenario: Color meaning is consistent
- WHEN user sees the lock indicator in any screen
- THEN green SHALL always mean "100% on-device, no data leaves device"
- AND amber SHALL always mean "hybrid mode, web search active"
- AND the color coding SHALL be consistent across all app screens

### Requirement: State Propagation Through Component Tree
The system SHALL propagate webSearchEnabled state from MessageInputText (source of truth) to ModelPageAppBar (lock display) through intermediate components.

#### Scenario: State flows from toggle to lock
- WHEN webSearchEnabled state changes in MessageInputText
- THEN it SHALL be passed to ChatPanel via callback
- AND ChatPanel SHALL pass it to ChatView
- AND ChatView SHALL pass it to LlmChatScreen
- AND LlmChatScreen SHALL pass it to ModelPageAppBar
- AND ModelPageAppBar SHALL pass it to OnDeviceIndicator
- AND the lock color SHALL update accordingly

#### Scenario: State updates are immediate
- WHEN user toggles web search
- THEN all state updates SHALL complete within 100ms
- AND the UI SHALL remain responsive during updates
- AND no intermediate states SHALL be visible to user

### Requirement: Web Search API Integration
The system SHALL integrate Google Custom Search JSON API to provide web search results when the feature is enabled.

#### Scenario: Web search is enabled and user sends query
- WHEN webSearchEnabled is true
- AND user sends a message
- THEN the system SHALL call Google Custom Search API with the user's query
- AND it SHALL retrieve top 3 search results
- AND it SHALL format results as: "Web search results:\n1. [title] - [snippet]\n2. [title] - [snippet]\n3. [title] - [snippet]"
- AND it SHALL prepend formatted results to the user's message before sending to LLM

#### Scenario: Web search is disabled
- WHEN webSearchEnabled is false
- AND user sends a message
- THEN the system SHALL NOT call Google Custom Search API
- AND the message SHALL be sent directly to on-device LLM without modification

#### Scenario: Web search API fails
- WHEN Google API call fails (network error, rate limit, invalid key)
- THEN the system SHALL log the error
- AND it SHALL send the user's message to LLM without web results
- AND it SHALL NOT display an error to the user (graceful degradation)
- AND the lock color SHALL remain amber (toggle state unchanged)

#### Scenario: API rate limit respected
- WHEN web search has been used 100 times in 24 hours (free tier limit)
- THEN subsequent searches SHALL be skipped
- AND the user's message SHALL be sent to LLM without web results
- AND an optional rate limit warning MAY be logged

### Requirement: First-Time Privacy Warning
The system SHALL display a privacy warning dialog when the user enables web search for the first time to ensure informed consent.

#### Scenario: First web search enablement
- WHEN user taps web search toggle to ON for the first time
- THEN a dialog SHALL appear with privacy warning text:
  - "Enabling web search will send your queries to Google's servers to retrieve current information. This allows the AI to access up-to-date data, but means your queries will leave your device."
- AND the dialog SHALL have "Cancel" and "Enable" buttons
- AND if user taps "Cancel", the toggle SHALL remain OFF
- AND if user taps "Enable", the toggle SHALL turn ON and preference SHALL be saved

#### Scenario: Subsequent web search enablement
- WHEN user has previously seen the privacy warning
- AND user toggles web search ON
- THEN NO dialog SHALL appear
- AND the toggle SHALL update immediately

#### Scenario: "Don't show again" preference
- WHEN privacy warning dialog is displayed
- THEN it SHALL include a "Don't show again" checkbox
- AND if checked and user taps "Enable", the warning SHALL never appear again
- AND the preference SHALL be saved to DataStore

### Requirement: Accessibility Compliance
The system SHALL ensure all new components (ICON_ONLY lock, web search toggle) are accessible to users with disabilities.

#### Scenario: Screen reader users with ICON_ONLY lock
- WHEN user navigates with TalkBack
- AND encounters the ICON_ONLY lock indicator
- THEN the contentDescription SHALL be "On-device processing: Web search off" when green
- AND the contentDescription SHALL be "Hybrid mode: Web search on" when amber

#### Scenario: Screen reader users with web search toggle
- WHEN user navigates to web search toggle with TalkBack
- THEN the toggle SHALL announce "Web search, switch, currently on" or "currently off"
- AND the label SHALL clearly indicate current state

#### Scenario: Color contrast validation
- WHEN lock indicator is displayed in green or amber
- THEN the icon color SHALL have minimum 4.5:1 contrast ratio against the bubble background
- AND the bubble SHALL have minimum 3:1 contrast ratio against the app bar background

## Implementation Notes

**New Component Signature:**
```kotlin
@Composable
fun OnDeviceIndicator(
    modifier: Modifier = Modifier,
    variant: IndicatorVariant = IndicatorVariant.COMPACT,
    isWebSearchEnabled: Boolean = false  // NEW PARAMETER
)
```

**Updated Enum:**
```kotlin
enum class IndicatorVariant {
    ICON_ONLY,   // NEW - 32dp bubble, no text, dynamic color
    COMPACT,     // EXISTING - 42dp bubble with "On-device" text
    PROMINENT,   // EXISTING - Large card format
    SUBTLE       // EXISTING - Minimal text-only
}
```

**Color Constants:**
```kotlin
private val OnDeviceGreen = Color(0xFF4CAF50)  // Pure on-device
private val HybridAmber = Color(0xFFFF9800)    // Web search enabled
```

**State Management:**
```kotlin
// In MessageInputText.kt
var webSearchEnabled by remember { mutableStateOf(false) }

// Load from DataStore on init
LaunchedEffect(Unit) {
    webSearchEnabled = dataStore.loadWebSearchEnabled()
}

// Save to DataStore on change
LaunchedEffect(webSearchEnabled) {
    dataStore.saveWebSearchEnabled(webSearchEnabled)
}
```

**Integration Changes:**
- `ModelPageAppBar.kt`: Remove OfflineCapabilityBadge, change variant to ICON_ONLY, add isWebSearchEnabled parameter
- `MessageInputText.kt`: Add web search toggle to ModalBottomSheet, manage webSearchEnabled state
- `ChatPanel.kt`, `ChatView.kt`, `LlmChatScreen.kt`: Add webSearchEnabled parameter to propagate state
- `PrivacyIndicators.kt`: Add ICON_ONLY variant, implement color logic, add isWebSearchEnabled parameter

## Related Specifications
- See `openspec/specs/conversation-history/spec.md` for database integration patterns
- See `openspec/specs/prompt-engineering/spec.md` for persona injection that web search results will interact with
- See `.bmad/docs/architecture.md` for state management patterns

## Version History
- **v2.0** (PENDING): Privacy lock enhancement + web search toggle
  - Added: ICON_ONLY variant, web search toggle, dynamic color coding, API integration
  - Modified: On-device indicator with 4 variants instead of 3, color becomes dynamic
  - Removed: OfflineCapabilityBadge component entirely
  - Related OpenSpec change: `openspec/changes/privacy-lock-web-search/`
- **v1.0** (2025-12-31): Initial specification from Story 10
  - Commit: acd0e18 "Story 10: Privacy Indicators - Screen Integrations"
