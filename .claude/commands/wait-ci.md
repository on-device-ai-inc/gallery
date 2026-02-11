# Smart CI Wait

Wait for CI to complete using historical build times to avoid excessive polling.

## Usage

Run after pushing code to wait efficiently for CI completion.

## Process

### Step 1: Get Latest Run Info

```bash
# Get the latest run ID and status
RUN_INFO=$(gh run list --limit 1 --json databaseId,status,conclusion,displayTitle,createdAt)
RUN_ID=$(echo "$RUN_INFO" | jq -r '.[0].databaseId')
STATUS=$(echo "$RUN_INFO" | jq -r '.[0].status')
TITLE=$(echo "$RUN_INFO" | jq -r '.[0].displayTitle')

echo "📦 CI Run: $RUN_ID"
echo "📝 Title: $TITLE"
echo "📊 Status: $STATUS"
```

### Step 2: If Already Complete

```bash
if [ "$STATUS" = "completed" ]; then
  CONCLUSION=$(echo "$RUN_INFO" | jq -r '.[0].conclusion')
  if [ "$CONCLUSION" = "success" ]; then
    echo "✅ CI PASSED"
  else
    echo "❌ CI FAILED - checking logs..."
    gh run view $RUN_ID --log-failed
  fi
  exit 0
fi
```

### Step 3: Calculate Smart Wait Time

```bash
# Get historical build times (last 5 successful builds)
echo "📊 Calculating wait time from historical builds..."

HISTORY=$(gh run list --limit 10 --json durationMs,conclusion --jq '[.[] | select(.conclusion=="success")] | .[0:5]')
AVG_MS=$(echo "$HISTORY" | jq 'map(.durationMs) | add / length')
AVG_MIN=$(echo "$AVG_MS / 60000" | bc)
MIN_MIN=$(echo "$HISTORY" | jq 'map(.durationMs) | min / 60000 | floor')
MAX_MIN=$(echo "$HISTORY" | jq 'map(.durationMs) | max / 60000 | ceil')

echo "📈 Historical build times:"
echo "   Average: ${AVG_MIN} minutes"
echo "   Range: ${MIN_MIN} - ${MAX_MIN} minutes"

# Wait 75% of average before first check
INITIAL_WAIT=$(echo "$AVG_MIN * 0.75" | bc | cut -d. -f1)
if [ "$INITIAL_WAIT" -lt 5 ]; then
  INITIAL_WAIT=5
fi

echo "⏳ Waiting ${INITIAL_WAIT} minutes before first check..."
```

### Step 4: Wait and Poll

```bash
# Initial wait (75% of average)
sleep ${INITIAL_WAIT}m

# Then poll every 2 minutes
POLL_COUNT=0
MAX_POLLS=15  # Max 30 more minutes of polling

while [ $POLL_COUNT -lt $MAX_POLLS ]; do
  STATUS=$(gh run view $RUN_ID --json status --jq '.status')
  
  if [ "$STATUS" = "completed" ]; then
    CONCLUSION=$(gh run view $RUN_ID --json conclusion --jq '.conclusion')
    DURATION=$(gh run view $RUN_ID --json durationMs --jq '.durationMs / 60000 | floor')
    
    if [ "$CONCLUSION" = "success" ]; then
      echo ""
      echo "✅ CI PASSED in ${DURATION} minutes"
      echo ""
      echo "📥 Download APK with:"
      echo "   gh run download $RUN_ID -n app-debug"
      exit 0
    else
      echo ""
      echo "❌ CI FAILED after ${DURATION} minutes"
      echo ""
      echo "📋 Failure logs:"
      gh run view $RUN_ID --log-failed
      exit 1
    fi
  fi
  
  POLL_COUNT=$((POLL_COUNT + 1))
  ELAPSED=$((INITIAL_WAIT + POLL_COUNT * 2))
  echo "⏳ Still running... (${ELAPSED} min elapsed, checking again in 2 min)"
  sleep 2m
done

echo "⚠️ CI still running after $((INITIAL_WAIT + MAX_POLLS * 2)) minutes"
echo "   Check manually: gh run view $RUN_ID"
```

## Quick Version (Copy-Paste)

```bash
# One-liner smart CI wait
RUN_ID=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId') && \
AVG=$(gh run list --limit 5 --json durationMs,conclusion --jq '[.[] | select(.conclusion=="success")] | map(.durationMs) | add / length / 60000 | floor') && \
WAIT=$((AVG * 3 / 4)) && \
echo "Waiting ${WAIT}min (avg build: ${AVG}min)..." && \
sleep ${WAIT}m && \
gh run watch $RUN_ID
```

## Expected Output (Example - actual values calculated dynamically)

```
📦 CI Run: 20827839192
📝 Title: feat: Add AI disclaimer
📊 Status: in_progress
📈 Historical build times:
   Average: [X] minutes (calculated from last 5 successful builds)
   Range: [min] - [max] minutes
⏳ Waiting [75% of avg] minutes before first check...
⏳ Still running... ([N] min elapsed, checking again in 2 min)
⏳ Still running... ([N+2] min elapsed, checking again in 2 min)

✅ CI PASSED in [actual] minutes

📥 Download APK with:
   gh run download [RUN_ID] -n app-debug
```
