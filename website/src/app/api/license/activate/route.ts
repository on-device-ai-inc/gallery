import { NextRequest, NextResponse } from 'next/server'
import { db, orders, deviceLicenses } from '@/lib/db'
import { eq } from 'drizzle-orm'
import { signModelToken } from '@/lib/license-token'
import { checkRateLimit } from '@/lib/rate-limit'

function ip(req: NextRequest): string {
  // Trust Cloudflare's verified IP header first; never trust X-Forwarded-For alone
  return (
    req.headers.get('cf-connecting-ip') ??
    req.headers.get('x-real-ip') ??
    'unknown'
  )
}

export async function POST(req: NextRequest) {
  // Rate limit per IP — 5 activations per hour
  if (!checkRateLimit(`activate:ip:${ip(req)}`, 5, 60 * 60 * 1000)) {
    return NextResponse.json(
      { error: 'RATE_LIMITED', retry_after_seconds: 3600 },
      { status: 429 }
    )
  }

  let body: { order_id?: string; device_fingerprint?: string }
  try {
    body = await req.json()
  } catch {
    return NextResponse.json({ error: 'INVALID_BODY' }, { status: 400 })
  }

  const { order_id, device_fingerprint } = body

  if (!order_id || typeof order_id !== 'string') {
    return NextResponse.json({ error: 'MISSING_ORDER_ID' }, { status: 400 })
  }

  // Per-order rate limit — prevents brute-forcing device mismatches
  if (!checkRateLimit(`activate:order:${order_id}`, 10, 60 * 60 * 1000)) {
    return NextResponse.json(
      { error: 'RATE_LIMITED', retry_after_seconds: 3600 },
      { status: 429 }
    )
  }
  if (!device_fingerprint || typeof device_fingerprint !== 'string' || device_fingerprint.length !== 64) {
    return NextResponse.json({ error: 'INVALID_DEVICE_FINGERPRINT' }, { status: 400 })
  }

  // Validate order exists and is paid
  const [order] = await db.select().from(orders).where(eq(orders.id, order_id))

  if (!order) {
    return NextResponse.json({ error: 'ORDER_NOT_FOUND' }, { status: 404 })
  }
  if (order.status !== 'paid') {
    return NextResponse.json({ error: 'ORDER_NOT_PAID' }, { status: 402 })
  }

  // Run activation inside a transaction to prevent TOCTOU race on concurrent requests
  try {
    const result = await db.transaction(async (tx) => {
      const [existing] = await tx
        .select()
        .from(deviceLicenses)
        .where(eq(deviceLicenses.orderId, order_id))
        .for('update') // row-level lock

      if (existing) {
        if (existing.deviceFingerprint === device_fingerprint) {
          await tx
            .update(deviceLicenses)
            .set({ reactivationCount: existing.reactivationCount + 1, lastVerifiedAt: new Date() })
            .where(eq(deviceLicenses.id, existing.id))
          return { type: 'reactivation' as const, licenseKey: existing.licenseKey, count: existing.reactivationCount + 1 }
        }
        return { type: 'mismatch' as const }
      }

      const licenseKey = crypto.randomUUID()
      await tx.insert(deviceLicenses).values({
        id: crypto.randomUUID(),
        orderId: order_id,
        deviceFingerprint: device_fingerprint,
        licenseKey,
        activatedAt: new Date(),
        lastVerifiedAt: new Date(),
      })
      return { type: 'new' as const, licenseKey }
    })

    if (result.type === 'mismatch') {
      return NextResponse.json(
        { error: 'DEVICE_MISMATCH', message: 'This order is already activated on a different device.' },
        { status: 409 }
      )
    }

    const model_download_token = signModelToken(result.licenseKey, device_fingerprint)

    if (result.type === 'reactivation') {
      return NextResponse.json({ license_key: result.licenseKey, model_download_token, reactivation_count: result.count })
    }

    return NextResponse.json({ license_key: result.licenseKey, model_download_token, activated_at: new Date().toISOString() })

  } catch (err) {
    console.error('[license/activate] transaction failed:', err)
    return NextResponse.json({ error: 'ACTIVATION_FAILED' }, { status: 500 })
  }
}
