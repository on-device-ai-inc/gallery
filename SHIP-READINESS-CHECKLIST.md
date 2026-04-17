# Ship Readiness Checklist

**OnDevice AI - Production Launch Validation**

**Last Updated**: 2026-04-17  
**Status**: NOT READY (See individual criteria)  
**Overall Score**: TBD / 56

---

## Quick Reference

| Dimension | Criteria | Status | Priority |
|-----------|----------|--------|----------|
| [Functional Completeness](#1-functional-completeness) | 8 | 🔴 | CRITICAL |
| [Reliability & Failure Handling](#2-reliability--failure-handling) | 8 | 🔴 | CRITICAL |
| [Performance Under Stress](#3-performance-under-stress) | 8 | 🟡 | HIGH |
| [Security & Abuse Resistance](#4-security--abuse-resistance) | 8 | 🟡 | CRITICAL |
| [UX Consistency & Edge Cases](#5-ux-consistency--edge-cases) | 8 | 🟡 | HIGH |
| [Deployment Integrity](#6-deployment-integrity) | 8 | 🔴 | CRITICAL |
| [Monitoring & Rollback Capability](#7-monitoring--rollback-capability) | 8 | 🔴 | CRITICAL |

**Legend**: ✅ Pass | 🟡 Partial | 🔴 Fail | ⚠️ Blocker

---

## Ship Decision Gate

**DO NOT SHIP** until all 56 criteria are ✅.

**Current Blockers**:
- [ ] Database destructive migration
- [ ] Test coverage 4% (need 80%)
- [ ] Context compression broken
- [ ] Web search not working
- [ ] Error UI missing
- [ ] Monitoring not implemented
- [ ] Rollback not tested

**Target Launch Date**: TBD (4-6 weeks minimum from 2026-04-17)

---

## 1. Functional Completeness

### 1.1 Zero Placeholder Content in Production
- **Status**: 🔴 FAIL
- **Current State**: Multiple TODOs and placeholder content found
- **Evidence Required**: 
  - Grep for "TODO", "FIXME", "HACK", "placeholder" returns 0 results in production code
  - All BuildConfig fields have real values
- **Verification Procedure**:
  ```bash
  grep -r "TODO\|FIXME\|HACK\|placeholder" Android/src/app/src/main --exclude-dir=test
  ```
- **Priority**: CRITICAL
- **Blocking**: Yes

### 1.2 All Error States Have User-Actionable Messages
- **Status**: 🔴 FAIL
- **Current State**: Multiple empty catch blocks, silent failures
- **Evidence Required**:
  - Every error/exception shows user-friendly message
  - Error messages include actionable next steps
  - No infinite spinners on error
- **Verification Procedure**:
  - Check ConfigDialog.kt lines 204, 226, 261, 332 (empty catches)
  - Visual verification: trigger errors and verify UI feedback
- **Priority**: CRITICAL
- **Blocking**: Yes

### 1.3 No TODO/FIXME/HACK Comments in Production Code
- **Status**: 🔴 FAIL
- **Current State**: 31+ TODO comments found
- **Evidence Required**:
  - 0 TODO/FIXME/HACK in production code (tests allowed)
- **Verification Procedure**:
  ```bash
  grep -r "TODO\|FIXME\|HACK" Android/src/app/src/main --exclude-dir=test | wc -l
  # Must return: 0
  ```
- **Priority**: HIGH
- **Blocking**: Yes

### 1.4 Feature Parity Between Advertised vs Shipped
- **Status**: 🔴 FAIL
- **Current State**: Audio Scribe advertised but not implemented, Web search broken
- **Evidence Required**:
  - All features on website work in app
  - Marketing copy matches actual functionality
- **Verification Procedure**:
  - Compare on-device.org feature list vs app capabilities
  - Manual E2E test of each advertised feature
- **Priority**: CRITICAL
- **Blocking**: Yes

### 1.5 All User Flows Tested for Success AND Failure Paths
- **Status**: 🔴 FAIL
- **Current State**: Only 3 E2E tests, many flows untested
- **Evidence Required**:
  - E2E tests for all critical flows (chat, image gen, model download, license activation)
  - Tests cover both happy path and error scenarios
- **Verification Procedure**:
  - Check .maestro/flows/ for completeness
  - Run: `maestro test .maestro/flows/` (all must pass)
- **Priority**: HIGH
- **Blocking**: Yes

### 1.6 Documentation Generated from Code, Not Manual
- **Status**: 🟡 PARTIAL
- **Current State**: Manual documentation exists but not auto-generated
- **Evidence Required**:
  - KDoc comments on all public APIs
  - Dokka generates API docs in CI
- **Verification Procedure**:
  - Check for `dokka` task in build.gradle.kts
  - Verify CI generates docs artifact
- **Priority**: MEDIUM
- **Blocking**: No

### 1.7 All Dependencies Pinned to Specific Versions
- **Status**: ✅ PASS
- **Current State**: build.gradle.kts uses specific versions
- **Evidence Required**:
  - No "+" or "latest" in dependency versions
  - All transitive deps resolved to specific versions
- **Verification Procedure**:
  ```bash
  grep -r "[\"'].*+[\"']" Android/src/app/build.gradle.kts
  # Should return: 0 matches
  ```
- **Priority**: HIGH
- **Blocking**: No

### 1.8 Every Advertised Feature Works End-to-End
- **Status**: 🔴 FAIL
- **Current State**: Web search broken (no API key), context compression broken
- **Evidence Required**:
  - Chat with AI: works
  - Image generation: works
  - Web search: works
  - Context compression: works
  - Model download: works
  - License activation: works
- **Verification Procedure**:
  - Run `/visual-verify` on each feature
  - Use DroidRun for automated verification
- **Priority**: CRITICAL
- **Blocking**: Yes

---

## 2. Reliability & Failure Handling

### 2.1 All Network Calls: 15s Timeout, 3 Retries with Exponential Backoff
- **Status**: 🔴 FAIL
- **Current State**: No timeout configuration found
- **Evidence Required**:
  - OkHttp client configured with 15s timeout
  - Retrofit/Ktor has retry interceptor with exponential backoff
- **Verification Procedure**:
  - Check AppModule.kt for HTTP client configuration
  - Verify retry logic in network layer
- **Priority**: HIGH
- **Blocking**: No (but recommended)
- **Reference**: [docs/ship/config-validation.md](docs/ship/config-validation.md)

### 2.2 Offline Mode for Read Operations
- **Status**: 🟡 PARTIAL
- **Current State**: Room DB provides offline chat history, but no offline indicator
- **Evidence Required**:
  - Read operations work without network
  - UI shows offline mode indicator
  - User can access chat history offline
- **Verification Procedure**:
  - Enable airplane mode
  - Verify chat history accessible
  - Verify offline indicator shown
- **Priority**: MEDIUM
- **Blocking**: No

### 2.3 Graceful Degradation for Low Memory
- **Status**: 🔴 FAIL
- **Current State**: No low memory handling found
- **Evidence Required**:
  - onTrimMemory() implemented in Application
  - Model cache cleared on TRIM_MEMORY_RUNNING_LOW
  - User warned before OOM crash
- **Verification Procedure**:
  - Test on 4GB RAM device
  - Trigger low memory condition
  - Verify app doesn't crash
- **Priority**: HIGH
- **Blocking**: No

### 2.4 State Recovery After Process Death
- **Status**: 🟡 PARTIAL
- **Current State**: ViewModels use SavedStateHandle but not fully tested
- **Evidence Required**:
  - SavedStateHandle used for all critical state
  - Process death recovery tested
  - No data loss on process death
- **Verification Procedure**:
  ```bash
  # Simulate process death
  adb shell am kill ai.ondevice.app
  # Reopen app, verify state restored
  ```
- **Priority**: HIGH
- **Blocking**: No

### 2.5 Database Migration Path for Every Version
- **Status**: 🔴 FAIL - **BLOCKER**
- **Current State**: `.fallbackToDestructiveMigration()` = data loss on schema change
- **Evidence Required**:
  - DatabaseMigrations.kt has migration objects for all versions
  - No `.fallbackToDestructiveMigration()` in production
  - Migration tested in instrumented tests
- **Verification Procedure**:
  - Check AppModule.kt:123 (must not have fallbackToDestructiveMigration)
  - Check DatabaseMigrations.kt for MIGRATION_X_Y objects
  - Run instrumented migration tests
- **Priority**: CRITICAL
- **Blocking**: YES ⚠️

### 2.6 Circuit Breaker for Failing Backends
- **Status**: 🔴 FAIL
- **Current State**: No circuit breaker implementation
- **Evidence Required**:
  - Network layer has circuit breaker pattern
  - After N failures, short-circuit to cached/offline mode
- **Verification Procedure**:
  - Mock failing API
  - Verify circuit breaker trips after threshold
  - Verify recovery after cooldown
- **Priority**: MEDIUM
- **Blocking**: No

### 2.7 Data Integrity Checksums for All File Operations
- **Status**: 🔴 FAIL
- **Current State**: Model downloads have no checksum verification
- **Evidence Required**:
  - SHA256 checksums for all downloaded models
  - Verification before model load
  - Corrupted files rejected with error
- **Verification Procedure**:
  - Check ModelDownloadWorker.kt for checksum logic
  - Test with corrupted model file
- **Priority**: HIGH
- **Blocking**: No

### 2.8 Automatic Crash Recovery with Context Preservation
- **Status**: 🟡 PARTIAL
- **Current State**: Crashlytics configured but no recovery logic
- **Evidence Required**:
  - App relaunches after crash
  - User returns to same screen/state
  - Crash context logged to Crashlytics
- **Verification Procedure**:
  - Force crash: `throw RuntimeException("Test")`
  - Relaunch, verify state recovered
  - Check Crashlytics for logged context
- **Priority**: MEDIUM
- **Blocking**: No

---

## 3. Performance Under Stress

### 3.1 Cold Start <3 Seconds to First Interaction
- **Status**: 🔴 FAIL
- **Current State**: No performance measurement
- **Evidence Required**:
  - P95 cold start <3s on mid-range device
  - Measured via Firebase Performance or custom instrumentation
- **Verification Procedure**:
  ```bash
  # Measure via adb
  adb shell am start -W ai.ondevice.app/.MainActivity
  # TotalTime should be <3000ms
  ```
- **Priority**: HIGH
- **Blocking**: No

### 3.2 UI Thread Never Blocked >16ms (60fps Guarantee)
- **Status**: 🔴 FAIL
- **Current State**: No frame profiling
- **Evidence Required**:
  - Android Profiler shows 0 jank
  - All heavy work on background threads
- **Verification Procedure**:
  - Run Android Profiler during heavy operations
  - Check for frames >16ms
- **Priority**: MEDIUM
- **Blocking**: No

### 3.3 Memory Footprint <300MB (Excluding Models)
- **Status**: 🟡 PARTIAL
- **Current State**: Not measured
- **Evidence Required**:
  - Memory usage measured via Android Profiler
  - Average <300MB during normal use
- **Verification Procedure**:
  ```bash
  adb shell dumpsys meminfo ai.ondevice.app
  # Check TOTAL RAM usage
  ```
- **Priority**: MEDIUM
- **Blocking**: No

### 3.4 Battery Consumption <5% Per Hour Active Use
- **Status**: 🔴 FAIL
- **Current State**: Not measured
- **Evidence Required**:
  - Battery Historian analysis
  - Measured drain <5%/hour
- **Verification Procedure**:
  - Use Battery Historian
  - 1-hour test session
  - Verify drain rate
- **Priority**: MEDIUM
- **Blocking**: No

### 3.5 Network Efficiency (Batched API Calls, HTTP/2)
- **Status**: 🟡 PARTIAL
- **Current State**: OkHttp supports HTTP/2 but no batching
- **Evidence Required**:
  - OkHttp configured for HTTP/2
  - Analytics events batched
- **Verification Procedure**:
  - Check network traffic via Charles Proxy
  - Verify HTTP/2 in use
  - Verify requests batched
- **Priority**: MEDIUM
- **Blocking**: No

### 3.6 Model Load Time <10s on Mid-Range Device
- **Status**: 🟡 PARTIAL
- **Current State**: Works but not measured
- **Evidence Required**:
  - Firebase Performance traces model load
  - P95 <10s on mid-range (6GB RAM, SD 778G)
- **Verification Procedure**:
  - Add Performance trace to ModelManager
  - Test on Samsung A53 or equivalent
- **Priority**: HIGH
- **Blocking**: No

### 3.7 Image Generation Latency Feedback Every 500ms
- **Status**: 🔴 FAIL
- **Current State**: No progress updates during generation
- **Evidence Required**:
  - UI updates every 500ms during image gen
  - Progress bar or partial image shown
- **Verification Procedure**:
  - Trigger image generation
  - Verify UI updates during process
- **Priority**: MEDIUM
- **Blocking**: No

### 3.8 Startup Performance Regression Testing in CI
- **Status**: 🔴 FAIL
- **Current State**: No performance tests in CI
- **Evidence Required**:
  - CI runs startup benchmark
  - Fails if regression >10%
- **Verification Procedure**:
  - Check .github/workflows/ for benchmark job
  - Run benchmark locally
- **Priority**: LOW
- **Blocking**: No

---

## 4. Security & Abuse Resistance

### 4.1 All User Input Validated at API Boundary + Client
- **Status**: 🟡 PARTIAL
- **Current State**: Some validation present but inconsistent
- **Evidence Required**:
  - Input validation on all text fields
  - Server-side validation for all API endpoints
- **Verification Procedure**:
  - Code review of input handling
  - Fuzz testing with malformed inputs
- **Priority**: HIGH
- **Blocking**: No (but recommended)
- **Reference**: [docs/ship/dependency-audit.md](docs/ship/dependency-audit.md)

### 4.2 Certificate Pinning for All API Calls
- **Status**: 🔴 FAIL
- **Current State**: No certificate pinning implemented
- **Evidence Required**:
  - OkHttp configured with CertificatePinner
  - Pinned to on-device.org certificate
- **Verification Procedure**:
  - Check AppModule.kt for CertificatePinner
  - Test with MITM proxy (should fail)
- **Priority**: HIGH
- **Blocking**: No

### 4.3 No Secrets in APK (Obfuscated or Encrypted)
- **Status**: 🔴 FAIL
- **Current State**: BRAVE_API_KEY removed but other secrets may exist
- **Evidence Required**:
  - No API keys in BuildConfig or strings.xml
  - Secrets fetched from CI environment
- **Verification Procedure**:
  ```bash
  unzip -p app-debug.apk | strings | grep -i "api_key\|secret\|password"
  # Should return: 0 matches
  ```
- **Priority**: CRITICAL
- **Blocking**: Yes
- **Reference**: [docs/ship/config-validation.md](docs/ship/config-validation.md)

### 4.4 Rate Limiting: 10 Req/Min Per User, 100 Per IP
- **Status**: 🔴 FAIL
- **Current State**: No rate limiting on website API
- **Evidence Required**:
  - Express middleware or Caddy rate limiter configured
  - 10/min per user, 100/min per IP
- **Verification Procedure**:
  - Test with rapid API requests
  - Verify 429 Too Many Requests returned
- **Priority**: HIGH
- **Blocking**: No

### 4.5 License Verification on Launch + Hourly Background
- **Status**: 🟡 PARTIAL
- **Current State**: Launch verification works, no hourly check
- **Evidence Required**:
  - License checked on app launch
  - WorkManager periodic check every hour
- **Verification Procedure**:
  - Check LicenseVerificationWorker.kt
  - Verify periodic work scheduled
- **Priority**: MEDIUM
- **Blocking**: No

### 4.6 Tamper Detection: Signature + Root + Frida
- **Status**: 🔴 FAIL
- **Current State**: IntegrityChecker.kt has placeholder hash
- **Evidence Required**:
  - Release signing certificate hash verified
  - Root detection implemented
  - Frida detection implemented
  - App refuses to run if tampered
- **Verification Procedure**:
  - Test on rooted device (should detect)
  - Test with Frida attached (should detect)
  - Check IntegrityChecker.kt for real hash
- **Priority**: MEDIUM
- **Blocking**: No

### 4.7 Sensitive Data Encrypted at Rest (SQLCipher)
- **Status**: 🔴 FAIL
- **Current State**: Using Room without encryption
- **Evidence Required**:
  - SQLCipher integrated with Room
  - Database encrypted with user key
- **Verification Procedure**:
  - Check build.gradle.kts for sqlcipher dependency
  - Verify database file is encrypted (unreadable with sqlite3)
- **Priority**: HIGH
- **Blocking**: No

### 4.8 Automated Dependency Vulnerability Scanning in CI
- **Status**: 🔴 FAIL
- **Current State**: No OWASP or Snyk in CI
- **Evidence Required**:
  - CI runs OWASP Dependency Check or Snyk
  - Fails on HIGH/CRITICAL vulnerabilities
- **Verification Procedure**:
  - Check .github/workflows/ for security scan job
  - Run locally: `./gradlew dependencyCheckAnalyze`
- **Priority**: HIGH
- **Blocking**: No
- **Reference**: [docs/ship/dependency-audit.md](docs/ship/dependency-audit.md)

---

## 5. UX Consistency & Edge Cases

### 5.1 Every Async Operation Has Loading State
- **Status**: 🟡 PARTIAL
- **Current State**: Some loading states present, inconsistent
- **Evidence Required**:
  - All network/file operations show loading UI
  - Loading states have timeout (no infinite spinners)
- **Verification Procedure**:
  - Visual audit of all screens
  - Simulate slow network, verify loading shown
- **Priority**: HIGH
- **Blocking**: No

### 5.2 All Error Messages User-Friendly + Actionable
- **Status**: 🔴 FAIL
- **Current State**: Many technical error messages, no actions
- **Evidence Required**:
  - No stack traces in error UI
  - Every error has "Try again" or specific action
- **Verification Procedure**:
  - Trigger errors (network fail, model missing, etc.)
  - Verify messages are user-friendly
- **Priority**: CRITICAL
- **Blocking**: Yes

### 5.3 Empty States Have Clear Next Action
- **Status**: 🟡 PARTIAL
- **Current State**: Some empty states exist, not all
- **Evidence Required**:
  - Empty chat: "Start a conversation"
  - No models: "Download a model"
  - Clear CTA buttons
- **Verification Procedure**:
  - Fresh install, verify empty states
  - Check for actionable buttons
- **Priority**: MEDIUM
- **Blocking**: No

### 5.4 Permission Requests Have Justification Before Prompt
- **Status**: 🔴 FAIL
- **Current State**: No permission rationale dialogs
- **Evidence Required**:
  - Before camera permission: explain why needed
  - Before storage: explain why needed
- **Verification Procedure**:
  - Fresh install, trigger permissions
  - Verify rationale shown before system prompt
- **Priority**: MEDIUM
- **Blocking**: No

### 5.5 All Text Supports Dynamic Type (Accessibility)
- **Status**: 🟡 PARTIAL
- **Current State**: Compose uses Material Typography, scales by default
- **Evidence Required**:
  - Text scales with system font size
  - No hardcoded text sizes
- **Verification Procedure**:
  - Change system font size to largest
  - Verify all text readable, no overflow
- **Priority**: MEDIUM
- **Blocking**: No

### 5.6 Screen Reader Support for All Interactive Elements
- **Status**: 🔴 FAIL
- **Current State**: Many missing contentDescription
- **Evidence Required**:
  - All buttons have contentDescription
  - TalkBack can navigate app fully
- **Verification Procedure**:
  - Enable TalkBack
  - Navigate through app
  - Verify all elements announced
- **Priority**: HIGH
- **Blocking**: No

### 5.7 Onboarding Completes in <60s with Skip Option
- **Status**: 🟡 PARTIAL
- **Current State**: TOS/Privacy acceptance required, no timing measured
- **Evidence Required**:
  - First-run onboarding <60s
  - Skip button on optional steps
- **Verification Procedure**:
  - Fresh install, time onboarding
  - Verify skip option exists
- **Priority**: LOW
- **Blocking**: No

### 5.8 All Forms Validate Real-Time with Inline Errors
- **Status**: 🟡 PARTIAL
- **Current State**: License input validates on submit, not real-time
- **Evidence Required**:
  - Input validation on text change
  - Inline error messages (not toasts)
- **Verification Procedure**:
  - Test all forms (license, settings)
  - Verify real-time validation
- **Priority**: MEDIUM
- **Blocking**: No

---

## 6. Deployment Integrity

### 6.1 Feature Flags for Gradual Rollout (1%→10%→50%→100%)
- **Status**: 🔴 FAIL
- **Current State**: No feature flags system
- **Evidence Required**:
  - Firebase Remote Config or LaunchDarkly integrated
  - Can enable/disable features remotely
- **Verification Procedure**:
  - Check for Remote Config in AppModule
  - Test toggling feature flag
- **Priority**: MEDIUM
- **Blocking**: No
- **Reference**: [docs/ship/release-prep.md](docs/ship/release-prep.md)

### 6.2 Database Migrations Reversible (Up + Down Scripts)
- **Status**: 🔴 FAIL - **BLOCKER**
- **Current State**: No migrations, `.fallbackToDestructiveMigration()`
- **Evidence Required**:
  - DatabaseMigrations.kt has up migrations
  - Downgrade path documented (manual or automated)
- **Verification Procedure**:
  - Check DatabaseMigrations.kt
  - Test migration forward then rollback
- **Priority**: CRITICAL
- **Blocking**: YES ⚠️

### 6.3 APK Versioning Auto-Increments in CI
- **Status**: 🔴 FAIL
- **Current State**: Manual version bumping in build.gradle.kts
- **Evidence Required**:
  - CI auto-increments versionCode
  - versionName follows semantic versioning
- **Verification Procedure**:
  - Check .github/workflows/ for version bumping
  - Verify no manual edits needed
- **Priority**: MEDIUM
- **Blocking**: No
- **Reference**: [docs/ship/release-prep.md](docs/ship/release-prep.md)

### 6.4 Configuration Management (Secrets in CI, Not Code)
- **Status**: 🟡 PARTIAL
- **Current State**: Some secrets in GitHub Secrets, some missing
- **Evidence Required**:
  - All secrets in GitHub Actions secrets
  - No secrets in code or version control
- **Verification Procedure**:
  - Check .env.example or similar
  - Verify secrets audit procedure followed
- **Priority**: CRITICAL
- **Blocking**: Yes
- **Reference**: [docs/ship/config-validation.md](docs/ship/config-validation.md)

### 6.5 Automated Rollback Trigger (>1% Crash Rate Reverts)
- **Status**: 🔴 FAIL
- **Current State**: No automated rollback
- **Evidence Required**:
  - Crashlytics webhook triggers rollback
  - Rollback script tested and ready
- **Verification Procedure**:
  - Simulate high crash rate
  - Verify rollback triggered
- **Priority**: MEDIUM
- **Blocking**: No

### 6.6 Blue/Green Deployment for Backend API
- **Status**: 🔴 FAIL
- **Current State**: Manual deployment, no blue/green
- **Evidence Required**:
  - Website can deploy to staging without downtime
  - Traffic can be switched between versions
- **Verification Procedure**:
  - Deploy to staging
  - Switch traffic
  - Verify zero downtime
- **Priority**: LOW
- **Blocking**: No

### 6.7 Version Compatibility Matrix (App X.Y Works with API Z.W)
- **Status**: 🔴 FAIL
- **Current State**: No version compatibility tracking
- **Evidence Required**:
  - API version negotiation in app
  - Old app versions still work after API update
- **Verification Procedure**:
  - Document minimum API version for each app version
  - Test old app with new API
- **Priority**: MEDIUM
- **Blocking**: No

### 6.8 Staged Rollout Schedule (Alpha → Beta → Prod)
- **Status**: 🔴 FAIL
- **Current State**: No staged rollout plan
- **Evidence Required**:
  - Alpha track (internal testers)
  - Beta track (early adopters)
  - Production track (public)
  - Rollout schedule documented
- **Verification Procedure**:
  - Check Play Console for tracks
  - Verify rollout plan exists
- **Priority**: MEDIUM
- **Blocking**: No
- **Reference**: [docs/ship/release-prep.md](docs/ship/release-prep.md)

---

## 7. Monitoring & Rollback Capability

### 7.1 Crash Reporting with Full Stack Traces + Device Context
- **Status**: 🟡 PARTIAL
- **Current State**: Crashlytics configured but minimal custom context
- **Evidence Required**:
  - Crashlytics captures all crashes
  - Custom keys logged (model, conversation ID, etc.)
- **Verification Procedure**:
  - Force crash, check Firebase Console
  - Verify custom context present
- **Priority**: HIGH
- **Blocking**: No
- **Reference**: [docs/ship/post-deploy.md](docs/ship/post-deploy.md)

### 7.2 Error Tracking for Non-Fatal Exceptions
- **Status**: 🔴 FAIL
- **Current State**: printStackTrace() in production (leaks to logcat only)
- **Evidence Required**:
  - All exceptions logged to Crashlytics
  - Non-fatal errors tracked
- **Verification Procedure**:
  - Trigger non-fatal error
  - Check Crashlytics for logged exception
- **Priority**: HIGH
- **Blocking**: No

### 7.3 Performance Monitoring (P50/P95/P99 Latency)
- **Status**: 🔴 FAIL
- **Current State**: No performance monitoring
- **Evidence Required**:
  - Firebase Performance traces key operations
  - P50/P95/P99 visible in dashboard
- **Verification Procedure**:
  - Check Firebase Console for Performance tab
  - Verify traces for model load, image gen, etc.
- **Priority**: HIGH
- **Blocking**: No
- **Reference**: [docs/ship/post-deploy.md](docs/ship/post-deploy.md)

### 7.4 User Analytics (Privacy-Respecting, Opt-Out)
- **Status**: 🔴 FAIL
- **Current State**: Firebase Analytics configured but minimal events
- **Evidence Required**:
  - Key events tracked (chat_sent, image_generated, etc.)
  - Analytics opt-out in Settings
  - Privacy policy updated
- **Verification Procedure**:
  - Check Firebase Console for Events
  - Verify opt-out toggle works
- **Priority**: MEDIUM
- **Blocking**: No

### 7.5 Health Checks for All Critical Services
- **Status**: 🔴 FAIL
- **Current State**: No health check endpoints
- **Evidence Required**:
  - Website has /health endpoint
  - Returns 200 if DB + Stripe reachable
- **Verification Procedure**:
  ```bash
  curl https://on-device.org/health
  # Should return: {"status": "ok"}
  ```
- **Priority**: MEDIUM
- **Blocking**: No

### 7.6 Automated Alerts (>5% Crash, >2s P95 Latency, >1% API Error)
- **Status**: 🔴 FAIL
- **Current State**: No alerting configured
- **Evidence Required**:
  - Firebase Alerts configured
  - Slack/email notifications on thresholds
- **Verification Procedure**:
  - Check Firebase Console for Alert rules
  - Trigger alert condition, verify notification
- **Priority**: HIGH
- **Blocking**: No
- **Reference**: [docs/ship/post-deploy.md](docs/ship/post-deploy.md)

### 7.7 Real-Time Dashboard for Key Metrics
- **Status**: 🔴 FAIL
- **Current State**: Firebase Console only, no custom dashboard
- **Evidence Required**:
  - Grafana or Firebase dashboard with key metrics
  - Visible to team 24/7
- **Verification Procedure**:
  - Access dashboard URL
  - Verify metrics update real-time
- **Priority**: MEDIUM
- **Blocking**: No
- **Reference**: [docs/ship/post-deploy.md](docs/ship/post-deploy.md)

### 7.8 Rollback Capability Within 5 Minutes
- **Status**: 🔴 FAIL - **BLOCKER**
- **Current State**: No tested rollback procedure
- **Evidence Required**:
  - Rollback script exists and tested
  - Can revert to previous version in <5min
  - Rollback documented in runbook
- **Verification Procedure**:
  - Run rollback script on staging
  - Time the process
  - Verify app reverts successfully
- **Priority**: CRITICAL
- **Blocking**: YES ⚠️
- **Reference**: [docs/ship/post-deploy.md](docs/ship/post-deploy.md)

---

## Summary

**Current Status**: 🔴 NOT READY

**Passing**: 1 / 56 (1.8%)  
**Partial**: 13 / 56 (23.2%)  
**Failing**: 42 / 56 (75.0%)

**Critical Blockers**: 7
1. Database destructive migration (2.5)
2. Test coverage 4% vs 80% requirement (1.3)
3. Context compression broken (1.8)
4. Web search not working (1.8)
5. Error UI missing (1.2, 5.2)
6. No secrets in APK (4.3)
7. Rollback not tested (7.8)

**Timeline to Ship**: 4-6 weeks minimum

---

## Next Steps

1. **Week 1**: Fix 7 critical blockers
2. **Week 2-3**: Test coverage sprint (4% → 80%)
3. **Week 4**: Monitoring + observability implementation
4. **Week 5**: Security audit + compliance
5. **Week 6**: Final verification + staged rollout prep

**For detailed procedures, see**:
- [Configuration Validation](docs/ship/config-validation.md)
- [Dependency Audit](docs/ship/dependency-audit.md)
- [Release Preparation](docs/ship/release-prep.md)
- [Final Verification](docs/ship/final-verification.md)
- [Post-Deploy Monitoring](docs/ship/post-deploy.md)

---

**Authority**: This checklist is derived from FORENSIC-SHIP-READINESS-REPORT.md (NVIDIA, Meta, Google, Apple, Microsoft, Amazon ship standards)

**Last Validation**: Never (first version)

**Next Review**: After each blocker fixed
