# OnDevice AI - Product Requirements Document v3.0 (MASTER)

**Document Version**: 3.0.0
**Last Updated**: 2026-01-11
**Current App Version**: 1.1.9 (Build 35)
**Methodology**: AI Mobile PM + OpenSpec-Driven Development
**Status**: VERIFIED AGAINST CODEBASE (forensic analysis complete)

---

## DOCUMENT PURPOSE

This is the **SINGLE SOURCE OF TRUTH** for OnDevice AI product development. It combines:
1. **Verified Reality**: Forensic codebase analysis (50+ files verified)
2. **AI Mobile PM Methodology**: Evaluation frameworks, golden QA datasets, latency SLAs
3. **OpenSpec Integration**: Each feature will have dedicated OpenSpec proposal before implementation
4. **User Corrections**: Context compression broken, web search buggy, advanced features in MVP1

---

## EXECUTIVE SUMMARY

### Product Vision
**OnDevice AI**: The world's first 100% offline-capable mobile AI assistant with **systematic quality guarantees** for users in emerging markets (Zimbabwe, Kenya) and privacy-conscious users globally.

### Core Differentiators
1. **100% Privacy** - All inference on-device (zero data transmission)
2. **Offline-First** - Full functionality without internet after model download
3. **Quality Parity** - Comparable to cloud AI (>80% task completion on golden QA)
4. **Low Latency** - TTFT <1s (p95), decode >20 tok/s (p95)
5. **Accessible** - $8 one-time purchase, unlimited usage

### Current State (v1.1.9)
**App Completion**: ~35% (verified)
- ✅ **Core chat working**: 6 models, streaming, markdown, regenerate
- ✅ **Multimodal working**: Vision (images), audio, voice input
- ✅ **Persona system working**: 5 variants, custom instructions
- 🔴 **Context compression BROKEN**: User-reported, needs DELETE & REBUILD
- 🔴 **Web search BUGGY**: Toggle works, but NOT fetching results
- ❌ **Evaluation framework MISSING**: No golden QA, no LLM-judge, no drift detection
- ❌ **Analytics MISSING**: No Crashlytics, no event tracking, no performance monitoring

---

## SUCCESS METRICS

### North Star Metric
**Weekly Active Users (WAU)** having meaningful AI conversations (≥5 messages/week)

### Model Quality Metrics (TARGETS)

| Metric | Target | Alert Threshold | Measurement Method |
|--------|--------|-----------------|-------------------|
| **Task completion rate** | >80% | <70% | Daily LLM-judge eval on golden QA |
| **Hallucination rate** | <2% | >5% | Daily LLM-judge eval (500 samples) |
| **Safety violation rate** | <0.1% | >0.5% | Real-time code-based guardrails |
| **Factual accuracy** | >85% | <75% | Weekly human eval on fact claims |
| **Thumbs up ratio** | >70% | <50% | In-app feedback (continuous) |

### Performance Metrics (TARGETS)

| Metric | Target (p50) | Target (p95) | Alert | Measurement |
|--------|--------------|--------------|-------|-------------|
| **TTFT** | <500ms | <1000ms | >2000ms | Client instrumentation |
| **Decode speed** | >30 tok/s | >20 tok/s | <15 tok/s | Client instrumentation |
| **Model init (cold)** | <3s | <5s | >10s | Performance monitoring |
| **Model init (warm)** | <100ms | <200ms | >500ms | Performance monitoring |

### User Satisfaction Metrics (TARGETS)

| Metric | Target | Alert | Measurement |
|--------|--------|-------|-------------|
| **Regenerate rate** | <20% | >35% | Analytics event |
| **Session abandonment** | <15% | >25% | Analytics funnel |
| **7-day retention** | >60% | <40% | Analytics cohort |
| **30-day retention** | >40% | <25% | Analytics cohort |

### Business Metrics (TARGETS)

| Metric | Target | Measurement |
|--------|--------|-------------|
| **DAU/MAU ratio** | >35% | Analytics |
| **Crash-free rate** | >99.5% | Firebase Crashlytics |
| **ANR rate** | <0.5% | Play Console |
| **App rating** | >4.5★ | Google Play |

---

## CRITICAL ISSUES (MUST FIX FIRST)

