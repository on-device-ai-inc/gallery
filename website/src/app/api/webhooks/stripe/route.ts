import { NextRequest, NextResponse } from 'next/server'
import { stripeProvider } from '@/lib/payments/stripe'
import { db, orders, downloadTokens } from '@/lib/db'
import { eq } from 'drizzle-orm'
import { SignJWT } from 'jose'
import { createHash } from 'crypto'
import { sendReceiptEmail, sendAdminSaleNotification } from '@/lib/email'

export async function POST(req: NextRequest) {
  const payload = await req.text()
  const signature = req.headers.get('stripe-signature') ?? ''

  try {
    const event = await stripeProvider.parseWebhook(payload, signature)

    if (event.status !== 'paid' || !event.providerRef) {
      return NextResponse.json({ received: true })
    }

    // Find the order
    const [order] = await db
      .select()
      .from(orders)
      .where(eq(orders.providerRef, event.providerRef))

    if (!order || order.status === 'paid') {
      return NextResponse.json({ received: true })
    }

    // Mark paid
    await db
      .update(orders)
      .set({ status: 'paid', paidAt: new Date() })
      .where(eq(orders.id, order.id))

    // Generate download token (24h, single-use)
    const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
    const token = await new SignJWT({ orderId: order.id, type: 'download' })
      .setProtectedHeader({ alg: 'HS256' })
      .setExpirationTime('24h')
      .setIssuedAt()
      .sign(secret)

    const tokenHash = createHash('sha256').update(token).digest('hex')
    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

    await db.insert(downloadTokens).values({
      id: crypto.randomUUID(),
      orderId: order.id,
      tokenHash,
      expiresAt,
    })

    // Send receipt email — non-fatal if it fails
    try {
      await sendReceiptEmail({
        to: order.email,
        downloadUrl: `${process.env.SITE_URL}/download?token=${token}`,
        orderId: order.id,
      })
      await sendAdminSaleNotification({
        customerEmail: order.email,
        orderId: order.id,
        amount: order.amountCents,
        currency: order.currency,
        provider: 'stripe',
      })
    } catch (emailErr) {
      console.error('[webhooks/stripe] email failed:', emailErr)
    }

    return NextResponse.json({ received: true })
  } catch (err) {
    console.error('[webhooks/stripe]', err)
    return NextResponse.json({ error: 'Webhook error' }, { status: 400 })
  }
}
