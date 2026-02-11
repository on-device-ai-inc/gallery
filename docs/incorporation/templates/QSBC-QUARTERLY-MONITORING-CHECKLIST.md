# QSBC Quarterly Monitoring Checklist
## Practical Tool for Maintaining Qualified Small Business Corporation Status

**Version:** 1.0
**Last Updated:** January 3, 2026
**Purpose:** Provide actionable quarterly monitoring system to ensure continuous QSBC qualification for LCGE eligibility

---

## Why Quarterly Monitoring is Non-Negotiable

**The LCGE Trap:** If your corporation FAILS the QSBC tests at disposition or anytime during the 24-month lookback, you lose the ENTIRE LCGE benefit.

**Financial Impact:**
- ✅ QSBC qualified: $5,018,556 sheltered (4 beneficiaries), $2.5M tax saved
- ❌ QSBC disqualified: $0 sheltered, $5M taxable, $2.5M tax owing

**The Problem:** Founders don't realize they've lost QSBC status until it's too late (during exit due diligence).

**The Solution:** Quarterly monitoring using this checklist catches problems BEFORE they become catastrophic.

---

## The Three QSBC Tests

### Test #1: CCPC Status (Continuous Requirement)

**Requirement:** Corporation must be a Canadian-Controlled Private Corporation at all relevant times.

**Fails If:**
- Non-residents own >50% of voting shares
- Public company owns >50% of voting shares
- Corporation becomes publicly traded

**Monitoring Frequency:** Every time shares are issued or transferred

---

### Test #2: 90% Active Business Assets Test (At Disposition)

**Requirement:** At the time of disposition (exit/sale), 90%+ of corporation's assets (by fair market value) must be:
- Used principally in an active business carried on primarily in Canada, OR
- Shares/debt of connected QSBC corporations

**Fails If:**
- >10% of assets are passive investments (stocks, bonds, rental real estate)
- >10% of assets are cash not deployed in active business

**Monitoring Frequency:** Quarterly + immediately before exit

---

### Test #3: 50% Active Business Assets Test (24-Month Lookback)

**Requirement:** Throughout the 24 months immediately preceding disposition, MORE THAN 50% of corporation's assets (by FMV) must be:
- Used principally in an active business carried on primarily in Canada, OR
- Shares/debt of connected QSBC corporations

**Fails If:**
- At ANY point in past 24 months, ≤50% of assets were active business assets
- Excess cash accumulated from funding without deployment plan

**Monitoring Frequency:** Quarterly (rolling 24-month calculation)

**CRITICAL:** The 50% test is CONTINUOUS over 24 months. If you fail in Q2 2024 (even for one day), your shares are disqualified even if you're compliant in Q3 2024.

---

## Quarterly Monitoring Schedule

**Set Calendar Reminders:**
- **Q1:** January 31 (for December 31 prior year-end)
- **Q2:** April 30 (for March 31 quarter-end)
- **Q3:** July 31 (for June 30 quarter-end)
- **Q4:** October 31 (for September 30 quarter-end)

**Each Quarter:**
1. Calculate fair market value of all assets
2. Categorize assets (active vs. passive)
3. Calculate 90% test (current)
4. Calculate 50% test (rolling 24-month)
5. Identify red flags
6. Take corrective action if needed

---

## Asset Classification Guide

### Active Business Assets ✅

**Count Toward QSBC Qualification:**

1. **Operating Assets:**
   - Equipment, computers, servers, hardware
   - Software licenses (if used in business)
   - Office furniture, fixtures
   - Leasehold improvements
   - Inventory (if applicable)
   - Vehicles used in business

2. **Intellectual Property Developed Internally:**
   - Proprietary software/code
   - Patents, trademarks (if used in active business)
   - Trade secrets, know-how

3. **Business Accounts Receivable:**
   - Amounts owed by customers for services rendered
   - Unbilled revenue (WIP)

4. **Reasonable Working Capital:**
   - Cash needed for 3-6 months operating expenses ✅
   - Payroll reserves
   - Tax installment reserves

5. **Prepaid Expenses:**
   - Prepaid rent, insurance (if for business operations)

