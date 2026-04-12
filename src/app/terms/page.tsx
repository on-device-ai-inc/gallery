import Link from 'next/link'
import Image from 'next/image'
import { ArrowLeft } from 'lucide-react'

export const metadata = {
  title: 'Terms of Service — OnDevice AI',
  description: 'Terms of service for OnDevice AI by OnDevice AI Inc.',
}

export default function TermsPage() {
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
        <h1 className="text-3xl font-black tracking-tight text-slate-900 mb-2">Terms of Service</h1>
        <p className="text-slate-400 text-sm mb-10">Last updated: April 2026 · OnDevice AI Inc.</p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">1. What you&apos;re buying</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          Your purchase is a personal, non-transferable license to install and use the OnDevice AI Android application.
          It is a one-time payment that grants lifetime access to the version purchased and all future updates.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">2. What you can and cannot do</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          You may install OnDevice AI on your personal Android devices. You may not redistribute, resell, or share
          the APK file or your download link with others. Each purchase is a single-user license.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">3. Refund policy</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          If OnDevice AI does not install or function on your device, contact us within 7 days of purchase for a
          full refund. We will issue a refund to your original payment method. Contact us via WhatsApp or
          support@on-device.org.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">4. Disclaimer</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          OnDevice AI is provided as-is. AI-generated responses may contain errors. Do not rely on AI outputs for
          medical, legal, or financial decisions without independent verification.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">5. Open-source components</h2>
        <p className="text-slate-500 leading-relaxed mb-4">
          The OnDevice AI application is built in part on{' '}
          <a href="https://github.com/google-ai-edge/gallery" className="text-cyan-600 underline">Google AI Edge Gallery</a>,
          which is licensed under the Apache License, Version 2.0. The full text of that license is available at
          apache.org/licenses/LICENSE-2.0. OnDevice AI Inc.&apos;s proprietary enhancements, UI, branding, and
          distribution infrastructure are separate works and are not covered by the Apache License.
        </p>

        <h2 className="text-xl font-bold text-slate-900 mt-8 mb-3">6. Governing law</h2>
        <p className="text-slate-500 leading-relaxed">
          These terms are governed by the laws of Ontario, Canada. OnDevice AI Inc. is incorporated in Ottawa, Canada.
        </p>

        <div className="mt-12 pt-6 border-t border-slate-200">
          <p className="text-xs text-slate-400">© 2026 OnDevice AI Inc. Ottawa, Canada. All rights reserved.</p>
        </div>
      </div>
    </main>
  )
}
