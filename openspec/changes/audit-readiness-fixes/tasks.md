# Tasks: audit-readiness-fixes

## Phase 1: Automated Batch Updates (Priority 1 - CRITICAL)

- [ ] **Task 1.1**: Update copyright headers in all Kotlin files
  - Target: 154 .kt files
  - Command: `find Android/src/app/src/main/java -name "*.kt" -exec sed -i 's/Copyright 2025 Google LLC/Copyright 2025 OnDevice Inc./g' {} +`
  - Verification: `grep -r "Google LLC" Android/src/app/src/main/java --include="*.kt"`
  - Acceptance: Zero matches of "Google LLC" in Kotlin source files

- [ ] **Task 1.2**: Update copyright headers in XML resource files
  - Target: res/ directory XML files with copyright headers
  - Command: `find Android/src/app/src/main/res -name "*.xml" -exec sed -i 's/Copyright 2025 Google LLC/Copyright 2025 OnDevice Inc./g' {} +`
  - Verification: `grep -r "Google LLC" Android/src/app/src/main/res`
  - Acceptance: Zero matches in resource files

- [ ] **Task 1.3**: Update copyright in AndroidManifest.xml
  - File: `Android/src/app/src/main/AndroidManifest.xml`
  - Manual review: Check if copyright header exists
  - Update if present: "Copyright 2025 Google LLC" → "Copyright 2025 OnDevice Inc."
  - Acceptance: Manifest shows correct copyright

- [ ] **Task 1.4**: Update copyright in build configuration files
  - Files: `build.gradle.kts`, `settings.gradle.kts`
  - Check for copyright headers in comments
  - Update if present
  - Acceptance: Build files show correct attribution

- [ ] **Task 1.5**: Replace ai.google.dev URLs in code
  - Files:
    - `MobileActionsScreen.kt`: `ai.google.dev/gemma/docs/mobile-actions`
    - `HomeScreen.kt`: Check for Google dev URLs
    - `MobileActionsChallengeDialog.kt`: Check for Google dev URLs
  - Replace with: `ondevice.ai/docs/[appropriate-page]`
  - Acceptance: No ai.google.dev URLs in UI code

- [ ] **Task 1.6**: Update github.com/google-ai-edge/LiteRT-LM references
  - Files: `LlmChatTaskModule.kt` (3 instances)
  - Decision: Keep if external library reference, update if internal doc
  - Update internal docs to ondevice.ai equivalents
  - Acceptance: Only legitimate external lib references remain

- [ ] **Task 1.7**: Verification - Grep audit for Google references
  - Command: `grep -r "google-ai-edge\|ai.google.dev\|Google LLC" Android/src/app/src --include="*.kt" --include="*.xml"`
  - Review all matches
  - Ensure remaining matches are acceptable (external lib references)
  - Acceptance: All inappropriate references removed

## Phase 2: Chat Disclaimer Implementation (Priority 3 - SPEC REQUIREMENT)

- [ ] **Task 2.1**: Create ChatDisclaimerRow composable
  - File: Create `Android/src/app/src/main/java/ai/ondevice/app/ui/chat/ChatDisclaimerRow.kt`
  - Implementation:
    - Row layout with horizontal padding 16.dp, vertical 8.dp
    - Color logo Icon (16.dp size, R.drawable.logo, Color.Unspecified tint)
    - Spacer 8.dp
    - Text with string resource, bodySmall typography, onSurfaceVariant color
  - Acceptance: Composable compiles without errors

- [ ] **Task 2.2**: Add disclaimer string resource
  - File: `Android/src/app/src/main/res/values/strings.xml`
  - Add: `<string name="chat_disclaimer_text">OnDevice AI can make mistakes</string>`
  - Acceptance: String resource available

- [ ] **Task 2.3**: Integrate disclaimer into ChatPanel
  - File: Modify `Android/src/app/src/main/java/ai/ondevice/app/ui/llmchat/ChatPanel.kt`
  - Add ChatDisclaimerRow after last AI message in conversation
  - Show only when AI response complete
  - Position according to OPENSPEC-SCREENS.md spec
  - Acceptance: Disclaimer visible after AI messages in chat

- [ ] **Task 2.4**: Verify logo asset exists
  - Check: `R.drawable.logo` exists and is color-capable
  - Verify: 16.dp rendering looks correct
  - Alternative: Use specific disclaimer logo if needed
  - Acceptance: Logo displays correctly at 16.dp

- [ ] **Task 2.5**: Visual verification - Screenshot disclaimer
  - Run app, open chat, send message
  - Take screenshot showing disclaimer after AI response
  - Verify: Text "OnDevice AI can make mistakes" visible
  - Verify: Color logo (16.dp) visible next to text
  - Acceptance: Screenshot matches spec requirements

## Phase 3: Legal Document Integration (Priority 4 - COMPLIANCE)

