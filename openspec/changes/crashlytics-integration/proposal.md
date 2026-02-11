# Proposal: crashlytics-integration

## Summary
Integrate Firebase Crashlytics for crash reporting, stability monitoring, and issue tracking in production app.

## Motivation

**Current State**:
- ❌ **NO crash reporting** - Zero visibility into production crashes
- ❌ **NO stability metrics** - Don't know crash-free rate
- ❌ **NO error tracking** - Can't diagnose user-reported issues
- ❌ **NO analytics** - Don't know user behavior patterns

**Impact**:
- **P0 Critical**: Users experiencing crashes with NO way to diagnose
- **User Trust**: Crashes go unfixed because we don't know they exist
- **Quality**: Cannot measure stability (<99.5% crash-free target)
- **Debugging**: User reports "app crashed" but no logs/stack traces
- **Compliance**: Production app should have basic observability

**Business Priority**: P0 (Critical) - Cannot operate production app responsibly without crash reporting

## Scope

### IN SCOPE
- ✅ Integrate Firebase Crashlytics SDK
- ✅ Configure crash reporting (automatic + manual)
- ✅ Add custom logging for key events (model load, inference, compression)
- ✅ Add user identifier (anonymized) for crash correlation
- ✅ Configure ProGuard mapping file upload (for obfuscated builds)
- ✅ Test crash reporting in debug + release builds
- ✅ Configure crash-free rate monitoring (target: >99.5%)
- ✅ Add non-fatal error logging for recoverable issues
- ✅ Basic Firebase Analytics integration (free tier)

### OUT OF SCOPE
- ❌ Firebase Performance Monitoring (Phase 2)
- ❌ Remote Config (Phase 2)
- ❌ A/B testing (Phase 2)
- ❌ Custom crash analytics dashboard (use Firebase Console)
- ❌ Third-party crash tools (Sentry, BugSnag) - stick with Firebase

## Acceptance Criteria

### Functional Requirements
- [ ] **AC1**: Fatal crashes automatically reported to Firebase Crashlytics
- [ ] **AC2**: Stack traces include file names and line numbers (ProGuard mapping uploaded)
- [ ] **AC3**: Custom logs visible in crash reports (model name, conversation ID, last action)
- [ ] **AC4**: User identifier attached to crashes (anonymized, for correlation)
- [ ] **AC5**: Non-fatal errors logged for: model load failures, inference errors, OOM warnings
- [ ] **AC6**: Crash-free rate >99.5% tracked in Firebase Console

### Testing Requirements
- [ ] **AC7**: Force crash in debug build → appears in Firebase Console within 5 minutes
- [ ] **AC8**: Force crash in release build → stack trace includes file names (not obfuscated)
- [ ] **AC9**: Custom log appears in crash report
- [ ] **AC10**: User identifier appears in crash report
- [ ] **AC11**: Non-fatal error logged → appears in Firebase Console

### Quality Requirements
- [ ] **AC12**: Crashlytics initialization <100ms (non-blocking)
- [ ] **AC13**: Crash reporting adds <5% to app size
- [ ] **AC14**: Privacy: No PII logged (user messages, profile data, etc.)
- [ ] **AC15**: Compliance: Crash data retention 90 days (Firebase default)

## Technical Approach

### Phase 1: Firebase Project Setup (1 hour)

**Step 1: Create Firebase Project**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project: "OnDevice AI Android"
3. Enable Crashlytics
4. Enable Analytics (free tier, required for Crashlytics)
5. Download `google-services.json`

**Step 2: Add Firebase to Android Project**
```gradle
// project-level build.gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.9.9'
    }
}

// app-level build.gradle
plugins {
    id 'com.google.gms.google-services'
    id 'com.google.firebase.crashlytics'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-crashlytics'
    implementation 'com.google.firebase:firebase-analytics'
}
```

**Step 3: Add google-services.json**
- Place in `app/` directory
- Add to `.gitignore` (sensitive credentials)
- Document how to obtain for other developers

### Phase 2: Crashlytics Integration (2 hours)

**Application Initialization**
```kotlin
// OnDeviceAIApplication.kt
class OnDeviceAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)

            // Set app version
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_number", BuildConfig.VERSION_CODE)

            // Log initialization
            log("Crashlytics initialized")
        }

        // Set exception handler (optional, for extra context)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
            // Re-throw to let Crashlytics handle fatal crash
            throw throwable
        }
    }
}
```

