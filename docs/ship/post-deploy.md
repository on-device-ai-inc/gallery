# Post-Deploy Monitoring

**Purpose**: Monitor production app health and respond to issues within minutes

**Last Updated**: 2026-04-17

---

## Section 1: Monitoring Dashboard Documentation

### Purpose
Centralize all critical metrics in real-time dashboards accessible 24/7.

### Firebase Console

**URL**: https://console.firebase.google.com/project/ondevice-ai-prod

#### Dashboard 1: Crashlytics

**Access**: Firebase Console → Crashlytics

**Key Metrics**:
- **Crash-free users (%)**: Should be ≥99%
- **Crash-free sessions (%)**: Should be ≥99.5%
- **Total crashes (count)**: Monitor spikes
- **Most impacted versions**: Check if new release regressing

**Normal Baselines**:
- Crash-free users: 99.2% - 99.8%
- Crashes per day: <10 (for 1000 DAU)

**Abnormal Patterns**:
- Crash-free users <98% → INVESTIGATE
- Crashes per day >50 → INVESTIGATE
- Single crash type >50% of total → CRITICAL BUG

**Drill-Down**:
```
Crashlytics → Crashes → Select crash
- Stack trace
- Device info (model, RAM, Android version)
- Custom keys (model name, conversation ID, etc.)
- Breadcrumbs (last 100 logs)
```

**Alerting**:
- Firebase Console → Crashlytics → Alerts
- Alert if crash-free users <99% for 1 hour
- Alert if single crash >10 occurrences in 1 hour

---

#### Dashboard 2: Performance Monitoring

**Access**: Firebase Console → Performance

**Key Metrics**:
- **App Start Time**: Cold start, warm start
- **Network Latency**: API calls (license, web search)
- **Custom Traces**: Model load time, image gen time

**Normal Baselines**:

| Metric | P50 | P95 | P99 |
|--------|-----|-----|-----|
| Cold start | 1.8s | 2.5s | 3.2s |
| Model load | 4s | 8s | 12s |
| Image gen | 25s | 45s | 60s |
| License API | 500ms | 1.5s | 3s |

**Abnormal Patterns**:
- P95 cold start >4s → INVESTIGATE (performance regression)
- P95 model load >15s → Check device distribution (too many low-end?)
- P95 license API >5s → Backend issue or network problem

**Custom Traces to Add**:
```kotlin
// In ModelManager.kt
val trace = Firebase.performance.newTrace("model_load")
trace.start()
// Load model...
trace.stop()

// In ImageGenerationUseCase.kt
val trace = Firebase.performance.newTrace("image_generation")
trace.putAttribute("model", "stable_diffusion_1_5")
trace.start()
// Generate image...
trace.stop()
```

---

#### Dashboard 3: Analytics

**Access**: Firebase Console → Analytics → Dashboard

**Key Metrics**:
- **Active Users**: DAU, WAU, MAU
- **Retention**: Day 1, Day 7, Day 30
- **Events**: Top user actions

**Key Events to Track**:

| Event | Parameters | Purpose |
|-------|-----------|---------|
| `app_open` | (automatic) | DAU tracking |
| `license_activated` | `license_tier` | Conversion tracking |
| `model_downloaded` | `model_name`, `size_mb` | Feature usage |
| `chat_message_sent` | `model_name`, `length` | Engagement |
| `image_generated` | `prompt_length`, `duration_ms` | Feature usage |
| `error_occurred` | `error_type`, `screen` | Error tracking |
| `settings_changed` | `setting_name`, `new_value` | User preferences |

**Add Events** (if not already present):
```kotlin
// In LicenseViewModel.kt
Firebase.analytics.logEvent("license_activated") {
    param("license_tier", licenseTier)
    param("activation_time_ms", activationTime)
}

// In ChatViewModel.kt
Firebase.analytics.logEvent("chat_message_sent") {
    param("model_name", currentModel.name)
    param("message_length", message.length.toLong())
    param("response_time_ms", responseTime)
}

// In ErrorHandler.kt
Firebase.analytics.logEvent("error_occurred") {
    param("error_type", error.javaClass.simpleName)
    param("screen", currentScreen)
    param("fatal", error.isFatal)
}
```

**Normal Baselines**:
- Day 1 retention: 40-60%
- Day 7 retention: 20-30%
- Day 30 retention: 10-15%
- Avg session duration: 5-10 minutes

---

#### Dashboard 4: Website API Health

**Tool**: Self-hosted monitoring or UptimeRobot

**URL**: https://uptimerobot.com (or similar)

**Endpoints to Monitor**:

