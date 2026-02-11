# OnDevice AI - Product Requirements Document

**Author:** Gora
**Date:** 2025-11-26
**Version:** 1.0

---

## Executive Summary

OnDevice AI is an Android application that brings powerful AI capabilities to users who need them most - those in remote areas and developing regions with limited or unreliable internet connectivity. Unlike cloud-based AI assistants that require constant internet access, OnDevice AI runs entirely on the user's device, providing intelligent conversation, image analysis, and audio transcription without any network dependency.

**Vision:** Democratize AI access by removing the internet barrier. Provide a private, always-available AI assistant that works wherever the user is.

**Core Promise:** Your AI. Your Device. Works Anywhere.

### What Makes This Special

**The Differentiator:** OnDevice AI is the AI that's truly YOURS:

1. **Works Offline** - Full AI functionality without internet. Critical for users in areas with limited connectivity.
2. **100% Private** - All data stays on the device. No cloud uploads, no accounts, no tracking.
3. **Unlimited Usage** - No API costs, no usage limits, no subscriptions beyond the app purchase.
4. **User Ownership** - Your conversations, your data, exportable anytime.

This isn't "Claude but offline" - it's AI designed from the ground up for independence and privacy.

---

## Project Classification

**Technical Type:** Mobile App (Android)
**Domain:** General Consumer AI
**Complexity:** Medium
**Current Status:** Live on Google Play (base version), feature enhancement in progress

### Technical Context

- **Framework:** Android with Jetpack Compose
- **AI Models:** Gemma 3 family (1B, E2B, E4B variants) - multimodal capable
- **Database:** Room (local SQLite)
- **Architecture:** MVVM with Hilt dependency injection
- **Existing Features:** Basic chat, conversation history (recently fixed), model download/management

### Market Context

- **Target Market:** Users in remote areas, developing regions, privacy-conscious consumers
- **Competition:** Cloud-based AI (ChatGPT, Claude, Gemini) - all require internet
- **Positioning:** The only AI that works where cloud AI can't

---

## Success Criteria

### Primary Success Metrics

1. **Offline Reliability:** 100% of core features work without internet connection
2. **User Retention:** Users return to the app weekly (indicates real utility)
3. **App Store Rating:** Maintain 4.0+ rating on Google Play
4. **Core Flow Completion:** Users can have a complete conversation within 60 seconds of opening app

### User Success Definition

A user succeeds when they:
- Download a model once (with internet)
- Use AI chat anytime afterward (without internet)
- Feel their data is private and under their control
- Can find and continue previous conversations easily

### What Success Looks Like

> "I can chat with AI even when I have no signal. My conversations are saved locally and I can export them. It just works."

---

## Product Scope

### MVP - Minimum Viable Product (This Release)

**Priority 1: Unified Chat Experience**
- Single chat interface (eliminate task tiles)
- Auto-detection of input type (text, image, audio)
- Model selector at top center
- Seamless conversation flow

**Priority 2: Privacy-First Visual Identity**
- Brand area with "Private & Local" indicator
- Storage usage display (X chats, Y MB)
- "Running privately on your device" messaging during inference
- Reframe all messaging to emphasize local/private

**Priority 3: Enhanced Message Experience**
- Copy button on code blocks
- Copy All button on AI messages
- Markdown rendering in responses
- Syntax highlighting for code
- Regenerate response option

**Additional MVP Items:**
- Conversation date grouping (Today, Yesterday, Last 7 days)
- Search conversations functionality
- Theme toggle (Light/Dark/System)

### Growth Features (Post-MVP)

- Tutorial/tip cards during model loading
- Long-press context menu (Copy, Share, Delete, Pin)
- Storage budget enforcement with warnings
- Battery warning for heavy operations
- Export conversations (JSON/Markdown/ZIP)
- Model Manager as dedicated screen
- Auto-cleanup option (delete chats after X days)
- Hold-to-speak voice input
- Download queue with permission on reconnect

### Vision (Future)

- Persistent artifact gallery (images, code, transcripts)
- Local RAG - import your documents for context
- Fix Stable Diffusion image generation
- Performance mode slider (Quality vs Speed)
- First-launch onboarding with privacy messaging
- Pinned/favorite conversations
- AI-generated conversation titles

