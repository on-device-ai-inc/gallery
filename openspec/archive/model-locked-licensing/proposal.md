# Proposal: model-locked-licensing

## Summary

Replace the raw APK download with a model-locked license system: device-bound activation, server-controlled encrypted model delivery via signed CDN URLs, and a license verification API — making the app valueless without a valid server-issued license.

## Motivation

**Current vulnerability**: After payment, users receive a direct APK download link. The APK is self-contained. Anyone who obtains the file can sideload it on any device, share it freely, and the AI works fully with no server contact needed.

**Root cause**: The AI models are bundled in or freely downloadable alongside the APK. There is no device binding and no server dependency beyond the initial download.

**Solution**: Separate the APK (app shell — no IP value) from the AI model files (the actual product). The APK is freely distributable. The models are delivered only after server-verified, device-bound license activation. Even if the APK is shared, it cannot run AI without models signed to the original purchaser's device.

**Key insight**: The Android Keystore stores the model decryption key in hardware. Even on a rooted device the key cannot be extracted. Without the hardware-bound key, model files are useless ciphertext.

## Scope

### Included (web platform — this repo)
- `device_licenses` table in Drizzle schema
- `POST /api/license/activate` — order validation + device binding + license issuance
- `GET /api/license/verify` — periodic license check with offline grace period
- `GET /api/models/[modelId]/download` — signed CDN URL, device-bound, short-lived
- Model CDN setup (Cloudflare R2 recommended — zero egress cost)
- Download page: updated instructions (install APK → open app → auto-activates)
- Post-purchase email: updated to explain activation flow
- Admin: license revocation capability
- Rate limiting on activation endpoint
- Android app API contract documentation (contract only — no Android code)

### NOT Included (deferred — Android app repo)
- Android Keystore key generation and model encryption at rest
- RASP checks (root/emulator/debugger detection)
- Deep link handling (`ondeviceai://activate?token=XYZ`)
- Offline grace period enforcement (30-day timer logic lives in app)
- Certificate pinning
- APK signature self-check

### NOT Included (separate decision)
- Play Store distribution
- Transferring license to a new device (manual support flow for now)

## Acceptance Criteria

- [ ] `device_licenses` table created and migrated
- [ ] `POST /api/license/activate` validates paid order, creates device binding, returns `{ license_key, model_download_token }`
- [ ] Same device can re-activate (re-install) without buying again
- [ ] Different device activation on same order is rejected with `409 DEVICE_MISMATCH`
- [ ] `GET /api/license/verify` returns `{ valid: true }` for active license within grace period
- [ ] `GET /api/models/[modelId]/download` returns a signed URL valid for 15 minutes, single-use
- [ ] Model download endpoint rejects requests from unrecognised `device_fingerprint` + `license_key` pairs
- [ ] Activation endpoint rate-limited to 5 attempts per IP per hour
- [ ] Android API contract documented in `docs/api-contract-android.md`
- [ ] Download page updated with new activation flow instructions
- [ ] Post-purchase email updated to explain two-step: install → open → activated

## Technical Approach

### Device Fingerprint
The Android app computes:
```
device_fingerprint = SHA-256(ANDROID_ID + Build.MODEL + Build.MANUFACTURER)
```
Stored as a hex string. This persists across re-installs. Resets only on factory reset.

### License Activation Flow
```
App (first launch)
  → POST /api/license/activate { order_id, device_fingerprint }
  ← { license_key, model_download_token }

App
  → GET /api/models/[modelId]/download
    headers: { X-License-Key, X-Device-Fingerprint }
  ← { signed_url: "https://cdn.on-device.org/models/...?sig=...&exp=..." }

App
  → Download model from signed_url
  → Encrypt model with Android Keystore key
  → Store encrypted on device
```

### License Verification Flow (periodic, when network available)
```
App (background, every 7 days)
  → GET /api/license/verify
    headers: { X-License-Key, X-Device-Fingerprint }
  ← { valid: true, grace_period_days: 30 }

If no network → app uses cached valid status for up to 30 days
```

### Model CDN
- **Recommended**: Cloudflare R2 (zero egress fees, global CDN, supports signed URLs)
- Models stored as: `r2://models/{model_id}/{version}/model.bin`
- Signed URLs: HMAC-SHA256 signed, 15-minute expiry, `device_fingerprint` embedded in URL path so they cannot be shared
- Fallback: self-hosted on DGX Spark with Caddy serving model files

### Database Changes
New table `device_licenses` added alongside existing `orders` and `download_tokens`.

### Backward Compatibility
- Existing `downloadTokens` and APK download flow remain intact during transition
- Existing customers (pre-activation system) handled via a grace migration (can still download APK; prompted to activate on first app launch)

## References

- Existing schema: `src/lib/db/schema.ts`
- Existing download flow: `src/app/api/download/apk/route.ts`, `src/app/download/page.tsx`
- Payment status API: `src/app/api/payments/status/[orderId]/route.ts`
- Architecture decisions: conversation history, Mar 20 2026

---

## Completion Record

- **Archived**: 2026-03-20
- **Commits**: `006298f` (implementation), `67ac678` (proposal), `96f57f9` (prior sprint)
- **Tasks**: 17/18 code tasks complete — T2-01 (R2 bucket upload) deferred to infrastructure
- **Lessons Learned**: See below
