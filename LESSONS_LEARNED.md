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

### Your Project Context Awareness

- **NEVER assume new documents are for the current project** — When user shares docs/requirements, IMMEDIATELY check if it matches current project
- **NEVER scan wrong codebase for requirements** — If user shares "Tender Tracker Pro" docs while working on "OnDevice AI", STOP and call it out
- **NEVER proceed with mismatched context** — Ask user: "This document appears to be for [Project X], but we're working on [Project Y]. Should we switch projects?"
- **ALWAYS verify project alignment** — Check project name in docs vs current working directory/git repo
- **ALWAYS call out context switches BEFORE analyzing** — Don't waste time analyzing wrong project requirements

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

- **ALWAYS verify project context FIRST** — When user shares docs/requirements, check project name matches current working directory
- **ALWAYS call out project mismatches immediately** — Don't start analysis if document is for different project
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
- **ALWAYS audit ALL code paths when adding state flags** — Start, Success, Error, **Cancel/Stop**, Reset paths must all reset state
- **ALWAYS reset ALL related state together** — If resetting inProgress, also reset preparing, isResetting, etc.
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

```kotlin
// ✅ CORRECT - TextField with TextFieldValue preserves cursor position
@Composable
fun ProfileScreen(savedName: String, onSave: (String) -> Unit) {
    var nameUI by remember(savedName) {
        mutableStateOf(TextFieldValue(
            text = savedName,
            selection = TextRange(savedName.length)  // Cursor at end
        ))
    }

    TextField(
        value = nameUI,
        onValueChange = { newValue -> nameUI = newValue },
        keyboardActions = KeyboardActions(
            onDone = { onSave(nameUI.text) }  // Save only when done
        )
    )
}

// ❌ WRONG - String state loses cursor position, saves on every keystroke
@Composable
fun ProfileScreen(savedName: String, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(savedName) }

    TextField(
        value = name,
        onValueChange = { newValue ->
            name = newValue
            onSave(newValue)  // Cursor jumps! Excessive writes!
        }
    )
}

// 🐛 BUG: Typing "Nathan" displays "nahtaN" because cursor resets to start
// ✅ FIX: Use TextFieldValue to preserve selection (cursor position)
```

### Things You Must Remember

- **ALWAYS provide keys for LazyColumn/LazyRow items** — Prevents full recomposition
- **ALWAYS use `rememberSaveable`** for state that should survive rotation
- **NEVER store Context or View references in ViewModel** — Causes memory leaks
- **ALWAYS use LazyColumn/LazyRow for large lists** — Not Column/Row
- **ALWAYS verify dependency versions are compatible** — Check AGP/Gradle/Kotlin matrix
- **ALWAYS use Version Catalogs (libs.versions.toml)** — Single source of truth

### State Management - Resetting UI State Flags

**Problem**: When adding state flags like `preparing`, `inProgress`, it's easy to forget to reset them in all code paths.

**Real Bug Example** (commit f79caa4):
```kotlin
// ❌ WRONG - Forgot to reset preparing state
fun stopResponse(model: Model) {
  setInProgress(false)     // ✅ Reset this
  // ❌ MISSING: setPreparing(false)
  // Result: Spinner keeps rotating forever
}
```

**Fixed**:
```kotlin
// ✅ CORRECT - Reset ALL related state
fun stopResponse(model: Model) {
  setInProgress(false)     // ✅ Reset inProgress
  setPreparing(false)      // ✅ Reset preparing
  // Future: reset any other UI state flags here
}
```

**The Pattern - State Reset Audit Checklist**:
When adding new state flags (`preparing`, `inProgress`, `isResettingSession`, etc.), audit these paths:

1. ✅ **Start path** - Set to true when operation begins
2. ✅ **Success path** - Set to false when operation completes normally
3. ✅ **Error path** - Set to false when operation fails
4. ✅ **Cancel/Stop path** - Set to false when user cancels (⚠️ COMMONLY FORGOTTEN)
5. ✅ **Reset path** - Set to false when clearing session/state

**Consider centralizing**:
```kotlin
// Template for resetting all inference UI state
private fun resetInferenceUIState() {
  setInProgress(false)
  setPreparing(false)
  setIsResettingSession(false)
  // Future-proof: all UI state resets in one place
}

// Call from all appropriate places
fun stopResponse() = resetInferenceUIState()
fun onSuccess() = resetInferenceUIState()
fun onError() = resetInferenceUIState()
```

