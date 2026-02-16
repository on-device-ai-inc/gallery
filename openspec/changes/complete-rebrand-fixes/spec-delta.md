# Spec Delta: complete-rebrand-fixes

## ADDED

### rebrand-completion-checklist.md

```markdown
# Rebrand Completion Checklist

## Overview
This specification defines the comprehensive checklist for completing a codebase rebrand, ensuring no runtime failures or documentation inconsistencies remain.

## Scope
When rebranding an application (package name, app name, repository), this checklist ensures all references are updated across code, documentation, and external integrations.

## Checklist

### 1. Runtime Code References
- [ ] **GitHub API Endpoints**: Search for hardcoded repository URLs in API clients
  - Pattern: `github.com/<old-org>/<old-repo>`
  - Files: Notification systems, update checkers, model downloaders

- [ ] **External Resource URLs**: Search for raw content URLs
  - Pattern: `raw.githubusercontent.com/<old-org>/<old-repo>`
  - Files: ViewModels, repositories, data fetchers

- [ ] **Documentation URLs**: Search for docUrl, helpUrl, learnMoreUrl properties
  - Pattern: `github.com/<old-org>/<old-repo>/tree/main/<old-package-path>`
  - Files: Task modules, feature modules, help systems

### 2. Package Name References
- [ ] **AndroidManifest.xml**: package, android:label, deep link schemes
- [ ] **build.gradle.kts**: namespace, applicationId, manifestPlaceholders
- [ ] **settings.gradle.kts**: rootProject.name
- [ ] **Proto definitions**: package declarations
- [ ] **All source files**: package declarations, imports

### 3. User-Facing Documentation
- [ ] **README.md**: Title, package ID, repository URLs, Play Store links
- [ ] **Module READMEs**: All subdirectory README files
- [ ] **GitHub Templates**: Issue templates, PR templates
- [ ] **Wiki Links**: Update documentation references
- [ ] **Installation Instructions**: Package IDs, deep links

### 4. Administrative Documentation
- [ ] **Planning Docs**: Architecture documents, status files
- [ ] **Legal Docs**: License files (if org/owner changed)
- [ ] **Configuration Files**: CI/CD configs, workflow files

### 5. Verification Steps
- [ ] **Grep Test**: `grep -r "<old-org>" . --exclude-dir={.git,build,node_modules}`
- [ ] **Runtime Test**: Verify app fetches external resources successfully
- [ ] **Link Test**: Click all in-app help/documentation links
- [ ] **CI Test**: Verify builds pass with new references

## Known Pitfalls

1. **Hardcoded Constants**: Constants declared as `private const val REPO = "old/repo"`
2. **String Templates**: URLs built from string concatenation may hide old references
3. **Comment Links**: Even comments with old links confuse developers
4. **Test Fixtures**: Test data may reference old package names or URLs

## Evidence Requirements

- [ ] TDD: Tests verify new endpoints resolve correctly
- [ ] CI: Build passes with all new references
- [ ] Visual: DroidRun verifies runtime functionality (model downloads, help links)
- [ ] Grep: No matches for old org/repo in runtime code

## References
- Based on: OnDevice AI rebrand forensic investigation (2026-02-16)
- Agent: abb7b33
- Commit: 95a4df7
```

---

## MODIFIED

### v1.1.9/OPENSPEC-FOUNDATION.md

```diff
 | **Original Product** | Google AI Edge Gallery (rebranded) | Git history |
+| **Previous Repository** | https://github.com/google-ai-edge/gallery | Historical reference |
+| **Current Repository** | https://github.com/anthropics/on-device | Active development |

 # Dependencies

 | Dependency | Version | Purpose |
 |------------|---------|---------|
 | com.google.ai.edge.litertlm:litertlm-android | 0.9.0-alpha01 | LiteRT LLM runtime |
+
+**Note**: LiteRT dependency retains `com.google.ai.edge` namespace as it's an external library owned by Google. Only OnDevice AI app code uses `ai.ondevice.app` namespace.

-**Original Source**: https://github.com/google-ai-edge/gallery
+**Migration Notes**:
+- **Original Source**: https://github.com/google-ai-edge/gallery (archived)
+- **Rebrand Completed**: 2026-02-11 (commit 95a4df7)
+- **Rebrand Finalized**: 2026-02-16 (commit TBD) - fixed remaining runtime references
```

---

## REMOVED

None. All existing specifications remain valid. This change only updates historical references and adds a reusable rebrand checklist for future projects.

---

## Validation

After applying this spec delta:

1. **Consistency Check**: `grep -r "google-ai-edge" openspec/specs/` should return:
   - OPENSPEC-FOUNDATION.md: Historical references (acceptable)
   - rebrand-completion-checklist.md: Example patterns (acceptable)
   - No other matches

2. **Completeness Check**: All runtime code updated per rebrand-completion-checklist.md

3. **Traceability**: OPENSPEC-FOUNDATION.md maintains historical context for future developers

---

## Implementation Notes

- The new `rebrand-completion-checklist.md` will be added to `openspec/specs/patterns/` directory (create if needed)
- OPENSPEC-FOUNDATION.md changes clarify the rebrand timeline and distinguish between external dependencies (keep old namespace) vs app code (new namespace)
- This spec delta will be merged during `/openspec-archive` after all tasks complete
