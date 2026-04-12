# Spec Delta: OnDevice AI Website Rebuild v3

**Status**: ✅ SPRINT 3 COMPLETE
**Tracks changes to**: All user-facing pages, shared components, config files, and SEO assets

---

## Files Changed

### NEW FILES
| File | Purpose |
|------|---------|
| `src/lib/pricing.config.ts` | Single source of truth for all prices |
| `src/middleware.ts` | Geo-detection: CF-IPCountry → country route redirect |
| `src/components/country-home.tsx` | Shared CountryHomePage server component |
| `src/app/privacy/page.tsx` | Placeholder Privacy Policy page |
| `src/app/terms/page.tsx` | Placeholder Terms of Service page |
| `src/app/ke/page.tsx` | Kenya geo-targeted homepage |
| `src/app/ng/page.tsx` | Nigeria geo-targeted homepage |
| `src/app/za/page.tsx` | South Africa geo-targeted homepage |
| `src/app/gh/page.tsx` | Ghana geo-targeted homepage |
| `src/app/tz/page.tsx` | Tanzania geo-targeted homepage |
| `src/app/zw/page.tsx` | Zimbabwe geo-targeted homepage |
| `public/robots.txt` | AI-crawler-friendly robots policy |
| `public/sitemap.xml` | Full sitemap with hreflang |

### MODIFIED FILES
| File | Changes |
|------|---------|
| `src/app/page.tsx` | Hero, mission, pills, testimonials→demo cards, use cases +Business Owners, image banner, pricing, comparison matrix, how-it-works, FAQ +4 Qs, footer, sticky CTA padding, GEO paragraph, JSON-LD schemas, priority images |
| `src/app/layout.tsx` | Page title, meta description, hreflang tags |
| `src/app/buy/page.tsx` | N/A (unchanged) |
| `src/app/buy/checkout-form.tsx` | Phone validation, M-Pesa pending state copy |
| `src/app/install/page.tsx` | Company name ×2, add app-download screenshot |
| `src/app/download/page.tsx` | Dark → light theme |
| `src/components/nav-bar.tsx` | Hamburger transition, remove user icon, active link state |
| `src/components/sticky-steps.tsx` | Step 01 + 03 copy, mobile layout (no sticky on mobile) |
| `src/components/particle-background.tsx` | Disable on mobile, respect prefers-reduced-motion |
| `src/components/download-button.tsx` | Error state with WhatsApp fallback |
| `src/middleware.ts` | ✅ Geo-detection — CF-IPCountry + Accept-Language fallback |

---

## Copy Changes by Section

### Page Title
```
BEFORE: OnDevice AI — AI That Works Without Internet
AFTER:  OnDevice AI — AI That Runs on Your Phone
```

### Meta Description
```
BEFORE: Full AI on your Android phone. No data needed, no subscription. Pay once, use forever. Works offline anywhere.
AFTER:  Powerful AI that runs entirely on your Android phone. No subscription, no network needed. Pay once, own it outright.
```

### Hero Headline
```
BEFORE: AI That Works Without Internet.
AFTER:  AI That Runs On Your Phone.
```

### Hero Subhead
```
BEFORE: Full private AI on your Android phone. Pay once, own it forever.
AFTER:  Powerful AI on your Android. Pay once, own it forever.
```

### Mission Banner
```
BEFORE: Our mission: give every African access to AI — regardless of internet, income, or infrastructure.
AFTER:  AI should work for everyone. We built it to run on the phone in your pocket.
```

### Value Pills (3 pills, was 4)
```
BEFORE: [0 MB data to chat] [Pay once, own forever] [No subscriptions] [No cloud dependency]
AFTER:  [Zero network needed] [You own the AI] [Works without connection]
```

### Image Banner Subhead
```
BEFORE: No subscription. No server. No one watching.
AFTER:  No subscription. No server. No wahala.
```

### Pricing: Remove Feature
```
REMOVED: "Free app updates forever"
KEPT:    "Lifetime offline access", "0 MB data per chat session", "All model sizes included"
```