- [ ] **Task 3.1**: Copy legal HTML files to assets
  - Source: `/tmp/ondevice-legal/privacy.html`, `/tmp/ondevice-legal/terms.html`
  - Destination: `Android/src/app/src/main/assets/legal/`
  - Create directory if needed: `mkdir -p Android/src/app/src/main/assets/legal/`
  - Copy files: `cp /tmp/ondevice-legal/*.html Android/src/app/src/main/assets/legal/`
  - Acceptance: privacy.html and terms.html exist in assets/legal/

- [ ] **Task 3.2**: Create LegalInformationDialog composable
  - File: Create `Android/src/app/src/main/java/ai/ondevice/app/ui/settings/LegalInformationDialog.kt`
  - Implementation:
    - AlertDialog with title "Legal Information"
    - TextButton: "Privacy Policy" (calls onPrivacyClick)
    - TextButton: "Terms of Service" (calls onTermsClick)
    - Display support emails: support@ondevice.ai, privacy@ondevice.ai
    - Close button
  - Acceptance: Dialog composable compiles

- [ ] **Task 3.3**: Add legal information strings
  - File: `Android/src/app/src/main/res/values/strings.xml`
  - Add:
    - `<string name="legal_information">Legal Information</string>`
    - `<string name="privacy_policy">Privacy Policy</string>`
    - `<string name="terms_of_service">Terms of Service</string>`
    - `<string name="support_email">support@ondevice.ai</string>`
    - `<string name="privacy_email">privacy@ondevice.ai</string>`
  - Acceptance: All strings available

- [ ] **Task 3.4**: Create WebView screen for legal documents (Option A)
  - File: Create `Android/src/app/src/main/java/ai/ondevice/app/ui/legal/LegalWebViewScreen.kt`
  - Implementation:
    - Composable with AndroidView(WebView)
    - Load from assets: `file:///android_asset/legal/privacy.html`
    - Or load from: `file:///android_asset/legal/terms.html`
    - Back button navigation
  - Acceptance: WebView displays HTML correctly

  **Alternative - Task 3.4 (Option B): Use Intent to browser**
  - Host legal docs on ondevice.ai website
  - Use Intent.ACTION_VIEW to open URLs
  - URLs: `https://ondevice.ai/privacy.html`, `https://ondevice.ai/terms.html`
  - Simpler implementation, requires web hosting

- [ ] **Task 3.5**: Add "Legal Information" to SettingsDialog
  - File: Modify `Android/src/app/src/main/java/ai/ondevice/app/ui/settings/SettingsDialog.kt`
  - Add menu item: "Legal Information"
  - Wire to show LegalInformationDialog
  - Acceptance: "Legal Information" clickable in Settings

- [ ] **Task 3.6**: Handle legal document viewing
  - From LegalInformationDialog, handle Privacy/Terms clicks
  - Route to WebView screen (Option A) or Intent (Option B)
  - Test both privacy and terms navigation
  - Acceptance: Both documents viewable from app

- [ ] **Task 3.7**: Update TosDialog with legal links
  - File: Modify `Android/src/app/src/main/java/ai/ondevice/app/ui/home/TosDialog.kt`
  - Add text: "By continuing, you agree to our [Terms of Service] and [Privacy Policy]"
  - Make Terms/Privacy clickable (ClickableText or similar)
  - Link to legal viewing mechanism
  - Acceptance: First-launch TOS links to full documents

## Phase 4: Support Contact Integration (Priority 5 - USER SUPPORT)

- [ ] **Task 4.1**: Add support contact strings
  - File: `Android/src/app/src/main/res/values/strings.xml`
  - Add: `<string name="support_contact_text">Need help? Contact us at support@ondevice.ai</string>`
  - Add: `<string name="error_support_text">If this problem persists, contact support@ondevice.ai</string>`
  - Acceptance: Support strings available

- [ ] **Task 4.2**: Add Help & Support section to Settings
  - File: Modify `Android/src/app/src/main/java/ai/ondevice/app/ui/settings/SettingsDialog.kt`
  - Add "Help & Support" section
  - Display support@ondevice.ai
  - Add "Send Email" button with Intent (ACTION_SENDTO, mailto: URI)
  - Acceptance: Support section visible and email intent works

- [ ] **Task 4.3**: Add support contact to error dialogs
  - Files: Check `ErrorDialog.kt` or similar error UI components
  - Add support contact text to error messages
  - "If this problem persists, contact support@ondevice.ai"
  - Acceptance: Error dialogs show support contact

- [ ] **Task 4.4**: Update legal HTML with support emails
  - Files: `Android/src/app/src/main/assets/legal/privacy.html`, `terms.html`
  - Verify support@ondevice.ai and privacy@ondevice.ai appear in documents
  - If not, add contact section to HTML files
  - Acceptance: Legal docs show support contacts

## Testing Tasks

- [ ] **Test 1**: Copyright verification
  - Run: `grep -r "Google LLC" Android/src/app/src`
  - Expected: Zero matches (or only in test fixtures)
  - Acceptance: All copyrights updated

