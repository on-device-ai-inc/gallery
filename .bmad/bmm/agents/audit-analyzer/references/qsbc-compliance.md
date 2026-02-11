# QSBC Qualification and LCGE Compliance

## Lifetime Capital Gains Exemption (LCGE)

**Current Limit (2024):** $1.25 million per individual

**Future Increases:**
- Indexed to inflation starting 2026
- Canadian Entrepreneurs' Incentive: additional $2M phased in ($400K/year 2025-2029) at reduced inclusion rate (33.3% vs 50%)

**Family Trust Multiplication Strategy:**
- Each beneficiary has separate $1.25M LCGE
- Family of 5 = $6.25M potential tax-free gains
- Trust distributes gains to beneficiaries
- Each claims exemption on their share

**Tax Savings Example:**
- $5M capital gain to one person: ~$1.25M tax (assuming 50% inclusion, 50% marginal rate)
- $5M distributed to 4 family members: $0 tax (all within LCGE limits)
- **Savings: $1.25M+**

## Qualified Small Business Corporation Shares Definition

All four tests must be satisfied:

### Test 1: CCPC at Disposition

**Requirement:** Shares must be of a Canadian-Controlled Private Corporation at time of disposition

**CCPC Definition:**
- Canadian corporation
- NOT controlled by non-residents
- NOT controlled by public corporations
- NOT controlled by combination of above

**Common Failures:**
1. **Non-resident investors with options/warrants** - See *Durocher* (options deemed control)
2. **Venture capital preferred shares** - May constitute control
3. **Non-resident founders** - Direct ownership by non-resident = failure
4. **Public corporation ownership** - Any control by public corp = failure

**Verification Required:**
- Review all shareholders agreements
- Examine all options, warrants, conversion rights
- Check for indirect control through partnerships
- Verify residency status of all shareholders

**Quarterly Monitoring Recommended:** CCPC status can be lost and regained

### Test 2: Canadian Resident Ownership

**Requirement:** Throughout 24 months before disposition, shares NOT owned by anyone other than:
- Canadian residents
- Partnerships where all members are Canadian residents

**Family Trust Consideration:**
- Trust itself need not be Canadian resident
- But all beneficiaries who could receive shares must be Canadian residents
- *Pellerin* (2015 TCC): Trust ownership can satisfy this test

**Common Issues:**
- Beneficiary class includes non-residents (even if contingent)
- Trust deed allows appointment of non-resident beneficiaries
- Settlor emigrates during 24-month period

### Test 3: 24-Month Holding Period

**Requirement:** No one other than taxpayer or related person owned shares during 24 months before disposition

**Related Person Includes:**
- Spouse
- Children, grandchildren, parents, grandparents, siblings
- Corporations/partnerships/trusts controlled by above
- Family trusts with only family beneficiaries

**Start of Holding Period:**
- Trust subscribes for shares: Date of subscription
- Trust purchases from founder: Date of purchase
- Property transferred to trust: Complex (*Sommerer* analysis needed)

**Common Traps:**
- Share reorganizations creating "new" shares (resets 24-month clock)
- Multiple classes with different acquisition dates
- Exchanges under section 85/86 may reset clock unless rollover complete

**Safe Harbor:** Shares held by family trust for 24+ months before any disposition

### Test 4: 90% Active Business Assets (ABC Test)

**Timing:** Immediately before disposition

**Requirement:** 90%+ of FMV of corporation's assets used principally in active business carried on primarily in Canada

**Calculation:**
```
Active Business Asset Test = (FMV of active business assets) / (FMV of all assets) ≥ 90%
```

**Active Business Assets:**
- Inventory
- Equipment and machinery
- Accounts receivable from active business
- Intellectual property used in active business
- Real property used in operations

**Non-Qualifying Assets (common tech company issues):**
- Cash exceeding working capital needs
- Marketable securities and investments
- Passive real estate holdings
- Loans to shareholders or related parties
- IP licensed to others (may be active or passive depending on facts)

**Tech Company Specific Issues:**

**Excess Cash Post-Funding:**
- Series A raises $10M, company needs $3M for operations
- $7M excess cash = passive asset
- Can disqualify QSBC status if represents >10% of total assets

**Solutions:**
1. Deploy capital rapidly into business
2. Purification: Pay dividends to reduce cash to <10%
3. Invest excess in related operating companies
4. Time disposition when cash is deployed

**IP Holding Structures:**
- IP held by operating company and used internally = active
- IP licensed to third parties = may be passive
- IP licensed to related OpCo = complex analysis needed

**SR&ED Credits:**
- Refundable credits treated as financial assets (passive)
- Can accumulate and disqualify QSBC

### Test 5: 50% Active Business Assets (MSBC Test)

**Timing:** Throughout 24 months before disposition, corporation must have been "small business corporation"

**Requirement:** 50%+ of FMV of assets used principally in active business carried on primarily in Canada

**More Lenient Than 90% Test:**
- Allows for more passive assets during growth phase
- But still requires active business to be dominant use

**Common Issues:**
- Bridge financing creates temporary passive asset accumulation
- Pre-exit restructuring moves assets around
- Working capital buildup pre-sale

