import { headers } from 'next/headers'
import Image from 'next/image'
import Link from 'next/link'
import { getCountryFromRequest } from '@/lib/geo'
import {
  BASE_PRICE_USD,
  PROMO_END_DATE,
  PROMO_PRICE_USD,
  getCurrencyForCountry,
  getExchangeRates,
  sortCurrencies,
} from '@/lib/currencies'
import CheckoutForm from './checkout-form'
import { ArrowLeft, Check, Lock, RefreshCw, WifiOff, Zap } from 'lucide-react'

function getActivePrice(): number {
  return Date.now() < new Date(PROMO_END_DATE).getTime() ? PROMO_PRICE_USD : BASE_PRICE_USD
}

export async function generateMetadata() {
  const price = getActivePrice()
  return {
    title: `Get OnDevice AI — $${price.toFixed(2)}`,
    description: 'One-time payment. Full AI on your Android phone, forever.',
  }
}

export default async function BuyPage() {
  const headersList = await headers()
  const req = new Request('https://on-device.org/buy', { headers: headersList })

  const [country, rates] = await Promise.all([
    getCountryFromRequest(req),
    getExchangeRates(),
  ])

  const defaultCurrency = getCurrencyForCountry(country)
  const sortedCurrencies = sortCurrencies(defaultCurrency)

  return (
    <main className="min-h-screen bg-slate-50 text-slate-700 antialiased">
      {/* Nav */}
      <nav className="glass-nav sticky top-0 z-50">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-14 flex items-center">
          <Link
            href="/"
            className="flex items-center gap-2 text-slate-500 hover:text-slate-900 transition-colors text-sm font-semibold tap-target"
          >
            <ArrowLeft className="w-4 h-4" />
            Back
          </Link>
          <div className="flex items-center gap-2 font-extrabold text-lg tracking-tight text-slate-900 absolute left-1/2 -translate-x-1/2">
            <Image
              src="/images/app-icon.png"
              alt="OnDevice AI"
              width={28}
              height={28}
              className="rounded-lg"
            />
            OnDevice<span className="text-cyan-600">AI</span>
          </div>
        </div>
      </nav>

      <div className="px-4 py-12 max-w-md mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-4xl font-black tracking-tight mb-3 text-slate-900">Own Your AI.</h1>
          <p className="text-slate-500 font-medium">
            Pay once. Private, powerful AI on your phone — forever.
          </p>
        </div>

        {/* What you get */}
        <div className="glass-card rounded-2xl p-5 mb-6 space-y-3">
          {[
            { icon: <Zap className="w-4 h-4" />, text: 'Full AI model on your device — instant responses' },
            { icon: <WifiOff className="w-4 h-4" />, text: 'Works completely offline after one-time model download' },
            { icon: <RefreshCw className="w-4 h-4" />, text: 'All future updates included' },
            { icon: <Check className="w-4 h-4" />, text: 'Download link sent to your email instantly' },
          ].map(({ icon, text }) => (
            <div key={text} className="flex items-center gap-3 text-sm text-slate-600">
              <span className="text-cyan-600 shrink-0">{icon}</span>
              {text}
            </div>
          ))}
        </div>

        {/* Checkout form */}
        <CheckoutForm
          currencies={sortedCurrencies}
          rates={rates}
          defaultCurrency={defaultCurrency}
          basePrice={getActivePrice()}
        />

        {/* Trust */}
        <div className="mt-6 flex items-center justify-center gap-1.5 text-xs text-slate-400 font-medium">
          <Lock className="w-3 h-3" /> Secure checkout · Stripe & M-Pesa · No account required
        </div>
      </div>
    </main>
  )
}
