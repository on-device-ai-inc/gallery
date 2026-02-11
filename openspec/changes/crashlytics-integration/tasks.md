# Tasks: crashlytics-integration

## Phase 1: Firebase Project Setup (1 hour)

- [ ] **TASK-1.1**: Create Firebase project
  - Go to https://console.firebase.google.com/
  - Click "Add project"
  - Name: "OnDevice AI Android"
  - Enable Google Analytics: Yes (free tier)
  - Create project
  - Acceptance: Firebase project created

- [ ] **TASK-1.2**: Enable Crashlytics in Firebase Console
  - Navigate to Crashlytics section
  - Click "Enable Crashlytics"
  - Follow setup wizard
  - Acceptance: Crashlytics enabled

- [ ] **TASK-1.3**: Register Android app in Firebase
  - Click "Add app" → Android icon
  - Package name: `ai.ondevice.app`
  - App nickname: "OnDevice AI"
  - Debug signing SHA-1 (optional): Get from `./gradlew signingReport`
  - Register app
  - Acceptance: App registered in Firebase

- [ ] **TASK-1.4**: Download google-services.json
  - Download from Firebase Console
  - Save to secure location (NOT in git yet)
  - Verify file contains correct package name
  - Acceptance: google-services.json downloaded

- [ ] **TASK-1.5**: Document Firebase project access
  - Create `docs/firebase-setup.md`
  - Document Firebase project URL
  - Document how other developers can access project
  - Document how to obtain google-services.json
  - Acceptance: Setup documented

## Phase 2: Crashlytics Integration (2 hours)

- [ ] **TASK-2.1**: Add Firebase dependencies to project-level build.gradle
  - Add to buildscript > dependencies:
    - `classpath 'com.google.gms:google-services:4.4.0'`
    - `classpath 'com.google.firebase:firebase-crashlytics-gradle:2.9.9'`
  - Acceptance: Dependencies added, syncs successfully

- [ ] **TASK-2.2**: Add Firebase plugins and dependencies to app-level build.gradle
  - Add plugins:
    - `id 'com.google.gms.google-services'`
    - `id 'com.google.firebase.crashlytics'`
  - Add dependencies:
    - `implementation platform('com.google.firebase:firebase-bom:32.7.0')`
    - `implementation 'com.google.firebase:firebase-crashlytics'`
    - `implementation 'com.google.firebase:firebase-analytics'`
  - Acceptance: Gradle sync successful, no errors

- [ ] **TASK-2.3**: Add google-services.json to app directory
  - Copy google-services.json to `app/` directory
  - Verify placement (root of app module)
  - Acceptance: File in correct location, builds successfully

- [ ] **TASK-2.4**: Add google-services.json to .gitignore
  - Add line: `google-services.json` to `.gitignore`
  - Verify file not staged for commit (git status)
  - Document how to obtain file for new developers
  - Acceptance: google-services.json not tracked by git

