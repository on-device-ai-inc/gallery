# Spec Delta: crashlytics-integration

This document describes the specification changes resulting from integrating Firebase Crashlytics for crash reporting and stability monitoring.

---

## ADDED

### crashlytics.md (New Spec)

**Location**: `openspec/specs/crashlytics.md`

```markdown
# Crashlytics Specification

## Overview
Firebase Crashlytics integration for crash reporting, stability monitoring, and issue tracking in production.

## Problem Statement

**Without Crashlytics**:
- Zero visibility into production crashes
- Cannot diagnose user-reported issues
- No stability metrics (crash-free rate unknown)
- Crashes go unfixed due to lack of information

**With Crashlytics**:
- Automatic crash reporting with stack traces
- Aggregated crash data (identify most impactful issues)
- Stability metrics (crash-free rate >99.5% target)
- Custom logging for debugging context

## Architecture

### Components

#### 1. Firebase Crashlytics SDK
Core crash reporting framework from Google Firebase.

**Dependencies**:
```gradle
// app/build.gradle
plugins {
    id 'com.google.gms.google-services'
    id 'com.google.firebase.crashlytics'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-crashlytics'
    implementation 'com.google.firebase:firebase-analytics'  // Required for Crashlytics
}
```

**Initialization**:
```kotlin
// OnDeviceAIApplication.kt
class OnDeviceAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_number", BuildConfig.VERSION_CODE)
            log("Crashlytics initialized")
        }
    }
}
```

#### 2. CrashlyticsLogger (Wrapper)
Centralized wrapper for Crashlytics API with privacy enforcement.

```kotlin
object CrashlyticsLogger {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    /**
     * Log model load event.
     * PRIVACY: Only log model name, success/failure, duration. NO user data.
     */
    fun logModelLoad(modelName: String, success: Boolean, durationMs: Long) {
        crashlytics.log("MODEL_LOAD: $modelName, success=$success, duration=${durationMs}ms")
        if (!success) {
            crashlytics.recordException(Exception("Model load failed: $modelName"))
        }
    }

    /**
     * Log inference event.
     * PRIVACY: Only log model name, token count, duration. NO prompt content.
     */
    fun logInference(modelName: String, tokenCount: Int, durationMs: Long) {
        crashlytics.log("INFERENCE: model=$modelName, tokens=$tokenCount, duration=${durationMs}ms")
    }

    /**
     * Log compression event.
     * PRIVACY: Only log token counts, reduction %. NO message content.
     */
    fun logCompression(originalTokens: Int, finalTokens: Int, reductionPercent: Float) {
        crashlytics.log("COMPRESSION: $originalTokens → $finalTokens (${reductionPercent}%)")
    }

    /**
     * Log user action.
     * PRIVACY: Only log action type and metadata (length, count). NO content.
     */
    fun logUserAction(action: String, details: String = "") {
        crashlytics.log("USER_ACTION: $action $details")
    }

    /**
     * Set user identifier (anonymized).
     * PRIVACY: Use hashed or UUID identifier, NEVER real name/email.
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId.hashCode().toString())
    }

    /**
     * Set conversation ID for crash correlation.
     */
    fun setConversationId(conversationId: Long) {
        crashlytics.setCustomKey("conversation_id", conversationId)
    }

