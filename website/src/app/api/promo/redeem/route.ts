import { NextRequest, NextResponse } from 'next/server'
import { z } from 'zod'
import { db, orders, downloadTokens, promoRedemptions } from '@/lib/db'
import { and, eq } from 'drizzle-orm'
import { SignJWT } from 'jose'
import { createHash } from 'crypto'
import { sendReceiptEmail, sendAdminSaleNotification } from '@/lib/email'
import { checkRateLimit } from '@/lib/rate-limit'
import { isValidOrigin } from '@/lib/origin-check'

const RedeemSchema = z.object({
  code: z.string().min(1).max(64),
  email: z.string().email(),
})

function ip(req: NextRequest): string {
  return (
    req.headers.get('cf-connecting-ip') ??
    req.headers.get('x-real-ip') ??
    'unknown'
  )
}

export async function POST(req: NextRequest) {
  if (!isValidOrigin(req.headers.get('origin'))) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  }

  // 10 attempts per IP per hour — limits brute-force code enumeration
  if (!checkRateLimit(`promo:${ip(req)}`, 10, 60 * 60 * 1000)) {
    return NextResponse.json({ error: 'Too many attempts. Please try again later.' }, { status: 429 })
  }

  try {
    let body: unknown
    try {
      body = await req.json()
    } catch {
      return NextResponse.json({ error: 'Invalid JSON body' }, { status: 400 })
    }

    const parsed = RedeemSchema.safeParse(body)
    if (!parsed.success) {
      return NextResponse.json(
        { error: 'Validation failed', details: parsed.error.flatten().fieldErrors },
        { status: 400 }
      )
    }

    const { code, email } = parsed.data

    const validCodes = (process.env.PROMO_CODES ?? '').split(',').map(c => c.trim().toUpperCase()).filter(Boolean)
    const normalizedCode = code.toUpperCase()

    if (!validCodes.includes(normalizedCode)) {
      return NextResponse.json({ error: 'Invalid or expired promo code.' }, { status: 400 })
    }

    // Check if this email has already redeemed this code
    const [alreadyRedeemed] = await db
      .select()
      .from(promoRedemptions)
      .where(and(eq(promoRedemptions.code, normalizedCode), eq(promoRedemptions.email, email.toLowerCase())))

    if (alreadyRedeemed) {
      return NextResponse.json({ error: 'This promo code has already been used with this email.' }, { status: 409 })
    }

    const orderId = crypto.randomUUID()

    await db.insert(orders).values({
      id: orderId,
      email,
      phone: null,
      amountCents: 0,
      currency: 'USD',
      amountUsdCents: 0,
      provider: 'promo',
      providerRef: `promo:${normalizedCode}:${orderId}`,
      status: 'paid',
      paidAt: new Date(),
    })

    // Record redemption — UNIQUE(code, email) prevents concurrent double-redeem
    await db.insert(promoRedemptions).values({
      id: crypto.randomUUID(),
      code: normalizedCode,
      email: email.toLowerCase(),
      orderId,
    })

    const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
    const token = await new SignJWT({ orderId, type: 'download' })
      .setProtectedHeader({ alg: 'HS256' })
      .setExpirationTime('24h')
      .setIssuedAt()
      .sign(secret)

    const tokenHash = createHash('sha256').update(token).digest('hex')
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

    await db.insert(downloadTokens).values({
      id: crypto.randomUUID(),
      orderId,
      tokenHash,
      expiresAt,
    })

    const downloadUrl = `${process.env.SITE_URL}/download?token=${token}`

    try {
      await sendReceiptEmail({ to: email, downloadUrl, orderId })
      await sendAdminSaleNotification({
        customerEmail: email,
        orderId,
        amount: 0,
        currency: 'USD',
        provider: 'promo',
      })
    } catch (emailErr) {
      console.error('[promo/redeem] email failed:', emailErr)
    }

    return NextResponse.json({ downloadUrl })
  } catch (err) {
    console.error('[promo/redeem]', err)
    return NextResponse.json({ error: 'Something went wrong. Please try again.' }, { status: 500 })
  }
}