6. **Connected Corporation Shares:**
   - Shares of subsidiary QSBCs (if >10% voting + >10% FMV ownership)

---

### Passive/Non-Qualifying Assets ❌

**Count AGAINST QSBC Qualification:**

1. **Investment Assets:**
   - Public company stocks, ETFs, mutual funds
   - Bonds, GICs, term deposits
   - Cryptocurrency held as investment

2. **Excess Cash:**
   - Cash beyond 6 months operating expenses
   - Accumulated profits not deployed in business
   - Funding proceeds sitting idle

3. **Real Estate:**
   - Rental properties
   - Land held for speculation
   - Office building (if >50% rented to third parties)

4. **Shareholder Loans Receivable:**
   - Loans to founders/shareholders (especially if low/no interest)

5. **Non-Business Investments:**
   - Art, collectibles
   - Luxury vehicles not used in business

6. **IP Licensing to Non-Connected Parties:**
   - If corporation licenses IP and receives royalties passively (not active business)

---

## Quarterly Monitoring Spreadsheet Template

**Create a Google Sheet or Excel with this structure:**

### Sheet 1: Asset Inventory

| Date | Asset Description | Category | Cost Basis | Fair Market Value | Active (✅) or Passive (❌) | Notes |
|------|------------------|----------|-----------|-------------------|---------------------------|-------|
| 2024-12-31 | Cash - operating account | Working capital | $50,000 | $50,000 | ✅ | 3 months expenses |
| 2024-12-31 | Cash - savings account | Excess cash | $500,000 | $500,000 | ❌ | From Series A, not deployed |
| 2024-12-31 | Computers & equipment | Operating assets | $30,000 | $20,000 | ✅ | Dev team equipment |
| 2024-12-31 | Software licenses | Operating assets | $15,000 | $15,000 | ✅ | AWS, GitHub, etc. |
| 2024-12-31 | AR - customer invoices | Receivables | $80,000 | $80,000 | ✅ | Billed revenue |
| 2024-12-31 | Proprietary software | IP | $0 | $2,000,000 | ✅ | Internally developed |
| 2024-12-31 | Public stock investments | Investments | $100,000 | $120,000 | ❌ | Invested excess cash |

**Formulas:**
- **Total Assets (FMV):** =SUM(FMV column)
- **Active Assets (FMV):** =SUMIF(Active/Passive column, "✅", FMV column)
- **Passive Assets (FMV):** =SUMIF(Active/Passive column, "❌", FMV column)

---

### Sheet 2: Quarterly QSBC Test Calculation

| Quarter End Date | Total Assets (FMV) | Active Assets (FMV) | Passive Assets (FMV) | Active % | 90% Test | 50% Test | Status |
|-----------------|-------------------|--------------------|--------------------|---------|----------|----------|--------|
| 2024-03-31 | $2,200,000 | $2,100,000 | $100,000 | 95.5% | ✅ PASS | ✅ PASS | COMPLIANT |
| 2024-06-30 | $2,650,000 | $2,150,000 | $500,000 | 81.1% | ⚠️ MARGINAL | ✅ PASS | WATCH |
| 2024-09-30 | $3,100,000 | $2,200,000 | $900,000 | 71.0% | ⚠️ MARGINAL | ✅ PASS | WATCH |
| 2024-12-31 | $3,500,000 | $2,300,000 | $1,200,000 | 65.7% | ❌ FAIL | ✅ PASS | ACTION REQUIRED |

**Formulas:**
- **Active %:** =(Active Assets / Total Assets) × 100
- **90% Test:** =IF(Active % ≥ 90%, "✅ PASS", IF(Active % ≥ 80%, "⚠️ MARGINAL", "❌ FAIL"))
- **50% Test:** =IF(Active % > 50%, "✅ PASS", "❌ FAIL")
- **Status:** =IF(AND(90% Test = PASS, 50% Test = PASS), "COMPLIANT", IF(50% Test = FAIL, "CRITICAL", "WATCH"))

---

### Sheet 3: 24-Month Rolling Compliance

