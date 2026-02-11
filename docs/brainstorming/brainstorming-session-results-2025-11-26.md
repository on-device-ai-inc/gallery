# Brainstorming Session Results

**Session Date:** 2025-11-26
**Facilitator:** Business Analyst Mary
**Participant:** Gora

## Session Start

**Approach Selected:** AI-Recommended Techniques
**Techniques Planned:**
1. Analogical Thinking (creative) - Map Claude.ai features to OnDevice
2. Resource Constraints (structured) - Work within on-device limits
3. First Principles Thinking (creative) - Find core UX truths
4. SCAMPER Method (structured) - Systematic UI improvements

**Context:** Redesigning OnDevice AI UX/UI to match Claude.ai experience while leveraging on-device advantages (Gemini Nano, Gemini Multimodal, Stable Diffusion, Whisper)

## Executive Summary

**Topic:** Redesigning OnDevice AI's UX/UI to match Claude.ai's experience

**Session Goals:**
- Left sidebar navigation with chat history (collapsible on mobile)
- Searchable conversation list with auto/manual titles
- Clean message bubbles with Markdown + syntax highlighting
- Copy code buttons & artifact support
- Model selector, settings, theme toggle
- Mobile-first: drawer navigation, bottom bar, swipe gestures
- Map Claude features to on-device feasibility

**Techniques Used:** Analogical Thinking, Resource Constraints, First Principles, SCAMPER

**Total Ideas Generated:** 55+

### Key Themes Identified:

1. **Core Identity:** "Private Offline Conversations" - the soul of OnDevice AI
2. **Privacy as Visible Feature:** Make the invisible (privacy) tangible through UI
3. **Differentiation over Imitation:** Don't copy Claude - leverage on-device advantages
4. **Constraints as Features:** Storage limits, offline mode, loading time → reframe positively
5. **Unified Experience:** One smart interface that handles all input types
6. **User Ownership:** Your data, your device, your control

## Technique Sessions

### Technique 1: Analogical Thinking (Claude.ai → OnDevice Mapping)

#### Sidebar / Brand Area Analysis

**Claude's Approach:**
- Logo at top as psychological anchor
- User control & personalization
- Accumulated value through conversation history

**OnDevice's Unique Opportunity - "Privacy as Visible Feature":**

| Abstract Concept | Tangible UI Element |
|------------------|---------------------|
| "Private" | **"No cloud uploads, ever"** badge |
| "Local" | **"X conversations, Y MB stored locally"** indicator |
| "Secure" | **Network indicator showing zero outbound calls** |
| "Yours" | **"Export your data anytime"** button |

#### OnDevice's Unfair Advantages (What Claude CAN'T Offer)

| Feature | Why Claude Can't Compete |
|---------|--------------------------|
| ✈️ Airplane mode works | Claude = cloud-dependent |
| 💰 Truly unlimited free usage | Claude = API costs |
| 🔒 Zero network calls | Claude = requires internet |
| 📦 Your data, exportable | Claude = their servers |
| ⚡ Battery/performance toggle | Claude = no device control |
| 📚 Local RAG with your docs | Claude = uploads to cloud |

#### Brand Area Concept

```
┌─────────────────────────────┐
│ 🤖 OnDevice AI              │
│ ──────────────────────────  │
│ 🔒 Private & Local          │
│ 📊 23 chats • 4.2 MB stored │
│ ✈️ Works offline            │
└─────────────────────────────┘
```

#### Storage/Value Indicators Ideas
- "X conversations, Y MB stored locally" - makes privacy tangible
- "No cloud uploads, ever" badge/indicator
- Data export option - "Your data, take it anywhere"

#### Personalization Features
- Model preferences per task (Gemini for chat, SD settings for images)
- Custom system prompts that stay local
- Favorite/pinned conversations with tags

#### Unique On-Device Features
- Offline indicator showing AI works without internet
- Battery/performance mode toggle (quality vs speed)
- Local knowledge base - import documents for RAG
- Conversation export to markdown/PDF

---

#### Chat Interface Mapping

| Claude.ai Feature | OnDevice Feasibility |
|-------------------|---------------------|
| Clean message bubbles | ✅ Already have basic version |
| Markdown rendering | 🟡 Needs implementation |
| Syntax highlighting | 🟡 Needs implementation |
| Copy code button | 🟡 Easy to add |
| Artifacts panel | 🔄 Redesigned for on-device (see below) |
| Image display | ✅ Working (SD output when fixed) |
| File attachments | 🟡 Gemini Multimodal can analyze! |
| Streaming response | ✅ Already implemented |

