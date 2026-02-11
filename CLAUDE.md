# OnDevice AI - Claude Code Knowledge Base

---

## 🚨 MANDATORY EXECUTION PROTOCOL - READ THIS FIRST

**For EVERY feature/task, follow this protocol. NO EXCEPTIONS.**

### Phase 0: PROVE You Read This
Before doing anything else, quote this rule in your response:
> "I must create an OpenSpec proposal and STOP for approval before writing any implementation code."

---

## 🔥 CRITICAL: Copy Battle-Tested Implementations

**NEVER reinvent the wheel. NEVER over-engineer. ALWAYS copy proven solutions.**

### Mandatory Approach for ALL Features

When implementing ANY feature:

1. **Find the battle-tested implementation** (GitHub stars >10k preferred)
2. **Copy it with MINIMAL changes** - only what's needed to integrate with our project
3. **DO NOT "improve" or "simplify"** - if it works for 10k+ users, it works
4. **DO NOT create your own version** - you will fail (42% hallucination rate)

### Examples

**✅ CORRECT Approach:**
- User: "Implement web search"
- You: Find Perplexica (27.7k stars) → Copy their prompt template EXACTLY → Port TypeScript to Kotlin → Keep all 2,000 chars of instructions

**❌ WRONG Approach:**
- User: "Implement web search"
- You: "I'll create an XML-style template inspired by Perplexica" → You reduce 2,000 chars to 350 chars → You add your own tags → It fails

### Critical Rules

1. **Copy EXACTLY** - Don't "improve", don't "simplify", don't "adapt"
2. **Minimal changes** - Only change language syntax (TypeScript → Kotlin), package names, imports
3. **Keep ALL logic** - If they have 16 bullet points, you have 16 bullet points
4. **Keep ALL text** - If their prompt is 2,000 chars, yours is 2,000 chars
5. **Trust the stars** - 27.7k stars means it works, your "improvement" doesn't

### When You're Tempted to Change Something

**STOP. Ask yourself:**
- "Do I have 27.7k stars on my implementation?" → NO
- "Have 44 contributors validated my approach?" → NO
- "Has my version been tested by thousands of users?" → NO

**Then COPY the proven version.**

### Exceptions (Rare)

Only deviate from battle-tested implementations when:
- Legal compliance requires it (licenses, patents)
- Platform constraints force it (Android vs. web, mobile vs. desktop)
- Security vulnerabilities in the original (verify first!)

**Document ALL deviations in proposal.md with justification.**

---

## 📋 OpenSpec-Driven Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│  PHASE 1: PROPOSAL                                                  │
│  /openspec-proposal <feature>                                       │
│     └── Creates: proposal.md, tasks.md, spec-delta.md              │
│     └── Defines: scope, acceptance criteria, implementation plan   │
│                                                                     │
│  ⛔ STOP - Wait for user approval of specs                         │
│                                                                     │
│  PHASE 2: APPLY                                                     │
│  /openspec-apply <change-name>                                      │
│     └── TDD RED: Write failing tests                               │
│     └── TDD GREEN: Implement to pass                               │
│     └── 🔄 TEST LOOP: Until all tests pass                         │
│     └── 🔄 CI LOOP: Until CI green                                 │
│     └── 🔄 VISUAL LOOP: Until UI verified                          │
│     └── Check off tasks in tasks.md                                │
│                                                                     │
│  PHASE 3: ARCHIVE                                                   │
│  /openspec-archive <change-name>                                    │
│     └── Verify all tasks complete                                  │
│     └── Merge specs to openspec/specs/                             │
│     └── Move to archive/                                           │
│     └── Update LESSONS_LEARNED.md                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

### Phase 1: PROPOSAL (Create Specs FIRST)

**Before ANY code, create the specification:**

```bash
/openspec-proposal <feature-description>
```

This creates:
```
openspec/changes/<feature-slug>/
├── proposal.md      # What, why, acceptance criteria
├── tasks.md         # Implementation checklist
└── spec-delta.md    # Spec changes (ADDED/MODIFIED/REMOVED)
```

