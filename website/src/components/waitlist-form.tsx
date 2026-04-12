'use client'

import { useState } from 'react'
import { ArrowRight } from 'lucide-react'

export function WaitlistForm() {
  const [email, setEmail] = useState('')
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!email) return
    setStatus('loading')
    try {
      const res = await fetch('/api/waitlist/subscribe', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const data = await res.json()
      if (res.ok) {
        setStatus('success')
        setMessage("You're on the list. We'll email you when we launch.")
      } else if (res.status === 409) {
        setStatus('success')
        setMessage("You're already on the list. See you March 26.")
      } else {
        setStatus('error')
        setMessage(data.error ?? 'Something went wrong. Try again.')
      }
    } catch {
      setStatus('error')
      setMessage('Network error. Please try again.')
    }
  }

  if (status === 'success') {
    return (
      <div className="flex flex-col items-center gap-2 py-2">
        <span className="text-3xl">✅</span>
        <p className="text-white font-semibold text-base text-center">{message}</p>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-3 w-full max-w-md mx-auto">
      <input
        type="email"
        required
        placeholder="your@email.com"
        value={email}
        onChange={e => setEmail(e.target.value)}
        disabled={status === 'loading'}
        className="flex-1 px-5 py-3.5 rounded-full bg-white/10 border border-white/20 text-white placeholder:text-white/40 text-sm font-medium focus:outline-none focus:border-white/50 disabled:opacity-50"
      />
      <button
        type="submit"
        disabled={status === 'loading'}
        className="inline-flex items-center justify-center gap-2 font-bold text-sm text-black bg-white hover:bg-white/90 transition-colors px-6 py-3.5 rounded-full disabled:opacity-50 shrink-0"
      >
        {status === 'loading' ? 'Joining...' : <><span>Notify Me</span> <ArrowRight className="w-4 h-4" /></>}
      </button>
      {status === 'error' && (
        <p className="text-red-400 text-xs text-center sm:col-span-2">{message}</p>
      )}
    </form>
  )
}