### 🔴 Issue 1: Context Compression BROKEN
**Severity**: P0 (Critical)
**User Report**: "CONTEXT COMPRESSION NOT WORKING PROPERLY"
**Status**: ❌ System exists (520+ lines) but NOT functioning correctly
**Action Required**: DELETE entire implementation, REBUILD from scratch
**Files to Delete**:
- `TokenMonitor.kt`
- `ContextManager.kt`
- `ConversationCompressor.kt`
- `QualityMonitor.kt`

**Impact**: Users lose conversation context, quality degrades over time
**Estimated Fix**: 5-7 days (full rebuild with proper testing)
**OpenSpec Required**: Yes (create compression-v2 spec)

---

### 🔴 Issue 2: Web Search NOT Fetching Results
**Severity**: P0 (Critical)
**User Report**: "APP DOESNT SEEM TO BE FETCHING ACTUAL RESULTS, INVESTIGATION NEEDED THEN FIX"
**Status**: ⚠️ Toggle UI works, backend code exists, but NOT actually fetching
**Action Required**: INVESTIGATE root cause, then FIX
**Files to Investigate**:
- `SearchRepository.kt` (130 lines)
- `WebSearchPreferencesDataStore.kt`
- `BraveSearchService.kt`
- `LlmChatViewModel.kt` (integration)

**Potential Issues**:
- API key not configured correctly?
- Network permission missing?
- Brave API endpoint changed?
- Rate limiting blocking all requests?
- Integration not triggered when toggle enabled?

**Impact**: Feature appears enabled but doesn't work (user trust issue)
**Estimated Fix**: 2-3 days (investigation + fix + testing)
**OpenSpec Required**: Yes (create web-search-fix spec)

---

## FEATURE INVENTORY - VERIFIED STATUS

### EPIC 1: CORE CHAT AI FEATURES

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **CHAT-001** | Multi-Turn LLM Chat | ✅ DONE | ChatViewModel.kt, ChatPanel.kt | Golden QA missing |
| **CHAT-002** | Response Streaming | ✅ DONE | 75ms batching, 13fps, 0% token loss | None |
| **CHAT-003** | Markdown Rendering | ✅ DONE | H1-H6, lists, code, tables, syntax highlighting | None |
| **CHAT-004** | Regenerate (4 styles) | ✅ DONE | FASTER, MORE_DETAILED, DIFFERENT, SHORTER | LLM-judge eval missing |
| **CHAT-005** | Model Initialization | ✅ DONE | <5s cold start, GPU fallback | None |
| **CHAT-006** | Continue Generating | ❌ NOT STARTED | No truncation detection, no UI | **ENTIRE FEATURE MISSING** |
| **CHAT-007** | Follow-up Suggestions | ❌ NOT STARTED | No suggestion generation | **ENTIRE FEATURE MISSING** |

**Epic 1 Completion**: 5/7 features (71%)

---

### EPIC 2: MULTIMODAL AI FEATURES

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **MULTI-001** | Vision Input (Images) | ✅ DONE | Gemma-3n models only, up to 10 images | Vision golden QA missing |
| **MULTI-002** | Audio Input | ✅ DONE | Gemma-3n models only, up to 30s | Audio golden QA missing |
| **MULTI-003** | Voice Input (STT) | ✅ DONE | Android SpeechRecognizer | None |

**Epic 2 Completion**: 3/3 features (100%), but evaluation infrastructure missing

---

### EPIC 3: CONTEXT MANAGEMENT (CRITICAL)

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **CTX-001** | Token Monitoring | ⚠️ BROKEN | TokenMonitor.kt exists, but NOT working | **DELETE & REBUILD** |
| **CTX-002** | Auto Compression | 🔴 **BROKEN** | User-reported: NOT WORKING PROPERLY | **DELETE & REBUILD** |
| **CTX-003** | Quality Monitoring | 🔴 **BROKEN** | QualityMonitor.kt exists, but NOT working | **DELETE & REBUILD** |

**Epic 3 Completion**: 0/3 features (0%) - **ENTIRE SYSTEM BROKEN**

**USER DIRECTIVE**: "DELETE THE ENTIRE IMPLEMENTATION AND START FRESH"

---

### EPIC 4: PERSONA SYSTEM

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **PERS-001** | Persona Injection | ✅ DONE | 5 variants (MAXIMUM to MINIMAL) | Effectiveness not measured |
| **PERS-002** | Custom Instructions | ✅ DONE | Global system prompt, DataStore persistence | Adherence not measured |

**Epic 4 Completion**: 2/2 features (100%), but evaluation missing

---

