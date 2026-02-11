# OnDevice AI - UX Design Specification

**Author:** Gora & Sally (UX Designer)
**Date:** 2025-11-26
**Version:** 1.0

---

## Project & User Foundation

### Project Vision

**OnDevice AI** democratizes AI access by removing the internet barrier. It's the AI that works where cloud AI can't - bringing powerful AI capabilities to users in remote areas and developing regions with limited or unreliable connectivity.

**Core Promise:** Your AI. Your Device. Works Anywhere.

### Target Users

**Primary Audience:**
- General consumers in remote areas and developing regions
- People with unreliable/limited internet access
- Privacy-conscious users who want local-only AI

**User Context:**
- May have limited data plans (data costs are significant portion of income)
- May have intermittent connectivity
- Trained by apps like WhatsApp, PhonePe/EcoCash that work offline
- Value apps that respect their device resources
- May have varying literacy levels - voice input is valuable

### Emotional Design Goals

Users should feel:

| Emotion | UX Implication |
|---------|----------------|
| **Empowered** | UI makes them feel capable, not dependent on cloud/internet |
| **Included** | "You belong here" - no gatekeeping, no complexity walls |
| **Curious** | Invite exploration - "What else can I do?" |
| **Experimental** | Safe to try things - no fear of costs or data loss |
| **Innovative** | They're pioneers of on-device AI |

**The Democratization Frame:**
This isn't "budget AI for poor people." It's: *"Welcome to the future. It runs on YOUR device. You're part of this now."*

### Core Experience

**The ONE thing users will do most:** Chat with AI - send a message, get a response.

**What should be effortless:**
- Starting a new conversation (one tap)
- Finding and continuing old conversations
- Switching between models
- Using voice input

**Critical to get right:** Unified chat experience - text, image attachment, voice input all flowing naturally into one interface.

### Platform

**Primary:** Android mobile (single platform focus)

**Device considerations:**
- May have limited storage
- May have limited RAM (some devices)
- Touch-first interactions
- Portrait orientation primary

---

## Inspiration Analysis

### Apps Users Love & Trust

| App | Why It Works | Pattern for OnDevice AI |
|-----|--------------|------------------------|
| **WhatsApp** | Works on spotty connections, voice notes bypass literacy | Message queuing, voice-first options |
| **PhonePe/EcoCash** | Offline mode, multi-language, error recovery | Offline-first, graceful error handling |
| **Facebook Lite** | <1MB, works on 2G | Efficiency mindset |
| **TikTok Lite** | Offline cached content, data saver | Small footprint awareness |
| **Meesho** | Zero technical barrier, built on trust | Accessible to non-tech-savvy |

### Critical UX Patterns to Apply

1. **Offline-first** - Make offline the celebrated default, not a fallback
2. **Extreme efficiency** - Respect storage, respect resources
3. **Zero-friction** - One tap to chat, no barriers
4. **Error recovery** - Never lose conversations, graceful failures
5. **Voice-first options** - Hold-to-speak bypasses typing barriers
6. **Trust signals** - Privacy indicators throughout
7. **Resource transparency** - Show storage usage, model sizes

### The "Lite App" Mindset

Users are trained by apps that:
- Work when nothing else does
- Never waste their data
- Never waste their storage
- Just. Work.

OnDevice AI should feel like a "Lite" app in spirit - not cheap, but *respectful* of user resources.

---

## Design System Decision

**Chosen System:** Material Design 3 (Material You) with customization

**Rationale:**
- Native to Android and Jetpack Compose
- Familiar to target users (Android users in developing regions)
- Built-in accessibility features
- Fast implementation path
- Customizable to maintain brand identity

**Implementation:**
- Use `androidx.compose.material3` components
- Custom color scheme already defined in `ui/theme/Color.kt`
- Custom typography in `ui/theme/Type.kt`
- Light and dark theme support via `GalleryTheme`

---

## Visual Foundation

### Color Palette (Existing - Retained)

**Primary Colors:**

