export type ProviderName = 'stripe' | 'intasend' | 'flutterwave' | 'mpesa'

export interface PaymentParams {
  amount: number        // in smallest unit (cents for USD, cents for KES etc)
  currency: string      // ISO 4217 e.g. 'USD', 'KES'
  email: string
  phone?: string        // required for M-Pesa providers
  orderId: string
  description: string
}

export interface InitiateResult {
  providerRef: string           // provider's transaction reference
  clientSecret?: string         // Stripe: for client-side confirmation
  checkoutUrl?: string          // Stripe: redirect URL
  stkPending?: boolean          // M-Pesa: waiting for PIN on handset
}

export type PaymentStatus = 'pending' | 'paid' | 'failed' | 'cancelled'

export interface WebhookEvent {
  providerRef: string
  status: PaymentStatus
  amount: number
  currency: string
  email?: string
  phone?: string
  raw: unknown
}

export interface PaymentProvider {
  name: ProviderName
  supportedCurrencies: string[]
  initiate(params: PaymentParams): Promise<InitiateResult>
  verify(providerRef: string): Promise<PaymentStatus>
  parseWebhook(payload: unknown, signature: string): Promise<WebhookEvent>
}