### EPIC 5: WEB SEARCH (CRITICAL)

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **WEB-001** | Web Search Integration | 🔴 **BUGGY** | Toggle works, backend exists, NOT fetching | **INVESTIGATE & FIX** |

**Epic 5 Completion**: 0/1 features (0%) - **BROKEN**

**USER DIRECTIVE**: "INVESTIGATION NEEDED THEN FIX"

---

### EPIC 6: IMAGE GENERATION

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **IMG-001** | Text-to-Image (Stable Diffusion) | ❌ BROKEN | MediaPipe init fails | **DEFER TO MVP2** |

**Epic 6 Completion**: 0/1 features (0%)

**USER DIRECTIVE**: "image generation is MVP2" (not immediate priority)

---

### EPIC 7: EVALUATION FRAMEWORK (CRITICAL)

| Component ID | Component | Status | Gap |
|--------------|-----------|--------|-----|
| **EVAL-001** | Golden QA: General Chat (GQA-001) | ❌ NOT CREATED | **150 prompts needed** |
| **EVAL-002** | Golden QA: Vision (GQA-002) | ❌ NOT CREATED | **100 prompts + images needed** |
| **EVAL-003** | Golden QA: Audio (GQA-003) | ❌ NOT CREATED | **50 prompts + audio needed** |
| **EVAL-004** | Golden QA: Code Gen (GQA-004) | ❌ NOT CREATED | **100 prompts needed** |
| **EVAL-005** | Golden QA: Safety (GQA-005) | ❌ NOT CREATED | **100 prompts needed** |
| **EVAL-006** | Golden QA: Context (GQA-006) | ❌ NOT CREATED | **50 multi-turn needed** |
| **EVAL-007** | LLM-Judge Evaluation System | ❌ NOT IMPLEMENTED | **Claude Sonnet 4 integration needed** |
| **EVAL-008** | Code-Based Evals | ⚠️ PARTIAL | Safety filters basic, need format validation |
| **EVAL-009** | Human Eval UI (Thumbs Up/Down) | ❌ NOT IMPLEMENTED | **UI component missing** |
| **EVAL-010** | Drift Detection Dashboard | ❌ NOT IMPLEMENTED | **Monitoring dashboard needed** |
| **EVAL-011** | Rollback Playbook | ❌ NOT DOCUMENTED | **Emergency response doc needed** |

**Epic 7 Completion**: 0/11 components (0%) - **ENTIRE FRAMEWORK MISSING**

**Impact**: Flying blind - cannot measure quality, detect drift, or rollback issues

---

### EPIC 8: ANALYTICS & MONITORING (CRITICAL)

| Component ID | Component | Status | Gap |
|--------------|-----------|--------|-----|
| **MON-001** | Firebase Analytics Events | ⚠️ PARTIAL | SDK configured, events NOT logged | **Event tracking needed** |
| **MON-002** | Firebase Crashlytics | ❌ NOT IMPLEMENTED | No crash reporting | **CRITICAL: Add immediately** |
| **MON-003** | Firebase Performance | ❌ NOT IMPLEMENTED | No latency tracking (TTFT, decode) | **Performance monitoring needed** |
| **MON-004** | Success Metrics Dashboard | ❌ NOT TRACKING | DAU/MAU, retention not measured | **Dashboard needed** |

**Epic 8 Completion**: 0/4 components (0%) - **NO PRODUCTION MONITORING**

**Impact**: Cannot detect crashes, performance degradation, or measure success

---

### EPIC 9: CONVERSATION MANAGEMENT

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **CONV-001** | Conversation Persistence | ✅ DONE | Room v4, auto-save | None |
| **CONV-002** | Conversation List | ✅ DONE | Date grouping, search | None |
| **CONV-003** | Conversation Search | ✅ DONE | Full-text search | None |
| **CONV-004** | Star/Favorite | ✅ DONE | isStarred field | None |
| **CONV-005** | Rename | ✅ DONE | updateTitle() method | None |
| **CONV-006** | Delete | ✅ DONE | Confirmation dialog, CASCADE | None |
| **CONV-007** | Archive | ❌ NOT STARTED | No is_archived column | **DB migration + UI needed** |
| **CONV-008** | Multi-Select | ❌ NOT STARTED | No selection mode | **UI feature needed** |
| **CONV-009** | Swipe Gestures | ❌ NOT STARTED | No swipe handlers | **UI feature needed** |
| **CONV-010** | Undo Delete | ❌ NOT STARTED | No soft delete | **DB migration + UI needed** |
| **CONV-011** | Folders | ❌ NOT STARTED | No folder tables | **DB migration + full feature** |
| **CONV-012** | Tags | ❌ NOT STARTED | No tag tables | **DB migration + full feature** |
| **CONV-013** | Edit User Messages | ⚠️ PARTIAL | DAO exists, schema missing | **DB migration v4→v5** |
| **CONV-014** | Message Branching | ❌ NOT STARTED | No branch schema | **DB migration + UI** |
| **CONV-015** | Export | ✅ DONE | SettingsViewModel.kt:181 | None |
| **CONV-016** | Import | ❌ NOT STARTED | No import logic | **Full feature needed** |

