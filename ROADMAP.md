# Gora AI Web Platform — Phased Roadmap

## Phase 0: Foundation (Day 1)
**Goal**: Repo scaffolded, ECC installed, deploys to DGX Spark

### Tasks
- [ ] `pnpm create next-app@latest gora-ai-web --typescript --tailwind --app --src-dir`
- [ ] Install shadcn/ui: `pnpm dlx shadcn@latest init`
- [ ] Copy CLAUDE.md to project root
- [ ] Run `./setup-ecc.sh` to install ECC skills, agents, commands
- [ ] Set up Docker Compose (PostgreSQL) on DGX Spark
- [ ] Install Caddy on DGX Spark: `sudo apt install caddy`
- [ ] Configure domain DNS → DGX Spark public IP
- [ ] First deploy: verify HTTPS works with empty Next.js app

### ECC Commands
```
/plan "Scaffold Next.js project with Tailwind, shadcn/ui, Drizzle ORM, and PostgreSQL"
/tdd   → set up Vitest config and first test
```

---

## Phase 1: Marketing Website (Week 1-2)
**Goal**: Landing page live, driving Play Store downloads

### Pages to Build
- [ ] Landing page — hero with app demo, value props, CTA
- [ ] Features page — detailed breakdown with screenshots
- [ ] Pricing page — Free vs Pro comparison
- [ ] About page — Gora AI story
- [ ] Contact page — form → Resend email

### ECC Commands
```
# For each page:
/plan "Build landing page with hero section showcasing OnDevice AI"
/tdd                              → component tests
/code-review                      → quality review before merge
/e2e                              → Playwright test for landing → pricing flow

# Design review:
/model opus                       → switch to Opus for design critique
"Review this landing page design for conversion optimization"
/model sonnet                     → back to Sonnet for implementation
```

### Key Decisions
- Static generation for all marketing pages (`generateStaticParams`)
- Dark mode first, toggle available
- next/image for all screenshots (WebP + AVIF)
- Schema.org structured data on landing page (SoftwareApplication)

---

## Phase 2: Content Engine (Week 2-3)
**Goal**: Blog live with 5+ SEO-optimized posts, content pipeline established

### Tasks
- [ ] Set up MDX processing with `next-mdx-remote` or `contentlayer`
- [ ] Blog index page with category filtering
- [ ] Article template with TOC, share buttons, related posts
- [ ] Open Graph image generation (dynamic with `@vercel/og` equivalent)
- [ ] RSS feed at `/feed.xml`
- [ ] Sitemap at `/sitemap.xml`

### Initial Blog Posts (use ECC content skills)
```
# Market research first, then write:
/plan "Research Android on-device AI competitive landscape — MLC-LLM, llamafile, Samsung Galaxy AI, Google AI Edge"
# → uses market-research skill, produces source-attributed analysis

/plan "Write blog post: Why On-Device AI is the Future of Mobile Privacy"
# → uses article-writing skill

/plan "Write tutorial: How to Run Gemma 3n on Your Android Phone with OnDevice AI"
# → technical tutorial with code snippets and screenshots

/plan "Write benchmark post: LLM Performance Comparison on Snapdragon 8 Gen 3"
# → data-driven, includes tables and charts

/plan "Write blog post: From Cloud to Device — The Privacy Case for Local AI"
# → thought leadership piece
```

### SEO Setup
- [ ] Metadata on every page (title, description, OG tags)
- [ ] robots.txt
- [ ] Google Search Console verification
- [ ] Schema.org markup (Article, SoftwareApplication, Organization)

---

## Phase 3: Growth & Capture (Week 3-4)
**Goal**: Email capture, waitlist, analytics tracking conversions

### Tasks
- [ ] Waitlist form with email validation → Drizzle → Resend welcome email
- [ ] PostHog self-hosted setup on DGX Spark
- [ ] Event tracking: page views, CTA clicks, Play Store clicks, waitlist signups
- [ ] Conversion funnel: landing → features → pricing → Play Store/waitlist
- [ ] Changelog page (auto-generated from GitHub releases API)

### ECC Commands
```
/plan "Build waitlist system with email capture, Resend welcome email, and referral tracking"
/tdd                              → test email validation, DB writes, Resend integration
/security-scan                    → check for injection, rate limiting on signup endpoint

/plan "Set up PostHog self-hosted with custom events for conversion funnel"
```

---

## Phase 4: Monetization (Week 4-6)
**Goal**: Stripe subscriptions live, users can upgrade to Pro

### Tasks
- [ ] Stripe integration: checkout session creation, webhook handling
- [ ] Pricing page with Stripe checkout buttons
- [ ] Customer portal for subscription management
- [ ] NextAuth.js setup (magic links + Google OAuth)
- [ ] User dashboard: subscription status, usage, model downloads
- [ ] Pro feature gating (middleware-based)

### ECC Commands
```
/plan "Integrate Stripe subscriptions with Next.js — checkout, webhooks, customer portal"
/tdd                              → test webhook signature verification, subscription state machine
/security-scan                    → Stripe security audit
/e2e                              → full checkout flow test with Stripe test mode

/plan "Build user dashboard with subscription management and usage stats"
/code-review                      → auth + payment code requires thorough review
```

### Stripe Architecture
```
Pricing page → Stripe Checkout → Webhook (checkout.session.completed) → DB update
Dashboard → Stripe Customer Portal (for manage/cancel)
Middleware → Check subscription status → Gate Pro features
```

---

## Phase 5: Scale & Campaign (Week 6+)
**Goal**: Content marketing machine, social campaigns, organic growth

### Tasks
- [ ] Social content templates (Twitter/X threads, LinkedIn posts)
- [ ] Programmatic SEO pages: "on-device AI for [use-case]" (100+ pages)
- [ ] Email drip campaign for waitlist (Loops or Resend sequences)
- [ ] Developer documentation at /docs
- [ ] API access for power users (future)
- [ ] Affiliate/referral system with tracked links

### ECC Commands
```
/plan "Create social media campaign for OnDevice AI Play Store launch"
# → uses content-engine skill

/plan "Build programmatic SEO pages for on-device AI use cases"
# → generates 20+ pages targeting long-tail keywords

/plan "Design email drip campaign: welcome → tutorial → features → upgrade"
# → uses content-engine skill for email copy
```

---

## Resource Allocation on DGX Spark

The DGX Spark runs both this website AND your AI models. Allocation:

| Service | Memory | CPU Cores | Notes |
|---------|--------|-----------|-------|
| Next.js (production) | ~512MB | 2 | Standalone Node.js server |
| PostgreSQL | ~1GB | 1 | Shared between web + PostHog |
| PostHog | ~2GB | 2 | Self-hosted analytics |
| Caddy | ~50MB | 0.5 | Reverse proxy |
| **Total web infra** | **~4GB** | **~5 cores** | |
| **Remaining for AI** | **~124GB** | **~15 cores** | Plus full GPU |

The web stack uses under 4% of the Spark's memory. Your AI inference workloads run completely unaffected.