#### On-Device Artifacts Concept (Key Differentiator!)

**The Reframe: Ephemeral → Persistent**

| Claude Artifacts | OnDevice Artifacts |
|------------------|-------------------|
| Session-based, disappears | **Persistent & local** |
| Cloud-stored (their servers) | **Device gallery (your storage)** |
| Upload size limits | **No limits** |
| Requires internet | **Works offline** |

**Artifact Types:**

| Type | Source | Features |
|------|--------|----------|
| 🖼️ Images | Stable Diffusion (BROKEN - needs fix) | Swipeable gallery, save/share/delete |
| 💻 Code | Gemini text generation | Syntax highlighted, copy, "Run" if executable |
| 🎤 Transcripts | Whisper | Timestamp navigation, export to text |
| 📄 Markdown | Gemini responses | Rendered preview, edit capability |

**UI Pattern: Bottom Sheet + Gallery**

```
┌─────────────────────────────────┐
│  Chat conversation...           │
│                                 │
├─────────────────────────────────┤  ← Bottom sheet slides up
│ 📎 Artifacts (3)                │
│ ┌─────┐ ┌─────┐ ┌─────┐        │
│ │ IMG │ │ CODE│ │ TXT │        │
│ └─────┘ └─────┘ └─────┘        │
│        [View All Artifacts]     │
└─────────────────────────────────┘
```

**Storage-Aware Features:**
- Show artifact size/count in settings
- Smart cleanup: "Delete artifacts older than 30 days?"
- Export all artifacts as ZIP
- Per-artifact control: "Keep" vs "Discard after session"

---

#### Input Area Design

**Model Selector:**
- Position: Top center (keep current)
- Remove: "AI Chat" label at top (redundant)
- Models: Only 2-3 downloadable (multimodal capable)
- Behavior: Auto-detect input type (image attached → uses multimodal)
- No separate modes - unified experience

**Attachments:**
- Keep current attachment buttons and design
- Works with multimodal model for image analysis

**Voice Input:**
- Both options:
  - Mic button → transcribes speech → fills text input
  - Full audio analysis mode for Whisper transcription tasks

**CRITICAL: Remove Task Tiles**
- DELETE/HIDE the separate task tiles (AI Chat, Ask Image, Audio Scribe)
- Reason: They break functionality, create confusion
- Solution: One unified chat interface handles ALL input types

**Proposed Unified UI Layout:**

```
┌─────────────────────────────────────────┐
│ ☰        [Gemini Nano ▼]          ⚙️   │  ← Model selector center
├─────────────────────────────────────────┤
│                                         │
│         Chat messages area              │
│                                         │
├─────────────────────────────────────────┤
│ [📎] [🎤] Type message...         [⬆️] │  ← Unified input
└─────────────────────────────────────────┘
```

**Auto-detection Logic:**
- Plain text → Text model
- Image attached → Multimodal model
- Mic held → Whisper transcription → text input
- "Generate image of..." → Stable Diffusion (future)

---

#### Settings/Preferences Design

**Privacy Approach: Subtle but Impressionable**
- Not a dedicated screaming section
- Weave throughout UI with small 🔒 icons
- Footer: "OnDevice AI never connects to the cloud"

**Model Manager: Dedicated Screen**
```
Model Manager
├── Downloaded
│   ├── Gemini Nano (450 MB) [DELETE]
│   └── Gemini Multimodal (890 MB) [DELETE]
├── Available to Download
│   ├── Whisper (320 MB) [DOWNLOAD]
│   └── Stable Diffusion (1.1 GB) [DOWNLOAD]
└── Storage Used: 1.34 GB / 4 GB allocated
```

**From Claude - INCLUDE:**
- Theme toggle (Light/Dark/System)
- Clear all conversations
- Export data
- App version + feedback link

**OnDevice UNIQUE - ADD:**
- Performance mode slider (Quality ↔ Speed)
- Default model preference
- Auto-cleanup toggle
- Storage breakdown visual
- Offline indicator

**EXCLUDE (not relevant):**
- Account/login (no accounts needed)
- Subscription management (free, local)
- API keys (no API)
- Usage limits (unlimited local)
- Keyboard shortcuts (mobile-first)

