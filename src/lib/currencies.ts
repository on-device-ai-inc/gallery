export const CURRENCIES = [
  { code: 'USD', symbol: '$', name: 'US Dollar', flag: '🇺🇸' },
  { code: 'KES', symbol: 'KSh', name: 'Kenyan Shilling', flag: '🇰🇪' },
  { code: 'CAD', symbol: 'CA$', name: 'Canadian Dollar', flag: '🇨🇦' },
  { code: 'GBP', symbol: '£', name: 'British Pound', flag: '🇬🇧' },
  { code: 'EUR', symbol: '€', name: 'Euro', flag: '🇪🇺' },
  { code: 'NGN', symbol: '₦', name: 'Nigerian Naira', flag: '🇳🇬' },
  { code: 'UGX', symbol: 'USh', name: 'Ugandan Shilling', flag: '🇺🇬' },
  { code: 'ZAR', symbol: 'R', name: 'South African Rand', flag: '🇿🇦' },
  { code: 'GHS', symbol: '₵', name: 'Ghanaian Cedi', flag: '🇬🇭' },
  { code: 'TZS', symbol: 'TSh', name: 'Tanzanian Shilling', flag: '🇹🇿' },
]

// Country code → preferred currency
export const COUNTRY_CURRENCY: Record<string, string> = {
  KE: 'KES', NG: 'NGN', UG: 'UGX', ZA: 'ZAR', GH: 'GHS', TZ: 'TZS',
  CA: 'CAD', GB: 'GBP',
  DE: 'EUR', FR: 'EUR', IT: 'EUR', ES: 'EUR', NL: 'EUR', BE: 'EUR',
  PT: 'EUR', AT: 'EUR', IE: 'EUR', FI: 'EUR', GR: 'EUR',
}

export const BASE_PRICE_USD = 4.99       // regular price
export const PROMO_PRICE_USD = 3.99      // launch promo, ends April 16 2026
export const PROMO_END_DATE = '2026-04-16T10:00:00Z'

// Fetch live rates, cached for 6 hours by Next.js
export async function getExchangeRates(): Promise<Record<string, number>> {
  try {
    const res = await fetch('https://open.er-api.com/v6/latest/USD', {
      next: { revalidate: 21600 }, // 6 hours
    })
    const data = await res.json()
    return data.rates ?? {}
  } catch {
    // Fallback approximate rates if API is down
    return {
      USD: 1, KES: 130, CAD: 1.36, GBP: 0.79, EUR: 0.92,
      NGN: 1600, UGX: 3700, ZAR: 18.5, GHS: 15, TZS: 2600,
    }
  }
}

export function convertPrice(rateToUSD: number): string {
  const price = BASE_PRICE_USD * rateToUSD
  // Round to sensible local amounts
  if (rateToUSD > 500) return Math.round(price / 100) * 100 + ''   // KES, UGX, TZS
  if (rateToUSD > 50) return Math.round(price / 10) * 10 + ''      // NGN
  return price.toFixed(2)
}

export interface DisplayPrices {
  usd: string       // "$0.99"
  kes: string       // "130"
  ngn: string       // "1,600"
  zar: string       // "18.32"
  ghs: string       // "14.85"
  shortLine: string // "KSh 130 · $0.99 · ₦1,600 · R18"
  longLine: string  // "₦1,600 · KSh 130 · R18 · GH₵15"
  faqLine: string   // "$0.99 (₦1,600 / KSh 130 / R18 / GH₵15)"
}

export function getDisplayPrices(rates: Record<string, number>): DisplayPrices {
  const kes = convertPrice(rates.KES ?? 130)
  const ngn = convertPrice(rates.NGN ?? 1600)
  const zar = convertPrice(rates.ZAR ?? 18.5)
  const ghs = convertPrice(rates.GHS ?? 15)
  const usd = BASE_PRICE_USD.toFixed(2)
  const ngnFmt = Number(ngn).toLocaleString()
  return {
    usd,
    kes,
    ngn,
    zar,
    ghs,
    shortLine: `KSh ${kes} · $${usd} · ₦${ngnFmt} · R${zar}`,
    longLine:  `₦${ngnFmt} · KSh ${kes} · R${zar} · GH₵${ghs}`,
    faqLine:   `$${usd} (₦${ngnFmt} / KSh ${kes} / R${zar} / GH₵${ghs})`,
  }
}

export function getCurrencyForCountry(countryCode: string): string {
  return COUNTRY_CURRENCY[countryCode] ?? 'USD'
}

// Sort currencies: user's local first, then rest alphabetically
export function sortCurrencies(userCurrency: string) {
  const local = CURRENCIES.find(c => c.code === userCurrency)
  const rest = CURRENCIES.filter(c => c.code !== userCurrency)
    .sort((a, b) => a.name.localeCompare(b.name))
  return local ? [local, ...rest] : rest
}
