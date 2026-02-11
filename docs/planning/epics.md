# OnDevice AI - Epic Breakdown

**Author:** Gora
**Date:** 2025-11-26
**Project Type:** Brownfield (enhancing existing app)
**Scope:** MVP + Growth features

---

## Overview

This document breaks down NEW features for OnDevice AI into implementable stories. Existing functionality (chat, model management, conversation storage, markdown rendering) is already in place.

**Epics Summary:**

| Epic | Name | Stories | Priority |
|------|------|---------|----------|
| 1 | Unified Chat Experience | 2 | MVP |
| 2 | Privacy Visual Identity | 4 | MVP |
| 3 | Message Interactions | 4 | MVP |
| 4 | Conversation History Enhancements | 3 | MVP |
| 5 | Settings & Data Management | 4 | Growth |
| 7 | AI Image Generation | 7 | MVP |

**Total: 24 stories**

*Note: Epic 6 was implemented previously (10 UI/UX updates). Epic 7 adds on-device image generation.*

---

## FR Coverage Map

| FR | Description | Epic | Story |
|----|-------------|------|-------|
| FR14 | Auto-detect input type | 1 | 1.1 |
| FR17 | Search conversations | 4 | 4.1 |
| FR18 | Continue saved conversations | 4 | 4.2 |
| FR21 | Date grouping | 4 | 4.3 |
| FR22 | Copy AI responses | 3 | 3.1 |
| FR23 | Copy code blocks | 3 | 3.2 |
| FR24 | Regenerate responses | 3 | 3.3 |
| FR27 | Long-press context menu | 3 | 3.4 |
| FR28 | Export conversations | 5 | 5.1 |
| FR29 | Storage usage display | 2 | 2.3 |
| FR30 | Storage budget warnings | 5 | 5.2 |
| FR31 | Auto-cleanup config | 5 | 5.3 |
| FR34 | Text size adjustment | 5 | 5.4 |
| FR42 | "Running locally" status | 2 | 2.1 |
| FR43 | Privacy indicators | 2 | 2.2 |
| FR44 | "Running privately" during inference | 2 | 2.1 |
| FR47 | Generate images from text prompts on-device | 7 | 7.2, 7.4 |
| FR48 | Configure generation iterations (5-50 steps) | 7 | 7.4 |
| FR49 | View generation progress with intermediate results | 7 | 7.3 |
| FR50 | Cancel image generation in progress | 7 | 7.3 |
| FR51 | Save generated images to device gallery | 7 | 7.5 |
| FR52 | Share generated images via system share | 7 | 7.6 |
| FR53 | Regenerate with same prompt (new seed) | 7 | 7.4 |
| FR54 | Show memory/device requirements before generation | 7 | 7.1 |
| FR55 | Image generation works completely offline | 7 | 7.0-7.6 |

---

## Epic 1: Unified Chat Experience

**Goal:** Eliminate task tiles and provide a single, unified chat interface where users can send text, attach images, or use voice - all from one screen.

**User Value:** One-tap to chat, no confusing navigation between different "tasks."

**FRs Covered:** FR14

---

### Story 1.1: Unified Input Bar with Auto-Detection

As a **user**,
I want **a single input bar that accepts text, images, and voice**,
So that **I don't need to switch between different task screens**.

**Acceptance Criteria:**

**Given** I am on the chat screen
**When** I type text and tap send
**Then** the message is sent as a text query

**And** when I tap the attachment button and select an image
**Then** the image is attached and I can add a question about it

**And** when I tap and hold the voice button
**Then** audio recording starts and transcribes to text

**Prerequisites:** None (first story)

**Technical Notes:**
- Input bar already exists in `MessageInputText.kt`
- Attachment button (📎) exists - ensure it's visible
- Voice button (🎤) exists via `AudioRecorderPanel.kt`
- Model capability detection via `model.llmSupportImage` and `model.llmSupportAudio`

---

### Story 1.2: Remove Task Tiles from Home Screen

As a **user**,
I want **to go directly to chat when I open the app**,
So that **I can start chatting immediately without extra taps**.

**Acceptance Criteria:**

**Given** I open the app
**When** the app launches
**Then** I see the chat screen directly (not task tiles)

**And** the model selector is visible in the top bar
**Then** I can switch models without leaving chat

**Prerequisites:** Story 1.1

**Technical Notes:**
- Modify `GalleryNavGraph.kt` to set chat as start destination
- Keep task tiles accessible via Settings for power users (optional)
- Ensure `LlmChatScreen` loads by default with first available model

---

