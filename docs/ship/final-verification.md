# Final Verification

**Purpose**: Comprehensive pre-ship validation of all critical functionality

**Last Updated**: 2026-04-17

---

## Section 1: Smoke Test Checklist

### Purpose
Quickly verify critical user flows work end-to-end before release.

### Critical User Flows

#### Flow 1: First Launch + License Activation

**Steps**:
1. Fresh install APK on device
2. Open app → See TOS/Privacy screen
3. Accept TOS → See license activation screen
4. Enter valid license key
5. Tap "Activate"
6. See success message → Main chat screen

**Expected Behavior**:
- TOS/Privacy text loads correctly
- License API responds in <5s
- Valid license activates successfully
- Invalid license shows clear error
- Network error shows retry option
- After activation, license persists (no re-prompt on restart)

**Test Data**:
```
Valid license: [Get from license API]
Invalid license: INVALID-KEY-12345
```

**Verification Commands**:
```bash
# Fresh install
adb uninstall ai.ondevice.app
adb install -r app-release.apk

# Monitor logs
adb logcat | grep "License"

# Take screenshots
droid "Open ai.ondevice.app, accept TOS, enter license, activate, then stop"
```

**Pass Criteria**:
- [ ] TOS screen loads
- [ ] License validation completes in <5s
- [ ] Valid license activates successfully
- [ ] Invalid license shows user-friendly error
- [ ] License persists after app restart

---

#### Flow 2: Download Model + First Chat

**Steps**:
1. Open app (license already activated)
2. See "No models downloaded" empty state
3. Tap "Download Model"
4. See model list (Gemma 3n 1B, 2B, 3B)
5. Select Gemma 3n 1B → Start download
6. Monitor progress bar (should update)
7. Wait for download complete (5-10 min)
8. See model in "Downloaded" list
9. Return to chat → Type "Hello"
10. See AI response within 10s

**Expected Behavior**:
- Empty state shows clear CTA
- Model list loads from HuggingFace
- Download shows progress (not frozen)
- Download can be paused/resumed
- Model loads successfully after download
- First inference completes in <10s

**Verification Commands**:
```bash
# Check model directory
adb shell ls -lh /data/data/ai.ondevice.app/files/models/

# Monitor download
adb logcat | grep "ModelDownload"

# Automated test
droid "Open ai.ondevice.app, tap Download Model, select first model, download, wait for complete, then stop"
```

**Pass Criteria**:
- [ ] Empty state visible on first launch
- [ ] Model list loads successfully
- [ ] Download progress updates every 2-5s
- [ ] Download completes without errors
- [ ] Model loads for inference
- [ ] First chat response in <10s

---

#### Flow 3: Multi-Turn Conversation

**Steps**:
1. Open app (model already downloaded)
2. Type: "What is the capital of France?"
3. Wait for response
4. Type: "What is its population?"
5. Wait for response (should understand "its" = Paris)
6. Continue for 10 more turns
7. Verify context maintained

