# Spec Delta: audit-readiness-fixes

## ADDED

### audit-readiness-checklist.md

```markdown
# Audit Readiness Checklist for Rebrand

## Overview
This specification defines the comprehensive checklist for ensuring a rebranded application meets 100% audit compliance, particularly for IP ownership verification and legal requirements.

## Scope
When completing a rebrand for audit purposes, this checklist ensures all corporate attribution, legal compliance, and support infrastructure requirements are met.

## Audit Compliance Requirements

### 1. Copyright Attribution (CRITICAL)
- [ ] **Source File Headers**: All source code files (.kt, .java, .xml) must show correct corporate copyright
  - Pattern: `Copyright [YEAR] [COMPANY_NAME]`
  - Example: `Copyright 2025 OnDevice Inc.`
  - Files: ALL source files in src/ directory
  - Verification: `grep -r "[OLD_COMPANY]" src/` returns 0 matches

- [ ] **Build Configuration Files**: build.gradle.kts, settings.gradle.kts copyright (if present)
- [ ] **Manifest Files**: AndroidManifest.xml copyright attribution
- [ ] **Resource Files**: XML resources with copyright headers
- [ ] **Documentation**: README, LICENSE files show correct company

**Automation**:
```bash
# Batch update all Kotlin files
find . -name "*.kt" -exec sed -i 's/Copyright 2025 Google LLC/Copyright 2025 OnDevice Inc./g' {} +

# Batch update all XML files
find . -name "*.xml" -exec sed -i 's/Copyright 2025 Google LLC/Copyright 2025 OnDevice Inc./g' {} +
```

### 2. External References (CRITICAL)
- [ ] **Documentation URLs**: No links to previous company's documentation
  - Replace: company-specific domain URLs
  - Update: GitHub repository references
  - Verify: All help/doc links functional

- [ ] **API Endpoints**: No calls to previous company's services (unless intentional)
  - Review: All network calls
  - Update: Service endpoints
  - Document: Any remaining legitimate external dependencies

- [ ] **Library References**: Distinguish external libraries (keep) vs internal docs (update)
  - Keep: `github.com/google-ai-edge/LiteRT-LM` (external library)
  - Update: `github.com/google-ai-edge/gallery` (internal documentation)

**Verification**:
```bash
# Find all Google domain references
grep -r "google.com\|googleapis.com\|ai.google.dev" src/ --include="*.kt"

# Review each match for legitimacy
```

### 3. Legal Compliance (CRITICAL)
- [ ] **Privacy Policy**: Accessible within app
  - Method: WebView, Intent to browser, or in-app dialog
  - Location: Settings → Legal Information → Privacy Policy
  - Verification: User can read full policy without leaving app context

- [ ] **Terms of Service**: Accessible within app
  - Method: WebView, Intent to browser, or in-app dialog
  - Location: Settings → Legal Information → Terms of Service
  - First-launch: TOS acceptance flow with link to full document

- [ ] **First-Launch Acceptance**: User must accept TOS/Privacy on first launch
  - Dialog: Welcome/TOS dialog
  - Content: "By continuing, you agree to our Terms of Service and Privacy Policy"
  - Links: Clickable access to full documents
  - Verification: App cannot proceed without acceptance

- [ ] **Legal Contact Information**: Privacy/support emails visible
  - Privacy inquiries: privacy@[company].com
  - General support: support@[company].com
  - Location: Legal documents, Settings, Error messages

**Integration Methods**:

**Option A: Asset-based (Offline)**
```kotlin
// Copy HTML to assets/legal/
// Display with WebView
AndroidView(factory = { context ->
    WebView(context).apply {
        loadUrl("file:///android_asset/legal/privacy.html")
    }
})
```

**Option B: Web-hosted (Online)**
```kotlin
// Host on company website
// Use Intent to open browser
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://company.com/privacy"))
context.startActivity(intent)
```

### 4. Support Infrastructure (HIGH PRIORITY)
- [ ] **Support Email**: Visible and functional
  - Email: support@[company].com
  - Location: Settings → Help & Support
  - Functionality: Tap to compose email (Intent)

- [ ] **Privacy Email**: For GDPR/privacy requests
  - Email: privacy@[company].com
  - Location: Legal documents, Privacy Policy

- [ ] **Error Support**: Contact path for error resolution
  - Error dialogs: "Contact support@[company].com"
  - Network errors: "Check connection or contact support"

- [ ] **Email Intent**: Support email tap opens email app
  ```kotlin
  val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:support@company.com")
      putExtra(Intent.EXTRA_SUBJECT, "App Support Request")
  }
  context.startActivity(intent)
  ```

### 5. Specification Compliance (SPEC REQUIREMENTS)
- [ ] **Disclaimer Text**: Per specification requirements
  - Example: "OnDevice AI can make mistakes" (chat disclaimer)
  - Location: After AI-generated responses
  - Format: Text + logo (per spec dimensions)

- [ ] **Branding Assets**: All visual elements match spec
  - Logos: Correct company logos
  - Colors: Brand color palette
  - Typography: Brand fonts/styles

- [ ] **Screen Requirements**: All screens per specification
  - Check: OPENSPEC-SCREENS.md requirements
  - Verify: All required UI elements present

### 6. Verification Steps
- [ ] **Grep Audit**: Search for old company references
  ```bash
  grep -ri "[OLD_COMPANY]" . --exclude-dir={.git,build,node_modules}
  ```

- [ ] **Link Testing**: Verify all URLs resolve correctly
  - Manual: Click all help/doc links
  - Automated: URL validation script (if available)

- [ ] **Legal Access Test**: User can reach all legal documents
  - Manual: Navigate to each legal document
  - Verify: Documents display correctly

- [ ] **Email Intent Test**: Support emails open email app
  - Manual: Tap support email links
  - Verify: Email app opens with correct recipient

- [ ] **Build Test**: CI passes all checks
  - Lint: No new errors
  - Tests: All tests pass
  - Build: APK builds successfully

## Evidence Requirements

For audit purposes, collect:

- [ ] **Copyright Evidence**: Git diff showing header updates
- [ ] **URL Evidence**: List of old vs new URLs
- [ ] **Legal Evidence**: Screenshots of legal document access
- [ ] **Support Evidence**: Screenshot of support contact info
- [ ] **CI Evidence**: Green CI build logs
- [ ] **Visual Evidence**: Screenshots of all UI changes

## Audit Score Calculation

| Category | Weight | Score | Points |
|----------|--------|-------|--------|
| Copyright Attribution | 25% | 0-100% | [weight × score] |
| External References | 20% | 0-100% | [weight × score] |
| Legal Compliance | 25% | 0-100% | [weight × score] |
| Support Infrastructure | 15% | 0-100% | [weight × score] |
| Spec Compliance | 15% | 0-100% | [weight × score] |

**Pass Threshold**: 90% overall score
**Target**: 100% compliance

## Common Pitfalls

1. **Missed Copyright Headers**: Build files, test files, XML resources often overlooked
2. **Hardcoded URLs**: URLs in string concatenation or computed at runtime
3. **External Library Confusion**: Incorrectly updating legitimate external library references
4. **Legal Hosting**: Forgetting to actually host legal documents if using web-hosted method
5. **Email Setup**: Support emails not actually configured/monitored
6. **First-Launch Flow**: TOS acceptance without link to full documents

## Automation Scripts

### Copyright Update Script
```bash
#!/bin/bash
# update-copyrights.sh

