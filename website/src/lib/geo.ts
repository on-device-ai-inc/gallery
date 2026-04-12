// Detect country from request headers
// Caddy doesn't set CF-IPCountry, so we use ipapi.co as fallback
export async function getCountryFromRequest(req?: Request): Promise<string> {
  // Check Cloudflare header (if behind CF later)
  if (req) {
    const cfCountry = req.headers.get('cf-ipcountry')
    if (cfCountry && cfCountry !== 'XX') return cfCountry

    // Check forwarded IP from Caddy
    const forwarded = req.headers.get('x-forwarded-for')
    const ip = forwarded?.split(',')[0]?.trim()
    if (ip && ip !== '127.0.0.1' && !ip.startsWith('192.168') && !ip.startsWith('10.')) {
      try {
        const res = await fetch(`https://ipapi.co/${ip}/country/`, {
          next: { revalidate: 86400 },
        })
        const country = await res.text()
        if (country.length === 2) return country.toUpperCase()
      } catch {
        // ignore
      }
    }
  }
  return 'US' // default
}