| Role | Light Mode | Dark Mode | Usage |
|------|------------|-----------|-------|
| Primary | `#0B57D0` | `#A8C7FA` | Main actions, key elements |
| Primary Container | `#D3E3FD` | `#0842A0` | Highlighted areas |
| Secondary | `#00639B` | `#7FCFFF` | Supporting actions |
| Tertiary | `#146C2E` | `#6DD58C` | Success states, "local" indicators |

**Semantic Colors:**

| Role | Light Mode | Dark Mode | Usage |
|------|------------|-----------|-------|
| Background | `#FFFFFF` | `#131314` | App background |
| Surface | `#FFFFFF` | `#131314` | Cards, sheets |
| Error | `#B3261E` | `#F2B8B5` | Error states |
| Success | `#3d860b` | `#A1CE83` | Success states |

**Chat Colors:**

| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| User Bubble | `#32628D` | `#1f3760` |
| Agent Bubble | `#e9eef6` | `#1b1c1d` |

**Brand Gradient:**
- App Title: `#85B1F8` → `#3174F1`

### UX Color Enhancements

| New Use Case | Color | Purpose |
|--------------|-------|---------|
| Privacy/Local indicator | Tertiary Green | Show "running locally" status |
| Storage warning (80%) | Amber `#F59E0B` | Approaching limit |
| Storage critical (95%) | Error Red | At limit |
| Offline/Local badge | Tertiary Green | Celebrate local operation |

### Typography

Using Material Design 3 type scale with system defaults:
- **Headline:** For screen titles
- **Title:** For section headers
- **Body:** For chat messages, content
- **Label:** For buttons, captions

### Spacing System

Material Design 3 default spacing:
- Base unit: 4dp
- Scale: 4, 8, 12, 16, 24, 32, 48, 64dp
- Touch targets: Minimum 48dp

---

## Design Direction

### Chosen Direction: "Clean Conversational"

**Philosophy:** A minimal, conversation-focused interface that celebrates the local/private nature of the AI. Inspired by Claude.ai's clean aesthetic but adapted for mobile-first, offline-first usage patterns.

**Key Characteristics:**

| Aspect | Design Decision |
|--------|-----------------|
| Visual Density | Medium-low - breathing room, not cramped |
| Chrome | Minimal - content takes center stage |
| Navigation | Hamburger drawer (mobile standard) |
| Branding | Subtle - privacy badge more prominent than logo |
| Feedback | Real-time streaming with "Running locally" indicator |

**Why This Direction:**

1. **Matches mental model** - Users expect chat apps to look like WhatsApp/Claude
2. **Reduces cognitive load** - Single interface for all input types
3. **Celebrates the differentiator** - Local/private indicators are first-class citizens
4. **Respects the device** - Clean UI = lighter rendering = better battery

### Screen Structure

```
┌─────────────────────────────────────────┐
│ ☰        [Gemma 3 1B ▼]          ⚙️    │  ← Top Bar
├─────────────────────────────────────────┤
│                                         │
│         Chat messages area              │  ← Scrollable
│         (markdown rendering)            │
│                                         │
├─────────────────────────────────────────┤
│ 🔒 Running privately on your device     │  ← Status Bar
├─────────────────────────────────────────┤
│ [📎] [🎤] Type message...         [⬆️]  │  ← Input Area
└─────────────────────────────────────────┘
```

### Drawer Structure

```
┌─────────────────────────────┐
│ 🤖 OnDevice AI              │  ← Brand
│ 🔒 Private & Local          │  ← Privacy Badge
│ 📊 12 chats • 2.1 MB        │  ← Storage Info
├─────────────────────────────┤
│ [🔍 Search conversations  ] │  ← Search
├─────────────────────────────┤
│ ✏️ New Chat                 │  ← Primary Action
├─────────────────────────────┤
│ TODAY                       │  ← Date Groups
│   Chat about Kotlin...      │
│ YESTERDAY                   │
│   Code review...            │
├─────────────────────────────┤
│ ⚙️ Settings                 │
└─────────────────────────────┘
```

---

## Defining Experience

### The "Magic Moment"

