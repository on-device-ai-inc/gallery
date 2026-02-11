# CRA Audit Analyzer Skill - Complete Package

## What You're Getting

You now have a **comprehensive CRA audit risk assessment system** consisting of:

1. **cra-audit-analyzer.skill** - Installable Claude skill with complete assessment framework
2. **cra_auditors_family_trust_tech_research.md** - Deep research on CRA enforcement teams and tactics
3. **Case law research (embedded in skill)** - All major precedents affecting family trust structures

## The Skill's Capabilities

The CRA Audit Analyzer skill provides **5-dimensional risk analysis** with **quantified confidence scores**:

### 1. Audit Trigger Probability (LOW/MEDIUM/HIGH/CRITICAL)
- Analyzes against documented CRA selection criteria
- Global High Wealth (GHW) program triggers
- High Complexity TSO indicators
- Trust reporting failures
- Passive income monitoring

### 2. Audit Methodology Exposure
- Economic entity approach (entire group examined)
- Net worth audit risk
- Bank deposit analysis
- Subsection 75(2) attribution testing
- QSBC qualification challenges
- GAAR "functional equivalence" analysis

### 3. Case Law Alignment (STRONG/MODERATE/WEAK)
- Compares your structure to taxpayer wins (Sommerer, Alta Energy, DAC Investment, Pellerin)
- Identifies CRA win patterns (Deans Knight, Collins, Garron, D'Arcy)
- Calculates confidence adjustments based on precedents

### 4. Technical Compliance (COMPLIANT/WATCH LIST/NON-COMPLIANT)
- Subsection 75(2) attribution traps
- QSBC qualification (90%/50% asset tests, 24-month holding)
- TOSI implications
- GAAR vulnerability assessment
- Section 107(4.1) rollover denial risk

### 5. Win Probability Confidence (Percentage Scores)

**Example Output:**
```
CONFIDENCE ASSESSMENT:
├─ Audit Selection: 65% confident structure avoids audit
├─ Objection Success: 75% confident objection would be allowed
├─ Tax Court Success: 60% confident of favorable court ruling
└─ Overall Win Probability: 67% weighted average
```

## How the Skill Works

### Assessment Process

**Step 1: Information Gathering**
The skill asks detailed questions about:
- CCPC structure (incorporation, ownership, operations)
- Family trust details (settlor, trustees, beneficiaries, terms)
- Financial profile (assets, passive income, SR&ED credits)
- Transactions and planning (estate freezes, distributions, reorganizations)
- Risk indicators (income/asset discrepancies, funding rounds)

**Step 2: Loads Relevant Reference Materials**

The skill includes 5 comprehensive reference files:

- `audit-triggers.md` - CRA selection criteria, GHW program stats ($4.1M avg reassessment), audit methodologies
- `case-law-database.md` - Complete summaries of major cases with win/loss patterns
- `gaar-analysis.md` - Post-Deans Knight "functional equivalence" test framework
- `qsbc-compliance.md` - Detailed QSBC qualification tests, quarterly monitoring requirements
- `attribution-traps.md` - Subsection 75(2) and section 107(4.1) permanent trap analysis

**Step 3: Multi-Dimensional Analysis**

Applies scoring framework across all 5 dimensions using:
- Documented CRA enforcement statistics
- Case law precedent alignment
- Technical compliance checklist
- Risk factor aggregation

**Step 4: Generates Comprehensive Report**

Delivers detailed assessment including:
- Executive summary with overall risk level
- Audit trigger analysis
- Technical compliance review
- Case law comparison
- Win probability breakdown with percentages
- Specific vulnerabilities (prioritized)
- Remediation recommendations

**Step 5: Actionable Recommendations**

Tailored guidance based on risk level:
- High-risk (≤40% confidence): Restructure immediately, engage specialized counsel
- Medium-risk (40-70%): Strengthen documentation, address compliance gaps
- Low-risk (70%+): Maintain current structure, ongoing monitoring

## Key Intelligence from Research

### CRA Enforcement Statistics

**Global High Wealth Audit Program:**
- 2,500+ identified groups (doubled from 1,100 in 2019)
- 180 audits completed in 2023-24
- **$745M fiscal impact = $4.1M average per audit**
- 30+ dedicated audit teams
- Multi-year audits examining entire economic groups

**Overall Compliance Activity:**
- $15.3B total fiscal impact (2023-24)
- 128,000 objections filed (doubled from 68,000 in 2018-19)
- **60% of objections fully or partially allowed** (CRA loses majority)
- Processing delays: 6-9 months for objection response

### Critical Case Law Outcomes

**Taxpayer Victories:**
- *Sommerer* (2012): FMV sales to trusts avoid subsection 75(2) attribution
- *Alta Energy* (2021): $380M gain preserved - treaty rights protected from GAAR
- *DAC Investment* (2024): Regime selection not inherently abusive
- *Pellerin* (2015): Trust holding satisfies 24-month QSBC requirement

**CRA Victories:**
- *Deans Knight* (2023): "Functional equivalence" test - technical compliance insufficient
- *Collins* (2022): No "undo" for tax mistakes - structures must be right from inception
- *Garron* (2012): $152M upheld - management/control test for trust residence
- *Durocher* (2016): Third-party options destroyed QSBC status retroactively

### The Subsection 75(2) Trap

**Critical Finding:** If subsection 75(2) has applied to ANY property of a trust at ANY TIME, section 107(4.1) denies rollover treatment FOREVER.

**Result:**
- Trust distribution triggers immediate capital gain
- Attributed gains not eligible for LCGE
- Cannot be fixed retroactively (*Collins* principle)
- Difference: $0 tax (proper structure) vs. $1M+ tax (improper structure)

**Safe Structure:**
- Independent settlor (NOT founder)
- Trust subscribes for shares at inception OR purchases at FMV
- Founder never named as beneficiary (no reversion possible)
- Independent trustees
- Fixed beneficiaries from creation

### GAAR Risk Post-Deans Knight (2023)

**New Standard:** "Functional equivalence" to prohibited outcome = abusive

**Plus Bill C-59 Amendments (2024):**
- **25% penalty** on denied GAAR benefits (new)
- +3 years reassessment period (6-7 years total)
- "One of the main purposes" test (lower bar)

**Impact:** Marginal planning now exposed to:
- Original tax + interest
- 25% penalty on tax benefit
- Extended audit window

**Example:** $1M LCGE denied = $500K tax + interest + $125K penalty = $625K+ exposure

## How to Install and Use the Skill

### Installation

1. **In Claude.ai:**
   - Go to Settings → Skills
   - Click "Upload Skill"
   - Select `cra-audit-analyzer.skill` file
   - Skill will appear in your available skills

2. **Triggering the Skill:**

The skill automatically triggers when you ask Claude to:
- "Assess audit risk of my CCPC family trust structure"
- "Evaluate my QSBC qualification for LCGE"
- "What's my confidence level for surviving a CRA audit?"
- "Analyze subsection 75(2) compliance for my trust"
- "Calculate win probability if CRA challenges my structure"

### Example Usage

**You:** "I have a CCPC worth about $15M that I incorporated 5 years ago. I created a family trust 2 years ago to hold the common shares for my wife and two kids. We're planning to sell the company in the next 12-18 months and want to use the LCGE to shelter the gains. Can you assess my audit risk and confidence level?"

**Claude (with skill):**
The skill will:
1. Ask detailed follow-up questions about your structure
2. Load relevant reference materials (audit-triggers.md, case-law-database.md, qsbc-compliance.md, attribution-traps.md)
3. Analyze across all 5 dimensions
4. Calculate specific confidence scores
5. Deliver comprehensive report with:
   - Overall risk level (e.g., MEDIUM - 3 audit triggers)
   - Win probability: 67% weighted average
   - Specific vulnerabilities identified
   - Actionable remediation steps

### What Makes This Skill Unique

**Quantified Confidence Scores:**
Unlike generic advice, the skill provides **specific percentage confidence levels** based on:
- 60% objection success rate (documented)
- $4.1M average GHW reassessment (documented)
- Case law win/loss patterns
- CRA enforcement statistics

**5-Dimensional Analysis:**
Most assessments focus only on technical compliance. This skill adds:
- Audit selection probability
- Specific CRA methodologies that would be used
- Case law precedent alignment
- Aggregate win probability across stages

**Reference to Real Cases:**
Every assessment cites specific precedents:
- "Your structure aligns with *Sommerer* (taxpayer win) but differs from *Deans Knight* (CRA win) because..."
- Provides confidence adjustments based on documented outcomes

**Actionable Recommendations:**
Not just "you might have a problem" - the skill provides:
- Immediate actions ranked by priority
- Specific documentation to gather
- Structural changes to consider
- Timeline for remediation

## Real-World Application Examples

### Example 1: Clean Structure (Low Risk)

**Facts:**
- CCPC incorporated 10 years ago
- Family trust created at inception by spouse (independent settlor)
- Trust subscribed for common shares (not transfer from founder)
- Founder not beneficiary
- Independent professional trustee
- 90%+ active business assets consistently
- No passive income issues

**Assessment:**
- Audit Trigger: LOW (0-1 triggers)
- Compliance: COMPLIANT (0 violations)
- Case Law: STRONG (matches Sommerer, Pellerin)
- Win Probability: 85% overall

**Recommendation:** Maintain current structure, continue quarterly monitoring

### Example 2: Moderate Risk Structure

**Facts:**
- CCPC incorporated 5 years ago, rapid growth
- Family trust created 18 months ago
- Trust purchased shares from founder at FMV (independent valuation)
- Recent funding round ($10M raised, $7M sitting in cash)
- Some international customers (30% of revenue)
- Founder's spouse is trustee

**Assessment:**
- Audit Trigger: MEDIUM (3-4 triggers: trust timing, excess cash, funding round, related trustee)
- Compliance: WATCH LIST (passive asset concern, trustee independence)
- Case Law: MODERATE (matches Sommerer on FMV purchase, but cash issue like some CRA wins)
- Win Probability: 58% overall

**Recommendations:**
- Deploy excess cash into active business immediately
- Add independent co-trustee
- Document business purpose for trust creation
- Conduct quarterly 50%/90% asset tests
- Prepare for possible audit in 12-24 months

### Example 3: High Risk Structure (Needs Remediation)

**Facts:**
- CCPC incorporated 8 years ago
- Family trust created 6 months ago (just before potential exit discussions)
- Founder is settlor AND discretionary beneficiary
- Trust received shares as gift from founder
- Trust deed has standard "in extremis" reversion clause
- $12M in cash from recent funding (>50% of assets)
- Net worth $35M

**Assessment:**
- Audit Trigger: HIGH (6+ triggers: timing, HNW threshold, cash, gift, poor trust structure)
- Compliance: NON-COMPLIANT (subsection 75(2) applies, QSBC likely fails, section 107(4.1) trap)
- Case Law: WEAK (matches Collins on cannot-fix, Deans Knight functional equivalence risk)
- Win Probability: 28% overall

**Recommendations:**
- **URGENT:** Structure is severely defective
- Subsection 75(2) applies → LCGE benefit lost
- Section 107(4.1) trap → distribution will trigger tax
- Cannot be fixed retroactively (*Collins*)
- Engage specialized tax counsel immediately
- Consider creating new compliant structure for future planning
- Prepare for potential $1M+ tax exposure on distribution
- Voluntary disclosure may be appropriate if non-compliance exists

## Maintenance and Updates

**When to Reassess:**
- Major corporate transactions (funding, M&A, reorganization)
- Trust distributions to beneficiaries
- Material changes in asset composition
- New case law released
- CRA policy changes
- Legislative amendments

**Skill Updates:**
As new case law emerges and CRA enforcement patterns change, reference files can be updated to reflect:
- New precedents
- Revised audit statistics
- Updated GAAR interpretations
- Legislative changes

## Value Proposition

**Traditional CPA/Tax Lawyer Assessment:**
- Cost: $5,000-$25,000 for initial assessment
- Timeline: 2-4 weeks
- Depth: Varies widely by practitioner
- Quantification: Rarely provided

**CRA Audit Analyzer Skill:**
- Cost: Included with Claude
- Timeline: 20-30 minutes interactive assessment
- Depth: 5-dimensional analysis with comprehensive reference materials
- Quantification: Specific percentage confidence scores based on documented outcomes

**Use Case:**
- **Initial screening** before engaging expensive specialists
- **Ongoing monitoring** of structure health
- **Decision support** for planning options
- **Due diligence** on proposed structures
- **Education** on CRA enforcement and case law

**Not a Replacement For:**
- Specialized tax counsel for actual audit defense
- Professional opinions for disclosure purposes
- Legal advice for specific transactions

**Best Used As:**
- Early warning system for audit risk
- Educational tool on CRA enforcement
- Framework for discussing issues with CPAs/lawyers
- Ongoing compliance monitoring

## Technical Details

**Skill Size:** ~23KB packaged

**Contents:**
- 1 SKILL.md (10KB) - Core assessment framework
- 5 reference files (total ~50KB uncompressed):
  - audit-triggers.md (comprehensive CRA selection criteria)
  - case-law-database.md (major precedents with outcomes)
  - gaar-analysis.md (post-Deans Knight framework)
  - qsbc-compliance.md (detailed qualification tests)
  - attribution-traps.md (subsection 75(2) and 107(4.1))

**Progressive Disclosure:**
- Skill description always loaded (triggers appropriately)
- SKILL.md loaded when skill triggers
- Reference files loaded only as needed based on specific issues

**Token Efficiency:**
- Concise instructions in main skill
- Detailed analysis in references (loaded on demand)
- Avoids bloating context window with unnecessary information

## Support and Feedback

**For Technical Issues:**
- Skill not triggering: Try more specific queries like "Assess my CCPC trust structure for CRA audit risk"
- Incomplete analysis: Provide more detailed structure information when asked

**For Content Updates:**
- New case law: Reference files can be updated with new precedents
- Changed CRA statistics: audit-triggers.md can reflect new data
- Legislative changes: GAAR, QSBC, or trust rules amendments can be incorporated

**For Enhancement Requests:**
- Additional analysis dimensions
- New compliance areas
- Different structure types (non-tech companies, real estate, etc.)

---

## Quick Start Guide

1. **Install** the cra-audit-analyzer.skill file in Claude
2. **Ask** Claude to assess your CCPC + family trust structure
3. **Provide** detailed information when prompted
4. **Review** the comprehensive assessment with confidence scores
5. **Act** on prioritized recommendations
6. **Monitor** ongoing compliance with quarterly reviews

You now have professional-grade CRA audit risk analysis at your fingertips, backed by documented enforcement statistics, case law precedents, and quantified confidence scoring.
