Begin a new feature using OpenSpec-driven development.

## Workflow Overview

```
┌──────────────────────────────────────────────────────────────┐
│  1. PROPOSAL → 2. APPROVAL → 3. APPLY → 4. ARCHIVE          │
└──────────────────────────────────────────────────────────────┘
```

## Step 1: Create OpenSpec Proposal (ALWAYS START HERE)

```bash
/openspec-proposal <feature-description>
```

This creates:
- `openspec/changes/<feature>/proposal.md` - What & why
- `openspec/changes/<feature>/tasks.md` - Implementation checklist
- `openspec/changes/<feature>/spec-delta.md` - Spec changes

### Optional: Use BMAD for Complex Features

For complex features requiring more analysis:

```bash
# 1. Requirements (Opus)
/analyst   # Work with Mary on requirements

# 2. PRD (Opus)  
/pm        # Work with John on PRD

# 3. Architecture (Opus)
/architect # Work with Winston on design

# 4. THEN create OpenSpec proposal
/openspec-proposal <feature-name>
```

## Step 2: Wait for Approval

⛔ **STOP HERE** - Present proposal and wait for user approval.

## Step 3: Apply (Implement)

Once approved:
```bash
/openspec-apply <change-name>
```

This runs autonomously:
- TDD Red → Green
- TEST LOOP until pass
- CI LOOP until green
- VISUAL LOOP until correct
- Updates tasks.md checkboxes

## Step 4: Archive

When all tasks complete:
```bash
/openspec-archive <change-name>
```

This:
- Merges specs to `openspec/specs/`
- Moves change to `openspec/archive/`
- Updates LESSONS_LEARNED.md

## Complete Example

```bash
# Start feature
/openspec-proposal Add disclaimer text below AI responses

# ... Claude creates proposal, user reviews ...
# User: "approved"

# Implement
/openspec-apply add-disclaimer

# ... Claude implements with TDD, CI, Visual loops ...

# Archive when done
/openspec-archive add-disclaimer
```

## Quick Reference

| Phase | Command | Model |
|-------|---------|-------|
| Proposal | `/openspec-proposal` | Sonnet |
| Complex Analysis | `/analyst`, `/pm`, `/architect` | Opus |
| Apply | `/openspec-apply` | Sonnet |
| Archive | `/openspec-archive` | Sonnet |
| Completion Check | `/done` | Sonnet |
