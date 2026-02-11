# BUG-001: App Crash on Launch with Conversation History

**Status:** In Progress
**Severity:** Critical (P0)
**Component:** Chat History / Model Initialization
**Reporter:** Development Team
**Created:** 2025-11-27
**Branch:** `feature/chat-history-phase1`
**Build Reference:** `10f8071`

---

## Executive Summary

After implementing Chat History Phase 1-4, the OnDevice AI app crashes on launch. Multiple interrelated issues have been identified including audio adapter initialization failures, conversation history rendering bugs, and potential tokenizer loading failures. Partial fixes have been applied but full resolution is pending verification.

---

## 1. Symptoms

### Primary Symptoms
- **App force closes immediately on launch** - Samsung Galaxy device
- **Empty response bubbles in conversation history** - Agent messages displayed without content/action buttons
- **Audio adapter crash** - INVALID_ARGUMENT error when initializing multimodal models

### Secondary Symptoms
- SentencePiece tokenizer fails to load
- Custom `model_allowlist.json` contained 6 models instead of expected 3
- Action buttons (copy, share, regenerate) and disclaimer not rendering for loaded conversations

### Error Messages Observed
```
INVALID_ARGUMENT: Unknown model type 'tf_lite_audio_adapter'
SentencePiece tokenizer initialization failed
```

---

## 2. Environment

| Property | Value |
|----------|-------|
| Device | Samsung Galaxy (Android version TBD) |
| Branch | `feature/chat-history-phase1` |
| Latest Commit | `eba12c0` |
| Build Reference | `10f8071` |
| App Package | `ai.ondevice.app` |
| Models Present | Gemma 3n variants (E2B-IT, E4B-IT) |

### Custom Configuration
- **model_allowlist.json**: Contains 6 models (expected: 3 default models)
- Location: Device external storage or pushed via ADB

---

## 3. Reproduction Steps

### Crash on Launch
1. Install build from commit `eba12c0` or earlier
2. Ensure Gemma 3n multimodal model (E2B-IT or E4B-IT) is downloaded
3. Launch the app
4. **Expected:** App loads to chat screen
5. **Actual:** App force closes immediately

### Empty Response Bubbles
1. Create a conversation with AI model
2. Close app and relaunch
3. Load saved conversation from history drawer
4. **Expected:** All messages render with action buttons
5. **Actual:** Agent messages show empty bubbles, no action buttons or disclaimer

### Audio Adapter Crash (Pre-fix)
1. Download Gemma 3n E2B-IT or E4B-IT model
2. Open AI Chat task (not Audio Scribe)
3. Send any message
4. **Expected:** Model responds normally
5. **Actual:** Crash with `Unknown model type 'tf_lite_audio_adapter'`

---

## 4. Attempted Fixes

### Fix 1: Empty Response Bubbles (Commit `9753430`)

**File:** `ChatViewModel.kt:161-167`

**Problem:** `latencyMs = -1f` was set for all loaded conversation messages. The UI uses `latencyMs >= 0` to determine whether to show action buttons and disclaimer for agent messages.

**Solution:** Conditional latencyMs assignment:
```kotlin
// Before (broken):
latencyMs = -1f // Historical messages don't have latency

// After (fixed):
latencyMs = if (msg.isUser) -1f else 0f // Agent messages need >= 0 for UI
```

**Status:** Applied but **untested on device**

---

### Fix 2: Audio Adapter Crash (Commit `eba12c0`)

**File:** `LlmChatTaskModule.kt:67-68, 133-134`

**Problem:** Commit `c465b3c` changed `supportAudio` from hardcoded `false` to `model.llmSupportAudio`. This enabled audio adapter initialization for multimodal models even in non-audio tasks (AI Chat, Ask Image), causing the MediaPipe LLM inference engine to fail with "Unknown model type".

**Root Cause:** The `tf_lite_audio_adapter` model type is only valid for the dedicated Audio Scribe task. Multimodal models like Gemma 3n support audio input, but enabling the audio adapter in text/image chat contexts triggers an invalid configuration.

