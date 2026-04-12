# OpenSpec: OnDevice AI Website — Full Rebuild & Polish
**Version**: 3.0
**Date**: 2026-03-20
**Source documents**: OnDevice-AI-Rebuild-Spec-v3.docx + Website Polish Sprint
**Status**: 🟡 AWAITING APPROVAL

---

## Overview

A complete, production-ready overhaul of on-device.org across three dimensions:

1. **Copy & Positioning** — Replace all copy per the v3 spec. Eliminate privacy/speed claims. Reframe as deliberate alternative, not fallback. Company name changed from Gora AI Inc → OnDevice AI Inc sitewide.
2. **UX Polish** — 20 targeted fixes to the checkout flow, landing page, nav, animations, and performance. One fix at a time, no regressions.
3. **Growth Infrastructure** — Dynamic pricing config, JSON-LD schema, robots.txt, sitemap.xml, geo-targeting routes for 6 African markets.

---

## Scope

### IN SCOPE
- All copy replacements on `/`, `/buy`, `/install`, `/download`
- Company name global find-and-replace (Gora AI Inc → OnDevice AI Inc)
- New sections: "See What You Can Do" (replaces testimonials), "Choose Your Architecture" (comparison matrix)
- New 4th persona card: Business Owners
- Dynamic pricing config (`pricing.config.ts` as single source of truth)
- JSON-LD schemas (MobileApplication + FAQPage)
- `robots.txt` and `sitemap.xml`
- GEO citation paragraph (AI-crawlable intro text)
- Hreflang tags for 6 markets
- Geo-targeted country routes: `/ke`, `/ng`, `/za`, `/gh`, `/tz`, `/zw`
- NavBar: hamburger transition, remove non-functional user icon, active link state
- StickySteps: mobile height reduction, reduced-motion support
- ParticleBackground: disable on mobile, respect `prefers-reduced-motion`
- Checkout flow: `/download` theme normalization, M-Pesa phone validation, pending state copy, download error state
- Landing page: hero anchor, pricing card width, FAQ expansion (7–8 Qs), footer legal links, sticky CTA guard
- Install page: back button, add app-download screenshot, company name update
- Performance: per-page metadata, `priority` prop on above-fold images

### OUT OF SCOPE (per spec §0.2)
- WhatsApp integration
- Social/OG meta tags
- Testimonials (removed, no placeholders)
- Privacy as selling point
- Speed/latency claims vs. cloud AI
- Blog, /about, /pricing comparison pages, /waitlist, /dashboard (Phase 2+)

---

## Explicit Rules (from spec §1.2 + §1.3)

**Never use**: "even when", "despite", "regardless of", "no matter what", "even without", "accessible", "affordable", "budget"
**Always use**: "built for", "designed to", "by default", "on your terms", "you own", "yours"
**Never claim**: speed, low latency, instant response time vs. cloud
**Never sell**: privacy, data staying on device, surveillance protection

---

## Sprint Breakdown

### Sprint 1 — Copy + Schema + Company Name (Days 1–3)
Highest-impact, no new components. Text replacements + config + schema files.

### Sprint 2 — UX Polish + New Sections (Days 4–7)
Checkout flow fixes, landing page improvements, new content sections.

### Sprint 3 — Geo-Targeting (Days 8–14)
Six country routes, Cloudflare geo-detection, hreflang tags.

---

## Acceptance Criteria (Global)

- [ ] Zero occurrences of "Gora AI Inc." in any rendered output
- [ ] Zero privacy claims in copy ("private", "no one watching", "data stays on device" as selling points)
- [ ] Zero speed claims ("instant", "faster than", "low latency" in comparison context)
- [ ] All prices render from `pricing.config.ts` — no hardcoded price strings in components
- [ ] JSON-LD schema validates at schema.org/validator
- [ ] `robots.txt` allows all AI crawlers
- [ ] `sitemap.xml` includes all 6 country routes
- [ ] `/download` page uses light theme (matches `/buy`)
- [ ] M-Pesa phone field rejects non-Kenyan numbers
- [ ] NavBar hamburger transition animates (150ms ease)
- [ ] `prefers-reduced-motion` respected by ParticleBackground and StickySteps
- [ ] ParticleBackground disabled on screens < 768px
- [ ] Build passes: `pnpm build` exits 0
- [ ] No TypeScript errors: `pnpm typecheck` exits 0
