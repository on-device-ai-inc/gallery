# BMAD Task Router

> **Claude: Reference this file when the user describes a task/feature to build.**
> **Use it to select the right agent(s) and generate a structured prompt.**

---

## How to Use This

When user says something like:
- "I want to add X feature"
- "Build me Y"
- "We need to implement Z"
- "Create a component that does W"

**DO NOT** start coding. Instead:
1. Assess complexity
2. Select agent(s) from the routing table
3. Generate a structured prompt
4. Present to user for approval

---

## Complexity Assessment

| Complexity | Indicators | Agents Needed |
|------------|------------|---------------|
| **Low** | Single file, <50 lines, UI tweak, clear requirements, no state changes | `/bmad:bmm:agents:dev` only |
| **Medium** | 2-5 files, new component, some state logic, clear requirements | `/bmad:bmm:agents:dev`, maybe `/bmad:bmm:agents:architect` for design review |
| **High** | 5+ files, new system/flow, unclear requirements, architectural decisions | `/bmad:bmm:agents:analyst` → `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| **Strategic** | Business decisions, market positioning, legal/financial implications | Domain-specific agents |
| **Multi-perspective** | Needs diverse expertise, cross-functional | `/bmad:core:workflows:party-mode` |

---

## Complete Agent Reference

### Core Software Development Team (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:analyst` | Mary | Business Analyst 📊 |
| `/bmad:bmm:agents:architect` | Winston | System Architect 🏗️ |
| `/bmad:bmm:agents:dev` | Amelia | Developer 💻 |
| `/bmad:bmm:agents:pm` | John | Product Manager 📋 |
| `/bmad:bmm:agents:sm` | Bob | Scrum Master 🏃 |
| `/bmad:bmm:agents:tea` | Murat | Master Test Architect 🧪 |
| `/bmad:bmm:agents:tech-writer` | Paige | Technical Writer 📚 |
| `/bmad:bmm:agents:ux-designer` | Sally | UX Designer 🎨 |

### Legal & Financial Team (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:legal-counsel` | Victoria | Canadian Tech Legal Counsel ⚖️ |
| `/bmad:bmm:agents:cpa-counsel` | Marcus | Canadian CPA Tax Specialist 💰 |
| `/bmad:bmm:agents:audit-analyzer` | Rachel | CRA Audit Risk Analyzer 🔍 |

### Marketing Leadership (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:cmo-dual-market` | Katherine Chen | CMO Dual Market Strategy 🎯 |
| `/bmad:bmm:agents:product-marketing-manager` | David Kim | Product Marketing Manager 🎯 |

### Privacy Marketing Team (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:privacy-marketing-lead` | Alex Rivera | Privacy Marketing Lead 🔒 |
| `/bmad:bmm:agents:privacy-content-manager` | Jordan Lee | Privacy Content Manager ✍️ |
| `/bmad:bmm:agents:privacy-community-manager` | Sam Torres | Privacy Community Manager 🤝 |
| `/bmad:bmm:agents:growth-manager-privacy` | Maya Patel | Growth Marketing Manager 📈 |

### Social Impact Marketing Team (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:social-impact-lead` | Amara Johnson | Social Impact Lead 🌍 |
| `/bmad:bmm:agents:community-organizer` | Carlos Mendez | Community Organizer 🤲 |
| `/bmad:bmm:agents:localization-manager` | Priya Sharma | Localization Manager 🌐 |
| `/bmad:bmm:agents:impact-storyteller` | Fatima Hassan | Impact Storyteller 📖 |

### Brand & Analytics (BMM)

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:bmm:agents:brand-creative-manager` | Lucia Torres | Brand & Creative Manager 🎨 |
| `/bmad:bmm:agents:marketing-analytics` | Elena Volkov | Marketing Analytics 📊 |

### CIS (Creative Intelligence System) Agents

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:cis:agents:brainstorming-coach` | Carson | Elite Brainstorming Specialist 🧠 |
| `/bmad:cis:agents:creative-problem-solver` | Dr. Quinn | Master Problem Solver 🔬 |
| `/bmad:cis:agents:design-thinking-coach` | Maya | Design Thinking Maestro 🎨 |
| `/bmad:cis:agents:innovation-strategist` | Victor | Disruptive Innovation Oracle ⚡ |
| `/bmad:cis:agents:storyteller` | Sophia | Master Storyteller 📖 |
| `/bmad:cis:agents:renaissance-polymath` | Leonardo | Renaissance Polymath 🎨 |
| `/bmad:cis:agents:surrealist-provocateur` | Salvador Dali | Surrealist Provocateur 🎭 |
| `/bmad:cis:agents:lateral-thinker` | Edward de Bono | Lateral Thinking Pioneer 🧩 |
| `/bmad:cis:agents:mythic-storyteller` | Joseph Campbell | Mythic Storyteller 🌟 |
| `/bmad:cis:agents:combinatorial-genius` | Steve Jobs | Combinatorial Genius 🍎 |