## Epic 2: Privacy Visual Identity

**Goal:** Make the local/private nature of the AI visible and celebrated throughout the UI.

**User Value:** Trust and confidence that data stays on device.

**FRs Covered:** FR29, FR42, FR43, FR44

---

### Story 2.1: Privacy Indicator During Inference

As a **user**,
I want **to see "Running privately on your device" while the AI is thinking**,
So that **I know my data isn't being sent to the cloud**.

**Acceptance Criteria:**

**Given** I send a message to the AI
**When** the AI is generating a response
**Then** I see a lock icon (🔒) with text "Running privately on your device"

**And** when the response completes
**Then** the privacy indicator disappears

**Prerequisites:** None

**Technical Notes:**
- Add to `ChatPanel.kt` based on `isGenerating` state
- Use `MaterialTheme.colorScheme.tertiary` (green) for the indicator
- Pattern defined in `architecture.md` Privacy Indicator Pattern

---

### Story 2.2: Privacy Badge in Drawer Header

As a **user**,
I want **to see a privacy badge in the navigation drawer**,
So that **I'm reminded my conversations are private**.

**Acceptance Criteria:**

**Given** I open the navigation drawer
**When** I look at the header area
**Then** I see "🔒 Private & Local" badge below the app name

**Prerequisites:** Story 2.1

**Technical Notes:**
- Add to `ChatMenuSheet.kt` drawer header
- Use tertiary green color
- Keep it subtle but visible

---

### Story 2.3: Storage Usage Display in Drawer

As a **user**,
I want **to see how much storage my conversations use**,
So that **I can manage my device storage**.

**Acceptance Criteria:**

**Given** I open the navigation drawer
**When** I look at the header area
**Then** I see storage info like "12 chats • 2.1 MB"

**And** the count updates when I add or delete conversations

**Prerequisites:** Story 2.2

**Technical Notes:**
- Query `ConversationDao` for thread count
- Calculate approximate size (message count × avg message size)
- Display in drawer header below privacy badge

---

### Story 2.4: Offline Status Indicator

As a **user**,
I want **to know the app works offline**,
So that **I'm confident I can use it anywhere**.

**Acceptance Criteria:**

**Given** I am using the app without internet
**When** I send a message
**Then** the AI responds normally (no error)

**And** there is no "offline mode" warning
**Then** local operation is the celebrated default

**Prerequisites:** Story 2.1

**Technical Notes:**
- This is mostly about NOT showing offline warnings
- Ensure no network checks block AI inference
- The privacy indicator already communicates local operation

---

## Epic 3: Message Interactions

**Goal:** Allow users to copy, regenerate, and interact with AI responses easily.

**User Value:** Get more value from AI-generated content.

**FRs Covered:** FR22, FR23, FR24, FR27

---

### Story 3.1: Copy Full AI Response

As a **user**,
I want **to copy an entire AI response to my clipboard**,
So that **I can paste it elsewhere**.

**Acceptance Criteria:**

**Given** I see an AI response in the chat
**When** I tap the copy button on the message
**Then** the entire response is copied to clipboard

**And** I see a brief "Copied" toast confirmation

**Prerequisites:** None

**Technical Notes:**
- Add `copyToClipboard()` utility to `Utils.kt` (per architecture.md)
- Add copy IconButton to `MessageBodyText.kt` for agent messages
- Use `Icons.Default.ContentCopy`

---

### Story 3.2: Copy Code Block with One Tap

As a **user**,
I want **to copy just a code block from an AI response**,
So that **I can paste the code into my project**.

**Acceptance Criteria:**

**Given** an AI response contains a code block
**When** I tap the copy button on the code block
**Then** only that code block is copied to clipboard

**And** I see "Code copied" toast confirmation

**Prerequisites:** Story 3.1

**Technical Notes:**
- Modify `MarkdownText.kt` or create custom code block renderer
- Add copy button overlay on code blocks
- Use same `copyToClipboard()` utility

---

### Story 3.3: Regenerate AI Response

As a **user**,
I want **to regenerate an AI response if I don't like it**,
So that **I can get a different answer**.

**Acceptance Criteria:**

**Given** I see an AI response in the chat
**When** I tap the regenerate button
**Then** the AI generates a new response to the same prompt

**And** the old response is replaced with the new one

**Prerequisites:** Story 3.1

**Technical Notes:**
- Add regenerate button to AI message actions
- Store the original user prompt with each AI response
- Call inference again with same prompt
- Use `Icons.Default.Refresh`

---

### Story 3.4: Long-Press Context Menu

