/**
 * HMAC-signed model download tokens.
 * Token format: base64url(payload) + '.' + hmacHex
 * Payload: { lk: licenseKey, df: deviceFingerprint, exp: unixTimestampSeconds }
 */
import { createHmac, timingSafeEqual } from 'crypto'

function getSecret(): string {
  const s = process.env.LICENSE_HMAC_SECRET
  if (!s) throw new Error('LICENSE_HMAC_SECRET env var is not set')
  return s
}

export function signModelToken(licenseKey: string, deviceFingerprint: string, ttlSeconds = 3600): string {
  const exp = Math.floor(Date.now() / 1000) + ttlSeconds
  const payload = Buffer.from(JSON.stringify({ lk: licenseKey, df: deviceFingerprint, exp })).toString('base64url')
  const hmac = createHmac('sha256', getSecret()).update(payload).digest('hex')
  return `${payload}.${hmac}`
}

export type ModelTokenPayload = { lk: string; df: string; exp: number }

export function verifyModelToken(token: string): ModelTokenPayload {
  const dot = token.lastIndexOf('.')
  if (dot === -1) throw new Error('Malformed token')

  const payload = token.slice(0, dot)
  const hmac = token.slice(dot + 1)

  const expected = createHmac('sha256', getSecret()).update(payload).digest('hex')
  const expectedBuf = Buffer.from(expected, 'hex')
  const hmacBuf = Buffer.from(hmac.padEnd(expected.length, '0'), 'hex')
  if (expectedBuf.length !== hmacBuf.length || !timingSafeEqual(expectedBuf, hmacBuf)) {
    throw new Error('Invalid token signature')
  }

  const data = JSON.parse(Buffer.from(payload, 'base64url').toString()) as ModelTokenPayload
  if (Math.floor(Date.now() / 1000) > data.exp) throw new Error('Token expired')

  return data
}
