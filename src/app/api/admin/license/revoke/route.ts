import { NextRequest, NextResponse } from 'next/server'
import { db, deviceLicenses } from '@/lib/db'
import { eq } from 'drizzle-orm'
import { timingSafeEqual } from 'crypto'
import { isValidOrigin } from '@/lib/origin-check'

function isValidAdminSecret(provided: string | null): boolean {
  const expected = process.env.ADMIN_SECRET
  if (!provided || !expected) return false
  try {
    const a = Buffer.from(provided)
    const b = Buffer.from(expected)
    if (a.length !== b.length) return false
    return timingSafeEqual(a, b)
  } catch {
    return false
  }
}

export async function POST(req: NextRequest) {
  if (!isValidOrigin(req.headers.get('origin'))) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  }

  if (!isValidAdminSecret(req.headers.get('x-admin-secret'))) {
    return NextResponse.json({ error: 'UNAUTHORIZED' }, { status: 401 })
  }

  let body: { license_key?: string }
  try {
    body = await req.json()
  } catch {
    return NextResponse.json({ error: 'INVALID_BODY' }, { status: 400 })
  }

  const { license_key } = body
  if (!license_key || typeof license_key !== 'string') {
    return NextResponse.json({ error: 'MISSING_LICENSE_KEY' }, { status: 400 })
  }

  const [existing] = await db
    .select()
    .from(deviceLicenses)
    .where(eq(deviceLicenses.licenseKey, license_key))

  if (!existing) {
    return NextResponse.json({ error: 'LICENSE_NOT_FOUND' }, { status: 404 })
  }
  if (existing.revoked) {
    return NextResponse.json({ error: 'ALREADY_REVOKED' }, { status: 409 })
  }

  const revokedAt = new Date()
  await db
    .update(deviceLicenses)
    .set({ revoked: true, revokedAt })
    .where(eq(deviceLicenses.licenseKey, license_key))

  return NextResponse.json({ revoked: true, revoked_at: revokedAt.toISOString() })
}