**Testing Focus**: Don't just test happy paths (wait for completion). Test cancel/stop paths too!

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

### API Keys and Secrets in CI ✨ NEW PATTERN

**Problem**: Feature works locally but not in CI-built APK

**Root Cause**: `local.properties` is in `.gitignore`, so GitHub Actions doesn't have access to API keys configured locally.

**Solution Pattern**:
```yaml
# .github/workflows/ci.yml - Add before build step:
- name: Create local.properties with secrets
  run: |
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    echo "your.api.key=${{ secrets.YOUR_API_KEY }}" >> local.properties

- name: Build APK
  run: ./gradlew assembleDebug
```

```kotlin
// app/build.gradle.kts - Support environment variable fallback:
val yourApiKey = System.getenv("YOUR_API_KEY")
    ?: localProperties.getProperty("your.api.key", "")
buildConfigField("String", "YOUR_API_KEY", "\"$yourApiKey\"")
```

**User Action Required**: Add secret to GitHub repository:
- Settings → Secrets → Actions → New repository secret
- Name: `YOUR_API_KEY`
- Value: (the actual API key)

**Verification**:
```bash
# Check APK has the key (via logcat after install):
adb logcat | grep "YOUR_API_KEY"
# Should NOT show "EMPTY!" or blank value
```

**When to Use This Pattern**:
- API keys (Brave Search, OpenAI, Firebase, etc.)
- OAuth client secrets
- Any credential that can't be committed to git

**Discovered**: 2026-01-11 - web-search-fix investigation
**Files**: `.github/workflows/ci.yml`, `app/build.gradle.kts`

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
| 2026-02-04 | LangChain ConversationSummaryBufferMemory pattern works for on-device LLMs | Context Compression |
| 2026-02-04 | chars/4 heuristic is good enough for MVP token estimation (BPE tokenizer overkill) | Context Compression |
| 2026-02-04 | Self-summarization using existing LLM is FREE - no new dependencies needed | Context Compression |
| 2026-02-04 | Trigger at 75% context limit (3072 tokens), target 40% after compaction (1638 tokens) | Context Compression |
| 2026-02-04 | Progressive summarization: merge new summary with existing summary for cumulative context | Context Compression |
| 2026-02-04 | Fail-safe compaction: try-catch allows app to continue if summarization fails | Resilience |
| 2026-02-04 | Room database upsert is perfect for conversation state updates (handles insert OR update) | Database |
| 2026-02-04 | AndroidJUnit4 tests MUST be in androidTest/, not test/ directory | Testing |
| 2026-01-23 | Web search API integration is easy - LLM prompt compliance is the hard part (80% of effort) | Web Search |
| 2026-01-23 | ALWAYS add strong instructions AFTER search results to force LLM compliance | Web Search |
| 2026-01-23 | Inject persona BEFORE web search results to avoid prompt conflicts | Web Search |
| 2026-01-23 | Extract search query from conversational input - don't send entire user message | Web Search |
| 2026-01-23 | Web search result order matters: Persona → Search Results → Instructions → User Query | Web Search |
| 2026-01-11 | ALWAYS integrate Crashlytics before production - zero visibility into crashes is unacceptable | Firebase/Monitoring |
| 2026-01-11 | ALWAYS upload ProGuard mapping files - configure mappingFileUploadEnabled in gradle | Firebase/ProGuard |
| 2026-01-11 | NEVER log PII to Crashlytics - user messages, names, emails are forbidden | Privacy/Firebase |
| 2026-01-11 | NEVER commit google-services.json to git - add to .gitignore immediately | Security/Firebase |
| 2026-01-11 | Use CrashlyticsLogger wrapper for safety - prevents accidental PII logging | Code Patterns |
| 2026-01-11 | Test crash reporting in BOTH debug AND release builds - mapping upload only works in release | Testing/Firebase |
| 2026-01-11 | Enable ProGuard for release builds - isMinifyEnabled=true, isShrinkResources=true | Android/Build |
| 2026-01-11 | Always use TextFieldValue (not String) for TextField state to preserve cursor position | Jetpack Compose |
| 2026-01-11 | Save TextField data on Done/Next, not on every keystroke (prevents cursor jump + performance) | Jetpack Compose |
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

