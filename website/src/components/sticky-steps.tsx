'use client'

import { useEffect, useRef, useState } from 'react'

const STEPS = [
  {
    n: '01',
    title: 'Buy Securely',
    body: 'Pay once — card or M-Pesa. Download link delivered to your email instantly.',
    accent: 'No subscription. No recurring charges. Lifetime ownership.',
    color: '#06b6d4',
    glow: 'rgba(6,182,212,',
  },
  {
    n: '02',
    title: 'Download Your Model',
    body: 'You get a personal download link immediately. Download the app and AI model — on Wi-Fi or mobile data, your choice.',
    accent: '584 MB to 4.9 GB. All model sizes included in your purchase.',
    color: '#3b82f6',
    glow: 'rgba(59,130,246,',
  },
  {
    n: '03',
    title: 'Chat Offline',
    body: 'Open the app. Ask anything. Your AI responds with zero network connection.',
    accent: 'No network. No server. Yours.',
    color: '#14b8a6',
    glow: 'rgba(20,184,166,',
  },
]

export function StickySteps() {
  const containerRef = useRef<HTMLDivElement>(null)
  const [stepIndex, setStepIndex] = useState(0)
  const [prefersReduced, setPrefersReduced] = useState(false)

  useEffect(() => {
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)')
    setPrefersReduced(mq.matches)
    const onChange = () => setPrefersReduced(mq.matches)
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [])

  useEffect(() => {
    if (prefersReduced) return
    const onScroll = () => {
      const el = containerRef.current
      if (!el) return
      const rect = el.getBoundingClientRect()
      const scrollable = el.offsetHeight - window.innerHeight
      if (scrollable <= 0) return
      const p = Math.max(0, Math.min(0.999, -rect.top / scrollable))
      setStepIndex(Math.min(STEPS.length - 1, Math.floor(p * STEPS.length)))
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    onScroll()
    return () => window.removeEventListener('scroll', onScroll)
  }, [prefersReduced])

  const step = STEPS[stepIndex]

  return (
    <>
      {/* ── MOBILE: simple stacked cards (no 300vh scroll) ── */}
      <div className="md:hidden px-6 pb-12 space-y-4">
        {STEPS.map((s) => (
          <div
            key={s.n}
            className="glass-card rounded-2xl p-6"
          >
            <div
              className="text-xs font-black uppercase tracking-[0.25em] mb-4 inline-block px-3 py-1.5 rounded-full border"
              style={{ color: s.color, borderColor: `${s.glow}0.3)`, background: `${s.glow}0.08)` }}
            >
              Step {s.n}
            </div>
            <h3 className="text-2xl font-black tracking-tight text-slate-900 mb-3">{s.title}</h3>
            <p className="text-slate-600 font-medium leading-relaxed mb-3">{s.body}</p>
            <p className="text-sm font-bold" style={{ color: s.color }}>{s.accent}</p>
          </div>
        ))}
      </div>

      {/* ── DESKTOP: scroll-driven sticky layout ── */}
      <div ref={containerRef} style={{ height: `${STEPS.length * 100}vh` }} className="hidden md:block">
        <div
          className="sticky top-16 overflow-hidden flex items-center"
          style={{ height: 'calc(100vh - 4rem)' }}
        >
          {/* Ghost number behind everything */}
          <div
            className="absolute inset-0 flex items-center justify-center pointer-events-none select-none"
            aria-hidden
          >
            <span
              className="font-black leading-none transition-all duration-700"
              style={{
                fontSize: 'clamp(180px, 35vw, 420px)',
                color: step.color,
                opacity: 0.06,
                letterSpacing: '-0.05em',
              }}
            >
              {step.n}
            </span>
          </div>

          {/* Background glow blob */}
          <div
            className="absolute inset-0 pointer-events-none transition-all duration-700"
            style={{
              background: `radial-gradient(ellipse 60% 60% at 50% 50%, ${step.glow}0.07) 0%, transparent 70%)`,
            }}
          />

          {/* Step content — cross-fades */}
          <div className="relative z-10 w-full max-w-screen-xl mx-auto px-6 lg:px-12">
            <div className="max-w-2xl mx-auto text-center">

              {/* Progress dots */}
              <div className="flex items-center justify-center gap-3 mb-8">
                {STEPS.map((_, i) => (
                  <div
                    key={i}
                    className="rounded-full transition-all duration-500"
                    style={{
                      height: '4px',
                      width: i === stepIndex ? '2.5rem' : '0.5rem',
                      background: i === stepIndex ? step.color : 'rgba(15,23,42,0.15)',
                    }}
                  />
                ))}
              </div>

              {STEPS.map((s, i) => (
                <div
                  key={i}
                  className="absolute inset-0 flex flex-col items-center justify-center px-6 transition-all duration-700"
                  style={{
                    opacity: i === stepIndex ? 1 : 0,
                    transform: i === stepIndex
                      ? 'translateY(0)'
                      : i < stepIndex
                        ? 'translateY(-48px)'
                        : 'translateY(48px)',
                    pointerEvents: i === stepIndex ? 'auto' : 'none',
                  }}
                >
                  <div
                    className="text-xs font-black uppercase tracking-[0.25em] mb-6 px-4 py-2 rounded-full border"
                    style={{
                      color: s.color,
                      borderColor: `${s.glow}0.3)`,
                      background: `${s.glow}0.08)`,
                    }}
                  >
                    Step {s.n}
                  </div>

                  <h3 className="text-4xl sm:text-6xl lg:text-7xl font-black tracking-tighter text-slate-900 mb-6 leading-[1.0]">
                    {s.title}
                  </h3>

                  <p className="text-lg sm:text-xl text-slate-600 mb-5 font-medium leading-relaxed max-w-xl">
                    {s.body}
                  </p>

                  <p className="text-base sm:text-lg font-bold" style={{ color: s.color }}>
                    {s.accent}
                  </p>
                </div>
              ))}

              {/* Invisible spacer to keep absolute children from collapsing */}
              <div style={{ height: '420px' }} />
            </div>
          </div>

          {/* Scroll hint — only on first step */}
          <div
            className="absolute bottom-8 left-1/2 -translate-x-1/2 transition-all duration-500 flex flex-col items-center gap-2"
            style={{ opacity: stepIndex === 0 ? 0.4 : 0 }}
          >
            <div className="w-px h-8 bg-gradient-to-b from-transparent to-slate-400 rounded-full animate-pulse" />
            <span className="text-xs text-slate-400 font-semibold uppercase tracking-widest">Scroll</span>
          </div>
        </div>
      </div>
    </>
  )
}