- [ ] **TASK-2.5**: Initialize Crashlytics in Application.onCreate()
  - Modify `OnDeviceAIApplication.kt` (or create if doesn't exist)
  - Add Crashlytics initialization code
  - Enable collection: `setCrashlyticsCollectionEnabled(true)`
  - Set custom keys: app_version, build_number
  - Log initialization
  - Acceptance: Crashlytics initializes on app launch

- [ ] **TASK-2.6**: Create CrashlyticsLogger utility class
  - Create `app/src/main/java/ai/ondevice/app/util/CrashlyticsLogger.kt`
  - Implement `logModelLoad(modelName, success, durationMs)`
  - Implement `logInference(modelName, tokenCount, durationMs)`
  - Implement `logCompression(originalTokens, finalTokens, reductionPercent)`
  - Implement `logUserAction(action, details)`
  - Implement `setUserId(userId)`
  - Implement `setConversationId(conversationId)`
  - Implement `recordNonFatalError(exception, context)`
  - Acceptance: CrashlyticsLogger compiles and wraps Firebase Crashlytics

- [ ] **TASK-2.7**: Integrate logging into ModelManager
  - Modify `ModelManager.kt` (or equivalent)
  - Wrap `loadModel()` with try-catch
  - Log success: `CrashlyticsLogger.logModelLoad(modelPath, true, duration)`
  - Log failure: `CrashlyticsLogger.logModelLoad(modelPath, false, duration)` + `recordNonFatalError()`
  - Acceptance: Model load events logged to Crashlytics

- [ ] **TASK-2.8**: Integrate logging into InferenceEngine
  - Modify `InferenceEngine.kt` (or equivalent)
  - Log inference start and end
  - Log inference metrics: `CrashlyticsLogger.logInference(modelName, tokenCount, duration)`
  - Log inference failures as non-fatal errors
  - Acceptance: Inference events logged to Crashlytics

- [ ] **TASK-2.9**: Integrate logging into ContextCompressor (when implemented)
  - Modify `ContextCompressor.kt` (from context-compression-rebuild)
  - Log compression success: `CrashlyticsLogger.logCompression(original, final, reduction%)`
  - Log compression failures as non-fatal errors
  - Acceptance: Compression events logged to Crashlytics

- [ ] **TASK-2.10**: Integrate user action logging into UI
  - Add logging to key user actions:
    - Send message: `CrashlyticsLogger.logUserAction("send_message", "length=$length")`
    - Download model: `CrashlyticsLogger.logUserAction("download_model", modelName)`
    - Change settings: `CrashlyticsLogger.logUserAction("change_setting", "$key=$value")`
    - Delete conversation: `CrashlyticsLogger.logUserAction("delete_conversation")`
  - Acceptance: User actions logged (verified in Logcat)

- [ ] **TASK-2.11**: Set user ID on app launch or first chat
  - Generate or load anonymized user ID (UUID or hashed device ID)
  - Set user ID: `CrashlyticsLogger.setUserId(userId.hashCode().toString())`
  - Acceptance: User ID set (verified in test crash)

- [ ] **TASK-2.12**: Set conversation ID when starting/switching conversations
  - In LlmChatViewModel or ChatScreen, when conversation loaded:
  - `CrashlyticsLogger.setConversationId(conversationId)`
  - Acceptance: Conversation ID set (verified in test crash)

## Phase 3: ProGuard Mapping Upload (1 hour)

- [ ] **TASK-3.1**: Enable ProGuard for release builds (if not already enabled)
  - Modify `app/build.gradle`
  - Set `minifyEnabled true` for release build type
  - Set `shrinkResources true`
  - Add `proguardFiles` configuration
  - Acceptance: Release build uses ProGuard

- [ ] **TASK-3.2**: Enable Crashlytics mapping file upload
  - In `app/build.gradle`, add to release buildType:
  - ```gradle
    firebaseCrashlytics {
        mappingFileUploadEnabled true
    }
    ```
  - Acceptance: Mapping upload configured

- [ ] **TASK-3.3**: Add ProGuard rules for Crashlytics
  - Create or modify `proguard-rules.pro`
  - Add: `-keep class com.google.firebase.crashlytics.** { *; }`
  - Add: `-dontwarn com.google.firebase.crashlytics.**`
  - Add: `-keepattributes SourceFile,LineNumberTable`
  - Add: `-keep public class * extends java.lang.Exception`
  - Acceptance: ProGuard rules added

- [ ] **TASK-3.4**: Test release build with ProGuard
  - Build release APK: `./gradlew assembleRelease` (via CI)
  - Verify build succeeds
  - Verify APK size reduced (ProGuard working)
  - Acceptance: Release build succeeds

- [ ] **TASK-3.5**: Verify mapping file upload in Firebase Console
  - Build release APK (triggers mapping upload)
  - Go to Firebase Console → Crashlytics → Settings → Mapping files
  - Verify latest build version has mapping file
  - Note: May take 10-15 minutes to appear
  - Acceptance: Mapping file uploaded successfully

## Phase 4: Testing (2 hours)

- [ ] **TASK-4.1**: Create force crash button (debug builds only)
  - Add button to Settings screen (visible only in BuildConfig.DEBUG)
  - Button onClick: `throw RuntimeException("Debug test crash")`
  - Acceptance: Force crash button exists in debug build

- [ ] **TASK-4.2**: Test crash reporting in debug build
  - Launch app in debug mode
  - Tap force crash button → app crashes
  - Relaunch app → Crashlytics uploads crash
  - Wait 5 minutes
  - Check Firebase Console → Crashlytics → verify crash appears
  - Acceptance: Debug crash appears in Firebase Console

- [ ] **TASK-4.3**: Test crash reporting in release build
  - Build release APK
  - Install on device: `adb install -r app/build/outputs/apk/release/app-release.apk`
  - Trigger force crash
  - Relaunch app (triggers upload)
  - Wait 5 minutes
  - Check Firebase Console → verify crash appears
  - Acceptance: Release crash appears in Firebase Console

- [ ] **TASK-4.4**: Verify stack traces are readable (not obfuscated)
  - View release build crash in Firebase Console
  - Check stack trace
  - Verify file names visible (e.g., "CrashlyticsLogger.kt:42")
  - Verify NOT obfuscated (e.g., NOT "a.b.c.d:10")
  - Acceptance: Stack traces include file names and line numbers

- [ ] **TASK-4.5**: Test custom logs in crash reports
  - Add custom logs before crash:
    - `CrashlyticsLogger.logModelLoad("gemma-2b-it", false, 1500)`
    - `CrashlyticsLogger.logUserAction("send_message", "length=50")`
  - Trigger crash
  - Check Firebase Console → crash report → Logs tab
  - Verify custom logs appear
  - Acceptance: Custom logs visible in crash report

- [ ] **TASK-4.6**: Test user identifier in crash reports
  - Set user ID: `CrashlyticsLogger.setUserId("test-user-123")`
  - Trigger crash
  - Check Firebase Console → crash report
  - Verify user ID appears
  - Acceptance: User ID visible in crash report

- [ ] **TASK-4.7**: Test conversation ID custom key
  - Set conversation ID: `CrashlyticsLogger.setConversationId(12345L)`
  - Trigger crash
  - Check Firebase Console → crash report → Keys tab
  - Verify "conversation_id: 12345" appears
  - Acceptance: Conversation ID visible in crash report

- [ ] **TASK-4.8**: Test non-fatal error logging
  - Trigger model load failure (or simulate)
  - Log as non-fatal: `CrashlyticsLogger.recordNonFatalError(exception, "Model load failed")`
  - DON'T crash app
  - Check Firebase Console → Non-fatals section
  - Verify non-fatal error appears
  - Acceptance: Non-fatal error visible in Firebase Console

- [ ] **TASK-4.9**: Test multiple crashes (aggregation)
  - Trigger same crash 3 times (force crash button)
  - Check Firebase Console → Crashlytics dashboard
  - Verify crash grouped together (1 issue, 3 events)
  - Acceptance: Crashes aggregated correctly

- [ ] **TASK-4.10**: Test privacy compliance (manual review)
  - Review all CrashlyticsLogger calls in codebase
  - Verify NO user messages logged
  - Verify NO full names logged
  - Verify NO PII logged
  - Only: message length, model names, duration, anonymized IDs
  - Acceptance: No PII found in crash logging code

## Phase 5: Monitoring & Alerting (1 hour)

- [ ] **TASK-5.1**: Configure crash alert emails
  - Firebase Console → Crashlytics → Alerts
  - Enable "New issue detected"
  - Add email addresses for notifications
  - Acceptance: Alert configured

- [ ] **TASK-5.2**: Configure crash-free rate alert
  - Firebase Console → Crashlytics → Alerts
  - Enable "Crash-free rate drops below threshold"
  - Set threshold: 99.5%
  - Add email addresses
  - Acceptance: Crash-free alert configured

- [ ] **TASK-5.3**: Configure velocity alert (crash spike)
  - Firebase Console → Crashlytics → Alerts
  - Enable "Velocity alert"
  - Set sensitivity (default is fine)
  - Add email addresses
  - Acceptance: Velocity alert configured

- [ ] **TASK-5.4**: Test alert notifications
  - Trigger test crash
  - Wait for "New issue detected" email
  - Verify email received
  - Acceptance: Alert email received within 15 minutes

- [ ] **TASK-5.5**: Create custom dashboard
  - Firebase Console → Crashlytics → Dashboard
  - Pin key metrics:
    - Crash-free rate (%)
    - Top crashes by occurrence
    - Crashes by Android version
    - Crashes by device model
  - Acceptance: Dashboard configured

- [ ] **TASK-5.6**: Document weekly review process
  - Create `docs/crashlytics-review-process.md`
  - Document steps: Review dashboard every Monday
  - Document triage criteria (P0: >100 users, P1: >10 users, P2: <10)
  - Document response times (P0: 48h, P1: 1 week, P2: backlog)
  - Acceptance: Process documented

- [ ] **TASK-5.7**: Set up GitHub issue automation (optional)
  - Explore Firebase Crashlytics → Jira/GitHub integration
  - If available, configure auto-create GitHub issue for P0 crashes
  - If not, document manual process
  - Acceptance: Automation explored, documented

## Phase 6: Privacy & Compliance (30 minutes)

- [ ] **TASK-6.1**: Review all logging code for PII
  - Search codebase for all `CrashlyticsLogger` calls
  - Verify each call: Does it log PII? (user messages, names, emails)
  - If found, replace with anonymized data (length, hash, etc.)
  - Acceptance: All logging calls reviewed, no PII found

- [ ] **TASK-6.2**: Add privacy compliance comments
  - Add comment above CrashlyticsLogger class:
  - "// PRIVACY: Never log PII (user messages, names, emails). Only log metadata."
  - Add comment to each log method with examples of what NOT to log
  - Acceptance: Privacy comments added

- [ ] **TASK-6.3**: Document data retention policy
  - Update `docs/privacy-policy.md` (or create if doesn't exist)
  - Document: "Crash data retained 90 days (Firebase default)"
  - Document: "Crash data includes: stack traces, device info, anonymized user ID"
  - Document: "Crash data does NOT include: user messages, profile data"
  - Acceptance: Data retention documented

- [ ] **TASK-6.4**: Implement opt-out (if required by policy)
  - Add setting: "Send crash reports" (default: ON)
  - When toggled off: `FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)`
  - Acceptance: Opt-out implemented (or marked as not required)

- [ ] **TASK-6.5**: Add privacy disclosure to app
  - If required by policy, add to onboarding or settings:
  - "This app collects anonymous crash reports to improve stability."
  - Provide link to full privacy policy
  - Acceptance: Disclosure added (or marked as not required)

## Documentation Tasks

- [ ] **TASK-7.1**: Update LESSONS_LEARNED.md
  - Add: "🟢 ALWAYS integrate Crashlytics before production release"
  - Add: "🟢 ALWAYS upload ProGuard mapping files (configure in gradle)"
  - Add: "🟢 ALWAYS test crash reporting in both debug and release builds"
  - Add: "🔴 NEVER log PII to Crashlytics (user messages, names, emails)"
  - Add: "🔴 NEVER commit google-services.json to git (add to .gitignore)"
  - Add entry to Change Log
  - Acceptance: Lessons documented

- [ ] **TASK-7.2**: Update CODE_INDEX.md
  - Add CrashlyticsLogger.kt entry
  - Update OnDeviceAIApplication.kt with "Crashlytics initialization"
  - Update build.gradle entries with Firebase dependencies
  - Acceptance: Index updated

- [ ] **TASK-7.3**: Create Firebase setup documentation
  - Create `docs/firebase-setup.md` (if not done in TASK-1.5)
  - Document: How to access Firebase project
  - Document: How to obtain google-services.json
  - Document: How to view Crashlytics dashboard
  - Document: How to upload mapping files manually (if needed)
  - Acceptance: Setup fully documented

- [ ] **TASK-7.4**: Create crash triage documentation
  - Create `docs/crash-triage-guide.md`
  - Document: How to interpret crash reports
  - Document: Triage criteria (P0/P1/P2)
  - Document: Response time SLAs
  - Document: How to mark crashes as resolved
  - Acceptance: Triage process documented

## Success Criteria

All tasks checked ✅ AND:
- [ ] Crashlytics integrated and initialized
- [ ] Test crash appears in Firebase Console (debug + release)
- [ ] Stack traces readable (file names visible, not obfuscated)
- [ ] Custom logs appear in crash reports
- [ ] User ID and conversation ID appear in crash reports
- [ ] Non-fatal errors logged to Crashlytics
- [ ] ProGuard mapping files uploaded for release builds
- [ ] Alerts configured (new issue, crash-free rate, velocity)
- [ ] Alert email received for test crash
- [ ] Dashboard configured with key metrics
- [ ] No PII logged (verified via code review)
- [ ] Documentation complete (setup, triage, privacy)
- [ ] google-services.json in .gitignore

---

**Total Tasks**: 49 implementation tasks + 4 documentation tasks = 53 tasks
**Estimated Effort**: 7.5 hours (1 day)
