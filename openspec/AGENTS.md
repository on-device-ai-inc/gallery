# OpenSpec + BMM Integration Guide for AI Agents

## Overview

This project uses a hybrid approach combining **BMad Method (BMM)** for strategic planning with **OpenSpec** for tactical spec-driven implementation.

**When to use BMM workflows:** Product vision, architecture decisions, epic planning, story creation
**When to use OpenSpec:** Detailed feature specifications, implementation tasks, change tracking

## OpenSpec Directory Structure

```
openspec/
├── specs/               # Current source-of-truth specifications
│   └── [feature]/
│       └── spec.md
├── changes/             # Proposed feature updates
│   └── [change-name]/
│       ├── proposal.md  # Why and what changes
│       ├── tasks.md     # Implementation checklist
│       ├── design.md    # Technical decisions (optional)
│       └── specs/
│           └── [feature]/
│               └── spec.md  # Delta showing additions/modifications
├── archive/             # Completed changes (auto-created on archive)
└── project.md           # Project conventions (READ THIS FIRST)
```

## Specification Format

All specs in `openspec/specs/` and `openspec/changes/*/specs/` follow this structure:

```markdown
# [Feature Name] Specification

## Purpose
Brief description of what this spec covers.

## Requirements

### Requirement: [Descriptive Name]
The system SHALL [capability or constraint].

#### Scenario: [Condition Description]
- WHEN [trigger event or condition]
- THEN [expected system behavior]

#### Scenario: [Another Condition]
- WHEN [different trigger]
- THEN [expected outcome]

### Requirement: [Another Requirement]
The system SHALL [another capability].

#### Scenario: [Edge Case]
- WHEN [exceptional condition]
- THEN [how system handles it]
```

**Key Conventions:**
- Use SHALL (recommended) or MUST for requirement statements
- Each requirement can have multiple scenarios
- Scenarios use WHEN/THEN format for clarity
- Keep scenarios focused on observable behavior

## Delta Notation (for Changes)

When proposing changes in `openspec/changes/[name]/specs/`, use delta blocks:

```markdown
# [Feature Name] Specification

## ADDED Requirements

### Requirement: New Capability
The system SHALL [new behavior].

#### Scenario: [When it applies]
- WHEN [condition]
- THEN [outcome]

## MODIFIED Requirements

### Requirement: Existing Capability (Updated)
The system SHALL [complete updated text of requirement].
*(Provide the FULL new text, not just the diff)*

#### Scenario: [Updated scenario]
- WHEN [new condition]
- THEN [new outcome]

## REMOVED Requirements

### Requirement: Deprecated Feature
Brief explanation of why this requirement was removed.
```

## OpenSpec Workflow

### Phase 1: Draft Change Proposal

**User request examples:**
- "Create an OpenSpec proposal for adding dark mode"
- "Draft a change proposal for user authentication"
- "Start a spec for implementing search functionality"

**Your actions:**
1. Create folder: `openspec/changes/[descriptive-name]/`
2. Create `proposal.md` with:
   - **Title:** Brief name of the change
   - **Context:** Why this change is needed
   - **Scope:** What will be changed
   - **Out of Scope:** What won't be included
3. Create `tasks.md` with implementation checklist:
   ```markdown
   # Implementation Tasks

   ## Phase 1: [Category]
   - [ ] Task description
   - [ ] Another task

   ## Phase 2: [Category]
   - [ ] Task description
   ```
4. Create spec deltas in `openspec/changes/[name]/specs/[feature]/spec.md`
5. (Optional) Create `design.md` for technical decisions

**Link to BMM:** Reference the BMM story/epic that triggered this change:
```markdown
## BMM Context
- **Epic:** Epic 2 - Premium UX Implementation
- **Story:** Story 10 - Privacy Differentiation Indicators
- **Story File:** `.bmad/docs/stories/story-10-privacy-indicators.md`
```

### Phase 2: Review & Align

**User request examples:**
- "Review the proposal for dark mode"
- "Let's refine the authentication specs"
- "Validate the search functionality specs"

