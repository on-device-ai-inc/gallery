# CLAUDE.md — OnDevice AI Web Platform

## Project Identity

**Product**: OnDevice AI — on-device AI inference for Android using LiteRT/MediaPipe
**Company**: On Device AI Inc. (Ottawa, Ontario)
**This repo**: Marketing website, content engine, market research hub, and monetization platform
**Brand voice**: Technical but accessible. Confident, not hype-driven. Speaks to developers who care about privacy and performance. African-rooted, globally ambitious.

## Tech Stack

- **Framework**: Next.js 14+ (App Router)
- **Styling**: Tailwind CSS 4 + shadcn/ui components
- **Database**: PostgreSQL (local container on DGX Spark)
- **ORM**: Drizzle ORM (type-safe, lightweight)
- **Auth**: NextAuth.js v5 (email magic links + Google OAuth)
- **Payments**: Stripe (subscriptions + one-time purchases)
- **Email**: Resend (transactional) + Loops (marketing automation)
- **Analytics**: PostHog (self-hosted on DGX Spark) or Plausible
- **Hosting**: Self-hosted on NVIDIA DGX Spark (ARM64/Grace CPU)
- **Reverse proxy**: Caddy (auto-HTTPS, simple config)
- **CI/CD**: GitHub Actions → SSH deploy to DGX Spark
- **Package manager**: pnpm

## Architecture

```
gora-ai-web/
├── src/
│   ├── app/                    # Next.js App Router
│   │   ├── (marketing)/        # Public marketing pages (landing, pricing, about)
│   │   ├── (blog)/             # Blog/content marketing pages
│   │   ├── (app)/              # Authenticated app pages (dashboard, downloads)
│   │   ├── api/                # API routes (webhooks, waitlist, contact)
│   │   └── layout.tsx          # Root layout with analytics
│   ├── components/
│   │   ├── ui/                 # shadcn/ui primitives
│   │   ├── marketing/          # Hero, features, testimonials, CTA sections
│   │   ├── blog/               # Article cards, TOC, share buttons
│   │   └── shared/             # Header, footer, navigation
│   ├── lib/
│   │   ├── db/                 # Drizzle schema + migrations
│   │   ├── stripe/             # Stripe helpers (checkout, webhooks, portal)
│   │   ├── email/              # Resend templates
│   │   ├── analytics/          # PostHog client wrapper
│   │   └── content/            # MDX processing for blog posts
│   ├── content/
│   │   ├── blog/               # MDX blog posts
│   │   ├── research/           # Market research MDX documents
│   │   └── campaigns/          # Campaign content and assets
│   └── styles/
│       └── globals.css         # Tailwind base + custom properties
├── public/
│   ├── images/                 # Static images, screenshots, app store badges
│   └── og/                     # Open Graph images for social sharing
├── drizzle/                    # Database migrations
├── scripts/                    # Deployment, seeding, content generation
├── Caddyfile                   # Reverse proxy config
├── docker-compose.yml          # PostgreSQL + PostHog containers
├── Dockerfile                  # Multi-stage Next.js build (ARM64)
└── CLAUDE.md                   # This file
```

## Brand & Design System

### Colors
```
Primary:        #0A0A0A (near-black)
Accent:         #22C55E (green — on-device, local, alive)
Accent Alt:     #3B82F6 (blue — trust, technology)
Surface:        #FAFAFA (off-white)
Surface Dark:   #111111 (dark mode)
Text Primary:   #0A0A0A / #FAFAFA (light/dark)
Text Secondary: #6B7280
```

### Typography
- Headlines: "General Sans" or "Satoshi" (variable, distinctive but readable)
- Body: "Inter" at 16px/1.6 (proven for long-form readability)
- Code: "JetBrains Mono"

### Design Principles
- Dark mode first (matches developer audience)
- Generous whitespace — let the product breathe
- App screenshots and demo videos are the hero, not stock imagery
- Motion: subtle scroll-triggered reveals, no gratuitous animation
- Mobile-first responsive (Android app → mobile users visit first)

