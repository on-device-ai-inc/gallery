# SR&ED TIME TRACKING SPREADSHEET TEMPLATE
## Weekly Development Hours Log

**Instructions:** Copy this template to Excel/Google Sheets and log hours every Friday.

---

## SPREADSHEET STRUCTURE

### Columns:

| Week Ending | Innovation/Project | Hours | SR&ED Eligible? | Description | Notes |
|-------------|-------------------|-------|-----------------|-------------|-------|
| YYYY-MM-DD | [Project name] | XX | Y/N | [Brief description] | [Technical notes] |

---

## SAMPLE ENTRIES

| Week Ending | Innovation/Project | Hours | SR&ED Eligible? | Description | Notes |
|-------------|-------------------|-------|-----------------|-------------|-------|
| 2026-01-10 | Token Monitoring System | 18 | Y | Experimented with 3/4/5 chars per token ratios. Tested accuracy against actual Gemini consumption. | Best result: 4 chars = 92% accuracy |
| 2026-01-10 | Web Search Integration | 12 | Y | Implemented Brave API rate limiting. Tested prompt engineering for result prioritization. | 5/day limit via DataStore working |
| 2026-01-10 | UI Design | 8 | N | Refined chat interface styling and color scheme | Non-technical, aesthetic work |
| 2026-01-10 | Business Planning | 2 | N | Planned go-to-market strategy, reviewed competitor apps | Marketing, not R&D |
| **TOTALS** | **Week Total** | **40** | **30/40 = 75%** | | |

---

## MONTHLY SUMMARY TABLE

| Month | Total Hours | SR&ED Hours | SR&ED % | Average Hourly Rate | Eligible Wages | Notes |
|-------|-------------|-------------|---------|---------------------|----------------|-------|
| Jan 2026 | 160 | 120 | 75% | $50 | $6,000 | High R&D focus - token system |
| Feb 2026 | 140 | 91 | 65% | $50 | $4,550 | Launch prep reduced R&D time |
| Mar 2026 | 180 | 126 | 70% | $50 | $6,300 | New features - conversation history |

**Year-to-date totals:** 480 hours total, 337 SR&ED (70% average)

---

## INNOVATION BREAKDOWN (For SR&ED Claim)

### Innovation 1: Context-Aware Resource Optimization
**Total hours Year 1:** 180 hours
**Key experiments:**
- Token ratio testing (3/4/5 chars per token)
- Persona variant optimization (5 variants, 110-620 tokens)
- Threshold tuning (84% warning, 95% critical)
- Thread-safety testing

**Files:** TokenMonitor.kt, PersonaManager.kt, PersonaVariant.kt

---

### Innovation 2: Hybrid Offline-with-Web AI
**Total hours Year 1:** 200 hours
**Key experiments:**
- Brave Search API integration and error handling
- Rate limiting strategy (5/day, daily reset logic)
- Prompt engineering for web result prioritization
- Graceful degradation testing
- Context window allocation experiments

**Files:** SearchRepository.kt, BraveSearchService.kt, LlmChatViewModel.kt

---

### Innovation 3: Conversation Persistence
**Total hours Year 1:** 120 hours
**Key experiments:**
- Database schema design (4 migrations)
- Multimodal storage optimization (URI vs blob)
- Token counting for context reconstruction
- Reactive Flow query performance

**Files:** AppDatabase.kt, ConversationDao.kt, ConversationThread.kt

---

### Innovation 4: Secure Model Distribution
**Total hours Year 1:** 80 hours
**Key experiments:**
- SHA-256 verification for multi-GB files
- Fallback URL retry logic
- Resume capability testing
- Storage buffer calculation (20% overhead)

**Files:** SecureModelDownloader.kt

---

## NON-SR&ED WORK CATEGORIES

