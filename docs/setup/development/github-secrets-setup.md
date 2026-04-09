# GitHub Secrets Setup Guide

## BRAVE_API_KEY Configuration

### Problem
The web search feature was not working in CI-built APKs because `local.properties` (which contains `brave.api.key`) is in `.gitignore` and not available during GitHub Actions builds.

### Solution
Add BRAVE_API_KEY as a GitHub repository secret so CI builds can access it.

---

## Quick Setup (2 minutes)

### Step 1: Get Your API Key
Your local API key is in `local.properties`:
```
brave.api.key=YOUR_BRAVE_API_KEY_HERE
```

### Step 2: Add to GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** (top navigation)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret** button
5. Fill in:
   - **Name**: `BRAVE_API_KEY`
   - **Secret**: *(paste your actual API key)*
6. Click **Add secret**

### Step 3: Verify Setup

Push any commit to trigger CI:
```bash
git add .
git commit -m "test: Verify BRAVE_API_KEY in CI"
git push
```

Wait for CI to complete, then download and test the APK:
```bash
# Download APK from GitHub Actions
gh run download <run-id> -n app-debug

# Install
adb install -r app-debug.apk

# Test web search
# 1. Open app
# 2. Go to Settings → Enable web search
# 3. Send a query like "weather today"
# 4. Check logcat for: "[DEBUG] API response received"
adb logcat | grep SearchRepository
```

---

## How It Works

### Local Development
```
build.gradle.kts reads:
1. BRAVE_API_KEY environment variable (if set)
2. brave.api.key from local.properties
3. Defaults to "" if neither found
```

### GitHub Actions CI
```
.github/workflows/ci.yml:
1. Creates local.properties dynamically
2. Writes brave.api.key from GitHub secret
3. Build picks it up → APK includes API key
```

---

## Verification

### Check APK Has API Key (Logcat)

After installing CI-built APK:
```bash
adb logcat | grep "Using API key"
```

You should see:
```
D/SearchRepository: [DEBUG] Using API key: (present) (not "EMPTY!")
```

If you see `EMPTY!`, the secret was not configured correctly.

---

## Troubleshooting

### "Using API key: EMPTY!" in Logcat

**Cause**: GitHub secret not configured or CI didn't have access

**Fix**:
1. Verify secret exists: GitHub → Settings → Secrets → Actions
2. Check secret name is exactly `BRAVE_API_KEY` (case-sensitive)
3. Rebuild: Push new commit to trigger fresh CI run

### CI Build Failed: "brave.api.key not found"

**Cause**: Old CI cache, or secret not available during build

**Fix**:
1. Go to GitHub Actions → Re-run workflow
2. If still fails, add secret and re-run

### Search Still Not Working After Adding Secret

**Checklist**:
- [ ] Secret added to GitHub with name `BRAVE_API_KEY`
- [ ] CI ran AFTER secret was added (push new commit)
- [ ] Downloaded APK from LATEST GitHub Actions run
- [ ] Web search toggle enabled in Settings
- [ ] Daily limit not exceeded (< 5 searches)
- [ ] Device has internet connection

---

## Security Notes

- ✅ **Safe**: GitHub secrets are encrypted and only available during workflow execution
- ✅ **Safe**: Secrets are not printed in CI logs
- ✅ **Safe**: Secrets are not accessible in pull requests from forks
- ⚠️ **Note**: API key is embedded in APK binary (obfuscated but extractable)
- 💡 **Best Practice**: Use server-side proxy for production apps (API key on server, not in APK)

---

## Other Secrets

If you add more secrets in the future, follow the same pattern:

### 1. Add to `.github/workflows/ci.yml`
```yaml
- name: Create local.properties with API keys
  run: |
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    echo "brave.api.key=${{ secrets.BRAVE_API_KEY }}" >> local.properties
    echo "your.new.key=${{ secrets.YOUR_NEW_KEY }}" >> local.properties
```

### 2. Update `app/build.gradle.kts`
```kotlin
val yourNewKey = System.getenv("YOUR_NEW_KEY")
    ?: localProperties.getProperty("your.new.key", "")
buildConfigField("String", "YOUR_NEW_KEY", "\"$yourNewKey\"")
```

### 3. Add secret to GitHub
Same process as above.

---

## Quick Reference

| Action | Command/Location |
|--------|------------------|
| Add secret | GitHub → Settings → Secrets → Actions → New secret |
| Trigger CI | `git push` |
| Check CI status | `gh run list --limit 1` |
| Download APK | `gh run download <id> -n app-debug` |
| Install APK | `adb install -r app-debug.apk` |
| Check logs | `adb logcat \| grep SearchRepository` |

---

**Questions?** Check CI logs: `gh run view <run-id> --log`
