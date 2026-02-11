# Complete UI/UX Documentation for Figma Design Review
## OnDevice AI Android App

**Document Version:** 1.0
**Date:** December 30, 2025
**Purpose:** Comprehensive UI/UX documentation for Figma redesign and improvement
**Total Screens:** 11 main screens + 4 dialogs

---

## Table of Contents

1. [App Overview](#app-overview)
2. [Design System](#design-system)
3. [Screen-by-Screen Breakdown](#screen-by-screen-breakdown)
4. [User Flows](#user-flows)
5. [Component Library](#component-library)
6. [Improvement Opportunities](#improvement-opportunities)
7. [Figma Setup Guide](#figma-setup-guide)

---

## App Overview

**App Name:** OnDevice AI (forked from Google AI Edge Gallery)
**Platform:** Android
**UI Framework:** Jetpack Compose
**Design Language:** Material Design 3 (Material You)
**Primary Function:** On-device AI model interaction (chat, image generation, prompt lab)

### Key Capabilities
- Multi-modal AI chat (text, vision, audio)
- On-device image generation (Stable Diffusion)
- Model management and downloading
- Conversation history and persistence
- Cross-model compatibility

### Target Users
- Developers testing on-device AI
- AI enthusiasts experimenting with models
- Users wanting privacy-focused AI (no cloud)

---

## Design System

### Color Palette (Material 3 Dynamic)

**Light Theme:**
- Primary: Dynamic (user wallpaper-based)
- On-Primary: White
- Surface: Light background
- Surface Variant: Slightly darker surface
- Error Container: Light red (#FDE7E9)
- On Error Container: Dark red text

**Dark Theme:**
- Primary: Dynamic (user wallpaper-based)
- On-Primary: Black
- Surface: Dark background
- Surface Variant: Slightly lighter surface
- Error Container: Dark red
- On Error Container: Light red text

### Typography

**Current Implementation:**
- Material 3 default typography
- Font: Roboto (system default)
- Text size options: Small / Medium (default) / Large (accessibility)

**Scale:**
- Headline Small: 24sp
- Title Large: 22sp
- Title Medium: 16sp
- Title Small: 14sp
- Body Large: 16sp
- Body Medium: 14sp
- Body Small: 12sp
- Label Large: 14sp
- Label Medium: 12sp
- Label Small: 11sp

### Spacing System

**Padding/Margins:**
- XXS: 4dp
- XS: 8dp
- S: 12dp
- M: 16dp
- L: 24dp
- XL: 32dp
- XXL: 48dp

### Elevation/Shadows

- Level 0: 0dp (no elevation)
- Level 1: 1dp (cards, modals)
- Level 2: 3dp (FAB)
- Level 3: 6dp (dialogs)
- Level 4: 8dp (top app bar when scrolled)
- Level 5: 12dp (bottom sheets)

### Border Radius

- Small: 8dp (buttons, chips)
- Medium: 12dp (cards)
- Large: 16dp (modals)
- Extra Large: 28dp (FAB)
- Full: 9999dp (pill shapes)

### Icons

- Size Small: 16dp
- Size Medium: 24dp (default)
- Size Large: 32dp
- Size XLarge: 64dp (task icons)
- Source: Material Icons Rounded

---

## Screen-by-Screen Breakdown

### Screen 1: Splash Screen

**File:** Android splash screen system
**Duration:** ~1 second with cross-fade

#### Visual Description

```
┌─────────────────────────────┐
│                             │
│                             │
│          [App Icon]         │
│                             │
│        OnDevice AI          │
│                             │
│     [Loading indicator]     │
│                             │
│                             │
└─────────────────────────────┘
```

**Components:**
- App icon (centered, 64dp)
- App name text (centered below icon)
- Indeterminate loading indicator

**Animation:**
- Icon fade in (300ms)
- Cross-fade to main content (400ms)
- Background color matches Material You theme

**Design Notes:**
- System splash screen (Android 12+)
- Minimal, clean design
- Follows Google's splash screen guidelines

---

### Screen 2: TOS Dialog (First Launch)

**File:** `app/src/main/java/ai/ondevice/app/ui/common/tos/TosDialog.kt`
**Trigger:** First app launch (before any other screen)
**Dismissible:** No (must accept to proceed)

#### Visual Layout

```
┌─────────────────────────────────────┐
│  [ℹ️]  OnDevice AI                  │
│        Terms of Service             │
│─────────────────────────────────────│
│                                     │
│  Before using this app, please      │
│  review and accept our Terms of     │
│  Service.                           │
│                                     │
│  OnDevice AI is built on the        │
│  open-source Google AI Edge Gallery │
│  (Apache 2.0 License).              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  📋 AI Model Terms                  │
│                                     │
│  Some AI models (like Gemma) have   │
│  their own terms. You'll be         │
│  prompted to accept those before    │
│  downloading.                       │
│                                     │
│  Links:                             │
│  • Full Terms of Service ↗          │
│  • Privacy Policy ↗                 │
│  • Gemma Terms of Use ↗             │
│                                     │
│─────────────────────────────────────│
│  [Cancel]      [Accept & Continue]  │
└─────────────────────────────────────┘
```

**Components:**
- AlertDialog (Material 3)
- Info icon (24dp, primary color)
- Title text (headline small)
- Body text (body medium)
- Divider
- Section header with emoji
- Clickable links (primary color, underlined)
- Two buttons: Cancel (text), Accept (filled)

**User Actions:**
- Read summary
- Click links to open browser
- Accept → Continue to ModelSelectionScreen
- Cancel → Exit app

**Design Improvements:**
- ✅ Currently clean and clear
- 💡 Consider: Checkbox "I have read and agree" for legal clarity
- 💡 Consider: Version number of TOS displayed
- 💡 Consider: Ability to scroll if content expands

---

### Screen 3: Model Selection Screen (First Launch)

**File:** `app/src/main/java/ai/ondevice/app/ui/modelselection/ModelSelectionScreen.kt`
**Trigger:** After TOS acceptance (first launch), or from Settings → "Download More Models"
**Purpose:** Choose initial AI model

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ← [Back]                             │
├───────────────────────────────────────┤
│                                       │
│         [🤖 Task Icon]                │
│         (64dp, animated)              │
│                                       │
│    Select Your On-Device AI Model     │
│           (gradient text)             │
│                                       │
│  Chat with AI models entirely on      │
│  your device. No internet required.   │
│                                       │
│  📚 API Documentation ↗               │
│  💻 Example code ↗                    │
│                                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                       │
│  3 models available                   │
│                                       │
│  Recommended models                   │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Gemma-3n-E2B-it          ⭐ Best│ │
│  │ For vision & audio              │ │
│  │                                 │ │
│  │ 📱 8GB RAM    📦 3.5 GB         │ │
│  │ 💬 Text  📷 Vision  🎤 Audio    │ │
│  │                                 │ │
│  │           [Download]            │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Gemma3-1B-IT            ⚡ Fast │ │
│  │ For text chat                   │ │
│  │                                 │ │
│  │ 📱 6GB RAM    📦 558 MB         │ │
│  │ 💬 Text                         │ │
│  │                                 │ │
│  │           [Download]            │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Gemma-3n-E4B-it         ⚡ Fast │ │
│  │ For vision & audio              │ │
│  │                                 │ │
│  │ 📱 12GB RAM   📦 4.6 GB         │ │
│  │ 💬 Text  📷 Vision  🎤 Audio    │ │
│  │                                 │ │
│  │           [Download]            │ │
│  └─────────────────────────────────┘ │
│                                       │
│  💡 You can change models later in    │
│     Settings                          │
│                                       │
└───────────────────────────────────────┘
```

**Components:**
- TopAppBar with back button
- Task icon (64dp, animated entrance)
- Title text (headline large, gradient animation)
- Description text (body medium)
- Clickable links (primary color)
- Divider
- Model count text (body small, muted)
- "Recommended models" section header
- LazyColumn with model cards:
  - Model name + badge (⭐ Best / ⚡ Fast)
  - Subtitle description
  - RAM requirement icon + text
  - Size icon + text
  - Capability badges (Text / Vision / Audio)
  - Download button (filled, full width)
- Footer hint text (body small, muted)

**Animations:**
- Task icon: Scale + fade in (500ms delay)
- Title: Revealing text effect with gradient
- Model cards: Stagger fade in (100ms intervals)

**User Actions:**
- Click back button → Return to previous screen
- Click documentation links → Open browser
- Click Download on model card → Downloads model, navigates to chat
- Scroll through model list

**States:**
- Default: All models ready to download
- Downloading: Progress bar on selected model card
- Downloaded: Checkmark + "Open" button
- Error: Snackbar with error message

**Design Improvements:**
- ✅ Beautiful animations and gradient text
- ✅ Clear model comparison
- 💡 Consider: Filter by capability (Text/Vision/Audio)
- 💡 Consider: Sort by size, RAM requirement
- 💡 Consider: "Why do I need this model?" info button
- 💡 Consider: Preview image/video of model capabilities
- 💡 Consider: Download queue (multiple selections)

---

### Screen 4: LLM Chat Screen (Main Screen)

**File:** `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatScreen.kt`
**Variants:**
- LlmChatScreen (text)
- LlmAskImageScreen (text + image)
- LlmAskAudioScreen (text + audio)

**Purpose:** Primary interaction screen for chatting with AI models

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  [Gemma-3n-E2B-it ▼]  ⋯  ⚙️  📜   │
├───────────────────────────────────────┤
│                                       │
│  ┌───────────────────────────────┐   │
│  │ Hi! How can I help?           │   │
│  │                         [AI]  │   │
│  │ 12:30 PM                      │   │
│  └───────────────────────────────┘   │
│                                       │
│               ┌───────────────────┐   │
│               │ Tell me about     │   │
│      [User]  │ on-device AI      │   │
│               │           12:31 PM│   │
│               └───────────────────┘   │
│                                       │
│  ┌───────────────────────────────┐   │
│  │ On-device AI refers to...     │   │
│  │                               │   │
│  │ [Response continues]          │   │
│  │                               │   │
│  │                         [AI]  │   │
│  │ 12:31 PM                      │   │
│  └───────────────────────────────┘   │
│                                       │
│  [Generating... ⏸️ Stop]              │
│                                       │
│                                       │
│                                       │
├───────────────────────────────────────┤
│                                       │
│  ┌─────────────────────────────┐     │
│  │ Type a message...           │ [↗] │
│  └─────────────────────────────┘     │
│                                       │
│  [📎 Attach] [🎤 Voice] [📷 Image]   │
│                                       │
└───────────────────────────────────────┘
```

**Top App Bar Components:**
- Back button (←)
- Model selector dropdown (Gemma-3n-E2B-it ▼)
- More options menu (⋯):
  - Change model
  - Configure model
  - Run inference again
  - Reset session
- Settings icon (⚙️) → SettingsScreen
- Conversation history icon (📜) → ConversationListScreen

**Chat Panel Components:**
- LazyColumn with messages
- Message bubbles:
  - **AI messages:** Left-aligned, surface variant color, rounded corners
  - **User messages:** Right-aligned, primary color, rounded corners
  - Timestamp below each message (body small, muted)
  - Max width: 80% of screen
- Active generation indicator:
  - "Generating..." text
  - Stop button (⏸️)
  - Optional: Streaming text appearing token-by-token

**Input Area Components:**
- Multi-line text field (outlined)
- Send button (arrow, primary color)
- Attachment row (if model supports):
  - Attach file button (📎) - for image models
  - Voice input button (🎤) - for audio models
  - Camera button (📷) - for vision models
- Keyboard shows automatically when focused

**States:**

**1. Empty State (New Conversation):**
```
┌───────────────────────────────────────┐
│                                       │
│             [🤖 Icon]                 │
│                                       │
│        Start a conversation           │
│                                       │
│  Ask me anything! I'm running         │
│  entirely on your device.             │
│                                       │
│  💡 Try asking:                       │
│  • "Explain quantum computing"        │
│  • "Write a haiku about AI"           │
│  • "What can you help me with?"       │
│                                       │
└───────────────────────────────────────┘
```

**2. Generating Response:**
```
┌─────────────────────────────────┐
│ [Response being generated...]   │
│ ▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░          │
│                                 │
│                     [⏸️ Stop]    │
└─────────────────────────────────┘
```

**3. Error State:**
```
┌─────────────────────────────────┐
│ ⚠️ Model failed to load         │
│                                 │
│ Error: Out of memory            │
│                                 │
│ [Try Again]  [Change Model]     │
└─────────────────────────────────┘
```

**4. Model Downloading:**
```
┌───────────────────────────────────────┐
│                                       │
│          [📦 Download Icon]           │
│                                       │
│     Downloading Gemma-3n-E2B-it       │
│                                       │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░  73%       │
│                                       │
│  1.2 GB / 3.5 GB                      │
│  Estimated: 2 minutes                 │
│                                       │
│          [⏸️ Pause] [✖️ Cancel]        │
│                                       │
│  💡 You can continue using the app    │
│     while the model downloads.        │
│                                       │
└───────────────────────────────────────┘
```

**User Actions:**
- Type message in text field
- Attach image (if vision model)
- Record/attach audio (if audio model)
- Send message (↗ button or Enter key)
- Stop generation (⏸️ Stop button)
- Scroll through chat history
- Select different model (dropdown)
- Open model config dialog (⚙️)
- Navigate to conversation history (📜)
- Navigate to settings (⚙️)
- Reset conversation (⋯ menu → Reset)
- Run inference again (⋯ menu)

**Animations:**
- Message appear: Fade + slide in from bottom (300ms)
- Streaming text: Token-by-token reveal
- Send button: Scale on tap
- Loading: Circular progress or shimmer

**Design Improvements:**
- ✅ Clean chat bubble design
- ✅ Clear visual hierarchy
- 💡 Consider: Message actions (copy, regenerate, edit)
- 💡 Consider: Code syntax highlighting in responses
- 💡 Consider: Markdown rendering (bold, italic, lists)
- 💡 Consider: Image previews in chat (for vision models)
- 💡 Consider: Audio waveform visualization (for audio)
- 💡 Consider: "Scroll to bottom" FAB when scrolled up
- 💡 Consider: Conversation title auto-generation from first message
- 💡 Consider: Export conversation button
- 💡 Consider: Typing indicator animation (while generating)

---

### Screen 5: Conversation List Screen

**File:** `app/src/main/java/ai/ondevice/app/ui/conversationlist/ConversationListScreen.kt`
**Purpose:** View and manage saved chat conversations

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  Chats                             │
├───────────────────────────────────────┤
│  ┌─────────────────────────────────┐ │
│  │ 🔍 Search conversations...      │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ ⭐ Quantum Physics Discussion    │ │
│  │    Last: Explain entanglement... │ │
│  │    2 hours ago              ⋮   │ │
│  └─────────────────────────────────┘ │
│  ─────────────────────────────────── │
│  ┌─────────────────────────────────┐ │
│  │    Recipe Ideas                  │ │
│  │    Last: Give me a chocolate...  │ │
│  │    Yesterday                ⋮   │ │
│  └─────────────────────────────────┘ │
│  ─────────────────────────────────── │
│  ┌─────────────────────────────────┐ │
│  │    Code Review Help              │ │
│  │    Last: Review this Python...   │ │
│  │    3 days ago               ⋮   │ │
│  └─────────────────────────────────┘ │
│  ─────────────────────────────────── │
│  ┌─────────────────────────────────┐ │
│  │ ⭐ Story Ideas                   │ │
│  │    Last: Write a sci-fi story... │ │
│  │    1 week ago               ⋮   │ │
│  └─────────────────────────────────┘ │
│                                       │
│                                   [+] │
└───────────────────────────────────────┘
```

**Empty State:**
```
┌───────────────────────────────────────┐
│  ←  Chats                             │
├───────────────────────────────────────┤
│                                       │
│                                       │
│             [💬 Icon]                 │
│                                       │
│       No conversations yet            │
│                                       │
│  Start chatting to see your           │
│  conversations here.                  │
│                                       │
│                                       │
│                                   [+] │
└───────────────────────────────────────┘
```

**Context Menu (on ⋮ click):**
```
┌─────────────────────┐
│ ⭐ Star             │
│ ✏️ Rename           │
│ 🗑️ Delete           │
└─────────────────────┘
```

**Delete Confirmation Dialog:**
```
┌─────────────────────────────────┐
│  Delete Conversation?           │
│─────────────────────────────────│
│  This action cannot be undone.  │
│                                 │
│  [Cancel]         [Delete]      │
└─────────────────────────────────┘
```

**Rename Dialog:**
```
┌─────────────────────────────────┐
│  Rename Conversation            │
│─────────────────────────────────│
│  ┌───────────────────────────┐ │
│  │ New name                  │ │
│  └───────────────────────────┘ │
│                                 │
│  [Cancel]         [Rename]      │
└─────────────────────────────────┘
```

**Components:**
- TopAppBar:
  - Back button
  - "Chats" title
- Search field (outlined, full width)
  - Magnifying glass icon
  - "Search conversations..." placeholder
  - Clear button (X) when typing
- LazyColumn with conversation items:
  - Star icon (⭐) if starred
  - Conversation title (title medium, bold)
  - Last message preview (body small, muted, truncated)
  - Timestamp (body small, muted, right-aligned)
  - More options (⋮) button
  - Horizontal divider between items
- FloatingActionButton (+)
  - Creates new conversation
  - Bottom-right position
  - Primary color

**User Actions:**
- Search conversations (filters list in real-time)
- Clear search query (X button)
- Click conversation → Navigate to ConversationDetailScreen
- Click + FAB → Navigate back to chat (new conversation)
- Click ⋮ → Show context menu
- Star/Unstar conversation
- Rename conversation (shows dialog)
- Delete conversation (shows confirmation dialog)
- Navigate back to chat

**States:**
- Loading: Circular progress in center
- Empty: Icon + "No conversations yet" message
- Success: List of conversations
- Empty search results: "No conversations found" message
- Error: Error message with retry button

**Design Improvements:**
- ✅ Clean, flat list design
- ✅ Clear visual hierarchy
- 💡 Consider: Swipe actions (swipe left to delete, swipe right to star)
- 💡 Consider: Group by date (Today, Yesterday, This Week, etc.)
- 💡 Consider: Show model used in each conversation
- 💡 Consider: Show message count per conversation
- 💡 Consider: Conversation preview with first few messages
- 💡 Consider: Bulk actions (select multiple, delete all)
- 💡 Consider: Filter by starred, date range, model
- 💡 Consider: Export selected conversations
- 💡 Consider: Conversation cards instead of flat list
- 💡 Consider: Pinned conversations section

---

### Screen 6: Conversation Detail Screen

**File:** `app/src/main/java/ai/ondevice/app/ui/conversationdetail/ConversationDetailScreen.kt`
**Purpose:** Read-only view of a conversation's message history

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  Quantum Physics Discussion        │
├───────────────────────────────────────┤
│                                       │
│  ┌───────────────────────────────┐   │
│  │ Hello! I have a question      │   │
│  │ about quantum physics.        │   │
│  │                               │   │
│  │ 2h                       [You]│   │
│  └───────────────────────────────┘   │
│                                       │
│  ┌───────────────────────────────┐   │
│  │ I'd be happy to help! What    │   │
│  │ would you like to know?       │   │
│  │                               │   │
│  │ [AI]                       2h │   │
│  └───────────────────────────────┘   │
│                                       │
│  ┌───────────────────────────────┐   │
│  │ Can you explain quantum       │   │
│  │ entanglement in simple terms? │   │
│  │                               │   │
│  │ 2h                       [You]│   │
│  └───────────────────────────────┘   │
│                                       │
│  ┌───────────────────────────────┐   │
│  │ Certainly! Quantum            │   │
│  │ entanglement is a phenomenon  │   │
│  │ where two particles become    │   │
│  │ connected in such a way...    │   │
│  │                               │   │
│  │ [AI]                       2h │   │
│  └───────────────────────────────┘   │
│                                       │
│                                       │
│                                   [▶] │
└───────────────────────────────────────┘
```

**Components:**
- CenterAlignedTopAppBar:
  - Back button
  - Conversation title (centered, title medium)
- LazyColumn with message bubbles:
  - **User messages:**
    - Right-aligned
    - Primary color background
    - White text
    - Max width: 280dp
    - Rounded corners: 16dp (except bottom-right: 4dp)
    - "[You]" label (body small)
    - Timestamp (body small, bottom-left)
  - **AI messages:**
    - Left-aligned
    - Surface variant color background
    - On-surface color text
    - Max width: 280dp
    - Rounded corners: 16dp (except bottom-left: 4dp)
    - "[AI]" label (body small)
    - Timestamp (body small, bottom-right)
- FloatingActionButton (▶ play icon):
  - Bottom-right position
  - Primary color
  - "Continue chat" tooltip

**User Actions:**
- Scroll through message history
- Click ▶ FAB → Navigate to LlmChatScreen with loaded conversation
- Navigate back to ConversationListScreen

**States:**
- Loading: Circular progress in center
- Success: Message history displayed
- Error: Error message with retry button
- Empty: (shouldn't occur - conversations always have messages)

**Timestamps:**
- "Now" (less than 1 minute ago)
- "Xm" (X minutes ago)
- "Xh" (X hours ago)
- "Yesterday at HH:MM"
- "MMM DD at HH:MM" (older)

**Design Improvements:**
- ✅ Clean message bubble design
- ✅ Clear sender identification
- 💡 Consider: Message actions (copy, share individual message)
- 💡 Consider: Search within conversation
- 💡 Consider: Jump to date (if long conversation)
- 💡 Consider: Show model used at top of screen
- 💡 Consider: Show total message count
- 💡 Consider: Scroll to bottom FAB (when scrolled up)
- 💡 Consider: Show images/audio if conversation includes them
- 💡 Consider: Export conversation from this screen
- 💡 Consider: Edit conversation title from here
- 💡 Consider: Delete conversation button in top bar

---

### Screen 7: Settings Screen

**File:** `app/src/main/java/ai/ondevice/app/ui/settings/SettingsScreen.kt`
**Purpose:** App configuration and data management

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  Settings                          │
├───────────────────────────────────────┤
│                                       │
│  ━━ APP INFO ━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  OnDevice AI                          │
│  🔒 Your data stays on your device    │
│                                       │
│  ━━ APPEARANCE ━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  Theme                                │
│  ┌─────┬─────┬──────┐                │
│  │ Auto│Light│ Dark │                │
│  └─────┴─────┴──────┘                │
│                                       │
│  Text Size                            │
│  ┌──────┬────────┬───────┐           │
│  │Small │ Medium │ Large │           │
│  └──────┴────────┴───────┘           │
│                                       │
│  ━━ STORAGE & DATA ━━━━━━━━━━━━━━━━  │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ 💾 Storage Usage                │ │
│  │                                 │ │
│  │ 45.2 MB used                    │ │
│  │ 127 conversations (1,834 msgs)  │ │
│  │                                 │ │
│  │ ⚠️ Storage high - consider      │ │
│  │    cleaning up old chats        │ │
│  └─────────────────────────────────┘ │
│                                       │
│  [Export Conversations]               │
│                                       │
│  Auto-delete old conversations        │
│  [ Never ▼ ]                          │
│    • Never                            │
│    • After 30 days                    │
│    • After 90 days                    │
│    • After 1 year                     │
│                                       │
│  💡 Starred conversations are never   │
│     auto-deleted                      │
│                                       │
│  ━━ MODEL MANAGER ━━━━━━━━━━━━━━━━━  │
│                                       │
│  Downloaded Models                    │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Gemma-3n-E2B-it                 │ │
│  │ 3.5 GB                          │ │
│  │ 💬 Text  📷 Vision  🎤 Audio    │ │
│  │                     [🗑️ Delete] │ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Gemma3-1B-IT                    │ │
│  │ 558 MB                          │ │
│  │ 💬 Text                         │ │
│  │                     [🗑️ Delete] │ │
│  └─────────────────────────────────┘ │
│                                       │
│  [Download More Models]               │
│                                       │
│  ━━ LEGAL ━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  [View Open Source Licenses]          │
│  [View Terms of Service]              │
│                                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  Version 1.0.7                        │
│                                       │
└───────────────────────────────────────┘
```

**Storage Warning States:**

**Normal (< 50MB):**
```
┌─────────────────────────────────┐
│ 💾 Storage Usage                │
│                                 │
│ 12.3 MB used                    │
│ 45 conversations (678 messages) │
└─────────────────────────────────┘
```

**Warning (50-100MB):**
```
┌─────────────────────────────────┐
│ 💾 Storage Usage                │
│                                 │
│ 73.5 MB used                    │
│ 156 conversations (2,341 msgs)  │
│                                 │
│ ⚠️ Storage getting high         │
└─────────────────────────────────┘
```

**Critical (> 100MB):**
```
┌─────────────────────────────────┐
│ 💾 Storage Usage                │
│                                 │
│ 127.8 MB used                   │
│ 289 conversations (4,892 msgs)  │
│                                 │
│ 🚨 Storage critical - delete    │
│    old conversations            │
└─────────────────────────────────┘
```

**Export Dialog:**
```
┌─────────────────────────────────┐
│  Export Conversations           │
│─────────────────────────────────│
│  Choose format:                 │
│                                 │
│  ○ Markdown (.md)               │
│  ○ JSON (.json)                 │
│                                 │
│  127 conversations will be      │
│  exported.                      │
│                                 │
│  [Cancel]         [Export]      │
└─────────────────────────────────┘
```

**Model Delete Confirmation:**
```
┌─────────────────────────────────┐
│  Delete Model?                  │
│─────────────────────────────────│
│  Gemma-3n-E2B-it (3.5 GB) will  │
│  be permanently deleted.        │
│                                 │
│  You can re-download it later.  │
│                                 │
│  [Cancel]         [Delete]      │
└─────────────────────────────────┘
```

**Components:**

**Section: App Info**
- App name text (title large)
- Privacy statement with lock icon
- Body small, muted color

**Section: Appearance**
- Theme label (title small)
- SegmentedButtonRow (3 buttons):
  - Auto (selected if dynamic)
  - Light
  - Dark
- Text size label (title small)
- SegmentedButtonRow (3 buttons):
  - Small
  - Medium (default)
  - Large

**Section: Storage & Data**
- Storage usage card:
  - Icon + "Storage Usage" title
  - Size text (headline small)
  - Conversation/message count (body small)
  - Warning message (if applicable)
  - Card color changes based on warning level:
    - Normal: surface variant
    - Warning: warning container
    - Critical: error container
- Export button (outlined, full width)
- Auto-delete label (title small)
- Dropdown menu (outlined)
- Info text about starred conversations

**Section: Model Manager**
- "Downloaded Models" label (title small)
- Model cards (for each downloaded model):
  - Model name (title medium)
  - Size (body medium, muted)
  - Capability badges (chips)
  - Delete button (text button, right-aligned)
- "Download More Models" button (outlined, full width)
- Empty state if no models:
  - Icon + "No models downloaded" message
  - "Download a model" button

**Section: Legal**
- "View Open Source Licenses" button (text button)
- "View Terms of Service" button (text button)

**Footer:**
- Version text (body small, muted, centered)

**User Actions:**
- Change theme (Auto/Light/Dark)
- Change text size (Small/Medium/Large)
- View storage usage
- Export conversations (choose Markdown/JSON)
- Change auto-delete schedule
- Delete individual models (with confirmation)
- Download more models → Navigate to ModelSelectionScreen
- View open source licenses → Navigate to LicenseViewerScreen
- View Terms of Service → Open TosDialog
- Navigate back to chat

**States:**
- Default: All sections visible and functional
- Loading models: Spinner in Model Manager section
- Export in progress: Loading indicator on Export button
- Export complete: Snackbar "Conversations exported"
- Model deleted: Snackbar "Model deleted"
- Auto-cleanup running: Background process (no UI indicator)

**Design Improvements:**
- ✅ Well-organized sections
- ✅ Clear visual hierarchy with dividers
- ✅ Color-coded storage warnings
- 💡 Consider: Storage breakdown by conversation/images/audio
- 💡 Consider: "Clear cache" button
- 💡 Consider: Model usage statistics (how often used)
- 💡 Consider: Conversation backup to cloud (optional)
- 💡 Consider: Import conversations
- 💡 Consider: Reset app data button (with strong warning)
- 💡 Consider: About screen (separate from settings)
- 💡 Consider: Help & Support section with FAQ
- 💡 Consider: Send feedback button
- 💡 Consider: Check for updates button
- 💡 Consider: Notification settings

---

### Screen 8: GemmaTermsDialog (Before Gemma Model Download)

**File:** `app/src/main/java/ai/ondevice/app/ui/common/tos/GemmaTermsDialog.kt`
**Trigger:** Before downloading any Gemma model (one-time acceptance)
**Dismissible:** Yes (cancels download)

#### Visual Layout

```
┌─────────────────────────────────────┐
│  [ℹ️]  Gemma Terms of Use Required  │
│─────────────────────────────────────│
│                                     │
│  To download Gemma-3n-E2B-it, you   │
│  must accept Google's Gemma Terms   │
│  of Use.                            │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Key Terms Summary:                 │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ ⚠️  Prohibited Uses         │   │
│  │                             │   │
│  │ • No illegal activities     │   │
│  │ • No harm to minors         │   │
│  │ • No generating/promoting   │   │
│  │   violence                  │   │
│  │ • No fraud or deception     │   │
│  │ • No defamation/harassment  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ✓ Personal use                     │
│  ✓ Research and education           │
│  ✓ Commercial applications          │
│    (with restrictions)              │
│  ✓ Model modifications and          │
│    derivatives                      │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Important:                         │
│                                     │
│  Google may restrict usage remotely │
│  if terms are violated. By clicking │
│  "I Accept" below, you agree to     │
│  comply with all Gemma terms and    │
│  restrictions.                      │
│                                     │
│  Links:                             │
│  • Read Full Gemma Terms of Use ↗   │
│  • Read Prohibited Use Policy ↗     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Gemma is provided under and        │
│  subject to the Gemma Terms of Use  │
│  found at ai.google.dev/gemma/terms │
│                                     │
│─────────────────────────────────────│
│  [Decline]          [I Accept]      │
└─────────────────────────────────────┘
```

**Components:**
- AlertDialog (Material 3)
- Info icon (24dp, primary color)
- Title text (headline small)
- Body text (body medium)
- Section dividers
- "Key Terms Summary:" header (title small, bold)
- Prohibited uses card:
  - Red background (error container)
  - Warning icon (⚠️)
  - Bulleted list (body small)
- Permitted uses list (checkmarks, body medium)
- "Important:" section (title small, bold)
- Warning text (body small)
- Clickable links (primary color, underlined)
- Attribution text (body small, muted)
- Two buttons:
  - Decline (text button)
  - I Accept (filled button, primary color)

**User Actions:**
- Read terms summary
- Scroll through content (if needed)
- Click "Read Full Gemma Terms of Use" → Opens browser
- Click "Read Prohibited Use Policy" → Opens browser
- Click "I Accept" → Records acceptance, proceeds with download
- Click "Decline" → Dismisses dialog, cancels download

**Design Improvements:**
- ✅ Clear visual hierarchy
- ✅ Color-coded prohibited uses (red warning)
- ✅ Legal attribution included
- 💡 Consider: Checkbox "I have read and accept" for explicit confirmation
- 💡 Consider: Version number of terms displayed
- 💡 Consider: Timestamp of acceptance stored and displayed
- 💡 Consider: Ability to review accepted terms later in Settings

---

### Screen 9: LlmSingleTurnScreen (Prompt Lab)

**File:** `app/src/main/java/ai/ondevice/app/ui/llmsingleturn/LlmSingleTurnScreen.kt`
**Purpose:** Experiment with AI prompts using templates (single-turn interactions)

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  [Gemma3-1B-IT ▼]      ⋯  ⚙️      │
├───────────────────────────────────────┤
│                                       │
│  ━━ PROMPT TEMPLATES ━━━━━━━━━━━━━━  │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ 📝 Explain a Concept            │ │
│  │                                 │ │
│  │ "Explain [topic] in simple      │ │
│  │  terms for a beginner."         │ │
│  │                                 │ │
│  │              [Use This Template]│ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ 💡 Generate Ideas               │ │
│  │                                 │ │
│  │ "Give me 5 creative ideas for   │ │
│  │  [topic]."                      │ │
│  │                                 │ │
│  │              [Use This Template]│ │
│  └─────────────────────────────────┘ │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ ✍️ Write Content                │ │
│  │                                 │ │
│  │ "Write a [length] [type] about  │ │
│  │  [topic]."                      │ │
│  │                                 │ │
│  │              [Use This Template]│ │
│  └─────────────────────────────────┘ │
│                                       │
├═══════════════════════════════════════┤
│                                       │
│  ━━ RESPONSE ━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  Prompt: "Explain quantum computing  │
│           in simple terms for a      │
│           beginner."                 │
│                                       │
│  ─────────────────────────────────── │
│                                       │
│  Response:                            │
│                                       │
│  Quantum computing is a new type of   │
│  computing that uses the principles   │
│  of quantum mechanics to process      │
│  information. Unlike traditional      │
│  computers that use bits (0 or 1),    │
│  quantum computers use qubits that    │
│  can be both 0 and 1 at the same...   │
│                                       │
│  [Response continues...]              │
│                                       │
│  [⏸️ Stop Generation]                 │
│                                       │
└───────────────────────────────────────┘
```

**Components:**

**Top Panel (Prompt Templates):**
- Section header: "PROMPT TEMPLATES"
- Scrollable list of template cards:
  - Icon + Template name (title medium)
  - Template text preview (body medium, muted)
  - "Use This Template" button (outlined)
- Each card is clickable

**Bottom Panel (Response):**
- Section header: "RESPONSE"
- Prompt display area:
  - "Prompt:" label (body small, muted)
  - Actual prompt text (body medium)
- Divider
- Response display area:
  - "Response:" label (body small, muted)
  - AI response text (body medium)
  - Scrollable if long
- Stop button (if generating)

**Split View:**
- Vertical divider (draggable to resize panels)
- Top panel: 40% of screen height (default)
- Bottom panel: 60% of screen height (default)

**User Actions:**
- Select prompt template → Sends prompt to model
- Adjust split view ratio (drag divider)
- Stop generation (⏸️ button)
- Change model (dropdown in top bar)
- Configure model settings (⚙️ icon)
- Navigate back (← button)

**States:**

**Idle (No Prompt Sent):**
```
━━ RESPONSE ━━━━━━━━━━━━━━━━━━

Select a template above to generate
a response.
```

**Generating:**
```
━━ RESPONSE ━━━━━━━━━━━━━━━━━━

Prompt: "Explain quantum computing..."

─────────────────────────────────

Response:

[Streaming text appears here...]

[⏸️ Stop Generation]
```

**Complete:**
```
━━ RESPONSE ━━━━━━━━━━━━━━━━━━

Prompt: "Explain quantum computing..."

─────────────────────────────────

Response:

[Full response text]
```

**Design Improvements:**
- ✅ Clean split view design
- ✅ Template-based interaction
- 💡 Consider: Custom prompt input (not just templates)
- 💡 Consider: Save favorite prompts
- 💡 Consider: Share response button
- 💡 Consider: Copy response button
- 💡 Consider: Regenerate with same prompt button
- 💡 Consider: Template categories (Coding, Writing, Ideas, etc.)
- 💡 Consider: User-created template library
- 💡 Consider: Prompt history (recently used prompts)
- 💡 Consider: Export response as text file

---

### Screen 10: Image Generation Screen

**File:** `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt`
**Purpose:** Generate images from text prompts using on-device Stable Diffusion

#### Visual Layout - Input Mode

```
┌───────────────────────────────────────┐
│  ←  Image Generation                  │
├───────────────────────────────────────┤
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ Enter your prompt (max 500)     │ │
│  │                                 │ │
│  │ A serene mountain landscape     │ │
│  │ with a crystal clear lake...    │ │
│  │                                 │ │
│  │                                 │ │
│  │                          243/500│ │
│  └─────────────────────────────────┘ │
│                                       │
│  Iterations: 20                       │
│  ├─────────●──────────────┤           │
│  5                        50          │
│                                       │
│  💡 Higher iterations = better        │
│     quality, longer generation time   │
│                                       │
│  [Generate Image]                     │
│                                       │
│  ━━ TIPS ━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  • Be specific and descriptive        │
│  • Mention art style (e.g. "oil       │
│    painting", "digital art")          │
│  • Include lighting details           │
│  • Describe colors and mood           │
│  • Example: "A futuristic city at     │
│    sunset, cyberpunk style, neon      │
│    lights, dramatic lighting"         │
│                                       │
└───────────────────────────────────────┘
```

#### Visual Layout - Generating

```
┌───────────────────────────────────────┐
│  ←  Image Generation                  │
├───────────────────────────────────────┤
│                                       │
│         Generating Image...           │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │      [Intermediate Image]       │ │
│  │         512 x 512               │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                       │
│  Step 14 / 20                         │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░  70%         │
│                                       │
│  Estimated time: 45 seconds           │
│                                       │
│  [Cancel]                             │
│                                       │
└───────────────────────────────────────┘
```

#### Visual Layout - Result

```
┌───────────────────────────────────────┐
│  ←  Image Generation                  │
├───────────────────────────────────────┤
│                                       │
│         Generated Image                │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │                                 │ │
│  │      [Final Image Display]      │ │
│  │         512 x 512               │ │
│  │                                 │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│                                       │
│  Prompt: "A serene mountain           │
│  landscape with a crystal clear       │
│  lake..."                             │
│                                       │
│  ┌─────┬──────┬──────┬──────┐        │
│  │ 💾  │  📤  │  🔄  │  ✏️  │        │
│  │Save │Share │Again │ New  │        │
│  └─────┴──────┴──────┴──────┘        │
│                                       │
│  💡 Tips:                             │
│  • Save: Downloads to gallery         │
│  • Share: Share via any app           │
│  • Again: Same prompt, new result     │
│  • New: Start with a new prompt       │
│                                       │
└───────────────────────────────────────┘
```

#### Visual Layout - Not Downloaded

```
┌───────────────────────────────────────┐
│  ←  Image Generation                  │
├───────────────────────────────────────┤
│                                       │
│                                       │
│             [🎨 Icon]                 │
│                                       │
│        Image Generation               │
│                                       │
│  Create beautiful images from text    │
│  prompts using on-device Stable       │
│  Diffusion.                           │
│                                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                                       │
│  📦 Model Required: 1.9 GB            │
│  📱 RAM Required: 6 GB                │
│  ⏱️ Generation Time: ~1-2 minutes     │
│                                       │
│  [Go to Model Manager]                │
│                                       │
└───────────────────────────────────────┘
```

**Components:**

**Input Mode:**
- TopAppBar with back button and title
- Multi-line text field (outlined):
  - Max 500 characters
  - Character counter (bottom-right)
  - Placeholder: "Enter your prompt"
- Iterations slider:
  - Range: 5-50
  - Default: 20
  - Label shows current value
- Explanation text (body small, muted)
- "Generate Image" button (filled, primary color, full width)
- Tips card:
  - Section header
  - Bulleted list of tips
  - Example prompt

**Generating Mode:**
- "Generating Image..." title
- Image preview area (512x512):
  - Shows intermediate result
  - Updates periodically
- Progress indicator:
  - "Step X / Y" text
  - Linear progress bar with percentage
  - Estimated time remaining
- "Cancel" button (outlined, full width)

**Result Mode:**
- "Generated Image" title
- Final image display (512x512)
- Prompt text (body small, muted)
- Action buttons row (4 buttons):
  - Save (💾 icon)
  - Share (📤 icon)
  - Generate Again (🔄 icon)
  - New Prompt (✏️ icon)
- Tips card explaining buttons

**Not Downloaded Mode:**
- Large icon (🎨, 64dp)
- Title: "Image Generation"
- Description text
- Requirements card:
  - Model size
  - RAM requirement
  - Generation time estimate
- "Go to Model Manager" button

**User Actions:**

**Input Mode:**
- Type prompt (up to 500 chars)
- Adjust iterations slider (5-50)
- Click "Generate Image" → Starts generation

**Generating Mode:**
- View progress and intermediate results
- Click "Cancel" → Returns to input mode

**Result Mode:**
- Click "Save" → Saves to gallery (Pictures/OnDeviceAI)
- Click "Share" → Opens system share sheet
- Click "Generate Again" → Same prompt, new random seed
- Click "New Prompt" → Clears prompt, returns to input

**Not Downloaded Mode:**
- Click "Go to Model Manager" → Navigate to settings

**States:**
- Input: Ready to accept prompt
- Validating: Checking prompt and model
- Generating: Creating image with progress
- Complete: Showing result with actions
- Error: Snackbar with error message
- Not Downloaded: Info screen with download prompt

**Design Improvements:**
- ✅ Clear progress indication
- ✅ Intermediate image previews
- ✅ Helpful tips and examples
- 💡 Consider: Gallery of previously generated images
- 💡 Consider: Preset prompts/examples (click to use)
- 💡 Consider: Image size selector (256x256, 512x512, 768x768)
- 💡 Consider: Negative prompts (what to avoid)
- 💡 Consider: Seed input for reproducibility
- 💡 Consider: Style presets (Realistic, Anime, Oil Painting, etc.)
- 💡 Consider: Multiple image generation (batch of 4)
- 💡 Consider: Prompt enhancement suggestions
- 💡 Consider: Image history/gallery view
- 💡 Consider: Edit generated image (inpainting)
- 💡 Consider: Upscale generated image

---

### Screen 11: License Viewer Screen

**File:** `app/src/main/java/ai/ondevice/app/ui/settings/LicenseViewerScreen.kt`
**Purpose:** Display legal text files (licenses, notices, attributions)

#### Visual Layout

```
┌───────────────────────────────────────┐
│  ←  Open Source License               │
├───────────────────────────────────────┤
│                                       │
│  Apache License                       │
│  Version 2.0, January 2004            │
│  http://www.apache.org/licenses/      │
│                                       │
│  TERMS AND CONDITIONS FOR USE,        │
│  REPRODUCTION, AND DISTRIBUTION        │
│                                       │
│  1. Definitions.                      │
│                                       │
│  "License" shall mean the terms and   │
│  conditions for use, reproduction,    │
│  and distribution as defined by       │
│  Sections 1 through 9 of this         │
│  document.                            │
│                                       │
│  [License text continues...]          │
│                                       │
│                                       │
│                                       │
│                                       │
└───────────────────────────────────────┘
```

**Components:**
- TopAppBar:
  - Back button
  - Dynamic title based on file type:
    - "Open Source License" (for LICENSE)
    - "Legal Notices" (for NOTICE)
    - "Third-Party Attributions" (for ATTRIBUTIONS)
- Scrollable text content:
  - Body small font
  - On-surface color
  - Full padding for readability

**User Actions:**
- Scroll through content
- Navigate back

**File Sources:**
- `assets/legal/LICENSE` → Apache 2.0 license text
- `assets/legal/NOTICE` → Copyright notices
- `assets/legal/ATTRIBUTIONS.txt` → Third-party attributions (includes Gemma terms)

**Design Improvements:**
- ✅ Simple, readable design
- 💡 Consider: Text size adjustment
- 💡 Consider: Copy text button
- 💡 Consider: Share license text
- 💡 Consider: Dark mode optimization for long reading
- 💡 Consider: Section navigation (table of contents)

---

## User Flows

### Flow 1: First-Time User Setup

```
App Launch
    ↓
[Splash Screen] (1s)
    ↓
[TOS Dialog] ← User must accept
    ↓ Accept
[Model Selection Screen]
    ↓ Select model (e.g., Gemma-3n-E2B-it)
    ↓
[Gemma Terms Dialog] ← User must accept for Gemma models
    ↓ Accept
Model downloads (with progress)
    ↓ Download complete
[LLM Chat Screen] ← Main app screen
```

### Flow 2: Returning User (Model Downloaded)

```
App Launch
    ↓
[Splash Screen] (1s)
    ↓
[LLM Chat Screen] ← Direct navigation (TOS already accepted, model already downloaded)
```

### Flow 3: Chat Conversation

```
[LLM Chat Screen]
    ↓ Type message
    ↓ Send
AI generates response
    ↓
User continues conversation
    ↓ Click conversation history icon (📜)
[Conversation List Screen]
    ↓ Click conversation
[Conversation Detail Screen] (read-only view)
    ↓ Click "Continue Chat" FAB
[LLM Chat Screen] with loaded conversation
```

### Flow 4: Model Management

```
[LLM Chat Screen]
    ↓ Click settings icon (⚙️)
[Settings Screen]
    ↓ Scroll to Model Manager section
    ↓ Click "Download More Models"
[Model Selection Screen]
    ↓ Select new model
    ↓ Accept Gemma terms (if needed)
Model downloads
    ↓ Complete
[Settings Screen] ← Returns after download
    ↓ Navigate back
[LLM Chat Screen] ← Can now switch to new model
```

### Flow 5: Image Generation

```
[LLM Chat Screen]
    ↓ User navigates to Image Generation task
[Image Generation Screen - Not Downloaded]
    ↓ Click "Go to Model Manager"
[Settings Screen]
    ↓ Download Stable Diffusion model
Model downloads (1.9 GB)
    ↓ Complete
[Image Generation Screen - Input Mode]
    ↓ Enter prompt
    ↓ Adjust iterations
    ↓ Click "Generate Image"
[Image Generation Screen - Generating]
    ↓ Shows progress and intermediate results
    ↓ Generation complete
[Image Generation Screen - Result]
    ↓ User can save, share, or regenerate
```

### Flow 6: Conversation Management

```
[Conversation List Screen]
    ↓ Long-press or click ⋮ on conversation
Context menu appears
    ↓ Select "Rename"
[Rename Dialog]
    ↓ Enter new name
    ↓ Click "Rename"
Conversation renamed
    ↓
[Conversation List Screen] ← Updated with new name
```

### Flow 7: Data Export

```
[Settings Screen]
    ↓ Scroll to Storage & Data section
    ↓ Click "Export Conversations"
[Export Dialog]
    ↓ Select format (Markdown/JSON)
    ↓ Click "Export"
System share sheet appears
    ↓ User selects destination (Email, Drive, etc.)
Export complete
    ↓
[Settings Screen] ← Snackbar: "Conversations exported"
```

---

## Component Library

### Common Components Used Across Screens

#### 1. **TopAppBar Variants**

**CenterAlignedTopAppBar:**
```kotlin
┌───────────────────────────────────────┐
│  ←          Title Text            ⋮   │
└───────────────────────────────────────┘
```
- Used in: ConversationDetailScreen
- Back button (left)
- Title (centered)
- Optional actions (right)

**SmallTopAppBar:**
```kotlin
┌───────────────────────────────────────┐
│  ←  Title Text                    ⋮   │
└───────────────────────────────────────┘
```
- Used in: Most screens
- Back button (left)
- Title (left-aligned)
- Optional actions (right)

**ModelPageAppBar:**
```kotlin
┌───────────────────────────────────────┐
│  ←  [Model Name ▼]    ⋯  ⚙️  📜       │
└───────────────────────────────────────┘
```
- Used in: LlmChatScreen, LlmSingleTurnScreen
- Back button
- Model selector dropdown
- More options menu
- Settings icon
- Conversation history icon

#### 2. **Buttons**

**Filled Button:**
```kotlin
┌─────────────────┐
│  Button Text    │
└─────────────────┘
```
- Primary color background
- On-primary color text
- Used for primary actions (Send, Accept, Generate)

**Outlined Button:**
```kotlin
┌─────────────────┐
│  Button Text    │ (outline only)
└─────────────────┘
```
- Transparent background
- Primary color border
- Used for secondary actions (Cancel, Export, Download More)

**Text Button:**
```kotlin
  Button Text
```
- No background, no border
- Primary color text
- Used for tertiary actions (links, dialog dismiss)

**Floating Action Button:**
```kotlin
    ┌───┐
    │ + │  (circular)
    └───┘
```
- Primary color background
- Large size: 56dp
- Small size: 40dp
- Used for primary floating actions (New Chat, Continue Chat)

**Segmented Button Row:**
```kotlin
┌─────┬──────┬──────┐
│ Opt1│ Opt2 │ Opt3 │
└─────┴──────┴──────┘
```
- Material 3 component
- Used for Theme and Text Size selection
- Single selection only

#### 3. **Text Fields**

**Outlined TextField:**
```kotlin
┌─────────────────────────────────┐
│ Label                           │
│ User input text...              │
└─────────────────────────────────┘
```
- Border when not focused
- Thicker border when focused
- Label floats above when focused
- Used for: Message input, search, rename dialog

**Multi-line TextField:**
```kotlin
┌─────────────────────────────────┐
│ Enter your prompt               │
│                                 │
│ Multi-line text here...         │
│                                 │
│                          243/500│
└─────────────────────────────────┘
```
- Supports multiple lines
- Character counter optional
- Used for: Image generation prompts, long text input

#### 4. **Cards**

**Standard Card:**
```kotlin
┌─────────────────────────────────┐
│ Card Title                      │
│                                 │
│ Card content goes here...       │
│                                 │
│ [Optional Action]               │
└─────────────────────────────────┘
```
- Surface color background
- 12dp border radius
- 1dp elevation
- 16dp padding
- Used throughout app

**Model Card:**
```kotlin
┌─────────────────────────────────┐
│ Gemma-3n-E2B-it          ⭐ Best│
│ For vision & audio              │
│                                 │
│ 📱 8GB RAM    📦 3.5 GB         │
│ 💬 Text  📷 Vision  🎤 Audio    │
│                                 │
│           [Download]            │
└─────────────────────────────────┘
```
- Specialized card for model display
- Badges for capabilities
- Download button or status
- Used in: ModelSelectionScreen, Settings

**Warning Card:**
```kotlin
┌─────────────────────────────────┐
│ ⚠️ Warning Title                │
│                                 │
│ Warning message text...         │
└─────────────────────────────────┘
```
- Warning/error container color
- Used for storage warnings, errors

#### 5. **Message Bubbles**

**User Message:**
```kotlin
          ┌───────────────────┐
          │ User message text │
          │           12:30 PM│
     [You]└───────────────────┘
```
- Primary color background
- Right-aligned
- Bottom-right corner squared (4dp)
- Max width: 280dp

**AI Message:**
```kotlin
┌───────────────────┐
│ AI response text  │
│ 12:30 PM          │
└───────────────────┘[AI]
```
- Surface variant background
- Left-aligned
- Bottom-left corner squared (4dp)
- Max width: 280dp

#### 6. **Lists**

**LazyColumn:**
- Used for scrollable lists
- Items load on-demand
- Used in: ConversationListScreen, ModelSelectionScreen

**LazyRow:**
- Horizontal scrollable list
- Used in: Category tabs (HomeScreen)

#### 7. **Dialogs**

**AlertDialog:**
```kotlin
┌─────────────────────────────────┐
│  [Icon]  Dialog Title           │
│─────────────────────────────────│
│                                 │
│  Dialog content text...         │
│                                 │
│─────────────────────────────────│
│  [Cancel]         [Confirm]     │
└─────────────────────────────────┘
```
- Material 3 component
- Optional icon
- Title
- Content
- Actions (1-2 buttons)
- Used for: TOS, Gemma Terms, confirmations

#### 8. **Progress Indicators**

**Circular Progress:**
```kotlin
    ⟳  (spinning)
```
- Indeterminate progress
- Used for loading states

**Linear Progress:**
```kotlin
▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░  50%
```
- Determinate progress
- Shows percentage
- Used for downloads, generation

#### 9. **Icons**

**Icon Sizes:**
- 16dp: Small inline icons
- 24dp: Standard button/list icons
- 32dp: Large feature icons
- 64dp: Task/category icons

**Common Icons:**
- ← (arrow_back): Navigation back
- ⋮ (more_vert): More options menu
- ⚙️ (settings): Settings
- 📜 (history): Conversation history
- + (add): New item
- ⭐ (star): Starred/favorite
- 🗑️ (delete): Delete action
- ✏️ (edit): Edit action
- 💬 (chat): Text chat capability
- 📷 (camera): Vision capability
- 🎤 (mic): Audio capability
- 📦 (package): Download/size
- 📱 (phone): Device requirement
- ⚠️ (warning): Warning message
- ℹ️ (info): Information
- ↗ (send): Send message
- ⏸️ (pause): Stop generation
- 💾 (save): Save file
- 📤 (share): Share action

#### 10. **Animations**

**Fade In:**
- Duration: 300ms
- Used for: Screen transitions, dialogs

**Slide In:**
- Duration: 500ms
- Easing: EaseOutExpo
- Used for: Screen navigation

**Scale:**
- Duration: 200ms
- Used for: Button taps, icon interactions

**Revealing Text:**
- Duration: 800ms
- Gradient animation
- Used for: ModelSelectionScreen title

**Stagger:**
- Delay: 100ms between items
- Used for: Model card list entrance

---

## Improvement Opportunities

### High Priority (UX Critical)

#### 1. **Message Actions in Chat**
**Current:** No way to interact with individual messages
**Improvement:**
- Long-press message bubble → Show context menu:
  - Copy text
  - Regenerate this response
  - Edit my message (user messages)
  - Delete message
  - Share message
- Benefits: Better user control, common in modern chat apps

#### 2. **Markdown Rendering in Chat**
**Current:** Plain text responses only
**Improvement:**
- Render markdown formatting:
  - **Bold**, *italic*
  - Code blocks with syntax highlighting
  - Bulleted/numbered lists
  - Headers
  - Links (clickable)
- Benefits: Much better readability for technical responses, code examples

#### 3. **Scroll to Bottom FAB**
**Current:** Must manually scroll to see new messages
**Improvement:**
- Show floating "scroll to bottom" button when scrolled up
- Auto-hide when at bottom
- Badge shows number of new messages
- Benefits: Essential for long conversations

#### 4. **Conversation Title Auto-Generation**
**Current:** Manual naming only
**Improvement:**
- Auto-generate title from first user message
- Example: "Tell me about quantum physics" → "Quantum Physics"
- Allow manual override
- Benefits: Better organization without user effort

#### 5. **Model Download Queue**
**Current:** Can only download one model at a time
**Improvement:**
- Multi-select models on ModelSelectionScreen
- Download queue with priority ordering
- Background downloads continue when app closed
- Benefits: Better first-time experience, less waiting

### Medium Priority (Nice to Have)

#### 6. **Swipe Actions in Conversation List**
**Current:** Must tap ⋮ for actions
**Improvement:**
- Swipe left → Delete (with undo snackbar)
- Swipe right → Star/Unstar
- Benefits: Faster, more intuitive interaction

#### 7. **Conversation Grouping**
**Current:** Flat list of all conversations
**Improvement:**
- Group by date: Today, Yesterday, This Week, This Month, Older
- Collapsible sections
- Benefits: Better organization for power users

#### 8. **Image Preview in Vision Chat**
**Current:** No visual indication when image attached
**Improvement:**
- Show thumbnail of attached image in message bubble
- Click to view full size
- Benefits: Better context in conversation history

#### 9. **Typing Indicator Animation**
**Current:** Just "Generating..." text
**Improvement:**
- Animated dots: "●●●" (pulsing)
- Or: "AI is typing..." with animated ellipsis
- Benefits: More engaging, polished feel

#### 10. **Storage Breakdown in Settings**
**Current:** Just total size
**Improvement:**
- Pie chart showing:
  - Conversations (text)
  - Images (if vision used)
  - Audio (if audio used)
  - Models (separate section)
- Benefits: Better understanding of storage usage

#### 11. **Model Usage Statistics**
**Current:** No visibility into model performance
**Improvement:**
- Show per model:
  - Times used
  - Average response time
  - Last used date
  - Total tokens generated
- Benefits: Helps users choose best model for their needs

#### 12. **Prompt Enhancement Suggestions**
**Current:** Raw prompts only
**Improvement:**
- For image generation, suggest improvements:
  - "Add art style"
  - "Describe lighting"
  - "Mention colors"
- Auto-enhance button
- Benefits: Better image quality without expertise

### Low Priority (Polish)

#### 13. **Empty State Illustrations**
**Current:** Simple icon + text
**Improvement:**
- Custom illustrations for:
  - No conversations yet
  - No models downloaded
  - Empty search results
- Benefits: More polished, brand personality

#### 14. **Onboarding Tutorial**
**Current:** Jump straight in
**Improvement:**
- Optional 3-step tutorial after TOS:
  1. "This is the chat screen"
  2. "Switch models here"
  3. "View conversation history here"
- Skip button
- Benefits: Lower friction for new users

#### 15. **Haptic Feedback**
**Current:** None
**Improvement:**
- Light vibration on:
  - Button taps
  - Swipe actions
  - Generation complete
- Benefits: More tactile, premium feel

#### 16. **Dark Mode Image Generation**
**Current:** Same UI in light/dark
**Improvement:**
- Optimize image viewer for dark mode
- Black background for images
- Better contrast
- Benefits: Easier on eyes at night

#### 17. **Conversation Export Formats**
**Current:** Markdown and JSON only
**Improvement:**
- Add: PDF, HTML, Plain Text
- Include metadata (model used, timestamp)
- Custom formatting options
- Benefits: More flexibility for sharing/archiving

---

## Figma Setup Guide

### Step 1: Create Figma File Structure

```
OnDevice AI Design System
│
├─ 📁 0 - Cover Page
│   └─ Project overview, version history
│
├─ 📁 1 - Design Tokens
│   ├─ Colors (Light theme, Dark theme)
│   ├─ Typography (Font sizes, weights)
│   ├─ Spacing (4dp grid)
│   ├─ Border Radius (8, 12, 16, 28dp)
│   └─ Elevation (shadows)
│
├─ 📁 2 - Components
│   ├─ Buttons (Filled, Outlined, Text, FAB)
│   ├─ Text Fields (Outlined, Multi-line)
│   ├─ Cards (Standard, Model, Warning)
│   ├─ Message Bubbles (User, AI)
│   ├─ TopAppBars (Centered, Small, ModelPage)
│   ├─ Dialogs (Alert, Confirmation)
│   ├─ Icons (16, 24, 32, 64dp)
│   └─ Progress Indicators (Circular, Linear)
│
├─ 📁 3 - Screens - Light Theme
│   ├─ 01 - Splash Screen
│   ├─ 02 - TOS Dialog
│   ├─ 03 - Model Selection
│   ├─ 04 - LLM Chat (Empty, Active, Generating)
│   ├─ 05 - Conversation List (Empty, Populated)
│   ├─ 06 - Conversation Detail
│   ├─ 07 - Settings
│   ├─ 08 - Gemma Terms Dialog
│   ├─ 09 - Prompt Lab
│   ├─ 10 - Image Generation (Input, Generating, Result)
│   └─ 11 - License Viewer
│
├─ 📁 4 - Screens - Dark Theme
│   └─ [Same as above, dark variants]
│
├─ 📁 5 - User Flows
│   ├─ First-Time Setup Flow
│   ├─ Chat Conversation Flow
│   ├─ Model Management Flow
│   ├─ Image Generation Flow
│   └─ Data Export Flow
│
├─ 📁 6 - Prototypes
│   ├─ Interactive prototype linking screens
│   └─ Animation specifications
│
└─ 📁 7 - Design Improvements
    ├─ Proposed redesigns
    ├─ New feature mockups
    └─ A/B testing variants
```

### Step 2: Import Material 3 Components

**Option A: Use Material 3 Design Kit (Recommended)**
1. Download: https://www.figma.com/community/file/1035203688168086460
2. Duplicate to your workspace
3. Link components to your file

**Option B: Build from Scratch**
1. Use Material 3 theme builder: https://material-foundation.github.io/material-theme-builder/
2. Export design tokens
3. Import into Figma variables

### Step 3: Set Up Variables (Figma Pro)

**Color Variables:**
```
Light Theme:
├─ Primary: #[Dynamic]
├─ On Primary: #FFFFFF
├─ Surface: #FFFBFE
├─ Surface Variant: #E7E0EC
├─ Error Container: #FDE7E9
└─ On Error Container: #410E0B

Dark Theme:
├─ Primary: #[Dynamic]
├─ On Primary: #000000
├─ Surface: #1C1B1F
├─ Surface Variant: #49454F
├─ Error Container: #93000A
└─ On Error Container: #FFDAD6
```

**Spacing Variables:**
```
XXS: 4px
XS: 8px
S: 12px
M: 16px
L: 24px
XL: 32px
XXL: 48px
```

**Typography Variables:**
```
Headline Small: 24px/32px, Regular
Title Large: 22px/28px, Regular
Title Medium: 16px/24px, Medium
Body Large: 16px/24px, Regular
Body Medium: 14px/20px, Regular
Body Small: 12px/16px, Regular
```

### Step 4: Create Component Library

#### Button Components:

**Filled Button:**
- Auto-layout: Horizontal, 16px padding
- Min width: 64dp
- Height: 40dp
- Border radius: 20dp
- Background: Primary color
- Text: On-Primary color, Label Large

**Outlined Button:**
- Same dimensions
- Border: 1px, Primary color
- Background: Transparent
- Text: Primary color

**Text Button:**
- No background or border
- Text: Primary color

#### Card Components:

**Standard Card:**
- Auto-layout: Vertical, 16px padding
- Border radius: 12dp
- Background: Surface color
- Shadow: Elevation 1

**Model Card:**
- Include variants:
  - Default state
  - Downloading state (with progress bar)
  - Downloaded state (with checkmark)

#### Message Bubble Components:

**User Message:**
- Auto-layout: Vertical, 12px padding
- Max width: 280px
- Border radius: 16dp (except bottom-right: 4dp)
- Background: Primary
- Text: On-Primary

**AI Message:**
- Same structure
- Border radius: 16dp (except bottom-left: 4dp)
- Background: Surface Variant
- Text: On-Surface

### Step 5: Build Screen Mockups

**For Each Screen:**
1. Create frame (360x800 for mobile, 1920x1080 for desktop preview)
2. Add status bar (system UI)
3. Add TopAppBar using component
4. Add content using auto-layout
5. Add navigation bar (if applicable)
6. Apply 8dp grid
7. Ensure all elements snap to grid

**States to Include:**
- Default/Empty state
- Populated state
- Loading state
- Error state
- Interaction states (hover, pressed, focused)

### Step 6: Create User Flow Diagrams

**Use FigJam or Miro for Flow Charts:**

Example structure:
```
[Screen Name]
    │
    ├─ Action 1 → [Next Screen]
    │
    ├─ Action 2 → [Dialog]
    │              │
    │              └─ Confirm → [Result Screen]
    │
    └─ Action 3 → [Settings]
```

### Step 7: Add Interactive Prototyping

**Prototype Settings:**
- Device: Android Phone
- Starting Frame: Splash Screen
- Flow: First-Time Setup

**Interactions to Add:**
- Button taps → Navigate to screen
- Back button → Navigate back
- Swipe gestures (if applicable)
- Scroll behavior
- Dialog open/close

**Animation Specs:**
- Smart Animate between frames
- Transition: Move In
- Direction: Left (for forward nav)
- Easing: Ease Out
- Duration: 300ms

### Step 8: Design Improvement Proposals

**Create "Before/After" Comparison Frames:**

Example:
```
┌─────────────────┬─────────────────┐
│   CURRENT       │   PROPOSED      │
│                 │                 │
│  [Screenshot]   │  [Redesign]     │
│                 │                 │
│  Issues:        │  Improvements:  │
│  • No markdown  │  • Markdown     │
│  • No actions   │  • Copy/Share   │
└─────────────────┴─────────────────┘
```

### Step 9: Documentation in Figma

**Add Description Fields:**
- Screen name and purpose
- Key components used
- User actions available
- States included
- Design notes/rationale

**Use Comments:**
- Tag team members for review
- Document design decisions
- Track feedback

**Create Annotation Layer:**
- Overlay frame with red boxes + numbers
- Match numbers to documentation

### Step 10: Handoff Preparation

**Developer Handoff Checklist:**
- ✅ All components have clear names
- ✅ Colors use variables (not hardcoded)
- ✅ Spacing follows 8dp grid
- ✅ Text styles are named and documented
- ✅ Auto-layout is used consistently
- ✅ Frames are properly named
- ✅ Assets exported as SVG/PNG (if needed)
- ✅ Prototype demonstrates all interactions
- ✅ Design tokens exported

**Export Options:**
- Inspect mode: Enabled
- Developer handoff: Configured
- Asset export: @1x, @2x, @3x for Android
- Code snippets: Jetpack Compose (if using plugin)

---

## Design Principles to Follow

### 1. **Material You / Material 3**
- Dynamic color from wallpaper
- Rounded corners throughout
- Elevated surfaces
- High contrast text
- Consistent spacing

### 2. **8dp Grid System**
- All measurements divisible by 8
- Exceptions: 4dp for fine-tuning
- Icon sizes: 16, 24, 32, 64dp
- Consistent padding/margins

### 3. **Typography Hierarchy**
- Clear distinction between heading and body
- Max 3 font sizes per screen
- Line height = font size × 1.4 (approx)
- Left-aligned for LTR languages

### 4. **Color Usage**
- Primary color for CTAs and focus
- Surface variant for cards
- Error color only for errors/warnings
- 4.5:1 contrast ratio minimum (WCAG AA)

### 5. **Accessibility**
- Touch targets: 48dp minimum
- Clear visual focus indicators
- Support for screen readers
- High contrast mode support
- Font scaling (Small/Medium/Large)

### 6. **Animation**
- Duration: 200-500ms (never > 1s)
- Easing: EaseOutExpo for entrance, EaseInExpo for exit
- Purposeful (communicates state change)
- Can be disabled (respect system settings)

### 7. **Responsive Design**
- Support phones: 360-420dp width
- Support tablets: 600-900dp width
- Landscape orientation
- Split-screen / multi-window

---

## Next Steps for Design Review

### Week 1: Foundation
- [ ] Set up Figma file structure
- [ ] Import Material 3 design kit
- [ ] Create color variables
- [ ] Define typography system
- [ ] Build core component library

### Week 2: Screens (Light Theme)
- [ ] Design all 11 main screens
- [ ] Create all 4 dialogs
- [ ] Include all states (empty, loading, error)
- [ ] Ensure consistency across screens

### Week 3: Screens (Dark Theme)
- [ ] Duplicate all screens for dark mode
- [ ] Adjust colors for dark theme
- [ ] Verify contrast ratios
- [ ] Test readability

### Week 4: User Flows & Prototypes
- [ ] Create user flow diagrams
- [ ] Build interactive prototypes
- [ ] Add all screen transitions
- [ ] Test prototype flows

### Week 5: Improvements & Testing
- [ ] Design proposed improvements
- [ ] Create before/after comparisons
- [ ] User testing sessions
- [ ] Iterate based on feedback

### Week 6: Handoff
- [ ] Final review with team
- [ ] Export design tokens
- [ ] Prepare developer handoff
- [ ] Document all components

---

**This documentation provides everything needed to recreate, review, and improve the OnDevice AI app design in Figma. All 11 screens, 4 dialogs, user flows, and component specifications are detailed above.**
