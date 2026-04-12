import { redirect } from 'next/navigation'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import ActivationCard from './activation-card'
import Stripe from 'stripe'
import { SignJWT, jwtVerify } from 'jose'
import { createHash } from 'crypto'
import { db, orders, downloadTokens } from '@/lib/db'
import { eq } from 'drizzle-orm'

// Lazy-init so `next build` page-data collection doesn't crash when
// STRIPE_SECRET_KEY is not present in the build environment.
let _stripe: Stripe | null = null
function stripe(): Stripe {
  if (_stripe) return _stripe
  const key = process.env.STRIPE_SECRET_KEY
  if (!key) throw new Error('STRIPE_SECRET_KEY is not set')
  _stripe = new Stripe(key, { apiVersion: '2026-02-25.clover' })
  return _stripe
}

export default async function DownloadPage({
  searchParams,
}: {
  searchParams: Promise<{ session_id?: string; token?: string }>
}) {
  const params = await searchParams
  const { session_id, token } = params

  // --- Path A: coming from Stripe redirect with session_id ---
  if (session_id) {
    const session = await stripe().checkout.sessions.retrieve(session_id)

    if (session.payment_status !== 'paid') {
      redirect('/#waitlist')
    }

    const [order] = await db
      .select()
      .from(orders)
      .where(eq(orders.providerRef, session_id))

    if (!order) redirect('/#waitlist')

    if (order.status !== 'paid') {
      await db
        .update(orders)
        .set({ status: 'paid', paidAt: new Date() })
        .where(eq(orders.id, order.id))
    }

    const downloadToken = await generateToken(order.id)
    redirect(`/download?token=${downloadToken}`)
  }

  // --- Path B: direct token link (from email or redirect above) ---
  if (!token) redirect('/#waitlist')

  const { valid, orderId, error } = await verifyToken(token)

  if (!valid || !orderId) {
    return <ErrorPage message={error ?? 'Invalid download link'} />
  }

  return (
    <main className="min-h-screen bg-slate-50 text-slate-700 flex items-center justify-center px-6 py-12">
      <div className="max-w-md w-full text-center">
        <div className="text-5xl mb-6">✅</div>
        <h1 className="text-2xl font-bold mb-3 text-slate-900">You&apos;re all set!</h1>
        <p className="text-slate-500 mb-8">
          Download the app, install it, then tap <strong>Open in OnDevice AI</strong> to activate.
        </p>

        <ActivationCard token={token} orderId={orderId} />

        <Link href="/install">
          <Button variant="outline" className="w-full mt-4">
            How to install the app →
          </Button>
        </Link>

        <p className="text-xs text-slate-400 mt-6">
          A receipt with a reinstall link has been sent to your email.
        </p>
      </div>
    </main>
  )
}

async function generateToken(orderId: string): Promise<string> {
  const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
  const token = await new SignJWT({ orderId, type: 'download' })
    .setProtectedHeader({ alg: 'HS256' })
    .setExpirationTime('24h')
    .setIssuedAt()
    .sign(secret)

  const tokenHash = createHash('sha256').update(token).digest('hex')
  const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000)

  await db.insert(downloadTokens).values({
    id: crypto.randomUUID(),
    orderId,
    tokenHash,
    expiresAt,
  }).onConflictDoNothing()

  return token
}

async function verifyToken(token: string): Promise<{ valid: boolean; orderId?: string; error?: string }> {
  try {
    const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET!)
    const { payload } = await jwtVerify(token, secret)

    const tokenHash = createHash('sha256').update(token).digest('hex')
    const [record] = await db
      .select()
      .from(downloadTokens)
      .where(eq(downloadTokens.tokenHash, tokenHash))

    if (!record) return { valid: false, error: 'Download link not found.' }
    if (record.usedAt) return { valid: false, error: 'This link has already been used.' }
    if (record.expiresAt < new Date()) return { valid: false, error: 'This link has expired.' }

    return { valid: true, orderId: payload.orderId as string }
  } catch {
    return { valid: false, error: 'Invalid download link.' }
  }
}

function ErrorPage({ message }: { message: string }) {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-700 flex items-center justify-center px-6 py-12">
      <div className="max-w-md w-full text-center">
        <div className="text-5xl mb-6">⚠️</div>
        <h1 className="text-2xl font-bold mb-3 text-slate-900">Link issue</h1>
        <p className="text-slate-500 mb-8">{message}</p>
        <Link href="/#waitlist">
          <Button
            className="text-white font-bold"
            style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}
          >
            Back to homepage
          </Button>
        </Link>
      </div>
    </main>
  )
}
