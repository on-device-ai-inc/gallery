import type { PaymentProvider } from './types'
import { stripeProvider } from './stripe'
import { mpesaProvider } from './mpesa'
// Uncomment when contracts confirmed:
// import { intasendProvider } from './intasend'
// import { flutterwaveProvider } from './flutterwave'

// Map currency codes to providers.
const CURRENCY_ROUTES: Record<string, PaymentProvider> = {
  KES: mpesaProvider,           // Daraja STK Push (Safaricom Kenya)
  // NGN: flutterwaveProvider,  // Nigeria — Phase 2
}

export function getProvider(currency: string): PaymentProvider {
  return CURRENCY_ROUTES[currency.toUpperCase()] ?? stripeProvider
}

export function getSupportedCurrencies(): string[] {
  // All currencies we show in the UI — routed to whichever provider handles them
  return ['USD', 'KES', 'CAD', 'GBP', 'EUR', 'NGN']
}
