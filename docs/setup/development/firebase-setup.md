# Firebase Crashlytics Setup Guide

This document explains how to complete the Firebase Crashlytics integration for OnDevice AI.

## ⚠️ Current Status

**Code Integration**: ✅ COMPLETE
- Firebase Crashlytics dependencies added
- CrashlyticsLogger utility created
- ProGuard configured
- Application initialization code added

**Firebase Project Setup**: ⏳ **USER ACTION REQUIRED**
- Firebase project needs to be created
- `google-services.json` needs to be downloaded and placed in `app/` directory
- Build plugins need to be enabled

---

## Quick Start (10 minutes)

### Step 1: Create Firebase Project (5 min)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Project name: **"OnDevice AI Android"** (or your preferred name)
4. Enable Google Analytics: **Yes** (free tier, required for Crashlytics)
5. Select or create Analytics account
6. Click **"Create project"**
7. Wait for project creation (~30 seconds)

### Step 2: Register Android App (3 min)

1. In Firebase Console, click **"Add app"** → **Android** icon
2. Fill in:
   - **Package name**: `ai.ondevice.app` (IMPORTANT: must match exactly)
   - **App nickname**: "OnDevice AI" (optional but recommended)
   - **Debug signing SHA-1**: (optional, skip for now)
3. Click **"Register app"**

### Step 3: Download google-services.json (1 min)

1. Download **google-services.json** file
2. **IMPORTANT**: Save to `app/` directory (same level as `build.gradle.kts`)
3. Verify file location:
   ```
   OnDeviceAI/
   └── src/
       └── app/
           ├── build.gradle.kts
           ├── google-services.json  ← HERE
           └── src/
   ```
4. **DO NOT commit** this file to git (already in `.gitignore`)

### Step 4: Enable Crashlytics in Firebase Console (1 min)

1. In Firebase Console, navigate to **Crashlytics** (left sidebar)
2. Click **"Enable Crashlytics"**
3. Follow the setup wizard (it will detect your Android app)
4. Click **"Finish"** when complete

### Step 5: Enable Plugins in Build Files

**File**: `app/build.gradle.kts`

**Step 5a**: Find lines 31-32:
```kotlin
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
```

Change to:
```kotlin
alias(libs.plugins.google.services)  // Remove "apply false"
alias(libs.plugins.firebase.crashlytics)  // Remove "apply false"
```

**Step 5b**: Uncomment ProGuard mapping upload (around line 100):
```kotlin
// Before:
// configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
//   mappingFileUploadEnabled = true
// }

// After:
configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
  mappingFileUploadEnabled = true
}
```

### Step 6: Build and Test

```bash
# Build the app (triggers Firebase configuration)
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n ai.ondevice.app/.MainActivity

# Trigger test crash (via debug force crash button in Settings)
# or manually trigger: throw RuntimeException("Test crash")

# Relaunch app (triggers crash upload)
adb shell am start -n ai.ondevice.app/.MainActivity

# Wait 5 minutes, then check Firebase Console → Crashlytics
```

---

## Detailed Setup Instructions

### Firebase Console Access

**Project URL**: https://console.firebase.google.com/project/[YOUR-PROJECT-ID]

**Key Sections**:
- **Crashlytics Dashboard**: View crashes, crash-free rate, top issues
- **Crashlytics → Settings**: View mapping files (ProGuard deobfuscation)
- **Crashlytics → Alerts**: Configure email notifications

### Verifying Setup

#### 1. Verify google-services.json

```bash
# Check file exists
ls -l app/google-services.json

# Verify package name matches
cat app/google-services.json | grep "package_name"
# Should show: "package_name": "ai.ondevice.app"
```

#### 2. Verify Build Configuration

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Check build logs for Firebase
# Should see: "Parsing json file: .../app/google-services.json"
# Should see: "Crashlytics build ID: ..."
```

#### 3. Verify Crashlytics Initialization

```bash
# Install and launch app
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n ai.ondevice.app/.MainActivity

# Check logs
adb logcat | grep Crashlytics
# Should see: "Crashlytics initialized successfully"
```

#### 4. Test Crash Reporting

**Method 1: Force Crash Button (Debug Build Only)**
1. Open app → Settings
2. Scroll to bottom → Tap "Force Crash (Debug Only)" button
3. App crashes
4. Relaunch app (triggers upload)
5. Wait 5 minutes
6. Check Firebase Console → Crashlytics → should see crash

**Method 2: Manual Trigger**
```kotlin
// Add temporary code in any screen
Button(onClick = {
    CrashlyticsLogger.logUserAction("test_crash", "manual_trigger")
    throw RuntimeException("Test crash from [YOUR NAME]")
}) {
    Text("Test Crash")
}
```

#### 5. Verify Stack Traces (Release Build)

```bash
# Build release APK with ProGuard
./gradlew assembleRelease

# Install and trigger crash
adb install -r app/build/outputs/apk/release/app-release.apk