**Custom Logging for Key Events**
```kotlin
// CrashlyticsLogger.kt
object CrashlyticsLogger {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logModelLoad(modelName: String, success: Boolean, durationMs: Long) {
        crashlytics.log("MODEL_LOAD: $modelName, success=$success, duration=${durationMs}ms")
        if (!success) {
            crashlytics.recordException(Exception("Model load failed: $modelName"))
        }
    }

    fun logInference(modelName: String, tokenCount: Int, durationMs: Long) {
        crashlytics.log("INFERENCE: model=$modelName, tokens=$tokenCount, duration=${durationMs}ms")
    }

    fun logCompression(originalTokens: Int, finalTokens: Int, reductionPercent: Float) {
        crashlytics.log("COMPRESSION: $originalTokens → $finalTokens (${reductionPercent}%)")
    }

    fun logUserAction(action: String, details: String = "") {
        crashlytics.log("USER_ACTION: $action $details")
    }

    fun setUserId(userId: String) {
        // Anonymized user ID (hash or UUID)
        crashlytics.setUserId(userId.hashCode().toString())
    }

    fun setConversationId(conversationId: Long) {
        crashlytics.setCustomKey("conversation_id", conversationId)
    }

    fun recordNonFatalError(exception: Exception, context: String) {
        crashlytics.log("NON_FATAL: $context")
        crashlytics.recordException(exception)
    }
}
```

**Integration Points**

1. **Model Loading** (ModelManager.kt):
```kotlin
suspend fun loadModel(modelPath: String): Result<LlmModel> {
    val startTime = System.currentTimeMillis()
    return try {
        val model = loadModelInternal(modelPath)
        val duration = System.currentTimeMillis() - startTime
        CrashlyticsLogger.logModelLoad(modelPath, success = true, duration)
        Result.success(model)
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        CrashlyticsLogger.logModelLoad(modelPath, success = false, duration)
        CrashlyticsLogger.recordNonFatalError(e, "Model load failed: $modelPath")
        Result.failure(e)
    }
}
```

2. **Inference** (InferenceEngine.kt):
```kotlin
suspend fun generate(prompt: String, maxTokens: Int): String {
    val startTime = System.currentTimeMillis()
    return try {
        val result = generateInternal(prompt, maxTokens)
        val duration = System.currentTimeMillis() - startTime
        CrashlyticsLogger.logInference(currentModel, maxTokens, duration)
        result
    } catch (e: Exception) {
        CrashlyticsLogger.recordNonFatalError(e, "Inference failed")
        throw e
    }
}
```

3. **Context Compression** (ContextCompressor.kt):
```kotlin
suspend fun compress(messages: List<ConversationMessage>): CompressionResult {
    return try {
        val result = compressInternal(messages)
        if (result is CompressionResult.Compressed) {
            CrashlyticsLogger.logCompression(
                result.originalTokens,
                result.finalTokens,
                result.reductionPercent
            )
        }
        result
    } catch (e: Exception) {
        CrashlyticsLogger.recordNonFatalError(e, "Compression failed")
        CompressionResult.Failed(e, messages)
    }
}
```

4. **User Actions** (UI):
```kotlin
// When user sends message
CrashlyticsLogger.logUserAction("send_message", "length=${message.length}")

// When user downloads model
CrashlyticsLogger.logUserAction("download_model", modelName)

// When user changes settings
CrashlyticsLogger.logUserAction("change_setting", "theme=dark")
```

### Phase 3: ProGuard Mapping Upload (1 hour)

**Configure Mapping File Upload**
```gradle
// app/build.gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

            // Crashlytics mapping file upload
            firebaseCrashlytics {
                mappingFileUploadEnabled true
            }
        }
    }
}
```

**ProGuard Rules for Crashlytics**
```proguard
# proguard-rules.pro

# Keep Crashlytics classes
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep custom exception classes
-keep public class * extends java.lang.Exception
```

**Verify Mapping Upload**:
1. Build release APK: `./gradlew assembleRelease` (via CI)
2. Check Crashlytics dashboard → Settings → Mapping files
3. Verify latest build has mapping file

### Phase 4: Testing (2 hours)

**Test 1: Force Crash in Debug**
```kotlin
// Debug menu or button
Button(onClick = {
    CrashlyticsLogger.logUserAction("force_crash", "debug_test")
    throw RuntimeException("Debug test crash")
}) {
    Text("Force Crash (Debug Only)")
}
```

Steps:
1. Add force crash button in Settings (debug build only)
2. Tap button → app crashes
3. Relaunch app → Crashlytics uploads crash
4. Check Firebase Console → Crashlytics → should appear within 5 minutes

**Test 2: Force Crash in Release**
1. Build release APK with ProGuard enabled
2. Install on device
3. Trigger crash (via force crash button)
4. Check Firebase Console → verify stack trace has file names (not obfuscated)

**Test 3: Custom Logs**
```kotlin
CrashlyticsLogger.logModelLoad("gemma-2b-it", success = false, durationMs = 1500)
CrashlyticsLogger.logUserAction("send_message", "length=50")
throw RuntimeException("Test crash with custom logs")
```

Steps:
1. Add custom logs before crash
2. Trigger crash
3. Check Firebase Console → crash report → should show custom logs

**Test 4: Non-Fatal Error**
```kotlin
try {
    val model = loadModel("invalid-model.task")
} catch (e: Exception) {
    CrashlyticsLogger.recordNonFatalError(e, "Model load failed")
}
```

Steps:
1. Trigger model load failure
2. Check Firebase Console → Non-fatals → should appear
3. Verify stack trace and context