OLD_COPYRIGHT="Copyright 2025 Google LLC"
NEW_COPYRIGHT="Copyright 2025 OnDevice Inc."

find . -name "*.kt" -o -name "*.xml" | while read file; do
    sed -i "s/$OLD_COPYRIGHT/$NEW_COPYRIGHT/g" "$file"
done

echo "Copyright headers updated. Verifying..."
grep -r "$OLD_COPYRIGHT" . --include="*.kt" --include="*.xml" | wc -l
echo "Remaining old copyrights (should be 0)"
```

### URL Verification Script
```bash
#!/bin/bash
# verify-urls.sh

OLD_DOMAINS=("ai.google.dev" "google-ai-edge/gallery")
FOUND=0

for domain in "${OLD_DOMAINS[@]}"; do
    count=$(grep -r "$domain" src/ --include="*.kt" | wc -l)
    if [ $count -gt 0 ]; then
        echo "Found $count references to $domain"
        grep -r "$domain" src/ --include="*.kt"
        FOUND=$((FOUND + count))
    fi
done

if [ $FOUND -eq 0 ]; then
    echo "✓ No old domain references found"
    exit 0
else
    echo "✗ Found $FOUND old references"
    exit 1
fi
```

## Post-Audit Maintenance

After passing initial audit:

- [ ] **Update on changes**: Any new code files must have correct copyright
- [ ] **Review external links**: Periodically verify documentation links work
- [ ] **Legal updates**: Keep privacy/terms documents current
- [ ] **Support monitoring**: Ensure support emails are monitored
- [ ] **Version tracking**: Document audit completion in version history

## References
- Based on: OnDevice AI audit readiness gap analysis (2026-02-16)
- Agent: aede1db (gap analysis)
- Related: complete-rebrand-fixes (initial rebrand)
- Compliance: GDPR, CCPA, app store requirements
```

---

## MODIFIED

### openspec/specs/v1.1.9/OPENSPEC-SCREENS.md

```diff
 ## Chat Screen

 ### Layout Structure
 ```
 ChatScreen
 ├── TopBar
 │   ├── Back button
 │   ├── Title: "Chat"
 │   └── Clear conversation button
 ├── Message list (LazyColumn)
 │   ├── User message bubbles (right-aligned)
 │   └── AI message bubbles (left-aligned)
