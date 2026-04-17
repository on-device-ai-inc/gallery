# Dependency Audit

**Purpose**: Ensure all dependencies are secure, up-to-date, and license-compatible

**Last Updated**: 2026-04-17

---

## Section 1: Dependency Vulnerability Scan

### Purpose
Identify known security vulnerabilities in dependencies before ship.

### Tools

#### OWASP Dependency-Check (Android)

**Setup**:
```kotlin
// Add to Android/src/app/build.gradle.kts
plugins {
    id("org.owasp.dependencycheck") version "8.4.0"
}

dependencyCheck {
    failBuildOnCVSS = 7.0f  // Fail on HIGH or CRITICAL
    suppressionFile = "config/owasp-suppressions.xml"
}
```

**Run**:
```bash
cd Android/src
./gradlew dependencyCheckAnalyze

# View report
open app/build/reports/dependency-check-report.html
```

#### npm audit (Website)

**Run**:
```bash
cd website
npm audit

# Auto-fix non-breaking updates
npm audit fix

# See detailed report
npm audit --json > audit-report.json
```

### Severity Classification

| CVSS Score | Severity | Action Required |
|------------|----------|-----------------|
| 9.0 - 10.0 | CRITICAL | MUST fix before ship |
| 7.0 - 8.9  | HIGH     | MUST fix before ship |
| 4.0 - 6.9  | MEDIUM   | Should fix, can defer if mitigated |
| 0.1 - 3.9  | LOW      | Track, fix in next release |

### Acceptable Risk Threshold

**Ship Criteria**:
- 0 CRITICAL vulnerabilities
- 0 HIGH vulnerabilities
- <5 MEDIUM vulnerabilities (with documented mitigation or suppression rationale)

### Remediation Workflow

#### Step 1: Identify Vulnerability
```bash
# Example output
CVE-2023-12345: Prototype Pollution in lodash < 4.17.21
Severity: HIGH (7.5)
Affected: lodash@4.17.15
```

#### Step 2: Check for Fixed Version
```bash
# npm
npm info lodash versions

# gradle
./gradlew dependencyUpdates
```

#### Step 3: Update Dependency
```bash
# npm
npm install lodash@4.17.21

# gradle (in build.gradle.kts)
implementation("com.example:library:1.2.3")  # Update version
```

#### Step 4: Test After Update
```bash
# Run full test suite
./gradlew test
npm test

# Run E2E tests
maestro test .maestro/flows/
```

#### Step 5: Suppress if Not Exploitable
If vulnerability doesn't affect your usage (e.g., unused code path):

