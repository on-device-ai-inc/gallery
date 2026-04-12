import { NextRequest, NextResponse } from 'next/server'
import { db, deviceLicenses } from '@/lib/db'
import { and, eq } from 'drizzle-orm'
import { checkRateLimit } from '@/lib/rate-limit'

function ip(req: NextRequest): string {
  return (
    req.headers.get('cf-connecting-ip') ??
    req.headers.get('x-real-ip') ??
    'unknown'
  )
}

export async function GET(req: NextRequest) {
  // 100 verify calls per license per hour — generous for legitimate app behaviour
  const licenseKey = req.headers.get('x-license-key')
  const deviceFingerprint = req.headers.get('x-device-fingerprint')

  const rateLimitKey = licenseKey ? `verify:lk:${licenseKey}` : `verify:ip:${ip(req)}`
  if (!checkRateLimit(rateLimitKey, 100, 60 * 60 * 1000)) {
    return NextResponse.json({ valid: false, reason: 'RATE_LIMITED' }, { status: 429 })
  }

  if (!licenseKey || !deviceFingerprint) {
    return NextResponse.json({ valid: false, reason: 'MISSING_HEADERS' }, { status: 400 })
  }

  const [license] = await db
    .select()
    .from(deviceLicenses)
    .where(
      and(
        eq(deviceLicenses.licenseKey, licenseKey),
        eq(deviceLicenses.deviceFingerprint, deviceFingerprint)
      )
    )

  if (!license) {
    return NextResponse.json({ valid: false, reason: 'NOT_FOUND' })
  }

  if (license.revoked) {
    return NextResponse.json({ valid: false, reason: 'REVOKED' })
  }

  // Update last verified timestamp (non-fatal — don't block the response)
  db.update(deviceLicenses)
    .set({ lastVerifiedAt: new Date() })
    .where(eq(deviceLicenses.id, license.id))
    .catch((err) => console.error('[license/verify] update failed:', err))

  return NextResponse.json({ valid: true, grace_period_days: 30 })
}