### Core System

| Command | Agent | Role |
|---------|-------|------|
| `/bmad:core:agents:bmad-master` | BMad Master | Orchestrator & Knowledge Custodian 🧙 |

### Group/Team Commands

| Command | Purpose |
|---------|---------|
| `/bmad:core:workflows:party-mode` | ALL 34+ agents together for group discussion |

### Key Workflows

| Command | Purpose |
|---------|---------|
| `/bmad:bmm:workflows:workflow-init` | Initialize new BMM project |
| `/bmad:bmm:workflows:prd` | Product Requirements Document |
| `/bmad:bmm:workflows:tech-spec` | Technical Specification (Quick Flow) |
| `/bmad:bmm:workflows:architecture` | Architecture decisions |
| `/bmad:bmm:workflows:brainstorm-project` | CIS-powered brainstorming |
| `/bmad:bmm:workflows:create-epics-and-stories` | Epic + story breakdown |
| `/bmad:bmm:workflows:dev-story` | Execute story implementation |
| `/bmad:bmm:workflows:code-review` | Senior dev code review |
| `/bmad:bmm:workflows:workflow-status` | Check "what should I do now?" |

---

---

## Task → Agent Routing

### Software Development

| Task Type | Agent(s) |
|-----------|----------|
| UI component, styling, small feature | `/bmad:bmm:agents:dev` |
| New screen, navigation flow | `/bmad:bmm:agents:dev` + `/bmad:bmm:agents:ux-designer` if UX unclear |
| State management, data flow | `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| API integration, new service | `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| Unclear requirements | `/bmad:bmm:agents:analyst` → `/bmad:bmm:agents:pm` → `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| Test strategy | `/bmad:bmm:agents:tea` |
| Documentation | `/bmad:bmm:agents:tech-writer` |
| Sprint planning | `/bmad:bmm:agents:sm` |
| Product decisions | `/bmad:bmm:agents:pm` |
| Full project kickoff | `/bmad:bmm:workflows:workflow-init` |
| Create PRD | `/bmad:bmm:workflows:prd` |

### Legal/Financial (Canadian)

| Task Type | Agent(s) |
|-----------|----------|
| Privacy policy, terms of service, IP | `/bmad:bmm:agents:legal-counsel` |
| Tax implications, corporate structure, CCPC | `/bmad:bmm:agents:cpa-counsel` |
| CRA audit risk, compliance | `/bmad:bmm:agents:audit-analyzer` |
| Tax + Legal combined | All three legal/financial agents |

### Marketing

| Task Type | Agent(s) |
|-----------|----------|
| Overall strategy, positioning | `/bmad:bmm:agents:cmo-dual-market` |
| Product launch, go-to-market | `/bmad:bmm:agents:product-marketing-manager` |
| Privacy-focused messaging | `/bmad:bmm:agents:privacy-marketing-lead` |
| Content creation (privacy) | `/bmad:bmm:agents:privacy-content-manager` |
| Community building (privacy) | `/bmad:bmm:agents:privacy-community-manager` |
| Growth, acquisition | `/bmad:bmm:agents:growth-manager-privacy` |
| Social impact campaigns | `/bmad:bmm:agents:social-impact-lead` |
| Grassroots, community | `/bmad:bmm:agents:community-organizer` |
| Localization, cultural | `/bmad:bmm:agents:localization-manager` |
| Storytelling, narratives | `/bmad:bmm:agents:impact-storyteller` |
| Brand, creative assets | `/bmad:bmm:agents:brand-creative-manager` |
| Analytics, metrics | `/bmad:bmm:agents:marketing-analytics` |

### Creative/Brainstorming

| Task Type | Agent(s) |
|-----------|----------|
| Structured brainstorming | `/bmad:cis:agents:brainstorming-coach` or `/bmad:bmm:workflows:brainstorm-project` |
| Complex problem solving | `/bmad:cis:agents:creative-problem-solver` |
| User-centered design thinking | `/bmad:cis:agents:design-thinking-coach` |
| Disruptive innovation | `/bmad:cis:agents:innovation-strategist` |
| Storytelling, narrative | `/bmad:cis:agents:storyteller` or `/bmad:cis:agents:mythic-storyteller` |
| Cross-domain connections | `/bmad:cis:agents:renaissance-polymath` or `/bmad:cis:agents:combinatorial-genius` |
| Unconventional thinking | `/bmad:cis:agents:lateral-thinker` or `/bmad:cis:agents:surrealist-provocateur` |

### Multi-Perspective / Cross-Functional

| Task Type | Agent(s) |
|-----------|----------|
| Strategic decisions needing all viewpoints | `/bmad:core:workflows:party-mode` |
| Complex problem with unknown domain | `/bmad:core:workflows:party-mode` |
| Campaign planning (full team) | `/bmad:core:workflows:party-mode` |
| Legal + Business + Technical alignment | `/bmad:core:workflows:party-mode` |

---

## Prompt Template

Once you've selected agent(s), generate this structure:

```
Read LESSONS_LEARNED.md first. Quote one rule from the "MANDATORY EXECUTION PROTOCOL" to prove you read it.

