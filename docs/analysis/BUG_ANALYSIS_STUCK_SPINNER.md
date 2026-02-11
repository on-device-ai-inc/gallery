# Bug Analysis: Stuck Loading Spinner After Stop/Regenerate

**Date**: 2026-02-02  
**Severity**: High (impacts UX)  
**Status**: Fixed in commit f79caa4

---

## Timeline: How This Bug Was Introduced

### 1. Original Code (Nov 2025) - No Bug
- Commit: `c894781` (2025-11-03)
- `stopResponse()` only managed `inProgress` state
- No `preparing` state existed yet
- **No bug at this point**

### 2. Crash Fix Added `setPreparing` (Jan 2026) - Bug Introduced
- Commit: `8f6f1b5` (2026-01-12)
- Added `setPreparing(false)` in `generateResponse()` early return path
- Fixed crash where UI got stuck after token limit reset
- **BUT**: Did NOT update `stopResponse()` to also call `setPreparing(false)`
- **Bug introduced**: Stop button leaves `preparing=true`

### 3. Perplexica Migration (Feb 2026) - Bug Remained
- Commit: `dee71f6` (2026-02-02)
- Added Perplexica web search integration
- Added more `setPreparing(false)` calls in success path (line 211)
- **BUT**: Still didn't fix `stopResponse()`
- **Bug persisted**: Regenerate/stop still leaves `preparing=true`

### 4. Bug Fix (Feb 2026) - Fixed
- Commit: `f79caa4` (2026-02-02)
- Added `setPreparing(false)` to `stopResponse()` (line 359)
- **Bug fixed**: Stop/regenerate now properly resets UI state

---

## Root Cause Analysis

### The State Machine

The UI has two related but independent states:

```kotlin
// ChatUiState in ChatViewModel.kt
val inProgress: Boolean     // Is inference running?
val preparing: Boolean      // Is model preparing (prefill phase)?
```

**Normal Flow**:
```
User sends message
  → setInProgress(true)
  → setPreparing(true)
  → Start inference
  → First token arrives → setPreparing(false)  // Still inProgress=true
  → All tokens complete → setInProgress(false)
```

**Stop/Regenerate Flow (BEFORE FIX)**:
```
User hits stop button
  → stopResponse() called
  → setInProgress(false) ✅
  → setPreparing(???) ❌ NOT CALLED
  
Result: preparing=true FOREVER
  → Loading spinner keeps rotating
  → Persists even in new chats
```

**Stop/Regenerate Flow (AFTER FIX)**:
```
User hits stop button
  → stopResponse() called
  → setInProgress(false) ✅
  → setPreparing(false) ✅
  
Result: UI state fully reset
  → Spinner stops
  → Works correctly in new chats
```

---

## Why This Wasn't Caught Earlier

### 1. Incomplete Fix in Commit 8f6f1b5
The crash fix only addressed the **early return** path in `generateResponse()`:

```kotlin
// Line 140-142 (commit 8f6f1b5)
setInProgress(false)
setPreparing(false)
return@launch
```

But **forgot** to update `stopResponse()` which is called from:
- Stop button press
- Regenerate button press  
- New chat creation (via `resetSession`)

### 2. Multiple Code Paths
There are 4 places that need to reset preparing state:
1. ✅ Early return (token limit) - **FIXED in 8f6f1b5**
2. ✅ Perplexica success path - **FIXED in dee71f6**
3. ✅ Normal inference completion - **ALREADY CORRECT**
4. ❌ Stop/regenerate - **BROKEN until f79caa4**

### 3. Testing Gap
The bug only manifests when user:
1. Starts a response
2. Hits **stop** or **regenerate** (not just waits for completion)
3. Creates **new chat** or sends another message

Most testing follows happy path (wait for completion), so this was missed.

---

## Code Locations

### `stopResponse()` - The Bug Location
**File**: `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt`  
**Line**: 352-372

