# Spec Delta: model-locked-licensing

**Status**: 🟡 AWAITING APPROVAL
**Tracks changes to**: DB schema, API routes, download UX, email templates, env config

---

## NEW FILES

| File | Purpose |
|------|---------|
| `src/app/api/license/activate/route.ts` | Device-bound license activation |
| `src/app/api/license/verify/route.ts` | Periodic license validity check |
| `src/app/api/models/[modelId]/download/route.ts` | Signed CDN URL for model delivery |
| `src/app/api/admin/license/revoke/route.ts` | Admin license revocation |
| `src/lib/models/signed-url.ts` | R2 presigned URL generation utility |
| `src/lib/models/registry.ts` | Typed model ID → metadata registry |
| `docs/api-contract-android.md` | Android app API integration contract |

---

## MODIFIED FILES

| File | Changes |
|------|---------|
| `src/lib/db/schema.ts` | Add `device_licenses` table |
| `src/lib/db/index.ts` | Export `deviceLicenses` |
| `drizzle/` | New migration for `device_licenses` |
| `src/app/download/page.tsx` | Activation flow instructions |
| `src/lib/email/` | Post-purchase email body updated |
| `.env.example` | New env vars: LICENSE_HMAC_SECRET, R2_*, ADMIN_SECRET |

---

## API Changes

### NEW: POST /api/license/activate

**Request**
```json
{
  "order_id": "clxxx...",
  "device_fingerprint": "a3f9c2..."  // SHA-256 hex
}
```

**Response 200 — new activation**
```json
{
  "license_key": "550e8400-e29b-41d4-a716-446655440000",
  "model_download_token": "eyJ...",   // HMAC-signed, 1h expiry
  "activated_at": "2026-03-20T14:00:00Z"
}
```

**Response 200 — same device reactivation**
```json
{
  "license_key": "550e8400-e29b-41d4-a716-446655440000",
  "model_download_token": "eyJ...",
  "reactivation_count": 2
}
```

**Response 409 — different device**
```json
{ "error": "DEVICE_MISMATCH", "message": "This order is already activated on a different device." }
```

**Response 402 — unpaid order**
```json
{ "error": "ORDER_NOT_PAID" }
```

**Response 429 — rate limited**
```json
{ "error": "RATE_LIMITED", "retry_after_seconds": 3600 }
```

---

### NEW: GET /api/license/verify

**Request headers**
```
X-License-Key: 550e8400-e29b-41d4-a716-446655440000
X-Device-Fingerprint: a3f9c2...
```

**Response 200 — valid**
```json
{
  "valid": true,
  "grace_period_days": 30
}
```

**Response 200 — invalid/revoked**
```json
{
  "valid": false,
  "reason": "REVOKED"   // or "NOT_FOUND"
}
```

---

### NEW: GET /api/models/[modelId]/download

**Request headers**
```
X-License-Key: 550e8400-e29b-41d4-a716-446655440000
X-Device-Fingerprint: a3f9c2...
X-Model-Token: eyJ...    // from activate response
```

**Valid modelId values**: `lite` | `standard` | `multimodal` | `max`

**Response 200**
```json
{
  "url": "https://models.on-device.org/lite/model.bin?X-Amz-Signature=...&X-Amz-Expires=900",
  "expires_at": "2026-03-20T14:15:00Z",
  "model_id": "lite"
}
```

**Response 401** — invalid/expired token
**Response 403** — license not found or revoked
**Response 404** — unknown model ID

---

### NEW: POST /api/admin/license/revoke

**Request headers**
```
X-Admin-Secret: <env:ADMIN_SECRET>
```

**Request body**
```json
{ "license_key": "550e8400-e29b-41d4-a716-446655440000" }
```

**Response 200**
```json
{ "revoked": true, "revoked_at": "2026-03-20T14:00:00Z" }
```

---

## Schema Changes

### ADDED: device_licenses table

```sql
CREATE TABLE device_licenses (
  id                  TEXT PRIMARY KEY,
  order_id            TEXT NOT NULL REFERENCES orders(id),
  device_fingerprint  TEXT NOT NULL,
  license_key         TEXT NOT NULL UNIQUE,
  activated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
  last_verified_at    TIMESTAMP,
  reactivation_count  INTEGER NOT NULL DEFAULT 0,
  revoked             BOOLEAN NOT NULL DEFAULT FALSE,
  revoked_at          TIMESTAMP,
  created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_device_licenses_order_id ON device_licenses(order_id);
CREATE INDEX idx_device_licenses_license_key ON device_licenses(license_key);
CREATE INDEX idx_device_licenses_fingerprint ON device_licenses(device_fingerprint);
```

### UNCHANGED: orders, download_tokens
Both tables remain as-is. The APK download flow via `download_tokens` continues to work for existing customers during migration.

---

## Environment Variables

### Added to .env.example

```bash
# License system
LICENSE_HMAC_SECRET=          # min 32 chars, random, used for model download token signing

# Cloudflare R2 (model CDN)
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=ondevice-models
R2_PUBLIC_DOMAIN=             # https://models.on-device.org

# Admin
ADMIN_SECRET=                 # For /api/admin/license/revoke
```

---

## UX Changes

### Download Page — After APK Download
```
BEFORE:
[Download APK] → "Your download has started."

AFTER:
[Download APK] → "Your download has started."
                 Step 1: Install the APK on your Android phone
                 Step 2: Open the app — your license activates automatically
                 Step 3: Download your AI model inside the app (Wi-Fi recommended for large files)
```

### Post-Purchase Email
```
BEFORE:
"Your download link is ready: [Download Now]"

AFTER:
"Your purchase is complete. Here's how to get started:
 1. Download and install the app: [Download APK]
 2. Open the app — it will verify your purchase automatically
 3. Download your AI model inside the app
 Need help? WhatsApp us: [link]"
```

---

## Exclusions (Do Not Touch)

- Stripe/M-Pesa payment flow — no changes
- `download_tokens` table — kept for backward compatibility
- Existing APK download endpoint (`/api/download/apk`) — kept unchanged
- Country pages (`/ke`, `/ng`, etc.) — no changes
- Any frontend checkout or buy pages — no changes
- Android app codebase — all Android changes are deferred

---

## Prerequisite (Before Sprint 2)

**R2 bucket must be created and models uploaded** before the model download endpoint can be tested end-to-end. This is an infrastructure task:

1. Create Cloudflare R2 bucket: `ondevice-models`
2. Upload: `models/lite/model.bin`, `models/standard/model.bin`, `models/multimodal/model.bin`, `models/max/model.bin`
3. Configure custom domain: `models.on-device.org` → R2 bucket
4. Set env vars in production