---

## User Experience Principles

### Core UX Philosophy

**The Tagline Test:** Every screen must pass this test:
> "Does this remind the user they're having a private, offline conversation?"

### Visual Personality

- **Clean & Minimal:** Reduce visual clutter, focus on content
- **Trust Signals:** Subtle privacy indicators throughout (🔒 icons)
- **Local Feel:** Language emphasizes "your device," "locally," "private"

### Key Interactions

**1. App Launch → Chat**
- Open app → immediately see chat interface
- No splash screens, no onboarding walls
- Model loads in background with progress indicator

**2. Conversation History**
- Hamburger menu → drawer slides in
- Brand area at top with privacy badge
- Searchable, date-grouped conversation list
- Swipe to delete conversations

**3. Model Switching**
- Tap model name → dropdown selector
- Shows downloaded vs available models
- Memory warning for large models

**4. Input Types**
- Text: Type and send
- Image: Attach via 📎 button → auto-uses multimodal
- Voice: Hold mic → transcribes to text input

### Proposed UI Layout

```
┌─────────────────────────────────────────┐
│ ☰        [Gemma 3 1B ▼]          ⚙️    │
├─────────────────────────────────────────┤
│                                         │
│         Chat messages area              │
│         (with markdown rendering)       │
│                                         │
├─────────────────────────────────────────┤
│ [📎] [🎤] Type message...         [⬆️]  │
└─────────────────────────────────────────┘
```

### Drawer Design

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

---

## Functional Requirements

### User Account & Identity

- FR1: Users can use the app without creating an account or signing in
- FR2: Users can set a display name for personalization (optional)
- FR3: All user preferences are stored locally on device

### Model Management

- FR4: Users can view list of available AI models with size and capability info
- FR5: Users can download AI models when connected to internet
- FR6: Users can delete downloaded models to free storage
- FR7: Users can select which downloaded model to use for conversations
- FR8: System shows memory warning when downloading models that may exceed device capacity
- FR9: Users can queue model downloads for when connectivity is restored

### Conversations

- FR10: Users can start new conversations with the AI
- FR11: Users can send text messages to the AI and receive responses
- FR12: Users can attach images to messages for AI analysis (multimodal)
- FR13: Users can use voice input to dictate messages (speech-to-text)
- FR14: System auto-detects input type and uses appropriate model capability
- FR15: Conversations are automatically saved locally after each message
- FR16: Users can view list of all saved conversations
- FR17: Users can search conversations by content or title
- FR18: Users can continue previously saved conversations
- FR19: Users can delete individual conversations
- FR20: Users can clear all conversation history
- FR21: Conversations are grouped by date (Today, Yesterday, Last 7 Days, Older)

### Message Interactions

- FR22: Users can copy individual AI responses to clipboard
- FR23: Users can copy code blocks from AI responses with one tap
- FR24: Users can regenerate AI responses
- FR25: Users can view AI responses with proper markdown formatting
- FR26: Code blocks in AI responses display with syntax highlighting
- FR27: Users can long-press messages for context menu (Copy, Share, Delete)

### Data Management

- FR28: Users can export all conversations in standard formats (JSON, Markdown, ZIP)
- FR29: Users can view storage usage breakdown (models, conversations, artifacts)
- FR30: System enforces configurable storage budget with warnings
- FR31: Users can configure auto-cleanup for old conversations
- FR32: All user data is stored exclusively on the local device

### Settings & Preferences

- FR33: Users can switch between Light, Dark, and System themes
- FR34: Users can adjust text size (Small, Medium, Large)
- FR35: Users can access dedicated Model Manager screen
- FR36: Users can enable/disable auto-save for conversations
- FR37: Users can view app version and provide feedback
- FR38: Users can rate the app on Google Play

### Offline Functionality

- FR39: All conversation features work without internet connection
- FR40: Model inference runs entirely on device
- FR41: Conversation history is accessible offline
- FR42: System indicates "Running locally" status during offline operation

### Privacy & Transparency

