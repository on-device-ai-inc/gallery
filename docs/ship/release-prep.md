# Release Preparation

**Purpose**: Prepare app and supporting materials for production release

**Last Updated**: 2026-04-17

---

## Section 1: Version Bump Procedure

### Purpose
Maintain consistent versioning across releases.

### Versioning Scheme

**Android**: Uses `versionCode` (int) + `versionName` (string)

```kotlin
// Android/src/app/build.gradle.kts
defaultConfig {
    versionCode = 10       // Integer, increments EVERY release
    versionName = "1.2.3"  // Semantic version (MAJOR.MINOR.PATCH)
}
```

**Semantic Versioning Rules**:
- **MAJOR** (1.x.x): Breaking changes, major feature additions
- **MINOR** (x.1.x): New features, backward-compatible
- **PATCH** (x.x.1): Bug fixes, minor improvements

### versionCode Increment Rules

**MUST increment for EVERY release** (Google Play requirement):

| Release Type | versionCode Change | Example |
|--------------|-------------------|---------|
| Internal alpha | +1 | 9 → 10 |
| Beta | +1 | 10 → 11 |
| Production | +1 | 11 → 12 |
| Hotfix | +1 | 12 → 13 |

**Never reuse a versionCode** - Google Play will reject it.

### Manual Version Bump Procedure

#### Step 1: Determine Version Type

```bash
# Check current version
grep "versionCode\|versionName" Android/src/app/build.gradle.kts

# Review commits since last release
git log v1.2.2..HEAD --oneline

# Decide version bump type:
# - Breaking changes? → MAJOR (2.0.0)
# - New features? → MINOR (1.3.0)
# - Bug fixes only? → PATCH (1.2.3)
```

#### Step 2: Update build.gradle.kts

```kotlin
// Android/src/app/build.gradle.kts
defaultConfig {
    versionCode = 11  // Increment by 1
    versionName = "1.2.3"  // Update based on type
}
```

#### Step 3: Create Git Tag

```bash
# Create annotated tag
git tag -a v1.2.3 -m "Release v1.2.3

- Added feature X
- Fixed bug Y
- Improved performance Z"

# Push tag
git push origin v1.2.3
```

#### Step 4: Update CHANGELOG.md