| Endpoint | Expected | Check Interval |
|----------|----------|----------------|
| `https://on-device.org/` | 200 OK | 5 minutes |
| `https://on-device.org/api/health` | `{"status":"ok"}` | 5 minutes |
| `https://on-device.org/api/license/activate` | 200 or 400 (not 500) | 10 minutes |
| `https://on-device.org/api/license/verify` | 200 or 400 (not 500) | 10 minutes |

**Health Check Endpoint** (add if missing):
```typescript
// website/src/app/api/health/route.ts
import { db } from '@/lib/db'
import { NextResponse } from 'next/server'

export async function GET() {
  try {
    // Check database
    await db.execute('SELECT 1')
    
    // Check Stripe (optional)
    // await stripe.customers.list({ limit: 1 })
    
    return NextResponse.json({ 
      status: 'ok',
      timestamp: new Date().toISOString(),
      services: {
        database: 'up',
        stripe: 'up'
      }
    })
  } catch (error) {
    return NextResponse.json(
      { 
        status: 'error', 
        message: error.message 
      },
      { status: 500 }
    )
  }
}
```

**Alerting**:
- Email/SMS if endpoint down for >5 minutes
- Slack notification for 500 errors

---

### Grafana Dashboard (Optional, Advanced)

**If using Grafana**:

**Panels to Add**:
1. Crash rate (%) over time
2. Active users (DAU) over time
3. P95 latency for critical operations
4. License activation success rate
5. Model download success rate
6. Error rate by type

**Query Examples** (if exporting Firebase to BigQuery):
```sql
-- Crash rate by day
SELECT
  DATE(event_timestamp) AS date,
  COUNTIF(event_name = 'app_exception') AS crashes,
  COUNTIF(event_name = 'app_open') AS sessions,
  SAFE_DIVIDE(COUNTIF(event_name = 'app_exception'), COUNTIF(event_name = 'app_open')) * 100 AS crash_rate_pct
FROM `ondevice-ai-prod.analytics_*.events_*`
WHERE _TABLE_SUFFIX BETWEEN FORMAT_DATE('%Y%m%d', DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY)) 
  AND FORMAT_DATE('%Y%m%d', CURRENT_DATE())
GROUP BY date
ORDER BY date DESC
```

---

### Access Instructions

**Firebase Console**:
1. Go to https://console.firebase.google.com
2. Select project: `ondevice-ai-prod`
3. Grant access: Settings → Users and permissions → Add member
4. Email: goraai.info@gmail.com

**UptimeRobot**:
1. Go to https://uptimerobot.com
2. Sign in with goraai.info@gmail.com
3. View monitors for on-device.org

**Grafana** (if applicable):
1. URL: TBD
2. Credentials: TBD

---

## Section 2: Post-Deploy Checklist (24-48hr)

### Purpose
Systematic verification that production deployment is healthy.

### Immediate Post-Deploy (First 2 Hours)

**Time**: 0-2 hours after release

#### Step 1: Verify Deployment Successful

```bash
# Check Play Store version
# Visit: https://play.google.com/store/apps/details?id=ai.ondevice.app
# Verify version number matches expected (1.2.3)

# Check APK downloadable from website
curl -I https://on-device.org/download/app-release.apk
# Should return: 200 OK

# Check license API working
curl -X POST https://on-device.org/api/license/activate \
  -H "Content-Type: application/json" \
  -d '{"license":"TEST-KEY","device":"test-device"}'
# Should return: 400 (invalid license) or 200 (if TEST-KEY valid)
# Should NOT return: 500 (server error)
```

**Checklist**:
- [ ] APK uploaded to Play Store successfully
- [ ] Version number correct (1.2.3, versionCode 11)
- [ ] Website download link works
- [ ] License API responding (200 or 400, not 500)
- [ ] Website homepage loads

---

#### Step 2: Monitor Crash Rate

**Where**: Firebase Console → Crashlytics

**Check**:
- Crash-free users ≥99%
- No new crash types introduced
- No spike in existing crash types

**Action if crash rate >1%**:
1. Identify top crash from Crashlytics
2. Check if regression from new release
3. If CRITICAL (crash-free <95%), consider rollback
4. If HIGH (crash-free 95-99%), hotfix ASAP
5. If MEDIUM (crash-free 99-99.5%), monitor and plan fix

---

#### Step 3: Monitor License Activation Success Rate

**Where**: Firebase Analytics → Events → `license_activated`

**Check**:
- Success rate ≥95%
- Activation time <5s (P95)
- No spike in `error_occurred` with `error_type=LicenseActivationError`