## Pages & Features

### Phase 1: Marketing Foundation
- **Landing page** (`/`): Hero with app demo video/screenshots, value props (privacy, speed, offline), social proof, CTA to Play Store
- **Features** (`/features`): Detailed feature breakdown with interactive demos
- **Pricing** (`/pricing`): Free tier vs Pro tier comparison, Stripe checkout
- **About: OnDevice AI story, Ottawa roots, mission
- **Blog** (`/blog`): MDX-powered, SEO-optimized, categories for tutorials/news/research
- **Contact** (`/contact`): Simple form → Resend

### Phase 2: Growth Engine
- **Waitlist** (`/waitlist`): Email capture for new features, with referral tracking
- **Download hub** (`/download`): Direct APK download + Play Store link + usage guide
- **Changelog** (`/changelog`): Public product updates, auto-generated from releases
- **Docs** (`/docs`): Developer documentation for advanced users
- **SEO content**: Programmatic pages for "on-device AI for [use-case]" long-tail keywords

### Phase 3: Monetization
- **Pro subscriptions**: Stripe recurring billing for premium models/features
- **User dashboard** (`/dashboard`): Subscription management, usage stats, model downloads
- **Affiliate/referral**: Tracked referral links with reward tiers
- **API access**: Rate-limited API for power users (future)

## Content Strategy

### Blog Categories
1. **Tutorials**: "How to run Gemma 3n on your phone", "Private AI assistant setup guide"
2. **Benchmarks**: Model performance comparisons on real Android devices
3. **Privacy**: Why on-device matters, data sovereignty, enterprise use cases
4. **Industry**: Android AI ecosystem news, model releases, hardware updates

### SEO Targets
- "on-device AI Android"
- "private AI assistant app"
- "run LLM on phone"
- "offline AI Android"
- "LiteRT model deployment"
- "MediaPipe LLM inference"

### Market Research Topics
- Android on-device AI inference market size and growth
- Competitor analysis: MLC-LLM, llamafile, Ollama mobile, Samsung Galaxy AI
- Enterprise privacy requirements driving on-device adoption
- Model optimization trends (quantization, distillation, MoE for mobile)

## Development Rules

### Code Style
- TypeScript strict mode everywhere
- Prefer server components; use `"use client"` only when necessary
- Collocate components with their routes
- Use Drizzle for all database operations (no raw SQL)
- Environment variables: `.env.local` for secrets, validated with `zod`
- All API routes return typed responses with proper error handling

### Git Workflow
- Conventional commits: `feat:`, `fix:`, `docs:`, `chore:`, `content:`
- Feature branches from `main`
- Squash merge PRs
- Tag releases with semver

### Testing
- Vitest for unit/integration tests
- Playwright for E2E (critical user flows: landing → pricing → checkout)
- 80%+ coverage target on `/lib` and `/api`
- Test Stripe webhooks with Stripe CLI in dev

### Performance
- Core Web Vitals: LCP < 2.5s, CLS < 0.1, INP < 200ms
- Images: next/image with WebP/AVIF, lazy loading below fold
- Fonts: `next/font` with swap display
- Static generation for marketing pages, ISR for blog posts (60s revalidation)
- API routes: edge runtime where possible

### Security
- CSP headers via Caddy
- Rate limiting on API routes (upstash/ratelimit or custom middleware)
- Stripe webhook signature verification
- Input sanitization on all forms
- No secrets in client bundles — server actions or API routes only

## Deployment (DGX Spark Self-Hosting)

### Infrastructure
```yaml
# docker-compose.yml services
services:
  web:
    build: .
    ports: ["3000:3000"]
    environment:
      - DATABASE_URL=postgresql://ondevice:${DB_PASSWORD}@db:5432/ondevice_web
    depends_on: [db]

  db:
    image: postgres:16
    volumes: ["pgdata:/var/lib/postgresql/data"]
    environment:
      - POSTGRES_DB=ondevice_web
      - POSTGRES_USER=ondevice

  # Optional: self-hosted analytics
  posthog:
    image: posthog/posthog:latest
    ports: ["8000:8000"]
```