**When users feel the app is special:** The first time they get an AI response while offline. No spinning "connecting..." - it just works. This is when they understand: *"This AI is mine. It's on MY device."*

### Key Experience Principles

1. **Instant Gratification**
   - App opens directly to chat (no splash, no task selection)
   - Model loads in background with subtle progress
   - First message can be typed immediately

2. **Privacy as a Feature, Not a Limitation**
   - "🔒 Running privately" appears during inference
   - No "offline mode" warnings - local IS the mode
   - Privacy badge in drawer reinforces ownership

3. **Conversation Continuity**
   - Conversations persist automatically (silent save)
   - Easy access to history via drawer
   - Continue any conversation with one tap

4. **Multimodal Simplicity**
   - One interface handles text, images, audio
   - Attach image → AI automatically "sees" it
   - Voice input → transcribes to text field

### The "Aha" Moments

| Moment | User Realization |
|--------|------------------|
| First offline response | "Wait, this works without internet?!" |
| Privacy indicator | "My data never leaves my phone" |
| Conversation search | "I can find that chat from last week" |
| Image analysis | "It can SEE what I'm showing it" |
| Model switching | "I can choose which AI to use" |

---

## Core Experience Principles

### 1. Zero-Friction Chat

**Principle:** Remove every barrier between "I want to ask AI something" and "AI responds."

**Implementation:**
- App opens to chat screen (no home/task selection)
- Keyboard available immediately
- Send button always visible
- No confirmation dialogs for normal operations

### 2. Conversation as Memory

**Principle:** Every conversation is valuable and should be preserved and accessible.

**Implementation:**
- Silent auto-save (no "save" button)
- Drawer shows recent conversations
- Search finds content across all chats
- Date grouping (Today, Yesterday, Last 7 Days)

### 3. Privacy Celebration

**Principle:** Local operation isn't a limitation - it's the feature. Celebrate it.

**Implementation:**
- "🔒 Private & Local" badge in drawer header
- "Running privately on your device" during inference
- No internet permission requests during AI operations
- Storage info shows user owns their data

### 4. Respect the Device

**Principle:** Users in our target market have limited resources. Never waste them.

**Implementation:**
- Efficient rendering (no unnecessary animations)
- Clear storage usage visibility
- Model size warnings before download
- Memory warnings for large models

### 5. Forgiving Interface

**Principle:** Users should feel safe to experiment without fear of data loss.

**Implementation:**
- Undo for message deletion
- Confirm before clearing all history
- Regenerate response option
- Never lose a conversation unexpectedly

---

## User Journey Flows

### Flow 1: First-Time User (Happy Path)

```
┌─────────────────┐
│  Install App    │
└────────┬────────┘
         ▼
┌─────────────────┐
│  Open App       │
│  → Chat screen  │
│  → Model loading│
└────────┬────────┘
         ▼
┌─────────────────┐
│  Type message   │
│  "Hello"        │
└────────┬────────┘
         ▼
┌─────────────────┐
│  See response   │
│  + "🔒 Running  │
│    locally"     │
└────────┬────────┘
         ▼
┌─────────────────┐
│  ✨ Magic       │
│  Moment!        │
└─────────────────┘
```

**Time to value:** < 60 seconds

### Flow 2: Continue Previous Conversation

```
Open App → Tap ☰ → See conversation list → Tap conversation → Continue chatting
```

**Steps:** 4 taps to continue

### Flow 3: Ask About an Image

```
Open App → Tap 📎 → Select image → Type question → Send → Get visual analysis
```

**Steps:** 5 taps + typing

### Flow 4: Switch Models

```
Tap model selector → See model list → Tap new model → Wait for load → Continue
```

**Steps:** 3 taps + wait

### Flow 5: Search for Old Conversation

```
Tap ☰ → Tap search → Type query → See results → Tap conversation
```

**Steps:** 5 taps

### Flow 6: Delete Conversation

```
Tap ☰ → Swipe conversation left → Tap "Delete" → Confirm → Done
```

**Steps:** 4 actions

### State Diagram: App States

