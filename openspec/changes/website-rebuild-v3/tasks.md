# Tasks: OnDevice AI Website Rebuild v3

**Status**: 🟡 AWAITING APPROVAL
**Total tasks**: 38
**Sprints**: 3

---

## Sprint 1 — Copy + Schema + Company Name

### S1-01 — Global company name replacement
**Files**: All `.tsx`, `.ts`, `.md` files
**Change**: Replace every occurrence of `"Gora AI Inc."` → `"OnDevice AI Inc."` sitewide
**Includes**: Page copy, FAQ answers, install page safety note, any legal text

---

### S1-02 — Pricing single source of truth
**File**: Create `src/lib/pricing.config.ts`
**Change**: Extract all prices into typed config:
```ts
export const PRICING = {
  USD: { amount: 0.99, symbol: "$", display: "$0.99" },
  KES: { amount: 130, symbol: "KSh", display: "KSh 130" },
  NGN: { amount: 1600, symbol: "₦", display: "₦1,600" },
  ZAR: { amount: 18, symbol: "R", display: "R18" },
  GHS: { amount: 15, symbol: "GH₵", display: "GH₵15" },
  TZS: { amount: 2700, symbol: "TSh", display: "TSh 2,700" },
  GBP: { amount: 0.79, symbol: "£", display: "£0.79" },
  CAD: { amount: 1.39, symbol: "CA$", display: "CA$1.39" },
  EUR: { amount: 0.89, symbol: "€", display: "€0.89" },
  UGX: { amount: 3700, symbol: "UGX", display: "UGX 3,700" },
} as const;
export const DEFAULT_CURRENCY = "USD";
```
Update all components that hardcode `"$0.99"` or any price string to read from this config.

---

### S1-03 — Page title + meta description
**File**: `src/app/layout.tsx` or per-page metadata
**Change**:
- Title: `"OnDevice AI — AI That Runs on Your Phone"`
- Meta description: `"Powerful AI that runs entirely on your Android phone. No subscription, no network needed. Pay once, own it outright."`
Add per-page metadata for `/buy`, `/install` as well.

---

### S1-04 — Hero copy replacement
**File**: `src/app/page.tsx`
**Change**:
- Headline: `"AI That Works Without Internet."` → `"AI That Runs On Your Phone."`
- Subhead: `"Full private AI on your Android phone. Pay once, own it forever."` → `"Powerful AI on your Android. Pay once, own it forever."`

---

### S1-05 — Mission banner replacement
**File**: `src/app/page.tsx`
**Change**: `"Our mission: give every African access to AI — regardless of internet, income, or infrastructure."` → `"AI should work for everyone. We built it to run on the phone in your pocket."`

---

### S1-06 — Value proposition bar (3 pills, replace text)
**File**: `src/app/page.tsx`
**Change**: Reduce from 4 pills to 3, replace text:
- Pill 1: `"0 MB data to chat"` → `"Zero network needed"`
- Pill 2: `"Pay once, own forever"` → `"You own the AI"`
- Pill 3: `"No subscriptions"` → `"Works without connection"`
- Remove Pill 4 entirely (Ban icon pill)

---

### S1-07 — Image banner subhead
**File**: `src/app/page.tsx`
**Change**: `"No subscription. No server. No one watching."` → `"No subscription. No server. No wahala."`
Keep headline `"Premium AI that belongs to you."` unchanged.

---

### S1-08 — Pricing section: remove "Free app updates forever"
**File**: `src/app/page.tsx`
**Change**: Remove the "Free app updates forever" feature item from the pricing card. Keep the other three:
- "Lifetime offline access"
- "0 MB data per chat session"
- "All model sizes included"

---

### S1-09 — How It Works copy (Step 01 + Step 03)
**File**: `src/app/page.tsx` or `src/components/sticky-steps.tsx`
**Change**:
- Step 01 body: `"Pay the $0.99 one-time fee using card. Receipt and download link sent to your email instantly."` → `"Pay once — card or M-Pesa. Download link delivered to your email instantly."`
- Step 01 tag: `"No subscription. No recurring charges. Ever."` → `"No subscription. No recurring charges. Lifetime ownership."`
- Step 03 body: `"Turn on Airplane mode to test it. Open the app. Your AI answers instantly with zero network connection."` → `"Open the app. Ask anything. Your AI responds with zero network connection."`
- Step 03 tag: `"Zero data. Zero cloud. Zero latency."` → `"No network. No server. Yours."`
Step 02: NO CHANGES.

