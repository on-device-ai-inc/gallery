# Tasks: Fix Light Mode UI Polish Issues

## Phase 1: Investigation

- [ ] **Task 1.1**: Locate ToS dialog and test in light mode
  - Open app in light mode
  - Trigger ToS dialog
  - Screenshot and document transparency issue
  - Acceptance: Clear understanding of contrast problem

- [ ] **Task 1.2**: Find all dialogs and overlays to audit
  - Search for `AlertDialog`, `Dialog`, `ModalBottomSheet`
  - List all files that need light mode check
  - Acceptance: Complete list of UI elements to fix

- [ ] **Task 1.3**: Locate model selector icon borders
  - Open model selector screen
  - Screenshot icon with borders
  - Find code responsible for border
  - Acceptance: Located border styling in code

- [ ] **Task 1.4**: Find rotating logo in model picker during download
  - Select model from top center picker
  - Start download
  - Observe rotating logo
  - Find code responsible
  - Acceptance: Located RotationalLoader in model picker code

## Phase 2: Implementation

- [ ] **Task 2.1**: Fix ToS dialog light mode contrast
  - Add `containerColor = MaterialTheme.colorScheme.surfaceContainerHighest`
  - Test in light mode
  - Ensure dark mode not affected
  - Acceptance: ToS dialog fully legible in light mode

- [ ] **Task 2.2**: Audit and fix all other dialogs
  - Check ConfigDialog
  - Check ConfirmDeleteModelDialog
  - Check any ModalBottomSheet
  - Fix any with poor light mode contrast
  - Acceptance: All dialogs legible in light mode

- [ ] **Task 2.3**: Remove model selector icon borders
  - Find border modifier or styling
  - Remove border
  - Ensure icon matches empty chat logo style
  - Acceptance: Icons clean and borderless

- [ ] **Task 2.4**: Remove rotating logo from model picker
  - Locate RotationalLoader in model picker
  - Comment out or remove
  - Verify model picker still functional
  - Acceptance: No rotating logo during download in picker

## Phase 3: Testing

- [ ] **Task 3.1**: Visual verification in light mode
  - Switch to light mode: `adb shell cmd uimode night no`
  - Test ToS dialog - verify legibility
  - Test all other dialogs - verify legibility
  - Test model selector - verify no borders on icons
  - Test model picker download - verify no rotating logo
  - Screenshot all for evidence
  - Acceptance: All issues fixed in light mode

- [ ] **Task 3.2**: Regression check in dark mode
  - Switch to dark mode: `adb shell cmd uimode night yes`
  - Test ToS dialog - verify still looks good
  - Test all other dialogs - verify no regressions
  - Test model selector - verify icons still look good
  - Test model picker download - verify no rotating logo
  - Acceptance: No regressions in dark mode

- [ ] **Task 3.3**: Edge case testing
  - Test with different theme settings
  - Test model initialization status chip (should KEEP rotating loader)
  - Test other download contexts (should keep appropriate indicators)
  - Acceptance: Only model picker download icon removed, others intact

## Phase 4: CI & Deployment

- [ ] **Task 4.1**: Commit and push
  - Commit: "fix(ui): Light mode dialog visibility and polish"
  - Push to feature branch
  - Acceptance: Code pushed

- [ ] **Task 4.2**: CI validation
  - Wait for CI green
  - Fix any lint/test issues
  - Acceptance: CI passes

- [ ] **Task 4.3**: APK verification on device
  - Download APK from CI
  - Install: `adb install -r app-debug.apk`
  - Test all fixes in both light and dark modes
  - Acceptance: All issues fixed, no regressions

## Phase 5: Documentation

- [ ] **Task 5.1**: Update LESSONS_LEARNED.md
  - Document light mode dialog contrast requirements
  - Document icon border removal technique
  - Document when to show/hide loading indicators
  - Acceptance: Lessons documented

- [ ] **Task 5.2**: Capture evidence
  - Before/after screenshots for all three issues
  - Both light and dark mode screenshots
  - Acceptance: Evidence saved to openspec/changes/fix-light-mode-ui-polish/

## Definition of Done

- All 17 tasks complete
- ToS dialog legible in light mode
- All dialogs checked and fixed for light mode
- Model selector icons have no borders
- Model picker shows no rotating logo during download
- No regressions in dark mode
- CI green
- Physical device tested
- Screenshots captured
- LESSONS_LEARNED.md updated