```
                    ┌──────────────┐
                    │   Loading    │
                    │    Model     │
                    └──────┬───────┘
                           │ Model ready
                           ▼
┌──────────┐       ┌──────────────┐       ┌──────────────┐
│  Drawer  │◄─────►│    Chat      │◄─────►│   Settings   │
│   Open   │  ☰    │    Screen    │  ⚙️   │    Screen    │
└──────────┘       └──────────────┘       └──────────────┘
                           │
                           │ Send message
                           ▼
                    ┌──────────────┐
                    │  Generating  │
                    │   Response   │
                    │ "🔒 Running  │
                    │  locally"    │
                    └──────────────┘
```

---

## Component Library Strategy

### Approach: Material Design 3 + Custom Chat Components

**Strategy:** Use M3 for standard UI, build custom for chat-specific needs.

### Standard M3 Components (Use As-Is)

| Component | Usage | M3 Component |
|-----------|-------|--------------|
| Top App Bar | Main navigation | `TopAppBar` |
| Navigation Drawer | Conversation list | `ModalNavigationDrawer` |
| Text Fields | Message input | `OutlinedTextField` |
| Buttons | Send, actions | `IconButton`, `FilledButton` |
| Cards | Conversation items | `Card` |
| Dialogs | Confirmations | `AlertDialog` |
| Bottom Sheets | Settings, menus | `ModalBottomSheet` |
| Progress Indicators | Loading states | `CircularProgressIndicator` |

### Custom Components (Build New)

#### 1. ChatBubble
```kotlin
@Composable
fun ChatBubble(
    message: ChatMessage,
    isUser: Boolean,
    onCopy: () -> Unit,
    onRegenerate: (() -> Unit)? = null
)
```
- Rounded corners (user: right-aligned, AI: left-aligned)
- Markdown rendering for AI responses
- Copy button on long-press or hover
- Regenerate button for AI messages

#### 2. CodeBlock
```kotlin
@Composable
fun CodeBlock(
    code: String,
    language: String?,
    onCopy: () -> Unit
)
```
- Syntax highlighting
- Copy button (prominent, one-tap)
- Language label
- Horizontal scroll for long lines

#### 3. PrivacyBadge
```kotlin
@Composable
fun PrivacyBadge(
    isProcessing: Boolean = false
)
```
- Shows "🔒 Private & Local" (static)
- Shows "🔒 Running privately..." (during inference)
- Tertiary green color

#### 4. StorageInfo
```kotlin
@Composable
fun StorageInfo(
    conversationCount: Int,
    totalSizeMb: Float
)
```
- Shows "12 chats • 2.1 MB"
- Color changes at 80%/95% thresholds

#### 5. ModelSelector
```kotlin
@Composable
fun ModelSelector(
    currentModel: Model,
    availableModels: List<Model>,
    onModelSelected: (Model) -> Unit
)
```
- Dropdown in top bar
- Shows model name + size
- Indicates download status
- Memory warning for large models

#### 6. ConversationListItem
```kotlin
@Composable
fun ConversationListItem(
    conversation: ConversationThread,
    onClick: () -> Unit,
    onDelete: () -> Unit
)
```
- Title (truncated)
- Preview of last message
- Timestamp
- Swipe-to-delete

#### 7. VoiceInputButton
```kotlin
@Composable
fun VoiceInputButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
)
```
- Hold-to-speak interaction
- Visual feedback during recording
- Transcription to text field

---

## UX Pattern Decisions

### Navigation Pattern

**Decision:** Hamburger Drawer (Side Navigation)

**Rationale:**
- Mobile standard for chat apps (WhatsApp, Messenger, Claude)
- Maximizes chat content area
- Familiar to target users
- Works well with conversation list

**Implementation:**
- Hamburger icon (☰) in top-left
- Swipe from left edge also opens drawer
- Drawer contains: Brand area, search, new chat, conversation list, settings

### Message Input Pattern

**Decision:** Fixed Bottom Input Bar

**Rationale:**
- Standard chat app pattern
- Thumb-friendly on mobile
- Clear visual hierarchy

