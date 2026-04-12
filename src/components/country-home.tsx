import Image from 'next/image'
import Link from 'next/link'
import {
  MessageCircle, WifiOff, CreditCard, GraduationCap,
  Briefcase, Code2, Star, HardDriveDownload, Check, Lock,
  ArrowRight, HelpCircle, ShieldCheck, Zap, Building2,
} from 'lucide-react'
import ParticleBackground from '@/components/particle-background'
import { ScrollReveal } from '@/components/scroll-reveal'
import { StickySteps } from '@/components/sticky-steps'
import { NavBar } from '@/components/nav-bar'
import { BASE_PRICE_USD, PROMO_PRICE_USD, PROMO_END_DATE, getExchangeRates, getDisplayPrices } from '@/lib/currencies'
import { PRICING, type Currency } from '@/lib/pricing.config'

const WHATSAPP_URL =
  'https://wa.me/16135550123?text=Hi%2C%20I%27d%20like%20to%20know%20more%20about%20OnDevice%20AI'

const mobileAppSchema = {
  '@context': 'https://schema.org',
  '@type': 'MobileApplication',
  name: 'OnDevice AI',
  operatingSystem: 'Android 9.0+',
  applicationCategory: 'UtilitiesApplication',
  description:
    'Powerful AI that runs entirely on your Android phone. No subscription, no network needed. Pay once, own it outright.',
  offers: {
    '@type': 'Offer',
    price: '3.99',
    priceCurrency: 'USD',
    availability: 'https://schema.org/InStock',
  },
  featureList: [
    'Runs entirely on-device — no network required after download',
    'One-time purchase, lifetime ownership',
    'Zero data cost per conversation',
    'Supports 140+ languages via Google Gemma models',
    'Works on Android phones with 3GB+ RAM',
    'Accepts M-Pesa, MTN MoMo, and card payments',
  ],
  downloadUrl: 'https://on-device.org/buy',
  softwareVersion: '1.0',
  fileSize: '584MB-4.9GB',
  author: {
    '@type': 'Organization',
    name: 'OnDevice AI Inc.',
    url: 'https://on-device.org',
    knowsAbout: ['on-device AI', 'mobile AI inference', 'Google LiteRT', 'Gemma language models'],
  },
}

export type CountryConfig = {
  /** ISO 4217 currency code to feature on this page */
  currency: Currency
  /** Body text for the Student use-case card — include exam names relevant to the country */
  studentBody: string
  /** Primary local payment method label, e.g. "M-Pesa" or "MTN MoMo" */
  paymentMethod: string
  /** Human-readable country name, e.g. "Kenya" */
  countryName: string
  /** Short country slug used in URLs, e.g. "ke" */
  countryCode: string
  /** Optional override for the mission banner text */
  missionText?: string
}

