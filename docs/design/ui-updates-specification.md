# OnDevice AI - UI Updates Specification

> **Living Document** - Updated after each implementation
> **Last Updated:** 2025-11-27
> **Status:** VALIDATED - Ready for Implementation

---

## Overview

| Update | Description | Status | Complexity |
|--------|-------------|--------|------------|
| #1 | Welcome Screen Logo | IMPLEMENTED | Low |
| #2 | Model Selector Alignment & Header | IMPLEMENTED | Low |
| #3 | Voice Mic Button & Flow | IMPLEMENTED | High |
| #4 | Post-Prompt Visual | IMPLEMENTED | Low |
| #5 | Response Action Buttons | IMPLEMENTED | Medium |
| #6 | Side Panel (Drawer) Cleanup | IMPLEMENTED | Medium |
| #7 | Chats Screen Redesign | IMPLEMENTED | High |
| #8 | Chat History Navigation | IMPLEMENTED | Medium |
| #9 | Response Disclaimer | IMPLEMENTED | Low |
| #10 | Update Model Allow Lists | IMPLEMENTED | Low |

---

## Recommended Implementation Order

1. #1 Welcome Logo
2. #9 Response Disclaimer
3. #10 Model Allowlist
4. #2 Header Alignment
5. #6 Side Panel Cleanup
6. #5 Action Buttons
7. #4 Post-Prompt Visual
8. #8 History Navigation
9. #7 Chats Screen Redesign
10. #3 Voice Mic Button

---

## Update #1: Welcome Screen Logo

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Splash Screen Type** | Android System SplashScreen API | `MainActivity.kt:68` |
| **Welcome Icon** | Sparkle emoji (✨) | `ChatPanel.kt:288` |
| **Greeting Text** | Time-dynamic: "How can I help you this $greeting?" | `ChatPanel.kt:280-308` |
| **Greeting Logic** | morning (5-11), afternoon (12-17), evening (else) | `ChatPanel.kt:280-308` |

### Target State

| Aspect | Decision |
|--------|----------|
| Change | Replace sparkle emoji (✨) with OnDevice logo |
| Position | Centered, same location as sparkle |
| Text | Keep time-dynamic greeting |
| Logo Asset | `res/drawable/logo.xml` (already exists) |

### Implementation

**File:** `ChatPanel.kt:288`

```kotlin
// Current:
Text("✨", fontSize = 48.sp)

// Target:
Image(
    painter = painterResource(id = R.drawable.logo),
    contentDescription = "OnDevice Logo",
    modifier = Modifier.size(64.dp)
)
```

---

## Update #2: Model Selector Alignment & Header

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **App Bar Type** | `CenterAlignedTopAppBar` | `ModelPageAppBar.kt:64-175` |
| **Title** | Task label (e.g., "AI Chat") displayed prominently | `ModelPageAppBar.kt:87` |
| **Model Selector** | `ModelPickerChip` below title | `ModelPageAppBar.kt:118` |
| **Left Navigation** | Menu button (hamburger) | `ModelPageAppBar.kt:131` |
| **Right Actions** | Config button + Reset session button | `ModelPageAppBar.kt:147, 159` |

### Target State

| Aspect | Decision |
|--------|----------|
| Remove | "AI Chat" title text only |
| Layout | Model selector moves up to center of header row |
| Keep | Config button (⚙️) in current position |
| Keep | All other elements unchanged |
| Long names | Truncate with "..." |

### Visual

```
Current:
┌─────────────────────────────────┐
│  ☰        "AI Chat"          ⚙️  │  ← Title row
│        [Gemma-3n-E2B-it]        │  ← Model selector below
└─────────────────────────────────┘

Target:
┌─────────────────────────────────┐
│  ☰    [Gemma-3n-E2B...]    ⚙️ ➕ │  ← Single row, no title
└─────────────────────────────────┘
```

### Implementation

**File:** `ModelPageAppBar.kt` - Remove title, adjust layout

---

## Update #3: Voice Mic Button & Flow

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Mic Button** | Inside "+" dropdown menu | `MessageInputText.kt:76` |
| **Recording UI** | `AudioRecorderPanel` with red styling | `AudioRecorderPanel.kt:92-250` |
| **Audio Format** | PCM 16-bit mono | `AudioRecorderPanel.kt:72-73` |

### Target State

