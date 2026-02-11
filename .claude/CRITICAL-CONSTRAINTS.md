# CRITICAL PROJECT CONSTRAINTS

## 🚨 DEBUGGING PRINCIPLE - READ THIS FIRST

**WHEN SOMETHING DOESN'T WORK:**

1. ✅ **FIRST:** Assume MY code/solution has a bug - verify MY changes
2. ✅ **SECOND:** Check for configuration issues in MY implementation
3. ✅ **THIRD:** Look for incomplete changes I made
4. ❌ **NEVER FIRST:** Assume user did something wrong
5. ❌ **NEVER FIRST:** Assume GitHub/external systems are broken

**Historical Pattern:** ~100% of issues have been caused by MY incomplete implementations or bugs, NOT user error or external systems.

**When user reports "it's not working":**
- Immediately audit MY recent changes
- Check for missing configuration updates
- Verify all related files were modified
- Look for resource reference mismatches
- Test MY logic assumptions

---

## Build Environment

**❌ CANNOT BUILD LOCALLY**
- The development machine CANNOT run `./gradlew assembleDebug`
- ALL builds MUST be done via GitHub Actions CI/CD
- NEVER suggest running local builds
- After code changes: commit → push → wait for GitHub Actions

## Workflow

1. Make code changes
2. Commit changes locally
3. Push to GitHub
4. Wait for GitHub Actions to build APK
5. Download APK from GitHub Actions artifacts
6. Install on device

## Native Libraries

- libpenguin.so and other native libraries are ONLY packaged by GitHub Actions build
- Local builds will NOT include native libraries
- This causes "Unable to open libpenguin.so" errors on device

## Testing

- All testing requires GitHub Actions build
- Device is connected locally (adb works)
- Cannot use local builds for testing

---

## Recent Issue Examples (Learn From These)

### Issue: Rebrand not showing in app
**Root Cause:** I updated XML drawables but forgot to change ic_launcher.xml to reference @drawable instead of @mipmap
**Lesson:** When changing resource types, check ALL references

### Issue: Blank screen rendering
**Root Cause:** I created invalid SVG paths with multiple M commands
**Lesson:** Validate vector drawable syntax before committing

---

**Last Updated**: 2025-12-15
**Reason**: Development environment constraints - local Gradle builds not possible