**Include in proposal.md:**
- Summary (one line)
- Motivation (why needed)
- Scope (what's in, what's out)
- Acceptance Criteria (checkboxes)
- Technical Approach

### ⛔ STOP HERE - Wait for Spec Approval

Say: **"Here's the proposal for [feature]. Please review and say 'approved' to proceed with implementation."**

**DO NOT write ANY implementation code until user approves the specs.**

---

### Phase 2: APPLY (Implement According to Specs)

Once approved:
```bash
/openspec-apply <change-name>
```

Execute autonomously with three loops:

```
APPLY PHASE (Autonomous)
         │
         ├─ Read approved specs from openspec/changes/<name>/
         │
         ▼
PHASE 2a: TDD RED - Write failing test
         │
         ▼
PHASE 2b: TDD GREEN - Implement
         │
         ├─🔄 TEST LOOP: Test fails? → Fix → Rerun → Repeat until PASS
         │
         ▼
PHASE 2c: BUILD & DEPLOY
         │
         ├─ Commit: "feat(<change-name>): <description>"
         ├─🔄 CI LOOP: CI fails? → Fix → Push → Repeat until GREEN
         ├─ Download APK: gh run download <id> -n app-debug
         └─ Install: adb install -r app-debug.apk
         │
         ▼
PHASE 2d: VISUAL VERIFICATION
         │
         ├─ Screenshot: adb exec-out screencap -p > feature.png
         ├─ Verify against acceptance criteria from proposal.md
         ├─🔄 VISUAL LOOP: Wrong? → Fix → Rebuild → Reinstall → Repeat until CORRECT
         │
         ▼
PHASE 2e: CHECK OFF TASK
         │
         └─ Update tasks.md: [ ] → [x]
         │
         ▼
         (Repeat for each task in tasks.md)
```

### Spec Adherence During Apply

**After EVERY code change, verify:**
- [ ] Does this match spec-delta.md exactly?
- [ ] Does this satisfy acceptance criteria in proposal.md?
- [ ] Am I within scope defined in proposal.md?

**If deviating from spec:** STOP and ask user before proceeding.

---

### Phase 3: ARCHIVE (Complete the Change)

When ALL tasks are checked:
```bash
/openspec-archive <change-name>
```

This:
1. Validates all tasks complete
2. Merges spec-delta.md → openspec/specs/
3. Moves to openspec/archive/<change-name>/
4. Updates LESSONS_LEARNED.md

---

### Three Loops - KEEP GOING Until Success

| Loop | When | Action |
|------|------|--------|
| 🔄 TEST LOOP | Test fails | Fix code → Run test → Repeat until PASS |
| 🔄 CI LOOP | CI fails | `gh run view --log-failed` → Fix → Push → Repeat until GREEN |
| 🔄 VISUAL LOOP | Screenshot wrong | Fix UI → Rebuild → Reinstall → Verify → Repeat until CORRECT |

### Critical Rules

1. **SPEC FIRST**: Always create OpenSpec proposal before any code
2. **ONE STOP**: Only stop after Phase 1 (proposal). Then run to completion.
3. **SPEC ADHERENCE**: Every change must match the approved spec
4. **NEVER GIVE UP**: Loop until each phase succeeds.
5. **EVIDENCE REQUIRED**: Use `/done` command - it validates all evidence.
6. **TDD REQUIRED**: Test first, then implement.

---

## 🪝 Automated Hooks (Boris-Style)

Hooks run automatically - you don't need to invoke them.

### PostToolUse Hook
**Runs after every Write/Edit operation:**
- Auto-formats Kotlin files with ktlint

### Stop Hook  
**Runs when Claude finishes a task:**
- Captures verification screenshot
- Shows active OpenSpec changes

These ensure quality without manual intervention.

## ⏱️ CI Build Timing - Smart Polling

**Always check historical build times first:**
```bash
gh run list --limit 5 --json durationMs --jq '.[].durationMs / 60000 | floor' 
```

