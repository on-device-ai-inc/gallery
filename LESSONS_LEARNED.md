# Lessons Learned - Claude's Operating Manual

> **Claude: READ THIS FILE at session start and on first failure.**
> **UPDATE THIS FILE when you discover patterns that work.**

---

## 🚦 MANDATORY EXECUTION PROTOCOL

**For EVERY task, follow this protocol. No exceptions.**

### The Flow

```
PHASE 0: READ DOCS
    │   □ Read LESSONS_LEARNED.md, CLAUDE.md
    │   □ PROVE you read it: quote a relevant rule
    ▼
PHASE 1: EXPLORE (No Code Yet)
    │   □ Search codebase (find, grep)
    │   □ Read existing patterns
    │   □ Identify risks
    │
    │   ⛔ STOP: Share findings. "Waiting for your approval."
    │      DO NOT write code until user says "approved"
    ▼
USER SAYS: "approved"
    │
    ▼
╔═══════════════════════════════════════════════════════════════════╗
║  AUTONOMOUS EXECUTION - Loop Until Complete Success               ║
╠═══════════════════════════════════════════════════════════════════╣
║                                                                   ║
║  PHASE 2: TDD RED                                                 ║
║      □ Write test(s) defining expected behavior                   ║
║      □ Test should FAIL initially                                 ║
║                         │                                         ║
║                         ▼                                         ║
║  PHASE 3: TDD GREEN                                               ║
║      □ Write implementation                                       ║
║      □ Run test                                                   ║
║                         │                                         ║
║      🔄 TEST LOOP ◄─────┤ Test fails?                             ║
║         │               │    YES → Fix code, run test again       ║
║         │               │    Loop until PASS                      ║
║         │               ▼                                         ║
║         │         Test passes                                     ║
║                         │                                         ║
║                         ▼                                         ║
║  PHASE 4: BUILD & DEPLOY                                          ║
║      □ Commit and push                                            ║
║      □ Wait for CI (gh run watch)                                 ║
║                         │                                         ║
║      🔄 CI LOOP ◄───────┤ CI fails?                               ║
║         │               │    YES → gh run view --log-failed       ║
║         │               │    Fix issue, push again                ║
║         │               │    Loop until CI PASSES                 ║
║         │               ▼                                         ║
║         │         CI passes                                       ║
║                         │                                         ║
║      □ Download APK (gh run download <id> -n app-debug)           ║
║      □ Install (adb install -r app-debug.apk)                     ║
║                         │                                         ║
║                         ▼                                         ║
║  PHASE 5: VISUAL VERIFICATION                                     ║
║      □ Launch app, navigate to feature                            ║
║      □ Screenshot (adb exec-out screencap -p > screenshot.png)    ║
║      □ Compare to requirements                                    ║
║                         │                                         ║
║      🔄 VISUAL LOOP ◄───┤ Doesn't match requirements?             ║
║         │               │    YES → Identify gap, fix UI code      ║
║         │               │    Rebuild, reinstall, screenshot       ║
║         │               │    Loop until MATCHES                   ║
║         │               ▼                                         ║
║         │         Visual correct                                  ║
║                         │                                         ║
║                         ▼                                         ║
║  PHASE 6: LEARN                                                   ║
║      □ Update LESSONS_LEARNED.md if anything new discovered       ║
║                                                                   ║
╚═══════════════════════════════════════════════════════════════════╝
    │
    ▼
REPORT TO USER
    "Feature complete. Evidence:
     - Tests: PASS
     - CI: PASS [run ID]
     - Screenshot: [image]"
```

### Three Loops - Keep Iterating Until Success

| Loop | Trigger | Action |
|------|---------|--------|
| **🔄 TEST LOOP** | Test fails | Read error → Fix code → Run test → Repeat until pass |
| **🔄 CI LOOP** | CI fails | Check logs (`gh run view --log-failed`) → Fix → Push → Repeat until green |
| **🔄 VISUAL LOOP** | Screenshot wrong | Identify gap → Fix UI → Rebuild → Reinstall → Screenshot → Repeat until correct |

### Critical Rules