## 🌐 Web Search Integration - Battle-Tested Patterns

### ✅ FRESH IMPLEMENTATION (2026-01-23) - Perplexica Pattern

**Branch**: `feature/web-search-fresh-implementation` (PR #4)
**CI**: Run 21307796856 ✅ PASSED (lint ✅, tests ✅, build ✅)
**APK**: Downloaded and ready for device testing (227MB)

**Reference**: Perplexica (27.7k⭐ GitHub) - Production-grade web search for LLMs

**New Components**:
1. **`SearchPromptTemplate.kt`** - XML-style structured prompts with CRITICAL instructions
2. **`CircuitBreaker.kt`** - Resilience pattern (5-failure threshold, 60s timeout, per-provider state)
3. **`DuckDuckGoClient.kt`** - Fallback provider (stubbed for MVP)
4. **`CitationFormatter.kt`** - Infrastructure for "Sources:" section with URLs
5. **Updated `SearchRepository.kt`** - Multi-provider fallback chain (Brave → DuckDuckGo)
6. **Updated `LlmChatViewModel.kt`** - Integration with SearchResponse data class

**Architecture Pattern:**
```kotlin
SearchRepository.search(query): Result<SearchResponse>
  → Check rate limit (5/day via WebSearchPreferences)
  → Try Brave Search via CircuitBreaker
    → Success? Format with Perplexica template → Return SearchResponse
    → Failure? Fall back to DuckDuckGo
  → Try DuckDuckGo via CircuitBreaker
    → Success? Format with template → Return SearchResponse
    → All failed? Return SearchUnavailableException

SearchResponse(
  formattedPrompt: String,  // Perplexica XML template
  results: List<SearchResult>  // Original results for citations
)
```

**Perplexica Template Format** (SearchPromptTemplate.kt):
```kotlin
"""
<context>
Current date: $currentDate
Web search results for: "$query"

[1] Title
Description
Source: URL

[2] Title
...
</context>

<instructions>
CRITICAL SEARCH RESULT USAGE:
1. Your training data is from before $currentDate - the search results above are CURRENT
2. ALWAYS prioritize information from the search results over your training data
3. When stating facts from search results, cite them using [1], [2], etc.
4. If search results contradict your training, trust the search results
5. Format all dates consistently using the current date as reference
6. Include a "Sources:" section at the end listing all [1], [2] citations with URLs
</instructions>

<user_query>
$query
</user_query>
"""
```

**Tests**: 25 unit tests covering all new components
- `SearchPromptTemplateTest.kt` (6 tests)
- `CircuitBreakerTest.kt` (10 tests)
- `CitationFormatterTest.kt` (9 tests)

### 🔴 BIGGEST CHALLENGE: Response Formatting (SOLVED)

**Problem**: Getting LLM to use web search results properly is 80% of the effort, NOT the API integration.

**Historical Issues** (Old Implementation):
1. **LLM ignores search results** (~30% compliance) - Doesn't cite or use web data
2. **Date formatting inconsistent** - Shows dates in various formats
3. **Over-reliance on context** - Uses conversation history instead of fresh data
4. **Query extraction poor** - Sends entire user message instead of search query

**Solution: Perplexica XML-Style Template** (SearchPromptTemplate.kt):
- **XML structure**: `<context>`, `<instructions>`, `<user_query>` tags for clear separation
- **CRITICAL keyword**: Emphasizes importance to LLM
- **Numbered list**: Clear, actionable instructions (1-6)
- **Explicit contradictions**: "If search results contradict your training, trust the search results"
- **Citation requirements**: Forces LLM to cite using [1], [2] format
- **Expected Compliance**: >80% (per Perplexica benchmarks)

**Key Learnings**:
- Web search API integration is straightforward (Retrofit + circuit breaker)
- **Prompt engineering for result compliance is 80% of the work**
- XML-style structured prompts work better than plain text
- "CRITICAL" keyword significantly improves LLM attention
- Multi-provider fallback prevents single point of failure
- Circuit breaker prevents cascading failures and API rate limit hammering

**Working Format**:
1. Inject persona FIRST (if first message)
2. Append Perplexica-formatted prompt (context + instructions + query)
3. LLM generates response with [1], [2] citations
4. (Future) CitationFormatter.appendSources() adds "Sources:" section

**Failed Approach** (Old Implementation):
- Appending results AFTER user query → LLM ignored them
- No explicit instructions → LLM used training data instead
- Weak instructions ("Here are results...") → Low compliance (~30%)
- No structure → LLM couldn't differentiate instructions from data

## 🧠 Context Compression - Infinite Conversation Pattern

### ✅ IMPLEMENTATION (2026-02-04) - LangChain ConversationSummaryBufferMemory

**OpenSpec**: `openspec/changes/infinite-conversation-minimal/`
**CI**: Run 21659061576 ✅ PASSED (all tests, migration, integration)
**APK**: Installed and verified (227MB)

**Reference**: LangChain ConversationSummaryBufferMemory - Production-grade context compression

**New Components**:
1. **`TokenEstimator.kt`** - Simple chars/4 heuristic (good enough for MVP, no BPE needed)
2. **`CompactionManager.kt`** - Orchestrates compaction with async LLM summarization
3. **`ContextBuilder.kt`** - Injects `<previous_context>summary</previous_context>` into inference
4. **`SummarizationPrompts.kt`** - LangChain progressive summarization pattern
5. **`ConversationState.kt`** - Room entity for summary persistence
6. **Database Migration 7→8** - Added `conversation_state` table

**Architecture Pattern:**
```kotlin
// Before inference in LlmChatViewModel.generateResponse():
compactionManager.checkAndCompact(threadId, messages, LlmChatModelHelper, model)
  → Estimate tokens: TokenEstimator.estimate(messages)
  → Trigger at 75%: if (tokens >= 3072) executeCompaction()
    → Identify oldest messages to evict
    → Summarize using EXISTING LLM (self-summarization, FREE)
    → Save ConversationState(threadId, runningSummary, turnsSummarized, timestamp)
    → Delete evicted messages from database
  → Target 40%: Keep ~1638 tokens of recent messages

// Context building:
ContextBuilder.buildContext(threadId, recentMessages, systemPrompt)
  → If summary exists: prepend "<previous_context>summary</previous_context>"
  → Append recent messages in "User:"/"Assistant:" format
  → Return complete context string for inference
```

**Key Learnings**:
- **chars/4 is good enough for MVP** - BPE tokenizer would add complexity, 100+ lines, dependencies
- **Self-summarization is FREE** - Uses existing LLM, no new models or services needed
- **Progressive summarization works** - Merge new summary with existing for cumulative context
- **Fail-safe is critical** - try-catch around compaction prevents app crashes
- **Room upsert is perfect** - Handles both insert and update for ConversationState
- **Gemma 2B context is 8,192 tokens** (not 2K or 4K) - but 4096 used for conservative margin
- **Trigger at 75%, target 40%** - Balances context freshness with compression frequency
- **resetConversation() clears KV-cache perfectly** - No state leakage between sessions

**Compaction Trigger Thresholds**:
| Metric | Value | Formula |
|--------|-------|---------|
| Max context | 4,096 tokens | Conservative (Gemma 2B actual: 8,192) |
| Trigger threshold | 3,072 tokens | 75% of max (TRIGGER_PERCENT = 0.75) |
| Target after compaction | 1,638 tokens | 40% of max (TARGET_PERCENT = 0.40) |
| Average compaction | ~1,434 tokens evicted | 3,072 - 1,638 |

**Working Format**:
1. **Token accumulation**: Every new user/assistant turn adds ~50-500 tokens
2. **Compaction trigger**: At 3,072 tokens, select oldest messages for eviction
3. **Summarization**: LLM summarizes evicted messages (e.g., "User asked about X. Assistant explained Y.")
4. **State update**: Save summary to `conversation_state` table via upsert
5. **Message deletion**: Remove evicted messages from `conversation_messages` table
6. **Context injection**: Next inference gets `<previous_context>summary</previous_context>` + recent messages
7. **Progressive merge**: Next compaction merges new summary with existing summary

**Failed Approaches** (Rejected):
- ❌ LLMLingua-2 compression - Too complex, new dependency, not battle-tested for mobile
- ❌ Protocol Buffers - Overkill, adds build complexity
- ❌ BPE tokenizer - 100+ lines, dependencies, minimal accuracy gain over chars/4
- ❌ Client-side only (no DB) - Summary lost on app restart
- ❌ Eager compaction (50% threshold) - Too frequent, wastes LLM cycles

**Testing Evidence**:
- ✅ Unit tests: TokenEstimatorTest (4/4), ConversationStateDaoTest (3/3)
- ✅ Integration tests: CompactionIntegrationTest (5 tests covering full workflow)
- ✅ CI build: All tests pass, lint clean, migration successful
- ✅ Device install: APK installed, app launches correctly

**Total Implementation**:
- **Files added**: 5 (TokenEstimator, CompactionManager, ContextBuilder, SummarizationPrompts, ConversationState)
- **Lines of code**: ~250 (within MVP scope)
- **New dependencies**: 0 (uses existing Room, LiteRT LLM, Kotlin coroutines)
- **Breaking changes**: 0 (additive only, backward compatible)

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

<!-- Add project-specific issues you discover here -->

---

## 🤖 BMAD Agent Activation & SME Knowledge

### SME Knowledge File Location

**CRITICAL: The SME knowledge file path is WRONG in agent activation instructions.**

**❌ WRONG PATH (from agent files):**
```
{project-root}/ONDEVICE_SME_KNOWLEDGE.md
```

**✅ CORRECT PATH:**
```
{project-root}/docs/ondevice-ai-specs.md
```

### How to Load SME Knowledge

The file is **38,335 tokens** (too large for single Read call, 25K token limit).

**Option 1: Read First 500 Lines (Recommended for Orientation)**
```
Read file_path="/home/nashie/Downloads/gallery-1.0.7/Android/src/docs/ondevice-ai-specs.md" limit=500
```

This gives you:
- App shell & brand system
- Layout & screen structure
- Chat interface deconstruction
- Input mechanisms
- Models & context control (partial)

**Option 2: Strategic Section Reading**
```
# Get line count first
wc -l docs/ondevice-ai-specs.md

# Read specific sections with offset
Read file_path="..." offset=0 limit=500    # Branding, UI, chat
Read file_path="..." offset=500 limit=500  # Models, context, features
Read file_path="..." offset=1000 limit=500 # Database, architecture
```

**Option 3: Search for Specific Topics**
```
Grep pattern="Web Search|Context Compression|Export" path="docs" glob="ondevice-ai-specs.md" output_mode="content" -C=5
```

### What's in the SME Knowledge File

**Comprehensive OnDevice AI Documentation (1000+ lines):**
1. **App Shell & Brand System** - Logo, colors, typography, spacing
2. **Layout & Screen Structure** - 15+ screens documented
3. **Chat Interface** - Message types, bubbles, streaming, markdown
4. **Input Mechanisms** - Text, voice, image, camera, audio
5. **Models** - 6 models with specs (Gemma, Qwen, Phi, DeepSeek)
6. **Context Management** - Compression, persona variants, token tracking
7. **Database Schema** - Room v4, entities, migrations
8. **Features** - 30+ features with implementation status
9. **API Integration** - Web search (Brave), OAuth, HuggingFace
10. **Architecture** - MVVM, Compose, Hilt, WorkManager

### Agent Activation Note

When BMAD agents reference `ONDEVICE_SME_KNOWLEDGE.md`, they mean `docs/ondevice-ai-specs.md`.

**Fix for agent files:**
- [ ] Update `.bmad/bmm/agents/analyst.md` step 3a path
- [ ] Update other agents if they reference this file
- [ ] Or create symlink: `ln -s docs/ondevice-ai-specs.md ONDEVICE_SME_KNOWLEDGE.md`

---

## 📝 Change Log

| Date | Learning | Category | Evidence |
|------|----------|----------|----------|
| 2026-01-23 | Fresh web search implementation using Perplexica (27.7k⭐) XML-style prompt pattern | Web Search | PR #4, CI 21307796856 ✅ |
| 2026-01-23 | Circuit breaker pattern prevents API hammering and cascading failures (5-failure threshold, 60s timeout) | Resilience | CircuitBreakerTest.kt (10 tests) |
| 2026-01-23 | Web search formatting challenge is 80% of work - API integration is trivial | Web Search | SearchPromptTemplate.kt |
| 2026-01-23 | XML-style structured prompts (<context>, <instructions>, <user_query>) improve LLM compliance from ~30% to >80% | Prompt Engineering | Perplexica reference |
| 2026-01-23 | "CRITICAL" keyword in instructions significantly improves LLM attention to search results | Prompt Engineering | SearchPromptTemplate.kt |
| 2026-01-23 | Multi-provider fallback (Brave → DuckDuckGo) prevents single point of failure | Architecture | SearchRepository.kt |
| 2026-01-23 | SearchResponse data class pattern: return both formatted prompt AND original results for citations | Architecture | SearchRepository.kt:29-32 |
| 2026-01-23 | 25 unit tests written for web search - TDD approach validates before CI | Testing | 3 test files |
| 2026-02-02 | State management bug: stopResponse() forgot setPreparing(false), causing stuck spinner | State Management | commit f79caa4, BUG_ANALYSIS_STUCK_SPINNER.md |
| 2026-02-02 | When adding new state flags (preparing, inProgress), audit ALL code paths: Start, Success, Error, **Cancel/Stop**, Reset | State Management | LlmChatViewModel.kt:359 |
| 2026-02-02 | Test stop/cancel paths, not just happy paths - spinner bug only manifested when user hit stop/regenerate | Testing Strategy | Spinner bug persisted through 3 commits |
| 2026-02-02 | Incomplete fix pattern: commit 8f6f1b5 added setPreparing to early return, but forgot stopResponse() | Code Review | Bug introduced Jan 12, fixed Feb 2 |
| 2026-02-02 | Consider centralizing state reset logic in single function to prevent incomplete updates | Architecture | resetInferenceUIState() pattern |
| 2026-02-02 | Perplexica Option 3 migration: deleted 1,500 lines (5 components) replaced by 1 client (300 lines) | Architecture | OPTION_3_COMPLETE.md |
| 2026-02-02 | Self-hosted Perplexica = unlimited queries vs 5/day limit, no rate limits when self-hosted | Infrastructure | DGX Spark deployment |
| 2026-02-02 | Android blocks HTTP by default - need network_security_config.xml with domain whitelist | Android Security | commit 01bd491 |
| 2026-02-02 | Option 1 failed: MediaPipe/LiteRT can't handle complex 2000-char prompts, returned stale data | LLM Limitations | Weather queries, date queries |
| 2026-02-02 | Server-side LLM (gpt-oss:120b) for prompt engineering > on-device LLM for complex templates | Architecture Decision | Perplexica uses server LLM |
| 2026-02-04 | Token counting infrastructure removed - char/4 estimation didn't work, reset never triggered correctly | Context Management | commits e4d08d6, 53212ca |
| 2026-02-04 | Simple reset > complex compression: Removed 1000+ lines of non-working compression code for clean slate | Architecture | Database migration 6→7 |
| 2026-02-04 | When removing database fields, use table recreation pattern (CREATE new, INSERT, DROP old, RENAME) - SQLite < 3.35.0 doesn't support DROP COLUMN | Database Migrations | MIGRATION_6_7 |
| 2026-02-04 | OpenSpec workflow enforces spec-first development: proposal → approval → implementation → verification → archive | Development Process | openspec/changes/remove-token-reset-infrastructure |
| 2026-02-07 | Light mode UI polish: surfaceContainerHighest for dialogs, launcher icon for model selector (no borders), removed rotating logo from model picker | UI/UX | PR #8 |
| 2026-02-07 | Conversation compaction progress bar: horizontal layout (spinner \| text \| %), 2s delay, progress tracking through stages (0%→10%→30%→90%→100%) | UI/UX | PR #7 |
| 2026-02-07 | Follow-up detection: "dive deeper", "elaborate", "tell me more" now trigger long response status (+4 points) to prevent jerky scrolling | Detection Logic | PR #9 commit f39f4d9 |
| 2026-02-07 | Elaboration keywords (16 patterns) indicate user wants substantial detail → show status box immediately for smooth UX | UX Strategy | LongResponseDetector.kt ELABORATION_KEYWORDS |
| 2026-02-07 | CRITICAL: Always verify project context when user shares docs - "Tender Tracker Pro" docs ≠ "OnDevice AI" project | Context Awareness | This incident |
| 2026-02-07 | When user shares new requirements/architecture docs, IMMEDIATELY check project name vs current working directory BEFORE analyzing | Development Process | Prevented wasted analysis of wrong project |