### Caddy Config
```
on-device.app {
    reverse_proxy localhost:3000
    encode gzip
    header {
        X-Content-Type-Options nosniff
        X-Frame-Options DENY
        Referrer-Policy strict-origin-when-cross-origin
    }
}
```

### Deploy Process
1. Push to `main` → GitHub Actions builds Docker image (ARM64)
2. SSH to DGX Spark → pull image → `docker compose up -d`
3. Caddy handles HTTPS via Let's Encrypt
4. Drizzle migrations run on container start

## ECC Skills to Activate

The following Everything Claude Code skills are directly relevant:

| Skill | Use For |
|-------|---------|
| `frontend-patterns` | React/Next.js component patterns, App Router conventions |
| `search-first` | Research before coding — always check current best practices |
| `market-research` | Competitor analysis, market sizing, source-attributed research |
| `content-engine` | Blog posts, social content, campaign material |
| `article-writing` | Long-form blog posts in brand voice |
| `tdd-workflow` | Test-driven development for all `/lib` and `/api` code |
| `security-review` | OWASP checks, Stripe security, auth review |
| `e2e-testing` | Playwright tests for critical user flows |
| `backend-patterns` | API design, database patterns, caching |
| `api-design` | REST API design for future developer API |
| `deployment-patterns` | Docker, CI/CD, health checks |
| `docker-patterns` | Docker Compose, networking, ARM64 builds |
| `coding-standards` | Universal code quality standards |
| `continuous-learning-v2` | Learn patterns from sessions, build instincts |
| `strategic-compact` | Context management in long sessions |

## ECC Commands Workflow

### Daily Development
```
/plan "Build pricing page with Stripe integration"    → planner creates blueprint
/tdd                                                   → test-first implementation
/code-review                                           → quality + security review
/build-fix                                             → if build breaks
```

### Content & Research
```
/plan "Research Android on-device AI competitive landscape"  → uses market-research skill
/plan "Write blog post: Why On-Device AI Matters for Privacy" → uses article-writing skill
/plan "Create social campaign for Play Store launch"          → uses content-engine skill
```

### Pre-Launch
```
/e2e                    → Playwright tests for landing → pricing → checkout flow
/security-scan          → OWASP audit + Stripe webhook security
/test-coverage          → verify 80%+ on critical paths
```

## Key Decisions Log

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Framework | Next.js 14 App Router | SSG for marketing, SSR for dashboard, API routes for webhooks — one framework |
| Database | PostgreSQL | Reliable, self-hosted, excellent Drizzle support, scales to enterprise |
| ORM | Drizzle | Type-safe, lightweight, better DX than Prisma for this scale |
| Hosting | DGX Spark self-hosted | Zero recurring hosting cost, full control, ARM64 native |
| Payments | Stripe | Industry standard, excellent webhook system, handles subscriptions |
| Styling | Tailwind + shadcn/ui | Fast iteration, accessible components, dark mode built-in |
| Content | MDX | Blog posts as code, version controlled, supports interactive components |
| Analytics | PostHog self-hosted | Privacy-aligned (on-device AI brand → self-hosted analytics), full-featured |

## Context for Claude Code

When working in this repo, remember:
- This is a **marketing and monetization platform** for an Android app, not the app itself
- The Android app (OnDevice AI) is a separate repo — this site drives downloads and subscriptions
- Target audience is **Android developers and privacy-conscious users**
- The DGX Spark runs both this website AND local AI models — don't over-provision
- Brand tone: technical credibility, not marketing fluff. Show benchmarks, not buzzwords.
- OnDevice AI is early-stage — prioritize speed of shipping over architectural perfection
- The founder (Nashie) is technical — don't over-abstract or hide complexity