**Epic 9 Completion**: 7/16 features (44%)

**USER DIRECTIVE**: Advanced features (archive, folders, tags, edit, branching) ARE IN SCOPE for MVP1

---

### EPIC 10: MODEL MANAGEMENT

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **MODEL-001** | Model Download | ✅ DONE | WorkManager, OAuth, resume support | None |
| **MODEL-002** | Model Deletion | ✅ DONE | Free storage immediately | None |
| **MODEL-003** | Model Selection | ✅ DONE | Top bar dropdown, 6 models | None |
| **MODEL-004** | First Launch | ✅ DONE | Auto-suggest best model | None |
| **MODEL-005** | Parameter Tuning | ✅ DONE | Temperature, top-k, top-p, max tokens | None |

**Epic 10 Completion**: 5/5 features (100%)

---

### EPIC 11: SETTINGS & PERSONALIZATION

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **SET-001** | Theme Selection | ✅ DONE | Light/Dark/System | None |
| **SET-002** | Text Size | ✅ DONE | Small/Medium/Large | None |
| **SET-003** | User Profile | ✅ DONE | Full name, nickname | None |
| **SET-004** | Storage Management | ⚠️ PARTIAL | Count exists, size calc missing | **Size calculation needed** |
| **SET-005** | Auto-Cleanup | ✅ DONE | Never/30d/90d/1yr | None |
| **SET-006** | Privacy Center | ✅ DONE | Policy, permissions | None |
| **SET-007** | About & Licenses | ✅ DONE | Version, OSS licenses | None |

**Epic 11 Completion**: 6/7 features (86%)

---

### EPIC 12: MESSAGE ACTIONS

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **MSG-001** | Copy Message | ✅ DONE | Clipboard API | None |
| **MSG-002** | Share Message | ✅ DONE | Android share intent | None |
| **MSG-003** | Delete Message | ✅ DONE | Confirmation dialog | None |
| **MSG-004** | Text-to-Speech | ✅ DONE | Android TTS | None |
| **MSG-005** | Thumbs Up/Down | ❌ NOT STARTED | No UI component | **CRITICAL: Blocks quality measurement** |

**Epic 12 Completion**: 4/5 features (80%)

---

### EPIC 13: PLATFORM FEATURES

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **PLAT-001** | Google Play Billing | ❌ NOT NEEDED | User correction: NOT REQUIRED | ℹ️ **Removed from scope** |
| **PLAT-002** | Landscape Mode | ❌ NOT STARTED | Portrait locked in manifest | **Remove lock + responsive layout** |
| **PLAT-003** | Tablet Two-Pane | ❌ NOT STARTED | No master-detail layout | **Tablet optimization needed** |

**Epic 13 Completion**: 0/3 features (0%), but Billing removed from scope

---

### EPIC 14: TESTING & QA

| Feature ID | Feature | Status | Evidence | Gap |
|------------|---------|--------|----------|-----|
| **TEST-001** | Maestro UI Tests | ✅ DONE | 9 flows covering core journeys | None |
| **TEST-002** | Unit Tests | ⚠️ PARTIAL | DAO tests exist, ViewModel incomplete | **Coverage <50%** |
| **TEST-003** | Integration Tests | ❌ NOT STARTED | No migration tests | **DB migration testing needed** |
| **TEST-004** | Performance Benchmarks | ❌ NOT STARTED | No automated latency tests | **TTFT/decode benchmarks needed** |

**Epic 14 Completion**: 1.5/4 features (38%)

---

## OVERALL COMPLETION SUMMARY

