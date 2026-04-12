import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  // Required for Docker standalone deployment
  output: 'standalone',

  images: {
    formats: ['image/avif', 'image/webp'],
    remotePatterns: [],
  },

  // Security headers (Caddy also sets these — belt and suspenders)
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          { key: 'X-Content-Type-Options', value: 'nosniff' },
          { key: 'X-Frame-Options', value: 'DENY' },
          { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
          { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload' },
          {
            key: 'Content-Security-Policy',
            value: [
              "default-src 'self'",
              "script-src 'self' 'unsafe-inline' 'unsafe-eval'", // Next.js requires unsafe-inline/eval
              "style-src 'self' 'unsafe-inline'",
              "img-src 'self' data: blob:",
              "font-src 'self'",
              "connect-src 'self' https://plausible.io",
              "frame-ancestors 'none'",
              "base-uri 'self'",
              "form-action 'self'",
            ].join('; '),
          },
        ],
      },
    ]
  },

  // Plausible analytics — allow their script domain
  async rewrites() {
    return [
      {
        source: '/stats/js/script.js',
        destination: 'https://plausible.io/js/script.js',
      },
      {
        source: '/stats/api/event',
        destination: 'https://plausible.io/api/event',
      },
    ]
  },
}

export default nextConfig
