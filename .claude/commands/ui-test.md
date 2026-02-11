Run UI tests. **DroidRun is now the PRIMARY method. Maestro is fallback.**

## PRIMARY: DroidRun (Natural Language)

```bash
# Use the droid alias
droid "Open ai.ondevice.app, send 'hello', verify response appears"

# With options
droidrun run -p Anthropic -m claude-sonnet-4-20250514 --steps 10 "your instruction"
```

See `/visual-verify` for full DroidRun documentation.

---

## FALLBACK: Maestro (Complex Flows)

Use Maestro only for:
- Fresh install flows (TOS acceptance, model download)
- Precise timing-dependent sequences
- Flows that need 20+ minute waits

## Maestro Prerequisites

```bash
# Install Maestro (one-time)
curl -Ls "https://get.maestro.mobile.dev" | bash
export PATH="$PATH:$HOME/.maestro/bin"

# Verify device connected
adb devices
```

## Run Tests

```bash
# Run all flows
maestro test .maestro/flows/

# Run specific flow
maestro test .maestro/flows/chat-flow.yaml

# Run with specific device
maestro test --device R3CT10HETMM .maestro/flows/chat-flow.yaml

# Run with JUnit output
maestro test --format junit .maestro/flows/
```

## Available Flows

| Flow | Purpose | When to Use |
|------|---------|-------------|
| `e2e-fresh-install-to-chat.yaml` | Full app setup from scratch | After fresh APK install |
| `verify-quick.yaml` | Quick feature verification | After code changes |
| `chat-flow.yaml` | Basic chat test | Quick chat check |
| `model-selection.yaml` | Model picker UI | Testing model list |
| `settings-flow.yaml` | Settings navigation | Testing settings |

## Critical: Send Button Selector

```yaml
# THIS WORKS - use accessibility label
- tapOn:
    text: "Message send prompt button"

# These do NOT reliably work:
# - tapOn: { id: "send_button" }
# - tapOn: { text: "Send" }
# - pressKey: Enter
```

## Interactive Tools

```bash
# Visual element inspector (find selectors!)
maestro studio

# Record new flow interactively
maestro record .maestro/flows/new-flow.yaml
```

## CRITICAL: Use Maestro, Not Coordinates

```yaml
# ✅ CORRECT - Element-based
- tapOn: "Download"
- tapOn:
    id: "send_button"

# ❌ WRONG - Coordinate-based (breaks across devices)
# adb shell input tap 540 1180
```

## Visual Verification for Feature Testing

When verifying a new feature:

### Quick Screenshot Flow
```yaml
# .maestro/verify-quick.yaml
appId: ai.ondevice.app
---
- launchApp:
    clearState: false
- takeScreenshot: "current-state"
```

### Feature Verification Flow (AI Response)
```yaml
# .maestro/verify-ai-feature.yaml
appId: ai.ondevice.app
---
- launchApp:
    clearState: false
- takeScreenshot: "01-initial"

# Send message
- tapOn:
    id: "message_input"
    optional: true
- inputText: "hello"
- tapOn:
    id: "send_button" 
    optional: true

# Wait for AI response (IMPORTANT: 60s timeout)
- extendedWaitUntil:
    visible: ".*"
    timeout: 60000

- takeScreenshot: "02-after-response"
```

### Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| "Element not found" | Add `optional: true` |
| Test times out waiting for AI | Increase timeout to 60000+ |
| App state is wrong | Use `clearState: true` or `false` appropriately |
| Screenshots not saved | Check `~/.maestro/tests/` directory |

### Find Screenshots
```bash
# Latest Maestro test run
ls -lt ~/.maestro/tests/ | head -5

# Copy screenshot to current dir
cp ~/.maestro/tests/*/screenshots/*.png ./
```
