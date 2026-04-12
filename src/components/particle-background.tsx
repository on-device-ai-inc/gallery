'use client'

import { useEffect, useRef } from 'react'

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  opacity: number
}

export default function ParticleBackground() {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const mouse = useRef({ x: -9999, y: -9999 })
  const animRef = useRef<number>(0)

  useEffect(() => {
    // Disable on mobile (saves battery — canvas is hidden under hero image anyway)
    if (window.innerWidth < 768) return

    // Respect prefers-reduced-motion
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return

    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const COUNT = 90
    const CONNECT_DIST = 140
    const MOUSE_REPEL = 120
    const particles: Particle[] = []

    function resize() {
      if (!canvas) return
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
    }

    function spawn(): Particle {
      return {
        x: Math.random() * window.innerWidth,
        y: Math.random() * window.innerHeight,
        vx: (Math.random() - 0.5) * 0.4,
        vy: (Math.random() - 0.5) * 0.4,
        radius: Math.random() * 1.5 + 0.5,
        opacity: Math.random() * 0.4 + 0.1,
      }
    }

    resize()
    for (let i = 0; i < COUNT; i++) particles.push(spawn())

    function draw() {
      if (!canvas || !ctx) return
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      const mx = mouse.current.x
      const my = mouse.current.y

      for (let i = 0; i < particles.length; i++) {
        const p = particles[i]

        // Mouse repulsion
        const dx = p.x - mx
        const dy = p.y - my
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < MOUSE_REPEL && dist > 0) {
          const force = (MOUSE_REPEL - dist) / MOUSE_REPEL * 0.8
          p.vx += (dx / dist) * force
          p.vy += (dy / dist) * force
        }

        // Dampen velocity
        p.vx *= 0.98
        p.vy *= 0.98

        // Clamp speed
        const speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy)
        if (speed > 1.2) { p.vx = (p.vx / speed) * 1.2; p.vy = (p.vy / speed) * 1.2 }

        p.x += p.vx
        p.y += p.vy

        // Wrap edges
        if (p.x < 0) p.x = canvas.width
        if (p.x > canvas.width) p.x = 0
        if (p.y < 0) p.y = canvas.height
        if (p.y > canvas.height) p.y = 0

        // Draw dot
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
        ctx.fillStyle = `rgba(6, 182, 212, ${p.opacity * 0.6})`
        ctx.fill()

        // Draw connecting lines
        for (let j = i + 1; j < particles.length; j++) {
          const q = particles[j]
          const ldx = p.x - q.x
          const ldy = p.y - q.y
          const d = Math.sqrt(ldx * ldx + ldy * ldy)
          if (d < CONNECT_DIST) {
            const alpha = (1 - d / CONNECT_DIST) * 0.12
            ctx.beginPath()
            ctx.moveTo(p.x, p.y)
            ctx.lineTo(q.x, q.y)
            ctx.strokeStyle = `rgba(6, 182, 212, ${alpha * 1.2})`
            ctx.lineWidth = 0.6
            ctx.stroke()
          }
        }
      }

      animRef.current = requestAnimationFrame(draw)
    }

    draw()

    const onResize = () => resize()
    const onMove = (e: MouseEvent) => { mouse.current.x = e.clientX; mouse.current.y = e.clientY }
    const onLeave = () => { mouse.current.x = -9999; mouse.current.y = -9999 }

    window.addEventListener('resize', onResize)
    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseleave', onLeave)

    return () => {
      cancelAnimationFrame(animRef.current)
      window.removeEventListener('resize', onResize)
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseleave', onLeave)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      className="fixed inset-0 w-full h-full pointer-events-none"
      style={{ zIndex: 0 }}
    />
  )
}