- [ ] **Test 2**: URL verification
  - Run: `grep -r "ai.google.dev\|google-ai-edge/gallery" Android/src/app/src --include="*.kt"`
  - Expected: Zero inappropriate matches
  - Acceptance: Only valid external library references remain

- [ ] **Test 3**: Chat disclaimer functional test
  - Launch app, open chat, send message
  - Wait for AI response
  - Verify disclaimer appears below last AI message
  - Acceptance: Disclaimer visible and correctly positioned

- [ ] **Test 4**: Legal document navigation test
  - Open Settings → Legal Information
  - Tap "Privacy Policy" → Verify privacy.html displays
  - Back → Tap "Terms of Service" → Verify terms.html displays
  - Acceptance: Both documents accessible and readable

- [ ] **Test 5**: Support email intent test
  - Open Settings → Help & Support → Tap "Send Email"
  - Verify email app opens with to: support@ondevice.ai
  - Acceptance: Email intent works correctly

- [ ] **Test 6**: First-launch TOS flow test
  - Clear app data, launch app
  - Verify TOS dialog appears
  - Tap Terms/Privacy links
  - Verify full documents accessible
  - Accept TOS → Verify app continues
  - Acceptance: First-launch flow complete with legal access

## CI/Build Tasks

- [ ] **Build 1**: Verify ktlint passes
  - Command: CI runs ktlint automatically
  - Acceptance: No new linting errors

- [ ] **Build 2**: Verify CI build succeeds
  - Push code, monitor GitHub Actions
  - Acceptance: CI status GREEN

- [ ] **Build 3**: Download and verify APK
  - Download APK from CI artifacts
  - Install on device: `adb install -r app-debug.apk`
  - Acceptance: APK installs successfully

- [ ] **Build 4**: Manual smoke test on device
  - Launch app
  - Verify no crashes
  - Test chat disclaimer
  - Test legal information access
  - Test support email
  - Acceptance: All critical features work

## Visual Verification Tasks

- [ ] **Visual 1**: Chat disclaimer screenshot
  - Path: `visual-evidence/chat-disclaimer.png`
  - Content: Chat screen with disclaimer visible
  - Acceptance: Screenshot shows disclaimer after AI message

- [ ] **Visual 2**: Legal Information dialog screenshot
  - Path: `visual-evidence/legal-dialog.png`
  - Content: Settings → Legal Information dialog
  - Acceptance: Dialog shows Privacy/Terms options

- [ ] **Visual 3**: Privacy policy WebView screenshot
  - Path: `visual-evidence/privacy-webview.png`
  - Content: Privacy policy displayed in app
  - Acceptance: HTML renders correctly

- [ ] **Visual 4**: Support section screenshot
  - Path: `visual-evidence/support-section.png`
  - Content: Settings showing Help & Support
  - Acceptance: Support email visible

## Documentation Tasks

- [ ] **Doc 1**: Update LESSONS_LEARNED.md
  - Add lesson: "Audit Readiness Checklist for Rebrand"
  - Document: Copyright updates, legal integration, support contact patterns
  - Include: Automation commands used (sed patterns)
  - Acceptance: Lesson captured with examples

- [ ] **Doc 2**: Create audit readiness report
  - File: `openspec/changes/audit-readiness-fixes/audit-report.md`
  - Content: Before/after comparison, compliance checklist, evidence
  - Include: Screenshots, grep results, CI status
  - Acceptance: Complete audit trail documented

- [ ] **Doc 3**: Update CODE_INDEX.md if new files added
  - Add: ChatDisclaimerRow.kt
  - Add: LegalInformationDialog.kt
  - Add: LegalWebViewScreen.kt (if created)
  - Acceptance: Index reflects new structure

## Completion Checklist

- [ ] All Phase 1 tasks completed (automated updates)
- [ ] All Phase 2 tasks completed (chat disclaimer)
- [ ] All Phase 3 tasks completed (legal integration)
- [ ] All Phase 4 tasks completed (support contact)
- [ ] All tests pass (TDD evidence collected)
- [ ] CI green (CI evidence collected)
- [ ] Visual verification complete (screenshots captured)
- [ ] Audit score: 100%
- [ ] LESSONS_LEARNED.md updated
- [ ] Ready for `/openspec-archive`

## Verification Commands

### Copyright Check
```bash
# Should return 0 matches
grep -r "Google LLC" Android/src/app/src --include="*.kt" --include="*.xml" | wc -l
```

### URL Check
```bash
# Should return 0 inappropriate matches
grep -r "ai.google.dev\|google-ai-edge/gallery" Android/src/app/src --include="*.kt" | wc -l
```

### Legal Assets Check
```bash
# Should list privacy.html and terms.html
ls -la Android/src/app/src/main/assets/legal/
```

### Build Check
```bash
# Should succeed
cd Android/src && ./gradlew lint
```
