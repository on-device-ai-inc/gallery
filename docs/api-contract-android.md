# Android App — License API Contract

**Base URL**: `https://on-device.org`
**Audience**: Android app developer
**Status**: Stable — web platform endpoints are live

---

## Overview

The license system works as follows:

1. User pays on the website → gets an `order_id` (in the APK download email or deep link)
2. On first app launch, the app calls `/api/license/activate` with the `order_id` and a device fingerprint
3. Server returns a `license_key` and a short-lived `model_download_token`
4. App calls `/api/models/[modelId]/download` to get a signed CDN URL for each model
5. App downloads the model, encrypts it with a key from Android Keystore, stores it locally
6. Every 7 days (when network is available), app calls `/api/license/verify` to refresh its cached status
7. If offline, app uses cached status for up to 30 days (grace period)

---

## Device Fingerprint

Compute once on first launch, store persistently in SharedPreferences (encrypted):

```kotlin
import android.provider.Settings
import java.security.MessageDigest

fun computeDeviceFingerprint(context: Context): String {
    val androidId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown"
    val raw = "$androidId${Build.MODEL}${Build.MANUFACTURER}"
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }  // 64-char hex string
}
```

**Important:**
- `ANDROID_ID` persists across reinstalls but resets on factory reset
- Store the computed fingerprint; do not recompute each launch (Build values can vary across OS updates)
- Fingerprint must be exactly 64 hex characters (the server validates this)

---

## 1. POST /api/license/activate

Call this on **first launch only** (or after detecting the license is missing).

### Request

```
POST https://on-device.org/api/license/activate
Content-Type: application/json

{
  "order_id": "clxxx...",          // from deep link or user input
  "device_fingerprint": "a3f9c2..." // 64-char SHA-256 hex
}
```

### Response — New activation (200)

```json
{
  "license_key": "550e8400-e29b-41d4-a716-446655440000",
  "model_download_token": "eyJ...",
  "activated_at": "2026-03-20T14:00:00.000Z"
}
```

### Response — Re-installation on same phone (200)

```json
{
  "license_key": "550e8400-e29b-41d4-a716-446655440000",
  "model_download_token": "eyJ...",
  "reactivation_count": 2
}
```

### Error Responses

| Status | `error` field | Meaning | Recommended action |
|--------|--------------|---------|-------------------|
| 400 | `MISSING_ORDER_ID` | Body malformed | Show "Enter your order ID" prompt |
| 400 | `INVALID_DEVICE_FINGERPRINT` | Not 64-char hex | Fix fingerprint computation |
| 402 | `ORDER_NOT_PAID` | Payment not confirmed yet | Show "Payment pending" with retry |
| 404 | `ORDER_NOT_FOUND` | Wrong order ID | Show "Order not found" with support link |
| 409 | `DEVICE_MISMATCH` | Already activated on another device | Show support contact — do not allow activation |
| 429 | `RATE_LIMITED` | Too many attempts | Wait `retry_after_seconds`, then retry |

### Storage

Store in Android Keystore / EncryptedSharedPreferences:
- `license_key` — permanent credential
- `device_fingerprint` — permanent, computed once
- `model_download_token` — temporary (1h), refresh by calling activate again if expired

---

## 2. GET /api/license/verify

Call this **every 7 days** when network is available (background task/WorkManager).
Cache the result. Allow **30 days offline** before showing a warning.

### Request

```
GET https://on-device.org/api/license/verify
X-License-Key: 550e8400-e29b-41d4-a716-446655440000
X-Device-Fingerprint: a3f9c2...
```

### Response (200) — Valid

```json
{
  "valid": true,
  "grace_period_days": 30
}
```

### Response (200) — Invalid

```json
{
  "valid": false,
  "reason": "REVOKED"       // or "NOT_FOUND"
}
```

### Recommended behaviour

```
valid: true  → Cache result + timestamp. App runs normally.
valid: false → Show "License issue — contact support" screen. Do NOT hard crash.
Network error → Use cached result. If cached result is > 30 days old, show soft warning.
```