**Components:**
```
┌──────────────────────────────────────────┐
│ [📎] [🎤] Type message...          [⬆️]  │
└──────────────────────────────────────────┘
```
- Attachment button (📎) - opens media picker
- Voice button (🎤) - hold to speak
- Text field - expands with content (max 4 lines)
- Send button (⬆️) - sends message

### Feedback & Loading Pattern

**Decision:** Streaming with Privacy Indicator

**During AI Response:**
```
┌──────────────────────────────────────────┐
│ 🔒 Running privately on your device...   │
│ ████░░░░░░░░                             │
└──────────────────────────────────────────┘
```
- Show progress indicator
- Display privacy message
- Stream tokens as they generate

### Error Handling Pattern

**Decision:** Inline Errors with Recovery Actions

**Types:**
| Error | Display | Recovery |
|-------|---------|----------|
| Model not loaded | Toast + top banner | "Tap to load" button |
| Generation failed | Inline error message | "Retry" button |
| Storage full | Dialog | "Manage storage" link |
| Low memory | Warning banner | "Switch to smaller model" |

### Conversation List Pattern

**Decision:** Grouped by Date with Swipe Actions

**Groups:**
- Today
- Yesterday
- Last 7 Days
- Older (by month)

**Item Layout:**
```
┌────────────────────────────────────────┐
│ Chat about Kotlin...                   │
│ "How do I implement..."    2:30 PM     │
└────────────────────────────────────────┘
```
- Title (first line, bold)
- Preview (second line, muted)
- Timestamp (right-aligned)

**Actions:**
- Tap → Open conversation
- Swipe left → Delete (with undo)
- Long press → Context menu (future: Copy, Share, Pin)

### Model Selection Pattern

**Decision:** Dropdown in Top Bar

**Display:**
```
┌─────────────────┐
│ Gemma 3 1B   ▼  │
└─────────────────┘
```

**Dropdown:**
```
┌─────────────────────────────────────┐
│ ✓ Gemma 3 1B (584 MB)     Loaded   │
│   Gemma 3n E2B (3.4 GB)   Download │
│   Gemma 3n E4B (4.7 GB)   ⚠️ 6GB   │
└─────────────────────────────────────┘
```
- Checkmark for active model
- Size indicator
- Status (Loaded, Download, memory warning)

### Empty States Pattern

**Decision:** Friendly, Actionable Empty States

**No Conversations:**
```
┌────────────────────────────────────────┐
│                                        │
│            💬                          │
│     Start your first conversation      │
│     Your AI runs entirely on           │
│     your device - no internet needed   │
│                                        │
│     [Type a message below]             │
│                                        │
└────────────────────────────────────────┘
```

**Search No Results:**
```
┌────────────────────────────────────────┐
│     No conversations found for         │
│     "kotlin coroutines"                │
│                                        │
│     Try a different search term        │
└────────────────────────────────────────┘
```

---

## Responsive & Accessibility Strategy

### Screen Size Strategy

**Primary Target:** Standard Android phones (360-412dp width)

**Approach:** Mobile-first, single-column layout

| Screen Size | Handling |
|-------------|----------|
| Small (< 360dp) | Reduce padding, smaller fonts |
| Standard (360-412dp) | Default design |
| Large (> 412dp) | Wider chat bubbles, more padding |
| Tablet (> 600dp) | Future: Side-by-side drawer |

### Orientation Support

**Primary:** Portrait

**Landscape:** Supported but not optimized
- Chat content scrolls
- Input bar remains at bottom
- Drawer opens as overlay

### Text Scaling

**Support:** 100% - 200% system text size

**Implementation:**
- Use `sp` for all text sizes
- Test at 200% scale
- Ensure no text truncation at max scale
- Adjust layouts for larger text

### Touch Targets

**Minimum Size:** 48dp x 48dp

**Applied To:**
- All buttons (send, attach, voice)
- Conversation list items
- Model selector dropdown
- Menu items

### Color Contrast

**Requirements:** WCAG 2.1 AA compliance