As a **user**,
I want **to long-press a message for more options**,
So that **I can copy, share, or delete it**.

**Acceptance Criteria:**

**Given** I see a message in the chat
**When** I long-press on it
**Then** a context menu appears with options: Copy, Share, Delete

**And** when I tap Copy, the message is copied
**And** when I tap Share, the system share sheet opens
**And** when I tap Delete, the message is removed (with confirmation)

**Prerequisites:** Story 3.1

**Technical Notes:**
- Use `combinedClickable` modifier for long-press detection
- Show `DropdownMenu` with options
- Share via Android Intent.ACTION_SEND
- Delete requires confirmation dialog

---

## Epic 4: Conversation History Enhancements

**Goal:** Make it easy to find and continue old conversations.

**User Value:** Never lose important conversations.

**FRs Covered:** FR17, FR18, FR21

---

### Story 4.1: Search Conversations

As a **user**,
I want **to search my conversation history**,
So that **I can find a specific chat quickly**.

**Acceptance Criteria:**

**Given** I am in the conversation list
**When** I type in the search bar
**Then** I see conversations matching my query (by title or content)

**And** results update as I type

**Prerequisites:** None

**Technical Notes:**
- Add `searchThreads()` to `ConversationDao` (per architecture.md)
- Add search TextField to `ConversationListScreen.kt`
- Use Room LIKE query with wildcards

---

### Story 4.2: Continue Saved Conversation

As a **user**,
I want **to continue a previous conversation**,
So that **I can pick up where I left off**.

**Acceptance Criteria:**

**Given** I am viewing a saved conversation
**When** I tap the "Continue" button
**Then** the conversation loads into the chat screen

**And** I can send new messages that continue the thread

**Prerequisites:** Story 4.1

**Technical Notes:**
- Add `loadExistingThread()` to `ChatViewModel` (per architecture.md)
- Add Continue FAB or button to `ConversationDetailScreen`
- Pass threadId via navigation, load messages without re-saving

---

### Story 4.3: Date-Grouped Conversation List

As a **user**,
I want **my conversations grouped by date**,
So that **I can find recent chats easily**.

**Acceptance Criteria:**

**Given** I open the conversation list
**When** I look at the list
**Then** conversations are grouped under headers: Today, Yesterday, Last 7 Days, Older

**And** within each group, conversations are sorted by most recent first

**Prerequisites:** Story 4.1

**Technical Notes:**
- Add `formatDateGroup()` to Utils.kt (per architecture.md)
- Group conversations in `ConversationListViewModel`
- Use sticky headers or section dividers

---

## Epic 5: Settings & Data Management

**Goal:** Give users control over their data and preferences.

**User Value:** Customization and data ownership.

**FRs Covered:** FR28, FR30, FR31, FR34

**Note:** This is Growth/Post-MVP scope.

---

### Story 5.1: Export Conversations

As a **user**,
I want **to export my conversations**,
So that **I can back them up or use them elsewhere**.

**Acceptance Criteria:**

**Given** I am in Settings
**When** I tap "Export Conversations"
**Then** I can choose format: JSON or Markdown

**And** the export file is saved or shared via system share sheet

**Prerequisites:** None

**Technical Notes:**
- Add export function to `ConversationDao` or ViewModel
- Generate JSON or Markdown format
- Use Android Share Intent or save to Downloads

---

### Story 5.2: Storage Budget Warnings

As a **user**,
I want **to be warned when storage is almost full**,
So that **I can free up space before it's too late**.

**Acceptance Criteria:**

**Given** my conversation storage exceeds 80% of budget
**When** I open the app
**Then** I see a warning banner (amber color)

**And** when storage exceeds 95%
**Then** I see a critical warning (red) with "Manage Storage" link

**Prerequisites:** Story 2.3

**Technical Notes:**
- Default budget: 4GB (from NFR6)
- Store budget setting in DataStoreRepository
- Check on app launch and show appropriate warning

---

### Story 5.3: Auto-Cleanup Configuration

As a **user**,
I want **to auto-delete old conversations**,
So that **storage doesn't fill up**.

**Acceptance Criteria:**

**Given** I am in Settings
**When** I enable "Auto-cleanup"
**Then** I can set: Never, After 30 days, After 90 days, After 1 year

**And** old conversations are deleted automatically based on my setting

**Prerequisites:** Story 5.2

**Technical Notes:**
- Add setting to DataStoreRepository
- Run cleanup check on app launch
- Delete threads older than threshold

---

### Story 5.4: Text Size Adjustment