1. **ONE STOP POINT ONLY**: Stop after Phase 1 (explore). After "approved", run autonomously to completion.
2. **NEVER GIVE UP**: Loop until each phase succeeds. Don't stop at first failure.
3. **NEVER CLAIM SUCCESS WITHOUT EVIDENCE**: Screenshot proving feature works, or it didn't happen.
4. **NEVER SKIP TDD**: Write test first, then implementation.

---

## 📊 Know Your Limitations

| Reality Check | What This Means For You |
|---------------|-------------------------|
| **42%** of your code contains hallucinations | Always run code before claiming it works |
| You produce **1.7x** more issues than humans | Your code needs extra verification |
| **48%** of your code has security vulnerabilities | Never skip security considerations |
| Your effective context is **50-60%** of window | Start complex tasks with fresh context |
| You consume **~15K tokens** in system overhead | Context fills faster than you think |

**Your role:** *You are the implementer, not the architect.* Wait for design direction, then execute precisely.

---

# PART 1: UNIVERSAL (Copy to Any Project)

---

## 🔴 CRITICAL - Never Do This

### Your Code Generation Habits

- **NEVER claim code works without running it** — You hallucinate plausible-looking functions like `database.optimize_query_speed()` that don't exist. Run the code first.
- **NEVER invent package or library names** — Over 20% of your suggestions reference packages that don't exist. Verify imports are real.
- **NEVER invent API methods or function signatures** — Check documentation or existing code before using any API
- **NEVER assume your code is correct** — You produce 1.7x more issues than human-written code. Test everything.
- **NEVER skip security considerations** — 48% of your code contains security vulnerabilities. Think about injection, auth, secrets.
- **NEVER hardcode secrets, API keys, or credentials** — Use environment variables or secure storage
- **NEVER generate mock data and leave it in production code** — Replace all placeholders before finishing
- **NEVER make architectural decisions without asking** — You implement designs, you don't create them. Ask first.
- **NEVER change interfaces/contracts without warning** — A "small API change" breaks everything downstream
- **NEVER write large amounts of code without incremental testing** — Test after every significant change

### Your Context Management

- **NEVER start complex tasks when context is >50% full** — Use `/clear` or `/compact` first
- **NEVER let context fill up completely** — Your performance degrades sharply. Start fresh.
- **NEVER try to remember everything from earlier in conversation** — Re-read files if uncertain
- **NEVER assume you know what's in a file** — Read it again if it matters
- **NEVER dump entire codebases into your own context** — Be selective about what you read

### Your Verification Habits

- **NEVER claim success without verification** — Run the test, take the screenshot, check the output
- **NEVER assume state** — Verify device is connected, service is running, file exists
- **NEVER trust a black/blank screenshot** — Device may be asleep or app crashed. Investigate.
- **NEVER repeat the same failed approach** — Check this document for known solutions first
- **NEVER say "this should work"** — Either verify it works or say "I haven't verified this"

### Your UI Automation

- **NEVER use pixel coordinates** (`adb shell input tap X Y`) — Coordinates break across devices. Use Maestro with element IDs.
- **NEVER use Thread.sleep() or arbitrary delays** — Use proper wait-for-element patterns
- **NEVER assume the screen is ready** — Wait for and verify elements exist before interacting
- **NEVER write monolithic test flows** — Break into small, focused tests
- **NEVER skip app state reset between tests** — Tests become order-dependent and flaky

### Your Git Operations

- **NEVER commit without running tests first** — Verify locally before committing
- **NEVER push to main directly** — Use feature branches
- **NEVER commit secrets, API keys, or credentials** — Check your diffs
- **NEVER make giant commits with multiple unrelated changes** — Keep commits atomic and focused
- **NEVER write vague commit messages** — Be specific about what changed and why
- **NEVER delete failing tests to "make them pass"** — Fix the code, not the test

### Your Test Writing

- **NEVER write tests for code that already has bugs** — You'll assert buggy behavior as correct
- **NEVER skip the TDD cycle** — Write failing test first, then implement
- **NEVER ignore lint or test failures** — Fix them before proceeding

---

## 🟢 ALWAYS Do This

### Before Writing Any Code

