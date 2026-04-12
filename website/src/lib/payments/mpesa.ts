import axios from 'axios'
import type { PaymentProvider, PaymentParams, InitiateResult, PaymentStatus, WebhookEvent } from './types'

const BASE = process.env.MPESA_ENV === 'live'
  ? 'https://api.safaricom.co.ke'
  : 'https://sandbox.safaricom.co.ke'

const client = axios.create({ baseURL: BASE, timeout: 15000 })
client.interceptors.response.use(
  r => r,
  err => {
    if (err?.response) {
      console.error(`[mpesa] ${err.config?.method?.toUpperCase()} ${err.config?.url} → ${err.response.status}`, JSON.stringify(err.response.data))
    }
    return Promise.reject(err)
  }
)

async function getAccessToken(): Promise<string> {
  const res = await client.get('/oauth/v1/generate', {
    params: { grant_type: 'client_credentials' },
    auth: {
      username: process.env.MPESA_CONSUMER_KEY!,
      password: process.env.MPESA_CONSUMER_SECRET!,
    },
  })
  const token = (res.data?.access_token ?? '').trim()
  if (!token) throw new Error('M-Pesa auth failed: no access_token in response')
  return token
}

function timestamp(): string {
  return new Date().toISOString().replace(/[-:T.Z]/g, '').slice(0, 14)
}

function password(ts: string): string {
  return Buffer.from(
    `${process.env.MPESA_SHORTCODE}${process.env.MPESA_PASSKEY}${ts}`
  ).toString('base64')
}

function normalizePhone(phone: string): string {
  return phone.replace(/^\+/, '').replace(/^0/, '254')
}

export const mpesaProvider: PaymentProvider = {
  name: 'mpesa',
  supportedCurrencies: ['KES'],

  async initiate(params: PaymentParams): Promise<InitiateResult> {
    if (!params.phone) throw new Error('Phone number is required for M-Pesa payments')

    const siteUrl = process.env.SITE_URL ?? ''
    if (!siteUrl.startsWith('https://')) {
      throw new Error('SITE_URL must use HTTPS for M-Pesa callback registration')
    }

    const token = await getAccessToken()
    const ts = timestamp()
    const shortcode = process.env.MPESA_SHORTCODE!
    const amount = Math.max(1, Math.round(params.amount / 100))
    const phone = normalizePhone(params.phone)

    const res = await client.post(
      '/mpesa/stkpush/v1/processrequest',
      {
        BusinessShortCode: shortcode,
        Password: password(ts),
        Timestamp: ts,
        TransactionType: 'CustomerPayBillOnline',
        Amount: amount,
        PartyA: phone,
        PartyB: shortcode,
        PhoneNumber: phone,
        CallBackURL: `${siteUrl}/api/webhooks/mpesa`,
        AccountReference: params.orderId,
        TransactionDesc: params.description,
      },
      { headers: { Authorization: `Bearer ${token}` } }
    )

    const data = res.data
    if (data.ResponseCode !== '0') {
      throw new Error(`STK Push failed: ${data.ResponseDescription ?? data.errorMessage}`)
    }

    return {
      providerRef: data.CheckoutRequestID,
      stkPending: true,
    }
  },

  async verify(providerRef: string): Promise<PaymentStatus> {
    const token = await getAccessToken()
    const ts = timestamp()
    const shortcode = process.env.MPESA_SHORTCODE!

    const res = await client.post(
      '/mpesa/stkpushquery/v1/query',
      {
        BusinessShortCode: shortcode,
        Password: password(ts),
        Timestamp: ts,
        CheckoutRequestID: providerRef,
      },
      { headers: { Authorization: `Bearer ${token}` } }
    )

    const { ResultCode } = res.data
    if (ResultCode === '0') return 'paid'
    if (ResultCode === '1032') return 'cancelled'
    return 'failed'
  },

  async parseWebhook(payload: unknown, _signature: string): Promise<WebhookEvent> {
    type MpesaCallback = {
      Body: {
        stkCallback: {
          CheckoutRequestID: string
          ResultCode: number
          CallbackMetadata?: { Item: Array<{ Name: string; Value?: string | number }> }
        }
      }
    }

    const body = payload as MpesaCallback
    const cb = body.Body.stkCallback
    const meta = cb.CallbackMetadata?.Item ?? []
    const get = (name: string) => meta.find(i => i.Name === name)?.Value

    const status: PaymentStatus =
      cb.ResultCode === 0 ? 'paid' :
      cb.ResultCode === 1032 ? 'cancelled' : 'failed'

    return {
      providerRef: cb.CheckoutRequestID,
      status,
      amount: Number(get('Amount') ?? 0) * 100,
      currency: 'KES',
      phone: String(get('PhoneNumber') ?? ''),
      raw: body,
    }
  },
}
