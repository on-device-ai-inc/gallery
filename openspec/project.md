# OnDevice AI Gallery - Project Conventions

## Project Overview

**Name:** OnDevice AI Gallery
**Purpose:** Premium on-device AI inference Android application showcasing 100% local, privacy-first AI capabilities
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room Database, LiteRT-LM, Dagger Hilt
**Repository:** https://github.com/on-device-ai-inc/on-device-ai
**Primary Branch:** premium-ux-implementation

## Development Methodology

This project uses **BMad Method (BMM)** in combination with **OpenSpec (Spec-Driven Development)**:

- **BMM** provides the high-level workflow: Product Brief → PRD → Architecture → Epics → Stories → Implementation
- **OpenSpec** provides tactical spec-driven implementation: Proposal → Review → Tasks → Archive

### Integration Pattern

```
BMM Epic/Story (What & Why)
    ↓
OpenSpec Change Proposal (Detailed Requirements & Scenarios)
    ↓
OpenSpec Tasks (Implementation Checklist)
    ↓
Code Implementation (Referencing Specs)
    ↓
OpenSpec Archive (Merge to Source of Truth)
```

## File Locations

### BMM Artifacts (Strategic)
- Product Brief: `.bmad/docs/product-brief.md`
- PRD + Epics: `.bmad/docs/prd.md` + `.bmad/docs/epics/*.md`
- Architecture: `.bmad/docs/architecture.md`
- UX Design: `.bmad/docs/ux-design.md`
- Stories: `.bmad/docs/stories/*.md`
- Workflow Status: `.bmad/_cfg/workflow-status.yaml`

### OpenSpec Artifacts (Tactical)
- Specifications: `openspec/specs/[feature]/spec.md`
- Active Changes: `openspec/changes/[change-name]/`
- Archived Changes: `openspec/archive/[change-name]/`

## Code Conventions

### Package Structure
```
ai.ondevice.app/
├── data/                    # Models, DAOs, repositories
├── ui/
│   ├── common/              # Reusable composables
│   ├── llmchat/             # Chat screens and ViewModels
│   ├── settings/            # Settings and related screens
│   └── home_archived/       # Home/onboarding screens
├── di/                      # Hilt dependency injection
├── persona/                 # Persona system (prompt engineering)
└── compression/             # Token monitoring
```

### Naming Conventions
- **Files:** PascalCase (e.g., `ModelPageAppBar.kt`)
- **Composables:** PascalCase functions (e.g., `OnDeviceIndicator()`)
- **ViewModels:** Suffix with `ViewModel` (e.g., `LlmChatViewModel`)
- **Data classes:** PascalCase (e.g., `ConversationThread`)
- **Constants:** UPPER_SNAKE_CASE in companion objects

### Compose Guidelines
- Use Material 3 design system exclusively
- Follow existing color schemes: primary, secondary, tertiary containers
- Maintain consistent spacing (8.dp grid system)
- Accessibility: Provide contentDescription for all icons
- State hoisting: Lift state to ViewModels, not in composables

### Database Migrations
- Version bump in `AppDatabase.kt`
- Migration in `DatabaseMigrations.kt` with `MIGRATION_X_Y` pattern
- Test migrations locally before committing
- **Never skip migrations** - data loss risk

### Git Workflow
- **Feature branches** from `premium-ux-implementation`
- **Commit messages:** Follow conventional commits
  - `feat:` for new features
  - `fix:` for bug fixes
  - `refactor:` for code restructuring
  - Include story/epic reference when applicable
- **Build verification:** Must build in GitHub Actions (local AAPT2 cache issues)
- **Co-authored commits:** Include Claude attribution when AI-assisted

## Quality Standards

### Code Quality
- No over-engineering: Only implement what's requested
- No premature abstraction: Don't create utilities for single-use code
- No unused code: Delete, don't comment out
- No backwards-compatibility hacks: Delete cleanly
- Minimal error handling: Only at system boundaries (user input, external APIs)

### Security
- No hardcoded credentials
- No data transmission to external servers (100% on-device)
- Validate user input at UI boundaries
- Use ProGuard/R8 for release builds

### Testing
- Manual testing required for all UI changes
- Verify all ACs (Acceptance Criteria) from stories
- Test on physical device when possible
- Check offline functionality

## Current Epic: Premium UX Implementation

**Goal:** Transform basic gallery app into premium on-device AI experience

**Completed Stories (10):**
1. ✅ Premium Error Experience
2. ✅ Accessibility Compliance (WCAG 2.2 Level AA)
3. ✅ Privacy Center
4. ✅ Context-Aware Permission Flow
5. ✅ Terms of Service Experience
6. ✅ Model Configuration Presets
7. ✅ Custom Instructions System
8. ✅ Storage Management UI
9. ✅ Privacy Differentiation Indicators
10. ✅ Prompt Engineering - Phase 1 (Persona System)

**Current Focus:**
- Fixing build errors (Story 10 integration)
- Runtime verification of privacy indicators
- Future: Prompt Engineering Phase 2-4 (Compression, UI, Testing)

## Known Issues

### Build Environment
- **Local AAPT2 cache corruption:** Cannot build locally, must use GitHub Actions
- **Workaround:** Commit + push to premium-ux-implementation → GitHub CI/CD builds APK

### Active Investigations
- Why previous privacy integration work was lost (suspected session/commit issues)
- Token monitoring architecture (requires protected access to ChatViewModel internals)

## Development Principles

1. **Read before writing:** Always read files before modifying them
2. **Verify git tracking:** Check `git status` after major changes
3. **Test before committing:** Verify code compiles (via GitHub Actions)
4. **Document decisions:** Use OpenSpec proposals for non-trivial changes
5. **Stay on scope:** Reference story ACs, don't add unasked features
6. **Ask when unclear:** Use AskUserQuestion for ambiguous requirements

## Glossary

- **AC:** Acceptance Criteria (from story definitions)
- **BMM:** BMad Method (project management workflow)
- **LiteRT-LM:** On-device language model runtime (Google)
- **Gemma:** Small language model (2B/7B parameters)
- **Story:** Smallest unit of user-facing value in BMM
- **Epic:** Collection of related stories delivering a feature
- **Persona:** System prompt injected to guide AI behavior
- **Token:** Unit of text processed by LLM (≈4 characters for Gemma)
