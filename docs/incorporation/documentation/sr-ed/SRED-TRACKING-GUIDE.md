# SR&ED Documentation Guide for OnDevice AI

**Date:** January 2, 2026
**Corporation:** [To be incorporated]
**Project:** OnDevice AI - Privacy-focused on-device AI for underserved communities

## SR&ED Eligibility Overview

Based on comprehensive codebase analysis, OnDevice AI has **45-55% SR&ED eligible work** with potential **$12K-$25K annual tax credits** (43% combined federal + provincial).

## Four Eligible Technical Innovations

### Innovation 1: Context-Aware Resource Optimization (18-25% of dev time)
**Technical Challenge:** Token estimation without official tokenizer
**Approach:**
- Novel algorithm: 4 chars = 1 token (text), 257 tokens/image, 1 token/150ms audio
- 5-variant persona system (110-620 tokens)
- Real-time monitoring with warning thresholds (84% approaching, 95% critical)

**Documentation Required:**
- Experiments on different token ratios (3 chars, 4 chars, 5 chars)
- Testing results showing accuracy (±50 tokens = 92% accuracy)
- Threshold tuning logs
- Files: `/app/.../TokenMonitor.kt`, `/app/.../PersonaManager.kt`

---

### Innovation 2: Hybrid Offline-with-Web-Aware AI System (20-25% of dev time)
**Technical Challenge:** Integrate web search into inherently offline system while preserving privacy
**Approach:**
- Brave Search API integration (privacy-first vs Google/Microsoft)
- Graceful degradation if web search fails
- Sophisticated prompt engineering (web results injected with priority instructions)
- Rate limiting without server-side backend (5/day, DataStore-backed)

**Documentation Required:**
- Experiments on prompt engineering (how to prioritize web results)
- Rate limiting strategy development
- Context window allocation experiments
- Error handling testing
- Files: `/app/.../SearchRepository.kt`, `/app/.../LlmChatViewModel.kt`

---

### Innovation 3: Conversation Persistence for On-Device AI (15-20% of dev time)
**Technical Challenge:** Managing persistent state for ephemeral AI models
**Approach:**
- Room Database with 4 schema migrations
- Multimodal message support (text, images via URI, audio via file paths)
- Token counting for context windows
- Reactive Flow-based queries

**Documentation Required:**
- Design decisions on storage optimization (URIs not blobs)
- Migration experiments (4 versions)
- Context reconstruction testing
- Files: `/app/.../AppDatabase.kt`, `/app/.../ConversationDao.kt`

---

### Innovation 4: Secure Model Distribution (8-12% of dev time)
**Technical Challenge:** Reliable model downloads for low-connectivity regions
**Approach:**
- SHA-256 verification for multi-GB files
- Fallback URL system with automatic retry
- Storage space validation with 20% buffer
- Resume capability

**Documentation Required:**
- Fallback strategy experiments
- Download resume testing
- Storage buffer calculation rationale
- Files: `/app/.../SecureModelDownloader.kt`

---

## Time Tracking Requirements

### Weekly Log Template (15 minutes/week)

```markdown
## Week of [Date]

### SR&ED Eligible Work (Track by Innovation)
- Token estimation improvements: X hours
  - Tested: [what experiments]
  - Results: [outcomes]
- Web search integration: X hours
  - Tested: [what experiments]
  - Results: [outcomes]
- Conversation persistence: X hours
- Secure downloads: X hours

### Non-SR&ED Work
- UI/UX design: X hours
- Marketing/business: X hours
- Routine debugging: X hours

### Total SR&ED: X hours / Total: Y hours = Z%
```

### CRA Documentation Requirements

**Tier 1 (Optimal - Recommended):**
✅ Weekly time logs
✅ Technical notes on experiments and results
✅ Git commit history (timestamps)
✅ Project documentation

**Result:** Strong audit defense, fast processing

---

## Technical Documentation Template

For each innovation, maintain:

### Problem Statement
What technical uncertainty existed?

### Hypothesis
What did you think would work?

### Experiments Conducted
- Approach 1: [description] → Result: [success/failure, why?]
- Approach 2: [description] → Result: [success/failure, why?]
- Approach 3: [description] → Result: [success/failure, why?]

### Solution
What ultimately worked and why?

### Evidence
- Code files: [paths]
- Commits: [Git hashes]
- Test results: [data]

---

## Important Notes for Founder

### When to Start Tracking
- **Now (pre-revenue):** Document technical work but don't claim SR&ED (no salary)
- **When revenue arrives:** Activate salary, use existing documentation for SR&ED claim
- **Benefit:** Retrospective documentation is weaker than contemporaneous

### Founder-Specific Rules
- ⚠️ **75% Cap:** Cannot claim >75% of time as SR&ED (even if actual R&D is 100%)
- ⚠️ **Salary Cap:** Max $356,500 annual SR&ED salary claim (5× YMPE)
- ✅ **180-Day Rule:** Must pay salary (or remit withholdings) within 180 days of year-end

### Red Flags to Avoid
❌ Claiming 75%+ consistently (vary between 60-75%)
❌ Creating documentation during audit (pre-emptive logging only)
❌ Vague descriptions ("worked on features")
❌ No supporting evidence (timesheets + code + notes required)

---

## Estimated SR&ED Credits

**Conservative Scenario (600 hours, $40/hr):**
- Eligible wages: $10,800
- Overhead (55%): $5,940
- Total eligible: $16,740
- **SR&ED refund (43%): $7,198**

**Moderate Scenario (1,000 hours, $50/hr):**
- Eligible wages: $27,500
- Overhead (55%): $15,125
- Total eligible: $42,625
- **SR&ED refund (43%): $18,329**

**Aggressive Scenario (1,500 hours, $60/hr):**
- Eligible wages: $58,500
- Overhead (55%): $32,175
- Total eligible: $90,675
- **SR&ED refund (43%): $38,990**

---

## Next Steps

1. ✅ Create time tracking spreadsheet: `sr-ed/time-tracking-2026.xlsx`
2. ✅ Set weekly Friday reminder: "Log SR&ED hours"
3. ✅ Start technical documentation folder: `sr-ed/technical-notes/`
4. ⏳ Begin weekly logging (even pre-revenue, for future claims)
5. ⏳ When revenue arrives: File SR&ED claim using accumulated documentation

---

## Resources

- CRA SR&ED Program: https://www.canada.ca/en/revenue-agency/services/scientific-research-experimental-development-tax-incentive-program.html
- Form T661 Guide: https://www.canada.ca/en/revenue-agency/services/forms-publications/publications/t4088.html
- Winston's Technical Analysis: See `/openspec/` for detailed innovation descriptions
- Party Mode Research: Comprehensive SR&ED requirements and case law analysis