| Aspect | Decision |
|--------|----------|
| Mic Position | Right side of input bar (always visible) |
| Mic Visibility | Hidden when user types, send button replaces it |
| Recording UI Colors | Grey/black scheme (not red) |
| Transcription | On-device (or Android OS SpeechRecognizer) |
| Failed Transcription | Retry with cached audio |
| Success Flow | Show transcribed text in input field → user can edit → send |

### Visual

```
Input bar:
┌─────────────────────────────────┐
│ [+]  Type message...         🎤 │  ← Mic on right
└─────────────────────────────────┘

When typing:
┌─────────────────────────────────┐
│ [+]  Hello world...          ➤  │  ← Send replaces mic
└─────────────────────────────────┘

Recording UI (grey scheme):
┌─────────────────────────────────┐
│  ✕   ∿∿∿∿∿∿∿∿∿   0:05    ▲     │
│ grey  waveform    timer  send   │
└─────────────────────────────────┘
```

### Edge Cases

- Permission handling for microphone
- Recording time limit (`MAX_AUDIO_CLIP_DURATION_SEC`)
- Cache audio on failure for retry

### Implementation

**Files:**
1. `MessageInputText.kt` - Add dedicated mic button to right
2. `AudioRecorderPanel.kt` - Update colors from red to grey scheme

---

## Update #4: Post-Prompt Visual

### Status: IMPLEMENTED (2025-11-27)

### Current State

Voice input processed via `AudioRecorderPanel`, sent as audio or transcribed.

### Target State

| Aspect | Decision |
|--------|----------|
| Display | Voice-transcribed text shows as normal message |
| Indicator | Small mic icon (🎤) on LEFT of message text |
| Integration | No other visual distinction from typed messages |

### Visual

```
Typed message:
┌─────────────────────────────────┐
│                    User message │
└─────────────────────────────────┘

Voice message:
┌─────────────────────────────────┐
│              🎤 Voice message   │  ← Mic icon prefix
└─────────────────────────────────┘
```

### Implementation

**File:** `ChatMessageText` data class - Add `isVoiceInput: Boolean` flag
**File:** Message bubble composable - Render mic icon conditionally

---

## Update #5: Response Action Buttons

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Buttons** | Copy, Regenerate, Show Stats | `ChatPanel.kt:470-520` |
| **Style** | Pill-shaped with labels + icons | `MessageActionButton.kt` |

### Target State

| Aspect | Decision |
|--------|----------|
| Remove | "Show stats" button |
| Keep | Copy, Regenerate (icon-only, cleaner) |
| Add | Share button |
| Add | Play button (TTS) |
| Button Order | Copy, Regenerate, Share, Play |
| Style | Icon-only with good spacing |

### Share Functionality

| Aspect | Decision |
|--------|----------|
| Format | Markdown |
| Options | User chooses: Last response OR Entire conversation |
| UI | Bottom sheet with radio options |

### Play (TTS) Functionality

| Aspect | Decision |
|--------|----------|
| Engine | On-device (leverage existing transcribe model) |
| Availability | Only after response completes (not during streaming) |
| States | Pause/Resume toggle |

### Visual

```
Action buttons:
[ 📋 ] [ 🔄 ] [ 📤 ] [ ▶️ ]
 Copy  Regen  Share  Play

Share bottom sheet:
┌─────────────────────────────┐
│  Share as Markdown          │
│  ─────────────────────────  │
│  ○ Last response only       │
│  ○ Entire conversation      │
│                             │
│        [ Share ]            │
└─────────────────────────────┘
```

### Implementation

**File:** `ChatPanel.kt:470-520` - Restructure action buttons

---

## Update #6: Side Panel (Drawer) Cleanup

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Header** | "OnDevice AI" | `ChatMenuSheet.kt:87` |
| **Privacy Badge** | Lock icon + "Private & Local" | `ChatMenuSheet.kt:93-109` |
| **Chat Counter** | "$conversationCount chats" | `ChatMenuSheet.kt:113` |
| **New Chat** | Edit icon + "New chat" | `ChatMenuSheet.kt:138-143` |
| **History** | History icon + "View conversation history" | `ChatMenuSheet.kt:151+` |

### Target State