[AGENT_INVOCATION]

## Feature: [Title]

[Clear description of what to build/do]

## Acceptance Criteria

- [ ] [Specific visual/behavioral requirement 1]
- [ ] [Specific visual/behavioral requirement 2]
- [ ] [Specific visual/behavioral requirement 3]

## Step 1: Explore (Then STOP)

Find and tell me:
1. [Relevant question about codebase/context]
2. [Relevant question about existing patterns]
3. [Relevant question about state/dependencies]
4. [Any risks or gotchas?]

**⛔ STOP: Share findings and say "Waiting for your approval." Do NOT write code yet.**

## Step 2: After Approval - Execute Autonomously

Once I say "approved", execute the full protocol WITHOUT stopping:

### 2a. TDD Red
- Write test(s) that verify the acceptance criteria
- Test should FAIL initially

### 2b. TDD Green  
- Write implementation to make test pass
- 🔄 **TEST LOOP**: If test fails → fix code → run test → repeat until PASS

### 2c. Build & Deploy
- Commit and push
- Wait for CI
- 🔄 **CI LOOP**: If CI fails → `gh run view --log-failed` → fix → push → repeat until PASS
- Download APK: `gh run download <id> -n app-debug`
- Install: `adb install -r app-debug.apk`

### 2d. Visual Verification
- Launch app, navigate to feature
- Screenshot: `adb exec-out screencap -p > feature.png`
- Compare to acceptance criteria
- 🔄 **VISUAL LOOP**: If doesn't match → identify gap → fix → rebuild → reinstall → screenshot → repeat until MATCHES

### 2e. Learn
- If anything unexpected happened, update LESSONS_LEARNED.md

## Step 3: Report

When complete, report:
- Tests: PASS/FAIL
- CI: PASS [run ID]
- Screenshot: [attached image proving feature works correctly]
```

---

## Examples

### Example 1: Small UI Feature

**User says:** "Add a loading spinner when fetching models"

**Assessment:** Low complexity - single component, clear requirement

**Generated prompt:**
```
Read LESSONS_LEARNED.md first. Quote one rule from the "MANDATORY EXECUTION PROTOCOL" to prove you read it.

/bmad:bmm:agents:dev

## Feature: Loading Spinner for Model Fetching

Add a loading spinner that shows while models are being fetched.

## Acceptance Criteria

- [ ] Spinner visible when isLoading is true
- [ ] Spinner hidden when loading completes
- [ ] Spinner centered in content area
- [ ] Uses existing app spinner/progress component if available

## Step 1: Explore (Then STOP)

Find and tell me:
1. Where model fetching is triggered
2. What loading state variable exists (isLoading, isFetching, etc.)
3. If there's an existing spinner/progress component in the app
4. Any risks or gotchas?

**⛔ STOP: Share findings and say "Waiting for your approval." Do NOT write code yet.**

## Step 2: After Approval - Execute Autonomously

Once I say "approved", execute the full protocol WITHOUT stopping:

### 2a. TDD Red
- Write test that verifies spinner shows during loading

### 2b. TDD Green
- Implement spinner
- 🔄 TEST LOOP until test passes

### 2c. Build & Deploy
- Commit, push, wait for CI
- 🔄 CI LOOP until green
- Download and install APK

### 2d. Visual Verification
- Screenshot showing spinner during fetch
- 🔄 VISUAL LOOP until matches acceptance criteria

## Step 3: Report

Tests: PASS/FAIL | CI: [run ID] | Screenshot: [attached]
```

---

### Example 2: Medium Complexity - New Screen

**User says:** "Create a settings screen with theme toggle and model preferences"

**Assessment:** Medium complexity - new screen, multiple components, state

**Generated prompt:**
```
Read LESSONS_LEARNED.md first. Quote one rule from the "MANDATORY EXECUTION PROTOCOL" to prove you read it.

/bmad:bmm:agents:architect then /bmad:bmm:agents:dev

## Feature: Settings Screen

Create a settings screen with theme toggle, default model selection, and clear chat history option.

## Acceptance Criteria

- [ ] Settings screen accessible from main navigation
- [ ] Theme toggle (light/dark) that persists across app restarts
- [ ] Default model selection dropdown
- [ ] Clear chat history button with confirmation dialog
- [ ] Follows existing app design patterns

## Step 1: Explore (Then STOP)

