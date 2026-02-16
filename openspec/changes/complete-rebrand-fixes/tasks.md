# Tasks: complete-rebrand-fixes

## Phase 1: Runtime Code Fixes (Priority 1 - CRITICAL)

- [ ] **Task 1.1**: Fix NewReleaseNotification GitHub repo constant
  - File: `Android/src/app/src/main/java/ai/ondevice/app/ui/home/NewReleaseNotification.kt`
  - Change: Line 56 `private const val REPO = "google-ai-edge/gallery"` → new repo name
  - Acceptance: App fetches latest release info successfully (test via DroidRun)

- [ ] **Task 1.2**: Fix ModelManagerViewModel allowlist endpoint
  - File: `Android/src/app/src/main/java/ai/ondevice/app/ui/modelmanager/ModelManagerViewModel.kt`
  - Change: Update GitHub raw URL from `google-ai-edge/gallery` → new repo
  - Acceptance: App fetches model allowlist JSON successfully (test via DroidRun)

- [ ] **Task 1.3**: Fix LlmChatTaskModule docUrl
  - File: `Android/src/app/src/main/java/ai/ondevice/app/tasks/llmchat/LlmChatTaskModule.kt`
  - Change: Update all GitHub URLs in docUrl and documentation strings
  - Acceptance: "Learn More" link opens correct documentation page

- [ ] **Task 1.4**: Fix LlmSingleTurnTaskModule docUrl
  - File: `Android/src/app/src/main/java/ai/ondevice/app/tasks/llmsingle/LlmSingleTurnTaskModule.kt`
  - Change: Update GitHub URLs in docUrl
  - Acceptance: "Learn More" link opens correct documentation page

- [ ] **Task 1.5**: Fix ExampleCustomTask docUrl
  - File: `Android/src/app/src/main/java/ai/ondevice/app/tasks/examples/ExampleCustomTask.kt`
  - Change: Update GitHub URLs and package paths in comments
  - Acceptance: Documentation references point to correct repo and package

- [ ] **Task 1.6**: Fix TinyGardenTask docUrl
  - File: `Android/src/app/src/main/java/ai/ondevice/app/tasks/tinygarden/TinyGardenTask.kt`
  - Change: Update GitHub URLs in docUrl
  - Acceptance: "Learn More" link opens correct documentation page

- [ ] **Task 1.7**: Fix MobileActionsTask docUrl
  - File: `Android/src/app/src/main/java/ai/ondevice/app/tasks/mobileactions/MobileActionsTask.kt`
  - Change: Update GitHub URLs in docUrl
  - Acceptance: "Learn More" link opens correct documentation page

## Phase 2: User-Facing Documentation (Priority 2)

- [ ] **Task 2.1**: Update root README.md
  - File: `README.md`
  - Changes:
    - Title: "Google AI Edge Gallery" → "OnDevice AI"
    - Package ID: `com.google.ai.edge.gallery` → `ai.ondevice.app`
    - Repository URLs: `google-ai-edge/gallery` → new repo name
    - Wiki links: Update GitHub wiki references
    - Play Store link: Update package ID in installation instructions
  - Acceptance: README displays OnDevice AI branding, all links work

- [ ] **Task 2.2**: Update Android/README.md
  - File: `Android/README.md`
  - Changes:
    - Title: "Google AI Edge Gallery (Android)" → "OnDevice AI (Android)"
    - All branding references updated
  - Acceptance: Android README consistent with root README

- [ ] **Task 2.3**: Update GitHub templates (if they exist)
  - Files: `.github/ISSUE_TEMPLATE/*`, `.github/PULL_REQUEST_TEMPLATE.md`
  - Changes: Update repository references
  - Acceptance: Templates reference correct repository

## Phase 3: Administrative Documentation (Priority 3)

- [ ] **Task 3.1**: Update bmm-workflow-status.yaml
  - File: `docs/planning/bmm-workflow-status.yaml`
  - Change: Update "AI Edge Gallery" references in architecture section
  - Acceptance: Technical documentation consistent

- [ ] **Task 3.2**: Scan and update remaining doc files
  - Files: Check `docs/` directory for remaining old references
  - Change: Update any remaining "Google AI Edge Gallery" branding
  - Acceptance: No old branding in docs/

## Testing Tasks

- [ ] **Test 1**: Unit test for GitHub endpoint resolution
  - Create test to verify NewReleaseNotification resolves correct repo
  - Verify test FAILS before fix, PASSES after fix

- [ ] **Test 2**: Unit test for model allowlist URL
  - Create test to verify ModelManagerViewModel constructs correct URL
  - Verify test FAILS before fix, PASSES after fix

- [ ] **Test 3**: DroidRun - Model download flow
  - Command: `droid "Open ai.ondevice.app, navigate to model manager, verify models load, then stop"`
  - Acceptance: Models list loads without errors

- [ ] **Test 4**: DroidRun - Help link navigation
  - Command: `droid "Open ai.ondevice.app, tap 'Learn More' on LLM Chat task, verify page loads, then stop"`
  - Acceptance: Help page opens to correct OnDevice AI documentation

- [ ] **Test 5**: Grep verification - no old references remain
  - Command: `grep -r "google-ai-edge" Android/src/app/src/ --include="*.kt"`
  - Acceptance: No matches in runtime code (only test/comment references acceptable)

## CI/Build Tasks

- [ ] **Build 1**: Verify ktlint passes
  - Command: CI runs ktlint automatically on push
  - Acceptance: No linting errors

- [ ] **Build 2**: Verify CI build succeeds
  - Command: Push code, wait for GitHub Actions
  - Acceptance: CI status GREEN

- [ ] **Build 3**: Download and install APK
  - Command: `gh run download <id> -n app-debug && adb install -r app-debug.apk`
  - Acceptance: APK installs successfully

## Documentation Tasks

- [ ] **Doc 1**: Update LESSONS_LEARNED.md
  - Add lesson: "Rebrand Completion Checklist"
  - Document: Runtime endpoints, documentation links, grep patterns
  - Acceptance: Lesson captured for future rebrand projects

- [ ] **Doc 2**: Archive forensic investigation
  - Save forensic report to: `openspec/changes/complete-rebrand-fixes/forensic-report.md`
  - Reference: Agent abb7b33 findings
  - Acceptance: Investigation findings preserved for reference

## Completion Checklist

- [ ] All Phase 1 tasks completed (runtime fixes)
- [ ] All Phase 2 tasks completed (user docs)
- [ ] All Phase 3 tasks completed (admin docs)
- [ ] All tests pass (TDD evidence collected)
- [ ] CI green (CI evidence collected)
- [ ] Visual verification complete (DroidRun evidence collected)
- [ ] LESSONS_LEARNED.md updated
- [ ] Ready for `/openspec-archive`
