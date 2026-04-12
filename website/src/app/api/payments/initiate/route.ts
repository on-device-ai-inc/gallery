import { NextRequest, NextResponse } from 'next/server'
import { z } from 'zod'
import { getProvider } from '@/lib/payments/router'
import { getExchangeRates, BASE_PRICE_USD } from '@/lib/currencies'
import { db, orders } from '@/lib/db'
import { isValidOrigin } from '@/lib/origin-check'
import { checkRateLimit } from '@/lib/rate-limit'

function ip(req: NextRequest): string {
  return req.headers.get('cf-connecting-ip') ?? req.headers.get('x-real-ip') ?? 'unknown'
}

const InitiateSchema = z.object({
  email: z.string().email({ message: 'Valid email required' }),
  currency: z.string().min(3).max(3).toUpperCase(),
  phone: z.string().regex(/^\+?[0-9\s\-()]{7,20}$/).optional(),
})

export async function POST(req: NextRequest) {
  if (!isValidOrigin(req.headers.get('origin'))) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  }

  // 20 payment initiations per IP per hour
  if (!checkRateLimit(`pay:init:${ip(req)}`, 20, 60 * 60 * 1000)) {
    return NextResponse.json({ error: 'Too many requests' }, { status: 429 })
  }

  try {
    let body: unknown
    try {
      body = await req.json()
    } catch {
      return NextResponse.json({ error: 'Invalid JSON body' }, { status: 400 })
    }

    const parsed = InitiateSchema.safeParse(body)
    if (!parsed.success) {
      return NextResponse.json(
        { error: 'Validation failed', details: parsed.error.flatten().fieldErrors },
        { status: 400 }
      )
    }

    const { currency, email, phone } = parsed.data

    const rates = await getExchangeRates()
    const rate = rates[currency.toUpperCase()] ?? 1
    const amountFloat = BASE_PRICE_USD * rate

    // Convert to smallest unit (cents for most, no decimals for KES/NGN etc)
    const amountCents = Math.round(amountFloat * 100)
    const amountUsdCents = Math.round(BASE_PRICE_USD * 100)

    const orderId = crypto.randomUUID()
    const provider = getProvider(currency)

    const result = await provider.initiate({
      amount: amountCents,
      currency: currency.toUpperCase(),
      email,
      phone,
      orderId,
      description: 'OnDevice AI — Private AI for Android',
    })

    // Persist pending order
    await db.insert(orders).values({
      id: orderId,
      email,
      phone: phone ?? null,
      amountCents,
      currency: currency.toUpperCase(),
      amountUsdCents,
      provider: provider.name,
      providerRef: result.providerRef,
      status: 'pending',
    })

    return NextResponse.json({
      orderId,
      checkoutUrl: result.checkoutUrl,
      stkPending: result.stkPending ?? false,
    })
  } catch (err) {
    console.error('[payments/initiate]', err)
    return NextResponse.json({ error: 'Payment initiation failed' }, { status: 500 })
  }
}
