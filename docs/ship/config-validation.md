# Configuration Validation

**Purpose**: Validate all configuration before production deployment

**Last Updated**: 2026-04-17

---

## Section 1: Environment Variables Documentation

### Required Environment Variables

#### Android App (via BuildConfig)

**Injected at CI Build Time**:

| Variable | Purpose | Format | Source | Required |
|----------|---------|--------|--------|----------|
| `BRAVE_API_KEY` | Web search integration | `bsa_*` | GitHub Secrets | Yes |
| `FIREBASE_PROJECT_ID` | Analytics + Crashlytics | Project ID string | google-services.json | Yes |
| `LICENSE_API_URL` | License verification endpoint | HTTPS URL | BuildConfig | Yes |

**How to Add**:
```bash
# GitHub Settings → Secrets and variables → Actions → New repository secret
Name: BRAVE_API_KEY
Value: bsa_your_actual_key_here
```

**Injection in CI**:
```kotlin
// In build.gradle.kts
buildConfigField("String", "BRAVE_API_KEY", "\"${System.getenv("BRAVE_API_KEY") ?: ""}\"")
```

#### Website/Backend

**Required in `.env` file** (not committed):

| Variable | Purpose | Format | Example | Required |
|----------|---------|--------|---------|----------|
| `DATABASE_URL` | PostgreSQL connection | `postgresql://...` | `postgresql://user:pass@localhost:5432/ondevice` | Yes |
| `STRIPE_SECRET_KEY` | Payment processing | `sk_live_*` or `sk_test_*` | `sk_live_...` | Yes |
| `RESEND_API_KEY` | Email delivery | `re_*` | `re_...` | Yes |
| `NEXT_PUBLIC_STRIPE_PK` | Stripe public key | `pk_live_*` or `pk_test_*` | `pk_live_...` | Yes |
| `LICENSE_ENCRYPTION_KEY` | Encrypt license codes | 32-byte hex string | Generate via `openssl rand -hex 32` | Yes |

**Validation Procedure**:

1. **Check all required variables are set**:
   ```bash
   # Android
   grep "buildConfigField" Android/src/app/build.gradle.kts | grep "System.getenv"
   # Verify each env var exists in GitHub Secrets
   
   # Website
   cd website
   cat .env.example  # Compare with actual .env
   ```

2. **Validate format**:
   ```bash
   # Database URL should start with postgresql://
   echo $DATABASE_URL | grep -q "^postgresql://" && echo "✅ Valid" || echo "❌ Invalid"
   
   # Stripe keys should start with sk_live_ in production
   echo $STRIPE_SECRET_KEY | grep -q "^sk_live_" && echo "✅ Live mode" || echo "⚠️ Test mode"
   ```

3. **Test connectivity**:
   ```bash
   # Test database connection
   psql $DATABASE_URL -c "SELECT 1"
   
   # Test Stripe key
   curl https://api.stripe.com/v1/customers -u $STRIPE_SECRET_KEY:
   ```

### Missing Variables

**Common Issues**:
- ❌ BRAVE_API_KEY removed (web search broken)
- ⚠️ Using test Stripe keys in production
- ⚠️ Missing LICENSE_ENCRYPTION_KEY (generates new key on restart = all existing licenses invalid)

**Fix**:
- Add missing secrets to GitHub Actions
- Document all secrets in `website/.env.example`
- Never commit `.env` to git

---

## Section 2: Secrets Audit Procedure

### Purpose
Ensure no hardcoded secrets in source code or APK.

### Audit Checklist

#### 2.1 Source Code Scan

**Run these greps**:
```bash
cd /home/nashie/Development/On-Device

# Check for API keys
grep -r "api_key\|apiKey\|API_KEY" \
  Android/src/app/src/main \
  website/src \
  --exclude-dir=node_modules \
  --exclude-dir=.next

# Check for passwords
grep -r "password\|PASSWORD" \
  Android/src/app/src/main \
  website/src \
  --exclude-dir=node_modules

# Check for tokens
grep -r "token\|TOKEN\|secret\|SECRET" \
  Android/src/app/src/main \
  website/src \
  --exclude-dir=node_modules

# Check for private keys
grep -r "BEGIN.*PRIVATE KEY" \
  Android/src/ \
  website/
```

**Expected**: Zero matches in production code (tests can have mock secrets)

#### 2.2 APK Inspection

