import Link from 'next/link'
import Image from 'next/image'
import { ArrowLeft } from 'lucide-react'

export const metadata = {
  title: 'Privacy Policy — OnDevice AI',
  description: 'Privacy policy for OnDevice AI by OnDevice AI Inc.',
}

export default function PrivacyPage() {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-700 antialiased">
      <nav className="glass-nav sticky top-0 z-50">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 h-14 flex items-center">
          <Link href="/" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 transition-colors text-sm font-semibold tap-target">
            <ArrowLeft className="w-4 h-4" />
            Back
          </Link>
          <div className="flex items-center gap-2 font-extrabold text-lg tracking-tight text-slate-900 absolute left-1/2 -translate-x-1/2">
            <Image src="/images/app-icon.png" alt="OnDevice AI" width={28} height={28} className="rounded-lg" />
            OnDevice<span className="text-cyan-600">AI</span>
          </div>
        </div>
      </nav>

      <div className="px-4 py-12 max-w-3xl mx-auto prose prose-slate">
        <h1 className="text-3xl font-black tracking-tight text-slate-900 mb-2">Privacy Policy</h1>
        <p className="text-slate-400 text-sm mb-10">Last updated: April 2026 · OnDevice AI Inc.</p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">1. What we collect</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          When you purchase OnDevice AI, we collect your email address to deliver your download link and send a receipt.
          Payment details are handled entirely by Stripe or Safaricom M-Pesa — we never see or store your card or M-Pesa PIN.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">2. What we do not collect</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          OnDevice AI runs all AI inference locally on your device. Your conversations, prompts, and responses are never
          transmitted to our servers or any third party. We have no visibility into your chat history.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">3. How we use your email</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          We use your email to send your download link and purchase receipt. We may send occasional product updates.
          You can unsubscribe from product emails at any time.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">4. Third-party services</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          We use Stripe for card payments and Safaricom M-Pesa for mobile payments in Kenya. These services have their
          own privacy policies. We use Resend to deliver transactional email.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">5. Contact</h2>
        <p className="text-slate-500 leading-relaxed">
          Questions about this policy? Contact us via WhatsApp or email at support@on-device.org.
        </p>

        <div className="mt-12 pt-6 border-t border-slate-200">
          <p className="text-xs text-slate-400">© 2026 OnDevice AI Inc. Ottawa, Canada. All rights reserved.</p>
        </div>
      </div>
    </main>
  )
}
