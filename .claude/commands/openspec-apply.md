# OpenSpec Apply

Implement an approved OpenSpec change proposal.

## Usage

```
/openspec-apply <change-name>
```

## Prerequisites

- [ ] Proposal exists in `openspec/changes/<change-name>/`
- [ ] User has reviewed and approved the proposal
- [ ] Specs are finalized (no more iteration needed)

## What This Does

1. **Reads the approved specs** from `openspec/changes/<change-name>/`
2. **Implements each task** from tasks.md
3. **Checks off tasks** as completed
4. **Validates against acceptance criteria**
5. **Runs verification loops** (TEST, CI, VISUAL)

## Workflow

```
┌─────────────────────────────────────────────────────────────┐
│  APPLY PHASE                                                │
│                                                             │
│  1. Read proposal.md → Understand scope                    │
│  2. Read spec-delta.md → Know exact spec requirements      │
│  3. Read tasks.md → Get implementation checklist           │
│                                                             │
│  For each task:                                            │
│    a. Implement according to spec                          │
│    b. Write tests (TDD)                                    │
│    c. Verify acceptance criteria                           │
│    d. Check off task in tasks.md                           │
│    e. Commit with reference to change                      │
│                                                             │
│  4. Run full verification (TEST, CI, VISUAL loops)         │
│  5. Update tasks.md with all boxes checked                 │
└─────────────────────────────────────────────────────────────┘
```

## Implementation Protocol

### Step 1: Load Context
```bash
# Read all spec files
cat openspec/changes/<change-name>/proposal.md
cat openspec/changes/<change-name>/tasks.md
cat openspec/changes/<change-name>/spec-delta.md
```

### Step 2: Implement with TDD
For each task in tasks.md:

```
1. Write failing test (TDD Red)
2. Implement to pass test (TDD Green)
3. Verify against acceptance criteria from proposal.md
4. Update tasks.md: [ ] → [x]
5. Commit: "feat(<change-name>): <task description>"
```

### Step 3: Continuous Spec Validation

**After EVERY code change, ask yourself:**
- Does this match the spec-delta.md?
- Does this satisfy the acceptance criteria?
- Am I staying within scope defined in proposal.md?

**If deviating from spec:**
1. STOP implementation
2. Ask user: "This requires spec change. Should I update proposal?"
3. Wait for approval before proceeding

### Step 4: Verification Loops

```
🔄 TEST LOOP
   └── Run tests → If fail → Fix → Repeat until PASS

🔄 CI LOOP  
   └── Push → CI runs → If fail → Fix → Push → Repeat until GREEN

🔄 VISUAL LOOP
   └── Screenshot → Compare to acceptance criteria → If wrong → Fix → Repeat
```

### Step 5: Mark Complete

When ALL tasks are checked:
```bash
# Verify all tasks done
grep -c "\[x\]" openspec/changes/<change-name>/tasks.md
grep -c "\[ \]" openspec/changes/<change-name>/tasks.md  # Should be 0

# Ready for archive
echo "All tasks complete. Ready for /openspec-archive"
```

## Commit Message Format

```
feat(<change-name>): <description>

Implements task from openspec/changes/<change-name>/tasks.md

- <what was done>
- <spec reference>

Refs: openspec/<change-name>
```

## Example

```
User: /openspec-apply add-disclaimer

Claude:
1. Reads openspec/changes/add-disclaimer/proposal.md
2. Reads openspec/changes/add-disclaimer/tasks.md
3. For each task:
   - Implements DisclaimerView.kt
   - Writes DisclaimerViewTest.kt
   - Updates ChatScreen to include disclaimer
   - Checks off task
   - Commits
4. Runs CI loop
5. Runs visual verification
6. Reports: "All 5 tasks complete. Ready for /openspec-archive"
```

## Spec Adherence Checklist

Before marking ANY task complete:

```
□ Implementation matches spec-delta.md exactly
□ Acceptance criteria from proposal.md are met
□ No scope creep (nothing added that wasn't in proposal)
□ Tests cover the specified behavior
□ Visual output matches expected UI (if applicable)
```

## DO NOT

- ❌ Add features not in the proposal
- ❌ Skip tasks in tasks.md
- ❌ Mark tasks complete without verification
- ❌ Deviate from spec-delta.md without approval
- ❌ Proceed if acceptance criteria aren't met

## After Apply

When all tasks are complete:
```
/openspec-archive <change-name>
```
