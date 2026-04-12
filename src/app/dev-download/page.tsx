import Link from 'next/link'
import { Button } from '@/components/ui/button'

export const metadata = {
  title: 'Developer Download — OnDevice AI',
  robots: 'noindex',
}

export default function DevDownloadPage() {
  return (
    <main className="min-h-screen bg-[#0A0A0A] text-white flex items-center justify-center px-6">
      <div className="max-w-md w-full text-center">
        <div className="inline-flex items-center gap-2 bg-[#111] border border-[#222] rounded-full px-4 py-1.5 text-sm text-yellow-400 mb-8">
          <span className="w-2 h-2 rounded-full bg-yellow-400" />
          Developer build
        </div>

        <h1 className="text-2xl font-bold mb-3">Download OnDevice AI</h1>
        <p className="text-[#6B7280] mb-8">
          Test build — no payment required.
        </p>

        <a href="/api/download/dev" download="OnDeviceAI.apk">
          <Button
            size="lg"
            className="w-full bg-[#22C55E] hover:bg-[#16a34a] text-black font-bold py-6 mb-4"
          >
            ⬇ Download OnDevice AI
          </Button>
        </a>

        <p className="text-xs text-[#6B7280] mb-8">
          ~150MB · Android 8.0 and newer
        </p>

        <Link href="/install">
          <Button variant="outline" className="w-full border-[#2A2A2A] text-white hover:bg-[#1A1A1A]">
            How to install the app →
          </Button>
        </Link>
      </div>
    </main>
  )
}
