import Link from 'next/link'
import Image from 'next/image'
import { ArrowLeft, ShieldCheck, ArrowRight } from 'lucide-react'
import { BASE_PRICE_USD } from '@/lib/currencies'
import { APK_INFO } from '@/lib/apk-info'

type Step = { number: string; title: string; body: string }

export const metadata = {
  title: 'How to Install OnDevice AI',
  description: 'Install OnDevice AI on your Android phone in under a minute.',
}

const steps = [
  {
    number: '01',
    title: 'Tap "Download anyway" in Chrome',
    body: 'Chrome will show a warning: "This type of file can harm your device." This is a standard message for all Android apps downloaded outside the Play Store. Tap Download anyway — it is safe to proceed.',
  },
  {
    number: '02',
    title: 'Open the downloaded file',
    body: 'Find OnDeviceAI.apk in your Downloads folder, or tap the download notification at the bottom of Chrome.',
  },
  {
    number: '03',
    title: 'Allow installation from this source',
    body: 'Android will ask permission to install apps from your browser. Tap Settings, toggle on "Allow from this source", then go back and tap Install.',
  },
  {
    number: '04',
    title: 'If Play Protect appears, tap "Install anyway"',
    body: 'Google Play Protect may warn that the app wasn\'t installed from the Play Store. This is expected — we\'re not on the Play Store yet. Tap "More details" then "Install anyway".',
  },
  {
    number: '05',
    title: 'Open OnDevice AI and download your model',
    body: 'Tap Open or find the app in your app drawer. On first launch, download the AI model — the Lite model is 584MB, our recommended model is 3.7GB. This is your one-time data cost. After that, every chat is free.',
  },
]

export default function InstallPage() {
  return (
    <main className="min-h-screen bg-slate-50 text-slate-700 antialiased">

      {/* Nav */}
      <nav className="glass-nav sticky top-0 z-50">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 h-14 flex items-center">
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

      <div className="px-4 py-12 max-w-2xl mx-auto">

        <h1 className="text-4xl font-black tracking-tight mb-3 text-slate-900">How to install</h1>
        <p className="text-slate-500 font-medium mb-12 leading-relaxed">
          OnDevice AI is not on the app store yet — you install it directly from this site.
          Takes about a minute.
        </p>

        <div className="space-y-6 mb-12">
          {(steps as Step[]).map(step => (
            <div key={step.number} className="glass-card rounded-2xl p-5 flex gap-5">
              <span className="font-mono font-black text-lg mt-0.5 shrink-0 text-cyan-600">
                {step.number}
              </span>
              <div className="flex-1">
                <h3 className="font-bold text-slate-900 mb-1.5">{step.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed">{step.body}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Safety note */}
        <div className="glass-card rounded-2xl p-6 mb-6 space-y-4 border border-cyan-500/20">
          <div className="flex items-center gap-2 text-cyan-600 font-bold text-sm">
            <ShieldCheck className="w-4 h-4" /> About the warnings
          </div>
          <p className="text-sm text-slate-500 leading-relaxed">
            <span className="text-slate-900 font-semibold">Why do these appear?</span>{' '}
            Android shows security warnings for every app installed outside the Play Store — it is a blanket policy, not a judgment on this specific app.
          </p>
          <p className="text-sm text-slate-500 leading-relaxed">
            <span className="text-slate-900 font-semibold">Is it safe?</span>{' '}
            Yes. OnDevice AI runs entirely on your device — no internet access after download, no accounts, no data collection. Signed by OnDevice AI Inc.
          </p>
        </div>

        {/* APK verification */}
        <div className="glass-card rounded-2xl p-6 mb-10 space-y-3">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">APK verification</p>
          <p className="text-sm text-slate-500 leading-relaxed">
            Verify the file you downloaded matches our official release:
          </p>
          <div className="bg-slate-100 rounded-lg px-4 py-3 font-mono text-xs text-slate-600 break-all leading-relaxed">
            <span className="text-slate-400 select-none">SHA-256  </span>{APK_INFO.sha256}
          </div>
          <p className="text-xs text-slate-400">
            Run{' '}
            <code className="bg-slate-100 px-1.5 py-0.5 rounded text-slate-600">sha256sum OnDeviceAI.apk</code>
            {' '}(Linux/Mac) or{' '}
            <code className="bg-slate-100 px-1.5 py-0.5 rounded text-slate-600">Get-FileHash OnDeviceAI.apk</code>
            {' '}(Windows PowerShell) and compare.
            <span className="block mt-1">Version {APK_INFO.version} · Built {APK_INFO.builtAt}</span>
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-4">
          <Link
            href="/buy"
            className="tap-target flex-1 text-white font-bold py-4 px-6 rounded-xl transition-all flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(6,182,212,0.25)]"
            style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}
          >
            Get OnDevice AI — ${BASE_PRICE_USD.toFixed(2)} <ArrowRight className="w-4 h-4" />
          </Link>
          <Link
            href="/"
            className="tap-target flex-1 glass-card text-slate-600 font-semibold py-4 px-6 rounded-xl hover:bg-slate-100 transition-colors flex items-center justify-center"
          >
            Back to home
          </Link>
        </div>
      </div>
    </main>
  )
}