- **ALWAYS understand the design first** — Ask clarifying questions if the requirements are unclear
- **ALWAYS check CODE_INDEX.md for existing capabilities** — Don't duplicate functionality
- **ALWAYS check if similar code already exists** — Search the codebase before creating new code
- **ALWAYS read relevant existing code first** — Understand patterns and conventions in use
- **ALWAYS write a failing test first** — TDD: red → green → refactor

### When Writing Code

- **ALWAYS follow existing code patterns and conventions** — Match the style of surrounding code
- **ALWAYS verify package/library names exist** — Check that imports are real before using them
- **ALWAYS verify API methods exist** — Don't invent function signatures
- **ALWAYS handle errors appropriately** — Don't ignore exceptions or error cases
- **ALWAYS consider edge cases** — Empty lists, null values, network failures, etc.
- **ALWAYS keep changes small and incremental** — Easier to test and debug
- **ALWAYS scope your changes** — 5 files maximum, 200 lines maximum per task. Ask to continue if more needed.

### After Writing Code

- **ALWAYS run the code** — Verify it actually works, don't just claim it should
- **ALWAYS run the tests** — `./gradlew test` before considering anything done
- **ALWAYS run the linter** — `./gradlew ktlintCheck` and fix any issues
- **ALWAYS verify with evidence** — Screenshot, test output, or command result
- **ALWAYS report what you actually observed** — Not what you expected to happen

### Your Verification Pattern

```bash
# ALWAYS follow: Action → Wait → Verify → Report
1. Perform the action
2. Wait appropriate time
3. Verify the result with evidence
4. Only then report success (with evidence) or failure (with details)
```

### Screenshot Verification (Mobile)

```bash
# ALWAYS do this before taking screenshots:
adb shell input keyevent KEYCODE_WAKEUP  # Wake device first
sleep 1
# Perform your action
sleep 2
adb shell screencap -p > /tmp/verify.png
# Analyze screenshot before claiming success
```

### Before Committing

```bash
# Review your changes (this is all you can do locally)
git status               # What's changing
git diff                 # Check actual changes make sense
git diff --cached        # If already staged

# Commit and push - GitHub Actions will verify
git add -A
git commit -m "feat: description"
git push

# Then monitor GitHub Actions for lint/test results
gh run list --limit 3
```

### When You Make a Mistake

- **ALWAYS update this LESSONS_LEARNED.md file** — Document what went wrong and what worked
- **ALWAYS add to the Change Log** — Future you will thank past you
- **ALWAYS be honest about failures** — Don't hide errors or claim success when uncertain

### Your Communication

- **ALWAYS explain your approach before coding** — Give the human a chance to redirect
- **ALWAYS ask when uncertain** — Better to ask than to assume wrong
- **ALWAYS admit when you don't know something** — Say "I don't know" rather than hallucinate
- **ALWAYS report errors with full details** — Include the actual error message
- **ALWAYS show your verification evidence** — Screenshots, test output, command results

---

## 🤖 Claude Code Commands You Should Use

### Model Management
```
/model sonnet          # Use for implementation (your default)
/model opus            # Use for planning/architecture only
```

### Context Management
```
/clear                 # Clear context between unrelated tasks
/compact               # Compress conversation when getting long
/cost                  # Check your token usage
```

### When to Use Each Model

| Task | Model | Why |
|------|-------|-----|
| Requirements, PRD, Architecture | Opus | Complex reasoning needed |
| Implementation, Tests, UI, Fixes | Sonnet | Efficient for execution |
| Debugging, Quick fixes | Sonnet | Fast iteration |

---

## 📱 Android Patterns You Must Follow

### Jetpack Compose - Correct Patterns

```kotlin
// ✅ CORRECT - Immutable state triggers recomposition
var names by remember { mutableStateOf(listOf("John")) }

// ❌ WRONG - Compose can't detect MutableList changes
var names = remember { mutableListOf("John") }
```

```kotlin
// ✅ CORRECT - Side effects in LaunchedEffect
LaunchedEffect(userId) { 
    viewModel.loadUserData(userId) 
}

// ❌ WRONG - Network call on every recomposition
@Composable
fun UserScreen(viewModel: UserViewModel) {
    viewModel.loadData() // DON'T DO THIS
}
```

