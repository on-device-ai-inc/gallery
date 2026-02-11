# Visual Verification with DroidRun

Run autonomous visual verification using natural language commands.

## Quick Usage

**IMPORTANT: Always end prompts with "then stop" to prevent infinite loops!**

```bash
# Basic verification
droid "Open ai.ondevice.app, send 'hello', verify response appears, then stop"

# With specific criteria
droid "Open OnDevice app, send message, verify disclaimer text 'OnDevice can make mistakes' appears, then stop"

# Settings verification
droid "Open OnDevice app, navigate to settings, verify dark mode toggle exists, then stop"
```

## Setup (in ~/.bashrc)

```bash
export GOOGLE_API_KEY="your-google-api-key"
alias droid="droidrun run -p GoogleGenAI -m gemini-2.0-flash --steps 10"
```

Get API key: https://makersuite.google.com/app/apikey

## Full Command Options

```bash
droidrun run -p GoogleGenAI -m gemini-2.0-flash [OPTIONS] "instruction"

Options:
  --steps INTEGER    Maximum steps (default: 15, recommend: 10)
  --vision           Use screenshots for all agents
  --debug            Verbose output
  --stream           Stream responses in real-time
```

## Example Verification Commands

### Disclaimer Feature
```bash
droid "Open ai.ondevice.app, tap the message input, type 'hello', send the message, wait for AI response, verify text 'OnDevice can make mistakes' is visible, then stop"
```

### Logo Color
```bash
droid "Open ai.ondevice.app, send a message, wait for response, verify the logo next to the disclaimer is in color, then stop"
```

### Settings Screen
```bash
droid "Open ai.ondevice.app, find and tap settings button, verify settings screen appears with theme options, then stop"
```

### Dark Mode
```bash
droid "Open ai.ondevice.app, go to settings, enable dark mode, go back to chat, verify the background is dark, then stop"
```

## Find Screenshots

```bash
# DroidRun saves to trajectories/
ls -lt trajectories/ | head -5

# Find recent screenshots
find trajectories/ -name "*.png" -mmin -30

# Copy to current directory
cp trajectories/$(ls -t trajectories/ | head -1)/*.png ./
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Portal errors | `adb shell am force-stop dev.anthropic.droidrun.portal` then retry |
| Multiple devices | `adb disconnect <ip>` |
| Runs forever | Use `--steps 10` |
| Wrong app | Use full package: "ai.ondevice.app" |

## Fallback to Maestro

If DroidRun can't handle a specific flow:

```bash
# Fresh install flow (TOS, model download)
maestro test .maestro/flows/e2e-fresh-install-to-chat.yaml

# Quick verification
maestro test .maestro/flows/verify-quick.yaml
```