+│       └── After last AI message:
+│           └── Disclaimer row
+│               ├── Color logo (16.dp)
+│               └── Text: "OnDevice AI can make mistakes"
 └── BottomBar
     ├── Text input field
     └── Send button
 ```

+### Chat Disclaimer
+
+**Requirement**: Display disclaimer after AI-generated responses
+
+**Position**: After the last AI message in the conversation
+
+**Content**:
+- Icon: App logo in color (16.dp size)
+- Text: "OnDevice AI can make mistakes"
+- Typography: bodySmall
+- Color: onSurfaceVariant
+
+**Behavior**:
+- Appears after AI completes response
+- Remains visible until new message sent
+- Scrolls with conversation
+
+**Implementation**: ChatDisclaimerRow.kt composable
```

### openspec/specs/v1.1.9/OPENSPEC-FOUNDATION.md

```diff
 # Legal & Compliance

+## Corporate Attribution
+
+**Copyright**: All source files must display:
+```
+Copyright 2025 OnDevice Inc.
+```
+
+**License**: Apache 2.0 (unchanged from upstream)
+
 ## Privacy & Terms

 **Privacy Policy**: Accessible via Settings → Legal Information
+- Source: assets/legal/privacy.html OR https://ondevice.ai/privacy
 - Content: Data collection, usage, retention policies
 - Compliance: GDPR, CCPA, app store requirements
+- Contact: privacy@ondevice.ai

 **Terms of Service**: Accessible via Settings → Legal Information
+- Source: assets/legal/terms.html OR https://ondevice.ai/terms
 - Content: Usage terms, limitations, disclaimers
 - First-launch: User must accept before using app
+- Contact: support@ondevice.ai
+
+## Support Infrastructure
+
+**Support Email**: support@ondevice.ai
+- Visible: Settings → Help & Support
+- Function: Tap to compose email (Intent)
+- Context: General inquiries, bug reports
+
+**Privacy Email**: privacy@ondevice.ai
+- Visible: Legal documents, Privacy Policy
+- Function: GDPR requests, privacy inquiries
+- Context: Data deletion, privacy concerns
```

### openspec/specs/v1.1.9/OPENSPEC-NFR.md

```diff
 # Legal & Compliance

+## Copyright Attribution
+
+**Requirement**: All source code must display correct corporate copyright
+
+**Pattern**: `Copyright [YEAR] [COMPANY_NAME]`
+
+**Scope**:
+- All Kotlin source files
+- All XML resource files
+- AndroidManifest.xml
+- Build configuration files (if applicable)
+
+**Verification**: `grep -r "[OLD_COMPANY]" src/` returns 0 matches
+
 ## Privacy Compliance

 **GDPR**: User must be able to:
 - View privacy policy
 - Request data deletion
 - Export personal data
+- Contact privacy officer: privacy@ondevice.ai

 **CCPA**: California residents must have:
 - Notice of data collection
 - Right to opt-out
 - Access to collected data

+**Accessibility**: Legal documents must be:
+- Readable within app (WebView or browser Intent)
+- Navigable from Settings menu
+- Linked in first-launch TOS dialog
+
 ## App Store Compliance

 **Google Play**:
 - Privacy policy URL required in listing
 - Data safety form completed
 - Target API level current
+
+**Legal Document Hosting**:
+- Option A: Embed in assets/legal/ (offline access)
+- Option B: Host on company website (online access)
+- Requirement: Documents must be accessible regardless of method
```

---

## REMOVED

None. All existing specifications remain valid. This change adds audit compliance specifications and updates legal/support requirements in existing specs.

---

## Validation

After applying this spec delta:

1. **Copyright Check**:
   ```bash
   grep -r "Google LLC" src/ | wc -l  # Should return 0
   ```

2. **Legal Access Check**:
   - Manual: Settings → Legal Information → Privacy Policy (accessible)
   - Manual: Settings → Legal Information → Terms of Service (accessible)

3. **Support Contact Check**:
   - Manual: Settings → Help & Support (visible)
   - Manual: Tap support email (opens email app)

4. **Disclaimer Check**:
   - Manual: Send chat message (disclaimer appears after AI response)

5. **Spec Compliance Check**:
   - Review OPENSPEC-SCREENS.md requirements (all implemented)
   - Review OPENSPEC-FOUNDATION.md requirements (all met)

---

## Implementation Notes

- The audit-readiness-checklist.md will be added to `openspec/specs/patterns/` directory (create if needed)
- OPENSPEC-SCREENS.md, OPENSPEC-FOUNDATION.md, OPENSPEC-NFR.md changes document the audit requirements and legal infrastructure
- This spec delta will be merged during `/openspec-archive` after all tasks complete
- Legal HTML files (privacy.html, terms.html) will be copied from GoraAI/ondevice-legal repository

---

## Traceability

| Spec File | Change Type | Reason |
|-----------|------------|--------|
| audit-readiness-checklist.md | NEW | Document audit compliance process |
| OPENSPEC-SCREENS.md | MODIFIED | Add chat disclaimer requirement |
| OPENSPEC-FOUNDATION.md | MODIFIED | Add copyright, legal, support requirements |
| OPENSPEC-NFR.md | MODIFIED | Add compliance verification requirements |

All changes support the audit readiness gap remediation identified in gap analysis (agent aede1db, 2026-02-16).