### Things You Must Remember

- **ALWAYS provide keys for LazyColumn/LazyRow items** — Prevents full recomposition
- **ALWAYS use `rememberSaveable`** for state that should survive rotation
- **NEVER store Context or View references in ViewModel** — Causes memory leaks
- **ALWAYS use LazyColumn/LazyRow for large lists** — Not Column/Row
- **ALWAYS verify dependency versions are compatible** — Check AGP/Gradle/Kotlin matrix
- **ALWAYS use Version Catalogs (libs.versions.toml)** — Single source of truth

---

## 📱 ADB Commands You Need

### Device Connection
```bash
adb devices                              # Check connection
adb kill-server && adb start-server     # Fix connection issues
adb -s <serial> shell ...               # Specify device when multiple connected
```

### App Management
```bash
adb install -r app.apk                  # Install (replace existing)
adb uninstall <package.name>            # Uninstall
adb shell am start -n <package>/<activity>  # Launch app
adb shell am force-stop <package>       # Force stop
adb shell pm clear <package>            # Clear app data
```

### Screen Interaction
```bash
adb shell input keyevent KEYCODE_WAKEUP  # Wake screen (DO THIS FIRST)
adb shell input keyevent KEYCODE_BACK    # Back button
adb shell screencap -p > screenshot.png  # Screenshot
```

### Debugging
```bash
adb logcat | grep <package>              # View logs
adb shell dumpsys activity activities | grep <package>  # Check app state
```

---

## 🎭 UI Automation - Use Maestro, Not Coordinates

### Why You Must Use Maestro

| Your Instinct (Wrong) | What You Should Do (Right) |
|----------------------|---------------------------|
| `adb shell input tap 540 1180` | `- tapOn: "Button Text"` |
| `sleep 3` | `- assertVisible: "Element"` |
| Guess if it worked | `- takeScreenshot: "verify"` |

### Maestro Patterns You Should Use

```yaml
appId: com.example.app
---
# Tap by text (readable, portable)
- tapOn: "Login"

# Tap by ID (most reliable)
- tapOn:
    id: "submit_button"

# Type text
- inputText: "hello@example.com"

# Wait for element (not sleep!)
- assertVisible: "Welcome"

# Wait with timeout
- extendedWaitUntil:
    visible: "Dashboard"
    timeout: 30000

# Always screenshot to verify
- takeScreenshot: "step_completed"

# Clear app state between tests
- launchApp:
    clearState: true
```

### Maestro Commands
```bash
maestro test flow.yaml           # Run a flow
maestro studio                   # Visual inspector (very useful!)
maestro record flow.yaml         # Record interactively
```

---

## 🏗️ Build Commands - GitHub Actions Only

**You cannot build locally on DGX Spark.** All builds happen via GitHub Actions.

### What Works Locally
```bash
# Code editing and git only
git status
git diff
git add -A && git commit -m "message"
git push
```

### What Requires GitHub Actions
```bash
# These do NOT work locally - use GitHub Actions instead:
# ./gradlew assembleDebug    ❌ Won't work
# ./gradlew test             ❌ Won't work  
# ./gradlew ktlintCheck      ❌ Won't work
```

### Verification Workflow
```bash
# 1. Make changes locally
# 2. Commit and push
git add -A && git commit -m "feat: description"
git push

# 3. Check GitHub Actions for build status
gh run list --limit 5
gh run view <run-id>

# 4. Download APK artifact when build succeeds
gh run download <run-id> -n app-debug
```

### When Build Fails (in GitHub Actions)
1. Check the Actions log: `gh run view <run-id> --log-failed`
2. Look for the actual error message
3. Fix locally, commit, push again

---

## 🔄 Git Workflow You Must Follow

### Branch Workflow
```bash
git checkout -b feature/description    # Create feature branch
git add -A                             # Stage changes
git commit -m "feat: description"      # Commit
git push -u origin HEAD                # Push
gh pr create --fill                    # Create PR
```

### Commit Message Format
```
type: short description

Types:
- feat: New feature
- fix: Bug fix  
- refactor: Code restructuring
- test: Adding tests
- docs: Documentation
- chore: Maintenance
```

