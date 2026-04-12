/**
 * Current release APK metadata.
 * Updated automatically by CI on each release build.
 * See .github/workflows/build-apk.yml — "Update APK info" step.
 */
export const APK_INFO = {
  version: '1.0',
  /** SHA-256 of the release APK. Verify with: sha256sum OnDeviceAI.apk */
  sha256: '28a177fbd05e6af7c22cd727137f51c77f3ec310f67ecfacc9113e16e818dd5d',
  /** Approximate size shown to users */
  sizeMb: 153,
  /** Build date (ISO 8601) */
  builtAt: '2026-04-10',
} as const
