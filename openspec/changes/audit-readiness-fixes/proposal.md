# Proposal: audit-readiness-fixes

## Summary
Complete the OnDevice AI rebrand by fixing 5 critical gaps identified in the GoraAI target state gap analysis, achieving 100% audit readiness and legal compliance.

## Motivation
The gap analysis revealed that while the rebrand is 95% complete architecturally, **5 critical issues block audit approval**:

1. **Copyright Attribution**: 154 files still credit "Google LLC" instead of "OnDevice Inc." - this creates IP ownership confusion and audit failures
2. **External References**: 15+ URLs point to Google domains (ai.google.dev, github.com/google-ai-edge), causing broken documentation links and user confusion
3. **Compliance Requirement**: Missing chat disclaimer "OnDevice AI can make mistakes" violates GoraAI/OnDeviceAI-OpenSpec specification (OPENSPEC-SCREENS.md line 54)
4. **Legal Integration**: Privacy policy and Terms of Service exist in GoraAI/ondevice-legal but are not accessible within the app, violating legal compliance requirements
5. **Support Infrastructure**: No visible support contact (support@ondevice.ai) leaves users without escalation path

Without these fixes:
- **Audit will fail** due to incorrect copyright attribution
- **Legal compliance incomplete** (privacy/TOS not accessible)
- **Spec violation** (missing disclaimer)
- **User experience broken** (links to wrong company's documentation)
- **Support requests misdirected** (no contact information)

**Current Audit Score**: 60% → **Target**: 100%

## Scope

### IN SCOPE

**Priority 1: Copyright & Legal Attribution (CRITICAL)**
- Update 154 Kotlin/XML files: "Copyright 2025 Google LLC" → "Copyright 2025 OnDevice Inc."
- Update AndroidManifest.xml, build files, all source headers
- Verify no Google corporate attribution remains in user-facing or source files

**Priority 2: External URL Remediation (CRITICAL)**
- Replace ai.google.dev URLs with ondevice.ai equivalents
- Update github.com/google-ai-edge references to ondevice-ai org
- Fix documentation links in:
  - MobileActionsScreen.kt
  - HomeScreen.kt
  - MobileActionsChallengeDialog.kt
  - LlmChatTaskModule.kt (3 instances)
  - All task modules with external doc links

**Priority 3: Chat Disclaimer (SPEC REQUIREMENT)**
- Create ChatDisclaimerRow composable
- Integrate disclaimer after last AI message in chat
- Display: "OnDevice AI can make mistakes" + color logo (16.dp)
- Per OPENSPEC-SCREENS.md specification

**Priority 4: Legal Document Integration (COMPLIANCE)**
- Integrate privacy.html from GoraAI/ondevice-legal
- Integrate terms.html from GoraAI/ondevice-legal
- Create LegalInformationDialog with WebView or Intent
- Add "Legal Information" entry to SettingsDialog
- Update TosDialog to link to full legal documents
- Ensure GDPR/compliance accessibility requirements met

**Priority 5: Support Contact Integration (USER SUPPORT)**
- Add support@ondevice.ai to:
  - Error dialogs (as contact escalation)
  - Settings dialog (under "Help & Support")
  - TOS/Privacy documents (for inquiries)
- Add privacy@ondevice.ai for privacy-specific inquiries
- Create strings resources for contact emails

### OUT OF SCOPE
- Feature changes beyond gap remediation
- UI redesign or visual changes
- Performance optimizations
- New functionality
- Changes to GoraAI/ondevice-legal documents themselves (use as-is)

## Acceptance Criteria

### Copyright & Attribution
- [ ] All 154 Kotlin files show "Copyright 2025 OnDevice Inc."
- [ ] All XML resource files show correct copyright
- [ ] AndroidManifest.xml shows correct copyright
- [ ] No "Google LLC" appears in any source or resource file
- [ ] Grep verification: No "Google LLC" matches in src/

### External URLs
- [ ] No ai.google.dev URLs remain in code
- [ ] No github.com/google-ai-edge URLs remain in code (except external library references)
- [ ] All documentation links point to ondevice.ai domain
- [ ] Help links in UI navigate to correct OnDevice documentation
- [ ] Grep verification: No inappropriate Google domain references

### Chat Disclaimer
- [ ] Disclaimer visible after last AI message in chat
- [ ] Text displays: "OnDevice AI can make mistakes"
- [ ] Color logo (16.dp) appears next to disclaimer text
- [ ] Disclaimer persists until new message sent
- [ ] Visual verification: Screenshot shows disclaimer correctly

### Legal Integration
- [ ] "Legal Information" menu item in Settings
- [ ] Tapping "Legal Information" opens LegalInformationDialog
- [ ] Dialog shows links to Privacy Policy and Terms of Service
- [ ] Links open privacy.html and terms.html (hosted or embedded)
- [ ] TosDialog first-launch acceptance links to full documents
- [ ] Visual verification: Legal dialogs accessible and functional

### Support Contact
- [ ] support@ondevice.ai visible in Settings → Help
- [ ] Error dialogs reference support@ondevice.ai
- [ ] privacy@ondevice.ai referenced in legal documents
- [ ] All support email strings in strings.xml
- [ ] Grep verification: Support emails present in appropriate places

### Code Quality
- [ ] All changes pass ktlint
- [ ] CI builds successfully
- [ ] No new warnings introduced
- [ ] No broken links or dead references

### Audit Readiness
- [ ] Audit checklist: 100% compliance
- [ ] Legal review: All documents accessible
- [ ] IP ownership: Clear OnDevice Inc. attribution
- [ ] User support: Contact paths established
- [ ] Spec compliance: All OPENSPEC requirements met

## Technical Approach

### Phase 1: Automated Batch Updates (Day 1, 3-4 hours)

**Copyright Header Updates** (154 files)
1. Use sed/awk to batch replace copyright headers
2. Target all .kt files: `find . -name "*.kt" -exec sed -i 's/Copyright 2025 Google LLC/Copyright 2025 OnDevice Inc./g' {} +`
3. Target all .xml files in res/
4. Manual review of AndroidManifest.xml, build files
5. Verify with grep: `grep -r "Google LLC" src/` should return 0 matches

**URL Replacements** (25 files)
1. Create URL mapping table:
   - `ai.google.dev/gemma/docs/mobile-actions` → `ondevice.ai/docs/mobile-actions`
   - `ai.google.dev/edge/mediapipe` → `ondevice.ai/docs/mediapipe`
   - `github.com/google-ai-edge/LiteRT-LM` → Keep (external library)
   - `github.com/google-ai-edge/gallery` → `github.com/on-device-ai-inc/on-device-ai`
2. Use targeted sed replacements for each URL pattern
3. Verify links work (or mark as documentation TODOs)

**Verification**:
- Run grep for all Google domain patterns
- Check CI lint passes
- Git diff review for unintended changes

### Phase 2: Chat Disclaimer Implementation (Day 2, 2-3 hours)

**Create ChatDisclaimerRow.kt** (New file)
```kotlin
@Composable
fun ChatDisclaimerRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color logo (16.dp)
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified // Preserve color
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Disclaimer text
        Text(
            text = stringResource(R.string.chat_disclaimer_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

**Modify ChatPanel.kt**
- Add disclaimer row after last AI message
- Show only after AI completes response
- Position per OPENSPEC-SCREENS.md spec

**Add String Resource**
- `strings.xml`: `<string name="chat_disclaimer_text">OnDevice AI can make mistakes</string>`

**Testing**:
- TDD: Test disclaimer appears after AI message
- Visual: Screenshot verification
- Accessibility: Verify screen reader compatibility

### Phase 3: Legal Document Integration (Days 2-3, 4-5 hours)

**Copy Legal Documents**
1. Copy from /tmp/ondevice-legal/ to Android/src/app/src/main/assets/legal/
2. Files: privacy.html, terms.html
3. Alternative: Host on ondevice.ai website and link via Intent

**Create LegalInformationDialog.kt** (New file)
```kotlin
@Composable
fun LegalInformationDialog(
    onDismiss: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Legal Information") },
        text = {
            Column {
                TextButton(onClick = onPrivacyClick) {
                    Text("Privacy Policy")
                }
                TextButton(onClick = onTermsClick) {
                    Text("Terms of Service")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("For questions: support@ondevice.ai")
                Text("Privacy inquiries: privacy@ondevice.ai")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
```

**Modify SettingsDialog.kt**
- Add "Legal Information" menu item
- Wire up dialog display
- Handle privacy/terms clicks (Intent or WebView)

**WebView Implementation** (if embedding HTML)
- Create LegalWebViewScreen.kt for privacy/terms display
- Load from assets/legal/
- Add navigation routes

**Update TosDialog.kt**
- Add "View full Privacy Policy" link
- Add "View full Terms of Service" link
- Wire to same legal viewing mechanism

**Testing**:
- Functional: All legal links work
- Visual: Dialogs display correctly
- Compliance: Verify accessibility

### Phase 4: Support Contact Integration (Day 3, 1-2 hours)

**String Resources**
```xml
<string name="support_email">support@ondevice.app</string>
<string name="privacy_email">privacy@ondevice.app</string>
<string name="support_contact_text">Need help? Contact us at support@ondevice.app</string>
```

**Modify SettingsDialog.kt**
- Add "Help & Support" section
- Display support email
- Add "Send Email" button (Intent to email client)

**Modify ErrorDialog.kt** (or similar)
- Add support contact to error messages
- "If this problem persists, contact support@ondevice.app"

**Modify Legal Documents** (minor)
- Ensure support/privacy emails appear in HTML documents

**Testing**:
- Verify email intents work
- Check all support text displays correctly

### Phase 5: Comprehensive Verification (Day 4, 2-3 hours)

**Automated Tests**:
- Grep verification for old references
- CI lint and build
- Link validation (where possible)

**Manual Testing**:
- Settings → Legal Information flow
- Chat disclaimer visibility
- Error dialog support contact
- First-launch TOS flow

**Visual Documentation**:
- Screenshots of all new UI elements
- Compliance checklist completion
- Audit readiness report

### Phase 6: CI Loop & Visual Loop
- Standard three-loop verification per CLAUDE.md
- CI must pass
- Visual verification via screenshots
- DroidRun testing (if available)

## Files to Change

**Automated Updates (179 files)**:
- 154 .kt files (copyright headers)
- 25 files with Google URLs

**New Files (2)**:
- `ChatDisclaimerRow.kt`
- `LegalInformationDialog.kt`

**Modified Files (5)**:
- `ChatPanel.kt` (add disclaimer)
- `SettingsDialog.kt` (add legal menu + support)
- `TosDialog.kt` (add legal links)
- `ErrorDialog.kt` (add support contact)
- `strings.xml` (add new strings)

**Asset Files (2)**:
- Copy `privacy.html` to assets/legal/
- Copy `terms.html` to assets/legal/

**Total Files**: ~188 files

## Risk Assessment

### Low Risk
- Copyright header updates (automated, safe)
- String resource additions (non-breaking)

### Medium Risk
- URL replacements (verify new URLs exist or document)
- Legal dialog integration (new UI, needs testing)

### High Risk (Mitigation Required)
- Chat disclaimer (affects core chat UX, needs careful positioning)
  - **Mitigation**: TDD first, visual verification, A/B test if possible

## Dependencies

### External Requirements
- Legal HTML files from GoraAI/ondevice-legal (already available)
- Replacement URLs for ai.google.dev links (need to be created or documented)
- Support email infrastructure (support@ondevice.ai should be active)

### Internal Requirements
- Logo asset (16.dp version) for disclaimer
- Navigation infrastructure for legal WebView (if embedding)

## Timeline Estimate

| Phase | Duration | Completion |
|-------|----------|------------|
| Phase 1: Automated updates | 3-4 hours | Day 1 |
| Phase 2: Chat disclaimer | 2-3 hours | Day 2 |
| Phase 3: Legal integration | 4-5 hours | Days 2-3 |
| Phase 4: Support contact | 1-2 hours | Day 3 |
| Phase 5: Verification | 2-3 hours | Day 4 |
| CI/Visual loops | 2-4 hours | Day 4 |

**Total Estimated Time**: 15-25 hours (2-4 days)

## Success Metrics

- **Audit Score**: 60% → 100%
- **Copyright Compliance**: 0/154 → 154/154 files correct
- **URL Compliance**: 15+ broken → 0 broken
- **Legal Accessibility**: 0% → 100% (all docs accessible)
- **Support Visibility**: Not present → Clearly visible
- **Spec Compliance**: Missing disclaimer → Fully implemented

## References
- Gap Analysis: Agent aede1db (2026-02-16)
- Target Specification: GoraAI/OnDeviceAI-OpenSpec
- Legal Documents: GoraAI/ondevice-legal
- Previous Rebrand: openspec/changes/complete-rebrand-fixes
- See: LESSONS_LEARNED.md for rebrand patterns
- See: CODE_INDEX.md for file inventory
