import Stripe from 'stripe'
import type { PaymentProvider, PaymentParams, InitiateResult, PaymentStatus, WebhookEvent } from './types'

// Lazy-init so `next build` page-data collection doesn't crash when
// STRIPE_SECRET_KEY is not present in the build environment.
let _stripe: Stripe | null = null
function stripeClient(): Stripe {
  if (_stripe) return _stripe
  const key = process.env.STRIPE_SECRET_KEY
  if (!key) throw new Error('STRIPE_SECRET_KEY is not set')
  _stripe = new Stripe(key, { apiVersion: '2026-02-25.clover' })
  return _stripe
}

export const stripeProvider: PaymentProvider = {
  name: 'stripe',
  supportedCurrencies: ['USD', 'CAD', 'GBP', 'EUR', 'KES', 'NGN'],

  async initiate(params: PaymentParams): Promise<InitiateResult> {
    const session = await stripeClient().checkout.sessions.create({
      mode: 'payment',
      line_items: [
        {
          price_data: {
            currency: params.currency.toLowerCase(),
            product_data: { name: params.description },
            unit_amount: params.amount,
          },
          quantity: 1,
        },
      ],
      customer_email: params.email,
      metadata: { orderId: params.orderId },
      success_url: `${process.env.SITE_URL}/download?session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `${process.env.SITE_URL}/buy?cancelled=true`,
    })

    return {
      providerRef: session.id,
      checkoutUrl: session.url!,
    }
  },

  async verify(providerRef: string): Promise<PaymentStatus> {
    const session = await stripeClient().checkout.sessions.retrieve(providerRef)
    switch (session.payment_status) {
      case 'paid': return 'paid'
      case 'unpaid': return 'pending'
      default: return 'failed'
    }
  },

  async parseWebhook(payload: unknown, signature: string): Promise<WebhookEvent> {
    if (typeof payload !== 'string' && !Buffer.isBuffer(payload)) {
      throw new Error('Stripe webhook payload must be raw string or Buffer')
    }
    const event = stripeClient().webhooks.constructEvent(
      payload,
      signature,
      process.env.STRIPE_WEBHOOK_SECRET!
    )

    if (event.type === 'checkout.session.completed') {
      const session = event.data.object as Stripe.Checkout.Session
      return {
        providerRef: session.id,
        status: session.payment_status === 'paid' ? 'paid' : 'pending',
        amount: session.amount_total ?? 0,
        currency: session.currency?.toUpperCase() ?? 'USD',
        email: session.customer_email ?? undefined,
        raw: event,
      }
    }

    return {
      providerRef: '',
      status: 'pending',
      amount: 0,
      currency: 'USD',
      raw: event,
    }
  },
}
