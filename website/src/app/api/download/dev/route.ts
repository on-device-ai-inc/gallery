import { NextRequest, NextResponse } from 'next/server'
import { createReadStream, statSync } from 'fs'
import { Readable } from 'stream'
import { timingSafeEqual } from 'crypto'

const APK_PATH = process.env.APK_FILE_PATH ?? '/var/www/downloads/ondevice-ai.apk'

export async function GET(req: NextRequest) {
  const secret = process.env.ADMIN_SECRET
  const provided = req.headers.get('x-admin-secret')
  if (!secret || !provided) {
    return NextResponse.json({ error: 'Not available' }, { status: 404 })
  }
  try {
    if (!timingSafeEqual(Buffer.from(provided), Buffer.from(secret))) {
      return NextResponse.json({ error: 'Not available' }, { status: 404 })
    }
  } catch {
    return NextResponse.json({ error: 'Not available' }, { status: 404 })
  }

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
    return NextResponse.json({ error: 'APK file not found' }, { status: 404 })
  }
}