**Test 5: User Identifier**
```kotlin
CrashlyticsLogger.setUserId("user-12345".hashCode().toString())
throw RuntimeException("Test crash with user ID")
```

Steps:
1. Set user ID
2. Trigger crash
3. Check Firebase Console → crash report → should show user ID

### Phase 5: Monitoring & Alerting (1 hour)

**Configure Alerts**
1. Firebase Console → Crashlytics → Alerts
2. Enable: "New issue detected" → Email notification
3. Enable: "Crash-free rate drops below 99.5%" → Email notification
4. Enable: "Velocity alert (spike in crashes)" → Email notification

**Custom Dashboards**
1. Firebase Console → Crashlytics → Dashboard
2. Pin key metrics:
   - Crash-free rate (target: >99.5%)
   - Top crashes by occurrence
   - Crashes by Android version
   - Crashes by device model

**Weekly Review Process**
1. Every Monday, review Crashlytics dashboard
2. Triage new crashes (P0: >100 users, P1: >10 users, P2: <10 users)
3. Create GitHub issues for P0/P1 crashes
4. Fix P0 crashes within 48 hours
5. Track crash-free rate trend

### Phase 6: Privacy & Compliance (30 minutes)

**Privacy Rules**
```kotlin
// ❌ NEVER log PII
CrashlyticsLogger.logUserAction("send_message", message)  // WRONG - includes message content

// ✅ ALWAYS log anonymized data
CrashlyticsLogger.logUserAction("send_message", "length=${message.length}")  // CORRECT

// ❌ NEVER log sensitive data
CrashlyticsLogger.setCustomKey("full_name", user.fullName)  // WRONG

// ✅ ALWAYS use anonymized identifiers
CrashlyticsLogger.setUserId(user.id.hashCode().toString())  // CORRECT
```

**Data Retention**
- Crashes: 90 days (Firebase default)
- Non-fatals: 90 days
- Custom logs: Attached to crashes (90 days)

**Opt-Out** (if required by privacy policy):
```kotlin
// Allow users to disable crash reporting (optional)
fun setCrashlyticsEnabled(enabled: Boolean) {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
}
```

## Implementation Files

### Files to CREATE
1. `CrashlyticsLogger.kt` - Centralized Crashlytics logging
2. `google-services.json` - Firebase config (not committed)
3. `.github/workflows/firebase-mapping-upload.yml` - Automate mapping upload (optional)

### Files to MODIFY
1. `OnDeviceAIApplication.kt` - Initialize Crashlytics in onCreate
2. `build.gradle` (project + app) - Add Firebase dependencies
3. `proguard-rules.pro` - Keep Crashlytics classes
4. `ModelManager.kt` - Add model load logging
5. `InferenceEngine.kt` - Add inference logging
6. `ContextCompressor.kt` - Add compression logging
7. `LlmChatViewModel.kt` - Add user action logging
8. `.gitignore` - Exclude google-services.json

### Files to CREATE (Tests)
1. `CrashlyticsIntegrationTest.kt` - Test crash reporting (manual/automated)

## References

### Firebase Documentation
- [Firebase Crashlytics Android](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Custom Logging](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android)
- [ProGuard Support](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android)

### PRD References
- PRD v3.0 MASTER - Epic 11: Analytics & Monitoring (F11.1)
- Target: Crash-free rate >99.5%
- Privacy: No PII logging

## Timeline

| Phase | Duration | Output |
|-------|----------|--------|
| **Phase 1**: Firebase setup | 1 hour | Project created, google-services.json downloaded |
| **Phase 2**: Integration | 2 hours | Crashlytics initialized, custom logging added |
| **Phase 3**: ProGuard config | 1 hour | Mapping upload configured |
| **Phase 4**: Testing | 2 hours | All test scenarios validated |
| **Phase 5**: Monitoring setup | 1 hour | Alerts configured, dashboard created |
| **Phase 6**: Privacy compliance | 30 min | Privacy rules enforced |

**Total Effort**: 7.5 hours (1 day)

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Crash reporting functional | 100% | Test crash appears in Firebase within 5 min |
| Stack traces readable | 100% | File names visible (not obfuscated) |
| Custom logs attached | 100% | Logs visible in crash reports |
| Crash-free rate tracking | >99.5% | Firebase Console dashboard |
| Alert notifications working | 100% | Receive email on test crash |
| Privacy compliance | 100% | No PII in any crash reports |

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| ProGuard mapping not uploaded | Medium | High | Test release build, verify mapping in console |
| Performance impact (initialization) | Low | Medium | Initialize async, measure startup time |
| Privacy violation (PII logged) | Medium | Critical | Code review, automated scanning for PII patterns |
| Firebase quota exceeded | Low | Low | Free tier: 500K events/month (sufficient for MVP) |
| google-services.json leaked | Medium | High | Add to .gitignore, document secure sharing |

---

**Status**: PROPOSAL - Awaiting approval
**Next Step**: User reviews and approves → then `/openspec-apply crashlytics-integration`