| Aspect | Decision |
|--------|----------|
| Header | "OnDevice" (shortened) |
| Remove | "Private & Local" section |
| Remove | Chat count |
| Remove | Horizontal divider |
| Keep | "New chat" button with icon |
| Rename | "View conversation history" → "Chats" |
| Add | "Recents" section |
| Recents Count | Last 10 chats |
| Recents Tap | Opens chat directly |
| Empty State | Show "No recent chats" |

### Visual

```
Current:                          Target:
┌─────────────────────────┐      ┌─────────────────────────┐
│ OnDevice AI             │      │ OnDevice                │
│ 🔒 Private & Local      │      │                         │
│ 7 chats                 │      │ ✏️ New chat             │
│ ─────────────────────── │      │ 💬 Chats                │
│ ✏️ New chat             │      │                         │
│ 🕐 View conversation    │      │ Recents                 │
│    history              │      │ ├─ How do I cook...     │
└─────────────────────────┘      │ ├─ Explain quantum...   │
                                 │ └─ Write a poem...      │
                                 └─────────────────────────┘
```

### Implementation

**File:** `ChatMenuSheet.kt` - Restructure layout

---

## Update #7: Chats Screen Redesign

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Header** | "Chat History" | `ConversationListScreen.kt:46` |
| **Search** | "Search conversations..." | `ConversationListScreen.kt:74` |
| **List Style** | Card/bubble containers | `ConversationListScreen.kt:106-112` |
| **Delete** | Visible trash icon on each item | Direct button |

### Target State

| Aspect | Decision |
|--------|----------|
| Header | "Chats" |
| Search | "Search Chats" |
| List Style | Flat list (no cards) |
| Item Content | Title + timestamp only |
| Delete Button | Hidden (long-press or ⋮ menu) |
| Access Options | Long-press OR ⋮ menu (accessibility) |

### Context Menu Options

| Option | Icon | Action |
|--------|------|--------|
| Rename | ✏️ | Dialog with text field |
| Star | ⭐ | Pin to top + filterable |
| Delete | 🗑️ | Confirmation dialog required |

### Additional Features

| Feature | Decision |
|---------|----------|
| Multi-select | Yes, bulk operations supported |
| Star behavior | Pinned to top + can filter |

### Visual

```
┌─────────────────────────────────┐
│ ← Chats                         │
├─────────────────────────────────┤
│ 🔍 Search Chats                 │
├─────────────────────────────────┤
│ ⭐ How do I cook pasta...     ⋮ │  ← Starred (pinned)
│ 19 minutes ago                  │
│─────────────────────────────────│
│ Explain quantum computing     ⋮ │
│ 2 hours ago                     │
└─────────────────────────────────┘

Context menu (long-press or ⋮):
┌─────────────────┐
│ ✏️ Rename       │
│ ⭐ Star         │
│ 🗑️ Delete       │
└─────────────────┘
```

### Implementation

**File:** `ConversationListScreen.kt` - Major redesign
**New Component:** `ConversationContextMenu` for long-press/menu options

---

## Update #8: Chat History Navigation

### Status: IMPLEMENTED (2025-11-27)

### Current State

| Aspect | Implementation | File |
|--------|---------------|------|
| **Thread View** | `ConversationDetailScreen` | `ConversationDetailScreen.kt` |
| **Continue Chat** | FAB with play icon | `ConversationDetailScreen.kt:73` |
| **Issue** | May show read-only / missing model responses | Potential bug |

### Target State

| Aspect | Decision |
|--------|----------|
| Navigation | One-step: Tap chat → Directly in active chat mode |
| Display | Full conversation (user + model messages) |
| Input | Active, ready to continue |
| Model Unavailable | Use different model + show warning |
| Context Limits | Truncate older messages + warn user |

### Warning Dialogs

```
If model unavailable:
┌─────────────────────────────────┐
│ ⚠️ Original model (Gemma-3n)    │
│ is not available. Using         │
│ Gemma-2B instead.               │
│              [ OK ]             │
└─────────────────────────────────┘

If context truncated:
┌─────────────────────────────────┐
│ ℹ️ Older messages were trimmed  │
│ to fit the model's context.     │
│              [ OK ]             │
└─────────────────────────────────┘
```

### Implementation

**Files:**
- `ConversationListScreen.kt` - Navigate directly to active chat
- `ChatViewModel.kt` - Verify `loadConversation()` loads all messages
- May need bug fixes for missing model responses

---

## Update #9: Response Disclaimer

### Status: IMPLEMENTED (2025-11-27)

### Current State

No disclaimer exists after model responses.

