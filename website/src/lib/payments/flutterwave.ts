// Flutterwave provider — M-Pesa / KES / NGN / multi-Africa
// STATUS: Awaiting contract confirmation
// NOTE: CBK licensing situation — reviewed and accepted by Nashie
//
// To activate:
// 1. Get API keys from flutterwave.com
// 2. Add FLUTTERWAVE_SECRET_KEY + FLUTTERWAVE_PUBLIC_KEY to .env.local
// 3. Uncomment 'KES: flutterwaveProvider' in router.ts

import type { PaymentProvider, PaymentParams, InitiateResult, PaymentStatus, WebhookEvent } from './types'

export const flutterwaveProvider: PaymentProvider = {
  name: 'flutterwave',
  supportedCurrencies: ['KES', 'NGN', 'GHS', 'ZAR'],

  async initiate(_params: PaymentParams): Promise<InitiateResult> {
    throw new Error('Flutterwave not yet activated — awaiting contract')
  },

  async verify(_providerRef: string): Promise<PaymentStatus> {
    throw new Error('Flutterwave not yet activated')
  },

  async parseWebhook(_payload: unknown, _signature: string): Promise<WebhookEvent> {
    throw new Error('Flutterwave not yet activated')
  },
}