### Step 01 Body
```
BEFORE: Pay the $0.99 one-time fee using card. Receipt and download link sent to your email instantly.
AFTER:  Pay once — card or M-Pesa. Download link delivered to your email instantly.
```

### Step 01 Tag
```
BEFORE: No subscription. No recurring charges. Ever.
AFTER:  No subscription. No recurring charges. Lifetime ownership.
```

### Step 03 Body
```
BEFORE: Turn on Airplane mode to test it. Open the app. Your AI answers instantly with zero network connection.
AFTER:  Open the app. Ask anything. Your AI responds with zero network connection.
```

### Step 03 Tag
```
BEFORE: Zero data. Zero cloud. Zero latency.
AFTER:  No network. No server. Yours.
```

### Footer Tagline
```
BEFORE: AI for everyone. Everywhere. Even when the network fails.
AFTER:  AI that runs on your phone. Powerful, portable, yours.
```

### Footer Company
```
BEFORE: Built by Gora AI Inc. (Ottawa, Canada) — African roots, global mission.
AFTER:  By OnDevice AI Inc.
```

### Company Name (Global)
```
BEFORE: Gora AI Inc.  (all occurrences)
AFTER:  OnDevice AI Inc.
```

---

## Component Behavior Changes

### ParticleBackground
- Returns `null` on `window.innerWidth < 768` (mobile)
- Returns `null` (or static) when `prefers-reduced-motion: reduce`

### StickySteps
- On `< md`: renders as 3 stacked cards (no sticky, no 300vh container)
- On `md+`: existing scroll-driven behavior unchanged

### NavBar
- Hamburger drawer: animated with `transition-all duration-150 ease-in-out`
- UserCircle icon: removed
- Nav links: active state via `usePathname()`

### CheckoutForm (M-Pesa)
- Phone input: validates Kenyan number format before submission
- Pending state: shows "Check your phone — enter your M-Pesa PIN" copy

### DownloadButton
- Error state: shows error message + WhatsApp link if download fetch fails

---

## New Sections

### "See What You Can Do" (replaces testimonials)
- 4 prompt/response cards: Student, Professional, Developer, Business Owner
- Pure CSS, no images, no API calls, < 5KB
- Mobile: horizontal scroll carousel | Desktop: 2×2 grid

### "Choose Your Architecture" (after pricing)
- Headline + subhead framing on-device vs cloud as architecture choice
- 6-row comparison table (no privacy/speed rows)

### Business Owners (4th use case card)
- Added to existing 3-card use cases grid

---

## SEO Assets

### robots.txt
- Allows all major AI crawlers explicitly
- Single sitemap reference

### sitemap.xml
- 8 URLs with hreflang alternates
- Priority: `/` (1.0), `/buy` (0.9), `/install` (0.8), country routes (0.8)

### JSON-LD
- MobileApplication schema on homepage
- FAQPage schema on homepage
- No privacy claims in either schema

### GEO citation paragraph
- Plain prose paragraph (visible + machine-readable) placed as first body content
- Names: OnDevice AI Inc., Gemma, LiteRT, M-Pesa, MTN MoMo, supported phone brands

---

## Pricing Config

All price values must be consumed from `pricing.config.ts`. No component may hardcode a price string.

| Currency | Amount | Display |
|----------|--------|---------|
| USD | 0.99 | $0.99 |
| KES | 130 | KSh 130 |
| NGN | 1600 | ₦1,600 |
| ZAR | 18 | R18 |
| GHS | 15 | GH₵15 |
| TZS | 2700 | TSh 2,700 |
| GBP | 0.79 | £0.79 |
| CAD | 1.39 | CA$1.39 |
| EUR | 0.89 | €0.89 |
| UGX | 3700 | UGX 3,700 |

---

## Exclusions (Do Not Touch)

- `/download` page logic (token, JWT, DB) — only theme changes
- `/api/*` routes — no changes in this spec
- Use Cases section copy (Students, Professionals, Developers) — NO CHANGES
- Download/Model Sizes section — NO CHANGES
- Sticky mobile CTA button label — NO CHANGES
- Step 02 in How It Works — NO CHANGES
