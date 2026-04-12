// Single source of truth for all prices.
// Change a price here — it propagates everywhere automatically.
// Note: promo pricing ($3.99) is managed in currencies.ts via PROMO_PRICE_USD / PROMO_END_DATE.
// These are the REGULAR (post-promo) prices.

export const PRICING = {
  USD: { amount: 4.99,  symbol: '$',    display: '$4.99'        },
  KES: { amount: 650,   symbol: 'KSh',  display: 'KSh 650'     },
  NGN: { amount: 8000,  symbol: '₦',    display: '₦8,000'      },
  ZAR: { amount: 90,    symbol: 'R',    display: 'R90'          },
  GHS: { amount: 75,    symbol: 'GH₵',  display: 'GH₵75'       },
  TZS: { amount: 13500, symbol: 'TSh',  display: 'TSh 13,500'  },
  GBP: { amount: 3.99,  symbol: '£',    display: '£3.99'       },
  CAD: { amount: 6.99,  symbol: 'CA$',  display: 'CA$6.99'     },
  EUR: { amount: 4.49,  symbol: '€',    display: '€4.49'       },
  UGX: { amount: 18500, symbol: 'UGX',  display: 'UGX 18,500'  },
} as const

export type Currency = keyof typeof PRICING
export const DEFAULT_CURRENCY: Currency = 'USD'