| Month | Active % | 50% Test | Status | Notes |
|-------|---------|----------|--------|-------|
| Jan 2023 | 92% | ✅ PASS | | Pre-funding |
| Feb 2023 | 91% | ✅ PASS | | |
| Mar 2023 | 55% | ✅ PASS | ⚠️ | Series A closed - $2M cash |
| Apr 2023 | 60% | ✅ PASS | | Started deploying cash |
| ... | | | | |
| Dec 2024 | 66% | ✅ PASS | | 24-month lookback: ALL PASS ✅ |

**Purpose:** Track every month in rolling 24-month window. If ANY month shows ≤50%, QSBC is disqualified.

**Formula:**
- Highlight in RED any month where Active % ≤ 50%
- Use conditional formatting: `=IF(Active % ≤ 50%, RED, GREEN)`

---

## Sample Quarterly Monitoring Workflow

### Step 1: Gather Data (1-2 hours)

**Bank Statements:**
- Download all corporate bank account statements for quarter
- Identify cash balances on last day of quarter

**Balance Sheet:**
- Obtain balance sheet from accounting software (QuickBooks, Xero, etc.)
- List all assets with cost basis

**Valuation:**
- Estimate FMV of key assets:
  - Equipment: Depreciated value (or cost - 20% per year)
  - IP: Most recent valuation or implied valuation from funding round
  - Receivables: Face value (if collectible)
  - Cash: Face value

---

### Step 2: Categorize Assets (30 minutes)

**Active Business Assets:**
- Operating cash (3-6 months expenses)
- Equipment, software, tools
- Receivables
- Internally developed IP

**Passive Assets:**
- Excess cash (beyond 6 months)
- Investments (stocks, bonds, crypto)
- Shareholder loans

**Gray Areas:**
- **Cash from recent funding:** If deployed within 6-12 months into active business (hiring, R&D, equipment), count as ACTIVE. If sitting idle for >12 months, count as PASSIVE.
- **IP not yet commercialized:** If corporation is DEVELOPING the IP (active), count as ACTIVE. If just holding IP and licensing out, may be PASSIVE.

---

### Step 3: Calculate Tests (15 minutes)

**90% Test (Current Quarter):**
```
Total Assets (FMV): $3,000,000
Active Assets: $2,400,000
Passive Assets: $600,000
Active %: 80% ⚠️ MARGINAL (need 90%+)
```

**50% Test (24-Month Rolling):**
```
Review past 24 months (8 quarters)
Identify MINIMUM Active % in that period: 65%
Result: ✅ PASS (all quarters >50%)
```

---

### Step 4: Identify Red Flags (10 minutes)

**Red Flag #1: Declining Active %**
```
Q1 2024: 95%
Q2 2024: 88%
Q3 2024: 75%
Q4 2024: 68% ⚠️ TREND DOWN
```

**Action:** Investigate why passive assets increasing (excess cash? investments?). Plan deployment.

---

**Red Flag #2: Approaching 50% Threshold**
```
Current Active %: 52%
If passive assets increase by $100K: Would drop to 48% ❌ FAIL
```

**Action:** URGENT - Deploy passive assets or reduce passive holdings immediately.

---

**Red Flag #3: Post-Funding Cash Accumulation**
```
Series A: Raised $5M in March 2024
Current: December 2024 (9 months later)
Cash remaining: $4.2M (only deployed $800K in 9 months)
Active %: 60% (declining toward 50%)
```

**Action:** Accelerate hiring, R&D spending, equipment purchases. If can't deploy, consider returning capital to shareholders OR accept lower Active % and plan for next test.

---

### Step 5: Take Corrective Action

**If Active % <90% (Failing "At Disposition" Test):**

**Options:**
1. **Deploy Excess Cash into Active Business:**
   - Hire employees (payroll = active expense)
   - Purchase equipment, servers, software licenses
   - R&D expenses (claim SR&ED credits)
   - Marketing, customer acquisition costs
   - Leasehold improvements, office expansion

2. **Distribute Excess Cash to Shareholders:**
   - Declare dividend (reduces corporate assets)
   - Shareholder can pay personal tax on dividend
   - Reduces passive asset base