Find and tell me:
1. How navigation is handled (NavController, routes)
2. Where app preferences/settings are stored (DataStore, SharedPrefs)
3. Existing toggle/switch components in the app
4. How theme is currently managed
5. Any risks or gotchas?

**⛔ STOP: Share findings and say "Waiting for your approval." Do NOT write code yet.**

## Step 2: After Approval - Execute Autonomously

Once I say "approved":

### 2a. Architecture Design (/bmad:bmm:agents:architect)
- Screen structure and components
- State management approach
- Navigation integration

### 2b. TDD Red
- Write tests for settings persistence and theme toggle

### 2c. TDD Green (/bmad:bmm:agents:dev)
- Implement settings screen
- 🔄 TEST LOOP until tests pass

### 2d. Build & Deploy
- 🔄 CI LOOP until green
- Install APK

### 2e. Visual Verification
- Screenshot settings screen
- Toggle theme, screenshot both states
- 🔄 VISUAL LOOP until matches acceptance criteria

## Step 3: Report

Tests: PASS/FAIL | CI: [run ID] | Screenshots: [attached]
```

---

### Example 3: High Complexity - New System

**User says:** "Add offline mode with local caching"

**Assessment:** High complexity - architectural decision, multiple systems affected

**Generated prompt:**
```
Read LESSONS_LEARNED.md first. Quote one rule from the "MANDATORY EXECUTION PROTOCOL" to prove you read it.

/bmad:bmm:agents:analyst

## Feature: Offline Mode with Local Caching

Add ability to use the app offline with previously downloaded models and cached conversations.

## Step 1: Requirements Discovery (Then STOP)

/bmad:bmm:agents:analyst - Help me understand:
1. What should work offline vs require network?
2. How much local storage is acceptable?
3. How should sync work when back online?
4. What's the user experience for offline state?
5. What are the risks?

**⛔ STOP: Share findings and say "Waiting for your approval." Do NOT proceed yet.**

## Step 2: After Approval - Architecture

/bmad:bmm:agents:architect - Design:
1. Caching strategy (what, where, how much)
2. Offline detection and state management
3. Sync/conflict resolution approach
4. Data layer changes needed

Present design and acceptance criteria for my review.

## Step 3: After Architecture Approval - Execute

Break into smaller tasks. For each task, follow the full protocol:
- TDD Red → TDD Green (with TEST LOOP)
- Build & Deploy (with CI LOOP)
- Visual Verification (with VISUAL LOOP)

## Step 4: Report

Tests: PASS/FAIL | CI: [run ID] | Screenshots/Demo: [attached]
```

---

### Example 4: Non-Dev Task

**User says:** "Review our privacy policy for the App Store submission"

**Assessment:** Legal task

**Generated prompt:**
```
/bmad:bmm:agents:legal-counsel

## Task: Privacy Policy Review for App Store

Review our privacy policy for Apple App Store submission compliance.

## Requirements

1. GDPR compliance check
2. Apple App Store privacy requirements
3. Data collection disclosure accuracy
4. Third-party SDK disclosures (if any)

## Deliverable

- List of issues/gaps found
- Recommended changes
- Compliance checklist
```

---

## Quick Reference: Common Tasks → Agents

| "I want to..." | Agent(s) |
|----------------|----------|
| Add a button/component | `/bmad:bmm:agents:dev` |
| Create a new screen | `/bmad:bmm:agents:dev`, maybe `/bmad:bmm:agents:ux-designer` |
| Fix a bug | `/bmad:bmm:agents:dev` |
| Refactor code | `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| Add an API integration | `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| Improve performance | `/bmad:bmm:agents:architect` → `/bmad:bmm:agents:dev` |
| Write tests | `/bmad:bmm:agents:tea` → `/bmad:bmm:agents:dev` |
| Write documentation | `/bmad:bmm:agents:tech-writer` |
| Plan a sprint | `/bmad:bmm:agents:sm` |
| Define requirements | `/bmad:bmm:agents:analyst` |
| Design user flow | `/bmad:bmm:agents:ux-designer` |
| Legal review | `/bmad:bmm:agents:legal-counsel` |
| Tax question | `/bmad:bmm:agents:cpa-counsel` |
| Marketing strategy | `/bmad:bmm:agents:cmo-dual-market` |
| Brainstorm ideas | `/bmad:cis:agents:brainstorming-coach` |
| Start new project | `/bmad:bmm:workflows:workflow-init` |
| Create PRD | `/bmad:bmm:workflows:prd` |

---

## Response Format

When generating a prompt for the user, present it like:

```
**Complexity:** [Low/Medium/High]
**Agent(s):** [agent names]
**Reasoning:** [1 sentence why]

---

[Generated prompt in code block for easy copy]
```

User can then copy the prompt into Claude Code.