**Solution:** Disable audio support in non-audio tasks:
```kotlin
// LlmChatTask and LlmAskImageTask:
supportAudio = false  // Audio not needed for text/image chat

// LlmAskAudioTask (unchanged):
supportAudio = model.llmSupportAudio  // Audio required for this task
```

**Status:** Applied but **untested on device**

---

### Attempted Mitigation: Clear App Data

**Command:** `adb shell pm clear ai.ondevice.app`

**Purpose:** Clear potentially corrupted state, cached model configurations, or stale conversation database

**Status:** Command issued but **app not relaunched to verify**

---

## 5. Root Cause Hypotheses

### Hypothesis 1: Audio Adapter Initialization Race (HIGH CONFIDENCE)
The audio adapter crash (`eba12c0` fix) is likely the primary cause of launch crashes. When the app initializes and attempts to load the last-used model configuration, it may trigger audio adapter initialization for models that don't support it in the current task context.

**Evidence:**
- Gemma 3n models have `llmSupportAudio = true`
- AI Chat task was incorrectly enabling audio adapter
- Error message explicitly mentions `tf_lite_audio_adapter`

### Hypothesis 2: Custom model_allowlist.json Corruption (MEDIUM CONFIDENCE)
The device had 6 models in `model_allowlist.json` instead of the expected 3. This could cause:
- Model lookup failures
- Invalid model configuration being loaded
- Reference to models that don't exist or have incompatible configurations

**Action Required:** Verify contents of custom allowlist and reset to defaults

### Hypothesis 3: SentencePiece Tokenizer Loading Failure (MEDIUM CONFIDENCE)
The tokenizer error suggests the model file may be corrupted or the tokenizer vocabulary file is missing/invalid. This is a separate issue from the audio adapter crash.

**Possible Causes:**
- Incomplete model download
- File corruption after download
- Missing vocab.spm or similar tokenizer file
- Model version mismatch with inference engine

### Hypothesis 4: Conversation Database Migration Issue (LOW CONFIDENCE)
Room database schema changes during Phase 1-4 implementation may have created migration conflicts, though this is less likely given the specific error messages observed.

---

## 6. Remaining Investigation Needed

### Immediate Priority

| # | Investigation Item | Method | Priority |
|---|-------------------|--------|----------|
| 1 | Verify fixes resolve crash | Reinstall and launch app | P0 |
| 2 | Capture full logcat on crash | `adb logcat -d` after crash | P0 |
| 3 | Check model_allowlist.json contents | `adb pull` and inspect | P0 |
| 4 | Verify conversation history renders | Load saved conversation | P1 |

### Secondary Priority

| # | Investigation Item | Method | Priority |
|---|-------------------|--------|----------|
| 5 | Inspect SentencePiece tokenizer error | Logcat filter for tokenizer | P1 |
| 6 | Verify model files integrity | Check file sizes/checksums | P2 |
| 7 | Test with default model_allowlist | Remove custom allowlist | P2 |
| 8 | Test clean install flow | Uninstall, reinstall, fresh start | P2 |

### Questions to Resolve
1. Is the crash occurring during app startup or model initialization?
2. Which model is configured as "last used" and auto-loading?
3. Does the crash occur with all models or only Gemma 3n variants?
4. Is the tokenizer error occurring before or after the audio adapter error?

---

## 7. Testing Checklist

### Pre-Testing Setup
- [ ] Build APK from `feature/chat-history-phase1` branch (commit `eba12c0`)
- [ ] Connect device via ADB and verify: `adb devices`
- [ ] Clear app data: `adb shell pm clear ai.ondevice.app`
- [ ] Optional: Reset model_allowlist.json to defaults

### Critical Path Testing

#### Test 1: App Launch (P0)
- [ ] Install APK: `adb install -r app-debug.apk`
- [ ] Launch app from device
- [ ] **PASS:** App opens without crashing
- [ ] **FAIL:** Capture logcat immediately: `adb logcat -d > crash_log.txt`