**Your actions:**
1. Read the proposal and specs
2. Check for:
   - Clear, unambiguous requirements
   - Testable scenarios with WHEN/THEN structure
   - Alignment with BMM story ACs
   - No scope creep beyond original intent
3. Suggest improvements or ask clarifying questions
4. Update specs based on user feedback
5. Validate format using checklist:
   - [ ] All requirements use SHALL/MUST
   - [ ] Each requirement has at least one scenario
   - [ ] Scenarios use WHEN/THEN format
   - [ ] Delta notation used correctly (ADDED/MODIFIED/REMOVED)

### Phase 3: Implement Tasks

**User request examples:**
- "The specs look good. Let's implement this change."
- "Start implementing the authentication feature"
- "Execute the tasks for dark mode"

**Your actions:**
1. Read `openspec/changes/[name]/tasks.md`
2. Read all spec files in `openspec/changes/[name]/specs/`
3. Read relevant code files (check `openspec/project.md` for package structure)
4. Implement each task, referencing specifications
5. Mark tasks complete in `tasks.md` as you finish them:
   ```markdown
   - [x] ~~Task description~~ ✅
   ```
6. Commit code with references to OpenSpec change:
   ```
   feat: Implement dark mode toggle

   Implements openspec/changes/add-dark-mode
   - Added DarkModeToggle composable (Requirement: Theme Switching)
   - Updated ThemeManager to persist preference

   Refs: openspec/changes/add-dark-mode/specs/theme/spec.md
   ```

### Phase 4: Archive Completed Changes

**User request examples:**
- "Archive the dark mode change"
- "The implementation is done, archive it"
- "Move the authentication change to archive"

**Your actions:**
1. Verify all tasks in `tasks.md` are marked complete
2. Create `openspec/archive/[name]/` directory
3. Move entire change folder: `openspec/changes/[name]` → `openspec/archive/[name]/`
4. Merge spec deltas:
   - Read `openspec/archive/[name]/specs/[feature]/spec.md` (delta)
   - Read `openspec/specs/[feature]/spec.md` (current source of truth)
   - Apply ADDED requirements to source spec
   - Replace MODIFIED requirements in source spec
   - Remove REMOVED requirements from source spec
   - Update `openspec/specs/[feature]/spec.md`
5. Commit the archive:
   ```
   docs: Archive [change-name] and merge specs

   - Moved openspec/changes/[name] to archive
   - Merged spec deltas into openspec/specs/[feature]/spec.md
   - Added X requirements, modified Y requirements, removed Z requirements
   ```

## Integration with BMM Workflows

### When User Triggers BMM Story Creation
**BMM workflow creates:** `.bmad/docs/stories/story-X-name.md` with ACs

**Your next step:** Suggest creating OpenSpec proposal:
```
"I've created Story X. Would you like me to create an OpenSpec change
proposal to detail the technical specifications for this story?"
```

### When User Triggers Implementation
**Check for OpenSpec change first:**
- If exists: Read `openspec/changes/[name]/specs/` and `tasks.md`
- If not exists: Suggest creating one before implementation
- If user declines: Proceed with direct implementation (simple changes only)

### When User Asks "What Should I Work On?"
**Priority order:**
1. Check `.bmad/_cfg/workflow-status.yaml` for current BMM phase/story
2. Check `openspec/changes/` for active proposals needing review/implementation
3. Recommend next action based on both sources

## Command Reference (For Supported Tools)

### Claude Code (This Tool)
OpenSpec doesn't have native slash commands yet, but you can respond to:
- "Create OpenSpec proposal for [feature]"
- "Review OpenSpec change [name]"
- "Implement OpenSpec change [name]"
- "Archive OpenSpec change [name]"

### Natural Language Protocol (All Tools)
When user says "proposal", "change proposal", or "OpenSpec":
1. Understand intent (create/review/implement/archive)
2. Follow the appropriate phase workflow above
3. Always reference `openspec/project.md` for project conventions