**Action if success rate <90%**:
1. Check license API logs (website backend)
2. Check database connectivity
3. Check Stripe webhook processing
4. Roll back if licensing completely broken

---

#### Step 4: Check for Hotfixes Needed

**Where**: GitHub Issues, support email (goraai.info@gmail.com)

**Look for**:
- Multiple users reporting same issue
- Critical bug that wasn't caught in testing
- Security vulnerability report

**Prioritize Hotfix If**:
- Data loss risk
- Security vulnerability
- Core feature broken (chat, license, model download)
- Crash affecting >10% of users

---

### First 24 Hours Monitoring

**Time**: 2-24 hours after release

#### Hour 4: First Retention Check

**Where**: Firebase Analytics → Retention

**Check**:
- Day 0 retention (how many opened app after install)
- Session duration (should be 5-10 min average)

**Normal**: 60-80% of new installs open app on day 0

**Action if <40%**:
- Check onboarding flow (might be broken)
- Check license activation (blocking users?)
- Check first-time UX

---

#### Hour 8: Performance Check

**Where**: Firebase Performance

**Check**:
- Cold start time still <3s (P95)
- Model load time within baseline
- No performance regression vs previous version

**Compare**:
```
v1.2.2 (old):
- Cold start P95: 2.5s
- Model load P95: 8s

v1.2.3 (new):
- Cold start P95: 2.1s ✅ (improved)
- Model load P95: 8.5s ⚠️ (slight regression, investigate)
```

---

#### Hour 12: Feature Usage Check

**Where**: Firebase Analytics → Events

**Check**:
- `chat_message_sent`: Users are chatting
- `model_downloaded`: Users downloading models
- `image_generated`: Image generation working

**Normal Ratios**:
- 80% of active users send at least 1 chat
- 60% download at least 1 model
- 30% try image generation

**Action if ratios lower**:
- Check if feature broken
- Check if UI hard to find
- Check error rates for that feature

---

#### Hour 24: Full Metrics Review

**Where**: All dashboards

**Check**:
- Crash rate still <1%
- DAU matches expected (based on MAU * 0.3-0.4)
- No unexpected errors
- License activation still working
- Website still up

**Document**:
```markdown
## 24-Hour Post-Deploy Report - v1.2.3

**Deployment Time**: 2026-04-17 10:00 UTC
**Review Time**: 2026-04-18 10:00 UTC

### Metrics

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Crash-free users | ≥99% | 99.4% | ✅ |
| License activation success | ≥95% | 97.2% | ✅ |
| Cold start P95 | <3s | 2.1s | ✅ |
| Day 0 retention | ≥60% | 68% | ✅ |
| DAU | ~300 | 287 | ✅ |

### Issues Found

1. Minor: Image generation slower on Android 9 devices (60s → 75s)
   - Impact: Low (only 5% of users on Android 9)
   - Action: Investigate, plan optimization for v1.2.4

2. None critical

### Recommendation

**CONTINUE ROLLOUT** - All metrics healthy, no blockers.
```

---

### First 48 Hours Monitoring

**Time**: 24-48 hours after release

#### Hour 36: Retention Re-Check

**Where**: Firebase Analytics → Retention

**Check**:
- Day 1 retention (how many came back next day)
- Compare to baseline (40-60% normal)

**Action if Day 1 retention <30%**:
- CRITICAL: Something pushing users away
- Check crash rate (might not be catching all crashes)
- Check user feedback (Play Store reviews, support emails)
- Consider rollback if <20%

---

#### Hour 48: Week 1 Projection

**Where**: Firebase Analytics + Crashlytics + Performance

**Calculate**:
- Projected Week 1 MAU (based on first 2 days)
- Projected crash rate (should stabilize by 48hr)
- Identify any lingering issues

**Document Final 48hr Report**:
```markdown
## 48-Hour Post-Deploy Report - v1.2.3

**Status**: HEALTHY ✅

### Summary

- No critical issues
- Crash rate stable at 99.5% crash-free
- Performance improved vs v1.2.2
- User retention on track
- License activation working reliably

### Known Issues

1. Image generation slower on Android 9 (tracked, not blocking)

### Next Steps

- Continue monitoring for Week 1
- Plan v1.2.4 hotfix for Android 9 image gen performance
- No immediate action needed

**Signed**: Lead Engineer, 2026-04-19
```

---

### Alert Validation

**Purpose**: Ensure alerts trigger correctly when thresholds exceeded.

#### Test Alerts (Before Go-Live)

1. **Crash Alert**:
   ```kotlin
   // Add to test build
   button("Trigger Crash") {
       throw RuntimeException("Test crash for alert validation")
   }
   ```
   - Trigger crash 10 times
   - Verify Firebase alert fires
   - Verify Slack/email notification received

