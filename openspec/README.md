# OpenSpec Integration for OnDevice AI Gallery

## What is This?

This directory implements **Spec-Driven Development (SDD)** using OpenSpec methodology to prevent scope creep, missed requirements, and implementation ambiguity when working with AI coding assistants.

## Why OpenSpec + BMM?

You're already using **BMad Method (BMM)** for strategic project planning (Product Brief → PRD → Architecture → Epics → Stories). OpenSpec complements this by adding **tactical specification rigor** at the implementation level:

| Layer | Tool | Purpose |
|-------|------|---------|
| **Strategic** | BMM | What to build, why, and in what order |
| **Tactical** | OpenSpec | Detailed requirements with testable scenarios |
| **Execution** | AI Assistant | Code implementation referencing specs |

## Quick Start

### For New Features

1. **User:** "Add dark mode to the app"
2. **AI:** Creates OpenSpec change proposal in `openspec/changes/add-dark-mode/`
   - `proposal.md` - Why and what
   - `tasks.md` - Implementation checklist
   - `specs/theme/spec.md` - Requirements with WHEN/THEN scenarios
3. **User:** Reviews and refines specs until they're clear
4. **AI:** Implements according to specs, marks tasks complete
5. **User:** "Archive the change"
6. **AI:** Moves to `openspec/archive/` and merges specs to `openspec/specs/`

### For BMM Stories

When a BMM story is created (e.g., Story 11), suggest creating an OpenSpec change:

```
User: Create Story 11 for implementing search functionality
AI: I've created Story 11. Would you like me to create an OpenSpec
    change proposal to detail the technical specifications?
User: Yes
AI: [Creates openspec/changes/story-11-search/]
```

This creates a bidirectional link:
- BMM Story → Strategic ACs (what success looks like)
- OpenSpec Change → Tactical requirements (how system behaves)

## Directory Structure

```
openspec/
├── specs/                           # Source of truth
│   ├── privacy-indicators/
│   │   └── spec.md                  # ✅ Current implementation
│   ├── prompt-engineering/
│   │   └── spec.md                  # ⚠️ Partially implemented
│   └── [other-features]/
│
├── changes/                         # Active development
│   └── [feature-name]/
│       ├── proposal.md              # Context and scope
│       ├── tasks.md                 # Implementation checklist
│       ├── design.md                # Technical decisions (optional)
│       └── specs/
│           └── [feature]/
│               └── spec.md          # Delta (ADDED/MODIFIED/REMOVED)
│
├── archive/                         # Completed changes
│   └── [feature-name]/              # Full history of proposal + specs
│
├── project.md                       # ⭐ Read this first - Project conventions
├── AGENTS.md                        # AI assistant instructions
└── README.md                        # This file
```

## Specification Format

All specs use this structure:

```markdown
# Feature Name Specification

## Purpose
Brief description of what this spec covers.

## Requirements

### Requirement: Descriptive Name
The system SHALL [capability or constraint].

#### Scenario: Specific Condition
- WHEN [trigger event]
- THEN [expected outcome]

#### Scenario: Edge Case
- WHEN [exceptional condition]
- THEN [system behavior]
```

**Key Principles:**
- Use **SHALL** (recommended) or **MUST** for requirements
- Every requirement has **at least one scenario**
- Scenarios use **WHEN/THEN** format for clarity
- Focus on **observable behavior**, not implementation details

## Change Proposal Format

When proposing changes in `openspec/changes/`, use **delta notation**:

```markdown
## ADDED Requirements
### Requirement: New Feature
[Complete requirement text with scenarios]

## MODIFIED Requirements
### Requirement: Existing Feature (Updated)
[COMPLETE new text - not just the diff]

## REMOVED Requirements
### Requirement: Deprecated Feature
[Brief explanation of why it was removed]
```

## Current State

### ✅ Specs for Implemented Features
1. **Privacy Indicators** (`openspec/specs/privacy-indicators/spec.md`)
   - On-device indicator (3 variants)
   - Offline capability badge
   - Privacy advantages card
   - Competitive comparison card
   - **Status:** Fully implemented (commit acd0e18)

2. **Prompt Engineering** (`openspec/specs/prompt-engineering/spec.md`)
   - Persona system (5 variants)
   - First message injection
   - Token monitoring (architecture needs refactoring)
   - **Status:** Phase 1 complete, token monitoring disabled temporarily

