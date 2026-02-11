# Active OpenSpec Proposals - Phase 1 Emergency Fixes

**Status**: All proposals awaiting user approval before implementation

**Created**: 2026-01-11

**Context**: PRD v3.0 MASTER gap analysis identified critical issues requiring immediate fixes. These OpenSpec proposals address Phase 1 emergency priorities.

---

## Summary

| # | Proposal | Priority | Effort | Status | Next Step |
|---|----------|----------|--------|--------|-----------|
| 1 | [web-search-fix](#1-web-search-fix) | P0 | 2-3 days (16h) | 📝 PROPOSAL | `/openspec-apply web-search-fix` |
| 2 | [profile-settings-cursor-fix](#2-profile-settings-cursor-fix) | P0 | 3.5 hours | 📝 PROPOSAL | `/openspec-apply profile-settings-cursor-fix` |
| 3 | [context-compression-rebuild](#3-context-compression-rebuild) | P0 | 6 days (48h) | 📝 PROPOSAL | `/openspec-apply context-compression-rebuild` |
| 4 | [crashlytics-integration](#4-crashlytics-integration) | P0 | 1 day (7.5h) | 📝 PROPOSAL | `/openspec-apply crashlytics-integration` |

**Total Effort**: ~11 days (87.5 hours) for all Phase 1 fixes

---

## Recommended Implementation Order

### Option A: Quick Wins First (Recommended)
**Rationale**: Build momentum with faster fixes, tackle complex rebuilds last

```
1. profile-settings-cursor-fix     (3.5h)  ✅ Quick UX fix
2. crashlytics-integration         (7.5h)  ✅ Enables monitoring for next fixes
3. web-search-fix                  (16h)   ✅ Investigate + fix feature
4. context-compression-rebuild     (48h)   ✅ Major rebuild (largest scope)
```

**Timeline**:
- Week 1: Tasks #1-2 (11h total) → 2 issues resolved, monitoring enabled
- Week 2-3: Task #3 (16h) → Web search working
- Week 4-5: Task #4 (48h) → Compression system rebuilt

### Option B: Critical Path First
**Rationale**: Fix user-facing bugs immediately, infrastructure later

```
1. profile-settings-cursor-fix     (3.5h)  ✅ Blocks profile editing
2. web-search-fix                  (16h)   ✅ Feature appears broken
3. context-compression-rebuild     (48h)   ✅ Blocks long conversations
4. crashlytics-integration         (7.5h)  ✅ Monitoring (not user-facing)
```

**Timeline**:
- Week 1: Tasks #1-2 (19.5h) → Key UX issues resolved
- Week 2-5: Task #3 (48h) → Compression working
- Week 6: Task #4 (7.5h) → Monitoring enabled

### Option C: Parallel Development (If Team Size Allows)
**Rationale**: Multiple developers can work simultaneously on different proposals

```
Developer 1: profile-settings-cursor-fix + web-search-fix (19.5h)
Developer 2: crashlytics-integration (7.5h)
Developer 3: context-compression-rebuild (48h)
```

**Timeline**: ~2 weeks (all work parallel)

---

## 1. web-search-fix

**Location**: `openspec/changes/web-search-fix/`

### Problem
- Web search toggle UI works, but app is **NOT fetching actual results** from Brave Search API
- Users think search is enabled but get no search data (silent failure)
- P0 Critical: Broken feature that users think is working

### Solution Approach
1. **Investigation** (Day 1, 4h): Systematic root cause analysis with 6 hypotheses
2. **Fix** (Day 2, 6h): Implement fix + error handling + logging
3. **Testing** (Day 3, 6h): Manual + automated validation

### Key Files
- **proposal.md**: Detailed investigation plan, 6 root cause hypotheses
- **tasks.md**: 25 tasks (investigation → fix → test → validate)
- **spec-delta.md**: Modified web-search.md, added error-handling-patterns.md

### Acceptance Criteria (Summary)
- [ ] Search fetches results from Brave API when enabled
- [ ] Results appear in AI response
- [ ] Rate limiting (5/day) works with clear messages
- [ ] Error handling (network, API, rate limit) with user feedback
- [ ] Logging for debugging

### Deliverables
- Working web search (backend + UI)
- Error handling patterns documentation
- Unit + integration tests

---

## 2. profile-settings-cursor-fix

**Location**: `openspec/changes/profile-settings-cursor-fix/`

### Problem
- Profile settings text inputs (Full Name, Nickname) have cursor jumping bug
- When typing "Nathan", cursor moves back → displays "nahtaN"
- P0 Critical: Breaks basic profile editing

### Root Cause (Hypothesis)
- Using `String` state instead of `TextFieldValue` in Compose TextField
- State updates lose cursor position (selection)

### Solution Approach
1. **Investigation** (1h): Identify root cause (likely String vs TextFieldValue)
2. **Fix** (1h): Change state to TextFieldValue
3. **Testing** (1h): Manual + DroidRun visual verification
4. **Prevention** (30m): Document pattern in error-handling-patterns.md

### Key Files
- **proposal.md**: Root cause hypotheses, TextField best practices
- **tasks.md**: 20 tasks (investigate → fix → test → document pattern)
- **spec-delta.md**: Modified error-handling-patterns.md, added profile-settings.md

### Acceptance Criteria (Summary)
- [ ] User can type "Nathan" → displays "Nathan" (not scrambled)
- [ ] Cursor stays at end after each character
- [ ] Backspace deletes from end
- [ ] Long names (30+ chars) work correctly

### Deliverables
- Fixed text inputs (Full Name, Nickname)
- TextField best practices documentation
- DroidRun visual verification

---

## 3. context-compression-rebuild

**Location**: `openspec/changes/context-compression-rebuild/`

### Problem
- **User Report**: "CONTEXT COMPRESSION NOT WORKING PROPERLY, DELETE THE ENTIRE IMPLEMENTATION AND START FRESH"
- 520+ lines of broken code across 4 files (TokenMonitor, ContextManager, ConversationCompressor, QualityMonitor)
- P0 Critical: Long conversations (100+ messages) break when hitting 4096 token limit

### Solution Approach
1. **Delete** (Phase 0, 1h): Remove all 520 lines of broken code
2. **Design** (Phase 1, 4h): Design v2 architecture (TokenCounter, ContextCompressor v2, CompressionStrategy, CompressionQualityMonitor)
3. **Implement** (Phase 2, 2 days): Build core components + integration
4. **Golden QA Dataset** (Phase 3, 1 day): Create GQA-006 with 50+ test cases
5. **Test & Validate** (Phase 4, 2 days): Unit + integration + golden QA evaluation
6. **Monitor** (Phase 5, 1 day): Metrics, drift detection, Firebase Analytics

### Key Files
- **proposal.md**: Complete v2 architecture, 6-phase plan, 6-day timeline
- **tasks.md**: 70 tasks (delete → design → implement → test → monitor → document)
- **spec-delta.md**: Removed v1 spec, added context-compression-v2.md, modified evaluation-framework.md (GQA-006)

### Acceptance Criteria (Summary)
- [ ] Conversations support 100+ messages without breaking
- [ ] Compression reduces tokens by ≥20%
- [ ] Response quality ≥90% post-compression (GQA-006 evaluation)
- [ ] Last 10 messages + starred messages always preserved
- [ ] Token counting accurate within ±5%
- [ ] Compression latency <500ms
- [ ] Drift detection alerts when quality <85%

### Deliverables
- New compression system (4 components, 6 strategies)
- GQA-006 golden dataset (50+ test cases)
- Quality monitoring + drift detection
- Comprehensive unit + integration tests

---

## 4. crashlytics-integration

**Location**: `openspec/changes/crashlytics-integration/`

### Problem
- **ZERO crash reporting** in production app (v1.1.9, Build 35)
- Users experiencing crashes with NO way to diagnose
- Cannot measure stability (crash-free rate unknown)
- P0 Critical: Cannot operate production app responsibly without crash reporting

### Solution Approach
1. **Firebase Setup** (Phase 1, 1h): Create project, download google-services.json
2. **Integration** (Phase 2, 2h): Initialize Crashlytics, add custom logging
3. **ProGuard** (Phase 3, 1h): Configure mapping file upload for deobfuscation
4. **Testing** (Phase 4, 2h): Test debug + release builds, verify stack traces readable
5. **Monitoring** (Phase 5, 1h): Configure alerts (new issues, crash-free rate, velocity)
6. **Privacy** (Phase 6, 30m): Verify NO PII logged

### Key Files
- **proposal.md**: Complete Firebase integration plan, privacy compliance
- **tasks.md**: 53 tasks (setup → integrate → test → monitor → document)
- **spec-delta.md**: Added crashlytics.md, modified analytics-monitoring.md

### Acceptance Criteria (Summary)
- [ ] Fatal crashes automatically reported to Firebase
- [ ] Stack traces include file names/line numbers (not obfuscated)
- [ ] Custom logs visible in crash reports (model load, inference, compression events)
- [ ] User ID (anonymized) attached to crashes
- [ ] Non-fatal errors logged
- [ ] Crash-free rate >99.5% tracked
- [ ] Alerts configured (new issue, crash-free rate drop, velocity spike)
- [ ] NO PII logged (verified via code review)

### Deliverables
- Firebase Crashlytics integrated
- CrashlyticsLogger utility (centralized logging)
- Alerts + monitoring dashboard
- Privacy compliance (NO PII)
- ProGuard deobfuscation working

---

## How to Proceed

### Step 1: Review Proposals
For each proposal, review:
- **proposal.md**: What, why, how (technical approach)
- **tasks.md**: Detailed task breakdown
- **spec-delta.md**: Specification changes

### Step 2: Approve or Request Changes
For each proposal, respond with:
- ✅ **Approved** - proceed with implementation
- 🔄 **Request changes** - specify what needs adjustment
- ❌ **Reject** - explain why (shouldn't happen if following requirements)

### Step 3: Implement (After Approval)
Once approved, run:
```bash
/openspec-apply <proposal-name>
```

This will:
- Execute tasks autonomously
- Run TDD loop (RED → GREEN → REFACTOR)
- Run CI loop (push → test → fix → repeat until green)
- Run visual verification loop (screenshot → verify → fix → repeat)
- Update tasks.md (check off completed tasks)

### Step 4: Archive (After Completion)
When all tasks done, run:
```bash
/openspec-archive <proposal-name>
```

This will:
- Verify all tasks complete
- Merge spec-delta.md → openspec/specs/
- Move proposal to openspec/archive/
- Update LESSONS_LEARNED.md

---

## Questions to Consider

### Scope
- **Do all 4 proposals align with your priorities?**
- **Are there any proposals you want to defer to Phase 2?**
- **Are there additional emergency fixes not covered?**

### Approach
- **Do the technical approaches make sense?**
- **Are there alternative solutions you prefer?**
- **Are the effort estimates realistic?**

### Timeline
- **Which implementation order do you prefer?** (Option A, B, or C)
- **Do you have multiple developers?** (affects parallel work)
- **Are there external deadlines driving priority?**

---

## Next Steps

**User Decision Required**:
1. Review each proposal (proposal.md, tasks.md, spec-delta.md)
2. Choose implementation order (Option A, B, or C)
3. Approve proposals (say "approved" or specify changes)
4. Claude will execute `/openspec-apply` for each approved proposal

**Estimated Total Time**:
- Option A (sequential, quick wins first): ~5-6 weeks
- Option B (sequential, critical path): ~6 weeks
- Option C (parallel, 3 developers): ~2 weeks

---

## Additional Notes

### User-Reported Bugs Covered
✅ All user-reported bugs from gap analysis are addressed:
1. ✅ **Web search not fetching** → web-search-fix
2. ✅ **Profile settings cursor jumping** → profile-settings-cursor-fix
3. ✅ **Context compression broken** → context-compression-rebuild
4. ✅ **No crash reporting** → crashlytics-integration

### PRD v3.0 MASTER Alignment
✅ All proposals align with Phase 1 emergency fixes:
- Epic 9: Web Search (web-search-fix)
- Epic 11: Analytics & Monitoring (crashlytics-integration)
- Epic 8: Context Compression (context-compression-rebuild)
- UX Quality: Profile Settings (profile-settings-cursor-fix)

### OpenSpec Workflow
Each proposal follows OpenSpec methodology:
- **Proposal**: Spec-first design (BEFORE any code)
- **Apply**: TDD implementation (RED → GREEN → REFACTOR)
- **Archive**: Merge to specs/, move to archive/

This ensures:
- ✅ Spec adherence (no scope creep)
- ✅ Quality gates (tests must pass)
- ✅ Evidence-based validation (screenshots, CI logs)