---

### S1-10 — FAQ: add ChatGPT/Claude comparison question
**File**: `src/app/page.tsx`
**Change**: Add new FAQ entry (5th question):
- Q: `"How does this compare to ChatGPT or Claude?"`
- A: `"ChatGPT and Claude run on cloud servers — every message requires internet and a monthly subscription. OnDevice AI runs the AI model directly on your phone. You pay once, own it outright, and use it with or without a connection. Different architecture, different tradeoffs — both are real AI."`

---

### S1-11 — Footer copy replacement
**File**: `src/app/page.tsx` (footer section)
**Change**:
- Tagline: `"AI for everyone. Everywhere. Even when the network fails."` → `"AI that runs on your phone. Powerful, portable, yours."`
- Company line: `"Built by Gora AI Inc. (Ottawa, Canada) — African roots, global mission."` → `"By OnDevice AI Inc."`
Add placeholder links: `/privacy` and `/terms` (can be simple static pages)

---

### S1-12 — Install page company name
**File**: `src/app/install/page.tsx`
**Change**:
- `"This is a legitimate product from Gora AI Inc."` → `"This is a legitimate product from OnDevice AI Inc."`
- `"Signed by Gora AI Inc."` → `"Signed by OnDevice AI Inc."`

---

### S1-13 — JSON-LD: MobileApplication schema
**File**: `src/app/layout.tsx` or `src/app/page.tsx`
**Change**: Add `<script type="application/ld+json">` with MobileApplication schema (no privacy claims):
- name, OS, category, description, offers (price + currency), featureList, downloadUrl, softwareVersion, fileSize, author
See spec §7.1 for full JSON-LD

---

### S1-14 — JSON-LD: FAQPage schema
**File**: `src/app/page.tsx`
**Change**: Add FAQPage JSON-LD with 4 Q&As matching the rendered FAQ section
See spec §7.2

---

### S1-15 — robots.txt
**File**: `public/robots.txt`
**Change**: Create/replace with version that allows all AI crawlers (GPTBot, PerplexityBot, Google-Extended, ClaudeBot, Amazonbot)
See spec §7.3

---

### S1-16 — sitemap.xml
**File**: `public/sitemap.xml`
**Change**: Create with 8 URLs (/, /buy, /install, /ke, /ng, /za, /gh, /tz, /zw) and hreflang alternates
See spec §7.4

---

### S1-17 — GEO citation paragraph
**File**: `src/app/page.tsx`
**Change**: Add AI-crawlable intro paragraph as the first body content after hero. Can be visually styled as a subtitle. Content:
> "OnDevice AI is a powerful AI assistant built by OnDevice AI Inc. that runs entirely on your Android phone using Google's Gemma language models via the LiteRT inference framework. Unlike ChatGPT or Claude, which route every query through cloud servers, OnDevice AI performs all inference locally on your device. It works with or without an internet connection, costs a one-time fee of $0.99, and accepts M-Pesa, MTN MoMo, and card payments. It runs on phones with as little as 3GB RAM, including Tecno Spark, Infinix Smart, Redmi, and Samsung A-series devices."

---

## Sprint 2 — UX Polish + New Sections

### S2-01 — New section: "See What You Can Do" (replaces testimonials)
**File**: `src/app/page.tsx`
**Change**: Remove the 3 testimonial cards (Kwame, Blessing, Samuel). Replace with a 4-card prompt-response demo section:
- Layout: horizontal scroll carousel on mobile, 2×2 grid on desktop
- Cards: Student (WAEC Biology), Professional (follow-up email), Developer (Python debug), Business Owner (shea butter listing)
- Pure text/CSS, no images, no JS beyond scroll snapping, under 5KB
See spec §4.2 for full card content

---

### S2-02 — New persona card: Business Owners (4th use case)
**File**: `src/app/page.tsx` (use cases section)
**Change**: Add 4th card to the use cases grid:
- Title: `"For Business Owners"`
- Copy: `"Write product listings, reply to customers, and generate invoices. Runs on the phone you already have."`

---

