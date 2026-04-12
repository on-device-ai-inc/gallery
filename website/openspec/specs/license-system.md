# Spec: License System

**Status**: Live
**Implemented**: 2026-03-20
**Source change**: `archive/model-locked-licensing`

---

## Architecture

The app is separated into two layers:

- **APK** (app shell) — freely distributable, no AI capability without models
- **AI models** (4.9 GB max) — delivered only after server-verified, device-bound license activation

This means sharing the APK gives nothing. Models are the product.

---

## Device Fingerprint

Computed by the Android app on first launch:

```
SHA-256(ANDROID_ID + Build.MODEL + Build.MANUFACTURER)
```

- Persists across reinstalls
- Resets only on factory reset
- Stored encrypted in EncryptedSharedPreferences

---

## API Endpoints

### POST /api/license/activate
- Validates order is paid
- Binds `device_fingerprint` to `order_id` (1:1)
- Same device → reactivation OK (re-installs supported)
- Different device → `409 DEVICE_MISMATCH`
- Returns `license_key` + short-lived `model_download_token`
- Rate limited: 5/IP/hour

### GET /api/license/verify
- Headers: `X-License-Key`, `X-Device-Fingerprint`
- Returns `{ valid: boolean, grace_period_days: 30 }`
- App polls every 7 days; caches result for 30-day offline grace

### GET /api/models/[modelId]/download
- Headers: `X-License-Key`, `X-Device-Fingerprint`, `X-Model-Token`
- Validates HMAC token (1h expiry) + active license
- Returns 15-minute R2 presigned URL
- Model IDs: `lite` | `standard` | `multimodal` | `max`

### POST /api/admin/license/revoke
- Header: `X-Admin-Secret`
- Sets `revoked = true` in `device_licenses`
- Next verify call from app returns `{ valid: false }`

---

## Database

Table: `device_licenses`

| Column | Type | Notes |
|--------|------|-------|
| id | text PK | cuid |
| order_id | text FK → orders | |
| device_fingerprint | text | SHA-256 hex, 64 chars |
| license_key | text UNIQUE | UUID v4 |
| activated_at | timestamp | |
| last_verified_at | timestamp | updated on each verify call |
| reactivation_count | integer | incremented on re-installs |
| revoked | boolean | default false |
| revoked_at | timestamp | nullable |

Indexes: `order_id`, `license_key`, `device_fingerprint`

---

## Token Format

`model_download_token` is an HMAC-SHA256 signed token:

```
base64url({ lk, df, exp }) + "." + hmacHex
```

- `lk` = license_key
- `df` = device_fingerprint
- `exp` = unix timestamp (now + 3600s)
- Secret: `LICENSE_HMAC_SECRET` env var

---

## Model CDN

- Provider: Cloudflare R2 (`ondevice-models` bucket)
- Storage path: `models/{modelId}/model.bin`
- Signed URLs: 15-minute expiry, S3-compatible presigning
- Env vars: `R2_ACCOUNT_ID`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_BUCKET_NAME`, `R2_PUBLIC_DOMAIN`

---

## Android App Contract

See `docs/api-contract-android.md` for the full integration spec including:
- Kotlin fingerprint computation
- Keystore model encryption flow
- Error handling table
- RASP hardening checklist (deferred to release sprint)