### Smart CI Wait Strategy
```
1. Push code
2. Get run ID: gh run list --limit 1 --json databaseId --jq '.[0].databaseId'
3. Get historical avg: gh run list --limit 5 --json durationMs,conclusion --jq '[.[] | select(.conclusion=="success")] | map(.durationMs) | add / length / 60000 | floor'
4. Wait 75% of average before first poll
5. Then poll every 2 minutes until complete
```

**Use `/wait-ci` command - it calculates timing automatically.**

**DO NOT** poll every 30 seconds. **DO** wait based on actual historical data.

---

## 📋 Evidence Requirements - MANDATORY

Before saying "Feature complete", you MUST have:

### 1. TDD Evidence
```
□ Test file created/modified (show path)
□ Test FAILED initially (show output or state "test failed as expected")
□ Test PASSED after implementation (show output)
□ If multiple iterations: show each fix attempt
```

### 2. CI Evidence  
```
□ CI run ID (e.g., "CI: 20827839192")
□ CI status: PASS
□ If CI failed: show `gh run view <id> --log-failed` output for each failure
□ Number of CI iterations if >1
```

### 3. Visual Evidence
```
□ Screenshot file exists (e.g., "feature.png")
□ Screenshot shows feature working correctly
□ If visual was wrong: show what was wrong, what was fixed
□ Number of visual iterations if >1
```

**Use `/done` command to validate all evidence before reporting.**

---

## 📸 Visual Verification with DroidRun

**DroidRun is the PRIMARY method for visual verification. No YAML flows needed.**

### Quick Reference
```bash
# Alias configured (uses Google Gemini - FREE tier):
droid "<natural language instruction>"

# IMPORTANT: Always end prompts with "then stop" to prevent infinite loops

# Examples:
droid "Open ai.ondevice.app and take a screenshot, then stop"
droid "Open OnDevice app, send 'hi', wait for response, verify disclaimer text appears, then stop"
droid "Open OnDevice app, go to settings, verify dark mode toggle exists, then stop"
```

### Setup (in ~/.bashrc)
```bash
export GOOGLE_API_KEY="your-google-api-key"
alias droid="droidrun run -p GoogleGenAI -m gemini-2.0-flash --steps 10"
```

### How It Works
DroidRun uses Google Gemini to:
1. Navigate the app autonomously
2. Interact with UI elements
3. Verify visual state against your criteria
4. Take screenshots automatically

### Visual Verification Loop
```
1. Run DroidRun with verification criteria:
   droid "Open ai.ondevice.app, send message, verify [YOUR CRITERIA], then stop"

2. If verification FAILS:
   - Identify what's wrong from DroidRun output
   - Fix the code
   - Rebuild (CI loop)
   - Reinstall APK
   - Run DroidRun again
   - Repeat until PASS

3. If verification PASSES:
   - Screenshots saved to: trajectories/[timestamp]/
   - Continue to report
```

### Example Verification Prompts

**Always end with "then stop" to prevent infinite loops!**

| Feature | DroidRun Command |
|---------|------------------|
| Disclaimer | `droid "Open OnDevice app, send 'hello', wait for AI response, verify text 'OnDevice can make mistakes' appears, then stop"` |
| Color Logo | `droid "Open OnDevice app, send message, verify the logo next to disclaimer is in color, then stop"` |
| Settings | `droid "Open OnDevice app, navigate to settings, verify theme toggle exists, then stop"` |
| Dark Mode | `droid "Open OnDevice app, go to settings, enable dark mode, verify background is dark, then stop"` |

### DroidRun Options
```bash
# Default (Google Gemini - uses alias)
droid "your instruction, then stop"

# More steps if needed
droidrun run -p GoogleGenAI -m gemini-2.0-flash --steps 15 "your instruction"

# With vision (screenshots for all agents)
droidrun run -p GoogleGenAI -m gemini-2.0-flash --vision --steps 10 "your instruction"

# Debug mode
droidrun run -p GoogleGenAI -m gemini-2.0-flash --debug --steps 10 "your instruction"

# ALTERNATIVES:
# Anthropic (requires ANTHROPIC_API_KEY, paid)
droidrun run -p Anthropic -m claude-sonnet-4-20250514 --steps 10 "your instruction"

# Ollama (free, local, uses DGX GPU)
droidrun run -p Ollama -m llama3.1:8b --steps 10 "your instruction"
```

