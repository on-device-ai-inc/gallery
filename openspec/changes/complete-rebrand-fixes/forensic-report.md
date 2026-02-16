# Forensic Investigation Report: OnDevice AI Rebrand
**Date**: 2026-02-16
**Agent**: abb7b33
**Scope**: Analyze completion status of OnDevice AI rebrand from Google AI Edge Gallery

---

## Executive Summary

The rebrand from Google AI Edge Gallery to OnDevice AI was **95% complete** as of commit 95a4df7 (Feb 11, 2026). The core package renaming and application configuration were executed successfully across 138 files, but **critical runtime issues and user-facing documentation gaps remain unresolved**.

---

## What Was Completed Successfully

**Package & Configuration Refactoring:**
- Package renamed: `com.google.ai.edge.gallery` → `ai.ondevice.app`
- Application ID: `com.google.aiedge.gallery` → `ai.ondevice.app`
- Version bumped: 1.0.9 (code 17) → 1.1.9 (code 35)
- All 131 Kotlin source files migrated to new package structure
- Directory structure refactored: `com/google/ai/edge/gallery/` → `ai/ondevice/app/`

**Build Configuration:**
- `/Android/src/app/build.gradle.kts`: Updated namespace, applicationId, manifestPlaceholders
- `/Android/src/settings.gradle.kts`: Updated rootProject.name = "OnDevice AI"
- Deep linking reconfigured with new scheme: `ai.ondevice.app`
- HuggingFace OAuth redirect scheme updated

**Application Resources:**
- `/AndroidManifest.xml`: package, android:label, deep link scheme all updated
- `/res/values/strings.xml`: App branding strings updated
- Proto definitions: `/proto/settings.proto` updated to `ai.ondevice.app.proto`

**Code Files Migrated:**
All core application files successfully moved and their package declarations updated:
- UI layers (chat, home, llmchat, modelmanager, etc.)
- Data repositories and serializers
- Custom task modules (MobileActions, TinyGarden, ExampleCustomTask)
- Dependency injection and configuration files

---

## Critical Issues Found

**PRIORITY 1: Runtime Failures**

1. **NewReleaseNotification.kt (Line 56)**
   - Status: Still references old GitHub repository
   ```kotlin
   private const val REPO = "google-ai-edge/gallery"
   ```
   - Impact: App cannot fetch release updates; users won't see new app versions
   - Location: `/Android/src/app/src/main/java/ai/ondevice/app/ui/home/NewReleaseNotification.kt`

2. **ModelManagerViewModel.kt**
   - Status: Still fetches model allowlist from old GitHub endpoint
   ```
   https://raw.githubusercontent.com/google-ai-edge/gallery/refs/heads/main/model_allowlists/...
   ```
   - Impact: Model allowlist downloads fail when app tries to fetch updated model lists
   - Location: `/Android/src/app/src/main/java/ai/ondevice/app/ui/modelmanager/ModelManagerViewModel.kt`

3. **Documentation URLs in Code (5 Task Module Files)**
   - `LlmChatTaskModule.kt` (4 occurrences of github.com/google-ai-edge/gallery)
   - `LlmSingleTurnTaskModule.kt` (GitHub references)
   - `ExampleCustomTask.kt` (GitHub references with old package paths)
   - `TinyGardenTask.kt` (GitHub references)
   - `MobileActionsTask.kt` (GitHub references)
   - Impact: In-app "Learn More" links point to broken old repository paths

---

**PRIORITY 2: User-Facing Documentation**

1. **Root README.md**
   - Status: Still shows "Google AI Edge Gallery" as title and throughout
   - References old Google Play store link: `com.google.ai.edge.gallery`
   - References old GitHub repository: `google-ai-edge/gallery`
   - References old wiki: `github.com/google-ai-edge/gallery/wiki`
   - Impact: Users and developers see old branding

2. **Android/README.md**
   - Status: Still titled "Google AI Edge Gallery (Android)"
   - Impact: SEO, discoverability, user confusion

3. **GitHub Configuration Files**
   - Old GitHub issue/PR templates still reference old repository names
   - Wiki links broken

---

**PRIORITY 3: Administrative Documentation**

1. **docs/planning/bmm-workflow-status.yaml**
   - Status: References "AI Edge Gallery" in technical architecture section
   - Impact: Technical documentation inconsistent

---