3. **Convert Passive to Active:**
   - Sell investments (stocks, bonds)
   - Use proceeds to fund active business operations

**Example:**
```
Current: $3M total assets, $2.4M active (80%)
Need: 90% active = $2.7M active out of $3M total
Gap: $300K needs to shift from passive to active

ACTION:
- Sell $200K in stock investments
- Use $200K to hire 2 engineers ($100K/year each = active expense)
- Reduces passive by $200K
- Increases active by $200K (payroll expenses)
- New calculation:
  - Total assets: $3M
  - Active: $2.6M (87% - still short, but closer)
  - Next quarter: Additional $100K deployment needed
```

---

**If Active % ≤50% (Failing "24-Month Lookback" Test):**

**🔴 CRITICAL - QSBC STATUS LOST**

**Immediate Actions:**
1. **Document the Date QSBC Was Lost:**
   - Identify exact quarter/month when dropped to ≤50%
   - This date destroys LCGE eligibility for shares held through trust

2. **Consult Tax Lawyer Immediately:**
   - May need to restructure (purification transaction)
   - Explore Section 84.1 safe harbor
   - Consider voluntary disclosure if non-compliant

3. **Purification Transaction (If Possible):**
   - Distribute ALL passive assets to shareholders
   - Leaves only active business assets in corporation
   - Resets QSBC compliance (but resets 24-month clock)

**WARNING:** Once QSBC status is lost, you CANNOT retroactively fix it for existing shares (*Collins* principle). You would need to:
- Issue NEW shares (which have new 24-month clock)
- Or execute butterfly reorganization (complex, expensive)

**This is why quarterly monitoring is NON-NEGOTIABLE.**

---

## Red Flag Thresholds & Action Triggers

| Active % | Status | Action Required | Timeline |
|----------|--------|----------------|----------|
| **95-100%** | ✅ EXCELLENT | Maintain current operations | Quarterly monitoring only |
| **90-94%** | ✅ GOOD | Monitor, avoid increasing passive | Quarterly monitoring |
| **80-89%** | ⚠️ MARGINAL | Develop deployment plan for passive assets | Within 1 quarter |
| **70-79%** | ⚠️ WATCH | Immediate deployment plan, monthly monitoring | Within 30 days |
| **60-69%** | 🟡 CAUTION | Urgent deployment, may need to distribute passive assets | Within 2 weeks |
| **51-59%** | 🔴 CRITICAL | Emergency action, risk of falling below 50% | IMMEDIATE |
| **≤50%** | 🔴 QSBC LOST | Engage tax lawyer, restructure immediately | Already too late |

---

## Specific Scenarios & Guidance

### Scenario #1: Just Raised Series A ($5M)

**Situation:**
- Raised $5M Series A in March 2024
- Corporation had $200K in assets pre-funding
- Now has $5.2M total assets ($5M cash + $200K operating assets)
- Active %: 4% ($200K / $5.2M) ❌ FAIL

**Is This a Problem?**
- **Yes, immediately** (fails 90% test)
- **Yes, in 24 months** (if cash not deployed)

**Action Plan:**

**Months 1-6 (March-August 2024):**
- Deploy $1M/month into active business:
  - Hire 10 engineers ($100K/year each = $50K/month in payroll)
  - Purchase $500K in equipment (servers, dev tools)
  - R&D expenses ($300K)
  - Marketing spend ($150K)

**By August 2024:**
- Spent $4M of $5M (only $1M cash remaining)
- Assets: $4.2M active ($4M deployed + $200K original), $1M cash
- Active %: 81% ($4.2M / $5.2M) ⚠️ MARGINAL

**Months 7-12 (Sept 2024-Feb 2025):**
- Deploy remaining $500K
- Keep $500K as working capital (3 months expenses)
- Active %: 90%+ ✅ COMPLIANT

**Key Insight:** Large funding rounds create TEMPORARY QSBC non-compliance. You have 6-12 months to deploy cash into active business before it becomes permanent problem.

---

### Scenario #2: Accumulated Profits ($2M Cash)