    /**
     * Record non-fatal error (logged but app doesn't crash).
     */
    fun recordNonFatalError(exception: Exception, context: String) {
        crashlytics.log("NON_FATAL: $context")
        crashlytics.recordException(exception)
    }
}
```

#### 3. ProGuard Mapping Upload
Automatic upload of ProGuard mapping files for deobfuscating release build crashes.

**Configuration**:
```gradle
// app/build.gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true

            // Crashlytics mapping file upload
            firebaseCrashlytics {
                mappingFileUploadEnabled true
            }
        }
    }
}
```

**ProGuard Rules**:
```proguard
# Keep Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Keep exception classes
-keep public class * extends java.lang.Exception
```

## Functional Requirements

### FR-1: Automatic Crash Reporting
- **When**: App crashes (uncaught exception)
- **Action**: Crash automatically logged to Firebase Crashlytics
- **Data Collected**:
  - Stack trace (file names, line numbers)
  - Device info (model, Android version, RAM, storage)
  - App version (version name, build number)
  - Custom logs (up to 64KB)
  - User ID (anonymized)
  - Conversation ID (current conversation)
- **Upload**: On next app launch (not during crash)

### FR-2: Custom Logging
- **Model Load**: Log model name, success/failure, duration
- **Inference**: Log model name, token count, duration
- **Compression**: Log token counts, reduction percentage
- **User Actions**: Log action type and metadata (NOT content)
- **Limit**: Last 64KB of logs included in crash report

### FR-3: Non-Fatal Error Logging
- **Use Cases**:
  - Model load failures (user can continue with different model)
  - Inference errors (user can retry)
  - OOM warnings (user can delete data)
  - Compression failures (fallback to uncompressed context)
- **Action**: Log exception without crashing app
- **Visibility**: Appears in Firebase Console → Non-fatals section

### FR-4: User & Conversation Correlation
- **User ID**: Set on app launch or first use
  - Use: `userId.hashCode().toString()` (anonymized)
  - Purpose: Correlate crashes to same user
- **Conversation ID**: Set when loading conversation
  - Purpose: Know which conversation caused crash
- **Privacy**: NO real names, emails, phone numbers

### FR-5: ProGuard Deobfuscation
- **Release Builds**: Obfuscated with ProGuard
- **Mapping Upload**: Automatic via Gradle plugin
- **Result**: Stack traces show original file names and line numbers
- **Verification**: Check Firebase Console → Settings → Mapping files

## Non-Functional Requirements

### NFR-1: Performance
- Initialization: <100ms (non-blocking)
- Crash upload: On next app launch (async, non-blocking)
- Impact: <5% increase in app size
- No impact on app performance (crash handling is post-crash)

### NFR-2: Privacy
- **NO PII logged**:
  - ❌ User messages/conversations
  - ❌ Full names, emails, phone numbers
  - ❌ Profile data
- **ONLY metadata logged**:
  - ✅ Message length (int)
  - ✅ Model name (string)
  - ✅ Duration (long)
  - ✅ Token count (int)
  - ✅ Anonymized user ID (hash)
- **Data Retention**: 90 days (Firebase default)
- **Compliance**: GDPR-compliant (Firebase certified)

### NFR-3: Reliability
- Crash reporting works even when network unavailable (queued)
- Crashes uploaded on next app launch
- Multiple crashes batched (not sent individually)
- Crash-free rate target: >99.5%

### NFR-4: Observability
- Firebase Console dashboard shows:
  - Crash-free rate (%)
  - Top crashes by occurrence
  - Crashes by Android version
  - Crashes by device model
  - Crash trend over time
- Alerts:
  - New issue detected → Email
  - Crash-free rate drops below 99.5% → Email
  - Crash spike (velocity alert) → Email

## Integration Points

### Application Initialization
```kotlin
// OnDeviceAIApplication.kt
class OnDeviceAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeCrashlytics()
    }

    private fun initializeCrashlytics() {
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_number", BuildConfig.VERSION_CODE)
            log("Crashlytics initialized")
        }
    }
}
```

### Model Loading
```kotlin
// ModelManager.kt
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

### Inference
```kotlin
// InferenceEngine.kt
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

### Context Compression
```kotlin
// ContextCompressor.kt
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

### User Actions
```kotlin
// ChatScreen.kt
Button(onClick = {
    CrashlyticsLogger.logUserAction("send_message", "length=${message.length}")
    viewModel.sendMessage(message)
})

// SettingsScreen.kt
Switch(
    checked = darkModeEnabled,
    onCheckedChange = { enabled ->
        CrashlyticsLogger.logUserAction("toggle_dark_mode", "enabled=$enabled")
        viewModel.setDarkMode(enabled)
    }
)
```

## Monitoring & Alerting

### Crash Dashboard
**Location**: Firebase Console → Crashlytics → Dashboard

**Key Metrics**:
- Crash-free rate (%) → Target: >99.5%
- Total crashes (count)
- Affected users (count)
- Top crashes by occurrence
- Crash trend (7-day, 30-day)

### Alerts
**New Issue Detected**:
- Trigger: New crash type detected
- Action: Email notification
- Response: Triage within 24h

**Crash-Free Rate Drop**:
- Trigger: Rate drops below 99.5%
- Action: Email notification
- Response: Investigate immediately (P0)

**Velocity Alert**:
- Trigger: Sudden spike in crashes (>3x baseline)
- Action: Email notification
- Response: Investigate immediately (P0)

### Weekly Review Process
**Schedule**: Every Monday, 10:00 AM

**Steps**:
1. Review Crashlytics dashboard
2. Triage new crashes:
   - P0: >100 users affected → Fix within 48h
   - P1: >10 users affected → Fix within 1 week
   - P2: <10 users affected → Add to backlog
3. Create GitHub issues for P0/P1 crashes
4. Update crash status (investigating, resolved)
5. Monitor crash-free rate trend

## Privacy & Compliance

### Privacy Principles
1. **Never log PII**: No user messages, names, emails, phone numbers
2. **Only log metadata**: Lengths, counts, durations, model names
3. **Anonymize identifiers**: Use hashed user IDs, not real IDs
4. **Minimize data**: Only log what's necessary for debugging

### Privacy Enforcement
**Code Review Checklist**:
- [ ] All `CrashlyticsLogger` calls reviewed
- [ ] No user message content logged
- [ ] No profile data logged
- [ ] Only metadata (length, duration, count) logged
- [ ] User ID is anonymized (hashed)

**Automated Scanning** (optional):
```bash
# Scan for potential PII logging
grep -r "CrashlyticsLogger" --include="*.kt" | grep -E "(message|content|fullName|email)"
```

### Data Retention
- **Crashes**: 90 days (Firebase default)
- **Non-fatals**: 90 days
- **Custom logs**: Attached to crashes (90 days)
- **Mapping files**: Retained indefinitely