| Epic | Features Complete | Completion % | Status |
|------|-------------------|--------------|--------|
| Epic 1: Core Chat | 5/7 | 71% | ⚠️ Missing continue + suggestions |
| Epic 2: Multimodal | 3/3 | 100% | ✅ Complete (eval missing) |
| Epic 3: Context Mgmt | 0/3 | 0% | 🔴 **BROKEN - DELETE & REBUILD** |
| Epic 4: Persona | 2/2 | 100% | ✅ Complete (eval missing) |
| Epic 5: Web Search | 0/1 | 0% | 🔴 **BUGGY - INVESTIGATE & FIX** |
| Epic 6: Image Gen | 0/1 | 0% | 📅 **MVP2 (DEFERRED)** |
| Epic 7: Evaluation | 0/11 | 0% | 🔴 **ENTIRE FRAMEWORK MISSING** |
| Epic 8: Monitoring | 0/4 | 0% | 🔴 **NO PRODUCTION VISIBILITY** |
| Epic 9: Conversations | 7/16 | 44% | ⚠️ Advanced features missing |
| Epic 10: Models | 5/5 | 100% | ✅ Complete |
| Epic 11: Settings | 6/7 | 86% | ⚠️ Storage size calc missing |
| Epic 12: Messages | 4/5 | 80% | ⚠️ Thumbs up/down missing |
| Epic 13: Platform | 0/2 | 0% | ⚠️ Landscape/tablet missing |
| Epic 14: Testing | 1.5/4 | 38% | ⚠️ Coverage insufficient |

**TOTAL COMPLETION**: ~35% (25/71 features)

---

## MVP1 SCOPE (USER-CONFIRMED)

### INCLUDED in MVP1
✅ All advanced features (folders, tags, archive, edit messages, branching)
✅ Evaluation framework (golden QA, LLM-judge, drift detection)
✅ Analytics & monitoring (Crashlytics, events, performance)
✅ Continue generating, follow-up suggestions
✅ Thumbs up/down feedback
✅ Landscape mode, tablet optimization

### EXCLUDED from MVP1 (Deferred to MVP2)
❌ Google Play Billing (not needed per user)
❌ Image generation (broken, low priority)

---

## MVP1 ROADMAP - PHASED DELIVERY

### **Phase 1: EMERGENCY FIXES** (Week 1-2, 2 weeks)
**Goal**: Fix broken critical features

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Fix web search result fetching | 2-3 days | Yes (investigation + fix) |
| P0 | Add Firebase Crashlytics | 1 day | No (standard integration) |
| P0 | Delete broken context compression | 1 day | Yes (deletion plan) |
| P0 | Create golden QA datasets (all 6) | 3-5 days | Yes (dataset specs) |

**Deliverable**: Web search working, Crashlytics live, compression deleted, baseline datasets exist

---

### **Phase 2: MEASUREMENT INFRASTRUCTURE** (Week 3-4, 2 weeks)
**Goal**: Implement evaluation framework

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Implement LLM-judge evaluation system | 3-4 days | Yes (eval pipeline spec) |
| P0 | Add thumbs up/down UI | 2 days | Yes (feedback UI spec) |
| P0 | Implement drift detection dashboard | 2-3 days | Yes (monitoring spec) |
| P1 | Add Firebase Analytics events | 2-3 days | Yes (event taxonomy spec) |
| P1 | Add Firebase Performance monitoring | 2 days | No (standard integration) |

**Deliverable**: Quality measurement operational, user feedback loop live, drift monitoring active

---

### **Phase 3: REBUILD COMPRESSION** (Week 5-6, 2 weeks)
**Goal**: Build new context compression system

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Design compression v2 architecture | 1 day | Yes (architecture spec) |
| P0 | Implement token monitoring v2 | 2 days | Yes (implementation spec) |
| P0 | Implement compression algorithm v2 | 2-3 days | Yes (algorithm spec) |
| P0 | Implement quality monitoring v2 | 1-2 days | Yes (quality checks spec) |
| P0 | Validate with GQA-006 (context dataset) | 1 day | No (testing only) |

**Deliverable**: Context compression working correctly, validated with golden QA

---

### **Phase 4: CORE AI FEATURES** (Week 7-8, 2 weeks)
**Goal**: Complete core chat experience

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Continue generating button | 3 days | Yes (continuation spec) |
| P1 | Follow-up suggestions | 3 days | Yes (suggestion spec) |
| P1 | Response length quick actions | 1 day | Yes (UI modification spec) |

**Deliverable**: Core chat experience matches ChatGPT/Claude feature parity