**Extract and search APK**:
```bash
# Download latest release APK
gh run download <run-id> -n app-release

# Unzip and search
unzip -p app-release.apk | strings | grep -i "api_key\|secret\|password\|token"

# Check BuildConfig.class
unzip app-release.apk -d /tmp/apk
javap -c /tmp/apk/classes.dex | grep -i "api_key\|secret"
```

**Expected**: Only encrypted/obfuscated values, no plaintext secrets

#### 2.3 Git History Scan

**Check for accidentally committed secrets**:
```bash
# Install truffleHog (if not installed)
# pip install trufflehog

# Scan entire repo history
trufflehog git file://. --since-commit HEAD~100

# Or use gitleaks
gitleaks detect --source . --verbose
```

**If secrets found**:
1. Rotate compromised secrets immediately
2. Update GitHub Secrets
3. Use BFG Repo-Cleaner to remove from history (if necessary)

#### 2.4 Files to Always Check

**High-risk locations**:
- `Android/src/app/build.gradle.kts` (BuildConfig definitions)
- `Android/src/app/src/main/res/values/strings.xml`
- `website/.env` (should NOT be committed)
- `website/next.config.js` (public vars only)
- Any `config.json`, `secrets.json` files

**Automated Check**:
```bash
# Add to CI pipeline
git ls-files | grep -E "\.env$|secrets|config\.json" | grep -v ".example" | grep -v ".template"
# Should return: 0 files (all secrets should be in .gitignore)
```

### Remediation Steps

If secrets found in code:

1. **Move to environment variables**:
   ```kotlin
   // BAD
   const val API_KEY = "bsa_hardcoded_key"
   
   // GOOD
   val apiKey = BuildConfig.BRAVE_API_KEY  // Injected at build time
   ```

2. **Add to GitHub Secrets**:
   - Go to repo Settings → Secrets and variables → Actions
   - Add secret with proper name
   - Update build.gradle.kts to inject

3. **Update CI workflow**:
   ```yaml
   # .github/workflows/android-build.yml
   env:
     BRAVE_API_KEY: ${{ secrets.BRAVE_API_KEY }}
   ```

4. **Verify in build**:
   ```bash
   # Build should fail if secret missing
   if [ -z "$BRAVE_API_KEY" ]; then
     echo "Error: BRAVE_API_KEY not set"
     exit 1
   fi
   ```

---

## Section 3: BuildConfig Validation

### Purpose
Verify all BuildConfig fields have correct values for release builds.

### BuildConfig Fields Audit

**Location**: `Android/src/app/build.gradle.kts`

#### Expected Fields

```kotlin
buildConfigField("String", "BRAVE_API_KEY", "\"${System.getenv("BRAVE_API_KEY")}\"")
buildConfigField("String", "LICENSE_API_URL", "\"https://on-device.org/api/license\"")
buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
buildConfigField("int", "VERSION_CODE", "${versionCode}")
buildConfigField("boolean", "ENABLE_DEBUG_FEATURES", "false")  // Must be false in release
```

### Validation Procedure

#### 3.1 Check All Fields Documented

```bash
# Extract all buildConfigField declarations
grep "buildConfigField" Android/src/app/build.gradle.kts

# Compare with BuildConfig usage in code
grep -r "BuildConfig\." Android/src/app/src/main --include="*.kt" | cut -d: -f2 | sort -u
```

**Verify**: Every `BuildConfig.X` in code has corresponding `buildConfigField` in gradle.

#### 3.2 Verify Release vs Debug Differences

```kotlin
// build.gradle.kts
buildTypes {
    getByName("release") {
        buildConfigField("boolean", "ENABLE_DEBUG_FEATURES", "false")
        buildConfigField("String", "API_BASE_URL", "\"https://on-device.org/api\"")
    }
    getByName("debug") {
        buildConfigField("boolean", "ENABLE_DEBUG_FEATURES", "true")
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.0.102:3000/api\"")
    }
}
```

**Check**:
- ✅ Debug features OFF in release
- ✅ Production API URL in release
- ✅ No hardcoded localhost in release

#### 3.3 Inspect Release APK

```bash
# Build release APK
# (via CI, cannot build locally on DGX)
gh run view --log | grep "BuildConfig"

# Download and inspect
gh run download <run-id> -n app-release
unzip app-release.apk -d /tmp/apk
javap -c /tmp/apk/classes.dex | grep "BuildConfig"
```

**Verify**:
- No empty strings for required fields
- No "null" values
- Debug features disabled

### Common Issues