**Situation:**
- Corporation profitable ($500K/year profit)
- 4 years of operation = $2M accumulated cash
- Operating assets: $500K
- Total assets: $2.5M
- Active %: 20% ($500K / $2.5M) ❌ FAIL

**Is This a Problem?**
- **Yes** - Fails both 90% and 50% tests
- **QSBC status already lost** (has been <50% for years)

**Action Plan:**

**Option A: Distribute Profits to Shareholders**
- Declare $1.8M dividend
- Shareholders pay personal tax (~50% = $900K tax)
- Corporation retains $200K working capital
- New assets: $700K ($500K operating + $200K cash)
- Active %: 71% ⚠️ MARGINAL (improved but still need 90% at exit)

**Option B: Deploy into Active Business**
- Aggressive expansion (hire 20 people = $2M/year payroll)
- Purchase equipment ($500K)
- R&D projects ($500K)
- Reduces cash by $2M over 12 months
- New assets: $2.5M ($500K original + $2M deployed)
- Active %: 100% ✅ COMPLIANT

**Key Insight:** Profitable corporations must EITHER distribute profits or reinvest in active business. Accumulating cash destroys QSBC status.

---

### Scenario #3: SR&ED Credits ($70K/year Refundable)

**Situation:**
- Tech company claims $200K R&D annually
- Receives $70K refundable SR&ED credits (35% × $200K)
- Cash from credits accumulates ($350K over 5 years)

**Is This a Problem?**
- **Potentially** - If cash sits idle, becomes passive asset

**Action Plan:**

**Deploy SR&ED Credits into Active Business:**
- Use $70K/year to hire additional R&D staff
- Purchase equipment for R&D lab
- Fund experimental projects (active R&D)

**Result:**
- SR&ED credits fuel ACTIVE business growth
- Cash doesn't accumulate (spent on active expenses)
- Maintains QSBC compliance

**Key Insight:** SR&ED credits are OPPORTUNITY, not threat, if deployed into active business immediately.

---

### Scenario #4: Exit in 6 Months (Due Diligence)

**Situation:**
- LOI received for $10M acquisition
- Closing in 6 months
- Current Active %: 75% ⚠️ MARGINAL (fails 90% test)

**Is This a Problem?**
- **Yes** - Purchaser due diligence will discover QSBC non-compliance
- **Founders will lose LCGE benefit** ($5M × 50% inclusion = $2.5M × 50% tax = $1.25M additional tax)

**Action Plan:**

**Immediate Purification (Next 30 Days):**
- Distribute ALL passive assets to shareholders ($900K investments + $200K excess cash)
- Shareholders pay tax on distribution (~50% = $550K tax)
- Corporation retains only active business assets
- Active %: 100% ✅ COMPLIANT

**Month 2-6:**
- Maintain 90%+ active assets
- Quarterly monitoring
- Provide QSBC compliance certificate to purchaser

**At Closing:**
- All shareholders (including trust beneficiaries) claim LCGE
- $5M sheltered, $2.5M tax saved
- Net benefit: $2.5M saved - $550K distribution tax = $1.95M net savings

**Key Insight:** Better to pay $550K tax on purification than lose $2.5M LCGE benefit entirely.

---

## Professional CPA Coordination

**When to Engage CPA:**

**Baseline Setup (Year 1):**
- CPA reviews initial QSBC qualification
- Sets up quarterly monitoring spreadsheet
- Cost: $1,000-$1,500

**Quarterly Reviews (Years 1-5):**
- **DIY Option:** You complete spreadsheet, CPA reviews annually ($500-$1,000/year)
- **Full Service:** CPA completes quarterly monitoring ($500/quarter = $2,000/year)

**Pre-Exit Validation (12-18 months before exit):**
- CPA conducts comprehensive QSBC compliance audit
- Issues QSBC compliance certificate for purchaser due diligence
- Cost: $3,000-$5,000

**Total 5-Year Cost:**
- DIY monitoring + annual CPA review: $4,000-$6,000
- Full-service CPA monitoring: $11,000-$13,000

**ROI:** Saving $2.5M LCGE benefit for $5K-$13K investment = 192x-500x

