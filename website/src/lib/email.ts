import { Resend } from 'resend'

// Lazy-init so `next build` page-data collection doesn't crash when
// RESEND_API_KEY is not present in the build environment.
let _resend: Resend | null = null
function resend(): Resend {
  if (_resend) return _resend
  const key = process.env.RESEND_API_KEY
  if (!key) throw new Error('RESEND_API_KEY is not set')
  _resend = new Resend(key)
  return _resend
}

export async function sendWaitlistConfirmEmail({ to }: { to: string }) {
  const siteUrl = (process.env.SITE_URL ?? 'https://on-device.org').replace(/\/$/, '')

  await resend().emails.send({
    from: process.env.EMAIL_FROM ?? 'OnDevice AI <hello@on-device.org>',
    to,
    subject: "You're on the list — OnDevice AI launches March 26",
    html: `
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"></head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0a0a0a; color: #f8fafc; padding: 40px 20px; margin: 0;">
  <div style="max-width: 480px; margin: 0 auto; background: #111; border-radius: 16px; padding: 40px; border: 1px solid #1e293b;">
    <div style="font-size: 32px; margin-bottom: 16px;">✅</div>
    <h1 style="font-size: 22px; font-weight: 800; margin: 0 0 8px; color: #f8fafc;">You&apos;re on the list.</h1>
    <p style="color: #94a3b8; margin: 0 0 32px; font-size: 15px;">
      OnDevice AI launches on <strong style="color: #22d3ee;">Wednesday, March 26</strong> — we&apos;ll send you a direct link the moment it&apos;s live.
    </p>

    <div style="background: #1e293b; border-radius: 12px; padding: 20px; margin-bottom: 32px;">
      <p style="font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin: 0 0 12px;">What you&apos;re getting</p>
      <ul style="margin: 0; padding: 0; list-style: none;">
        <li style="font-size: 14px; color: #cbd5e1; padding: 5px 0;">🤖 Full AI model running on your Android</li>
        <li style="font-size: 14px; color: #cbd5e1; padding: 5px 0;">✈️ Works completely offline after setup</li>
        <li style="font-size: 14px; color: #cbd5e1; padding: 5px 0;">🔒 Zero data sent to any server</li>
        <li style="font-size: 14px; color: #cbd5e1; padding: 5px 0;">💳 One-time payment — own it forever</li>
        <li style="font-size: 14px; color: #cbd5e1; padding: 5px 0;">📱 M-Pesa, MTN MoMo, and card accepted</li>
      </ul>
    </div>

    <a href="${siteUrl}" style="display: block; background: linear-gradient(to right, #0891b2, #1d4ed8); color: #fff; text-align: center; padding: 16px 24px; border-radius: 12px; font-weight: 700; font-size: 16px; text-decoration: none; margin-bottom: 24px;">
      Visit on-device.org
    </a>

    <hr style="border: none; border-top: 1px solid #1e293b; margin: 24px 0;">
    <p style="color: #475569; font-size: 12px; margin: 0;">
      OnDevice AI Inc. &middot; Ottawa, Ontario &middot;
      <a href="${siteUrl}/privacy" style="color: #475569;">Privacy Policy</a>
    </p>
  </div>
</body>
</html>
    `,
  })
}

export async function sendReceiptEmail({
  to,
  downloadUrl,
  orderId,
}: {
  to: string
  downloadUrl: string
  orderId: string
}) {
  const deepLink = `ai.ondevice.app://activate?order_id=${orderId}`
  const siteUrl = (process.env.SITE_URL ?? 'https://on-device.org').replace(/\/$/, '')

  await resend().emails.send({
    from: process.env.EMAIL_FROM ?? 'OnDevice AI <hello@on-device.org>',
    to,
    subject: 'Your OnDevice AI download is ready',
    html: `
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"></head>
<body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f8fafc; color: #0f172a; padding: 40px 20px; margin: 0;">
  <div style="max-width: 480px; margin: 0 auto; background: #fff; border-radius: 16px; padding: 40px; border: 1px solid #e2e8f0;">
    <h1 style="font-size: 22px; font-weight: 800; margin: 0 0 6px;">Your purchase is complete.</h1>
    <p style="color: #64748b; margin: 0 0 32px; font-size: 15px;">Follow the steps below to get started.</p>

    <!-- Step 1: Download APK -->
    <p style="font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin: 0 0 8px;">Step 1 — Install the app</p>
    <a href="${downloadUrl}" style="display: block; background: #f1f5f9; color: #0f172a; text-align: center; padding: 14px 24px; border-radius: 12px; font-weight: 700; font-size: 15px; text-decoration: none; margin-bottom: 24px; border: 1px solid #e2e8f0;">
      ⬇ Download OnDevice AI APK
    </a>

    <!-- Step 2: Activate via deep link -->
    <p style="font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; margin: 0 0 8px;">Step 2 — Activate your license</p>
    <a href="${deepLink}" style="display: block; background: linear-gradient(to right, #0891b2, #1d4ed8); color: #fff; text-align: center; padding: 16px 24px; border-radius: 12px; font-weight: 700; font-size: 16px; text-decoration: none; margin-bottom: 8px;">
      Open in OnDevice AI
    </a>
    <p style="color: #94a3b8; font-size: 12px; margin: 0 0 24px; text-align: center;">
      Tap after installing — activates your purchase instantly. Works for reinstalls too.
    </p>

    <!-- Step 3 -->
    <div style="background: #f1f5f9; border-radius: 12px; padding: 20px; margin-bottom: 24px;">
      <p style="font-weight: 700; font-size: 13px; color: #475569; text-transform: uppercase; letter-spacing: 0.05em; margin: 0 0 10px;">Step 3 — Download your AI model</p>
      <p style="margin: 0; font-size: 14px; color: #334155; line-height: 1.6;">
        Inside the app, choose a model and download it over Wi-Fi. Models range from 584MB to 4.9GB. Once downloaded, your AI runs fully offline.
      </p>
    </div>

    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;">

    <p style="color: #64748b; font-size: 13px; margin: 0 0 4px;">
      Need help installing?
      <a href="${siteUrl}/install" style="color: #0891b2;">See the install guide →</a>
    </p>
    <p style="color: #64748b; font-size: 13px; margin: 0 0 16px;">
      Questions? <a href="https://wa.me/16135550123" style="color: #16a34a;">WhatsApp us →</a>
    </p>
    <p style="color: #cbd5e1; font-size: 11px; margin: 0;">
      Order ID: ${orderId} · OnDevice AI Inc., Ottawa, Ontario
    </p>
  </div>
</body>
</html>
    `,
  })
}