**Final Settings Structure:**
```
⚙️ Settings
├── 🎨 Appearance
│   ├── Theme: [Light] [Dark] [System]
│   └── Text size: [Small] [Medium] [Large]
├── 🤖 Models
│   └── [Open Model Manager →]
├── 💬 Conversations
│   ├── Auto-save: [ON]
│   ├── Auto-cleanup: [Never ▼]
│   ├── [Export All]
│   └── [Clear All] ⚠️
├── 💾 Storage
│   ├── ████████░░ 1.8 GB used
│   └── [Manage Storage →]
├── ⚡ Performance
│   ├── Mode: [Quality ←●→ Speed]
│   └── Battery saver: [OFF]
└── ℹ️ About
    ├── Version info
    ├── 🔒 Data never leaves device
    └── [Feedback] [Rate]
```

---

### Technique 1 Summary: Analogical Thinking Complete

**Components Mapped:**
1. ✅ Sidebar / Brand area - Privacy as visible feature
2. ✅ Chat interface / Artifacts - Persistent local artifacts
3. ✅ Input area - Unified, auto-detecting
4. ✅ Settings - Streamlined, privacy-subtle

**Key Insight:** Don't copy Claude - **differentiate** with on-device advantages

---

### Technique 2: Resource Constraints

#### 1. 💾 Storage: Enforced Budget

**Decision:** Set storage budget, enforce it, give user control

- Default budget: 4 GB (adjustable)
- Warning at 80%, blocking at 95%
- User can adjust budget in settings
- Never auto-delete without permission
- Can't download new model if over budget

**Storage Breakdown:**
- Models: 2.7 GB max
- Conversations: 500 MB max
- Artifacts: 800 MB max
- System: ~200 MB reserved

#### 2. 🔋 Battery: Warn, Don't Block

**Decision:** Inform user, let them decide, no hard constraints

**Triggers for warning:**
- Image generation (Stable Diffusion)
- Audio transcription > 2 minutes
- Battery below 30%

**User control:**
- Can dismiss permanently ("Don't show again")
- No blocking - just informed consent
- Shows estimated battery usage

#### 3. 📴 Offline: Queue with Permission

**Decision:** Queue downloads, ask before executing when online

**Rules:**
- Never auto-download without asking
- Show connection type (WiFi vs Mobile)
- Queue persists across app restarts
- User can cancel queued downloads anytime
- Prompt: "Download now?" or "Wait for WiFi"

#### 4. 🧠 Memory: Model Loading UX

**Current Problem:** User blocked during model load, just spinning icon

**Solution:** Tutorial/Info cards during loading - turn wait into onboarding!

**Model Loading Screen:**
- Progress indicator with percentage
- Rotating tip cards (every 3-4 seconds)
- User can swipe through manually
- First-time: full tutorial sequence
- Returning: random tips

**Tip Card Content:**
1. 🔒 Private by Design - Conversations never leave device
2. ✈️ Works Offline - No internet needed once downloaded
3. 📎 Attach Images - Tap to analyze photos
4. 🎤 Voice Input - Hold mic to speak
5. 💾 Auto-Saved - Chats saved locally
6. ⚡ Tip: Shorter prompts = faster responses

**Model Download Screen (longer wait):**
- Download progress with time remaining
- Feature overview cards
- Privacy benefits explained
- What this model can do
- Cancel option always visible

#### 5. ⏱️ Response Time: Reframe as Feature

**During inference, show:** "Running privately on your device..."

- Turns wait time into privacy reminder
- Token-by-token streaming (already implemented)
- Reinforces on-device value proposition

---

### Technique 2 Summary: Resource Constraints Complete

**Decisions Made:**
1. 💾 Storage: Enforced budget with user control
2. 🔋 Battery: Warn but don't block
3. 📴 Offline: Queue with permission
4. 🧠 Memory: Tutorial during model loads
5. ⏱️ Response: Reframe wait as privacy feature

**Key Insight:** Constraints become features when framed correctly

---

### Technique 3: First Principles Thinking

#### Core Identity Discovered

**OnDevice AI = Private Offline Conversations**

```
NOT "Claude but on your phone"
NOT "Free AI chat"
NOT "Multimodal AI demo"

IT IS: The AI that's YOURS. Truly yours.
       Works anywhere. Leaves no trace outside your device.
```

#### The Tagline Test

Every screen must pass: *"Does this remind the user they're having a private, offline conversation?"*

#### Feature Validation Through Core Identity

| Feature | Serves Core Identity? |
|---------|----------------------|
| Chat history | ✅ Your private conversations |
| Export to ZIP | ✅ Your data, your control |
| Storage indicators | ✅ Transparency about YOUR device |
| "Running privately..." | ✅ Core message |
| Cloud sync | ❌ Violates "private" |
| Account/login | ❌ Violates "private" |