# Check Firebase Console → Crashlytics
# Stack trace should show file names (e.g., "CrashlyticsLogger.kt:42")
# NOT obfuscated (e.g., NOT "a.b.c.d:10")
```

---

## Monitoring & Alerts

### Configure Email Alerts

1. Firebase Console → **Crashlytics** → **Alerts**
2. Enable:
   - **New issue detected** → Email notification
   - **Crash-free rate drops below 99.5%** → Email notification
   - **Velocity alert (spike in crashes)** → Email notification
3. Add email addresses for notifications

### Dashboard Setup

1. Firebase Console → **Crashlytics** → **Dashboard**
2. Pin key metrics:
   - Crash-free rate (%)
   - Top crashes by occurrence
   - Crashes by Android version
   - Crashes by device model

### Weekly Review Process

**Schedule**: Every Monday, 10:00 AM

**Steps**:
1. Review Crashlytics dashboard
2. Triage new crashes:
   - **P0**: >100 users affected → Fix within 48h
   - **P1**: >10 users affected → Fix within 1 week
   - **P2**: <10 users affected → Add to backlog
3. Create GitHub issues for P0/P1 crashes
4. Update crash status (investigating, resolved)
5. Monitor crash-free rate trend

---

## Troubleshooting

### Build Fails: "File google-services.json is missing"

**Solution**: Download google-services.json from Firebase Console and place in `app/` directory.

### Build Fails: "No matching client found for package name 'ai.ondevice.app'"

**Solution**: Verify package name in Firebase Console matches exactly. Re-download google-services.json if needed.

### Crashlytics Not Initializing

**Check**:
1. google-services.json exists in `app/` directory
2. Plugins applied in `app/build.gradle.kts` (remove `apply false`)
3. Check logcat for error: `adb logcat | grep Crashlytics`

### Crashes Not Appearing in Console

**Common Causes**:
1. **Uploads queued**: Crashes upload on next app launch, not during crash
2. **Network required**: Device must have internet connection
3. **Delay**: Can take 5-15 minutes to appear in console
4. **Crashlytics disabled**: Check `setCrashlyticsCollectionEnabled(true)`

**Solution**:
1. Trigger crash
2. **Relaunch app** (triggers upload)
3. Wait 5-15 minutes
4. Check Firebase Console

### Stack Traces Obfuscated (Release Builds)

**Cause**: ProGuard mapping file not uploaded

**Solution**:
1. Verify `mappingFileUploadEnabled = true` in `app/build.gradle.kts`
2. Rebuild release: `./gradlew assembleRelease`
3. Check Firebase Console → Crashlytics → Settings → Mapping files
4. Wait 10-15 minutes for mapping upload

---

## Privacy & Compliance

### Data Retention

- **Crashes**: 90 days (Firebase default)
- **Non-fatals**: 90 days
- **Custom logs**: Attached to crashes (90 days)
- **Mapping files**: Retained indefinitely

### PII Policy

**NEVER log**:
- User messages or conversations
- Full names, emails, phone numbers
- Profile data
- Any personally identifiable information

**ONLY log**:
- Metadata (message length, token count, duration)
- Model names
- Anonymized user IDs (hashed)
- Technical details (Android version, device model)

### Code Review Checklist

Before deploying:
- [ ] All `CrashlyticsLogger` calls reviewed
- [ ] No user message content logged
- [ ] No profile data logged
- [ ] Only metadata (length, duration, count) logged
- [ ] User ID is anonymized (hashed)

---

## For Other Developers

### Getting google-services.json

**Option 1: Firebase Console** (Recommended)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select "OnDevice AI Android" project
3. Project Settings (gear icon) → Your apps
4. Find "ai.ondevice.app" Android app
5. Click "google-services.json" to download

**Option 2: Ask Project Owner**
Contact project owner/admin to:
1. Add you to Firebase project (as Editor or Viewer)
2. Provide google-services.json file securely (NOT via git/email)

**Option 3: Build Without Firebase** (Development Only)
Keep plugins `apply false` in `app/build.gradle.kts`:
```kotlin
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
```

App will build and run, but Crashlytics will be disabled.

---

## Additional Resources

### Firebase Documentation
- [Get Started with Crashlytics](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Customize Crash Reports](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android)
- [Get Deobfuscated Reports](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android)

### Internal Documentation
- `CrashlyticsLogger.kt` - Logging API with privacy enforcement
- `LESSONS_LEARNED.md` - Crashlytics best practices
- `openspec/changes/crashlytics-integration/` - Implementation details

---

## Quick Reference

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test crash
# Settings → Force Crash button (debug only)

# View logs
adb logcat | grep Crashlytics

# Firebase Console
https://console.firebase.google.com/project/[YOUR-PROJECT-ID]/crashlytics
```

---

**Questions?** Contact project owner or check [Firebase Support](https://firebase.google.com/support)
