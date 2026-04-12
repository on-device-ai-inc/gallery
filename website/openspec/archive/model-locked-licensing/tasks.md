# Tasks: model-locked-licensing

**Status**: ✅ COMPLETE (T2-01 deferred — R2 bucket setup is infrastructure)
**Total tasks**: 18
**Sprints**: 3

---

## Sprint 1 — Database + Core License API

### ✅ T1-01 — device_licenses schema
**File**: `src/lib/db/schema.ts`
**Change**: Add `device_licenses` table:
```ts
export const deviceLicenses = pgTable('device_licenses', {
  id: text('id').primaryKey(),                          // cuid
  orderId: text('order_id').notNull().references(() => orders.id),
  deviceFingerprint: text('device_fingerprint').notNull(), // SHA-256 hex
  licenseKey: text('license_key').notNull().unique(),   // UUID v4
  activatedAt: timestamp('activated_at').notNull().defaultNow(),
  lastVerifiedAt: timestamp('last_verified_at'),
  reactivationCount: integer('reactivation_count').notNull().default(0),
  revoked: boolean('revoked').notNull().default(false),
  revokedAt: timestamp('revoked_at'),
  createdAt: timestamp('created_at').notNull().defaultNow(),
})
```
**Acceptance**: Migration runs clean, table created in dev DB

---

### ✅ T1-02 — Drizzle migration
**File**: `drizzle/` (generated)
**Change**: Run `pnpm drizzle-kit generate` then `pnpm drizzle-kit migrate`
**Acceptance**: Migration file generated, applied without errors, table visible in DB

---

### ✅ T1-03 — POST /api/license/activate
**File**: `src/app/api/license/activate/route.ts` (new)
**Logic**:
1. Validate body: `{ order_id: string, device_fingerprint: string }`
2. Look up order — must exist and `status === 'paid'`
3. Check if license already exists for this `order_id`:
   - Same `device_fingerprint` → reactivation (increment count, return existing `license_key`)
   - Different `device_fingerprint` → return `409 { error: 'DEVICE_MISMATCH' }`
4. New activation → insert `device_licenses`, return `{ license_key, model_download_token }`
5. `model_download_token` = HMAC-SHA256 signed token: `{ license_key, device_fingerprint, exp: now+1h }`

**Error responses**:
- `404` — order not found
- `402` — order not paid
- `409` — device mismatch (already activated on different device)
- `429` — rate limited

**Acceptance**: All paths return correct status codes; same-device reactivation works; cross-device rejected

---

### ✅ T1-04 — GET /api/license/verify
**File**: `src/app/api/license/verify/route.ts` (new)
**Logic**:
1. Read headers: `X-License-Key`, `X-Device-Fingerprint`
2. Look up `device_licenses` by `license_key` + `device_fingerprint`
3. If not found or `revoked === true` → `{ valid: false }`
4. Update `last_verified_at = now()`
5. Return `{ valid: true, grace_period_days: 30 }`

**Acceptance**: Returns correct response; updates `last_verified_at` on each call; revoked license returns `valid: false`

---

### ✅ T1-05 — Rate limiting on activate endpoint
**File**: `src/app/api/license/activate/route.ts`
**Change**: Add rate limit: 5 activations per IP per hour using in-memory store or Upstash Redis
**Acceptance**: 6th request in 1 hour from same IP returns `429`

---

## Sprint 2 — Model CDN + Download Endpoint

