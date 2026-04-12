'use client'

import { useEffect, useState } from 'react'

// Launch: Wednesday March 26, 2026 at 10:00 UTC (13:00 Nairobi / 06:00 New York)
const LAUNCH_DATE = new Date('2026-03-26T10:00:00Z')

function getTimeLeft() {
  const diff = LAUNCH_DATE.getTime() - Date.now()
  if (diff <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0, launched: true }
  return {
    days: Math.floor(diff / (1000 * 60 * 60 * 24)),
    hours: Math.floor((diff / (1000 * 60 * 60)) % 24),
    minutes: Math.floor((diff / (1000 * 60)) % 60),
    seconds: Math.floor((diff / 1000) % 60),
    launched: false,
  }
}

export function CountdownTimer() {
  const [time, setTime] = useState(getTimeLeft)

  useEffect(() => {
    const id = setInterval(() => setTime(getTimeLeft()), 1000)
    return () => clearInterval(id)
  }, [])

  if (time.launched) {
    return <p className="text-cyan-400 font-bold text-xl">We&apos;re live! Get it now.</p>
  }

  const pad = (n: number) => String(n).padStart(2, '0')

  return (
    <div className="flex items-center gap-4 sm:gap-6">
      {[
        { value: time.days, label: 'Days' },
        { value: time.hours, label: 'Hours' },
        { value: time.minutes, label: 'Mins' },
        { value: time.seconds, label: 'Secs' },
      ].map(({ value, label }, i) => (
        <div key={label} className="flex items-center gap-4 sm:gap-6">
          <div className="flex flex-col items-center min-w-[3rem]">
            <span className="text-4xl sm:text-5xl font-black text-white tabular-nums leading-none">
              {pad(value)}
            </span>
            <span className="text-[10px] font-semibold uppercase tracking-widest text-white/40 mt-1.5">
              {label}
            </span>
          </div>
          {i < 3 && <span className="text-white/20 text-3xl font-light -mt-2">:</span>}
        </div>
      ))}
    </div>
  )
}