---

## Technology & Automation

**Spreadsheet Template:**
- Available in this package (Google Sheets / Excel format)
- Formulas pre-built for 90%/50% tests
- Conditional formatting (red flags automatic)

**Accounting Integration:**
- QuickBooks / Xero balance sheet export → import to spreadsheet
- Monthly/quarterly automatic updates

**Alerts:**
- Set up Google Sheets notifications:
  - If Active % < 90%: Email alert
  - If Active % < 60%: URGENT email alert
  - If Active % ≤ 50%: CRITICAL alert + calendar event

---

## Annual Compliance Summary

**Each December 31 (Year-End):**

**1. Complete Annual Monitoring Report:**
```
QSBC COMPLIANCE ANNUAL REPORT
Year: 2024
Corporation: [Name]

Q1 2024 (March 31): Active 92% - ✅ COMPLIANT
Q2 2024 (June 30): Active 88% - ⚠️ MARGINAL
Q3 2024 (Sept 30): Active 85% - ⚠️ MARGINAL
Q4 2024 (Dec 31): Active 90% - ✅ COMPLIANT

24-Month Rolling: All quarters >50% ✅ PASS
Minimum Active % in 24-month period: 85%

Status: COMPLIANT
Action Items for 2025: Deploy $200K passive assets by Q2
```

**2. Review with CPA:**
- Present annual report
- Discuss trends (improving/declining Active %)
- Plan for next year's compliance

**3. Update Tax Planning:**
- If exit anticipated in next 24 months, ensure 90%+ active NOW
- If Active % declining, develop action plan

---

## Exit Planning: 12-Month Pre-Exit Checklist

**12 Months Before Anticipated Exit:**

- ✅ Review past 24 months of QSBC monitoring
- ✅ Confirm ALL quarters were >50% active
- ✅ Current Active % is 90%+ (for at-disposition test)
- ✅ Engage CPA for pre-exit QSBC audit ($3K-$5K)
- ✅ Obtain QSBC compliance certificate
- ✅ Engage tax lawyer for opinion letter ($5K-$10K)

**6 Months Before Exit:**
- ✅ Purification transaction if needed (distribute passive assets)
- ✅ Final QSBC validation
- ✅ Prepare Section 107(2) rollover mechanics (trust distributions)

**At Closing:**
- ✅ Provide QSBC compliance certificate to purchaser
- ✅ Execute trust distributions (if applicable)
- ✅ File T1 returns with LCGE claims

---

## Conclusion: The $2.5M Quarterly Habit

**QSBC quarterly monitoring is the SIMPLEST way to protect $2.5M in tax savings.**

**Time Required:**
- Quarterly: 2-3 hours
- Annually: 4-5 hours (year-end review with CPA)
- Total: 12-20 hours/year

**Cost:**
- DIY + annual CPA review: $500-$1,000/year
- Full CPA service: $2,000-$2,500/year

**Value Protected:**
- $5,018,556 LCGE benefit (4 beneficiaries)
- $2,500,000 tax savings

**ROI:** Spending 15 hours/year and $1,000/year to protect $2.5M = $166,667 per hour

**This is the highest-ROI task in your business.**

**Set calendar reminders TODAY:**
- ✅ Q1 Monitoring: January 31
- ✅ Q2 Monitoring: April 30
- ✅ Q3 Monitoring: July 31
- ✅ Q4 Monitoring: October 31

**Never miss a quarter. Your family's financial future depends on it.**

---

**Next Steps:**
1. ✅ Download QSBC Quarterly Monitoring Spreadsheet (included with package)
2. ✅ Complete baseline assessment (current Active %)
3. ✅ Set calendar reminders for quarterly reviews
4. ✅ Engage CPA for initial setup consultation ($1,000-$1,500)
5. ✅ Integrate with accounting software (QuickBooks/Xero export)
6. ✅ Review with tax advisor annually
7. ✅ Execute purification transaction if currently non-compliant

**The difference between QSBC compliance and non-compliance: $2,500,000 in tax.**

**Make quarterly monitoring NON-NEGOTIABLE.**