As a **user**,
I want **to adjust the text size**,
So that **I can read comfortably**.

**Acceptance Criteria:**

**Given** I am in Settings
**When** I select text size: Small, Medium, or Large
**Then** chat text size changes throughout the app

**Prerequisites:** None

**Technical Notes:**
- Add setting to DataStoreRepository
- Create CompositionLocal for text scale
- Apply to `MarkdownText` and message components

---

## Epic 7: AI Image Generation

**Goal:** Enable users to generate images from text prompts entirely on-device, delivering cloud-comparable quality with the same offline-first, privacy-preserving experience as text/audio features.

**User Value:** Creative content generation without internet, API costs, or privacy concerns. Democratizes AI image generation for users in emerging markets (Mumbai, Bulawayo) who can't afford $10-20/month cloud subscriptions.

**FRs Covered:** FR47-FR55

**Architecture:** New task tile following existing pattern (like Ask Image, Audio Scribe). Creates `ImageGenerationTaskModule.kt`, `ImageGenerationScreen.kt`, `ImageGenerationViewModel.kt`.

---

### Story 7.0: Foundation - Manifest & Model Path Setup

As a **developer**,
I want **the app configured for MediaPipe image generation**,
So that **the ImageGenerator can initialize without crashes**.

**Acceptance Criteria:**

**Given** a fresh app install
**When** the image generation model is downloaded
**Then** model files are unzipped to the correct path for MediaPipe

**And** AndroidManifest.xml contains OpenCL native library declarations:
```xml
<uses-native-library android:name="libOpenCL.so" android:required="false" />
<uses-native-library android:name="libOpenCL-car.so" android:required="false"/>
<uses-native-library android:name="libOpenCL-pixel.so" android:required="false" />
```

**Prerequisites:** None (first story)

