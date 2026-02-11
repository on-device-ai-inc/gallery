# OpenSpec Proposal

Create a new OpenSpec change proposal for spec-driven development.

## Usage

```
/openspec-proposal <feature-description>
```

## What This Does

1. **Creates change folder**: `openspec/changes/<feature-slug>/`
2. **Generates proposal.md**: Summary, motivation, scope, references
3. **Generates tasks.md**: Implementation checklist with acceptance criteria
4. **Generates spec-delta.md**: What specs will be ADDED/MODIFIED/REMOVED

## Workflow

```
┌─────────────────────────────────────────────────────────────┐
│  1. PROPOSAL (You are here)                                 │
│     └── Create specs BEFORE any code is written            │
│                                                             │
│  2. REVIEW                                                  │
│     └── Iterate on specs until consensus                   │
│     └── Run: openspec validate <change-name>               │
│                                                             │
│  3. APPLY (/openspec-apply)                                │
│     └── Implement based on approved specs                  │
│     └── Check off tasks as completed                       │
│                                                             │
│  4. ARCHIVE (/openspec-archive)                            │
│     └── Merge to specs/, move to archive/                  │
└─────────────────────────────────────────────────────────────┘
```

## Template: proposal.md

```markdown
# Proposal: <feature-slug>

## Summary
<One-line description of the change>

## Motivation
<Why is this change needed? What problem does it solve?>

## Scope
- <What's included>
- <What's NOT included>

## Acceptance Criteria
- [ ] <Criterion 1>
- [ ] <Criterion 2>
- [ ] <Criterion 3>

## Technical Approach
<High-level implementation strategy>

## References
- See openspec/specs/<relevant-spec>.md
- Related to: <other changes or issues>
```

## Template: tasks.md

```markdown
# Tasks: <feature-slug>

## Implementation Tasks

- [ ] **Task 1**: <description>
  - Acceptance: <how to verify>
  
- [ ] **Task 2**: <description>
  - Acceptance: <how to verify>

- [ ] **Task 3**: <description>
  - Acceptance: <how to verify>

## Testing Tasks

- [ ] Unit tests for <component>
- [ ] Integration test for <flow>
- [ ] Visual verification via Maestro/screenshot

## Documentation Tasks

- [ ] Update README if needed
- [ ] Update LESSONS_LEARNED.md with discoveries
```

## Template: spec-delta.md

```markdown
# Spec Delta: <feature-slug>

## ADDED

### <new-spec-name>.md
```
<new specification content>
```

## MODIFIED

### <existing-spec>.md
```diff
- <old line>
+ <new line>
```

## REMOVED

- <spec being removed, if any>
```

## Commands

```bash
# After creating proposal, validate it
openspec validate <change-name>

# Show the proposal
openspec show <change-name>

# List all active changes
openspec list
```

## Example

```
User: /openspec-proposal Add disclaimer text below AI responses

Claude creates:
  openspec/changes/add-disclaimer/
  ├── proposal.md      # What and why
  ├── tasks.md         # Implementation checklist
  └── spec-delta.md    # Spec changes (ADDED/MODIFIED/REMOVED)
```

## CRITICAL: No Code Until Approved

**DO NOT write any implementation code until:**
1. Proposal is reviewed
2. User says "approved" or "proceed with apply"
3. Then use `/openspec-apply` to implement

This ensures spec adherence and prevents scope creep.
