import { NextRequest, NextResponse } from 'next/server'
import { db, deviceLicenses } from '@/lib/db'
import { and, eq } from 'drizzle-orm'
import { verifyModelToken } from '@/lib/license-token'
import { generateModelSignedUrl } from '@/lib/models/signed-url'
import { isValidModelId } from '@/lib/models/registry'
import { checkRateLimit } from '@/lib/rate-limit'

export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ modelId: string }> }
) {
  const { modelId } = await params

  // Validate model ID
  if (!isValidModelId(modelId)) {
    return NextResponse.json({ error: 'UNKNOWN_MODEL_ID' }, { status: 404 })
  }

  const licenseKey = req.headers.get('x-license-key')
  const deviceFingerprint = req.headers.get('x-device-fingerprint')
  const modelToken = req.headers.get('x-model-token')

  // 30 signed URL requests per license per hour (4 models × multiple retries is plenty)
  const rateLimitKey = licenseKey ? `model:dl:${licenseKey}` : `model:dl:ip:${req.headers.get('cf-connecting-ip') ?? 'unknown'}`
  if (!checkRateLimit(rateLimitKey, 30, 60 * 60 * 1000)) {
    return NextResponse.json({ error: 'RATE_LIMITED' }, { status: 429 })
  }

  if (!licenseKey || !deviceFingerprint || !modelToken) {
    return NextResponse.json({ error: 'MISSING_HEADERS' }, { status: 401 })
  }

  // Verify HMAC token matches this license + device, and isn't expired
  let tokenPayload: { lk: string; df: string; exp: number }
  try {
    tokenPayload = verifyModelToken(modelToken)
  } catch {
    return NextResponse.json({ error: 'INVALID_OR_EXPIRED_TOKEN' }, { status: 401 })
  }

  if (tokenPayload.lk !== licenseKey || tokenPayload.df !== deviceFingerprint) {
    return NextResponse.json({ error: 'TOKEN_MISMATCH' }, { status: 401 })
  }

  // Verify license is active and not revoked
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
    return NextResponse.json({ error: 'LICENSE_NOT_FOUND' }, { status: 403 })
  }
  if (license.revoked) {
    return NextResponse.json({ error: 'LICENSE_REVOKED' }, { status: 403 })
  }

  // Generate signed CDN URL
  try {
    const { url, expiresAt } = await generateModelSignedUrl(modelId, deviceFingerprint)
    return NextResponse.json({ url, expires_at: expiresAt.toISOString(), model_id: modelId })
  } catch (err) {
    console.error('[models/download] signed URL generation failed:', err)
    return NextResponse.json({ error: 'CDN_UNAVAILABLE' }, { status: 503 })
  }
}