### Find Screenshots
```bash
# DroidRun saves screenshots to trajectories folder
ls -lt trajectories/ | head -5

# Find latest screenshots
find trajectories/ -name "*.png" -mmin -30
```

### Troubleshooting

| Issue | Fix |
|-------|-----|
| "Portal error" | `adb shell am force-stop dev.anthropic.droidrun.portal` then retry |
| Multiple devices | `adb disconnect <ip>` to leave only physical device |
| Keeps running after Ctrl+C | `adb shell am force-stop dev.anthropic.droidrun.portal` |
| Won't stop on phone | Always say "then stop" in prompt |
| Wrong app opened | Use full package: "ai.ondevice.app" |
| API key not found | `source ~/.bashrc` or check `echo $GOOGLE_API_KEY` |
| Multiple devices | `adb disconnect <wireless-ip>` to leave only physical device |
| Keeps running | Use `--steps 10` to limit, or press `Ctrl+C` |
| Wrong app opened | Be explicit: "Open the app with package ai.ondevice.app" |

---

## 📸 Fallback: Maestro (Complex Flows Only)

Use Maestro ONLY when DroidRun can't handle:
- Fresh app install with TOS acceptance
- Model download flows (20+ min waits)
- Precise timing-dependent sequences

### Existing Maestro Flows (Backup)
```
.maestro/flows/
├── e2e-fresh-install-to-chat.yaml  # Full fresh install flow
├── verify-quick.yaml                # Quick verification
├── chat-flow.yaml                   # Basic chat test
└── capture-state.yaml               # Generic screenshot
```

### Run Maestro (if needed)
```bash
maestro test .maestro/flows/e2e-fresh-install-to-chat.yaml
```

---

## 📸 Simple Screenshot (Quick Check)

For quick manual verification:
```bash
# Take screenshot
adb exec-out screencap -p > screenshot.png

# Screenshot after launching app
adb shell am start -n ai.ondevice.app/.MainActivity && sleep 3 && adb exec-out screencap -p > app.png
```

Then drag the image into Claude Code or say "Analyze screenshot.png"

---

## 📊 Know Your Limitations (Memorize These)

| Reality | Number | Your Action |
|---------|--------|-------------|
| Your code hallucination rate | **42%** | Always run code before claiming it works |
| Your issues vs human code | **1.7x more** | Test everything, expect review |
| Your security vulnerabilities | **48%** | Never skip security considerations |
| Your effective context | **50-60%** | Don't start complex tasks when context is filling up |

**Your Role:** *You are the implementer, not the architect.* Wait for design direction, then execute precisely.

---

## 📚 Reference Documents

| Document | When to Read |
|----------|--------------|
| **LESSONS_LEARNED.md** | Contains 50+ verified patterns, detailed examples, common problems |
| **CODE_INDEX.md** | Before creating any new file |
| **BMAD_ROUTER.md** | When routing tasks to agents |

---

## 🧠 AUTOMATIC LESSON CAPTURE

### Trigger 1: Claude fails → then succeeds
**Action:** IMMEDIATELY append to LESSONS_LEARNED.md
- Add failed approach to "🔴 CRITICAL - Never Do This"
- Add working approach to "🟢 ALWAYS Do This"
- Add entry to Change Log

### Trigger 2: User redirects Claude → new approach works
**Action:** IMMEDIATELY append to LESSONS_LEARNED.md
- What Claude tried that didn't work → "Never Do This"
- What user suggested that worked → "Always Do This"
- Add entry to Change Log

### Trigger 3: Feature built → doesn't work → retry succeeds
**Action:** IMMEDIATELY append to LESSONS_LEARNED.md
- Document the working approach with code example
- Add entry to Change Log