#### First Principles UX Implications

**1. First Launch - Lead with Core Truth:**
```
Welcome to OnDevice AI

🔒 Your AI. Your Device. 100% Private.

No cloud. No account. No data leaves here.

[Get Started]
```

**2. Reframe Offline as Feature:**
- NOT: "You're offline" (implies broken)
- YES: "Running locally" (implies working as intended)
- NOT: "No internet connection"
- YES: "Private mode active"

**3. Every Export = Ownership Reminder:**
- "Your conversations. Your data."
- "This data exists ONLY on your device. Take it anywhere."

#### Key Insight

The core identity "Private Offline Conversations" becomes the filter for EVERY design decision. If it doesn't reinforce privacy + offline + yours, question if it belongs.

---

### Technique 4: SCAMPER Analysis

**S**ubstitute | **C**ombine | **A**dapt | **M**odify | **P**ut to other uses | **E**liminate | **R**everse

#### 1. Home Screen / Task Tiles
- **ELIMINATE** task tiles entirely
- **SUBSTITUTE** with unified chat screen
- Auto-detects input type (text/image/audio)

#### 2. Top Bar
- **ELIMINATE** redundant "AI Chat" label
- **MODIFY** to: `☰ [Gemma 3 1B ▼] ⚙️`
- **ADD** subtle privacy indicator

#### 3. Hamburger Menu (Left Drawer)
- **COMBINE** brand area + storage + history
- **ADAPT** Claude's searchable conversation list
- **MODIFY** with date grouping (Today, Yesterday, Last 7 days)

**Enhanced Drawer:**
```
┌─────────────────────────────┐
│ 🤖 OnDevice AI              │
│ 🔒 Private & Local          │
│ 📊 12 chats • 2.1 MB        │
├─────────────────────────────┤
│ [🔍 Search conversations  ] │
├─────────────────────────────┤
│ ✏️ New Chat                 │
├─────────────────────────────┤
│ TODAY                       │
│   Chat about Kotlin...      │
│ YESTERDAY                   │
│   Code review...            │
├─────────────────────────────┤
│ ⚙️ Settings                 │
└─────────────────────────────┘
```

#### 4. Chat Message Bubbles
- **MODIFY** styling - cleaner, subtle shadows
- **ADAPT** markdown + syntax highlighting
- **COMBINE** with action buttons (Copy, Regenerate)
- **PUT TO OTHER USES** long-press menu (Copy, Share, Delete, Pin)

**Enhanced AI Message:**
```
┌─────────────────────────────────────┐
│ Here's the code:                    │
│ ```kotlin                           │
│ fun example() { }                   │
│ ```                    [📋 Copy]    │
├─────────────────────────────────────┤
│ [🔄 Regenerate]  [📋 Copy All]      │
└─────────────────────────────────────┘
```

#### 5. Input Area
- **KEEP** current design (already decided)
- **COMBINE** mic with hold-to-speak for voice-to-text
- **MODIFY** to show token estimate for long inputs

#### 6. Settings Screen
- Already designed in Technique 1 ✅

#### 7. Model Loading State
- Already designed in Technique 2 ✅
- **SUBSTITUTE** spinner with progress + tutorial cards

---

### Technique 4 Summary: SCAMPER Complete

**Key Changes Identified:**
1. ELIMINATE task tiles → unified chat
2. ELIMINATE "AI Chat" label → model name only
3. ENHANCE drawer with brand, search, date groups
4. ADD copy/regenerate buttons to messages
5. ADD markdown + syntax highlighting
6. ADD long-press context menu

---

## Idea Categorization

### Immediate Opportunities (Quick Wins)

_Low effort, high impact - implement soon_

1. Remove "AI Chat" label from top bar
2. Add "🔒 Private & Local" to drawer header
3. Add storage indicator to drawer (X chats • Y MB)
4. Add "Running privately on your device..." during inference
5. Add Copy button to code blocks
6. Add Copy All button to AI messages
7. Reframe "offline" as "Running locally" in UI
8. Add conversation date grouping (Today, Yesterday, etc.)
9. Delete/hide task tiles - direct to unified chat

### Future Innovations

_Medium effort, significant value_

