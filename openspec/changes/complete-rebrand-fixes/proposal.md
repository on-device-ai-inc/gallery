# Proposal: complete-rebrand-fixes

## Summary
Complete the OnDevice AI rebrand by fixing critical runtime failures, broken documentation links, and user-facing branding inconsistencies.

## Motivation
The initial rebrand (commit 95a4df7) successfully migrated the package structure from `com.google.ai.edge.gallery` to `ai.ondevice.app` across 138 files. However, forensic analysis revealed that **5% of the rebrand remains incomplete**, causing critical runtime failures:

1. **Runtime Failures**: The app cannot fetch release updates or model allowlists because hardcoded GitHub URLs still point to the old `google-ai-edge/gallery` repository
2. **User Experience**: "Learn More" links in task modules return 404 errors, frustrating users seeking documentation
3. **Branding Confusion**: All public-facing documentation (README files, GitHub templates) still displays "Google AI Edge Gallery" branding, harming SEO and user trust

Without these fixes, the app appears functional but silently fails to update, fetch models, or guide users to correct documentation.

## Scope

### IN SCOPE
**Priority 1: Critical Runtime Fixes**
- NewReleaseNotification.kt: Update GitHub repo constant
- ModelManagerViewModel.kt: Update model allowlist endpoint URL
- Task module docUrls: Fix 5 files with broken "Learn More" links

**Priority 2: User-Facing Documentation**
- Root README.md: Complete rebrand of title, links, Play Store ID
- Android/README.md: Update branding
- GitHub issue/PR templates: Update repository references

**Priority 3: Administrative Documentation**
- docs/planning/bmm-workflow-status.yaml: Update architecture references
- Any remaining doc files with old branding

### OUT OF SCOPE
- Changes to model allowlist JSON files (they correctly reference LiteRT-LM)
- Functionality changes beyond rebrand completion
- New features or refactoring
- Play Store listing updates (manual process outside codebase)

## Acceptance Criteria

### Runtime Functionality
- [ ] App successfully fetches latest release info from new GitHub repository
- [ ] App successfully fetches model allowlist from new GitHub endpoint
- [ ] All in-app "Learn More" links navigate to correct OnDevice AI documentation

### Documentation Completeness
- [ ] README.md shows "OnDevice AI" branding throughout
- [ ] Android/README.md shows "OnDevice AI" branding
- [ ] No references to "google-ai-edge" or "Google AI Edge Gallery" remain in user-facing docs

### Code Quality
- [ ] All changes pass ktlint
- [ ] CI builds successfully
- [ ] No new warnings or errors introduced

### Verification
- [ ] DroidRun verification: App downloads model list successfully
- [ ] DroidRun verification: In-app help links open correct pages
- [ ] Grep verification: No remaining "google-ai-edge" in runtime code

## Technical Approach

### Phase 1: Runtime Code Fixes (TDD)
1. **Test**: Write tests to verify GitHub endpoints resolve correctly
2. **Implement**: Update hardcoded constants and URLs in 7 Kotlin files
3. **Verify**: Run tests + DroidRun to confirm app fetches data successfully

### Phase 2: Documentation Updates
1. Update markdown files (README.md, Android/README.md)
2. Update GitHub templates (.github/)
3. Update docs/ directory files
4. Run comprehensive grep to catch any remaining references

### Phase 3: Validation
1. CI: Verify lint + build passes
2. DroidRun: Test model download flow
3. DroidRun: Test help link navigation
4. Grep: Confirm no "google-ai-edge" remains in critical paths

### Files to Change (10 files total)

**Priority 1 - Runtime Code (7 .kt files):**
- `app/src/main/java/ai/ondevice/app/ui/home/NewReleaseNotification.kt`
- `app/src/main/java/ai/ondevice/app/ui/modelmanager/ModelManagerViewModel.kt`
- `app/src/main/java/ai/ondevice/app/tasks/llmchat/LlmChatTaskModule.kt`
- `app/src/main/java/ai/ondevice/app/tasks/llmsingle/LlmSingleTurnTaskModule.kt`
- `app/src/main/java/ai/ondevice/app/tasks/examples/ExampleCustomTask.kt`
- `app/src/main/java/ai/ondevice/app/tasks/tinygarden/TinyGardenTask.kt`
- `app/src/main/java/ai/ondevice/app/tasks/mobileactions/MobileActionsTask.kt`

**Priority 2 - User Documentation (2 .md files):**
- `README.md`
- `Android/README.md`

**Priority 3 - Admin Documentation (1 .yaml file):**
- `docs/planning/bmm-workflow-status.yaml`

### Update Strategy
- **GitHub Repository**: `google-ai-edge/gallery` → `anthropics/on-device` (or actual new repo name)
- **Package Paths**: `com.google.ai.edge.gallery` → `ai.ondevice.app`
- **Branding**: "Google AI Edge Gallery" → "OnDevice AI"

## References
- Related to: Commit 95a4df7 "feat(rebrand): Complete OnDevice AI rebrand from Google AI Edge Gallery"
- Forensic investigation: Agent abb7b33
- See: LESSONS_LEARNED.md for rebrand patterns
- See: CODE_INDEX.md for file inventory