### Opt-Out (Optional)
If required by privacy policy:
```kotlin
// Settings
fun setCrashlyticsEnabled(enabled: Boolean) {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    PreferencesDataStore.setCrashlyticsEnabled(enabled)
}
```

## Testing

### Unit Tests
- CrashlyticsLoggerTest: Verify logging methods work (mock Crashlytics)

### Integration Tests
- Force crash → verify appears in Firebase Console
- Custom logs → verify appear in crash report
- User ID → verify appears in crash report
- Non-fatal error → verify appears in Non-fatals

### Manual Tests
- [ ] Debug build: Force crash → appears in console within 5 min
- [ ] Release build: Force crash → stack trace readable (not obfuscated)
- [ ] Custom logs: Appear in crash report
- [ ] User ID: Appears in crash report
- [ ] Conversation ID: Appears in custom keys
- [ ] Non-fatal: Appears in Non-fatals section
- [ ] Alert: Email received for test crash

## Acceptance Criteria

- [ ] AC-1: Fatal crashes automatically reported
- [ ] AC-2: Stack traces include file names and line numbers
- [ ] AC-3: Custom logs visible in crash reports
- [ ] AC-4: User ID (anonymized) attached to crashes
- [ ] AC-5: Non-fatal errors logged
- [ ] AC-6: Crash-free rate >99.5% tracked
- [ ] AC-7: Alerts configured (new issue, crash-free rate, velocity)
- [ ] AC-8: ProGuard mapping files uploaded
- [ ] AC-9: No PII logged (verified via code review)
- [ ] AC-10: Documentation complete

## References

- [Firebase Crashlytics Docs](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Custom Logging](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android)
- [ProGuard Support](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android)
- error-handling-patterns.md (Crash reporting patterns)

## Known Issues & Limitations

### google-services.json Security
- **Issue**: Contains Firebase credentials
- **Mitigation**: Add to .gitignore, document secure sharing

### Mapping File Upload Timing
- **Issue**: Can take 10-15 minutes to appear in console
- **Mitigation**: Document expected delay, verify after builds

### Firebase Quota (Free Tier)
- **Limit**: 500K analytics events/month
- **Mitigation**: Sufficient for MVP (10K users × 50 events/user/month)

### Network Dependency
- **Issue**: Requires network to upload crashes
- **Mitigation**: Crashes queued, uploaded when network available

## Migration

**Current State**: NO crash reporting

**Post-Integration**:
- All crashes reported to Firebase Crashlytics
- Stack traces deobfuscated
- Custom logs attached
- Alerts configured

**No Data Migration**: First-time integration, no existing crash data.
```

---

## MODIFIED

### analytics-monitoring.md (Add Crashlytics as Core Component)

**Location**: `openspec/specs/analytics-monitoring.md`

```diff
# Analytics & Monitoring Specification

## Overview
Comprehensive observability covering crashes, performance, user analytics, and quality metrics.

## Components

+ ### Crashlytics (CRITICAL - PHASE 1)
+
+ **Purpose**: Crash reporting and stability monitoring
+
+ **Integration**: Firebase Crashlytics SDK
+
+ **Key Metrics**:
+ - Crash-free rate (target: >99.5%)
+ - Top crashes by occurrence
+ - Non-fatal errors
+
+ **See**: crashlytics.md for detailed spec

### Performance Monitoring (PHASE 2)
(Existing content...)

### User Analytics (PHASE 2)
(Existing content...)

### Quality Metrics (PHASE 3)
(Existing content...)
```

---

## SUMMARY

| Spec File | Type | Changes |
|-----------|------|---------|
| `crashlytics.md` | ADDED | Complete spec for crash reporting, custom logging, privacy compliance |
| `analytics-monitoring.md` | MODIFIED | Added Crashlytics as Phase 1 critical component |

## Rationale

### Why Add Comprehensive Crashlytics Spec?
Production app (v1.1.9, Build 35) has ZERO crash reporting. This is a P0 gap. A detailed spec ensures:
- Correct Firebase integration (dependencies, initialization)
- Privacy compliance (NO PII logging)
- ProGuard deobfuscation (readable stack traces)
- Monitoring and alerting (crash-free rate >99.5%)
- Testing coverage (debug + release builds)

### Why Modify analytics-monitoring.md?
Crashlytics is part of the broader analytics/monitoring framework. Adding it to analytics-monitoring.md:
- Shows Crashlytics as Phase 1 (most critical)
- Positions it alongside other monitoring (Performance, Analytics)
- Establishes that monitoring is layered (crashes first, then performance, then analytics)

## Validation

After implementation:
- [ ] Firebase Crashlytics integrated per crashlytics.md
- [ ] Test crash appears in Firebase Console
- [ ] Stack traces include file names (not obfuscated)
- [ ] Custom logs appear in crash reports
- [ ] No PII logged (code review confirms)
- [ ] Crash-free rate tracking enabled
- [ ] Alerts configured and tested
- [ ] All acceptance criteria met
