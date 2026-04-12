import { NextRequest, NextResponse } from 'next/server'

// Country code → localised route
const COUNTRY_ROUTES: Record<string, string> = {
  KE: '/ke',
  NG: '/ng',
  ZA: '/za',
  GH: '/gh',
  TZ: '/tz',
  ZW: '/zw',
}

function countryFromAcceptLanguage(header: string): string | null {
  // e.g. "en-KE,en;q=0.9" → "KE"
  const first = header.split(',')[0]?.split(';')[0]?.trim() ?? ''
  const region = first.split('-')[1]?.toUpperCase()
  return region ?? null
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // Only redirect from the root — never touch other paths
  if (pathname !== '/') return NextResponse.next()

  // Cloudflare injects CF-IPCountry; fallback to Accept-Language
  const country =
    request.headers.get('CF-IPCountry') ??
    request.headers.get('cf-ipcountry') ??
    countryFromAcceptLanguage(request.headers.get('accept-language') ?? '') ??
    ''

  const route = COUNTRY_ROUTES[country.toUpperCase()]
  if (!route) return NextResponse.next()

  const url = request.nextUrl.clone()
  url.pathname = route
  // 307 = temporary redirect, preserves method, won't be cached permanently
  return NextResponse.redirect(url, { status: 307 })
}

export const config = {
  matcher: '/',
}
