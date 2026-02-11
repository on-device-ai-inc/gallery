Route a task to the appropriate BMAD agent(s) and workflow.

## Usage

When the user describes a task, determine the best workflow path.

## Decision Tree

```
┌─────────────────────────────────────────────────────────────────────┐
│  /route-task                                                        │
│       │                                                             │
│       ├── NEW FEATURE?                                              │
│       │   └── Yes → /openspec-proposal (specs first!)              │
│       │                                                             │
│       ├── BUG FIX?                                                  │
│       │   └── Yes → Direct to /dev (no OpenSpec)                   │
│       │                                                             │
│       ├── COMPLEX/STRATEGIC?                                        │
│       │   └── Yes → BMAD agents first (/analyst, /pm, /architect)  │
│       │             then → /openspec-proposal                       │
│       │                                                             │
│       └── QUICK TASK?                                               │
│           └── Yes → Direct execution                                │
└─────────────────────────────────────────────────────────────────────┘
```

## Process

1. **Analyze the request**
   - Is this a new feature or capability? → OpenSpec
   - Is this a bug fix? → Direct dev
   - Is this complex/strategic? → BMAD agents first
   - Is this a quick task? → Direct execution

2. **Route appropriately**

| Task Type | OpenSpec? | Route To |
|-----------|-----------|----------|
| New feature | ✅ Yes | `/openspec-proposal` |
| Bug fix | ❌ No | `/dev` agent |
| Refactor (large) | ✅ Yes | `/openspec-proposal` |
| Refactor (small) | ❌ No | `/dev` agent |
| UI tweak | ❌ No | Direct implementation |
| Architecture | ✅ Yes | `/architect` → `/openspec-proposal` |
| Strategic | ✅ Yes | `/analyst` → `/pm` → `/architect` → `/openspec-proposal` |

3. **Generate appropriate prompt**

## Output for NEW FEATURES

```
**Task Type:** New Feature
**Workflow:** OpenSpec-Driven Development
**Route:** /openspec-proposal

---

Starting with spec creation. Run:

/openspec-proposal <feature-description>

This will create proposal.md, tasks.md, and spec-delta.md.
Review and approve before implementation.
```

## Output for BUG FIXES

```
**Task Type:** Bug Fix
**Complexity:** [Low/Medium/High]
**Agent:** /dev
**Route:** Direct implementation (no OpenSpec needed)

---

[Generated prompt with:]
- Quote requirement from CLAUDE.md
- Acceptance criteria
- STOP after explore
- Three loops (TEST, CI, VISUAL)
- /done validation
```

## Output for COMPLEX/STRATEGIC

```
**Task Type:** Strategic Feature
**Complexity:** High/Strategic
**Workflow:** BMAD → OpenSpec
**Route:** /analyst → /pm → /architect → /openspec-proposal

---

Starting with requirements analysis:

/analyst

After gathering requirements, create PRD:

/pm

Then architecture:

/architect

Finally, create spec:

/openspec-proposal <feature>
```

## Key Rules

1. **New features ALWAYS start with `/openspec-proposal`**
2. **Bug fixes skip OpenSpec** (direct to fix)
3. **Complex features go through BMAD agents THEN OpenSpec**
4. **Always include STOP after explore phase**
5. **Always require `/done` validation**

## Generated Prompt Elements

For non-OpenSpec tasks, include:
1. **Quote requirement**: "I must STOP after exploration and wait for user approval"
2. **Acceptance criteria**: Specific, checkable items
3. **STOP after explore**: "⛔ STOP: Share findings and say 'Waiting for your approval.'"
4. **Three loops**: TEST LOOP, CI LOOP, VISUAL LOOP
5. **Report with /done**: Validates all evidence

For OpenSpec tasks:
1. **Start with /openspec-proposal**
2. **Wait for approval**
3. **Then /openspec-apply**
4. **Finally /openspec-archive**