### Trigger 4: Any non-obvious solution discovered
**Action:** IMMEDIATELY append to LESSONS_LEARNED.md
- Document with full context and example
- Add entry to Change Log

---

## ⚡ Model Strategy (Max 5x Optimized)

**DEFAULT: Sonnet** (140-280 hrs/week)
**RESERVE: Opus** (15-35 hrs/week) - planning only

| Task | Model | BMAD Agent |
|------|-------|------------|
| Requirements/Planning | Opus | /analyst, /pm |
| Architecture | Opus | /architect |
| Implementation | **Sonnet** | /dev |
| Testing | **Sonnet** | /qa |
| UI/Docs/Fixes | **Sonnet** | - |

---

## 🏗️ Build Pipeline

**All builds via GitHub Actions - cannot build locally on DGX Spark**

| Local | GitHub Actions |
|-------|----------------|
| ✅ Code editing | ✅ Full builds |
| ✅ Git operations | ✅ Lint (ktlint) |
| ❌ ./gradlew anything | ✅ Tests |
| ❌ assembleDebug | ✅ APK artifacts |

---

## 📱 Device Testing

### Physical Device (USB-C Connected)
```bash
adb devices  # Should show R3CT10HETMM
adb shell am start -n ai.ondevice.app/.MainActivity
```

---

## 🎭 UI Automation - Use DroidRun

### ✅ PRIMARY: DroidRun (Natural Language)
```bash
droid "Open ai.ondevice.app, send 'hello', verify response appears"
droid "Navigate to settings, toggle dark mode, verify background changes"
```

### ❌ NEVER
- `adb shell input tap X Y` (breaks across devices)
- Claim success without verification

### 🔄 FALLBACK: Maestro (Complex Flows Only)
```yaml
appId: ai.ondevice.app
---
- tapOn: "Download"
- assertVisible: "Success"
- takeScreenshot: "verify"
```

---

## 📋 Commands

### OpenSpec Commands (Spec-Driven Development)

| Command | Purpose |
|---------|---------|
| `/openspec-proposal <feature>` | Create change proposal with specs (ALWAYS START HERE) |
| `/openspec-apply <change-name>` | Implement approved specs |
| `/openspec-archive <change-name>` | Archive completed change, update specs/ |

### Workflow Commands

| Command | Purpose |
|---------|---------|
| `/status` | Project status |
| `/done` | Validate evidence, generate completion report |
| `/verify` | Lint + tests |
| `/ship "msg"` | Commit, push |
| `/wait-ci` | Smart CI polling |

### Verification Commands

| Command | Purpose |
|---------|---------|
| `/visual-verify` | Screenshot + analysis |
| `/ui-test` | Maestro tests (fallback) |

### Utility Commands

| Command | Purpose |
|---------|---------|
| `/emulator` | Start Waydroid |
| `/add-lesson` | Update LESSONS_LEARNED.md |
| `/opus-think` | Switch to Opus for planning |
| `/sonnet` | Switch to Sonnet for implementation |

### Quick Reference

```bash
# Start new feature (ALWAYS do this first)
/openspec-proposal Add disclaimer text below AI responses

# After approval, implement
/openspec-apply add-disclaimer

# When all tasks done, archive
/openspec-archive add-disclaimer

# Visual verification
adb exec-out screencap -p > feature.png
```

---

## 📦 App Info

- **Package**: `ai.ondevice.app`
- **Activity**: `ai.ondevice.app/.MainActivity`
- **Device**: R3CT10HETMM (Samsung S22 Ultra)

---

## 📁 Structure

```
src/                          # Git root
├── .bmad/                    # BMAD agents
├── openspec/changes/         # Active features
├── .claude/                  # Claude config
├── .maestro/flows/           # UI tests
├── CLAUDE.md                 # This file
├── LESSONS_LEARNED.md        # UPDATE ON LEARNINGS
└── CODE_INDEX.md             # Check before creating
```

---

## 🔗 Read Order

1. **LESSONS_LEARNED.md** - FIRST, always
2. **CODE_INDEX.md** - Before creating code
3. **openspec/changes/** - For active work