### 🎯 Next Steps

When you're ready to add a new feature:

**Option 1: Start with OpenSpec**
```
User: Create an OpenSpec proposal for [feature]
AI: [Creates change folder with proposal, tasks, specs]
User: [Reviews and refines]
AI: [Implements according to specs]
User: Archive the change
```

**Option 2: Start with BMM Story**
```
User: Create Story 12 for [feature]
AI: [Creates story in .bmad/docs/stories/]
AI: Should I create an OpenSpec change proposal for this?
User: Yes
AI: [Creates openspec/changes/story-12-feature/]
```

## Benefits You'll See

### Before OpenSpec (Typical AI Workflow)
❌ "Add dark mode" → Vague requirements → Scope creep
❌ Requirements buried in chat history → Hard to verify
❌ Ambiguous ACs → Multiple interpretations
❌ No audit trail of what changed and why

### With OpenSpec
✅ "Add dark mode" → Proposal with clear requirements
✅ Specs in files → Easy to review and refine
✅ WHEN/THEN scenarios → Testable acceptance criteria
✅ Change history in archive → Full traceability

## Integration with BMM Workflow

### BMM Phases → OpenSpec Usage

| BMM Phase | OpenSpec Activity |
|-----------|-------------------|
| **Phase 1: Product Brief** | Read `openspec/project.md` for context |
| **Phase 2: PRD + Epics** | Suggest creating specs for complex epics |
| **Phase 3: Architecture** | Document decisions in OpenSpec specs |
| **Phase 4: Implementation** | Create change proposals for each story |
| **Post-Implementation** | Archive changes, update source specs |

### Story Workflow Example

```
BMM Story 11: Search Functionality
    ↓
OpenSpec Change: openspec/changes/story-11-search/
    ├── proposal.md (why search is needed, scope)
    ├── tasks.md (7 implementation steps)
    └── specs/search/spec.md (5 requirements, 12 scenarios)
    ↓
Implementation (referencing specs)
    ↓
Archive: openspec/archive/story-11-search/
Merge to: openspec/specs/search/spec.md
```

## How AI Uses This

When you work with Claude Code (or other AI assistants):

1. **AI reads** `openspec/AGENTS.md` to understand the workflow
2. **AI reads** `openspec/project.md` for your project conventions
3. **AI follows** the 4-phase cycle: Proposal → Review → Implement → Archive
4. **AI references** specs during implementation to stay on scope
5. **AI asks** clarifying questions when specs are ambiguous

## Common Commands

```bash
# Natural language (works with Claude Code)
"Create OpenSpec proposal for [feature]"
"Review the [feature] specs"
"Implement the [feature] change"
"Archive the [feature] change"

# If you install OpenSpec CLI (optional)
openspec list              # View active changes
openspec view              # Interactive dashboard
openspec show [change]     # Display change details
openspec validate [change] # Check spec formatting
openspec archive [change]  # Move to archive and merge specs
```

## Files to Commit

All files in `openspec/` should be committed to git:
- ✅ `project.md` - Project conventions
- ✅ `AGENTS.md` - AI instructions
- ✅ `README.md` - This documentation
- ✅ `specs/` - Source of truth specifications
- ✅ `changes/` - Active proposals (work in progress)
- ✅ `archive/` - Completed changes (historical record)

## Getting Help

- **OpenSpec Documentation:** https://github.com/Fission-AI/OpenSpec
- **Project Conventions:** Read `openspec/project.md`
- **AI Instructions:** See `openspec/AGENTS.md`
- **Example Specs:** Check `openspec/specs/privacy-indicators/spec.md`

## Pro Tips

### For Users
- Review proposals **before** implementation starts
- Ask "what scenarios did you consider?" if specs seem incomplete
- Use "archive" command only when ALL tasks are complete
- Check `openspec/specs/` to see current system behavior

### For AI Assistants
- Always read `openspec/project.md` before starting work
- Create proposals for changes affecting >3 files or >50 lines
- Never implement features not in the specs (scope creep!)
- Ask clarifying questions if scenarios are ambiguous
- Mark tasks complete in `tasks.md` as you implement them

---

**Ready to start?** Ask Claude Code to create an OpenSpec proposal for your next feature, or to create specs for an existing BMM story.