**Quarterly Monitoring Essential:**
Calculate 50%/90% tests quarterly:
- Q1 2024: 75% active (PASS)
- Q2 2024: 45% active (FAIL - disqualifies for that quarter)
- Q3 2024: 80% active (PASS)
- Q4 2024: 92% active (PASS)

Single quarter failure in 24-month period = QSBC disqualified

## "Primarily in Canada" Requirement

**Test:** Corporation's business must be carried on primarily in Canada

**Primarily = More than 50%** (measured by revenue, employees, or assets)

**Tech Company Challenges:**
- Remote employees in other countries
- Revenue from international customers
- Cloud infrastructure hosted abroad
- Development teams offshore

**Safe Harbors:**
- Head office in Canada
- Majority of employees in Canada (by headcount or compensation)
- Majority of revenue from Canadian customers
- IP developed in Canada

**Risk Factors:**
- >50% employees outside Canada
- >50% revenue from non-Canadian sources
- Core operations managed from abroad
- Foreign subsidiaries conducting primary business

## Purification Strategies

**Goal:** Increase active business asset percentage before disposition

**Methods:**

1. **Pay Dividends**
   - Reduces cash (passive asset)
   - Can pay to holding company or shareholders
   - Must not violate other tax rules (section 84.1, TOSI)

2. **Invest in Active Business**
   - Purchase equipment/inventory
   - Hire employees (creates AR receivable = active)
   - Expand operations

3. **Transfer to Related OpCo**
   - Section 85 rollovers to move passive assets
   - Leaves PureCo with 90%+ active assets
   - Complex - requires proper planning

4. **Redemption of Shares**
   - Reduces paid-up capital
   - Can remove passive asset holdings
   - Creates deemed dividend - tax implications

**Timing Critical:**
- Must be done before disposition
- Some methods require 12-24 months advance planning
- Cannot be done as accommodation transaction (GAAR risk)

**CRA Scrutiny:**
- Last-minute purification raises red flags
- Must have business justification
- Documentation essential

## CRA Audit Approach to QSBC

**Initial Red Flags:**
- Large LCGE claims ($500K+)
- Tech companies (excess cash common)
- Recent funding rounds
- International operations
- Multiple family member claims

**Audit Focus:**

1. **CCPC Status Verification**
   - Request shareholder agreements
   - Review all option/warrant agreements
   - Check for non-resident shareholders
   - Examine control provisions

2. **Asset Test Documentation**
   - Request quarterly balance sheets for 24 months
   - Calculate 50%/90% tests for each quarter
   - Classify each asset as active vs. passive
   - Verify "primarily in Canada" through payroll, revenue data

3. **Holding Period Verification**
   - Share register review
   - Trust deed examination
   - Related party transaction timeline
   - Section 85/86 rollover analysis

4. **Third-Party Data Cross-Reference**
   - T2 corporate returns (asset composition)
   - T5 slips (investment income = passive)
   - Bank statements (cash levels)
   - Payroll records (Canadian vs. foreign employees)

**Common CRA Reassessment Positions:**

1. **"Cash exceeds working capital needs"**
   - CRA calculates "normal" cash requirement
   - Excess treated as passive investment
   - Taxpayer must justify all cash holdings

2. **"IP licensing is passive income"**
   - CRA argues royalties = passive
   - Taxpayer must prove active management
   - Related-party licenses especially scrutinized

3. **"Foreign operations are dominant"**
   - CRA counts foreign employees/revenue
   - Argues "not primarily in Canada"
   - Cloud infrastructure used as evidence

4. **"Non-resident shareholder controlled corporation"**
   - Options/warrants as deemed control
   - Investor rights as de facto control
   - Destroys CCPC status retroactively

## Defensive Documentation

**Maintain Contemporaneously:**

1. **Quarterly Asset Classifications**
   - Spreadsheet showing each asset
   - Active vs. passive designation
   - Justification for classifications
   - 50%/90% calculations

2. **Business Purpose Memos**
   - Why cash held (working capital analysis)
   - Why IP structured as held
   - Why foreign operations necessary
   - Commercial justifications for structure

3. **CCPC Status Monitoring**
   - Shareholder agreements on file
   - Option/warrant schedules
   - Residency confirmations
   - Control analysis

4. **Canadian Operations Evidence**
   - Employee location records
   - Revenue source documentation
   - Operational headquarters proof
   - Management location evidence

5. **Related Party FMV Evidence**
   - Independent valuations
   - Comparable transactions
   - Arm's length character
   - No benefit conferral

## Win Probability by Issue Type

**Strong QSBC Position (75-85% confidence):**
- 24+ months holding clearly documented
- All quarterly asset tests passed with >15% margin
- No non-resident shareholders or options
- Clear Canadian operations dominance
- Independent valuations on file

**Moderate Position (50-65% confidence):**
- Holding period met but some complexity
- Asset tests passed but close to thresholds
- Some international operations but majority Canadian
- FMV support exists but could be challenged

**Weak Position (20-40% confidence):**
- Holding period questionable
- Asset tests failed in some quarters
- Significant non-Canadian operations
- Non-resident investors with rights
- Purification appears artificial

**Critical Issues (10-25% confidence):**
- CCPC status lost during 24-month period
- Asset tests clearly failed
- Foreign operations dominant
- Accommodation transactions evident
- No contemporaneous documentation
