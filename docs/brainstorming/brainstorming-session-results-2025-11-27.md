# Brainstorming Session Results

**Date:** 2025-11-27
**Facilitator:** Claude (AI Assistant)
**Participant:** Gora
**Project:** OnDevice AI Android App

---

## Session Overview

| Aspect | Details |
|--------|---------|
| **Topic** | 10 UI/UX Updates Validation |
| **Goal** | Validate target states & explore edge cases before implementation |
| **Approach** | Structured validation with Six Thinking Hats + Question Storming |
| **Duration** | Single session |
| **Outcome** | All 10 updates validated with comprehensive specifications |

---

## Techniques Used

1. **Six Thinking Hats** - Analyzed each update through multiple lenses (facts, risks, benefits)
2. **Question Storming** - Generated edge case questions for each update
3. **Codebase Analysis** - Researched current implementation to understand exact state

---

## Ideas Generated

### Update #1: Welcome Screen Logo
- Replace sparkle emoji with OnDevice logo
- Keep time-dynamic greeting
- Logo asset already exists

### Update #2: Model Selector Alignment
- Remove "AI Chat" title only
- Center model selector in header
- Truncate long model names with "..."

### Update #3: Voice Mic Button & Flow
- Dedicated mic button on right of input bar
- Grey/black color scheme for recording UI
- On-device transcription with retry on failure
- Show transcribed text in input for editing before send

### Update #4: Post-Prompt Visual
- Small mic icon prefix for voice-originated messages
- Otherwise identical to typed messages

### Update #5: Response Action Buttons
- Icon-only buttons: Copy, Regenerate, Share, Play
- Share: User chooses last response or entire chat (Markdown)
- Play: On-device TTS with pause/resume (after response completes)

### Update #6: Side Panel Cleanup
- Shorten header to "OnDevice"
- Remove "Private & Local" and chat count
- Rename history to "Chats"
- Add "Recents" section (10 items)

### Update #7: Chats Screen Redesign
- Flat list (no cards)
- Long-press OR 3-dot menu for context options
- Star (pin + filter), Rename (dialog), Delete (confirm)
- Multi-select for bulk operations

### Update #8: Chat History Navigation
- One-step: Tap chat goes directly to active chat
- Full conversation visible with input ready
- Handle unavailable models with warning
- Truncate + warn for context limits

### Update #9: Response Disclaimer
- "OnDevice can make mistakes. Please double check responses."
- Right-aligned, subtle styling
- Show on first response + periodic intervals
- Non-dismissible

### Update #10: Model Allow Lists
- Add 6 new models from upstream commit 3900fbc
- Gemma-3n variants, Qwen, Phi-4, DeepSeek

---

## Key Insights

1. **Existing Implementation Discovery**
   - Mic button already exists (in dropdown) - just needs repositioning
   - Continue chat functionality exists via FAB - may have bug
   - Voice recording panel exists - needs color scheme update

2. **UX Best Practices Applied**
   - ChatGPT voice input research: Cache audio on failure, allow retry
   - Accessibility: Added 3-dot menu as long-press alternative
   - Mobile rename: Dialog preferred over inline editing

3. **Technical Considerations**
   - On-device transcription avoids network errors during STT
   - Context truncation needed for long conversation continuations
   - Model availability checks when resuming old conversations

---

## Prioritized Next Steps

### Immediate (Low Complexity)
1. #1 Welcome Logo
2. #9 Response Disclaimer
3. #10 Model Allowlist

### Short-term (Medium Complexity)
4. #2 Header Alignment
5. #6 Side Panel Cleanup
6. #5 Action Buttons
7. #4 Post-Prompt Visual

### Longer-term (High Complexity)
8. #8 History Navigation (may involve bug fixes)
9. #7 Chats Screen Redesign
10. #3 Voice Mic Button (most complex)

---

## Deliverables Created

1. **Living UI Specification Document**
   - Location: `docs/ui-updates-specification.md`
   - Contains: Current state, target state, edge cases, implementation notes
   - Purpose: Reference during implementation, update after each change

2. **This Brainstorming Session Results**
   - Location: `docs/brainstorming-session-results-2025-11-27.md`

---

## Session Reflection

### What Worked Well
- Systematic update-by-update validation
- Codebase analysis revealed existing implementations
- Edge case questioning uncovered important decisions
- Web research informed voice UX best practices

### Areas for Future Exploration
- TTS engine selection and integration
- Multi-model conversation handling
- Advanced search/filter in Chats screen

---

## Follow-up Actions

- [ ] Implement Epic 5 (as mentioned by user)
- [ ] Update living document after each implementation
- [ ] Track issues discovered during implementation

