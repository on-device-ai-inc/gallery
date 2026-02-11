# Legal Quick Reference: Self-Hosted Gemma Models

**Last Updated:** December 30, 2025

---

## 📋 At a Glance

**What:** Self-hosting 3 Gemma AI models on HuggingFace (na5h13 account)
**Why:** Improve download success from 45% → 91%
**Legal Basis:** Gemma Terms of Use Section 3.1 (Redistribution Rights)
**Status:** ✅ Code complete, ⏳ Awaiting legal approval

---

## ✅ Compliance Checklist

### Gemma Terms Section 3.1 Requirements

- [x] **(a) Use Restrictions Enforced**
  - GemmaTermsDialog.kt shows prohibited uses
  - User must click "I Accept" before download
  - Acceptance stored in SharedPreferences

- [x] **(b) License Copy Provided**
  - Full terms linked in dialog (https://ai.google.dev/gemma/terms)
  - Prohibited Use Policy linked (https://ai.google.dev/gemma/prohibited_use_policy)
  - ATTRIBUTIONS.txt includes full legal notice

- [x] **(c) Modifications Noted**
  - No modifications made to model weights
  - SHA-256 checksums verify authenticity
  - Only change: hosting location

- [x] **(d) Attribution Retained**
  - ATTRIBUTIONS.txt credits Google DeepMind
  - In-app dialog includes attribution notice
  - Model source properly documented

---

## 🔒 Security Measures

| Measure | Implementation | Purpose |
|---------|----------------|---------|
| **SHA-256 Verification** | SecureModelDownloader.kt | Prevent tampering |
| **WiFi Enforcement** | Model config (3.5GB, 4.6GB models) | Avoid data charges |
| **Fallback URLs** | Primary: na5h13 → Fallback: Google | Ensure availability |
| **Storage Check** | Pre-download validation | Prevent failed downloads |

---

## 📊 Self-Hosted Models

| Model | Size | Primary URL | SHA-256 Checksum |
|-------|------|-------------|------------------|
| Gemma-3n-E2B-it | 3.5GB | https://huggingface.co/na5h13/gemma-3n-E2B-it-litert-lm | `2ed7bc3a...4010d6` |
| Gemma-3n-E4B-it | 4.6GB | https://huggingface.co/na5h13/gemma-3n-E4B-it-litert-lm | `2e67a6cd...3cd3c1f4` |
| Gemma3-1B-IT | 558MB | https://huggingface.co/na5h13/Gemma3-1B-IT-litert-lm | `1325ae36...bba98be` |

**Fallback:** All models fall back to Google's official HuggingFace repos if na5h13 fails

---

## ⚖️ Legal Documents

### For Lawyer Review
1. **LAWYER_REVIEW_SUMMARY.md** (5 pages) - Executive summary with critical questions
2. **LEGAL_COMPLIANCE_SELF_HOSTED_GEMMA.md** (18 pages) - Full compliance documentation
3. **Gemma Terms of Use** - https://ai.google.dev/gemma/terms

### Implementation Files
- `app/src/main/java/ai/ondevice/app/ui/common/tos/GemmaTermsDialog.kt` - Terms acceptance UI
- `app/src/main/java/ai/ondevice/app/helper/SecureModelDownloader.kt` - Download security
- `app/src/main/assets/legal/ATTRIBUTIONS.txt` - Legal notices
- `app/src/main/res/raw/model_allowlist.json` - Model configuration

---

## 🚨 Prohibited Uses (Must Notify Users)

Users **CANNOT** use Gemma for:

1. ❌ Illegal activities or harm to minors
2. ❌ Violence, terrorism, or weapons development
3. ❌ Fraud, deception, or manipulation
4. ❌ Defamation, harassment, or bullying
5. ❌ Spam, phishing, or malware
6. ❌ Privacy violations (doxxing, stalking, etc.)
7. ❌ Legal, financial, or medical advice
8. ❌ High-risk government decision-making (law enforcement, immigration, etc.)
9. ❌ Illegal discrimination
10. ❌ Any violation of laws or third-party rights

**How We Enforce:** Display in GemmaTermsDialog + require acceptance (we don't monitor usage)

---

## 🎯 Critical Legal Questions for Counsel

### 1. Terms Acceptance - Sufficient?
**Question:** Is our click-through dialog an "enforceable provision" per Section 3.1(a)?

**Our Implementation:**
- Mandatory dialog before download
- Clear "Prohibited Uses" section
- "I Accept" button required
- Links to full terms

**Alternatives to Consider:**
- Display full terms text (not just link)?
- Separate acceptance for each model?
- Explicit "I agree to be bound" language?

### 2. Liability - Adequate Protection?
**Question:** Are we liable if user violates Prohibited Uses?

**Our Position:**
- We notify users of restrictions
- Users accept binding terms
- We don't monitor model usage
- Similar to GitHub, npm, Docker Hub

**Protections Implemented:**
- Disclaimer of warranties
- Indemnification clause (proposed TOS Section 6.3)
- "AS IS" language

### 3. Privacy - GDPR Compliance?
**Question:** HuggingFace collects IP/metadata during downloads - need consent?

**Our Analysis:**
- Legitimate interest (technical necessity)
- User initiates download
- Privacy Policy discloses third-party sharing

**Possible Concerns:**
- Need explicit opt-in for EU users?
- Data processing agreement with HuggingFace?
- CCPA disclosures?

### 4. TOS Updates - Approve Language?
**Proposed Additions (see Section 6 of full compliance doc):**
- New Section 7: Third-Party AI Models
- Enhanced Disclaimers
- Indemnification Clause

**Question:** Is proposed language legally sound?

---

## 📅 Review Timeline

**Current Status:** Code complete, awaiting legal approval

**What We Need:**
1. ✅ Legal counsel review (est. 2-3 hours)
2. ✅ Approval or requested changes
3. ✅ TOS/Privacy Policy updates (if needed)
4. ✅ Final implementation (if changes needed)
5. ✅ Production release

**Estimated Time:**
- No changes needed: Release immediately
- Minor changes (disclaimers): 1-2 days
- Major changes (contract language): 1-2 weeks

---

## 🌍 International Considerations

### Export Controls
- ✅ AI models generally not export-controlled
- ✅ Gemma Terms already prohibit weapons use
- ⚠️ Monitor 2025 AI export regulations

### GDPR (EU)
- ✅ Data minimization (only acceptance timestamp)
- ✅ Local storage only (not transmitted)
- ⚠️ Third-party processor (HuggingFace)

### Geographic Restrictions?
**Question for Counsel:** Should we geo-block certain countries?
- China (CAC AI regulations)
- Russia (data localization laws)
- Others?

---

## 📞 Contacts

**Legal Questions:**
- Primary: [Your Legal Contact]
- Technical: [Your Technical Contact]

**Documents Location:**
`/home/nashie/Downloads/gallery-1.0.7/Android/src/`

---

## ✍️ Approval Sign-Off

**Once reviewed, legal counsel to advise:**

- [ ] **Approve as-is** - No changes needed
- [ ] **Approve with edits** - Specific changes required
- [ ] **Do not approve** - Fundamental redesign needed

**Specific Approvals:**

1. Terms Dialog Implementation
   - [ ] Approved
   - [ ] Needs changes: _________________

2. Liability Protection (Disclaimers/Indemnification)
   - [ ] Approved
   - [ ] Needs changes: _________________

3. Privacy Compliance (HuggingFace data sharing)
   - [ ] Approved
   - [ ] Needs changes: _________________

4. TOS Updates (Section 6 of compliance doc)
   - [ ] Approved
   - [ ] Needs changes: _________________

5. Geographic Restrictions
   - [ ] None needed
   - [ ] Block: _________________

**Signed:**

________________________________
Legal Counsel Name

________________________________
Date

---

## 🔄 Change Log

| Date | Change | Approved By |
|------|--------|-------------|
| 2025-12-30 | Initial implementation | Pending |

---

## 📚 Additional Resources

**Gemma Documentation:**
- Terms of Use: https://ai.google.dev/gemma/terms
- Prohibited Use Policy: https://ai.google.dev/gemma/prohibited_use_policy
- Model Cards: https://ai.google.dev/gemma/docs

**HuggingFace:**
- Terms of Service: https://huggingface.co/terms-of-service
- Privacy Policy: https://huggingface.co/privacy
- Model Repos: https://huggingface.co/na5h13

**Our Implementation:**
- GitHub Repo: on-device-ai-inc/OnDevice
- Latest Build: See GitHub Actions
- Commit: `1289464` (DownloadRepository fix)

---

**Document End**

For questions or clarifications, contact legal counsel or technical team.
