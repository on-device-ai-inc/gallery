'use client'

import { useEffect, useRef, type ReactNode } from 'react'

interface Props {
  children: ReactNode
  delay?: number
  direction?: 'up' | 'left' | 'right' | 'scale'
  className?: string
}

export function ScrollReveal({ children, delay = 0, direction = 'up', className = '' }: Props) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const el = ref.current
    if (!el) return

    const show = () => {
      el.style.transitionDelay = `${delay}ms`
      el.classList.add('sr-visible')
    }

    // Fire immediately if already in view (above fold)
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          show()
          observer.disconnect()
        }
      },
      { threshold: 0.08, rootMargin: '0px 0px -40px 0px' },
    )

    observer.observe(el)
    return () => observer.disconnect()
  }, [delay])

  const dirClass = direction === 'left' ? 'sr sr-left'
    : direction === 'right' ? 'sr sr-right'
    : direction === 'scale' ? 'sr sr-scale'
    : 'sr'

  return (
    <div ref={ref} className={`${dirClass} ${className}`}>
      {children}
    </div>
  )
}