**BEFORE (broken)**:
```kotlin
fun stopResponse(model: Model) {
  Log.d(TAG, "Stopping response for model ${model.name}...")
  if (getLastMessage(model = model) is ChatMessageLoading) {
    removeLastMessage(model = model)
  }
  viewModelScope.launch(Dispatchers.Default) {
    setInProgress(false)  // ✅ Resets inProgress
    // ❌ MISSING: setPreparing(false)
    
    val instance = model.instance as? LlmModelInstance
    if (instance != null) {
      instance.conversation.cancelProcess()
    }
  }
}
```

**AFTER (fixed)**:
```kotlin
fun stopResponse(model: Model) {
  Log.d(TAG, "Stopping response for model ${model.name}...")
  if (getLastMessage(model = model) is ChatMessageLoading) {
    removeLastMessage(model = model)
  }
  viewModelScope.launch(Dispatchers.Default) {
    setInProgress(false)   // ✅ Resets inProgress
    setPreparing(false)    // ✅ Resets preparing (THE FIX)
    
    val instance = model.instance as? LlmModelInstance
    if (instance != null) {
      instance.conversation.cancelProcess()
    }
  }
}
```

---

## Impact Assessment

### User-Visible Symptoms
- ❌ Loading spinner keeps rotating after hitting stop
- ❌ Spinner persists even in new chats
- ❌ Send button may not appear (blocked by `preparing=true`)
- ❌ Confusing UX: looks like app is still generating

### Affected Scenarios
1. Stop button during generation → **BROKEN**
2. Regenerate button → **BROKEN**
3. Normal completion (wait for all tokens) → **WORKS** (not affected)
4. Perplexica search success → **WORKS** (fixed in dee71f6)

### Frequency
**HIGH** - Any user who uses stop/regenerate buttons encounters this.

---

## Prevention Strategies

### 1. Audit All State Setters
When adding new state flags (`preparing`, `inProgress`, etc.), audit ALL code paths:
- ✅ Start path (set to true)
- ✅ Success path (set to false)
- ✅ Error path (set to false)
- ✅ **Cancel/Stop path** (set to false) ← **This was missed**
- ✅ Reset/Clear path (set to false)

### 2. State Reset Checklist
Every function that can abort/stop/cancel inference must reset:
```kotlin
// Template for stopping inference
setInProgress(false)
setPreparing(false)
setIsResettingSession(false)  // If applicable
// ... any other UI state flags
```

### 3. Test Stop/Cancel Paths
Add test cases:
- Start generation → stop midway → verify UI reset
- Start generation → regenerate → verify UI reset
- Start generation → new chat → verify UI reset

### 4. Centralize State Reset
Consider creating a single function:
```kotlin
private fun resetInferenceUIState() {
  setInProgress(false)
  setPreparing(false)
  // Future-proof: all UI state resets in one place
}
```

Then call from:
- `stopResponse()`
- Early returns in `generateResponse()`
- Error handlers
- `resetSession()`

---

## Related Issues

### Similar Bug Fixed in 8f6f1b5
**Problem**: Token limit reset left UI stuck  
**Fix**: Added `setPreparing(false)` to early return path  
**Lesson**: Same root cause - forgot to reset preparing state

### Perplexica Integration (dee71f6)
**Problem**: Added new early return path (Perplexica success)  
**Fix**: Correctly added `setPreparing(false)` + `setInProgress(false)`  
**Lesson**: This commit got it RIGHT, but didn't fix the existing bug in `stopResponse()`

---

## Conclusion

**Why this happened**:
1. New state flag (`preparing`) added incrementally across multiple commits
2. Not all code paths were updated consistently
3. `stopResponse()` was overlooked because it's in a different section of code
4. Testing focused on happy paths, not cancel/stop scenarios

**How we fixed it**:
- Single line change: Added `setPreparing(false)` to `stopResponse()`

**Takeaway**:
When adding **state management** (flags like `inProgress`, `preparing`), do a **complete audit** of all code paths:
- Start, Success, Error, **Cancel**, Reset
- Don't just fix the immediate bug - fix ALL similar paths at once