1. Markdown rendering in AI responses
2. Syntax highlighting for code blocks
3. Tutorial/tip cards during model loading
4. Search conversations functionality
5. Regenerate response button
6. Long-press context menu (Copy, Share, Delete, Pin)
7. Storage budget enforcement with warnings
8. Battery warning for heavy operations
9. Export conversations (JSON/Markdown/ZIP)
10. Theme toggle (Light/Dark/System)
11. Model Manager as dedicated screen
12. Auto-cleanup option (delete chats after X days)
13. Hold-to-speak voice input
14. Download queue with permission on reconnect

### Moonshots

_High effort, transformative_

1. Persistent artifact gallery (images, code, transcripts)
2. Local RAG - import your documents
3. Fix Stable Diffusion image generation
4. Performance mode slider (Quality ↔ Speed)
5. First-launch onboarding with privacy messaging
6. Pinned/favorite conversations
7. AI-generated conversation titles

### Insights and Learnings

_Key realizations from the session_

1. **Core Identity Discovered:** OnDevice AI = "Private Offline Conversations"
2. **Don't Copy Claude:** Differentiate with on-device advantages
3. **Privacy as Visible Feature:** Make the invisible (privacy) visible through UI
4. **Constraints Become Features:** Storage limits, offline mode, loading time - all reframeable
5. **Ownership Matters:** User data is USER data - export, delete, control
6. **The Tagline Test:** Every screen should remind user of private + offline + yours

## Action Planning

### Top 3 Priority Ideas

#### #1 Priority: Unified Chat Experience

- **Rationale:** Core UX simplification - removes confusion from multiple task tiles, matches Claude's single entry point simplicity, directly enables the "Private Offline Conversations" identity
- **Next steps:**
  1. Delete/hide task tile home screen
  2. Make unified chat the default entry point
  3. Remove "AI Chat" label from top bar
  4. Implement auto-detection of input type (text/image/audio)
- **Resources needed:** UI/Navigation refactoring, ViewModel consolidation
- **Dependencies:** Existing chat infrastructure (already working)

#### #2 Priority: Privacy-First Visual Identity

- **Rationale:** Directly reinforces core identity "Private Offline Conversations" with every user interaction - makes the invisible (privacy) visible
- **Next steps:**
  1. Add brand area to drawer: "🔒 Private & Local"
  2. Add storage indicator: "X chats • Y MB stored"
  3. Show "Running privately on your device..." during inference
  4. Reframe all "offline" messaging as "Running locally"
- **Resources needed:** UI updates to drawer, inference status display
- **Dependencies:** ConversationDao for chat counts, storage calculations

#### #3 Priority: Enhanced Message Experience

- **Rationale:** Makes conversations more useful, shareable, and professional - key to user retention and satisfaction
- **Next steps:**
  1. Add Copy button to code blocks
  2. Add Copy All button to AI messages
  3. Implement Markdown rendering
  4. Add syntax highlighting for code
  5. Add Regenerate response button
- **Resources needed:** Markdown library integration, clipboard handling, UI components
- **Dependencies:** Chat message rendering infrastructure

## Reflection and Follow-up

### What Worked Well

1. **Analogical Thinking** - Mapping Claude.ai features revealed both what to copy AND what to differentiate
2. **Resource Constraints** - Forced practical thinking about on-device limitations
3. **First Principles** - Discovered the core identity "Private Offline Conversations" which became our North Star
4. **SCAMPER** - Systematic UI review caught specific changes needed

### Areas for Further Exploration

1. Stable Diffusion integration (currently broken - needs investigation)
2. Local RAG implementation - significant technical complexity
3. Artifact persistence architecture
4. Performance benchmarking across Gemma model sizes

### Recommended Follow-up Techniques

1. User testing with prototype of unified chat
2. A/B testing privacy messaging effectiveness
3. Technical spike on Markdown/syntax highlighting libraries

### Questions That Emerged

1. How to handle model switching gracefully when user is mid-conversation?
2. What's the right default storage budget for different device tiers?
3. Should artifact gallery be separate screen or integrated into chat?
4. How to surface voice-to-text vs full audio transcription clearly?

### Next Session Planning

- **Suggested topics:** Technical architecture for unified chat implementation
- **Recommended timeframe:** After PRD approval
- **Preparation needed:** Create PRD from this brainstorming session, get stakeholder alignment on priorities

---

## Session Statistics

- **Techniques Used:** 4 (Analogical Thinking, Resource Constraints, First Principles, SCAMPER)
- **Total Ideas Generated:** 55+
- **Quick Wins Identified:** 9
- **Future Innovations:** 14
- **Moonshots:** 7
- **Top Priorities:** 3

---

_Session facilitated using the BMAD CIS brainstorming framework_
