// IntaSend provider — M-Pesa / KES
// STATUS: Awaiting +254 number resolution with IntaSend support
//
// To activate:
// 1. Get API keys from intasend.com
// 2. Add INTASEND_PUBLIC_KEY + INTASEND_SECRET_KEY to .env.local
// 3. Uncomment 'KES: intasendProvider' in router.ts

import type { PaymentProvider, PaymentParams, InitiateResult, PaymentStatus, WebhookEvent } from './types'

export const intasendProvider: PaymentProvider = {
  name: 'intasend',
  supportedCurrencies: ['KES'],

  async initiate(_params: PaymentParams): Promise<InitiateResult> {
    throw new Error('IntaSend not yet activated — awaiting account confirmation')
  },

  async verify(_providerRef: string): Promise<PaymentStatus> {
    throw new Error('IntaSend not yet activated')
  },

  async parseWebhook(_payload: unknown, _signature: string): Promise<WebhookEvent> {
    throw new Error('IntaSend not yet activated')
  },
}