---

### **Phase 5: DATABASE MIGRATIONS** (Week 9-10, 2 weeks)
**Goal**: Enable advanced conversation features

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | DB Migration v4→v5 (edit messages schema) | 2-3 days | Yes (migration spec) |
| P0 | DB Migration v5→v6 (folders + tags schema) | 2 days | Yes (migration spec) |
| P0 | DB Migration v6→v7 (archive + soft delete schema) | 1-2 days | Yes (migration spec) |
| P1 | Migration testing suite | 1 day | No (standard testing) |

**Deliverable**: Database ready for advanced features, migrations tested

---

### **Phase 6: CONVERSATION ORGANIZATION** (Week 11-12, 2 weeks)
**Goal**: Folders, tags, archive

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Folder management UI (create, rename, delete) | 3 days | Yes (folder UI spec) |
| P0 | Move conversations to folders | 1 day | Yes (move action spec) |
| P0 | Archive conversations UI | 2-3 days | Yes (archive spec) |
| P1 | Tag management UI | 2-3 days | Yes (tag spec) |
| P1 | Multi-select mode | 3 days | Yes (selection mode spec) |

**Deliverable**: Users can organize conversations with folders/tags/archive

---

### **Phase 7: MESSAGE EDITING** (Week 13-14, 2 weeks)
**Goal**: Edit messages and branching

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P0 | Edit user message UI | 2-3 days | Yes (edit UI spec) |
| P0 | Edit creates new branch | 1 day | Yes (branching logic spec) |
| P0 | Branch navigation UI | 3 days | Yes (branch UI spec) |
| P1 | Branch switcher (chevron/dropdown) | 2 days | Yes (switcher spec) |

**Deliverable**: Users can edit messages and navigate conversation branches

---

### **Phase 8: MOBILE UX POLISH** (Week 15-16, 2 weeks)
**Goal**: Gestures, landscape, tablet

| Priority | Feature | Effort | OpenSpec Required |
|----------|---------|--------|-------------------|
| P1 | Swipe gestures (archive, delete, star) | 2-3 days | Yes (gesture spec) |
| P1 | Undo delete (soft delete + snackbar) | 2-3 days | Yes (undo spec) |
| P1 | Landscape mode support | 3 days | Yes (responsive layout spec) |
| P2 | Tablet two-pane layout | 4 days | Yes (tablet optimization spec) |

**Deliverable**: Mobile UX polished, tablet support added

---

### **Phase 9: FINAL TESTING & LAUNCH PREP** (Week 17-18, 2 weeks)
**Goal**: Quality assurance and launch readiness

| Priority | Task | Effort | OpenSpec Required |
|----------|------|--------|-------------------|
| P0 | Unit test coverage to 80% | 5 days | No (standard testing) |
| P0 | Integration test suite | 3 days | No (standard testing) |
| P0 | Performance benchmark suite | 2 days | Yes (benchmark spec) |
| P0 | End-to-end golden QA validation | 2 days | No (validation only) |
| P0 | Staged rollout plan | 1 day | No (operational doc) |
| P0 | Rollback playbook documentation | 1 day | Yes (playbook spec) |

**Deliverable**: MVP1 ready for production launch

---

## OPENSPEC WORKFLOW

Each feature will follow this process:

### Step 1: OpenSpec Proposal
Create `openspec/changes/<feature-slug>/`:
- `proposal.md` - What, why, acceptance criteria
- `tasks.md` - Implementation checklist
- `spec-delta.md` - Spec changes (ADDED/MODIFIED/REMOVED)

### Step 2: User Approval
⛔ **STOP** - Wait for user approval before any code

### Step 3: OpenSpec Apply
Implement according to approved spec with:
- TDD RED: Write failing test
- TDD GREEN: Implement to pass
- TEST LOOP: Until all tests pass
- CI LOOP: Until CI green
- VISUAL LOOP: Until UI verified

### Step 4: OpenSpec Archive
When complete:
- Merge specs to `openspec/specs/`
- Move to `openspec/archive/<feature-slug>/`
- Update `LESSONS_LEARNED.md`

---

## SHIP/DON'T SHIP CRITERIA

### ✅ Ship When

