import { NextRequest, NextResponse } from 'next/server'
import { db, orders, downloadTokens } from '@/lib/db'
import { eq } from 'drizzle-orm'
import { SignJWT } from 'jose'
import { createHash } from 'crypto'
import { checkRateLimit } from '@/lib/rate-limit'

function ip(req: NextRequest): string {
  return req.headers.get('cf-connecting-ip') ?? req.headers.get('x-real-ip') ?? 'unknown'
}

export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ orderId: string }> }
) {
  const { orderId } = await params

  // 60 polls per IP per hour — generous for M-Pesa 3s polling loop
  if (!checkRateLimit(`pay:status:${ip(req)}`, 60, 60 * 60 * 1000)) {
    return NextResponse.json({ error: 'Too many requests' }, { status: 429 })
  }

  // Require caller to prove knowledge of the order's email to prevent enumeration
  const email = req.nextUrl.searchParams.get('email')?.toLowerCase().trim()
  if (!email) {
    return NextResponse.json({ error: 'Missing email parameter' }, { status: 400 })
  }

  const [order] = await db
    .select()
    .from(orders)
    .where(eq(orders.id, orderId))

  // Return the same 404 whether order doesn't exist or email doesn't match
  if (!order || order.email.toLowerCase() !== email) {
    return NextResponse.json({ status: 'not_found' }, { status: 404 })
  }

  if (order.status !== 'paid') {
    return NextResponse.json({ status: order.status })
  }

  // Order is paid — generate a download token for immediate access
  const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
  const token = await new SignJWT({ orderId: order.id, type: 'download' })
    .setProtectedHeader({ alg: 'HS256' })
    .setExpirationTime('24h')
    .setIssuedAt()
    .sign(secret)

  const tokenHash = createHash('sha256').update(token).digest('hex')
  const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

  // Store the token (idempotent — user may poll multiple times)
  await db.insert(downloadTokens).values({
    id: crypto.randomUUID(),
    orderId: order.id,
    tokenHash,
    expiresAt,
  }).onConflictDoNothing()

  const downloadUrl = `${process.env.SITE_URL}/download?token=${token}`

  return NextResponse.json({ status: 'paid', downloadUrl })
}