**Technical Notes:**
- Model already defined: `MODEL_IMAGE_GENERATION_STABLE_DIFFUSION` in `Model.kt`
- URL: `storage.googleapis.com/tfweb/app_gallery_models/sd15.zip` (Google's verified model)
- Size: 1.9GB, unzips to `sd15/` directory
- Fallback model: `huggingface.co/na5h13/stable-diffusion-v1-5-mediapipe`
- Ensure model unzip path matches MediaPipe's expected directory structure

---

### Story 7.1: Task Registration & Device Capability Check

As a **user**,
I want **to see if my device supports image generation before downloading the model**,
So that **I don't waste bandwidth on an incompatible device**.

**Acceptance Criteria:**

**Given** I navigate to model management
**When** I view the Stable Diffusion model
**Then** I see device compatibility info: "Requires 8GB+ RAM, Android 12+"

**And** if my device has <6GB RAM
**Then** I see warning: "Your device may not support image generation"

**And** task ID `IMAGE_GENERATION` is registered in `Tasks.kt`
**And** Image Generation tile appears on home screen

**Prerequisites:** Story 7.0

**Technical Notes:**
- Add `BuiltInTaskId.IMAGE_GENERATION = "image_generation"` to `Tasks.kt`
- Check `ActivityManager.getMemoryInfo()` for available RAM
- Check `Build.VERSION.SDK_INT >= 31` for Android 12 (OpenCL support)
- Create `ImageGenerationTask` class following `LlmAskImageTask` pattern
- Register via Hilt `@IntoSet` in `ImageGenerationTaskModule.kt`

---

### Story 7.2: MediaPipe Inference Wrapper

As a **developer**,
I want **an async wrapper around MediaPipe ImageGenerator**,
So that **image generation doesn't block the UI thread**.

**Acceptance Criteria:**

**Given** a valid prompt and iteration count
**When** `generateImage(prompt, iterations, seed)` is called
**Then** generation runs on background thread (`Dispatchers.Default`)

**And** progress is emitted via `Flow<GenerationProgress>`
**And** intermediate images are available when requested
**And** final bitmap is returned on completion
**And** errors are caught and returned as `GenerationResult.Error`
**And** memory is properly released after generation

**Prerequisites:** Story 7.1

**Technical Notes:**
- Create `ImageGenerationHelper.kt` following MediaPipe codelab pattern
- Initialize: `ImageGenerator.createFromOptions(context, options)`
- Use `imageGenerator.setInputs(prompt, iterations, seed)` + loop with `execute(showResult)`
- Wrap in `withContext(Dispatchers.Default)` for CPU-bound inference
- Emit: `GenerationProgress(step: Int, total: Int, intermediateBitmap: Bitmap?)`
- Generation time: ~15-60 seconds depending on device and iterations
- Use `BitmapExtractor.extract(result.generatedImage())` to get Bitmap

---

### Story 7.3: Progress Display & Cancel

As a **user**,
I want **to see generation progress and cancel if needed**,
So that **I know it's working and can abort mistakes**.

**Acceptance Criteria:**

**Given** I start image generation
**When** generation is in progress
**Then** I see: "Generating... Step 5 of 20"

**And** I see intermediate preview images (updated every 5 steps)
**And** I see a Cancel button
**And** when I tap Cancel, generation stops within 2 seconds
**And** partial result is discarded
**And** "Running privately on your device" indicator is shown

**Prerequisites:** Story 7.2

**Technical Notes:**
- Use existing `ChatMessageImageWithHistory` for progress display
- `curIteration` and `totalIterations` already supported in message type
- `MessageBodyImageWithHistory.kt` handles swipe navigation between iterations
- Cancel via `Job.cancel()` on coroutine scope
- Reuse privacy indicator pattern from Epic 2 (Story 2.1)

---

### Story 7.4: Image Generation Screen UI

As a **user**,
I want **a screen to enter prompts and see generated images**,
So that **I can create images from text**.

**Acceptance Criteria:**

**Given** I tap the Image Generation tile on home screen
**When** I enter a prompt and tap Generate
**Then** generation starts with progress display

**And** prompt field supports multi-line text (max 500 chars)
**And** I can adjust iterations via slider (5-50, default 20)
**And** generated image displays at 512x512
**And** I can tap "Generate Again" to regenerate with new seed
**And** first-time users see: "Generate images from text, completely offline. Download the 1.9GB model to get started."

**Prerequisites:** Story 7.1, Story 7.3

**Technical Notes:**
- Create `ImageGenerationScreen.kt` as new task tile (like `LlmAskImageScreen`)
- Create `ImageGenerationViewModel.kt` extending pattern from `LlmChatViewModel`
- Follow existing task tile navigation via `GalleryNavGraph.kt`
- Use `IMAGE_GENERATION_CONFIGS` from `Model.kt` for iteration slider
- Model selector in header (same pattern as other tasks)
- Seed is auto-generated randomly; "Generate Again" uses new seed

---

### Story 7.5: Save Generated Image to Gallery

As a **user**,
I want **to save generated images to my device**,
So that **I can use them elsewhere**.

**Acceptance Criteria:**

**Given** a generated image is displayed
**When** I tap the Save button
**Then** image is saved to device gallery/Pictures folder

**And** I see confirmation toast: "Image saved to gallery"
**And** image is named: `ondevice_ai_[timestamp].png`
**And** image appears in device Photos/Gallery app

**Prerequisites:** Story 7.3

**Technical Notes:**
- Use `MediaStore.Images` API for Android 10+ (scoped storage)
- For Android 9 and below, use `WRITE_EXTERNAL_STORAGE` permission
- Save as PNG format for quality preservation
- Add Save icon button to image display area
- Use `Icons.Default.SaveAlt` or similar

---

### Story 7.6: Share Generated Image

As a **user**,
I want **to share generated images**,
So that **I can send them to others**.

**Acceptance Criteria:**

**Given** a generated image is displayed
**When** I tap the Share button
**Then** system share sheet opens with the image

**And** I can share via any installed app (WhatsApp, Telegram, email, etc.)
**And** shared image includes metadata: "Generated with OnDevice AI"

**Prerequisites:** Story 7.5

**Technical Notes:**
- Use `Intent.ACTION_SEND` with `image/png` MIME type
- Use `FileProvider` for secure URI sharing (required for Android 7+)
- Reuse sharing pattern from Story 3.4 (Long-press context menu)
- Add Share icon button next to Save button
- Use `Icons.Default.Share`

---

## Summary

**MVP Epics (1-4):** 13 stories
**Growth Epic (5):** 4 stories
**MVP Epic (7):** 7 stories
**Total:** 24 stories

**Implementation Order:**
1. Epic 1: Unified Chat Experience (foundation for new UX)
2. Epic 2: Privacy Visual Identity (key differentiator)
3. Epic 3: Message Interactions (user productivity)
4. Epic 4: Conversation History Enhancements (usability)
5. Epic 5: Settings & Data Management (post-MVP)
6. Epic 7: AI Image Generation (on-device creative tools)

**All 25 FRs are covered by stories (FR14-FR44 + FR47-FR55).**

---

_For implementation: Use stories as dev agent work items. Each story is sized for single-session completion._

_Context: PRD + UX Design + Architecture documents provide full implementation details._
