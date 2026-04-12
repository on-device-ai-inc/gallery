'use client'

import { useState, useEffect, useRef } from 'react'
import type { CURRENCIES } from '@/lib/currencies'

interface Props {
  currencies: typeof CURRENCIES
  rates: Record<string, number>
  defaultCurrency: string
  basePrice: number
}

function getPrice(currency: string, rates: Record<string, number>, basePrice: number): string {
  const rate = rates[currency] ?? 1
  const price = basePrice * rate
  if (rate > 500) return Math.round(price / 100) * 100 + ''
  if (rate > 50) return Math.round(price / 10) * 10 + ''
  return price.toFixed(2)
}

export default function CheckoutForm({ currencies, rates, defaultCurrency, basePrice }: Props) {
  const [currency, setCurrency] = useState(defaultCurrency)
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [promoCode, setPromoCode] = useState('')
  const [showPromo, setShowPromo] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [emailError, setEmailError] = useState(false)
  const [phoneError, setPhoneError] = useState(false)
  const [stkPending, setStkPending] = useState(false)
  const [orderId, setOrderId] = useState('')
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const isMpesa = currency === 'KES'

  function isValidKenyanPhone(p: string): boolean {
    const cleaned = p.replace(/[\s\-()]/g, '')
    return /^(07|01)\d{8}$/.test(cleaned) || /^\+2547\d{8}$/.test(cleaned) || /^\+2541\d{8}$/.test(cleaned)
  }

  // Poll for payment confirmation while STK is pending
  useEffect(() => {
    if (!stkPending || !orderId) return

    pollRef.current = setInterval(async () => {
      try {
        const res = await fetch(`/api/payments/status/${orderId}?email=${encodeURIComponent(email)}`)
        const data = await res.json()
        if (data.status === 'paid' && data.downloadUrl) {
          clearInterval(pollRef.current!)
          window.location.href = data.downloadUrl
        }
      } catch {
        // keep polling
      }
    }, 3000)

    return () => { if (pollRef.current) clearInterval(pollRef.current) }
  }, [stkPending, orderId])

  const selected = currencies.find(c => c.code === currency) ?? currencies[0]
  const price = getPrice(currency, rates, basePrice)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    let valid = true
    if (!email.trim()) { setEmailError(true); valid = false }
    if (isMpesa && (!phone.trim() || !isValidKenyanPhone(phone))) { setPhoneError(true); valid = false }
    if (!valid) return

    setLoading(true)

    try {
      if (promoCode.trim()) {
        const res = await fetch('/api/promo/redeem', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ code: promoCode.trim(), email }),
        })
        const data = await res.json()
        if (res.ok && data.downloadUrl) {
          window.location.href = data.downloadUrl
          return
        }
        setError(data.error ?? 'Invalid promo code.')
        setLoading(false)
        return
      }

      const res = await fetch('/api/payments/initiate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currency, email, phone: phone.trim() || undefined }),
      })

      const data = await res.json()

      if (!res.ok) {
        setError(data.error ?? 'Something went wrong. Please try again.')
        return
      }

      if (data.stkPending) {
        setOrderId(data.orderId)
        setStkPending(true)
        return
      }

      window.location.href = data.checkoutUrl
    } catch {
      setError('Network error. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  if (stkPending) {
    return (
      <div className="glass-card rounded-2xl p-8 text-center space-y-4">
        <div className="text-4xl">📱</div>
        <h2 className="text-xl font-bold text-slate-900">Check your phone</h2>
        <p className="text-slate-500 text-sm">
          A payment prompt has been sent to <span className="font-semibold text-slate-700">{phone}</span>.
          Enter your M-Pesa PIN to complete the purchase.
        </p>
        <div className="flex items-center justify-center gap-2 text-xs text-slate-400">
          <svg className="animate-spin h-3.5 w-3.5 text-cyan-500" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
          </svg>
          Waiting for confirmation — you&apos;ll be redirected automatically
        </div>
        <p className="text-slate-400 text-xs">
          Receipt also sent to <span className="font-medium">{email}</span>
        </p>
        <button
          type="button"
          onClick={() => { setStkPending(false); setLoading(false) }}
          className="text-xs text-slate-400 hover:text-slate-600 underline underline-offset-4"
        >
          Didn&apos;t receive it? Go back
        </button>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">

      {/* Currency selector */}
      <div>
        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-widest mb-2">
          Currency
        </label>
        <div className="relative">
          <select
            value={currency}
            onChange={e => setCurrency(e.target.value)}
            className="w-full glass-card rounded-xl px-4 py-3 text-slate-900 appearance-none focus:outline-none focus:ring-2 focus:ring-cyan-500/40 cursor-pointer text-sm"
          >
            {currencies.map(c => (
              <option key={c.code} value={c.code}>
                {c.flag} {c.name} ({c.code})
              </option>
            ))}
          </select>
          <span className="absolute right-4 top-3.5 text-slate-400 pointer-events-none text-xs">▾</span>
        </div>
      </div>

      {/* Price display */}
      <div className="glass-card rounded-2xl p-5 flex items-center justify-between">
        <div>
          <p className="font-bold text-slate-900">OnDevice AI</p>
          <p className="text-xs text-slate-500 mt-0.5">One-time · All models · All updates</p>
        </div>
        <div className="text-right">
          <p className="text-3xl font-black text-slate-900">
            {selected.symbol}{price}
          </p>
          <p className="text-xs text-slate-500 mt-0.5">{currency}</p>
        </div>
      </div>

      {/* Email */}
      <div>
        <label className="block text-xs font-semibold text-slate-500 uppercase tracking-widest mb-2">
          Email — download link sent here
        </label>
        <input
          type="email"
          value={email}
          onChange={e => { setEmail(e.target.value); setEmailError(false) }}
          placeholder="you@example.com"
          className={`w-full glass-card rounded-xl px-4 py-3 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition-all text-sm ${
            emailError
              ? 'ring-2 ring-red-400/60'
              : 'focus:ring-cyan-500/40'
          }`}
        />
        {emailError && (
          <p className="text-red-500 text-xs mt-1.5">Enter your email to continue</p>
        )}
      </div>

      {/* Phone — M-Pesa only */}
      {isMpesa && (
        <div>
          <label className="block text-xs font-semibold text-slate-500 uppercase tracking-widest mb-2">
            M-Pesa Phone Number
          </label>
          <input
            type="tel"
            value={phone}
            onChange={e => { setPhone(e.target.value); setPhoneError(false) }}
            placeholder="0712 345 678"
            className={`w-full glass-card rounded-xl px-4 py-3 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 transition-all text-sm ${
              phoneError
                ? 'ring-2 ring-red-400/60'
                : 'focus:ring-cyan-500/40'
            }`}
          />
          {phoneError && (
            <p className="text-red-500 text-xs mt-1.5">Enter a valid Kenyan number (e.g. 0712 345 678)</p>
          )}
        </div>
      )}

      {/* Promo code */}
      {showPromo ? (
        <div>
          <label className="block text-xs font-semibold text-slate-500 uppercase tracking-widest mb-2">
            Promo code
          </label>
          <input
            type="text"
            value={promoCode}
            onChange={e => setPromoCode(e.target.value.toUpperCase())}
            placeholder="ENTER CODE"
            className="w-full glass-card rounded-xl px-4 py-3 text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-cyan-500/40 uppercase tracking-widest font-mono text-sm"
          />
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setShowPromo(true)}
          className="text-xs text-slate-400 hover:text-slate-700 underline underline-offset-4 w-full text-center transition-colors"
        >
          Have a promo code?
        </button>
      )}

      {error && (
        <p className="text-red-600 text-sm bg-red-50 border border-red-200 rounded-xl px-4 py-3">
          {error}
        </p>
      )}

      {/* Submit */}
      <button
        type="submit"
        disabled={loading}
        className="tap-target w-full text-white font-bold py-4 rounded-xl transition-all text-base disabled:opacity-50 shadow-[0_0_24px_rgba(6,182,212,0.25)]"
        style={{ background: loading ? '#94a3b8' : 'linear-gradient(to right, #0891b2, #1d4ed8)' }}
      >
        {loading
          ? 'Please wait…'
          : promoCode.trim()
          ? 'Apply Code & Download'
          : `Pay ${selected.symbol}${price} ${currency}`}
      </button>

    </form>
  )
}
