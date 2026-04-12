'use client'

import { useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { Menu, X } from 'lucide-react'
import { usePathname } from 'next/navigation'

export function NavBar() {
  const [menuOpen, setMenuOpen] = useState(false)
  const pathname = usePathname()

  const navLinks = [
    { href: '/#how-it-works', label: 'How It Works' },
    { href: '/#pricing', label: 'Pricing' },
    { href: '/install', label: 'Install Guide' },
    { href: '/blog', label: 'Blog' },
  ]

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-black" style={{ height: '60px' }}>
      <div className="w-full h-full px-6 lg:px-10 flex items-center justify-between">

        {/* Mobile left: hamburger */}
        <button
          className="lg:hidden flex items-center gap-2.5 text-white"
          onClick={() => setMenuOpen(v => !v)}
          aria-label={menuOpen ? 'Close menu' : 'Open menu'}
          aria-expanded={menuOpen}
        >
          {menuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          <div className="flex flex-col leading-none">
            <span className="text-[10px] font-semibold tracking-wide uppercase text-white/90">On-Device</span>
            <span className="text-[10px] font-semibold tracking-wide uppercase text-white/60">Menu</span>
          </div>
        </button>

        {/* Logo — center on mobile, left on desktop */}
        <Link
          href="/"
          className="absolute left-1/2 -translate-x-1/2 lg:static lg:translate-x-0 flex items-center gap-2 text-white font-bold text-base tracking-tight"
        >
          <Image
            src="/images/app-icon.png"
            alt="OnDevice AI"
            width={28}
            height={28}
            className="rounded-lg"
            priority
          />
          <span className="hidden sm:inline">OnDevice<span className="text-white/60">AI</span></span>
        </Link>

        {/* Desktop right: nav links + CTA */}
        <nav className="hidden lg:flex items-center gap-6">
          {navLinks.map(({ href, label }) => {
            const isActive = pathname === href
            return (
              <Link
                key={href}
                href={href}
                className={`text-sm font-medium transition-colors ${
                  isActive ? 'text-white' : 'text-white/60 hover:text-white'
                }`}
              >
                {label}
              </Link>
            )
          })}
          <Link
            href="/buy"
            className="inline-flex items-center justify-center text-xs font-semibold text-white border border-white/40 hover:bg-white hover:text-black transition-all duration-200 px-5 py-1.5 ml-2"
            style={{ borderRadius: '999px' }}
          >
            Buy Now
          </Link>
        </nav>

        {/* Mobile right: Buy pill */}
        <Link
          href="/buy"
          className="lg:hidden text-xs font-semibold text-white border border-white/40 px-3 py-1.5 rounded-full"
        >
          Buy
        </Link>
      </div>

      {/* Mobile menu drawer — animated */}
      <div
        className={`lg:hidden overflow-hidden transition-all duration-150 ease-in-out ${
          menuOpen ? 'max-h-64 opacity-100' : 'max-h-0 opacity-0'
        }`}
        style={{ background: '#000', borderTop: menuOpen ? '1px solid rgba(255,255,255,0.1)' : 'none' }}
      >
        <div className="px-6 py-6 flex flex-col gap-5">
          {navLinks.map(({ href, label }) => {
            const isActive = pathname === href
            return (
              <Link
                key={href}
                href={href}
                onClick={() => setMenuOpen(false)}
                className={`font-semibold text-sm transition-colors ${
                  isActive ? 'text-cyan-400' : 'text-white hover:text-white/80'
                }`}
              >
                {label}
              </Link>
            )
          })}
          <Link
            href="/buy"
            onClick={() => setMenuOpen(false)}
            className="font-semibold text-sm text-cyan-400"
          >
            Buy Now →
          </Link>
        </div>
      </div>
    </header>
  )
}
