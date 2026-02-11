# Phase 1: Merge Ask Image into AI Chat

## Goal
Remove Ask Image tile, keep functionality in AI Chat

## Why This First?
- Simplest merge (both use text + images)
- Feature flags already working
- Lower risk than full unification
- Can test and validate before audio merge

## Changes Required

### 1. HomeScreen.kt
Remove only `BuiltInTaskId.LLM_ASK_IMAGE` from task list

**BEFORE:**
```kotlin
private val PREDEFINED_LLM_TASK_ORDER =
  listOf(
    BuiltInTaskId.LLM_ASK_IMAGE,      // Remove this
    BuiltInTaskId.LLM_ASK_AUDIO,      // Keep for now
    BuiltInTaskId.LLM_PROMPT_LAB,
    BuiltInTaskId.LLM_CHAT,
  )
```

**AFTER:**
```kotlin
private val PREDEFINED_LLM_TASK_ORDER =
  listOf(
    BuiltInTaskId.LLM_ASK_AUDIO,      // Keep
    BuiltInTaskId.LLM_PROMPT_LAB,     // Keep
    BuiltInTaskId.LLM_CHAT,           // Enhanced with images
    // Note: Ask Image functionality merged into AI Chat
  )
```

### 2. What Stays the Same
- ✅ ChatPanel.kt - Already model-based (no changes needed)
- ✅ LlmChatTaskModule.kt - Already has multi-modal description
- ✅ Audio Scribe - Still separate task for now
- ✅ Prompt Lab - Unchanged

## Expected Result

### Home Screen
```
❌ Ask Image (removed)
✅ Audio Scribe (still visible)
✅ Prompt Lab (unchanged)
✅ AI Chat (now handles images!)
```

### User Experience
- User opens AI Chat
- Selects Gemini 2.0 (image-capable)
- Sees Camera + Image in + button
- Can chat with images!

## Safety Measures
1. Only remove 1 task
2. Keep Audio Scribe as fallback
3. Test immediately after change
4. Easy to revert if crashes

## Success Criteria
- [x] Build succeeds
- [ ] App launches
- [ ] AI Chat shows image options
- [ ] Images work in AI Chat
- [ ] No crashes