| Criterion | Threshold | Evidence Required |
|-----------|-----------|-------------------|
| Golden QA pass rate | ≥80% | Eval report with breakdown |
| Hallucination rate | <2% | LLM-judge eval (500+ samples) |
| Safety eval pass | 100% | GQA-005 zero failures |
| TTFT p95 | <1500ms | Performance benchmark report |
| Decode p95 | >15 tok/s | Performance benchmark report |
| Crash-free rate | ≥99.5% | Crashlytics dashboard |
| Unit test coverage | ≥80% | Coverage report |
| Maestro tests | 100% pass | CI/CD pipeline |
| Drift detection | Dashboard live | Monitoring screenshot |

### ❌ Don't Ship When

| Criterion | Threshold | Risk |
|-----------|-----------|------|
| Hallucination rate | >5% | Reputation damage |
| Safety eval fail | Any failure | Legal liability |
| TTFT p95 | >3000ms | User abandonment |
| Crash rate | >1% | App store ranking |
| No eval framework | N/A | Flying blind |

---

## APPENDIX A: MODEL PORTFOLIO

| Model | Params | Size | RAM | TTFT* | Decode* | Accuracy* |
|-------|--------|------|-----|-------|---------|-----------|
| Gemma-3n-E2B-it | ~2B | 3.65 GB | 8 GB | 650ms | 28 tok/s | 82% |
| Gemma-3n-E4B-it | ~4B | 4.92 GB | 12 GB | 850ms | 22 tok/s | 87% |
| Gemma3-1B-IT | ~1B | 584 MB | 6 GB | 350ms | 45 tok/s | 75% |
| Qwen2.5-1.5B | ~1.5B | 1.6 GB | 6 GB | 450ms | 35 tok/s | 79% |
| Phi-4-mini | ~3.8B | 3.91 GB | 6 GB | 750ms | 24 tok/s | 84% |
| DeepSeek-R1-Distill | ~1.5B | 1.83 GB | 6 GB | 500ms | 32 tok/s | 81% |

*Baseline: Pixel 8 Pro (GPU enabled)

---

## APPENDIX B: DATABASE SCHEMA ROADMAP

### Current: Version 4
- `conversation_threads`: id, title, modelId, taskId, createdAt, updatedAt, isStarred, personaVariant, estimatedTokens, lastTokenUpdate
- `conversation_messages`: id, threadId, content, isUser, timestamp, imageUris, audioUri, audioSampleRate, messageType

### Version 5 (Edit Messages)
ADD to `conversation_messages`:
- `parent_message_id` (INTEGER, nullable)
- `branch_id` (INTEGER, default 0)
- `edited_at` (INTEGER, nullable)
- `original_content` (TEXT, nullable)

### Version 6 (Folders & Tags)
ADD tables:
- `conversation_folders`: id, name, createdAt
- `conversation_tags`: id, name, createdAt
- `thread_folder_mapping`: threadId, folderId
- `thread_tag_mapping`: threadId, tagId

### Version 7 (Archive & Soft Delete)
ADD to `conversation_threads`:
- `is_archived` (INTEGER, default 0)
- `deleted_at` (INTEGER, nullable)

---

## APPENDIX C: TECHNOLOGY STACK

| Layer | Technology | Version |
|-------|------------|---------|
| UI | Jetpack Compose | BOM 2025.05.00 |
| Design | Material Design 3 | Latest |
| DI | Hilt | 2.57 |
| Database | Room | 2.6.1 |
| Preferences | DataStore (Proto) | 1.1.7 |
| ML Runtime | LiteRT LM | 0.9.0-alpha01 |
| ML Tasks | MediaPipe | 0.10.27 |
| GPU | TensorFlow Lite GPU | 16.4.0 |
| Background | WorkManager | 2.10.0 |
| Analytics | Firebase BOM | 33.16.0 |

---

## DOCUMENT HISTORY

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-26 | Initial aspirational PRD |
| 2.0 | 2026-01-11 | Forensic verification (50+ files) |
| **3.0** | **2026-01-11** | **MASTER: User corrections, OpenSpec integration, 18-week roadmap** |

---

**Next Actions:**
1. ✅ Create OpenSpec proposals for Phase 1 (emergency fixes)
2. Begin implementation after user approval
3. Follow OpenSpec workflow for each feature
4. Validate with golden QA before shipping

---

*This is the SINGLE SOURCE OF TRUTH. All previous PRDs (v1.0, v2.0, MASTER PRD v2.0) are superseded.*

**Last Updated**: 2026-01-11
**Status**: READY FOR OPENSPEC PROPOSALS
**Methodology**: AI Mobile PM + OpenSpec-Driven Development