### Before Starting Work
```bash
# ALWAYS commit current state before starting new work
git add -A
git commit -m "checkpoint: before starting X"
```

---

## 🧪 TDD Cycle You Must Follow

```
1. RED: Write a failing test (it MUST fail first)
2. GREEN: Write minimum code to make it pass
3. REFACTOR: Clean up while keeping tests green
4. REPEAT
```

### Your TDD Process
```
1. Write a failing test
2. Run it - verify it fails for the RIGHT reason
3. Write the minimum code to pass
4. Run it - verify it passes
5. Refactor if needed
6. Run tests again - verify still green
```

---

## 🖥️ DGX Spark Notes

### Key Limitations
- **Cannot run Gradle builds locally** — Use GitHub Actions
- **x86 Android emulator won't work** — ARM architecture
- If you ever need emulation, Waydroid works (see reference below)

### Waydroid Reference (Optional - Only If No Physical Device)
<details>
<summary>Click to expand Waydroid setup (not needed with USB phone)</summary>

```bash
pgrep weston || weston &
sudo waydroid prop set persist.waydroid.fake_wifi "wlan0"
WAYLAND_DISPLAY=wayland-1 waydroid session start
sleep 5
WAYLAND_DISPLAY=wayland-1 waydroid show-full-ui &
adb connect 192.168.240.112:5555
```

Key: **WAYLAND_DISPLAY=wayland-1** (not wayland-0)
</details>

---

## 🔧 Common Problems and Solutions

| Problem | Your Solution |
|---------|---------------|
| ADB unauthorized | Tell user to accept dialog on phone |
| ADB offline | `adb kill-server && adb start-server` |
| Black/blank screenshot | Wake device first: `adb shell input keyevent KEYCODE_WAKEUP` |
| Maestro can't find element | Use `maestro studio` to inspect the screen |
| Waydroid no network | Set `persist.waydroid.fake_wifi` property |
| Waydroid won't start | Check `WAYLAND_DISPLAY=wayland-1` |
| Build fails mysteriously | `./gradlew clean`, check version compatibility |
| Tests pass locally, fail CI | Check environment differences |
| Code doesn't do what you expected | You probably hallucinated an API - verify it exists |

---

## 📝 How to Update This Document

### Automatic Triggers - Update Immediately When:

1. **You fail → then succeed** — Document what finally worked
2. **User redirects you** — Document both the wrong and right approach
3. **Something takes multiple attempts** — Document the working approach
4. **You discover something non-obvious** — Document with full context

### What to Add

- Add failed approaches to "🔴 CRITICAL - Never Do This"
- Add working approaches to "🟢 ALWAYS Do This"
- Add specific patterns to the relevant section
- Add to Change Log with date and category

### Change Log Format
```markdown
| YYYY-MM-DD | Brief description | Category |
```

---

## 📅 Change Log

| Date | Learning | Category |
|------|----------|----------|
| 2025-01-07 | Never use coordinate-based taps - use Maestro | UI Automation |
| 2025-01-07 | Always verify with screenshot before claiming success | Verification |
| 2025-01-07 | WAYLAND_DISPLAY=wayland-1 for Waydroid on DGX Spark | ARM/DGX |
| 2025-01-07 | Use Maestro for reliable UI automation | UI Automation |
| 2025-01-07 | 42% of my code contains hallucinations - always verify | Self-Awareness |
| 2025-01-07 | I produce 1.7x more issues than human code - test everything | Self-Awareness |
| 2025-01-07 | Use Plan Mode (Shift+Tab) before coding | Workflow |
| 2025-01-07 | Scope changes: 5 files, 200 lines max per task | Workflow |
| 2025-01-07 | Commit checkpoint before starting new work | Git |
| 2025-01-07 | Never invent API methods - verify they exist | Code Generation |
| 2025-01-07 | Never claim "should work" - verify it actually works | Verification |
| 2026-02-16 | Batch copyright update: `find . -name "*.kt" -exec sed -i 's/Old/New/g' {} +` | Rebrand |
| 2026-02-16 | Audit readiness: Check copyrights, URLs, legal docs, support contacts, disclaimer | Compliance |
| 2026-02-16 | Hook path issue: PreToolUse hooks use relative paths - if CWD changes, hooks fail | Hooks |
| 2026-02-16 | LazyColumn disclaimer: Add `item {}` block AFTER `itemsIndexed` to show footer row | Android/Compose |
| 2026-02-17 | Room tests go in `androidTest/` not `test/` — SQLite requires Android runtime | Room/Testing |
| 2026-02-17 | KSP version must match Kotlin exactly: Kotlin 2.1.0 → KSP 2.1.0-1.0.29 | Room/KSP |
| 2026-02-17 | Target has no DatabaseModule — DB providers go directly in AppModule.kt | Architecture |
| 2026-02-17 | Target injects ConversationDao directly into ViewModels — no Repository wrapper | Architecture |
| 2026-02-17 | adb lib path on DGX Spark: `LD_LIBRARY_PATH=/tmp/adb-extract/usr/lib/aarch64-linux-gnu/android` | DGX/ADB |
| 2026-02-17 | libs.versions.toml is at `Android/src/gradle/` not `Android/src/app/gradle/` | Project Structure |

