'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { APK_INFO } from '@/lib/apk-info'

const WHATSAPP_URL =
  'https://wa.me/16135550123?text=Hi%2C%20my%20download%20link%20is%20not%20working'

interface Props {
  token: string
  orderId: string
}

export default function ActivationCard({ token, orderId }: Props) {
  const [apkStarted, setApkStarted] = useState(false)
  const deepLink = `ai.ondevice.app://activate?order_id=${orderId}`

  function handleApkClick() {
    setApkStarted(true)
  }

  return (
    <div className="space-y-3 mb-4">

      {/* Primary: Open in app (for reinstalls or if app already installed) */}
      <a href={deepLink}>
        <Button
          size="lg"
          className="w-full text-white font-bold py-6"
          style={{ background: 'linear-gradient(to right, #0891b2, #1d4ed8)' }}
        >
          Open in OnDevice AI
        </Button>
      </a>

      <p className="text-xs text-slate-400">
        Already installed? Tap above to activate instantly.
      </p>

      {/* Divider */}
      <div className="flex items-center gap-3 py-1">
        <div className="flex-1 h-px bg-slate-200" />
        <span className="text-xs text-slate-400 font-medium">first time?</span>
        <div className="flex-1 h-px bg-slate-200" />
      </div>

      {/* Secondary: Download APK */}
      {apkStarted ? (
        <div className="glass-card border border-cyan-200 rounded-xl p-4 text-left">
          <p className="text-cyan-700 font-semibold text-sm mb-1">⬇ Download started</p>
          <p className="text-slate-500 text-sm leading-relaxed">
            Once installed, tap <span className="font-semibold text-slate-900">Open in OnDevice AI</span> above to activate your license.
          </p>
          <p className="text-slate-400 text-xs mt-2">
            If your browser blocked it, open your Downloads folder and tap <span className="text-slate-700">OnDeviceAI.apk</span>.
          </p>
          <a
            href={WHATSAPP_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs text-slate-400 hover:text-slate-600 underline underline-offset-4 mt-3 block"
          >
            Need help? WhatsApp us →
          </a>
        </div>
      ) : (
        <>
          <a
            href={`/api/download/apk?token=${token}`}
            download="OnDeviceAI.apk"
            onClick={handleApkClick}
          >
            <Button
              size="lg"
              variant="outline"
              className="w-full font-bold py-6"
            >
              ⬇ Download APK — ~{APK_INFO.sizeMb}MB
            </Button>
          </a>
          <p className="text-xs text-slate-400">
            Android 9.0+ · Your browser may show a security warning — tap <span className="font-medium text-slate-600">Download anyway</span>
          </p>
        </>
      )}

    </div>
  )
}
