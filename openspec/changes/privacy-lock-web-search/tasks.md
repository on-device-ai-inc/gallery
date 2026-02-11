# Implementation Tasks

## Phase 1: Remove OfflineCapabilityBadge

- [ ] Remove OfflineCapabilityBadge import from ModelPageAppBar.kt
- [ ] Remove OfflineCapabilityBadge() call from ModelPageAppBar.kt actions section (lines ~122)
- [ ] Clean up any unused network connectivity checks
- [ ] Verify no other files reference OfflineCapabilityBadge component

## Phase 2: Simplify Lock Icon

- [ ] Add ICON_ONLY variant to IndicatorVariant enum in PrivacyIndicators.kt
- [ ] Add isWebSearchEnabled parameter to OnDeviceIndicator() composable
- [ ] Implement color logic: Green when isWebSearchEnabled=false, Amber when true
- [ ] Remove text display in ICON_ONLY variant (show only lock icon)
- [ ] Reduce bubble size from 42dp to 32dp for ICON_ONLY variant
- [ ] Update ModelPageAppBar.kt to use ICON_ONLY variant
- [ ] Test lock icon visibility at different screen sizes

## Phase 3: Add Web Search Toggle to + Menu

- [ ] Add webSearchEnabled state variable to MessageInputText.kt
- [ ] Create HorizontalDivider after existing Row in ModalBottomSheet (line ~507)
- [ ] Add Column with toggle row after divider
- [ ] Implement toggle UI with globe icon (🌐) and "Web search" label
- [ ] Add Switch component that updates webSearchEnabled state
- [ ] Pass webSearchEnabled state up to parent (ChatPanel → LlmChatScreen → ModelPageAppBar)
- [ ] Test toggle interaction (ON/OFF transitions smooth)

## Phase 4: State Persistence

- [ ] Create WebSearchPreferences data class
- [ ] Add DataStore configuration for web search preference
- [ ] Implement saveWebSearchEnabled() method
- [ ] Implement loadWebSearchEnabled() method
- [ ] Initialize webSearchEnabled from DataStore on app start
- [ ] Persist state change when toggle is clicked
- [ ] Test: Enable toggle, restart app, verify toggle remains ON

## Phase 5: Lock Color Reactivity

- [ ] Pass webSearchEnabled state from MessageInputText → ChatPanel
- [ ] Pass webSearchEnabled state from ChatPanel → ChatView
- [ ] Pass webSearchEnabled state from ChatView → LlmChatScreen
- [ ] Pass webSearchEnabled state from LlmChatScreen → ModelPageAppBar
- [ ] Update OnDeviceIndicator call with isWebSearchEnabled parameter
- [ ] Test: Toggle web search, verify lock color changes immediately
- [ ] Verify color contrast meets WCAG AA (green on dark, amber on dark)

## Phase 6: Google Custom Search API Integration

- [ ] Add Retrofit dependency to build.gradle if not present
- [ ] Create GoogleSearchService interface with search() method
- [ ] Create SearchResult data class for API response
- [ ] Implement API key configuration (BuildConfig or hardcoded initially)
- [ ] Create SearchRepository for API calls
- [ ] Add Hilt provider for SearchRepository
- [ ] Inject SearchRepository into LlmChatViewModel
- [ ] Modify generateResponse() to check webSearchEnabled state
- [ ] If enabled: Call Google API, format results, prepend to prompt
- [ ] Handle API errors gracefully (no network, rate limit, etc.)

## Phase 7: Privacy & User Education

- [ ] Create first-time web search warning dialog
- [ ] Implement "Don't show again" checkbox for warning
- [ ] Show warning when user enables web search for first time
- [ ] Add tooltip to lock icon explaining color meanings (optional)
- [ ] Update app's privacy policy with web search disclosure (if applicable)

## Phase 8: Testing & Verification

- [ ] Manual test: Lock icon is smaller and doesn't overlap settings button
- [ ] Manual test: Lock is green by default
- [ ] Manual test: Enable web search → Lock turns amber
- [ ] Manual test: Disable web search → Lock turns green
- [ ] Manual test: Restart app with web search ON → Lock is amber on launch
- [ ] Manual test: Web search query returns relevant results
- [ ] Manual test: Web search disabled → Query uses only on-device model
- [ ] Manual test: No "Works offline" badge appears anywhere
- [ ] Verify WCAG AA contrast for both green and amber locks
- [ ] Test on different screen sizes (small phone, tablet)

## Phase 9: Cleanup

- [ ] Remove OfflineCapabilityBadge component from PrivacyIndicators.kt (if no other uses)
- [ ] Remove unused imports
- [ ] Update openspec/specs/privacy-indicators/spec.md with changes
- [ ] Commit changes with descriptive message
- [ ] Create pull request with screenshots

## Acceptance Criteria Checklist

- [ ] AC1: Lock icon is icon-only (no text)
- [ ] AC2: Lock bubble is 32dp (smaller than before)
- [ ] AC3: Lock does not overlap with settings gear button
- [ ] AC4: "Works offline" badge is completely removed
- [ ] AC5: Web search toggle appears in + menu below buttons
- [ ] AC6: Lock is green when web search disabled
- [ ] AC7: Lock is amber when web search enabled
- [ ] AC8: Toggle state persists across app restarts
- [ ] AC9: Web search returns relevant results when enabled
- [ ] AC10: Privacy warning shown on first enable