### ⚠️ T2-01 (INFRASTRUCTURE — manual setup required) — Cloudflare R2 bucket setup
**Config**: External (not a code change)
**Steps**:
1. Create R2 bucket: `ondevice-models`
2. Upload model files: `models/{model_id}/model.bin` (e.g. `lite/model.bin`, `standard/model.bin`, `multimodal/model.bin`, `max/model.bin`)
3. Create R2 API token with read access for signed URL generation
4. Add env vars: `R2_ACCOUNT_ID`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_BUCKET_NAME`, `R2_PUBLIC_DOMAIN`

**Acceptance**: Model files accessible via signed URL from R2; env vars set in `.env.local` and production

---

### ✅ T2-02 — Signed URL generation utility
**File**: `src/lib/models/signed-url.ts` (new)
**Change**: Utility that generates an R2 presigned URL:
```ts
export async function generateModelSignedUrl(
  modelId: string,
  deviceFingerprint: string,
  expirySeconds = 900 // 15 minutes
): Promise<string>
```
Uses AWS S3-compatible SDK (R2 is S3-compatible): `@aws-sdk/s3-request-presigner`

**Acceptance**: Returns valid signed URL; URL expires after 15 minutes; URL is device-fingerprint-scoped in metadata

---

### ✅ T2-03 — GET /api/models/[modelId]/download
**File**: `src/app/api/models/[modelId]/download/route.ts` (new)
**Logic**:
1. Read headers: `X-License-Key`, `X-Device-Fingerprint`, `X-Model-Token`
2. Validate HMAC token matches `license_key` + `device_fingerprint` (not expired)
3. Look up `device_licenses` — must be active and not revoked
4. Validate `modelId` is one of: `lite`, `standard`, `multimodal`, `max`
5. Call `generateModelSignedUrl(modelId, deviceFingerprint)`
6. Return `{ url: signedUrl, expires_at: ISO8601, model_id: modelId }`

**Error responses**:
- `401` — invalid or expired token
- `403` — license not found or revoked
- `404` — unknown model ID

**Acceptance**: Returns signed URL for valid license; URL works for 15 minutes; expired/invalid token rejected

---

### ✅ T2-04 — Model ID registry
**File**: `src/lib/models/registry.ts` (new)
**Change**: Typed registry of available models:
```ts
export const MODEL_REGISTRY = {
  lite:        { id: 'lite',        sizeBytes: 612270080,  displayName: 'Lite (584 MB)' },
  standard:    { id: 'standard',    sizeBytes: 1288490189, displayName: 'Standard (1.2 GB)' },
  multimodal:  { id: 'multimodal',  sizeBytes: 3972844134, displayName: 'Multimodal (3.7 GB)' },
  max:         { id: 'max',         sizeBytes: 5260827648, displayName: 'Max (4.9 GB)' },
} as const
export type ModelId = keyof typeof MODEL_REGISTRY
```
**Acceptance**: Import works, type-safe model ID validation in download endpoint

---

## Sprint 3 — UX + Documentation

### ✅ T3-01 — Download page: updated activation instructions
**File**: `src/app/download/page.tsx`
**Change**: After successful APK download, show activation flow:
- Step 1: "Download and install the APK" (existing)
- Step 2: "Open the app — it will activate your license automatically on first launch"
- Step 3: "Download your AI model inside the app (Wi-Fi recommended)"
- Remove any copy implying the APK alone gives full access
- Keep APK download button (app shell is still needed)

**Acceptance**: Instructions clearly explain two-step flow; no misleading "you're done" copy after APK download

---

### ✅ T3-02 — Post-purchase email: activation instructions
**File**: `src/lib/email/` (existing email templates)
**Change**: Update the post-purchase email body to explain:
- "Your APK download is ready — but the AI models are delivered inside the app"
- Clear step 1/2/3 install + activate + download model flow
- WhatsApp support link if activation fails

**Acceptance**: Email accurately describes new flow; no mention of "download and you're done"

---

### ✅ T3-03 — Android API contract documentation
**File**: `docs/api-contract-android.md` (new)
**Change**: Document the full API contract for the Android developer:
- All three endpoints with request/response shapes
- Header names (`X-License-Key`, `X-Device-Fingerprint`, `X-Model-Token`)
- Device fingerprint computation: `SHA-256(ANDROID_ID + Build.MODEL + Build.MANUFACTURER)`
- Error codes and recommended retry/fallback behaviour
- Grace period: 30 days offline (app enforces; server just returns `grace_period_days`)
- Token format (HMAC-SHA256, fields, expiry)
- Re-activation flow (same device = OK; factory reset = contact support)
- Recommended polling interval for `verify`: every 7 days when network available

**Acceptance**: Doc is self-contained; Android developer can implement without asking follow-up questions

---

### ✅ T3-04 — Admin: license revocation endpoint
**File**: `src/app/api/admin/license/revoke/route.ts` (new)
**Change**: Admin-only endpoint:
- `POST /api/admin/license/revoke { license_key }`
- Auth: `X-Admin-Secret` header (env var `ADMIN_SECRET`)
- Sets `revoked = true`, `revoked_at = now()` in `device_licenses`
- Next `verify` call from app returns `{ valid: false }`

**Acceptance**: Revoked license immediately returns `valid: false` on verify; requires correct admin secret

---

### ✅ T3-05 — Env var documentation
**File**: `.env.example` (update)
**Change**: Add new required env vars:
```
# License system
LICENSE_HMAC_SECRET=         # 32+ char random string for signing model download tokens

# Cloudflare R2 (model CDN)
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=ondevice-models
R2_PUBLIC_DOMAIN=            # e.g. https://models.on-device.org

# Admin
ADMIN_SECRET=                # For license revocation endpoint
```

**Acceptance**: All new env vars documented in `.env.example` with comments

---

### ✅ T3-06 (auto — schema export via export * from) — db/index.ts: export new table
**File**: `src/lib/db/index.ts`
**Change**: Export `deviceLicenses` alongside existing `orders` and `downloadTokens`
**Acceptance**: `import { deviceLicenses } from '@/lib/db'` works

---

## Summary

| Sprint | Tasks | Key Output |
|--------|-------|-----------|
| Sprint 1 | T1-01 to T1-05 | DB schema, activate API, verify API, rate limiting |
| Sprint 2 | T2-01 to T2-04 | R2 CDN setup, signed URL utility, model download API |
| Sprint 3 | T3-01 to T3-06 | UX updates, API contract doc, admin revocation, env vars |
| **Total** | **18 tasks** | **Full license system, web platform only** |

## Android App Implementation Notes (Deferred)

The following must be implemented in the Android app repo before the new system is active end-to-end. Document in `docs/api-contract-android.md`:

- On first launch: compute device fingerprint, call `/api/license/activate`, store `license_key` in Android Keystore
- Model download: call `/api/models/[modelId]/download`, download from signed URL, encrypt with Keystore key
- Periodic verify: call `/api/license/verify` every 7 days; cache result; allow 30-day offline grace
- If `verify` returns `valid: false`: show "License issue — contact support" screen (not a hard crash)
- RASP: root/emulator/debugger detection before any license operation
- APK self-signature check: verify own signing cert at runtime
- Certificate pinning: accept only `on-device.org` TLS cert on license API calls
