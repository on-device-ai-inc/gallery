# OpenSpec Archive

Archive a completed OpenSpec change and update source-of-truth specs.

## Usage

```
/openspec-archive <change-name>
```

## Prerequisites

- [ ] All tasks in tasks.md are checked `[x]`
- [ ] All tests passing
- [ ] CI is green
- [ ] Visual verification complete
- [ ] Code has been reviewed/merged

## What This Does

1. **Validates completion**: Ensures all tasks are done
2. **Merges spec-delta.md** → Updates `openspec/specs/`
3. **Moves change folder** → `openspec/changes/archive/<change-name>/`
4. **Updates LESSONS_LEARNED.md** with any discoveries
5. **Creates completion record**

## Workflow

```
┌─────────────────────────────────────────────────────────────┐
│  ARCHIVE PHASE                                              │
│                                                             │
│  1. Verify all tasks complete                              │
│     └── grep "\[ \]" tasks.md should return nothing        │
│                                                             │
│  2. Apply spec-delta.md to openspec/specs/                 │
│     └── ADDED: Create new spec files                       │
│     └── MODIFIED: Update existing specs                    │
│     └── REMOVED: Delete obsolete specs                     │
│                                                             │
│  3. Move to archive                                        │
│     └── mv openspec/changes/<name> openspec/archive/<name> │
│                                                             │
│  4. Update knowledge base                                  │
│     └── Add learnings to LESSONS_LEARNED.md                │
│                                                             │
│  5. Commit archive                                         │
│     └── "chore: archive openspec change <name>"            │
└─────────────────────────────────────────────────────────────┘
```

## Archive Checklist

Before archiving, verify:

```bash
# 1. All tasks complete
echo "Checking tasks..."
grep -c "\[ \]" openspec/changes/<change-name>/tasks.md
# Should output: 0

# 2. Tests pass
echo "Checking tests..."
# (via CI - should be green)

# 3. Acceptance criteria met
echo "Review acceptance criteria in proposal.md..."
cat openspec/changes/<change-name>/proposal.md | grep -A 20 "Acceptance Criteria"
```

## Spec Merge Process

### For ADDED specs:
```bash
# Create new spec file
cp openspec/changes/<change-name>/specs/<new-spec>.md openspec/specs/<new-spec>.md
```

### For MODIFIED specs:
```bash
# Apply the diff from spec-delta.md to existing spec
# Review the changes carefully before committing
```

### For REMOVED specs:
```bash
# Remove obsolete spec (rare)
rm openspec/specs/<obsolete-spec>.md
```

## Directory Structure After Archive

```
openspec/
├── specs/                          # Source of truth (UPDATED)
│   ├── existing-spec.md
│   └── new-spec-from-change.md     # ← Added from change
│
├── changes/                        # Active changes (now empty)
│
└── archive/                        # Completed changes
    └── <change-name>/              # ← Moved here
        ├── proposal.md
        ├── tasks.md
        └── spec-delta.md
```

## Completion Record

Add to the archived proposal.md:

```markdown
---
## Completion Record

- **Archived**: <date>
- **Commits**: <list of commit hashes>
- **CI Run**: <run ID>
- **Lessons Learned**: <any discoveries added to LESSONS_LEARNED.md>
```

## Commit Message

```
chore(openspec): archive <change-name>

Completed implementation of <summary from proposal>

Tasks completed:
- <task 1>
- <task 2>
- <task 3>

Specs updated:
- ADDED: <new-spec>.md
- MODIFIED: <existing-spec>.md

Closes: #<issue-number-if-applicable>
```

## Example

```
User: /openspec-archive add-disclaimer

Claude:
1. Verifies all tasks in add-disclaimer/tasks.md are [x]
2. Merges spec-delta.md:
   - Creates openspec/specs/ui/disclaimer.md
   - Updates openspec/specs/ui/chat-screen.md
3. Moves folder:
   - openspec/changes/add-disclaimer/ → openspec/archive/add-disclaimer/
4. Updates LESSONS_LEARNED.md:
   - "DisclaimerView: Use Row for horizontal logo+text layout"
5. Commits: "chore(openspec): archive add-disclaimer"
6. Reports: "✅ Change archived. Specs updated. Ready for next feature!"
```

## Post-Archive

After archiving:

```bash
# Verify specs are updated
ls openspec/specs/

# Verify change is archived  
ls openspec/archive/

# Start next feature
/openspec-proposal <next-feature>
```

## CRITICAL: Don't Archive Incomplete Work

**NEVER archive if:**
- Any task is unchecked `[ ]`
- Tests are failing
- CI is red
- Acceptance criteria not verified
- Visual verification not done

**The archive is permanent history.** Only archive completed, verified work.
