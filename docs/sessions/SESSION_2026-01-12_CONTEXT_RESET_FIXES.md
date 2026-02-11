# Session Summary: Context Reset Bug Fixes
**Date:** 2026-01-12
**Duration:** ~3 hours
**Focus:** Conversation token limit reset crashes and UX issues

---

## 🐛 Issues Solved

### Issue #1: App Crash on Token Limit Reset ✅
**Problem:**
App crashed when showing "start fresh, conversation getting too long" message after conversation exceeded ~3200 tokens.

**Root Cause:**
Race condition between background token check and main thread inference:
- Token check ran in `viewModelScope.launch(Dispatchers.IO)` (async, background)
- Inference continued immediately on main thread
- When token check triggered reset, it destroyed the MediaPipe Conversation object
- Inference tried to use the destroyed object → **SIGSEGV crash: "Pure virtual function called!"**

**Fix (Commit b278e9b):**
```kotlin
// BEFORE (buggy - async):
viewModelScope.launch(Dispatchers.IO) {
  if (estimatedTokens > RESET_THRESHOLD) {
    LlmChatModelHelper.resetConversation(...)  // Destroys Conversation
  }
}
// Inference continues immediately, uses destroyed object → CRASH

// AFTER (fixed - synchronous):
val conversationMessages = withContext(Dispatchers.IO) {  // Wait for this
  conversationDao.getMessagesForThread(threadId)
}
if (estimatedTokens > RESET_THRESHOLD) {
  LlmChatModelHelper.resetConversation(...)  // Destroys Conversation
  return@launch  // Skip inference - don't use destroyed object
}
// Inference only runs if no reset needed → SAFE
```

**Evidence:**
- CI passed (build 20934543318)
- Crash logs confirmed native crash in `liblitertlm_jni.so`
- Stack trace: `runInference` → `sendMessageAsync` → segfault

---

### Issue #2: Stuck UI - Rotating Logo After Reset ✅
**Problem:**
After conversation reset, rotating logo kept spinning forever. Send button didn't appear when typing. App appeared frozen.

**Root Cause:**
Early return after reset never cleared the "generating" state:
- Line 77: `setInProgress(true)` set at start of `generateResponse()`
- Line 237: `return@launch` after reset (early exit)
- Lines 307, 342, 348: `setInProgress(false)` only called in normal completion paths
- **Bug**: Early return skipped clearing state → UI stuck

**Fix (Commit 8f6f1b5):**
```kotlin
// Show user a message explaining the reset
val systemChatMessage = ChatMessageText(...)
addMessage(model = model, message = systemChatMessage)

// CRITICAL: Clear the generating state and remove loading indicator
// before returning early, otherwise UI stays stuck in "generating" mode
if (getLastMessage(model = model) is ChatMessageLoading) {
  removeLastMessage(model = model)
}
setInProgress(false)
setPreparing(false)

// Return early - DO NOT proceed with inference
return@launch
```

**Evidence:**
- CI passed (build 20939821926)
- Visual confirmation: rotating logo stopped after fix
- User could type new messages

---

### Issue #3: Infinite Reset Loop - 3 Rotating Icons ✅
**Problem:**
After reset, user sent new message → another reset immediately → sent again → another reset → 3 rotating icons, no responses.

**Root Cause:**
`resetConversation()` only reset in-memory Conversation object, NOT database messages:
- Database still contained all old messages
- Token count calculated from DB: `conversationDao.getMessagesForThread(threadId)`
- After reset: old messages still in DB → token count still ~3300+ → immediate reset again → infinite loop

**Logs showing the bug:**
```
20:34:37 Conversation: 9 messages, ~3310 tokens → RESET
20:34:39 Conversation reset successfully
20:34:55 User sends new message
20:34:55 Conversation: 11 messages, ~3333 tokens → RESET AGAIN!
20:34:56 Conversation reset successfully
20:35:11 User sends new message
20:35:11 Conversation: 13 messages, ~3356 tokens → RESET AGAIN!
```

**Fix (Commit 1b018fd):**
```kotlin
try {
  LlmChatModelHelper.resetConversation(
    model = model,
    supportImage = supportImage,
    supportAudio = supportAudio
  )
  Log.i(TAG, "[TOKEN-LIMIT] Conversation reset successfully")

  // CRITICAL: Delete old messages from database to clear token count
  // Without this, token count stays high and triggers infinite reset loop
  withContext(Dispatchers.IO) {
    conversationDao.deleteMessagesForThread(threadId)
    Log.i(TAG, "[TOKEN-LIMIT] Deleted old messages from database")
  }
} catch (e: Exception) {
  Log.e(TAG, "[TOKEN-LIMIT] Failed to reset conversation: ${e.message}", e)
}
```

**Evidence:**
- CI passed debug build (20941427364)
- Release build failed (expected - missing signing keys)
- User confirmed: "ITS WORKING NOW"

---

## 📊 Technical Details

### Files Modified
1. `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt`
   - Lines 186-250: Token limit checking and reset logic
   - Added synchronous token check
   - Added UI state cleanup before early return
   - Added database message deletion after reset

### Commits
- `b278e9b`: fix(crash): Prevent crash when resetting conversation at token limit
- `8f6f1b5`: fix(crash): Clear UI state when conversation reset to prevent stuck loading
- `1b018fd`: fix(context): Delete DB messages after reset to prevent infinite reset loop