**Never block the app entirely due to a network failure.** The 30-day grace period is intentional for users in low-connectivity areas.

---

## 3. GET /api/models/[modelId]/download

Call this to get a signed CDN URL for downloading a model file.
The URL is valid for **15 minutes**. After expiry, call this endpoint again.

### Valid model IDs

| `modelId` | Size | Notes |
|-----------|------|-------|
| `lite` | 584 MB | Minimum 3GB RAM |
| `standard` | 1.2 GB | Balanced performance |
| `multimodal` | 3.7 GB | Recommended |
| `max` | 4.9 GB | Flagship phones only |

### Request

```
GET https://on-device.org/api/models/lite/download
X-License-Key: 550e8400-e29b-41d4-a716-446655440000
X-Device-Fingerprint: a3f9c2...
X-Model-Token: eyJ...    (from /activate response — valid for 1 hour)
```

### Response (200)

```json
{
  "url": "https://models.on-device.org/models/lite/model.bin?X-Amz-Signature=...&X-Amz-Expires=900",
  "expires_at": "2026-03-20T14:15:00.000Z",
  "model_id": "lite"
}
```

### Error Responses

| Status | Meaning | Action |
|--------|---------|--------|
| 401 | Token invalid/expired | Re-call `/activate` to get fresh token |
| 403 | License not found or revoked | Show support contact |
| 404 | Unknown `modelId` | Fix model ID string |
| 503 | CDN temporarily unavailable | Retry with exponential backoff |

### Model Download Flow

```kotlin
// 1. Get signed URL
val response = api.getModelDownloadUrl(modelId, licenseKey, deviceFingerprint, modelToken)

// 2. Download the raw bytes (standard HTTP GET — no auth headers needed)
val modelBytes = httpClient.get(response.url)

// 3. Generate or retrieve AES key from Android Keystore
val encryptionKey = keystore.getOrCreateKey("model_key_${modelId}")

// 4. Encrypt and write to internal storage
val encrypted = encrypt(modelBytes, encryptionKey)
File(context.filesDir, "${modelId}.enc").writeBytes(encrypted)

// 5. At model load time — decrypt from Keystore key
val decrypted = decrypt(File(context.filesDir, "${modelId}.enc").readBytes(), encryptionKey)
// Pass decrypted bytes to LiteRT interpreter
```

---

## 4. Order ID Delivery

The `order_id` is delivered to the user in two ways:

1. **Post-purchase email** — the email body contains the order ID and a download link
2. **Deep link (recommended UX)** — the download page can include a button:
   ```
   ondeviceai://activate?order_id=clxxx...
   ```
   The app should register an intent filter for this scheme and auto-populate the activation field.

---

## Security Recommendations (Android App Side)

These are deferred for final release hardening. Implement before production:

| Measure | Priority | Notes |
|---------|----------|-------|
| APK self-signature check | High | Compare `context.packageManager.getPackageInfo().signingInfo` against your release key hash |
| Root detection | High | Use RootBeer or manual checks (`/system/xbin/su`, `test-keys` in build tags) |
| Emulator detection | Medium | Check `Build.FINGERPRINT`, `Build.MODEL` for emulator strings |
| Debugger detection | Medium | `android.os.Debug.isDebuggerConnected()` |
| Certificate pinning on license API | High | Pin `on-device.org` TLS cert in OkHttp `CertificatePinner` |
| RASP — Frida detection | Low | Check for suspicious libraries in `/proc/self/maps` |

**Ordering**: Implement root + signature checks first. Run all RASP checks before calling `/api/license/activate`.

---

## Error Handling Summary

```
Network unavailable → use cached license state (30-day grace)
Token expired (401) → re-call /activate with stored order_id + fingerprint
License revoked (403) → show support screen, do not loop
Rate limited (429)   → honour retry_after_seconds
CDN unavailable (503) → retry with backoff, show progress to user
```

---

*Document version: 1.0 — March 2026*
*Web platform implementation: complete*
*Android implementation: deferred to release hardening sprint*