- FR43: System displays privacy indicators showing local-only operation
- FR44: System shows "Running privately on your device" during AI inference
- FR45: No user data is transmitted to external servers
- FR46: Users can verify no network calls are made during AI operations

---

## Non-Functional Requirements

### Performance

- NFR1: AI response streaming should begin within 3 seconds of sending message
- NFR2: App should launch to usable state within 2 seconds
- NFR3: Conversation list should load within 500ms
- NFR4: Search results should appear within 1 second of typing
- NFR5: Model switching should complete within 30 seconds

### Storage

- NFR6: Default storage budget of 4GB (user adjustable)
- NFR7: Warning displayed at 80% storage usage
- NFR8: Blocking notification at 95% storage usage
- NFR9: Conversation data should use efficient compression

### Battery

- NFR10: Display battery usage warning before heavy operations (when below 30%)
- NFR11: Provide estimated battery impact for large model downloads
- NFR12: Allow users to dismiss battery warnings permanently

### Reliability

- NFR13: App should never crash during normal operation
- NFR14: Conversation data should never be lost unexpectedly
- NFR15: Model downloads should be resumable after interruption
- NFR16: App should handle low memory gracefully (release resources, not crash)

### Accessibility

- NFR17: Support Android TalkBack for screen reader users
- NFR18: Maintain minimum touch target size of 48dp
- NFR19: Ensure sufficient color contrast ratios
- NFR20: Support dynamic text sizing

---

## Technical Constraints

### Platform Requirements

- **Minimum Android Version:** Android 8.0 (API 26) or higher
- **Target Android Version:** Latest stable
- **Architecture Support:** ARM64 primary, ARM32 secondary

### Model Constraints

| Model | Size | RAM Required | Notes |
|-------|------|--------------|-------|
| Gemma 3 1B-IT | 584 MB | ~2 GB | Lightweight, fast |
| Gemma 3n E2B-IT | 3.4 GB | ~4 GB | Balanced |
| Gemma 3n E4B-IT | 4.7 GB | ~6 GB | Memory warning shown |

### Known Limitations

- Stable Diffusion image generation is currently broken (future fix)
- Only one model can be loaded in memory at a time
- Model switching requires reload time
- Very long conversations may impact response time

---

## Out of Scope

The following are explicitly NOT part of this release:

- iOS version
- Web version
- Cloud sync of conversations
- User accounts or authentication
- Multi-device sync
- Real-time collaboration
- API access for third-party apps
- Stable Diffusion image generation (broken, future fix)

---

## Dependencies

### External Dependencies

- Google Play Store (distribution)
- Gemma model files (one-time download)

### Internal Dependencies

- Existing Room database schema (conversations, messages)
- Current chat infrastructure (ChatViewModel, ChatView)
- Model download/management system
- Navigation framework (Jetpack Navigation)

---

## Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Large model size deters downloads | High | Medium | Clear size indicators, start with smaller model |
| Users expect cloud AI speed | Medium | High | Set expectations with "Running locally" messaging |
| Storage fills up on low-end devices | High | Medium | Enforced storage budget with warnings |
| Model loading time frustrates users | Medium | High | Tutorial cards during loading |

---

## Appendix

### Reference Documents

- Brainstorming Session Results: `docs/brainstorming-session-results-2025-11-26.md`
- Forensic Analysis: `docs/CONVERSATION_HISTORY_FORENSIC_ANALYSIS.md`
- Chat System Architecture: `docs/CHAT_SYSTEM_ARCHITECTURE.md`
- Remediation Plan: `docs/CHAT_HISTORY_REMEDIATION_PLAN.md`

### Glossary

- **On-Device AI:** AI models that run entirely on the user's device without cloud connectivity
- **Multimodal:** AI capability to process multiple input types (text, images, audio)
- **Artifact:** Generated content (code, images, documents) from AI conversations
- **RAG:** Retrieval Augmented Generation - using local documents to provide context to AI

---

_This PRD captures the essence of OnDevice AI - bringing AI to users who need it most, in places where cloud AI can't reach._

_Created through collaborative discovery between Gora and AI facilitator._