### CI Builds
- 20934543318: ✅ First fix passed
- 20939821926: ✅ Second fix passed
- 20941427364: ✅ Third fix passed (debug only)

---

## ⚠️ Known Issues (Not Fixed)

### Issue A: Rotating Icon Shown on First Reset
**Status:** ⚠️ Needs investigation
**Description:**
User reports: "WE STILL HAVE THE ROTATING ICON ON THE FIRST COMPRESSION"

The reset message is shown, but the rotating icon (ChatMessageLoading) appears briefly during the first reset. This might be because:
1. Loading indicator added at line 93 before token check runs
2. We remove it at line 239-241, but there's a timing issue
3. May need to check if token limit is near BEFORE adding loading indicator

**Proposed fix location:**
Lines 90-95 in `LlmChatViewModel.kt` - check token count before adding loading message

---

### Issue B: Conversation Scrolling Not Smooth
**Status:** ⚠️ Needs investigation
**Description:**
User reports: "THE SCROLLING IS NOT SMOOTH WITHIN THE CONVERSATION"

Potential causes:
1. LazyColumn performance with large message lists
2. Heavy recomposition on scroll
3. Image/content rendering in messages
4. Missing `key` parameter in LazyColumn items

**Investigation needed:**
`app/src/main/java/ai/ondevice/app/ui/common/chat/ChatPanel.kt` - LazyColumn implementation around line 600-700

---

## 🎯 What's Left for Next Session

### Priority 1: Fix Rotating Icon on First Reset
- [ ] Investigate why loading indicator appears on first reset
- [ ] Move token check before loading indicator OR
- [ ] Remove loading indicator immediately when adding reset message

### Priority 2: Fix Scrolling Performance
- [ ] Profile LazyColumn performance in ChatPanel.kt
- [ ] Add proper `key` parameters to items
- [ ] Consider virtualization optimizations
- [ ] Test with long conversations (50+ messages)

### Priority 3: Validation
- [ ] Run full UI test suite with Maestro
- [ ] Test with multiple resets in same session
- [ ] Verify no memory leaks after multiple resets
- [ ] Test with different models (Gemma, Qwen, Phi)

---

## 📝 Session Notes

### White/Black Screen Issue (Resolved)
**Problem:** App showed white/black screen after reinstall
**Cause:** Fresh install deleted all models. App requires model download before showing UI.
**Resolution:** Normal behavior - not a bug. User needs to download model first.

### Device Connection
- Device: Samsung S22 Ultra (R3CT10HETMM)
- Connection: USB-C, sometimes disconnected during session
- APK installed manually when connection stable

### Build Pipeline
- Cannot build locally on DGX Spark
- All builds via GitHub Actions
- Debug builds work fine
- Release builds fail (missing signing keys - expected)

---

## 🔍 Code Patterns Learned

### Pattern 1: Synchronous Critical Checks
When checking conditions that affect object lifecycle, use synchronous execution:
```kotlin
// ❌ BAD - async check, object destroyed while in use
viewModelScope.launch(Dispatchers.IO) {
  if (shouldDestroy) destroyObject()
}
useObject()  // Race condition!

// ✅ GOOD - synchronous check
val shouldDestroy = withContext(Dispatchers.IO) { checkCondition() }
if (shouldDestroy) {
  destroyObject()
  return
}
useObject()  // Safe
```

### Pattern 2: UI State Cleanup on Early Exit
Always clear UI state before early return:
```kotlin
// ❌ BAD - state left dirty
setInProgress(true)
if (earlyExitCondition) {
  return@launch  // UI stuck!
}
doWork()
setInProgress(false)

// ✅ GOOD - clean state
setInProgress(true)
if (earlyExitCondition) {
  setInProgress(false)
  setPreparing(false)
  return@launch
}
doWork()
setInProgress(false)
```

### Pattern 3: Database and Memory Sync
Keep in-memory and database state synchronized:
```kotlin
// ❌ BAD - memory cleared, DB not cleared
resetInMemoryObject()

// ✅ GOOD - both cleared
resetInMemoryObject()
withContext(Dispatchers.IO) {
  clearDatabase()
}
```

---

## 📦 Deliverables

### Commits
1. `b278e9b` - Crash fix (synchronous token check)
2. `8f6f1b5` - Stuck UI fix (state cleanup)
3. `1b018fd` - Infinite loop fix (DB cleanup)

### APKs
- `/tmp/fix3/app-debug.apk` (227MB) - Ready for testing

### Documentation
- This session summary
- Inline code comments explaining each fix
- Commit messages with detailed root cause analysis

---

## 🚀 Next Session Checklist

- [ ] Reconnect device (adb devices)
- [ ] Install latest APK: `adb install -r /tmp/fix3/app-debug.apk`
- [ ] Fix rotating icon on first reset
- [ ] Fix conversation scrolling smoothness
- [ ] Run `/verify` to ensure lint + tests pass
- [ ] Run `/ship` to commit and push
- [ ] Run `/ci-status` to verify build
- [ ] Test with long conversations (50+ messages)
- [ ] Update LESSONS_LEARNED.md with new patterns
- [ ] Consider adding unit tests for token limit logic

---

**Status:** ✅ Three critical bugs fixed, two UX improvements needed
**Next Focus:** Polish and performance optimization
**Ready for:** Continuation in new session with fresh context