#### Test 2: Model Initialization (P0)
- [ ] Download Gemma 3n E2B-IT model (if not present)
- [ ] Initialize model from AI Chat task
- [ ] **PASS:** Model loads without "Unknown model type" error
- [ ] **FAIL:** Document exact error message from logcat

#### Test 3: Conversation History UI (P1)
- [ ] Send test message and receive response
- [ ] Verify response bubble has action buttons (copy, share, regenerate)
- [ ] Verify AI disclaimer appears below response
- [ ] Close and reopen app
- [ ] Load conversation from history drawer
- [ ] **PASS:** Loaded messages show action buttons and disclaimer
- [ ] **FAIL:** Document which UI elements are missing

#### Test 4: Audio Scribe Task (P1)
- [ ] Switch to Audio Scribe task from home screen
- [ ] Initialize Gemma 3n model
- [ ] **PASS:** Audio adapter initializes correctly
- [ ] **FAIL:** Audio features broken by fix

### Regression Testing

#### Test 5: Ask Image Task (P2)
- [ ] Open Ask Image task
- [ ] Select/capture an image
- [ ] Send image with question
- [ ] **PASS:** Image analysis works without crash

#### Test 6: Multiple Model Switching (P2)
- [ ] Download multiple models (if available)
- [ ] Switch between models in AI Chat
- [ ] **PASS:** No crashes on model switching

#### Test 7: Fresh Install Flow (P2)
- [ ] Uninstall app: `adb uninstall ai.ondevice.app`
- [ ] Fresh install
- [ ] Accept ToS and complete onboarding
- [ ] Download default model
- [ ] Send first message
- [ ] **PASS:** Complete flow without errors

---

## 8. Related Commits

| Commit | Description | Files Changed |
|--------|-------------|---------------|
| `eba12c0` | Disable audio support in AI Chat/Ask Image | `LlmChatTaskModule.kt` |
| `9753430` | Fix latencyMs for loaded conversation messages | `ChatViewModel.kt` |
| `c465b3c` | Original change enabling audio based on model capability | `LlmChatTaskModule.kt` |
| `10f8071` | Build reference (isUser boolean fix) | `ConversationExporter.kt` |

---

## 9. Resolution Tracking

| Date | Action | Result | Next Step |
|------|--------|--------|-----------|
| 2025-11-27 | Identified empty bubble cause | Fixed in `9753430` | Test on device |
| 2025-11-27 | Identified audio adapter crash | Fixed in `eba12c0` | Test on device |
| 2025-11-27 | Issued `pm clear` command | Pending verification | Relaunch app |
| 2025-11-27 | Created bug report | Documentation complete | Execute test plan |

---

## 10. Appendix

### A. Key File Locations

```
LlmChatTaskModule.kt:
  app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatTaskModule.kt

ChatViewModel.kt:
  app/src/main/java/ai/ondevice/app/ui/common/chat/ChatViewModel.kt

Model.kt (llmSupportAudio definition):
  app/src/main/java/ai/ondevice/app/data/Model.kt:211

model_allowlist.json (device):
  /storage/emulated/0/Android/data/ai.ondevice.app/files/model_allowlist.json
```

### B. Useful ADB Commands

```bash
# Clear app data
adb shell pm clear ai.ondevice.app

# Capture crash logs
adb logcat -d > crash_log.txt

# Filter for relevant errors
adb logcat | grep -E "(INVALID_ARGUMENT|SentencePiece|audio_adapter|ChatViewModel)"

# Pull model allowlist for inspection
adb pull /storage/emulated/0/Android/data/ai.ondevice.app/files/model_allowlist.json

# Check app storage
adb shell ls -la /storage/emulated/0/Android/data/ai.ondevice.app/files/
```

### C. Expected Default Models (model_allowlist.json)

The standard model allowlist should contain 3 models. The presence of 6 models indicates a custom configuration that may need to be reset for debugging.

---

*Document generated following BMM (Big Map Method) bug documentation standards.*
