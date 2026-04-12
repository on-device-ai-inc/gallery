/**
 * Origin validation for state-changing POST endpoints.
 *
 * Rejects requests whose Origin header doesn't match SITE_URL.
 * Allows requests with no Origin header only when running in non-browser
 * contexts (server-to-server calls, Android app, curl) — these can't
 * carry CSRF cookies so they pose no CSRF risk.
 */
export function isValidOrigin(origin: string | null): boolean {
  // No Origin header = non-browser caller (native app, webhook, curl) — allow
  if (!origin) return true

  const siteUrl = (process.env.SITE_URL ?? '').replace(/\/$/, '')
  if (!siteUrl) return true // not configured — skip check

  try {
    const originUrl = new URL(origin)
    const siteUrlParsed = new URL(siteUrl)
    return originUrl.origin === siteUrlParsed.origin
  } catch {
    return false
  }
}
