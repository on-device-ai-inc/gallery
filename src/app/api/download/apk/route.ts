import { NextRequest, NextResponse } from 'next/server'
import { jwtVerify } from 'jose'
import { createHash } from 'crypto'
import { createReadStream, statSync } from 'fs'
import { db, downloadTokens } from '@/lib/db'
import { and, eq, isNull } from 'drizzle-orm'
import { Readable } from 'stream'

const APK_PATH = process.env.APK_FILE_PATH ?? '/var/www/downloads/ondevice-ai.apk'

export async function GET(req: NextRequest) {
  const token = req.nextUrl.searchParams.get('token')
  if (!token) return NextResponse.json({ error: 'Missing token' }, { status: 400 })

  // Verify JWT
  try {
    const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
    await jwtVerify(token, secret)
  } catch {
    return NextResponse.json({ error: 'Invalid or expired token' }, { status: 401 })
  }

  // Check DB record
  const tokenHash = createHash('sha256').update(token).digest('hex')
  const [record] = await db
    .select()
    .from(downloadTokens)
    .where(eq(downloadTokens.tokenHash, tokenHash))

  if (!record) return NextResponse.json({ error: 'Token not found' }, { status: 404 })
  if (record.expiresAt < new Date()) return NextResponse.json({ error: 'Link expired' }, { status: 410 })

  // Atomically claim the token — WHERE usedAt IS NULL prevents double-download in a race
  const claimed = await db
    .update(downloadTokens)
    .set({ usedAt: new Date() })
    .where(and(eq(downloadTokens.tokenHash, tokenHash), isNull(downloadTokens.usedAt)))
    .returning({ id: downloadTokens.id })

  if (claimed.length === 0) {
    return NextResponse.json({ error: 'Already downloaded' }, { status: 410 })
  }

  // Stream APK
  try {
    const stat = statSync(APK_PATH)
    const stream = createReadStream(APK_PATH)

    return new NextResponse(Readable.toWeb(stream) as ReadableStream, {
      status: 200,
      headers: {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': 'attachment; filename="OnDeviceAI.apk"',
        'Content-Length': stat.size.toString(),
        'Cache-Control': 'no-store',
      },
    })
  } catch {
    return NextResponse.json({ error: 'APK file not found on server' }, { status: 404 })
  }
}