## Critical Guidelines

### DO:
✅ Read `openspec/project.md` before starting any work
✅ Create proposals for non-trivial changes (>3 files or >50 lines)
✅ Use clear, testable requirements with SHALL/MUST
✅ Write specific WHEN/THEN scenarios for all requirements
✅ Link to BMM stories/epics when applicable
✅ Mark tasks complete as you implement them
✅ Commit code with OpenSpec references
✅ Archive changes when fully implemented

### DON'T:
❌ Skip proposal phase for complex features
❌ Use vague requirements ("should be fast", "user-friendly")
❌ Write scenarios without WHEN/THEN structure
❌ Implement features not in the specs (scope creep)
❌ Leave tasks incomplete without explanation
❌ Forget to merge spec deltas back to source of truth on archive
❌ Create specs that contradict BMM story ACs

## Error Recovery

### If specs are unclear during implementation:
1. **STOP** implementing
2. Ask user for clarification
3. Update specs in `openspec/changes/[name]/specs/`
4. Resume implementation with clear requirements

### If requirements conflict with code:
1. Document the conflict
2. Ask user which takes precedence (spec or code)
3. Update specs OR refactor code accordingly
4. Never silently ignore the conflict

### If scope creep detected:
1. **STOP** and alert user
2. Explain what's in scope vs. out of scope
3. Suggest creating a new change proposal for additional features
4. Only proceed with original scope unless user approves expansion

## Example: Complete Change Lifecycle

**User:** "Add a feature to export conversation history as PDF"

**You:**
```
I'll create an OpenSpec change proposal for PDF export. This will help us
define the requirements before implementation.

[Create openspec/changes/add-pdf-export/]
- proposal.md (why, what, scope)
- tasks.md (implementation checklist)
- specs/export/spec.md (requirements with scenarios)

Would you like to review the proposal?
```

**User:** "Yes, review it"

**You:**
```
[Read and analyze proposal.md and specs/export/spec.md]

Here's the proposal summary:
- Title: PDF Export for Conversation History
- Scope: Export single or multiple conversations to PDF
- Requirements: 4 (PDF generation, formatting, metadata, error handling)
- Tasks: 8 implementation steps

Recommendations:
1. Requirement "Formatting" needs scenarios for different conversation types
2. Should we include image/audio transcripts in PDF or just text?
3. File size limits should be specified

Shall I update the specs based on these questions?
```

**User:** "Images should be included. Max 50MB per PDF. Update specs."

**You:**
```
[Update specs/export/spec.md with clarifications]

Specs updated:
- Added Scenario: Images in conversation
  WHEN conversation contains images THEN include as inline images in PDF
- Added Requirement: File size limit SHALL be 50MB per PDF
- Added Scenario: Exceeds limit
  WHEN export would exceed 50MB THEN notify user and offer to split

Ready to implement?
```

**User:** "Yes, implement it"

**You:**
```
[Work through tasks.md, implement code, mark tasks complete]

Implementation complete:
✅ Created PdfExportManager.kt
✅ Added PDF generation library dependency
✅ Implemented conversation-to-PDF conversion
✅ Added export button to conversation list
✅ Added file size validation
✅ Added error handling for export failures

All 8 tasks complete. Ready to archive?
```

**User:** "Archive it"

**You:**
```
[Move to archive/, merge specs to openspec/specs/export/spec.md]

Archived openspec/changes/add-pdf-export
- Merged 4 requirements into openspec/specs/export/spec.md
- Source of truth updated with PDF export specifications

Change complete!
```

---

## Quick Reference

**Read first:** `openspec/project.md`
**Create proposal:** `openspec/changes/[name]/` with proposal.md, tasks.md, specs/
**Review specs:** Check requirements use SHALL/MUST, scenarios use WHEN/THEN
**Implement:** Follow tasks.md, reference specs/, mark complete
**Archive:** Move to archive/, merge deltas to openspec/specs/

**Questions?** Ask the user or refer to https://github.com/Fission-AI/OpenSpec