**UI/UX Design:** Visual styling, layout, color schemes, icon design
**Marketing:** Business planning, market research, competitor analysis
**Routine Debugging:** Fixing known bugs, standard troubleshooting
**Documentation:** User guides, marketing materials (not technical documentation)
**Administrative:** Email, meetings, non-technical planning

---

## WEEKLY LOGGING WORKFLOW

### Every Friday (15 minutes):

1. **Open time tracking spreadsheet**

2. **Review Git commits from the week:**
   ```bash
   git log --since="1 week ago" --oneline
   ```

3. **Categorize work:**
   - Which commits were SR&ED eligible? (solving technical uncertainty)
   - Which were routine? (styling, known solutions)

4. **Estimate hours per innovation:**
   - Token monitoring: X hours
   - Web search: Y hours
   - UI work: Z hours
   - Etc.

5. **Add row to spreadsheet with:**
   - Week ending date
   - Innovation/project
   - Hours
   - SR&ED Y/N
   - Brief description
   - Technical notes

6. **Calculate weekly SR&ED %:**
   - Total SR&ED hours / Total hours
   - Should average 60-75% (don't exceed 75% as founder)

7. **Save and backup spreadsheet**

---

## MONTHLY REVIEW (Last Friday of Month)

1. **Verify monthly totals**
2. **Calculate monthly SR&ED percentage**
3. **Compare against 75% cap**
4. **Review technical notes for completeness**
5. **Flag any weeks with insufficient documentation**
6. **Update Innovation Breakdown totals**

---

## YEAR-END SR&ED CLAIM PREPARATION

**When you have revenue and pay yourself salary:**

1. **Calculate total SR&ED eligible wages:**
   - Total SR&ED hours: [X]
   - Average hourly rate: [$Y]
   - Total eligible wages: [X × $Y]

2. **Prepare Form T661 documentation:**
   - Project descriptions (use Innovation Breakdown)
   - Technical challenges (from Notes column)
   - Systematic investigation (experiments logged)
   - Results and outcomes

3. **Supporting evidence:**
   - This time tracking spreadsheet
   - Git commit history
   - Technical documentation files
   - Project notes and experiment logs

4. **CPA filing:**
   - Provide all above documentation to CPA
   - CPA prepares T661 and includes in T2 return
   - SR&ED refund (43%) arrives 6-12 months later

---

## RED FLAGS TO AVOID

❌ **DON'T:**
- Claim 100% of hours as SR&ED (unrealistic)
- Claim >75% consistently (founder cap)
- Create documentation during audit (must be contemporaneous)
- Use vague descriptions ("worked on features")
- Batch log at year-end (weak evidence)

✅ **DO:**
- Log weekly (15 minutes every Friday)
- Vary SR&ED percentage (60-75% range)
- Write specific descriptions
- Reference code files and commits
- Maintain supporting technical notes

---

## SAMPLE YEAR-END CALCULATION

**Total development hours Year 1:** 1,000 hours
**SR&ED eligible hours:** 700 hours (70%)
**Hourly rate:** $50/hour
**Total wages paid:** $50,000 (1,000 × $50)
**SR&ED eligible wages:** $35,000 (700 × $50)

**Overhead allocation (55%):** $19,250
**Materials (cloud, APIs):** $2,000
**Total SR&ED expenditures:** $56,250

**Federal ITC (35%):** $19,688
**Provincial OITC (8%):** $4,500
**Total SR&ED refund:** $24,188

**This spreadsheet supports the $35,000 eligible wage claim.**

---

## GETTING STARTED

**Today:** Create this spreadsheet in Excel or Google Sheets

**This week:** Log hours for Week 1 (even though no salary yet - builds documentation)

**Every Friday:** 15 minutes to log the week

**Year-end:** Complete documentation ready for SR&ED claim when revenue arrives

---

**Template created:** January 2, 2026
**Status:** Ready to use - start logging immediately
**Location:** Save as `time-tracking-2026.xlsx` in `documentation/sr-ed/` folder