2. **Performance Alert**:
   ```kotlin
   // Add artificial delay
   Thread.sleep(5000)  // Slow down cold start
   ```
   - Trigger slow starts
   - Verify Performance alert fires

3. **Uptime Alert**:
   ```bash
   # Stop website temporarily
   docker stop website
   # Wait 5 minutes
   # Verify UptimeRobot alert fires
   docker start website
   ```

**Checklist**:
- [ ] Crash alert tested and working
- [ ] Performance alert tested and working
- [ ] Uptime alert tested and working
- [ ] Slack notifications configured
- [ ] Email notifications configured
- [ ] On-call person confirmed reachable

---

### Rollback Decision Criteria

**ROLLBACK IMMEDIATELY if**:
- Crash rate >5% for 2 consecutive hours
- License activation success rate <50%
- Data loss reports from >3 users
- Security vulnerability discovered (HIGH/CRITICAL)
- Core feature completely broken (chat, model load, image gen)

**HOTFIX WITHIN 24 HOURS if**:
- Crash rate 2-5%
- License activation success rate 50-90%
- Performance regression >50% (e.g., 3s → 4.5s cold start)
- UX bug affecting >50% of users

**PLAN FIX FOR NEXT RELEASE if**:
- Crash rate 1-2%
- Minor performance regression (<30%)
- Edge case bugs
- UI polish issues

---

### Rollback Execution Steps

**If rollback decision made**:

1. **Stop rollout immediately**:
   ```bash
   # In Play Console:
   # Release Management → App releases → Production → Halt rollout
   ```

2. **Revert to previous version**:
   ```bash
   # Promote previous release
   # Play Console → Previous release (v1.2.2) → Resume rollout
   ```

3. **Notify users**:
   ```markdown
   Subject: OnDevice AI - Temporary Rollback to v1.2.2
   
   We've identified an issue in v1.2.3 and are rolling back to v1.2.2 
   while we investigate. Your data is safe. We'll notify you when the 
   fixed version is available.
   
   If you're experiencing issues, please update to the latest version 
   (v1.2.2) from the Play Store.
   ```

4. **Investigate root cause**:
   ```bash
   # Pull logs from affected users
   # Reproduce bug locally
   # Fix and test thoroughly
   # Create v1.2.4 with fix
   ```

5. **Document incident**:
   - What went wrong
   - Why it wasn't caught in testing
   - How to prevent in future
   - Update test cases

---

### On-Call Confirmation

**Responsibilities**:
- Monitor Firebase dashboards every 2-4 hours (first 24hr)
- Respond to alerts within 15 minutes
- Escalate if unable to resolve within 1 hour
- Document all incidents

**On-Call Schedule**:
- **Primary**: goraai.info@gmail.com (24/7)
- **Backup**: TBD
- **Escalation**: TBD

**Contact Methods**:
- Email: goraai.info@gmail.com
- Slack: TBD
- Phone: TBD

**Runbook**: [RUNBOOK.md](../RUNBOOK.md) (to be created)

---

## Summary Checklist

Before declaring "Post-Deploy Monitoring Complete":

- [ ] Firebase Crashlytics dashboard accessible
- [ ] Firebase Performance dashboard configured
- [ ] Firebase Analytics events tracked
- [ ] Website health endpoint monitoring configured
- [ ] Alerts tested and working (crash, performance, uptime)
- [ ] Rollback procedure documented and tested
- [ ] On-call person confirmed and reachable
- [ ] First 2-hour checks completed
- [ ] First 24-hour checks completed
- [ ] First 48-hour report documented

**Next**: Continue monitoring for Week 1, then monthly reviews.

---

## Appendix: Alert Configuration

### Firebase Crashlytics Alerts

**Setup**:
1. Firebase Console → Crashlytics → Alerts
2. Create alert: "Crash rate threshold"
   - Condition: Crash-free users <99%
   - Duration: 1 hour
   - Notification: Email + Slack
3. Create alert: "New fatal issue"
   - Condition: New crash type appears
   - Notification: Email + Slack immediately

### Firebase Performance Alerts

**Setup**:
1. Firebase Console → Performance → Alerts (if available)
2. Or export to BigQuery and use Grafana alerts

### UptimeRobot Alerts

**Setup**:
1. UptimeRobot → Add Monitor
2. Monitor type: HTTPS
3. URL: https://on-device.org/api/health
4. Check interval: 5 minutes
5. Alert contacts: goraai.info@gmail.com

---

**End of Post-Deploy Monitoring Documentation**