| Issue | Detection | Fix |
|-------|-----------|-----|
| Empty API key | `BuildConfig.BRAVE_API_KEY == ""` | Add to GitHub Secrets |
| Debug features in release | `BuildConfig.ENABLE_DEBUG_FEATURES == true` | Set to false in release buildType |
| Localhost URL in release | `BuildConfig.API_BASE_URL.contains("localhost")` | Use production URL |
| Missing field | Runtime crash: `Unresolved reference: BuildConfig.X` | Add buildConfigField |

---

## Section 4: ProGuard Rules Verification

### Purpose
Ensure release builds work with code obfuscation/minification enabled.

### Critical ProGuard Rules

**Location**: `Android/src/app/proguard-rules.pro`

#### Required Rules

```proguard
# Hilt (dependency injection)
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Room (database)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.migration.Migration

# MediaPipe (on-device inference)
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.mediapipe.** { *; }

# Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Data classes used in API
-keepclassmembers class ai.ondevice.app.data.** {
    *;
}
```

### Validation Procedure

#### 4.1 Build Release APK and Test

```bash
# Trigger CI build
git push

# Wait for build
gh run watch

# Download release APK
gh run download <run-id> -n app-release

# Install on device
adb install -r app-release.apk

# Test critical flows
adb shell am start -n ai.ondevice.app/.MainActivity
# - Chat with AI
# - Generate image
# - Download model
# - Check settings
```

**Expected**: All features work identically to debug build.

#### 4.2 Check for ProGuard Crashes

```bash
# Monitor logcat for ProGuard-related crashes
adb logcat | grep -E "ClassNotFoundException|NoSuchMethodException|NoSuchFieldException"
```

**Common ProGuard issues**:
- `ClassNotFoundException: ai.ondevice.app.data.X` → Add `-keep` rule
- `NoSuchMethodException` → Add `-keepclassmembers`
- Hilt injection fails → Check Hilt `-keep` rules
- Room crashes → Ensure entity classes not stripped

#### 4.3 Verify Mapping File

```bash
# Download ProGuard mapping file (for crash deobfuscation)
gh run download <run-id> -n mapping

# Check mapping.txt exists and is complete
wc -l mapping.txt  # Should be thousands of lines

# Test deobfuscation
# Simulate obfuscated crash log, verify mapping can decode it
```

**Store mapping.txt**: Upload to Firebase Crashlytics for automatic stack trace deobfuscation.

#### 4.4 APK Size Verification

```bash
# Compare debug vs release APK size
ls -lh app-debug.apk app-release.apk

# Release should be significantly smaller (30-50% reduction)
# Debug: ~50MB
# Release: ~25-35MB (with ProGuard/R8)
```

**If release APK is NOT smaller**:
- Check that `minifyEnabled true` in build.gradle.kts
- Check that `shrinkResources true` is enabled

### Known ProGuard Issues

#### Issue 1: Hilt ViewModels Not Injected
**Symptom**: `java.lang.IllegalStateException: Cannot create ViewModel`

**Fix**:
```proguard
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
```

#### Issue 2: Room Entities Stripped
**Symptom**: `IllegalArgumentException: Cannot find Entity`

**Fix**:
```proguard
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
```

#### Issue 3: MediaPipe Models Fail to Load
**Symptom**: `java.lang.UnsatisfiedLinkError: dlopen failed`

**Fix**:
```proguard
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.mediapipe.** { *; }
-keep class com.google.android.odml.** { *; }
```

### Rollback Procedure

If ProGuard breaks release build:

1. **Disable temporarily**:
   ```kotlin
   // build.gradle.kts
   buildTypes {
       release {
           isMinifyEnabled = false  // TEMPORARY - revert after fixing rules
       }
   }
   ```

2. **Identify broken class**:
   - Check crash logs for `ClassNotFoundException`
   - Add `-keep` rule for that class

3. **Test incrementally**:
   - Add `-keep` rules one at a time
   - Test after each addition
   - Document why each rule is needed

4. **Re-enable minification**:
   ```kotlin
   isMinifyEnabled = true
   ```

---

## Summary Checklist

Before declaring "Configuration Validated":

- [ ] All environment variables documented in this file
- [ ] All required secrets added to GitHub Secrets
- [ ] Secrets audit shows 0 hardcoded secrets
- [ ] BuildConfig has all required fields
- [ ] Release APK tested with ProGuard enabled
- [ ] ProGuard mapping.txt uploaded to Crashlytics
- [ ] APK size reduced vs debug build
- [ ] No ProGuard crashes in testing

**Next**: [Dependency Audit](dependency-audit.md)