| Element | Requirement | Status |
|---------|-------------|--------|
| Body text on background | 4.5:1 | ✓ Verified |
| Large text on background | 3:1 | ✓ Verified |
| Icons on background | 3:1 | ✓ Verified |
| User bubble text | 4.5:1 | ✓ Verified |
| Agent bubble text | 4.5:1 | ✓ Verified |

### Screen Reader Support (TalkBack)

**Requirements:**
- All interactive elements have content descriptions
- Logical reading order
- Focus management during navigation
- Announcements for dynamic content

**Implementation:**
```kotlin
// Example: Send button
IconButton(
    onClick = { sendMessage() },
    modifier = Modifier.semantics {
        contentDescription = "Send message"
    }
)

// Example: AI response streaming
LaunchedEffect(responseText) {
    // Announce completion
    if (isComplete) {
        announceForAccessibility("AI response complete")
    }
}
```

### Motion & Animation

**Principles:**
- Respect `reduceMotion` system setting
- Keep animations under 300ms
- No essential information conveyed only through animation
- Provide static alternatives

**Implementation:**
```kotlin
val reduceMotion = LocalReducedMotion.current
val animationDuration = if (reduceMotion) 0 else 200
```

### Keyboard Navigation

**For External Keyboards:**
- Tab order follows logical flow
- Enter sends message
- Escape closes drawer/dialogs
- Arrow keys navigate conversation list

### Localization Readiness

**RTL Support:**
- Use `start`/`end` instead of `left`/`right`
- Mirror layouts for RTL languages
- Test with Arabic/Hebrew

**String Externalization:**
- All user-facing strings in `strings.xml`
- Parameterized strings for dynamic content
- Pluralization support

---

## Completion Summary

### UX Design Specification Status

| Section | Status | Notes |
|---------|--------|-------|
| Project & User Foundation | Complete | Target users, emotional goals defined |
| Inspiration Analysis | Complete | WhatsApp, EcoCash patterns applied |
| Design System Decision | Complete | Material Design 3 selected |
| Visual Foundation | Complete | Existing color palette retained |
| Design Direction | Complete | "Clean Conversational" chosen |
| Defining Experience | Complete | Magic moment and principles defined |
| Core Experience Principles | Complete | 5 principles documented |
| User Journey Flows | Complete | 6 key flows mapped |
| Component Library Strategy | Complete | 7 custom components specified |
| UX Pattern Decisions | Complete | Navigation, input, feedback patterns |
| Responsive & Accessibility | Complete | WCAG AA, TalkBack support |

### Key Design Decisions Summary

1. **Unified Chat Interface** - Single interface for text, image, audio
2. **Hamburger Drawer Navigation** - Familiar pattern, conversation list
3. **Privacy Celebration** - "Running privately" is a feature, not a disclaimer
4. **Material Design 3** - Native Android, familiar to target users
5. **Existing Color Palette** - Retain current Google Blue-based theme
6. **Date-Grouped Conversations** - Easy to find recent chats
7. **Swipe-to-Delete** - Standard mobile pattern
8. **Streaming with Privacy Indicator** - Real-time feedback with local emphasis

### Implementation Priorities

**MVP (This Release):**
1. Unified chat screen (eliminate task tiles)
2. Drawer with conversation history
3. Model selector in top bar
4. Privacy badge and "Running locally" indicator
5. Code block copy button
6. Markdown rendering

**Growth (Next Release):**
1. Conversation search
2. Voice input (hold-to-speak)
3. Tutorial cards during model loading
4. Export conversations

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Time to first message | < 60 seconds | From app install |
| Conversation retrieval | < 4 taps | To continue old chat |
| Privacy visibility | 100% | Users see "running locally" |
| Accessibility score | 100% TalkBack | All elements labeled |

### Files Referenced

- PRD: `docs/prd.md`
- Brainstorming: `docs/brainstorming-session-results-2025-11-26.md`
- Color Theme: `app/src/main/java/ai/ondevice/app/ui/theme/Color.kt`
- Typography: `app/src/main/java/ai/ondevice/app/ui/theme/Type.kt`

---

_This UX Design Specification was created collaboratively through visual exploration and informed decision-making._

_Created by Gora with Sally (UX Designer) facilitation._
