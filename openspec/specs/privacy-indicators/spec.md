# Privacy Differentiation Indicators Specification

## Purpose
This specification covers the on-device privacy indicator components that visually communicate the app's privacy-first, offline-capable nature to users across all inference screens and onboarding experiences.

## Requirements

### Requirement: Persistent On-Device Indicator
The system SHALL display a persistent "On-device" indicator on all AI inference screens to communicate that processing happens locally.

#### Scenario: User views any chat screen
- WHEN user navigates to any LLM chat, image analysis, or audio processing screen
- THEN a compact "🔒 On-device" badge SHALL appear in the top app bar

#### Scenario: User opens model configuration screen
- WHEN user accesses model parameters or configuration
- THEN the on-device indicator SHALL remain visible in the header

### Requirement: Offline Capability Badge
The system SHALL display an "Works offline" badge when network connectivity is unavailable to emphasize offline functionality.

#### Scenario: Network becomes unavailable
- WHEN device loses internet connectivity
- THEN an "Works offline" badge SHALL animate into view in the top app bar
- AND the badge SHALL appear within 5 seconds of connectivity loss

#### Scenario: Network becomes available
- WHEN device regains internet connectivity
- THEN the "Works offline" badge SHALL animate out of view
- AND the badge SHALL disappear within 5 seconds of connectivity restoration

#### Scenario: App starts without network
- WHEN app launches while device is offline
- THEN the "Works offline" badge SHALL be visible immediately on all inference screens

### Requirement: First-Launch Privacy Advantages
The system SHALL emphasize privacy and performance advantages during the onboarding experience to communicate value proposition.

#### Scenario: User sees onboarding screen
- WHEN user launches the app for the first time
- THEN a PrivacyAdvantagesCard SHALL appear after the app title and intro text
- AND the card SHALL display 4 key advantages: Complete Privacy, Unlimited Conversations, Instant Responses, Works Offline
- AND each advantage SHALL have an accompanying icon for visual recognition

#### Scenario: Returning user
- WHEN user has already completed onboarding
- THEN the PrivacyAdvantagesCard SHALL NOT appear on the home screen

### Requirement: Competitive Comparison Display
The system SHALL provide a side-by-side comparison of on-device vs cloud AI to educate users on privacy benefits.

#### Scenario: User accesses Settings Privacy section
- WHEN user navigates to Settings → Privacy & Security
- THEN a CompetitiveComparisonCard SHALL be visible
- AND the card SHALL show 4 comparison rows: Privacy, Cost, Speed, Offline capability
- AND on-device advantages SHALL be marked with ✓ indicators
- AND cloud AI limitations SHALL be marked with ✗ indicators

### Requirement: Three Indicator Variants
The system SHALL provide three visual variants of the on-device indicator for different UI contexts.

#### Scenario: Compact spaces (app bars)
- WHEN indicator is used in top app bars or toolbars
- THEN the COMPACT variant SHALL be used
- AND it SHALL display as a small badge with lock icon and "On-device" text

#### Scenario: Feature screens (prominent display)
- WHEN indicator is used in feature introduction or marketing contexts
- THEN the PROMINENT variant SHALL be used
- AND it SHALL display as a card with "100% On-Device Processing" headline and explanatory subtitle

#### Scenario: Inline text contexts
- WHEN indicator is embedded within body text or descriptions
- THEN the SUBTLE variant SHALL be used
- AND it SHALL display as minimal text-only indicator

### Requirement: Material 3 Design Compliance
The system SHALL implement all privacy indicators using Material 3 design system for consistency with the app's visual language.

#### Scenario: Color schemes
- WHEN privacy indicators are rendered
- THEN on-device indicators SHALL use primary or tertiary container colors
- AND offline badges SHALL use secondary container colors to differentiate
- AND all color combinations SHALL meet WCAG 2.2 Level AA contrast requirements

#### Scenario: Typography and spacing
- WHEN privacy indicators are displayed
- THEN text SHALL use Material 3 typography scale (bodyLarge, bodyMedium, etc.)
- AND spacing SHALL follow 8dp grid system
- AND icons SHALL be properly sized for their context (14dp, 16dp, 20dp)

### Requirement: Accessibility Support
The system SHALL ensure privacy indicators are accessible to users with disabilities.

#### Scenario: Screen reader users
- WHEN user navigates with TalkBack or other screen readers
- THEN all indicators SHALL have semantic contentDescription values
- AND icon-only indicators SHALL include descriptive text for screen readers

#### Scenario: Visual contrast
- WHEN user views indicators in any theme (light/dark)
- THEN indicators SHALL maintain minimum 4.5:1 contrast ratio for text
- AND background/foreground color combinations SHALL be distinguishable

## Implementation Notes

**Component Locations:**
- All components defined in: `app/src/main/java/ai/ondevice/app/ui/common/PrivacyIndicators.kt`
- Integration points:
  - `app/src/main/java/ai/ondevice/app/ui/common/ModelPageAppBar.kt` (COMPACT + offline badge)
  - `app/src/main/java/ai/ondevice/app/ui/home_archived/home/HomeScreen.kt` (PrivacyAdvantagesCard)
  - `app/src/main/java/ai/ondevice/app/ui/settings/SettingsScreen.kt` (CompetitiveComparisonCard)

**Component Signatures:**
```kotlin
@Composable
fun OnDeviceIndicator(
    modifier: Modifier = Modifier,
    variant: IndicatorVariant = IndicatorVariant.COMPACT
)

@Composable
fun OfflineCapabilityBadge(modifier: Modifier = Modifier)

@Composable
fun PrivacyAdvantagesCard(modifier: Modifier = Modifier)

@Composable
fun CompetitiveComparisonCard(modifier: Modifier = Modifier)
```

**Network Detection:**
- OfflineCapabilityBadge uses `ConnectivityManager` with `LaunchedEffect` polling every 5 seconds
- Checks for `NetworkCapabilities.NET_CAPABILITY_INTERNET`
- Animated transitions using `AnimatedVisibility` with `expandVertically`/`shrinkVertically`

## Related Specifications
- See `openspec/specs/premium-ux/spec.md` for overall UX guidelines
- See `.bmad/docs/stories/story-10-privacy-indicators.md` for original story ACs

## Version History
- **v1.0** (2025-12-31): Initial specification created from implemented Story 10
  - Integrated into ModelPageAppBar, HomeScreen, SettingsScreen
  - Commit: acd0e18 "Story 10: Privacy Indicators - Screen Integrations"