See [Section 2: Changelog Template](#section-2-changelog-template)

#### Step 5: Trigger Release Build

```bash
# Push to main (triggers CI)
git push origin main

# Or manually trigger workflow
gh workflow run android-release.yml -f version=v1.2.3
```

### Automated Version Bumping (Future)

**Goal**: CI auto-increments versionCode, derives versionName from git tags.

```yaml
# .github/workflows/android-release.yml
name: Release Build

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - name: Extract version from tag
        run: |
          VERSION=${GITHUB_REF#refs/tags/v}
          echo "VERSION_NAME=$VERSION" >> $GITHUB_ENV
      
      - name: Auto-increment versionCode
        run: |
          LAST_CODE=$(grep versionCode build.gradle.kts | grep -o '[0-9]*')
          NEW_CODE=$((LAST_CODE + 1))
          sed -i "s/versionCode = $LAST_CODE/versionCode = $NEW_CODE/" build.gradle.kts
          sed -i "s/versionName = .*/versionName = \"$VERSION_NAME\"/" build.gradle.kts
      
      - name: Build release APK
        run: ./gradlew assembleRelease
```

### Files to Update

| File | What to Update | Example |
|------|---------------|---------|
| `Android/src/app/build.gradle.kts` | `versionCode`, `versionName` | 10 → 11, "1.2.2" → "1.2.3" |
| `CHANGELOG.md` | Add new version section | ## [1.2.3] - 2026-04-17 |
| Git tag | Create annotated tag | `git tag -a v1.2.3` |
| `README.md` (optional) | Update version badge | v1.2.2 → v1.2.3 |

### Verification

```bash
# Verify version updated
grep "versionCode\|versionName" Android/src/app/build.gradle.kts

# Verify git tag created
git tag -l "v*" | tail -5

# Verify tag pushed to remote
git ls-remote --tags origin | grep v1.2.3
```

---

## Section 2: Changelog Template

### Purpose
Maintain human-readable history of all changes.

### CHANGELOG.md Template

**Location**: `/home/nashie/Development/On-Device/CHANGELOG.md`

```markdown
# Changelog

All notable changes to OnDevice AI will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- (Features added in main but not yet released)

### Changed
- (Changes to existing features)

### Fixed
- (Bug fixes)

### Security
- (Security vulnerability fixes)

---

## [1.2.3] - 2026-04-17

### Added
- Context compression for long conversations
- Web search integration with Brave API
- Model parameter persistence

### Changed
- Improved error messages with actionable suggestions
- Updated Firebase Analytics event tracking
- Enhanced loading states with timeouts

### Fixed
- Back button navigation now exits app cleanly
- Database migrations no longer destroy user data
- Empty catch blocks replaced with proper error handling

### Security
- Removed printStackTrace() from production code
- Added certificate pinning for API calls
- Implemented secrets audit in CI

---

## [1.2.2] - 2026-04-10

### Added
- License verification system
- On-device Gemma 3n model support

### Fixed
- Model download reliability improvements
- Crash on low memory devices

---

## [1.2.1] - 2026-04-03

### Fixed
- Hotfix for license activation bug

---

## [1.2.0] - 2026-04-01

### Added
- Image generation with Stable Diffusion
- Dark mode support
- Model management UI

### Changed
- Redesigned settings screen
- Improved chat UI with message grouping

---

## [1.1.0] - 2026-03-15

### Added
- Multi-turn conversations
- Chat history
- Model download progress

### Fixed
- Memory leaks in model inference
- ANR on model initialization

---

## [1.0.0] - 2026-03-01

### Added
- Initial release
- On-device chat with Gemma models
- Firebase Analytics and Crashlytics
- License activation

---

[Unreleased]: https://github.com/ondeviceai/on-device/compare/v1.2.3...HEAD
[1.2.3]: https://github.com/ondeviceai/on-device/compare/v1.2.2...v1.2.3
[1.2.2]: https://github.com/ondeviceai/on-device/compare/v1.2.1...v1.2.2
[1.2.1]: https://github.com/ondeviceai/on-device/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/ondeviceai/on-device/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/ondeviceai/on-device/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/ondeviceai/on-device/releases/tag/v1.0.0
```

### Sections Explained

| Section | Purpose | Example |
|---------|---------|---------|
| **Added** | New features | "Context compression for long conversations" |
| **Changed** | Changes to existing features | "Improved error messages" |
| **Deprecated** | Soon-to-be-removed features | "Model import UI deprecated, use download" |
| **Removed** | Features removed this release | "Removed experimental audio scribe" |
| **Fixed** | Bug fixes | "Fixed back button navigation" |
| **Security** | Security vulnerability fixes | "Fixed CVE-2023-12345 in lodash" |

### Automation Notes

**Manual for now**, but can be automated with:

```bash
# Generate changelog from commits
git log v1.2.2..v1.2.3 --pretty=format:"- %s" --reverse

# Auto-categorize by commit type
git log v1.2.2..v1.2.3 --pretty=format:"%s" | grep "^feat:" # → Added
git log v1.2.2..v1.2.3 --pretty=format:"%s" | grep "^fix:" # → Fixed
```

**Tools**:
- `conventional-changelog` (npm package)
- `git-chglog` (Go tool)
- GitHub Release notes (auto-generated from PRs)

### Example Workflow

```bash
# 1. Review commits since last release
git log v1.2.2..HEAD --oneline --no-merges

# 2. Categorize by type
git log v1.2.2..HEAD --grep="^feat:" --pretty=format:"- %s"
git log v1.2.2..HEAD --grep="^fix:" --pretty=format:"- %s"

# 3. Add to CHANGELOG.md under [Unreleased]

# 4. On release, rename [Unreleased] to [1.2.3] with date

# 5. Create new empty [Unreleased] section
```

---

## Section 3: Release Notes Template

### Purpose
User-facing communication about what's new (different from CHANGELOG.md).

### User-Facing Release Notes

**Audience**: End users (non-technical)

```markdown
# What's New in OnDevice AI 1.2.3

## 🎉 New Features

**Longer Conversations**
Chat for as long as you want! We've added context compression so conversations don't lose quality after 20+ messages.

**Web Search**
Your AI can now search the web for up-to-date information. Just ask a question that requires current data.

**Smarter Error Messages**
When something goes wrong, we now tell you exactly what happened and how to fix it. No more spinning wheels!

## 🔧 Improvements

- Faster app startup (30% quicker to first message)
- Better memory usage (uses less RAM on older devices)
- Dark mode improvements

## 🐛 Bug Fixes

- Fixed back button sometimes not closing the app
- Fixed rare crash when downloading large models
- Fixed settings not saving properly

---

**Known Issues**
- Image generation can take up to 60 seconds on some devices (we're working on it!)

**Need Help?**
Visit https://on-device.org/support or email goraai.info@gmail.com
```

### Developer Release Notes

**Audience**: Technical users, contributors

```markdown
# OnDevice AI 1.2.3 Release Notes

## Breaking Changes
None

## New Features
- Context compression with sliding window + importance sampling
- Web search via Brave API integration
- Model parameter persistence in Proto DataStore

## API Changes
- Added `BuildConfig.BRAVE_API_KEY` (requires GitHub Secret)
- Room database schema updated to v10 (migration included)

## Bug Fixes
- Fixed destructive database migration (data loss risk)
- Replaced empty catch blocks with proper error logging
- Removed printStackTrace() calls (security issue)

## Performance
- Cold start: 2.8s → 2.1s (-25%)
- Memory: 280MB → 240MB (-14%)
- APK size: 52MB → 38MB (-27% with R8 full mode)

## Security
- Added certificate pinning for on-device.org API
- Implemented secrets audit in CI
- Updated Firebase SDK to 32.7.0 (fixes CVE-2023-45678)

## Testing
- Test coverage: 4% → 22% (target: 80%)
- Added 45 new unit tests
- E2E tests now run in CI

## Dependencies
- Kotlin 2.0.21 → 2.1.0
- Compose BOM 2024.01.00 → 2024.02.00
- MediaPipe 0.10.9 → 0.10.10

## Migration Guide
No breaking changes - automatic migration from v1.2.2.

## Known Issues
- Context compression not yet optimized for very long conversations (>100 turns)
- Web search results limited to 10 items

## Contributors
Thanks to @contributor1, @contributor2 for bug reports!

---

Full changelog: https://github.com/ondeviceai/on-device/blob/main/CHANGELOG.md
```

### Distribution Channels

| Audience | Format | Where |
|----------|--------|-------|
| End users | User-facing notes | Play Store "What's New", in-app update prompt |
| Email subscribers | Rich HTML email | Mailchimp/Resend campaign |
| Developers | Technical notes | GitHub Release page, blog post |
| Social media | Short highlights | Twitter/X, LinkedIn, Reddit |

### Email Template (HTML)

```html
<!DOCTYPE html>
<html>
<head>
  <title>OnDevice AI 1.2.3 is Live!</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
  <h1>🎉 OnDevice AI 1.2.3 is Here!</h1>
  
  <p>We've just released a major update with features you've been asking for:</p>
  
  <h2>What's New</h2>
  <ul>
    <li><strong>Longer Conversations:</strong> Context compression keeps quality high even after 50+ messages</li>
    <li><strong>Web Search:</strong> Your AI can now search the web for current information</li>
    <li><strong>Better Errors:</strong> Clear, actionable error messages instead of spinning wheels</li>
  </ul>
  
  <h2>Improvements</h2>
  <ul>
    <li>30% faster app startup</li>
    <li>Uses less memory on older devices</li>
    <li>Dark mode polish</li>
  </ul>
  
  <p style="text-align: center; margin: 30px 0;">
    <a href="https://on-device.org/download" style="background: #007AFF; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px;">
      Download Update
    </a>
  </p>
  
  <p>Questions? Reply to this email or visit <a href="https://on-device.org/support">our support page</a>.</p>
  
  <p>— The OnDevice Team</p>
</body>
</html>
```

---

## Section 4: App Store Assets Checklist

### Purpose
Ensure all required materials are ready for Play Store / App Store submission.

### Google Play Store Requirements

#### Screenshots

**Phone (Required)**:
- [ ] Minimum 2 screenshots, maximum 8
- [ ] 16:9 aspect ratio (1920x1080 or 1080x1920)
- [ ] PNG or JPEG
- [ ] Show actual app UI (no marketing graphics only)

**Tablet (Recommended)**:
- [ ] 7-inch screenshots (1024x600 or 600x1024)
- [ ] 10-inch screenshots (1920x1200 or 1200x1920)

**Screenshot Locations**:
```bash
/home/nashie/Development/On-Device/marketing/screenshots/
├── phone/
│   ├── 01-chat.png
│   ├── 02-image-gen.png
│   ├── 03-models.png
│   ├── 04-settings.png
│   └── 05-dark-mode.png
└── tablet/
    ├── 01-chat-tablet.png
    └── 02-models-tablet.png
```

**How to Generate**:
```bash
# On physical device
adb exec-out screencap -p > screenshot.png

# Or use DroidRun for automated screenshots
droid "Open ai.ondevice.app, navigate to chat, take screenshot, then stop"

# Crop/resize if needed
convert screenshot.png -resize 1080x1920 -gravity center -extent 1080x1920 final.png
```

#### Feature Graphic

- [ ] 1024x500 PNG or JPEG
- [ ] Used in Play Store listing header
- [ ] No text (will be overlaid with title)

**Create with**:
- Figma template: https://figma.com/@google
- Or hire designer on Upwork

#### App Icon

- [ ] 512x512 PNG
- [ ] 32-bit PNG with alpha channel
- [ ] Transparent background (not required but recommended)

**Current icon**: `Android/src/app/src/main/res/drawable/ondevice_logo_full_1024.png`

**Generate all densities**:
```bash
# Use Android Asset Studio or:
convert icon-512.png -resize 48x48 mipmap-mdpi/ic_launcher.png
convert icon-512.png -resize 72x72 mipmap-hdpi/ic_launcher.png
convert icon-512.png -resize 96x96 mipmap-xhdpi/ic_launcher.png
convert icon-512.png -resize 144x144 mipmap-xxhdpi/ic_launcher.png
convert icon-512.png -resize 192x192 mipmap-xxxhdpi/ic_launcher.png
```

#### App Descriptions

**Short Description** (80 characters max):
```
On-device AI chat and image generation. Private, fast, no internet required.
```

**Long Description** (4000 characters max):
```
OnDevice AI brings powerful AI models directly to your phone - no cloud, no internet, 100% private.

KEY FEATURES
✓ On-Device Chat: Talk to Gemma 3n AI models running entirely on your device
✓ Image Generation: Create images with Stable Diffusion locally
✓ Web Search: Get up-to-date information when connected
✓ 100% Private: Your conversations never leave your device
✓ No Internet Required: Use core features completely offline
✓ Fast Responses: No network latency, instant results

ADVANCED FEATURES
• Context Compression: Long conversations stay coherent
• Model Management: Download and switch between multiple AI models
• Dark Mode: Easy on the eyes in any lighting
• License-Based: Fair pricing, no subscriptions

WHY ONDEVICE AI?
Unlike cloud AI services (ChatGPT, Gemini), OnDevice AI runs entirely on your phone. This means:
- Your data stays private (no servers)
- Works offline (no internet needed)
- Fast responses (no network latency)
- No monthly fees (one-time license)

REQUIREMENTS
• Android 9.0+
• 6GB RAM recommended (4GB minimum)
• 8GB free storage (for AI models)
• License key (purchase at on-device.org)

PRICING
$20-30 one-time license (region-based pricing)
No subscriptions. No hidden fees.

SUPPORT
Email: goraai.info@gmail.com
Website: https://on-device.org/support

OPEN SOURCE
Built on Google AI Edge Gallery (Apache 2.0)
See full attributions: Settings → About → Licenses

---

OnDevice AI is developed by On Device AI Inc.
Privacy policy: https://on-device.org/privacy
Terms of service: https://on-device.org/terms
```

#### Video (Optional but Recommended)

- [ ] 30-120 seconds
- [ ] MP4, MPEG, or AVI
- [ ] Max 100MB
- [ ] Shows app in use

**Script outline**:
1. Problem: Cloud AI is slow, not private
2. Solution: OnDevice AI runs locally
3. Demo: Show chat, image gen, offline mode
4. CTA: Download now at on-device.org

#### Privacy Policy Link (Required)

- [ ] Must be publicly accessible URL
- [ ] Current: https://on-device.org/privacy

#### Content Rating

- [ ] Complete IARC questionnaire
- [ ] Declare if app collects user data
- [ ] OnDevice AI likely: PEGI 3, ESRB Everyone

#### App Category

- [ ] Primary: Productivity
- [ ] Secondary: Tools

#### Pricing & Distribution

- [ ] Paid app: NO (use license system instead)
- [ ] In-app purchases: NO
- [ ] Ads: NO
- [ ] Countries: Select Kenya + others

#### Contact Details

- [ ] Email: goraai.info@gmail.com
- [ ] Phone: (optional)
- [ ] Website: https://on-device.org

### Play Store Console Setup Steps

1. **Create app**:
   - Go to https://play.google.com/console
   - Create application → Enter app name
   - Default language: English (US)

2. **Store listing**:
   - Upload screenshots (phone + tablet)
   - Upload feature graphic
   - Upload app icon
   - Write descriptions (short + long)
   - Add privacy policy URL
   - (Optional) Upload video

3. **Content rating**:
   - Start questionnaire
   - Answer truthfully (OnDevice AI likely PEGI 3)
   - Submit for rating

4. **App content**:
   - Privacy policy: Link to https://on-device.org/privacy
   - Data safety: Declare if collecting analytics (Firebase)
   - Government ads: NO

5. **Pricing & distribution**:
   - Free (use license system, not Play billing)
   - Select countries (Kenya + ...)
   - Content guidelines: Accept

6. **Release tracks**:
   - Internal testing: 5-10 testers
   - Closed testing (Beta): 50-100 testers
   - Open testing (Public beta): Unlimited
   - Production: Public release

7. **Upload APK/AAB**:
   - Production release → Create new release
   - Upload app bundle (AAB) or APK
   - Release notes: Copy from [Section 3](#section-3-release-notes-template)
   - Review → Start rollout to production

### Verification Checklist

Before submitting to Play Store:

- [ ] All required screenshots uploaded (min 2 phone)
- [ ] Feature graphic 1024x500
- [ ] App icon 512x512
- [ ] Short description ≤80 chars
- [ ] Long description complete
- [ ] Privacy policy URL working
- [ ] Content rating completed
- [ ] Pricing set (free)
- [ ] Countries selected
- [ ] APK/AAB uploaded and signed
- [ ] Release notes written

**Test Play Store Listing**:
- Create internal testing track first
- Share with 5-10 testers
- Verify all assets display correctly
- Fix issues before public release

---

## Summary Checklist

Before declaring "Release Prep Complete":

- [ ] Version bumped in build.gradle.kts (versionCode + versionName)
- [ ] Git tag created and pushed (v1.2.3)
- [ ] CHANGELOG.md updated with release notes
- [ ] User-facing release notes written
- [ ] Developer release notes written
- [ ] Screenshots captured (min 2, max 8)
- [ ] Feature graphic created (1024x500)
- [ ] App descriptions written (short + long)
- [ ] Privacy policy URL verified
- [ ] Play Store console configured
- [ ] APK/AAB ready to upload

**Next**: [Final Verification](final-verification.md)
