import { NextRequest, NextResponse } from 'next/server'
import { z } from 'zod'
import { isValidOrigin } from '@/lib/origin-check'
import { checkRateLimit } from '@/lib/rate-limit'
import { db } from '@/lib/db'
import { waitlistEntries } from '@/lib/db/schema'
import { sendWaitlistConfirmEmail } from '@/lib/email'

function ip(req: NextRequest): string {
  return req.headers.get('cf-connecting-ip') ?? req.headers.get('x-real-ip') ?? 'unknown'
}

const SubscribeSchema = z.object({
  email: z.string().email(),
})

export async function POST(req: NextRequest) {
  if (!isValidOrigin(req.headers.get('origin'))) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  }

  if (!checkRateLimit(`waitlist:subscribe:${ip(req)}`, 3, 60 * 60 * 1000)) {
    return NextResponse.json({ error: 'Too many requests' }, { status: 429 })
  }

  let body: unknown
  try {
    body = await req.json()
  } catch {
    return NextResponse.json({ error: 'Invalid request' }, { status: 400 })
  }

  const parsed = SubscribeSchema.safeParse(body)
  if (!parsed.success) {
    return NextResponse.json({ error: 'Invalid email address' }, { status: 400 })
  }

  const { email } = parsed.data
  const normalised = email.toLowerCase().trim()

  try {
    await db.insert(waitlistEntries).values({
      id: crypto.randomUUID(),
      email: normalised,
      source: req.headers.get('referer') ? 'website' : 'direct',
    })
  } catch (err: unknown) {
    // Drizzle wraps the postgres error — check both the top-level and cause
    const pgCode =
      (err && typeof err === 'object' && 'code' in err && (err as { code: string }).code) ||
      (err && typeof err === 'object' && 'cause' in err &&
        (err as { cause?: { code?: string } }).cause?.code)
    if (pgCode === '23505') {
      return NextResponse.json({ error: 'Already subscribed' }, { status: 409 })
    }
    console.error('[waitlist/subscribe]', err)
    return NextResponse.json({ error: 'Something went wrong' }, { status: 500 })
  }

  // Confirmation email — best-effort, never blocks the response
  try {
    await sendWaitlistConfirmEmail({ to: normalised })
  } catch (err) {
    console.error('[waitlist/subscribe] email failed', err)
  }

  return NextResponse.json({ success: true })
}