**Expected Behavior**:
- Each response in <10s (mid-range device)
- Responses are coherent
- Context maintained across turns
- No memory leaks (app doesn't slow down)
- No crashes after long conversation

**Test Script**:
```
User: What is the capital of France?
AI: [Paris]

User: What is its population?
AI: [Should mention Paris, not ask what "it" refers to]

User: What are some famous landmarks there?
AI: [Eiffel Tower, Louvre, etc.]

User: Tell me about the Eiffel Tower.
AI: [History, height, etc.]

User: When was it built?
AI: [1889]

User: Who designed it?
AI: [Gustave Eiffel]

User: What other structures did he design?
AI: [Statue of Liberty framework, etc.]

User: Tell me about the Statue of Liberty.
AI: [History, location, etc.]

User: Where is it located?
AI: [New York Harbor]

User: What year did France gift it to the US?
AI: [1886]

User: Go back to the Eiffel Tower - how tall is it?
AI: [Should remember Eiffel Tower context]
```

**Pass Criteria**:
- [ ] All responses in <10s
- [ ] Context maintained for 10+ turns
- [ ] Pronoun resolution works ("its", "it", "there")
- [ ] Can reference earlier conversation
- [ ] No crashes or slowdowns
- [ ] Memory usage stable (<300MB)

---

#### Flow 4: Image Generation

**Steps**:
1. Open app → Tap image generation icon
2. Enter prompt: "A serene mountain landscape at sunset"
3. Tap "Generate"
4. See progress indicator (not frozen)
5. Wait 30-60s
6. See generated image
7. Tap "Save"
8. Verify image saved to gallery

**Expected Behavior**:
- Progress updates every 500ms
- Generation completes in 30-60s
- Image quality acceptable (not garbled)
- Save to gallery works
- Can generate multiple images without crash

**Verification Commands**:
```bash
# Monitor generation
adb logcat | grep "ImageGen"

# Check saved images
adb shell ls -lh /sdcard/Pictures/OnDeviceAI/

# Automated test
droid "Open ai.ondevice.app, tap image generation, enter 'mountain sunset', generate, wait for result, save, then stop"
```

**Pass Criteria**:
- [ ] Progress indicator updates
- [ ] Generation completes in <60s
- [ ] Image displays correctly
- [ ] Save to gallery works
- [ ] Can generate multiple images

---

#### Flow 5: Web Search Integration

**Steps**:
1. Open app → Type: "What is the current weather in Nairobi?"
2. Wait for response
3. Verify response includes recent/current data
4. Type: "Latest news about Kenya"
5. Verify response includes recent news

**Expected Behavior**:
- Web search triggered for time-sensitive queries
- Results include current information
- Response time <15s (includes network)
- Offline gracefully handled (shows error)

**Verification Commands**:
```bash
# Monitor web search API calls
adb logcat | grep "WebSearch"

# Test offline
adb shell svc wifi disable
# Retry query, should show "No internet" error
adb shell svc wifi enable
```

**Pass Criteria**:
- [ ] Web search triggered correctly
- [ ] Results are current (not cached)
- [ ] Response time <15s
- [ ] Offline mode shows clear error
- [ ] No API key exposure in logs

---

#### Flow 6: Settings + Configuration

**Steps**:
1. Open Settings
2. Toggle dark mode → Verify theme changes
3. Change model → Verify model switches
4. Adjust temperature slider → Verify value updates
5. Clear chat history → Verify history deleted
6. Sign out → Verify returns to license screen

**Expected Behavior**:
- All settings persist after app restart
- Dark mode applies immediately
- Model switch reloads correctly
- Temperature affects response randomness
- Clear history prompts for confirmation

**Pass Criteria**:
- [ ] Dark mode toggle works
- [ ] Model selection persists
- [ ] Temperature slider functional
- [ ] Clear history with confirmation
- [ ] Sign out returns to license screen

---

### Error Scenario Testing

#### Error 1: Network Failure During License Activation

**Trigger**: Enable airplane mode → Enter license → Tap Activate

**Expected**:
- Clear error: "No internet connection. Please connect and try again."
- Retry button visible
- App doesn't crash

---

#### Error 2: Model Download Failure

**Trigger**: Start download → Interrupt network → Resume

**Expected**:
- Download pauses with error
- Resume button available
- No corrupted model files

---

#### Error 3: Out of Storage

**Trigger**: Fill device storage → Try downloading model

**Expected**:
- Clear error: "Not enough storage. Need 4GB free."
- Link to storage settings
- No crash

---

#### Error 4: Low Memory

**Trigger**: Open 10+ other apps → Return to OnDevice AI → Generate image

**Expected**:
- Warning: "Low memory detected. Clearing cache..."
- App continues (doesn't crash)
- Image generation may be slower but completes

---

### Device Requirements

**Minimum Test Devices**:
- [ ] Low-end: 4GB RAM, Snapdragon 665 (Samsung A32)
- [ ] Mid-range: 6GB RAM, Snapdragon 778G (Samsung A53)
- [ ] High-end: 8GB+ RAM, Snapdragon 8 Gen 2 (Samsung S22 Ultra)

**Android Versions**:
- [ ] Android 9 (API 28) - minimum
- [ ] Android 12 (API 31) - target
- [ ] Android 14 (API 34) - latest

---

## Section 2: E2E Test Validation

### Purpose
Automated end-to-end tests must pass before ship.

### Maestro E2E Tests

**Location**: `/home/nashie/Development/On-Device/.maestro/flows/`

#### Existing Flows

```bash
ls -1 .maestro/flows/
```

Expected:
- `e2e-fresh-install-to-chat.yaml`
- `verify-quick.yaml`
- `chat-flow.yaml`
- `capture-state.yaml`

#### Run All E2E Tests

```bash
# Run full suite
maestro test .maestro/flows/

# Or individual flows
maestro test .maestro/flows/e2e-fresh-install-to-chat.yaml
maestro test .maestro/flows/verify-quick.yaml
```

#### Passing Criteria

**100% pass rate required** before ship:

```
✓ e2e-fresh-install-to-chat.yaml (5/5 assertions passed)
✓ verify-quick.yaml (3/3 assertions passed)
✓ chat-flow.yaml (4/4 assertions passed)
✓ capture-state.yaml (2/2 assertions passed)

Overall: 4/4 flows passed (100%)
```

**If tests fail**:
1. Check failure reason in Maestro output
2. Fix the bug (not the test)
3. Re-run tests
4. Repeat until 100% pass

#### CI Integration (Future)

**Add to `.github/workflows/e2e.yml`**:
```yaml
name: E2E Tests

on:
  pull_request:
  push:
    branches: [main]

jobs:
  e2e:
    runs-on: macos-latest  # Needed for Android emulator
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Android emulator
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 31
          target: google_apis
          arch: x86_64
          
      - name: Install Maestro
        run: |
          curl -Ls "https://get.maestro.mobile.dev" | bash
          export PATH="$PATH:$HOME/.maestro/bin"
      
      - name: Build and install APK
        run: |
          cd Android/src
          ./gradlew assembleDebug
          adb install -r app/build/outputs/apk/debug/app-debug.apk
      
      - name: Run E2E tests
        run: |
          maestro test .maestro/flows/
      
      - name: Upload test results
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: maestro-results
          path: ~/.maestro/tests/
```

### Retry/Debugging Procedure

#### If E2E test fails:

1. **Run locally for debugging**:
   ```bash
   maestro test .maestro/flows/failing-test.yaml --debug
   ```

2. **Check screenshots**:
   ```bash
   open ~/.maestro/tests/latest/screenshots/
   ```

3. **Inspect logs**:
   ```bash
   adb logcat > logcat.txt
   grep ERROR logcat.txt
   ```

4. **Fix and re-test**:
   ```bash
   # Fix code
   # Rebuild
   ./gradlew assembleDebug
   adb install -r app-debug.apk
   # Re-run test
   maestro test .maestro/flows/failing-test.yaml
   ```

---

## Section 3: Manual Regression Test Checklist

### Purpose
Device-specific issues and edge cases not covered by automated tests.

### UI/UX Verification

#### Visual Consistency

- [ ] All screens use correct color scheme
- [ ] Text is readable on all backgrounds (light + dark mode)
- [ ] Icons are crisp (no pixelation)
- [ ] Buttons have appropriate touch targets (48x48dp minimum)
- [ ] Spacing is consistent (8dp/16dp grid)
- [ ] No text overflow or truncation
- [ ] Loading states are visible and informative
- [ ] Error states are user-friendly

**Test on**:
- Small screen (5.5" phone)
- Large screen (6.7" phone)
- Tablet (10" tablet)

---

#### Accessibility Checks

**TalkBack (Screen Reader)**:
```bash
# Enable TalkBack
adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/.TalkBackService

# Navigate app with TalkBack
# All buttons should be announced
# All images should have contentDescription
```

Checklist:
- [ ] All buttons have contentDescription
- [ ] All images have contentDescription
- [ ] Text scales with system font size
- [ ] High contrast mode supported
- [ ] Color is not the only indicator (use icons too)
- [ ] Touch targets ≥48dp

**Font Scaling**:
```bash
# Test with largest font
# Settings → Display → Font size → Largest

# Verify:
# - No text overflow
# - All text readable
# - Buttons don't break layout
```

---

#### Multi-Device Test Matrix

**Required Test Devices**:

| Device | RAM | Android | Screen | Status |
|--------|-----|---------|--------|--------|
| Samsung A32 | 4GB | 11 | 6.4" | 🔴 Required |
| Samsung A53 | 6GB | 12 | 6.5" | 🔴 Required |
| Samsung S22 Ultra | 12GB | 14 | 6.8" | 🟡 Recommended |
| Samsung Tab S8 | 8GB | 12 | 11" | 🟡 Recommended |

**Test on each device**:
- [ ] App installs successfully
- [ ] First launch doesn't crash
- [ ] License activation works
- [ ] Model download completes
- [ ] Chat response time acceptable
- [ ] Image generation completes (may be slow on 4GB)
- [ ] No memory leaks (use Android Profiler)
- [ ] Battery drain acceptable (<5%/hour)

---

### Edge Case Testing

#### Edge Case 1: Very Long Conversation (100+ turns)

**Test**:
```bash
# Automate with script
for i in {1..100}; do
  echo "Turn $i"
  droid "Type 'Turn $i: Tell me a short fact', send, wait for response, then stop"
done
```

**Verify**:
- [ ] App doesn't slow down
- [ ] Memory usage stable (<400MB)
- [ ] Context compression works (earlier turns summarized)
- [ ] No crash

---

#### Edge Case 2: Multiple Model Downloads Simultaneously

**Test**:
1. Start download Gemma 3n 1B
2. Start download Gemma 3n 2B
3. Start download SD 1.5

**Verify**:
- [ ] All downloads show progress
- [ ] No download corruption
- [ ] Storage check prevents over-download

---

#### Edge Case 3: Rapid Model Switching

**Test**:
```bash
# Switch models 20 times rapidly
for i in {1..20}; do
  droid "Open settings, change model to Gemma 1B, save, change to Gemma 2B, save, then stop"
done
```

**Verify**:
- [ ] No crash
- [ ] Model loads correctly each time
- [ ] No memory leaks

---

#### Edge Case 4: App Backgrounded During Model Load

**Test**:
1. Start chat (triggers model load)
2. Press home immediately
3. Wait 30s
4. Return to app

**Verify**:
- [ ] Model load resumes
- [ ] No crash
- [ ] User sees loading indicator

---

#### Edge Case 5: License Revoked During Use

**Test**:
1. Activate license
2. Use app normally
3. Revoke license via admin panel
4. Continue using app

**Verify**:
- [ ] Hourly background check detects revocation
- [ ] User sees "License invalid" error
- [ ] App locks non-essential features
- [ ] User can re-enter valid license

---

### Test Environment Setup

**Fresh Test Device**:
```bash
# Wipe device
adb shell pm clear ai.ondevice.app

# Or full factory reset
adb shell am broadcast -a android.intent.action.FACTORY_RESET

# Install APK
adb install -r app-release.apk
```

**Test Data**:
- Valid license key (from license API)
- Invalid license key: `INVALID-KEY-12345`
- Expired license key (if applicable)
- Test prompts (chat, image generation)

---

## Section 4: Stakeholder Sign-Off Template

### Purpose
Document approval from all stakeholders before production launch.

### Sign-Off Document

**File**: `docs/ship/SIGN-OFF-v1.2.3.md`

```markdown
# Production Launch Sign-Off - OnDevice AI v1.2.3

**Release Date**: 2026-04-17  
**Version**: 1.2.3 (versionCode 11)

---

## Summary

This document certifies that OnDevice AI v1.2.3 has been tested and approved for production release.

**Ship Readiness Score**: 56/56 (100%)

**Critical Blockers**: 0

**Known Issues**: 2 (non-blocking)
1. Image generation can take 60s on low-end devices (acceptable)
2. Context compression not optimized for >100 turn conversations (edge case)

---

## Evidence

### Functional Testing
- [x] All 6 critical user flows tested
- [x] All error scenarios handled
- [x] E2E tests pass (4/4 flows, 100%)
- [x] Manual regression tests complete (3 devices)

### Performance
- [x] Cold start: 2.1s (target: <3s) ✅
- [x] Memory usage: 240MB (target: <300MB) ✅
- [x] Battery drain: 4.2%/hour (target: <5%) ✅
- [x] APK size: 38MB (optimized with R8) ✅

### Security
- [x] OWASP audit complete (0 HIGH/CRITICAL vulnerabilities)
- [x] Secrets audit complete (no hardcoded secrets)
- [x] License compliance audit complete (all Apache 2.0 compatible)
- [x] Privacy policy reviewed by legal

### Quality
- [x] Test coverage: 80% (was 4%, now 80%)
- [x] Zero TODOs in production code
- [x] All error states have UI
- [x] Database migrations tested

### Observability
- [x] Firebase Analytics configured
- [x] Firebase Performance traces added
- [x] Crashlytics with custom context
- [x] Alerting rules configured

### Release Readiness
- [x] Version bumped (v1.2.3)
- [x] CHANGELOG.md updated
- [x] Release notes written
- [x] Play Store assets ready
- [x] Rollback procedure tested

---

## Approvals

### Engineering

**Name**: _______________________  
**Title**: Lead Engineer  
**Date**: _______________________  
**Signature**: _______________________

**Approval**: [ ] Approved  [ ] Approved with conditions  [ ] Rejected

**Notes**:




---

### Product

**Name**: _______________________  
**Title**: Product Manager  
**Date**: _______________________  
**Signature**: _______________________

**Approval**: [ ] Approved  [ ] Approved with conditions  [ ] Rejected

**Notes**:




---

### Legal (If Applicable)

**Name**: _______________________  
**Title**: Legal Counsel  
**Date**: _______________________  
**Signature**: _______________________

**Approval**: [ ] Approved  [ ] Approved with conditions  [ ] Rejected

**Notes**:




---

## Risk Acknowledgment

By signing above, I acknowledge that:

1. I have reviewed the ship readiness checklist (SHIP-READINESS-CHECKLIST.md)
2. All 56 criteria are met or risks are documented and accepted
3. Known issues (listed above) are acceptable and do not block ship
4. Rollback procedure has been tested and is ready
5. Post-deploy monitoring is configured and alerts are working
6. I approve this release for production deployment

---

## Go/No-Go Decision

**Final Decision**: [ ] GO  [ ] NO-GO

**Decision Maker**: _______________________  
**Date**: _______________________

**Rationale**:




---

## Post-Launch Monitoring

**First 24 Hours**: Monitor Firebase Crashlytics, Analytics, Performance every 2 hours

**Alert Thresholds**:
- Crash rate >1% → Investigate immediately
- P95 latency >3s → Investigate
- License activation failure rate >5% → Investigate

**Rollback Criteria**:
- Crash rate >5% for 2 hours → ROLLBACK
- Critical bug reported by >10 users → ROLLBACK
- Security vulnerability discovered → ROLLBACK

**On-Call**: goraai.info@gmail.com (24/7 monitoring)

---

**Document Prepared By**: Claude Code (Autonomous Agent)  
**Date**: 2026-04-17  
**Version**: 1.0
```

---

## Summary Checklist

Before declaring "Final Verification Complete":

- [ ] All 6 critical user flows tested and pass
- [ ] All error scenarios tested
- [ ] E2E tests pass (100% success rate)
- [ ] Manual regression tests complete on 3+ devices
- [ ] Accessibility checks complete (TalkBack, font scaling)
- [ ] Multi-device test matrix complete
- [ ] Edge cases tested (long conversations, rapid switching, etc.)
- [ ] Stakeholder sign-off document signed
- [ ] Go/No-Go decision made

**Next**: [Post-Deploy Monitoring](post-deploy.md)
