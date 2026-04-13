import { NextRequest, NextResponse } from 'next/server'
import { mpesaProvider } from '@/lib/payments/mpesa'
import { db, orders, downloadTokens } from '@/lib/db'
import { eq } from 'drizzle-orm'
import { SignJWT } from 'jose'
import { createHash, timingSafeEqual } from 'crypto'
import { sendReceiptEmail, sendAdminSaleNotification } from '@/lib/email'

// M-Pesa requires ResultCode 0 in ALL responses — even errors — or it will retry indefinitely
const ack = () => NextResponse.json({ ResultCode: 0, ResultDesc: 'Accepted' })

function isAuthorized(req: NextRequest): boolean {
  const username = process.env.MPESA_CALLBACK_USERNAME
  const password = process.env.MPESA_CALLBACK_PASSWORD
  // If credentials are not configured, skip auth (sandbox / dev)
  if (!username || !password) return true
  const auth = req.headers.get('authorization') ?? ''
  if (!auth.startsWith('Basic ')) return false
  const decoded = Buffer.from(auth.slice(6), 'base64').toString('utf8')
  const [u, p] = decoded.split(':')
  try {
    const uOk = timingSafeEqual(Buffer.from(u ?? ''), Buffer.from(username))
    const pOk = timingSafeEqual(Buffer.from(p ?? ''), Buffer.from(password))
    return uOk && pOk
  } catch {
    return false
  }
}

export async function POST(req: NextRequest) {
  if (!isAuthorized(req)) {
    return NextResponse.json({ ResultCode: 1, ResultDesc: 'Unauthorized' }, { status: 401 })
  }

  try {
    const payload = await req.json()
    const event = await mpesaProvider.parseWebhook(payload, '')

    if (event.status !== 'paid' || !event.providerRef) {
      return ack()
    }

    const [order] = await db
      .select()
      .from(orders)
      .where(eq(orders.providerRef, event.providerRef))

    if (!order || order.status === 'paid') {
      return ack()
    }

    await db
      .update(orders)
      .set({ status: 'paid', paidAt: new Date() })
      .where(eq(orders.id, order.id))

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
        provider: 'mpesa',
      })
    } catch (emailErr) {
      console.error('[webhooks/mpesa] email failed:', emailErr)
    }

    return ack()
  } catch (err) {
    console.error('[webhooks/mpesa]', err)
    return ack()
  }
}