export async function CountryHomePage({ config }: { config: CountryConfig }) {
  const rates = await getExchangeRates()
  const prices = getDisplayPrices(rates)
  const local = PRICING[config.currency]

  // Compose price strings
  const localDisplay =
    config.currency === 'USD'
      ? `$${local.amount.toFixed(2)}`
      : local.display

  const heroSubPrice =
    config.currency === 'USD'
      ? `$${BASE_PRICE_USD.toFixed(2)} one-time`
      : `${local.display} one-time · $${BASE_PRICE_USD.toFixed(2)} USD`

  // For the big pricing number, pick appropriate font size
  const priceStr = String(local.amount)
  const priceFontClass =
    priceStr.length > 5
      ? 'text-[2.8rem]'
      : priceStr.length > 3
      ? 'text-[4rem]'
      : 'text-[5.5rem]'

  const missionText =
    config.missionText ??
    'AI should work for everyone. We built it to run on the phone in your pocket.'

  const paymentBadge =
    config.paymentMethod === 'Card'
      ? 'Secure · Stripe'
      : `Secure · ${config.paymentMethod} & Stripe`

  const faqSchema = {
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: [
      {
        '@type': 'Question',
        name: 'Does OnDevice AI work without internet?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'Yes. After a one-time model download, the AI runs entirely on your phone with zero network dependency.',
        },
      },
      {
        '@type': 'Question',
        name: 'How does OnDevice AI compare to ChatGPT and Claude?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'ChatGPT and Claude are cloud AI requiring internet and monthly subscriptions. OnDevice AI runs the model on your phone. You pay once and own it. Different architecture, both real AI.',
        },
      },
      {
        '@type': 'Question',
        name: 'What phones does OnDevice AI work on?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'Any Android 9.0+ phone with at least 3GB RAM, including Tecno Spark, Infinix Smart, Redmi, and Samsung Galaxy A-series.',
        },
      },
      {
        '@type': 'Question',
        name: `Can I pay with ${config.paymentMethod}?`,
        acceptedAnswer: {
          '@type': 'Answer',
          text:
            config.paymentMethod === 'M-Pesa'
              ? 'Yes. OnDevice AI accepts M-Pesa in Kenya and Tanzania, MTN MoMo in Ghana, and card payments globally.'
              : config.paymentMethod === 'MTN MoMo'
              ? 'Yes. OnDevice AI accepts MTN MoMo in Ghana, M-Pesa in Kenya and Tanzania, and card payments globally.'
              : 'OnDevice AI accepts M-Pesa, MTN MoMo, and card payments globally.',
        },
      },
    ],
  }

  return (
    <div className="text-slate-700 antialiased selection:bg-cyan-500 selection:text-white min-h-screen pb-24 lg:pb-0">

      {/* ── JSON-LD SCHEMAS ───────────────────────────────────── */}
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(mobileAppSchema) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }}
      />

      {/* ── PARTICLE CANVAS ──────────────────────────────────── */}
      <ParticleBackground />

      {/* ── 1. NAV ───────────────────────────────────────────── */}
      <NavBar />

      {/* ── 2. HERO — DESKTOP ────────────────────────────────── */}
      <section className="relative hidden lg:block">
        <Image
          src="/images/hero.png"
          alt="OnDevice AI"
          width={1376}
          height={614}
          className="w-full h-auto"
          priority
          sizes="100vw"
        />
        <div
          className="absolute inset-0 pointer-events-none"
          style={{ background: 'linear-gradient(to right, rgba(0,0,0,0.60) 0%, rgba(0,0,0,0.15) 55%, transparent 100%)' }}
        />
        <div className="absolute" style={{ top: '100px', left: '72px', maxWidth: '500px' }}>
          <h1
            className="text-white font-bold leading-[1.1] mb-5"
            style={{ fontSize: '56px', letterSpacing: '-0.02em' }}
          >
            AI That Runs<br />On Your Phone.
          </h1>
          <p className="text-white/80 font-normal leading-relaxed mb-2" style={{ fontSize: '18px' }}>
            Powerful AI on your Android.<br />Pay once, own it forever.
          </p>
          <p className="text-white/45 text-sm mb-10">{heroSubPrice}</p>
          <div className="flex items-center gap-4">
            <Link
              href="/buy"
              className="inline-flex items-center justify-center font-semibold text-black bg-white hover:bg-white/90 transition-colors px-8 py-3.5 text-sm"
              style={{ borderRadius: '999px' }}
            >
              Buy Now
            </Link>
            <a
              href="#how-it-works"
              className="inline-flex items-center gap-2 font-semibold text-white hover:bg-white/10 transition-colors px-8 py-3.5 text-sm border"
              style={{ borderRadius: '999px', background: 'rgba(0,0,0,0.45)', borderColor: 'rgba(255,255,255,0.4)' }}
            >
              See How It Works <ArrowRight className="w-3.5 h-3.5" />
            </a>
          </div>
        </div>
      </section>

      {/* ── 2. HERO — MOBILE ─────────────────────────────────── */}
      <section className="lg:hidden bg-black">
        <div className="relative">
          <Image
            src="/images/hero-mobile.png"
            alt="OnDevice AI"
            width={768}
            height={1238}
            className="w-full h-auto"
            priority
            sizes="100vw"
          />
          <div
            className="absolute inset-0 pointer-events-none"
            style={{ background: 'linear-gradient(to bottom, rgba(0,0,0,0.70) 0%, rgba(0,0,0,0.30) 35%, transparent 65%)' }}
          />
          <div className="absolute" style={{ top: '80px', left: '20px', right: '20px' }}>
            <h1
              className="text-white font-bold leading-[1.1] mb-3"
              style={{ fontSize: '38px', letterSpacing: '-0.02em' }}
            >
              AI That Runs<br />On Your Phone.
            </h1>
            <p className="text-white/75 font-normal mb-2" style={{ fontSize: '15px' }}>
              Powerful AI on your Android.<br />Pay once, own it forever.
            </p>
            <p className="text-white/40 text-xs">{heroSubPrice}</p>
          </div>
        </div>
        <div className="px-5 pt-6 pb-8 flex flex-col gap-3">
          <Link
            href="/buy"
            className="flex items-center justify-center font-semibold text-black bg-white text-sm py-3.5"
            style={{ borderRadius: '999px' }}
          >
            Buy Now
          </Link>
          <a
            href="#how-it-works"
            className="flex items-center justify-center gap-2 font-semibold text-white text-sm py-3.5 border"
            style={{ borderRadius: '999px', background: 'rgba(0,0,0,0.20)', borderColor: 'rgba(255,255,255,0.4)' }}
          >
            See How It Works <ArrowRight className="w-3.5 h-3.5" />
          </a>
        </div>
      </section>

      {/* ── GEO CITATION ─────────────────────────────────────── */}
      <div className="relative z-10 bg-slate-900 px-6 py-4 text-center">
        <p className="text-slate-400 text-xs sm:text-sm leading-relaxed max-w-4xl mx-auto">
          OnDevice AI is a powerful AI assistant built by OnDevice AI Inc. that runs entirely on your Android phone
          using Google&apos;s Gemma language models via the LiteRT inference framework. Unlike ChatGPT or Claude,
          which route every query through cloud servers, OnDevice AI performs all inference locally on your device.
          It works with or without an internet connection, costs {localDisplay} (one-time),
          and accepts {config.paymentMethod !== 'Card' ? `${config.paymentMethod} and ` : ''}card payments
          in {config.countryName}. It runs on phones with as little as 3GB RAM, including Tecno Spark, Infinix Smart,
          Redmi, and Samsung A-series devices.
        </p>
      </div>

      {/* ── SAMUEL PULLQUOTE ─────────────────────────────────── */}
      <div className="relative z-10 bg-black px-6 py-5 flex items-center justify-center gap-4 border-t border-white/10">
        <div className="flex text-yellow-400 shrink-0">
          {[...Array(5)].map((_, i) => <Star key={i} className="w-3.5 h-3.5 fill-current" />)}
        </div>
        <p className="text-white/80 text-sm font-medium italic leading-snug max-w-xl">
          &ldquo;Works on my Redmi even when Safaricom network is completely down.&rdquo;
        </p>
        <p className="text-white/40 text-xs shrink-0 hidden sm:block">Samuel M. · Nairobi 🇰🇪</p>
      </div>

      {/* ── MISSION BANNER ───────────────────────────────────── */}
      <div className="relative z-10 bg-cyan-50 border-b border-cyan-200 text-center px-4 py-2.5">
        <p className="text-xs sm:text-sm font-semibold text-cyan-700 tracking-wide">
          {missionText}
        </p>
      </div>

      {/* ── LAUNCH PRICING CALLOUT ─────────────────────────────── */}
      <section id="pricing" className="relative z-10 bg-black px-6 py-16 sm:py-20 text-center border-t border-white/10">
        <div className="w-full max-w-2xl mx-auto flex flex-col items-center gap-8">
          <div>
            <p className="text-xs font-bold uppercase tracking-widest text-cyan-400 mb-3">Now available</p>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white leading-tight mb-3">
              One price. Yours forever.
            </h2>
            <p className="text-white/60 font-medium text-base max-w-md mx-auto">
              One-time purchase. No subscription. Full AI on your phone, instant download.
            </p>
          </div>

          {/* Price display */}
          <div className="flex flex-col items-center gap-2">
            <div className="flex items-center gap-3">
              <span className="text-white/35 line-through text-xl font-semibold">${BASE_PRICE_USD.toFixed(2)}</span>
              <span className="text-4xl sm:text-5xl font-black text-white">${PROMO_PRICE_USD.toFixed(2)}</span>
              <span className="text-xs font-bold uppercase tracking-wide text-black bg-cyan-400 px-2.5 py-1 rounded-full">Save 20%</span>
            </div>
            <p className="text-white/40 text-xs">
              Launch price · One-time · Ends {new Date(PROMO_END_DATE).toLocaleDateString('en-US', { month: 'long', day: 'numeric' })}
            </p>
          </div>

          <Link
            href="/buy"
            className="inline-flex items-center justify-center font-bold text-black bg-white hover:bg-white/90 transition-colors px-10 py-4 text-base shadow-[0_0_24px_rgba(255,255,255,0.2)]"
            style={{ borderRadius: '999px' }}
          >
            Buy Now — ${PROMO_PRICE_USD.toFixed(2)}
          </Link>
          <p className="text-white/30 text-xs">
            Secure checkout · Stripe &amp; M-Pesa · Instant email delivery
          </p>
        </div>
      </section>

      {/* ── 3. TRUST STRIP ───────────────────────────────────── */}
      <section className="relative z-10 py-6 border-y border-slate-200 bg-white">
        <div className="w-full max-w-screen-xl mx-auto px-6 lg:px-12 flex flex-wrap justify-center items-center gap-x-5 gap-y-3 font-semibold text-sm sm:text-base">
          {[
            { icon: <WifiOff className="w-4 h-4 text-cyan-600" />, label: 'Zero network needed' },
            { icon: <CreditCard className="w-4 h-4 text-cyan-600" />, label: 'You own the AI' },
            { icon: <Zap className="w-4 h-4 text-cyan-600" />, label: 'Works without connection' },
          ].map(({ icon, label }, i) => (
            <ScrollReveal key={label} delay={i * 80}>
              <div className="glass-card px-5 py-2 rounded-full flex items-center gap-2 text-slate-700">
                {icon} {label}
              </div>
            </ScrollReveal>
          ))}
        </div>
      </section>

      {/* ── 4. USE CASES ─────────────────────────────────────── */}
      <section className="relative z-10 py-20 px-6 lg:px-12">
        <div className="w-full max-w-screen-xl mx-auto">
          <ScrollReveal className="text-center mb-14">
            <h2 className="text-3xl sm:text-5xl font-extrabold text-slate-900 mb-4 tracking-tight">
              One AI. Endless Uses.
            </h2>
            <p className="text-slate-500 font-medium text-lg max-w-2xl mx-auto">
              Handles everyday AI tasks — writing, summarizing, translating, brainstorming, coding assistance — on your phone, on your terms.
            </p>
          </ScrollReveal>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
            {[
              {
                icon: <GraduationCap className="w-7 h-7" />,
                color: 'rgba(6,182,212,', label: '#0891b2',
                title: 'For Students',
                body: config.studentBody,
              },
              {
                icon: <Briefcase className="w-7 h-7" />,
                color: 'rgba(59,130,246,', label: '#2563eb',
                title: 'For Professionals',
                body: '"Draft professional emails and reports instantly. Works perfectly whether you\'re in a boardroom, on a flight, or when the network drops."',
              },
              {
                icon: <Code2 className="w-7 h-7" />,
                color: 'rgba(20,184,166,', label: '#0d9488',
                title: 'For Developers',
                body: '"Debug code, generate functions, and learn new frameworks without burning data on slow browser tabs all day."',
              },
              {
                icon: <Building2 className="w-7 h-7" />,
                color: 'rgba(168,85,247,', label: '#9333ea',
                title: 'For Business Owners',
                body: '"Write product listings, reply to customers, and generate invoices. Runs on the phone you already have."',
              },
            ].map(({ icon, color, label, title, body }, i) => (
              <ScrollReveal key={title} delay={i * 120}>
                <div className="glass-card p-8 rounded-3xl hover:-translate-y-1 transition-transform duration-300 h-full">
                  <div
                    className="w-14 h-14 rounded-2xl flex items-center justify-center mb-6"
                    style={{ background: `${color}0.08)`, color: label, border: `1px solid ${color}0.2)` }}
                  >
                    {icon}
                  </div>
                  <h3 className="font-bold text-xl mb-3 text-slate-900">{title}</h3>
                  <p className="text-slate-500 font-medium leading-relaxed">{body}</p>
                </div>
              </ScrollReveal>
            ))}
          </div>

          <div className="rounded-3xl glass-card p-10 sm:p-14 flex flex-col items-start justify-end"
            style={{ background: 'linear-gradient(135deg, #0f172a 0%, #0e3a47 60%, #0f172a 100%)', minHeight: '200px' }}>
            <p className="text-white font-bold text-2xl sm:text-3xl max-w-md mb-2">Premium AI that belongs to you.</p>
            <p className="text-slate-300 font-medium text-lg">No subscription. No server. No wahala.</p>
          </div>
        </div>
      </section>

      {/* ── 5. SEE WHAT YOU CAN DO ───────────────────────────── */}
      <section className="relative z-10 py-20 px-6 lg:px-12 border-t border-slate-200 bg-slate-50">
        <div className="w-full max-w-screen-xl mx-auto">
          <ScrollReveal className="text-center mb-14">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 mb-4 tracking-tight">
              See What You Can Do
            </h2>
            <p className="text-slate-500 font-medium text-lg max-w-2xl mx-auto">
              Real prompts. Real responses. All running on your phone.
            </p>
          </ScrollReveal>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[
              {
                persona: '🎓 Student',
                prompt: 'Explain the process of photosynthesis for my WAEC Biology exam. Include the light and dark reactions.',
                response: 'Photosynthesis occurs in two stages. The light-dependent reactions take place in the thylakoid membranes, where water molecules are split using sunlight to produce ATP and NADPH. The light-independent reactions (Calvin cycle) occur in the stroma, where CO₂ is fixed into glucose using the ATP and NADPH from...',
              },
              {
                persona: '💼 Professional',
                prompt: "Draft a follow-up email to a client who hasn't responded to our proposal in 10 days. Keep it polite but create urgency.",
                response: "Subject: Following Up on Our Proposal — Happy to Discuss\n\nDear [Client Name],\n\nI hope this message finds you well. I wanted to follow up on the proposal we shared on [date]. We'd love to address any questions before...",
              },
              {
                persona: '💻 Developer',
                prompt: 'Why does this Python function return None instead of the filtered list?\n\ndef filter_even(nums):\n    result = [n for n in nums if n % 2 == 0]',
                response: 'Your function creates the filtered list correctly but never returns it. Add a return statement:\n\ndef filter_even(nums):\n    result = [n for n in nums if n % 2 == 0]\n    return result  # This was missing...',
              },
              {
                persona: '🛒 Business Owner',
                prompt: 'Write a short product description for handmade shea butter soap. 50g bar. Ingredients: shea butter, coconut oil, honey, lavender.',
                response: 'Handmade Shea Butter Soap (50g)\n\nCrafted with pure shea butter, virgin coconut oil, natural honey, and calming lavender. This 50g bar gently cleanses and moisturizes, leaving skin soft and naturally fragrant. Free from artificial additives...',
              },
            ].map(({ persona, prompt, response }, i) => (
              <ScrollReveal key={persona} delay={i * 100}>
                <div className="glass-card rounded-2xl p-6 h-full flex flex-col gap-4">
                  <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">{persona}</span>
                  <div className="bg-slate-100 rounded-xl px-4 py-3">
                    <p className="text-xs font-semibold text-slate-500 mb-1">Prompt</p>
                    <p className="text-sm text-slate-700 font-medium whitespace-pre-line">{prompt}</p>
                  </div>
                  <div className="flex-1 border border-slate-200 rounded-xl px-4 py-3 bg-white">
                    <p className="text-xs font-semibold text-cyan-600 mb-1">Response</p>
                    <p className="text-sm text-slate-600 leading-relaxed line-clamp-4 whitespace-pre-line">{response}</p>
                  </div>
                </div>
              </ScrollReveal>
            ))}
          </div>
        </div>
      </section>

      {/* ── 6. MODEL SIZES ───────────────────────────────────── */}
      <section className="relative z-10 py-20 px-6 lg:px-12">
        <div className="w-full max-w-screen-xl mx-auto">
          <ScrollReveal direction="scale">
            <div className="flex flex-col md:flex-row items-center gap-12 glass-card rounded-[2.5rem] p-8 sm:p-12">
              <div className="flex-1 text-center md:text-left">
                <div className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto md:mx-0 mb-6"
                  style={{ background: 'rgba(6,182,212,0.08)', border: '1px solid rgba(6,182,212,0.2)' }}>
                  <HardDriveDownload className="w-8 h-8 text-cyan-600" />
                </div>
                <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 mb-6">Download Once.<br />Use Forever.</h2>
                <p className="text-lg text-slate-600 mb-4 font-medium leading-relaxed">
                  Download the AI model once — on Wi-Fi or mobile data, your choice —
                  <strong className="text-cyan-600"> and every single chat after that uses 0MB.</strong>
                </p>
                <p className="text-base text-slate-500 font-medium leading-relaxed">
                  Choose based on your phone storage. Lite starts at <strong className="text-slate-900">584MB</strong>.
                  Recommended multimodal is <strong className="text-slate-900">3.7GB</strong>. All models included in your purchase.
                </p>
              </div>
              <div className="w-full md:w-[360px] rounded-3xl p-6 border border-slate-200 bg-slate-50 shrink-0">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-5">Available Models</p>
                <div className="space-y-4">
                  {[
                    { label: 'Lite',         size: '584 MB', bar: 'w-[12%]', note: 'Fast · Any phone · Works on 3GB RAM' },
                    { label: 'Standard',     size: '1.2 GB', bar: 'w-[24%]', note: 'Balanced · Tecno, Infinix, Redmi' },
                    { label: 'Multimodal ★', size: '3.7 GB', bar: 'w-[75%]', note: 'Recommended · Best performance' },
                    { label: 'Max',          size: '4.9 GB', bar: 'w-full',  note: 'Most powerful · Flagship phones' },
                  ].map(({ label, size, bar, note }) => (
                    <div key={label}>
                      <div className="flex justify-between text-sm mb-1.5">
                        <span className={`font-bold ${label.includes('★') ? 'text-cyan-600' : 'text-slate-700'}`}>{label}</span>
                        <span className="text-slate-400 font-mono">{size}</span>
                      </div>
                      <div className="h-1.5 w-full bg-slate-200 rounded-full overflow-hidden">
                        <div className={`h-full rounded-full ${label.includes('★') ? 'bg-gradient-to-r from-cyan-500 to-blue-500' : 'bg-slate-400'} ${bar}`} />
                      </div>
                      <p className="text-xs text-slate-400 mt-1">{note}</p>
                    </div>
                  ))}
                </div>
                <div className="mt-6 pt-5 border-t border-slate-200 text-center">
                  <p className="text-sm font-bold text-cyan-600">Zero data cost forever after.</p>
                </div>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* ── 7. PRICING ───────────────────────────────────────── */}
      <section id="pricing" className="relative z-10 py-24 px-6 lg:px-12 bg-slate-50 border-t border-slate-200">
        <div className="max-w-2xl mx-auto">
          <ScrollReveal direction="scale">
            <div
              className="glass-card rounded-[3rem] p-8 sm:p-12 relative overflow-hidden"
              style={{ borderColor: 'rgba(6,182,212,0.3)', boxShadow: '0 0 60px rgba(6,182,212,0.08)' }}
            >
              <div className="absolute top-0 right-0 text-white text-[10px] font-black px-6 py-2 rounded-bl-2xl uppercase tracking-[0.2em]"
                style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}>
                One-Time Purchase
              </div>

              <div className="flex justify-center mb-6">
                <Image src="/images/app-icon.png" alt="OnDevice AI" width={72} height={72} className="rounded-2xl shadow-[0_0_20px_rgba(6,182,212,0.3)]" />
              </div>

              <div className="text-center mb-10">
                <h2 className="text-4xl font-black text-slate-900 mb-2 tracking-tight">Own Your AI.</h2>
                <p className="text-slate-500 font-medium">Pay once. Keep it forever. No limits.</p>
              </div>

              {/* Local price as the hero price */}
              <div className="text-center mb-8">
                <div className="flex items-start justify-center text-slate-900">
                  {config.currency !== 'USD' && (
                    <span className="text-3xl font-bold mt-2 text-slate-400">{local.symbol}</span>
                  )}
                  {config.currency === 'USD' && (
                    <span className="text-3xl font-bold mt-2 text-slate-400">$</span>
                  )}
                  <span className={`${priceFontClass} leading-none font-black tracking-tighter`}>
                    {config.currency === 'USD'
                      ? BASE_PRICE_USD.toFixed(2)
                      : String(local.amount).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  </span>
                </div>
                {config.currency !== 'USD' && (
                  <p className="text-slate-400 text-sm mt-1">≈ ${BASE_PRICE_USD.toFixed(2)} USD</p>
                )}
                <div className="mt-4 inline-block px-5 py-2.5 rounded-xl border border-slate-200 bg-slate-50">
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-[0.15em] mb-1">Also Available In</p>
                  <p className="text-sm text-slate-700 font-semibold">
                    {prices.longLine.split(' · ').filter(p => !p.startsWith(local.symbol)).slice(0, 3).join(' · ')}
                  </p>
                </div>
                <p className="mt-3 text-xs text-slate-400">One-time. No recurring charges. Ever.</p>
              </div>

              <ul className="space-y-4 mb-10">
                {[
                  { text: 'Lifetime offline access', sub: 'Use it 10 years from now, still works' },
                  { text: '0 MB data per chat session', sub: 'Every conversation costs you nothing' },
                  { text: 'All model sizes included', sub: 'Pick the right size for your phone' },
                ].map(({ text, sub }) => (
                  <li key={text} className="flex items-start gap-4 p-4 rounded-2xl border border-slate-100 bg-slate-50/60">
                    <div className="p-1.5 rounded-full shrink-0 mt-0.5"
                      style={{ background: 'rgba(6,182,212,0.1)', color: '#0891b2', border: '1px solid rgba(6,182,212,0.2)' }}>
                      <Check className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-slate-900 font-semibold">{text}</div>
                      <div className="text-sm text-slate-500">{sub}</div>
                    </div>
                  </li>
                ))}
              </ul>

              <Link href="/buy"
                className="tap-target w-full text-white font-bold text-xl py-5 rounded-2xl transition-all flex justify-center items-center gap-3 shadow-[0_0_24px_rgba(6,182,212,0.25)]"
                style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}>
                Buy Now — ${PROMO_PRICE_USD.toFixed(2)} <ArrowRight className="w-5 h-5" />
              </Link>

              <div className="mt-6 flex flex-col items-center gap-2">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 uppercase tracking-widest">
                  <Lock className="w-3 h-3" /> {paymentBadge}
                </div>
                <div className="text-xs text-slate-400">
                  Questions? <a href={WHATSAPP_URL} target="_blank" rel="noopener noreferrer" className="text-green-600 hover:text-green-500">Ask on WhatsApp</a>
                </div>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* ── 8. CHOOSE YOUR ARCHITECTURE ──────────────────────── */}
      <section className="relative z-10 py-20 px-6 lg:px-12 border-t border-slate-200 bg-white">
        <div className="w-full max-w-screen-xl mx-auto">
          <ScrollReveal className="text-center mb-12">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 mb-4 tracking-tight">
              Choose Your Architecture
            </h2>
            <p className="text-slate-500 font-medium text-lg max-w-2xl mx-auto">
              Cloud AI routes your questions to a server. On-device AI runs the model on your phone.
              Both are real AI. One you rent. One you own.
            </p>
          </ScrollReveal>

          <ScrollReveal delay={100}>
            <div className="overflow-x-auto">
              <table className="w-full max-w-3xl mx-auto text-sm">
                <thead>
                  <tr className="border-b border-slate-200">
                    <th className="text-left py-3 px-4 text-slate-400 font-semibold uppercase tracking-widest text-xs w-1/3"></th>
                    <th className="py-3 px-4 text-center">
                      <span className="text-slate-900 font-bold text-base">OnDevice AI</span>
                      <div className="text-xs text-cyan-600 font-semibold mt-0.5">On your phone</div>
                    </th>
                    <th className="py-3 px-4 text-center">
                      <span className="text-slate-500 font-bold text-base">ChatGPT / Claude</span>
                      <div className="text-xs text-slate-400 font-semibold mt-0.5">Cloud servers</div>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {[
                    { label: 'Architecture', ours: 'Runs on your phone',       theirs: 'Routes to cloud server' },
                    { label: 'Internet',     ours: 'Not required after setup', theirs: 'Required every message' },
                    { label: 'Price',        ours: `${localDisplay} one-time`, theirs: '$20–$25/month' },
                    { label: 'Data cost',    ours: '0 MB per chat',            theirs: 'Uses data every session' },
                    { label: 'Rate limits',  ours: 'None — unlimited',         theirs: 'Monthly caps apply' },
                    { label: 'Annual cost',  ours: `${localDisplay} total`,    theirs: '$240–$300/year' },
                  ].map(({ label, ours, theirs }, i) => (
                    <tr key={label} className={`border-b border-slate-100 ${i % 2 === 0 ? 'bg-slate-50/50' : ''}`}>
                      <td className="py-3.5 px-4 text-slate-500 font-semibold text-xs uppercase tracking-wider">{label}</td>
                      <td className="py-3.5 px-4 text-center text-slate-900 font-medium">{ours}</td>
                      <td className="py-3.5 px-4 text-center text-slate-400">{theirs}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="text-center text-xs text-slate-400 mt-4 max-w-xl mx-auto">
              ChatGPT and Claude are excellent cloud AI. OnDevice AI is excellent on-device AI. Choose based on what matters to you.
            </p>
          </ScrollReveal>
        </div>
      </section>

      {/* ── 9. HOW IT WORKS + FAQ ────────────────────────────── */}
      <section id="how-it-works" className="relative z-10 border-t border-slate-200">
        <div className="bg-slate-50">
          <ScrollReveal className="text-center pt-20 pb-6 px-6">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
              Get started in 3 steps
            </h2>
          </ScrollReveal>
          <StickySteps />
        </div>

        <div className="px-6 lg:px-12 pb-20 bg-white border-t border-slate-200">
          <div className="w-full max-w-screen-xl mx-auto pt-16">
            <ScrollReveal>
              <div className="glass-card rounded-[2rem] p-8 sm:p-12">
                <h3 className="text-2xl font-bold text-slate-900 mb-8 flex items-center gap-3">
                  <HelpCircle className="w-6 h-6 text-cyan-600" /> Common Questions
                </h3>
                <div className="space-y-8">
                  {[
                    { q: 'Is this safe to install? I worry about scams.',
                      a: 'This is a legitimate product from OnDevice AI Inc., a registered company in Ottawa, Canada. Payment is processed by Stripe — used by millions of businesses globally. The APK does not request suspicious permissions and all AI runs locally on your phone.' },
                    { q: 'Is there a subscription or hidden charges?',
                      a: `None. You pay ${localDisplay} once. No monthly fees, no data charges, no in-app purchases. The AI is yours forever.` },
                    { q: 'How does this compare to ChatGPT or Claude?',
                      a: 'ChatGPT and Claude run on cloud servers — every message requires internet and a monthly subscription. OnDevice AI runs the AI model directly on your phone. You pay once, own it outright, and use it with or without a connection. Different architecture, different tradeoffs — both are real AI.' },
                    { q: 'How do I know it truly works without internet?',
                      a: 'After installing, turn on Airplane mode — cut all Wi-Fi and data. Open the app and ask anything. You\'ll get a response. Nothing is ever sent to a server.' },
                    { q: 'What Android version do I need?',
                      a: 'Android 9.0 or higher. Works on most phones made after 2018, including Tecno, Infinix, Redmi, and Samsung Galaxy A-series.' },
                    { q: 'What if I lose my phone or get a new one?',
                      a: 'Email your original receipt to support and we\'ll resend your download link. One purchase covers you across devices.' },
                    { q: 'Will it work on my phone?',
                      a: 'Requires Android 9.0+ and at least 3GB RAM. Works on Tecno Spark, Infinix Smart, Redmi, Samsung, and most modern Android phones.' },
                  ].map(({ q, a }) => (
                    <div key={q} className="border-b border-slate-100 pb-8 last:border-0 last:pb-0">
                      <h4 className="font-bold text-slate-900 text-lg mb-3">{q}</h4>
                      <p className="text-slate-500 font-medium leading-relaxed">{a}</p>
                    </div>
                  ))}
                </div>
              </div>
            </ScrollReveal>
          </div>
        </div>
      </section>

      {/* ── 10. FOOTER ───────────────────────────────────────── */}
      <footer className="relative z-10 bg-white border-t border-slate-200 py-14 px-6 lg:px-12 pb-32 lg:pb-14 mt-8 rounded-t-[2.5rem]">
        <div className="w-full max-w-screen-xl mx-auto text-center">
          <div className="flex items-center justify-center gap-2.5 font-extrabold text-2xl text-slate-900 mb-5">
            <Image src="/images/app-icon.png" alt="OnDevice AI" width={32} height={32} className="rounded-xl" />
            OnDevice<span className="text-cyan-600">AI</span>
          </div>
          <p className="mb-2 max-w-md mx-auto text-sm font-semibold text-cyan-600">
            AI that runs on your phone. Powerful, portable, yours.
          </p>
          <p className="mb-8 max-w-md mx-auto text-sm font-medium text-slate-500 leading-relaxed">
            By OnDevice AI Inc.
          </p>
          <div className="flex flex-wrap justify-center gap-6 text-sm font-semibold mb-8">
            <Link href="/buy" className="text-slate-500 hover:text-slate-900 transition-colors">Buy</Link>
            <Link href="/install" className="text-slate-500 hover:text-slate-900 transition-colors">Install Guide</Link>
            <Link href="/privacy" className="text-slate-500 hover:text-slate-900 transition-colors">Privacy Policy</Link>
            <Link href="/terms" className="text-slate-500 hover:text-slate-900 transition-colors">Terms of Service</Link>
            <a href={WHATSAPP_URL} target="_blank" rel="noopener noreferrer" className="text-slate-500 hover:text-green-600 transition-colors flex items-center gap-1.5">
              <MessageCircle className="w-4 h-4" /> WhatsApp Support
            </a>
          </div>
          <div className="flex flex-wrap justify-center gap-4 text-xs text-slate-400 font-medium mb-6">
            <span className="flex items-center gap-1.5"><ShieldCheck className="w-3 h-3" /> {paymentBadge} Payment</span>
            <span>·</span>
            <span>OnDevice AI Inc. · Ottawa, Canada</span>
            <span>·</span>
            <span>Android is a trademark of Google LLC</span>
          </div>
          <p className="text-xs text-slate-400">© 2026 OnDevice AI Inc. All rights reserved.</p>
        </div>
      </footer>

      {/* ── 11. STICKY MOBILE CTA ────────────────────────────── */}
      <div className="fixed bottom-0 left-0 w-full bg-white border-t border-slate-200 shadow-lg p-4 z-50 lg:hidden">
        <div className="flex items-center justify-between max-w-screen-xl mx-auto gap-4">
          <div className="hidden sm:flex items-center gap-2.5">
            <Image src="/images/app-icon.png" alt="" width={28} height={28} className="rounded-lg" />
            <div>
              <div className="font-extrabold text-slate-900 text-base leading-tight">OnDevice AI</div>
              <div className="text-xs font-medium text-slate-500 flex items-center gap-1">
                <WifiOff className="w-3 h-3 text-cyan-600" /> 0 MB to chat
              </div>
            </div>
          </div>
          <div className="flex-1 sm:flex-none w-full sm:w-auto">
            <Link href="/buy"
              className="tap-target w-full flex items-center justify-center text-white font-bold px-6 rounded-xl transition-all text-lg gap-2 shadow-[0_0_20px_rgba(6,182,212,0.3)]"
              style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}>
              Buy Now — ${PROMO_PRICE_USD.toFixed(2)}
            </Link>
          </div>
        </div>
      </div>

    </div>
  )
}
