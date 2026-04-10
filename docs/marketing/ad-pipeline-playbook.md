# OnDevice AI — Ad Pipeline Playbook
## How We Actually Run Organic Marketing (As We Go)

*Started: 2026-04-10*
*Purpose: Pipeline learning — document what worked, what didn't, so the next product launch is faster.*

---

## What This Is

This is the log of how we run organic content for OnDevice AI. Not theory — what we actually did, in order. Updated after each post, each experiment, each failure.

The goal isn't to be a marketing reference doc. It's to answer the question "how do you actually go from product to first customer?" for future launches.

---

## The Stack (What We're Using)

| Layer | Tool | Cost | Why |
|-------|------|------|-----|
| Content strategy | Claude Code (me) | $0 | Drafts hooks, calendars, copy |
| Video production | ComfyUI on DGX Spark | $0 (hardware owned) | Wan 2.2 for hero, LTX-2.3 for iteration |
| Video editing | ffmpeg | $0 | Platform exports (9:16 for Reels/TikTok) |
| Posting | Manual (Meta Suite, TikTok app) | $0 | No scheduling tool yet |
| Analytics | FB/IG Insights + Stripe | $0 | What matters: orders, not vanity |
| WhatsApp broadcast | Personal WhatsApp Business | $0 | Limited to 256 contacts for now |
| Payments | Stripe + IntaSend (M-Pesa) | 2.9% + fees | Revenue |

**Total paid ad spend: $0.** Everything here is organic.

---

## Pre-Launch Decisions (Made Before Week 1)

### 1. Kenya-first, not global

Why: CPM is lower, competition is lower, M-Pesa integration differentiates us, data costs make offline AI more valuable. We're not trying to win in the US on $0 organic budget.

### 2. Single CTA everywhere: on-device.org

No QR codes, no multiple links, no "link in bio" ambiguity. One URL. Said explicitly in every post. Easy to type on mobile.

### 3. Don't mention Google Play

The app isn't on the Play Store. Mentioning it to say "not there" raises doubt. Just say "download from on-device.org."

### 4. Price in KSh for Kenya content

$4.99 = ~KSh 650 at current rates. Local currency feels more accessible and reduces the "is this a foreign app?" friction.

### 5. Show airplane mode in every video demo

The offline capability is the product's single biggest differentiator. Every demo must prove it visually. No airplane mode on screen = audience assumes it needs internet.

---

## Week-by-Week Log

### Week 1 (2026-04-10 start)

**Theme: Proof**

Posts planned: 6 (FB/IG) + 2 Reels + 2 TikTok + 7 WhatsApp Status

Key posts:
- Day 1: Launch announcement (static, airplane mode screenshot)
- Day 2: First demo Reel (airplane mode ON, AI answering real question)
- Day 6: Comparison post (OnDevice AI vs. ChatGPT Plus, KSh pricing)

**What to watch for:**
- Which post format gets the most link clicks? (Reel vs. static vs. text)
- Do people DM asking questions before buying?
- Does the KSh comparison post get shared?

*Results — update after week:*
[ ] Orders in week 1:
[ ] Best-performing post:
[ ] Most common question received:
[ ] Unexpected channel (something worked that wasn't expected):

---

### Week 2 (2026-04-17 start)

**Theme: Pull**

Posts planned: 4 (FB/IG) + 2 Reels + 2 TikTok + 7 WhatsApp Status

Key posts:
- Day 8: Address the "Unknown source" sideloading fear directly
- Day 10: Data cost math (offline AI vs. burning data for ChatGPT)
- Day 13: Soft launch pricing urgency (honest, no countdown)

*Results — update after week:*
[ ] Orders in week 2:
[ ] Objection post performance (Day 8): did addressing fear increase trust?
[ ] Did scarcity post (Day 13) drive same-day conversions?
[ ] Any organic shares (WhatsApp, FB groups)?

---

## What We Know So Far (Running List)

*Add entries as we learn. Date each one.*

**2026-04-10:**
- The deep link activation flow works end-to-end. Email → APK download → app install → `ai.ondevice.app://activate?order_id=X` → license stored in EncryptedSharedPreferences. Verified on physical Samsung S22 Ultra.
- WhatsApp Status is the distribution channel we're most uncertain about — no analytics, no click tracking. May not be measurable.
- The comparison post (KSh pricing vs. ChatGPT) is the strongest hook we have on paper. Need to validate with real engagement data.

---

## Failure Log

*Document every post/experiment that didn't work. More valuable than wins.*

*None yet — add as they happen.*

---

## The Meta-Questions (Pipeline Learning)

These are the questions this whole exercise is trying to answer for future launches:

1. **What's the minimum viable content output for organic launch?**
   Hypothesis: 2 posts/week on FB/IG + daily WhatsApp Status is the floor. Less than that and the algorithm won't show you to anyone.

2. **Does Reel/TikTok actually convert to sales, or just views?**
   Short video gets views. Whether those views become $4.99 purchases is unclear. Tracking via Stripe + UTM parameters on on-device.org links.

3. **How many touchpoints before someone buys?**
   Typical SaaS: 7–12 touchpoints. We're a $4.99 impulse buy. Hypothesis: 3–4 touchpoints if the product demo is convincing.

4. **Does addressing objections publicly (Day 8 post) build trust or amplify fear?**
   Risk: saying "Android will show a warning" might scare people who wouldn't have seen it. Reward: people who were scared and on the fence may convert. Testing this explicitly.

5. **What's the real CAC on $0 ad spend?**
   CAC = founder time. If it takes 2 hours/week of content creation and we get 10 customers/week, CAC = ~1/5 of an hour = a meaningful number when hourly rate is considered. Track this.

---

## Tools We Evaluated But Didn't Use (And Why)

| Tool | Reason not used |
|------|----------------|
| Buffer/Later (scheduling) | Overhead not worth it at this stage. Post manually. |
| Canva | DGX Spark + ComfyUI produces better visuals. |
| Influencer marketing | No budget. Would consider product-for-review for micro-influencers. |
| Google Ads | Too expensive for Kenya installs at $0 budget. Facebook CPIs are 5–10x cheaper anyway. |
| SMS marketing | No +254 number yet. Africa's Talking is the path when ready. |

---

## Next Experiments (Planned, Not Done Yet)

- [ ] Product-for-review: DM 5 Kenyan tech creators with free license, no payment
- [ ] WhatsApp group seeding: share demo video in 10 Kenyan tech/entrepreneur groups
- [ ] Student channel: reach out to university tech clubs (JKUAT, Strathmore, UoN)
- [ ] Africa's Talking WhatsApp Business API: get proper +254 number for broadcast
- [ ] UTM links on all posts: `on-device.org?utm_source=instagram&utm_campaign=week1`