### S2-03 — New section: "Choose Your Architecture" comparison matrix
**File**: `src/app/page.tsx`
**Change**: Add section after pricing, before how-it-works:
- Headline: `"Choose Your Architecture"`
- Subhead: `"Cloud AI routes your questions to a server. On-device AI runs the model on your phone. Both are real AI. One you rent. One you own."`
- 6-row table: Architecture, Internet, Price, Data Cost, Rate Limits, Annual Cost
- Columns: OnDevice AI vs ChatGPT/Claude

---

### S2-04 — /download page: light theme
**File**: `src/app/download/page.tsx`
**Change**: Replace dark theme (`bg-[#0A0A0A]`, white text) with light theme matching `/buy` (slate-50 background, slate-800 text). Maintain visual hierarchy and contrast.

---

### S2-05 — M-Pesa phone input validation
**File**: `src/app/buy/checkout-form.tsx`
**Change**: Add client-side validation for Kenyan phone format:
- Must start with `07`, `01`, or `+2547`, `+2541`
- Must be 10 digits (local) or 13 digits (international)
- Show inline error: `"Enter a valid Kenyan number (e.g. 0712345678)"`
- Block form submission if invalid

---

### S2-06 — M-Pesa pending state copy
**File**: `src/app/buy/checkout-form.tsx`
**Change**: Replace generic spinner copy with:
- Heading: `"Check your phone"`
- Body: `"A payment request has been sent to [phone]. Enter your M-Pesa PIN to confirm."`
- Sub: `"This page will update automatically once payment is confirmed."`

---

### S2-07 — DownloadButton error state
**File**: `src/components/download-button.tsx`
**Change**: Add error handler to download link click. If fetch fails (non-200), show:
- Error box: `"Download failed. Try the button again, or contact us on WhatsApp."`
- WhatsApp link: `wa.me/254...` (use existing contact number from site)

---

### S2-08 — Hero anchor scroll target
**File**: `src/app/page.tsx`
**Change**: Verify `"See How It Works"` button has `href="#how-it-works"`. Add `id="how-it-works"` to the StickySteps section wrapper if missing. Ensure smooth scroll is enabled (`scroll-behavior: smooth` in globals.css).

---

### S2-09 — Pricing card width
**File**: `src/app/page.tsx`
**Change**: Increase pricing card container from `max-w-lg` to `max-w-2xl`. Rebalance internal padding and font sizes so card doesn't look sparse.

---

### S2-10 — FAQ expansion (7–8 questions total)
**File**: `src/app/page.tsx`
**Change**: Add 3 additional FAQ entries alongside the new ChatGPT/Claude Q (already in S1-10):
- Q: `"What Android version do I need?"` — A: `"Android 9.0 or higher. Works on most phones made after 2018, including Tecno, Infinix, Redmi, and Samsung Galaxy A-series."`
- Q: `"What if I lose my phone or get a new one?"` — A: `"Email your original receipt to support and we'll resend your download link. One purchase covers you across devices."`
- Q: `"Can I share the app with someone else?"` — A: `"Each purchase is a personal license. If someone else wants to use it, they can buy their own copy — it's $0.99."`

---

### S2-11 — Footer: add Privacy Policy + Terms links
**File**: `src/app/page.tsx` (footer)
**Change**: Add two placeholder links in footer nav:
- `/privacy` — Privacy Policy
- `/terms` — Terms of Service
Create minimal static pages for each (single paragraph, placeholder text is fine, just needs to exist for legal hygiene).

---

### S2-12 — Sticky mobile CTA padding guard
**File**: `src/app/page.tsx`
**Change**: Add `pb-20 sm:pb-0` (or `pb-16`) to the main page content wrapper on mobile, so the fixed sticky CTA bar doesn't overlap the footer content on short screens.

---

### S2-13 — NavBar: hamburger transition animation
**File**: `src/components/nav-bar.tsx`
**Change**: Replace instant show/hide of the mobile drawer with CSS transition:
- Add `transition-all duration-150 ease-in-out overflow-hidden`
- Animate `max-height` from `0` → content height (or use `opacity` + `translate-y`)

---

### S2-14 — NavBar: remove non-functional user icon
**File**: `src/components/nav-bar.tsx`
**Change**: Remove the UserCircle icon from mobile nav header. It has no click handler and misleads users into thinking there's an account system. If a "Get App" CTA is more useful here, replace it.

---