## Files Changed in Rebrand Commit (95a4df7)

**Total: 138 files** (754 insertions, 754 deletions)

**Categories:**
- Kotlin source files: 131
- Build configuration: 4 (build.gradle.kts, settings.gradle.kts, bundle_config.pb.json, strings.xml)
- Manifest/Proto: 2 (AndroidManifest.xml, settings.proto)

**All source code changes are internal package/class renames** - functionally equivalent, just moved from `com.google.ai.edge.gallery` structure to `ai.ondevice.app` structure.

---

## Files Still Containing Old References

**Total: 43 files** containing "google-ai-edge" or "Edge Gallery":

**Code Files (10 .kt files):**
- `NewReleaseNotification.kt` - hardcoded repo name
- `ModelManagerViewModel.kt` - hardcoded GitHub URL for model list
- `LlmChatTaskModule.kt` - docUrl references (multiple)
- `LlmChatModelHelper.kt` - documentation URLs
- `LlmSingleTurnTaskModule.kt` - docUrl references
- `LlmSingleTurnViewModel.kt` - documentation references
- `ExampleCustomTask.kt` - docUrl with old package paths
- `TinyGardenTask.kt` - docUrl references
- `TinyGardenViewModel.kt` - documentation references
- `MobileActionsTask.kt` - docUrl references

**Documentation/Config (33 files):**
- README.md, Android/README.md
- Model allowlist JSON files (model_allowlists/1_0_8.json, 1_0_9.json) - contain LiteRT-LM links ✓ CORRECT
- DEVELOPMENT.md, Bug_Reporting_Guide.md
- Various doc files in docs/planning, docs/design, docs/legal, docs/incorporation
- GitHub workflow and CI configuration files
- Reference documentation files

---

## What Would Break Today

1. **Release Updates**: NewReleaseNotification would fetch from old repo, find no releases
2. **Model Loading**: ModelManagerViewModel would fail to fetch model allowlist from old GitHub endpoint
3. **Help Links**: Users clicking "Learn More" in app would see 404 on GitHub (wrong package paths)
4. **SEO/Branding**: All public documentation shows old "Google AI Edge Gallery" name

---

## Next Steps to Complete the Rebrand

**Immediate (Critical):**
1. [ ] Fix `NewReleaseNotification.kt` - change REPO constant
2. [ ] Fix `ModelManagerViewModel.kt` - update GitHub URL endpoint
3. [ ] Update all docUrl references in task module files to point to new repository

**Short-term (High):**
4. [ ] Update root README.md with new branding, links, Google Play ID
5. [ ] Update Android/README.md
6. [ ] Update all GitHub wiki and issue template links
7. [ ] Verify Google Play store listing shows correct package ID

**Medium-term (Medium):**
8. [ ] Update docs/planning/bmm-workflow-status.yaml
9. [ ] Document rebrand completion in LESSONS_LEARNED.md
10. [ ] Create post-rebrand verification checklist

---

## Assessment

**Completion Status: 95%**

The rebrand was executed professionally for the core application code. The directory restructuring and package migrations were thorough and correct. However, the handoff to external systems (GitHub, documentation endpoints) was incomplete, leaving runtime failures and user-facing issues.

**Risk Level: MEDIUM-HIGH** - The app will compile and run, but cannot fetch updates or load new model lists until GitHub integration is fixed.

---

## Investigation Methodology

1. **OpenSpec Analysis**: Reviewed openspec/changes/ and openspec/archive/ directories
2. **Git History**: Analyzed commits d16ad29, 303d333, 95a4df7
3. **Code Search**: Used Grep to find remaining "google-ai-edge" and "Edge Gallery" references
4. **File Comparison**: Identified all files changed in rebrand commit
5. **Runtime Analysis**: Traced API endpoints and external resource URLs
6. **Documentation Audit**: Reviewed all user-facing and administrative docs

---

## Recommendations

1. **Immediate Action**: Fix Priority 1 runtime failures before next release
2. **Communication**: Update public documentation to avoid user confusion
3. **Process Improvement**: Create rebrand checklist specification for future projects
4. **Verification**: Use comprehensive grep patterns to catch all references
5. **Testing**: Implement automated tests for external resource URLs

---

**Agent ID**: abb7b33
**Report Generated**: 2026-02-16
**Estimated Fix Time**: 2-3 hours for complete implementation