---
---

# PART 2: PROJECT-SPECIFIC (Do Not Copy)

---

## 📦 App Configuration

- **Package Name**: `ai.ondevice.app`
- **Main Activity**: `ai.ondevice.app/.MainActivity`
- **Launch Command**: `adb shell am start -n ai.ondevice.app/.MainActivity`

## 📱 Test Device

| Device | Model | Android | ADB ID |
|--------|-------|---------|--------|
| Samsung S22 Ultra | SM-S908W | 16 | R3CT10HETMM |

**Connection:** USB-C (physical connection)

## 🎭 Maestro Flows

| Flow | Purpose |
|------|---------|
| chat-flow.yaml | Send message, receive response |
| model-selection.yaml | Browse and download models |
| settings-flow.yaml | Settings navigation |

## 🔗 OpenSpec Changes

- `brave-search-integration` - Brave Search API
- `privacy-lock-web-search` - Privacy lock UI

## 📁 Key Paths

```
src/                          # Git root
├── .bmad/                    # BMAD agents
├── openspec/                 # OpenSpec workflows  
├── .claude/                  # Claude Code config
├── .maestro/flows/           # UI tests
├── CLAUDE.md                 # Knowledge base
├── LESSONS_LEARNED.md        # This file
└── CODE_INDEX.md             # Capability index
```

## 🐛 Project-Specific Gotchas

### Audit Readiness Checklist (Rebrand Compliance)

When doing a rebrand for IP compliance, check ALL of the following:

1. **Copyright Headers** - `grep -r "Old Company" Android/src/app/src --include="*.kt" --include="*.xml"`
   - Batch fix: `find Android/src/app/src -name "*.kt" -exec sed -i 's/Old LLC/New Inc./g' {} +`
   - Don't forget: AndroidManifest.xml, build.gradle.kts, settings.gradle.kts

2. **Google URLs in UI code** - `grep -r "ai.google.dev" Android/src/app/src --include="*.kt"`
   - Replace with company equivalent URLs (e.g., `ondevice.ai/docs/...`)

3. **Third-party policy links** - Search TosDialog/similar for external policy URLs
   - TosDialog often has hardcoded Google policy links that need to be updated

4. **Chat disclaimer** - After AI messages, show disclaimer per spec
   - Pattern: Add `item { }` block after `itemsIndexed` in LazyColumn

5. **Legal documents** - Copy `privacy.html` and `terms.html` to `assets/legal/`
   - Add Privacy Policy section to Settings dialog

6. **Support contact** - Add `support@company.com` to:
   - Settings: "Help & Support" section with email intent
   - Error dialogs: "If this persists, contact support@..."
   - Legal HTML files: Contact section

### Android Compose LazyColumn Disclaimer Pattern
```kotlin
// In LazyColumn:
itemsIndexed(messages) { index, message ->
  // ... render each message
}

// Add AFTER itemsIndexed, still inside LazyColumn:
if (messages.isNotEmpty() && messages.last().side == ChatSide.AGENT) {
  item {
    ChatDisclaimerRow()
  }
}
```