### Target State

| Aspect | Decision |
|--------|----------|
| Text | "OnDevice can make mistakes. Please double check responses." |
| Position | Below action buttons, RIGHT ALIGNED |
| Style | Subtle (small font, muted color) |
| Frequency | First response + periodic (every N responses) |
| Timing | After response completes only (not during streaming) |
| Dismissible | No, always visible |

### Visual

```
┌─────────────────────────────────┐
│ Model response text here...     │
│                                 │
│ [ 📋 ] [ 🔄 ] [ 📤 ] [ ▶️ ]     │
│                                 │
│    OnDevice can make mistakes.  │
│   Please double check responses.│  ← Right-aligned, subtle
└─────────────────────────────────┘
```

### Implementation

**File:** `ChatPanel.kt` - Add Text composable after action buttons
- Track response count per session
- Show on responses 1, 5, 10, 20, etc. (configurable interval)

---

## Update #10: Update Model Allow Lists

### Status: IMPLEMENTED (2025-11-27)

### Current State

~3 models available in current allowlist.

### Target State

| Aspect | Decision |
|--------|----------|
| Source | Upstream commit `3900fbc` |
| File | `model_allowlists/1_0_8.json` |
| Action | Merge new models into local allowlist |

### New Models (6 total)

| Model | Size | Type | Min RAM |
|-------|------|------|---------|
| Gemma-3n-E2B-it | 3.6GB | Text/Vision/Audio | 8GB |
| Gemma-3n-E4B-it | 4.9GB | Text/Vision/Audio | 12GB |
| Gemma3-1B-IT | 584MB | Text only | 6GB |
| Qwen2.5-1.5B-Instruct | 1.6GB | Text only | 6GB |
| Phi-4-mini-instruct | 3.9GB | Text only | 6GB |
| DeepSeek-R1-Distill-Qwen-1.5B | 1.8GB | Text only | 6GB |

### Reference

https://github.com/google-ai-edge/gallery/commit/3900fbc3d23c42f5087db6234cb436f6aef7a73e

### Implementation

Fetch and merge `1_0_8.json` into local `model_allowlist.json`

---

## Change Log

| Date | Update | Change |
|------|--------|--------|
| 2025-11-27 | Initial | Document created from codebase analysis |
| 2025-11-27 | All | Validated all 10 updates via brainstorming session |
| 2025-11-27 | #1 | Implemented - Replaced sparkle emoji with logo in ChatPanel.kt |
| 2025-11-27 | #9 | Implemented - Added disclaimer after action buttons for agent responses |
| 2025-11-27 | #10 | Implemented - Updated versionName to 1.0.8 (auto-fetches new models) |
| 2025-11-27 | #2 | Implemented - Removed task label, model picker now centered in header |
| 2025-11-27 | #6 | Implemented - Side panel cleanup with Recents section |
| 2025-11-27 | #5 | Implemented - Icon-only buttons (Copy, Regenerate, Share, Play), Share bottom sheet, TTS playback |
| 2025-11-27 | #4 | Implemented - Added isVoiceInput flag to ChatMessageText, mic icon prefix in MessageBodyText (activated by Update #3 transcription) |
| 2025-11-27 | #8 | Implemented - One-step navigation: tap chat in history → loads directly in active chat mode via savedStateHandle |
| 2025-11-27 | #7 | Implemented - Flat list design, context menu (⋮ + long-press), Rename/Star/Delete options, starred items pinned to top |
| 2025-11-27 | #3 | Implemented - Dedicated mic button (shows when text empty), Android SpeechRecognizer transcription, grey recording indicator, isVoiceInput flag integration |

---

## Key Files Reference

| Component | Primary File |
|-----------|-------------|
| Welcome/Greeting | `ChatPanel.kt:280-308` |
| Header/App Bar | `ModelPageAppBar.kt` |
| Input Area | `MessageInputText.kt` |
| Voice Recording | `AudioRecorderPanel.kt` |
| Action Buttons | `ChatPanel.kt:470-520`, `MessageActionButton.kt` |
| Side Panel | `ChatMenuSheet.kt` |
| Chats List | `ConversationListScreen.kt` |
| Chat Detail | `ConversationDetailScreen.kt` |
| Chat ViewModel | `ChatViewModel.kt` |
| Model Config | `ModelManagerViewModel.kt`, `model_allowlist.json` |
