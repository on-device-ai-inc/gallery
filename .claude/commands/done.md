# Task Completion Validator

Before marking any task complete, validate ALL evidence requirements.

## Validation Checklist

Run these checks and report results:

### 0. OpenSpec Compliance Check (CRITICAL)

```bash
# Check if there's an active OpenSpec change
ls openspec/changes/ 2>/dev/null

# If active change exists, verify all tasks are complete
CHANGE_NAME=$(ls openspec/changes/ 2>/dev/null | grep -v archive | head -1)
if [ -n "$CHANGE_NAME" ]; then
  echo "Active change: $CHANGE_NAME"
  echo "=== Uncompleted tasks ==="
  grep "\[ \]" openspec/changes/$CHANGE_NAME/tasks.md 2>/dev/null || echo "All tasks complete!"
  echo "=== Completed tasks ==="
  grep "\[x\]" openspec/changes/$CHANGE_NAME/tasks.md 2>/dev/null | wc -l
fi
```

**OpenSpec Compliance Questions:**
- Is there an active OpenSpec change for this feature? If NO → ⚠️ Should have used `/openspec-proposal`
- Are ALL tasks in tasks.md checked `[x]`? If NO → ❌ Incomplete - finish remaining tasks
- Does implementation match spec-delta.md? If NO → ❌ Spec deviation - fix or update spec
- Are acceptance criteria in proposal.md met? If NO → ❌ Not done - meet criteria

### 1. TDD Evidence Check

```bash
# Find test files modified in this session (last 2 hours)
find . -name "*Test*.kt" -mmin -120 2>/dev/null | head -5
```

**Ask yourself:**
- Did I create or modify a test file? If NO → ❌ TDD FAILED - go write tests
- Did the test fail initially (RED)? If NO → ❌ TDD FAILED - test wasn't testing anything
- Did the test pass after implementation (GREEN)? If NO → ❌ TDD FAILED - implementation incomplete

### 2. CI Evidence Check

```bash
# Get latest CI run status
gh run list --limit 1 --json databaseId,status,conclusion,displayTitle

# If in_progress, get average build time and wait smartly
AVG_MINUTES=$(gh run list --limit 5 --json durationMs --jq 'map(.durationMs) | add / length / 60000 | floor')
echo "Average build time: ${AVG_MINUTES} minutes"
```

**If CI status is:**
- `completed` + `success` → ✅ CI PASSED
- `completed` + `failure` → ❌ CI FAILED - run `gh run view <id> --log-failed`, fix, push, wait again
- `in_progress` → ⏳ WAITING - calculate wait time (see below)

**Smart CI Wait:**
```bash
# Get run ID and average time
RUN_ID=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')
AVG_MIN=$(gh run list --limit 5 --json durationMs --jq 'map(.durationMs) | add / length / 60000 | floor')
WAIT_MIN=$((AVG_MIN * 80 / 100))  # Wait 80% of average

echo "CI run $RUN_ID - Average build: ${AVG_MIN}min, waiting ${WAIT_MIN}min before polling"
sleep ${WAIT_MIN}m

# Then poll every 2 minutes
while true; do
  STATUS=$(gh run view $RUN_ID --json status,conclusion --jq '.status')
  if [ "$STATUS" = "completed" ]; then
    RESULT=$(gh run view $RUN_ID --json conclusion --jq '.conclusion')
    echo "CI $RESULT"
    break
  fi
  echo "Still running... checking again in 2 minutes"
  sleep 2m
done
```

### 3. Visual Evidence Check (DroidRun)

**PRIMARY: Use DroidRun for autonomous visual verification**
```bash
# Run verification with your acceptance criteria
droid "Open ai.ondevice.app, [test steps], verify [YOUR CRITERIA]"

# Example:
droid "Open OnDevice app, send 'hello', wait for response, verify disclaimer 'OnDevice can make mistakes' appears"
```

**Check DroidRun output:**
- Did it complete successfully? ("Goal achieved" message)
- Did it find the expected elements?
- Are screenshots in the trajectories folder?

```bash
# Find screenshots from DroidRun
ls -lt trajectories/ | head -3
find trajectories/ -name "*.png" -mmin -30
```

**FALLBACK: Simple screenshot + Claude Vision**
```bash
adb exec-out screencap -p > feature.png
# Then analyze in Claude Code: "Look at feature.png and verify [criteria]"
```

**LEGACY FALLBACK: Maestro (for complex flows)**
```bash
maestro test .maestro/flows/verify-quick.yaml
cp ~/.maestro/tests/$(ls -t ~/.maestro/tests/ | head -1)/screenshots/*.png ./
```

**Ask yourself:**
- Did DroidRun verification pass? If NO → ❌ VISUAL FAILED - fix and retry
- Does the screenshot match acceptance criteria? If NO → ❌ VISUAL FAILED - fix and retry
- For complex flows: Did Maestro assertions pass? If NO → fix and retry

### 4. APK Installed Check

```bash
# Verify app is installed and get version
adb shell dumpsys package ai.ondevice.app | grep -E "versionName|lastUpdateTime" | head -2
```

## Final Report Template

Only after ALL checks pass, report:

```
## ✅ Task Complete

### OpenSpec Change
- Change: `[change-name]`
- Proposal: `openspec/changes/[change-name]/proposal.md`
- Tasks completed: [X]/[Y]
- Ready to archive: Yes/No

### Evidence Summary

**TDD:**
- Test file: `[path/to/TestFile.kt]`
- Iterations: [N] (RED → GREEN on attempt [N])

**CI:**
- Run ID: [ID]
- Status: ✅ PASSED
- Iterations: [N] (passed on attempt [N])
- Build time: [X] minutes

**Visual:**
- Screenshot: `[filename.png]`
- Verification: Screenshot/Maestro
- Iterations: [N] (correct on attempt [N])
- Acceptance criteria: All met ✅

### Spec Compliance
- Implementation matches spec-delta.md: ✅
- All acceptance criteria met: ✅
- Within defined scope: ✅

### What Was Built
[Brief description of feature]

### Files Changed
[List of files modified]

### Next Step
Run `/openspec-archive [change-name]` to complete the workflow.
```

## If Any Check Fails

DO NOT report completion. Instead:

1. Identify which check failed
2. Fix the issue
3. Re-run the relevant loop (TEST/CI/VISUAL)
4. Run `/done` again

**You are NOT done until `/done` shows all checks passing.**

## OpenSpec Archive Reminder

When all tasks are complete and verified:

```bash
/openspec-archive <change-name>
```

This merges specs to `openspec/specs/` and archives the change.