### S2-15 — NavBar: active link state
**File**: `src/components/nav-bar.tsx`
**Change**: Use `usePathname()` to detect current route. Apply active styles to matching nav link (e.g., `font-semibold` or `text-cyan-600` underline).

---

### S2-16 — StickySteps: reduce mobile height
**File**: `src/components/sticky-steps.tsx`
**Change**: On mobile (`< md`), switch from the 300vh scroll-driven layout to a simpler sequential stack (3 cards stacked vertically, no sticky viewport). Preserve the scroll-driven animation only on `md+` screens.

---

### S2-17 — ParticleBackground: respect prefers-reduced-motion
**File**: `src/components/particle-background.tsx`
**Change**: Check `window.matchMedia('(prefers-reduced-motion: reduce)')`. If true, skip `requestAnimationFrame` loop entirely (render a static blank canvas or just return null).

---

### S2-18 — ParticleBackground: disable on mobile
**File**: `src/components/particle-background.tsx`
**Change**: Return `null` early if `window.innerWidth < 768`. Saves battery on mobile, where the canvas is hidden under the hero image anyway.

---

### S2-19 — Install page: add app-download screenshot
**File**: `src/app/install/page.tsx`
**Change**: Add `app-download.png` (already exists at `/public/images/app-download.png`) as a visual in Step 5 ("Download your first model"). Use `next/image` with appropriate alt text.

---

### S2-20 — Performance: priority images
**File**: `src/app/page.tsx`
**Change**: Add `priority` prop to above-fold `<Image>` components (hero desktop and hero mobile images). This fixes LCP by telling Next.js to preload them.

---

## Sprint 3 — Geo-Targeting

### S3-01 — Hreflang tags
**File**: `src/app/layout.tsx`
**Change**: Add 7 `<link rel="alternate" hreflang="...">` tags to `<head>`:
- en-KE, en-GH, en-TZ, en-ZW, en-ZA, en-NG, x-default

---

### S3-02 — Geo-detection middleware
**File**: `src/middleware.ts` (create or update)
**Change**: Read `CF-IPCountry` header (Cloudflare) or fallback to `Accept-Language`. Map country codes to localized routes. Pass country as header to page components. Never block — always fall through to default.

---

### S3-03 — Country routes: Kenya (/ke)
**File**: `src/app/ke/page.tsx` (create)
**Change**: Clone homepage with KE-specific overrides:
- Default currency: KES (KSh 130)
- Student card: KCPE/KCSE exam prep variant
- Payment callout: M-Pesa prominent

---

### S3-04 — Country routes: Nigeria (/ng)
**File**: `src/app/ng/page.tsx` (create)
**Change**: NG-specific overrides:
- Default currency: NGN (₦1,600)
- Student card: WAEC/JAMB exam prep variant
- Cultural copy: "No wahala" banner prominent

---

### S3-05 — Country routes: South Africa (/za)
**File**: `src/app/za/page.tsx` (create)
**Change**: ZA-specific overrides:
- Default currency: ZAR (R18)
- Student card: Matric exam prep variant

---

### S3-06 — Country routes: Ghana (/gh)
**File**: `src/app/gh/page.tsx` (create)
**Change**: GH-specific overrides:
- Default currency: GHS (GH₵15)
- Student card: WASSCE exam prep variant
- Payment callout: MTN MoMo

---

### S3-07 — Country routes: Tanzania (/tz)
**File**: `src/app/tz/page.tsx` (create)
**Change**: TZ-specific overrides:
- Default currency: TZS (TSh 2,700)
- Payment callout: M-Pesa Tanzania

---

### S3-08 — Country routes: Zimbabwe (/zw)
**File**: `src/app/zw/page.tsx` (create)
**Change**: ZW-specific overrides:
- Default currency: USD ($0.99 — ZWL excluded per spec)
- Student card: ZIMSEC exam prep variant

---

## Summary

| Sprint | Tasks | Key Output |
|--------|-------|-----------|
| Sprint 1 | S1-01 to S1-17 | Copy replaced, company name clean, pricing config, SEO/schema/sitemap |
| Sprint 2 | S2-01 to S2-20 | New sections, UX polish, checkout fixed, nav/animation improved |
| Sprint 3 | S3-01 to S3-08 | Geo-targeting live for 6 markets |
| **Total** | **45 tasks** | **Full rebuild + polish** |
