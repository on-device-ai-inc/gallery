/**
 * Simple in-memory rate limiter.
 * Suitable for single-server deployment (DGX Spark).
 * State resets on server restart — acceptable for license activation.
 */

interface Bucket {
  count: number
  resetAt: number  // unix ms
}

const store = new Map<string, Bucket>()

// Clean up expired buckets periodically to avoid memory leak
if (typeof setInterval !== 'undefined') {
  setInterval(() => {
    const now = Date.now()
    for (const [key, bucket] of store) {
      if (bucket.resetAt < now) store.delete(key)
    }
  }, 60_000)
}

/**
 * Returns true if the request is within limits, false if rate-limited.
 * @param key       Identifier (e.g. IP address)
 * @param limit     Max requests per window
 * @param windowMs  Window size in milliseconds
 */
export function checkRateLimit(key: string, limit: number, windowMs: number): boolean {
  const now = Date.now()
  const bucket = store.get(key)

  if (!bucket || bucket.resetAt < now) {
    store.set(key, { count: 1, resetAt: now + windowMs })
    return true
  }

  if (bucket.count >= limit) return false

  bucket.count++
  return true
}