**Create suppression file** (`config/owasp-suppressions.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <suppress>
        <notes>
            CVE-2023-12345: lodash prototype pollution
            Not exploitable - we don't use lodash's merge() function
            Will update in next major version
        </notes>
        <cve>CVE-2023-12345</cve>
    </suppress>
</suppressions>
```

**Document in SECURITY.md**:
```markdown
## Known Suppressions

- CVE-2023-12345 (lodash): Not exploitable in our usage (no merge() calls)
- Planned fix: v2.0.0 (major version update)
```

### CI Integration

**Add to `.github/workflows/security.yml`**:
```yaml
name: Security Scan

on:
  pull_request:
  push:
    branches: [main]
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday

jobs:
  android-security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: OWASP Dependency Check
        run: |
          cd Android/src
          ./gradlew dependencyCheckAnalyze
      - name: Fail on vulnerabilities
        run: |
          if grep -q "HIGH\|CRITICAL" app/build/reports/dependency-check-report.html; then
            echo "HIGH or CRITICAL vulnerabilities found"
            exit 1
          fi

  website-security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: npm audit
        run: |
          cd website
          npm audit --audit-level=high
```

---

## Section 2: Outdated Dependencies Check

### Purpose
Keep dependencies up-to-date to avoid accumulating technical debt.

### Check for Outdated Packages

#### Android (Gradle)

**Plugin**: `com.github.ben-manes.versions`

```kotlin
// Add to build.gradle.kts
plugins {
    id("com.github.ben-manes.versions") version "0.50.0"
}

tasks.named("dependencyUpdates").configure {
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !stableKeyword && !regex.matches(version)
}
```

**Run**:
```bash
cd Android/src
./gradlew dependencyUpdates

# View report
cat app/build/dependencyUpdates/report.txt
```

#### Website (npm)

```bash
cd website

# Check for outdated
npm outdated

# Interactive update (major versions)
npx npm-check-updates -i
```

### Update Strategy

| Version Change | Risk | Testing Required | Approval |
|----------------|------|------------------|----------|
| Patch (1.0.0 → 1.0.1) | LOW | Unit tests | Auto-update |
| Minor (1.0.0 → 1.1.0) | MEDIUM | Unit + integration tests | Tech lead review |
| Major (1.0.0 → 2.0.0) | HIGH | Full regression + E2E | Architect approval |

### Update Procedure

#### Step 1: Check Changelog
```bash
# npm
npm info <package> versions
npm info <package>@<version>

# View on GitHub
# Navigate to releases page for changelog
```

#### Step 2: Update Incrementally
```bash
# Update ONE dependency at a time
# Android
# Edit build.gradle.kts, change version
./gradlew build

# Website
npm install <package>@<version>
npm test
```

#### Step 3: Run Tests
```bash
# Android
./gradlew test
./gradlew connectedAndroidTest

# Website
npm test
npm run build

# E2E
maestro test .maestro/flows/
```

#### Step 4: Check for Breaking Changes
**Look for**:
- API changes (method signatures)
- Behavior changes (different output)
- Performance regressions
- New warnings/deprecations

#### Step 5: Commit with Changelog Entry
```bash
git commit -m "chore: update <package> from X.Y.Z to A.B.C

Breaking changes:
- None

Fixes:
- CVE-2023-12345 (security vulnerability)

Testing:
- All tests pass
- E2E verified"
```

### Rollback Procedure

If update breaks production:

1. **Identify broken dependency**:
   ```bash
   git log --oneline -- build.gradle.kts package.json | head -5
   ```

2. **Revert to previous version**:
   ```bash
   git revert <commit-hash>
   ```

3. **Test rollback**:
   ```bash
   ./gradlew clean build
   npm clean-install
   ```

4. **Document issue**:
   - Create GitHub issue
   - Tag with "dependency-update-blocked"
   - Link to upstream bug report

---

## Section 3: License Compatibility Verification

### Purpose
Ensure all dependencies have compatible licenses (no GPL/AGPL in proprietary app).

### Allowed Licenses

| License | Commercial Use | Attribution Required | Copyleft |
|---------|----------------|---------------------|----------|
| MIT | ✅ Yes | Yes (in app) | No |
| Apache 2.0 | ✅ Yes | Yes (in app) | No |
| BSD 2/3-Clause | ✅ Yes | Yes (in app) | No |
| ISC | ✅ Yes | Yes (in app) | No |
| CC0 / Public Domain | ✅ Yes | No | No |

### Prohibited Licenses

| License | Reason |
|---------|--------|
| GPL 2.0/3.0 | Copyleft - forces entire app to be GPL |
| AGPL 3.0 | Copyleft + network use = source disclosure |
| SSPL | MongoDB's proprietary copyleft license |
| Commons Clause | Restricts commercial use |

### Audit Procedure

#### Android (Gradle)

**Plugin**: `com.github.jk1.dependency-license-report`

```kotlin
// Add to build.gradle.kts
plugins {
    id("com.github.jk1.dependency-license-report") version "2.5"
}

licenseReport {
    allowedLicensesFile = file("config/allowed-licenses.json")
}
```

**Run**:
```bash
cd Android/src
./gradlew generateLicenseReport

# View report
cat app/build/reports/dependency-license/index.html
```

#### Website (npm)

**Tool**: `license-checker`

```bash
npm install -g license-checker

cd website
license-checker --summary
license-checker --json > licenses.json

# Check for GPL
license-checker --exclude "MIT,Apache-2.0,BSD-2-Clause,BSD-3-Clause,ISC" --production
```

### Compliance Checking

**Automated check**:
```bash
#!/bin/bash
# scripts/check-licenses.sh

cd website
license-checker --production --failOn "GPL;AGPL;SSPL"

if [ $? -ne 0 ]; then
  echo "ERROR: Incompatible license found"
  exit 1
fi

echo "✅ All licenses compatible"
```

**Add to CI**:
```yaml
# .github/workflows/license-check.yml
name: License Check

on: [pull_request, push]

jobs:
  check-licenses:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Check licenses
        run: ./scripts/check-licenses.sh
```

### Apache 2.0 Fork Requirements

**OnDevice AI is a fork of Google AI Edge Gallery (Apache 2.0)**

#### Required Attributions

1. **NOTICE file at app root**:
   ```
   OnDevice AI
   Copyright 2024-2026 On Device AI Inc.

   This product includes software developed by Google LLC.
   Original project: https://github.com/google-ai-edge/gallery
   License: Apache 2.0
   ```

2. **In-app attribution** (Settings → About → Licenses):
   - Show full Apache 2.0 license text
   - Credit Google AI Edge Gallery
   - Link to original repo

3. **Modified files header**:
   ```kotlin
   /*
    * Copyright 2024 Google LLC
    * Copyright 2024-2026 On Device AI Inc.
    *
    * Licensed under the Apache License, Version 2.0
    * Modifications: Added license verification, context compression
    */
   ```

#### Verification
```bash
# Check NOTICE file exists
test -f NOTICE && echo "✅ NOTICE file exists" || echo "❌ Missing NOTICE"

# Check for original copyright headers
grep -r "Copyright.*Google" Android/src/app/src/main | wc -l
# Should be >0 (original files retain Google copyright)

# Check in-app licenses screen
# Manual verification: Settings → About → Open Source Licenses
```

---

## Section 4: Third-Party Attributions

### Purpose
Display all open-source licenses in the app (legal requirement + community respect).

### Generate THIRD_PARTY_LICENSES.txt

#### Android

**Plugin**: `com.google.android.gms.oss-licenses-plugin`

```kotlin
// Add to build.gradle.kts
plugins {
    id("com.google.android.gms.oss-licenses-plugin") version "0.10.6"
}

dependencies {
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")
}
```

**In app**:
```kotlin
// SettingsScreen.kt
@Composable
fun OpenSourceLicensesItem() {
    val context = LocalContext.current
    
    SettingsItem(
        title = "Open Source Licenses",
        subtitle = "View third-party software licenses",
        onClick = {
            // Uses Google's OSS Licenses library
            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
    )
}
```

**Generate at build time**:
```bash
cd Android/src
./gradlew generateLicenseReport

# Output: app/build/reports/licenses/third_party_licenses.txt
```

#### Website

**Tool**: `license-checker`

```bash
cd website
npm install --save-dev license-checker

# Generate licenses
license-checker --production --json > public/third-party-licenses.json

# Or human-readable
license-checker --production > public/THIRD_PARTY_LICENSES.txt
```

**Display in app**:
```tsx
// app/about/licenses/page.tsx
import licenses from '@/public/third-party-licenses.json'

export default function LicensesPage() {
  return (
    <div>
      <h1>Third-Party Licenses</h1>
      {Object.entries(licenses).map(([name, info]) => (
        <div key={name}>
          <h2>{name}</h2>
          <pre>{info.licenseText}</pre>
        </div>
      ))}
    </div>
  )
}
```

### Location in App

**Android**:
- Settings → About → Open Source Licenses
- Long-press app version 7 times → Advanced → Licenses

**Website**:
- Footer → Legal → Open Source Licenses
- `/about/licenses` route

### Verify All Attributions Present

```bash
# Android
./gradlew generateLicenseReport
wc -l app/build/reports/licenses/third_party_licenses.txt
# Should be hundreds of lines (many dependencies)

# Website
cd website
npm run build
test -f public/THIRD_PARTY_LICENSES.txt && echo "✅ Licenses generated"
```

### Include Copyright Notices for Forked Code

**For Google AI Edge Gallery fork**:

1. **Keep original files with headers**:
   ```kotlin
   /*
    * Copyright 2024 Google LLC
    *
    * Licensed under the Apache License, Version 2.0
    */
   ```

2. **Add OnDevice copyright to modified files**:
   ```kotlin
   /*
    * Copyright 2024 Google LLC
    * Copyright 2024-2026 On Device AI Inc.
    *
    * Licensed under the Apache License, Version 2.0
    *
    * Modifications:
    * - Added license verification system
    * - Implemented context compression
    */
   ```

3. **Document modifications**:
   - Create `MODIFICATIONS.md` at repo root
   - List all major changes from upstream
   - Link to original Google repo

### Automation

**Add to CI**:
```yaml
# .github/workflows/licenses.yml
name: Generate Licenses

on:
  push:
    branches: [main]

jobs:
  licenses:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Generate Android licenses
        run: |
          cd Android/src
          ./gradlew generateLicenseReport
      
      - name: Generate website licenses
        run: |
          cd website
          npm install
          npx license-checker --production > THIRD_PARTY_LICENSES.txt
      
      - name: Commit if changed
        run: |
          git config user.name "github-actions"
          git config user.email "github-actions@github.com"
          git add .
          git diff --quiet && git diff --staged --quiet || git commit -m "chore: update third-party licenses"
          git push
```

---

## Summary Checklist

Before declaring "Dependency Audit Complete":

- [ ] OWASP Dependency Check passes (0 HIGH/CRITICAL)
- [ ] npm audit passes (0 HIGH/CRITICAL)
- [ ] All dependencies up-to-date or suppressed with rationale
- [ ] License audit shows no GPL/AGPL
- [ ] NOTICE file exists with Google attribution
- [ ] In-app licenses screen shows all attributions
- [ ] THIRD_PARTY_LICENSES.txt generated
- [ ] CI runs security scans on every PR

**Next**: [Release Preparation](release-prep.md)
