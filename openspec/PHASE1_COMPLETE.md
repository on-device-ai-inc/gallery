# Step 0 — Baseline Complete

**Date**: 2026-02-17
**Status**: ✅ COMPLETE

## What Was Done

The codebase was established as a clean fork of Google AI Edge Gallery (Apache 2.0),
fully rebranded to OnDevice AI with no third-party branding references.

### Completed Work
- ✅ Package renamed: `ai.ondevice.app`
- ✅ All copyright headers updated: "OnDevice Inc."
- ✅ TOS dialog: "OnDevice AI Terms of Service" and "OnDevice AI Privacy Policy"
- ✅ Chat disclaimer: "OnDevice AI can make mistakes"
- ✅ Legal assets: terms.html, privacy.html in assets/legal/
- ✅ Support contact strings added
- ✅ Model allowlist bundled as asset fallback (3 models)
- ✅ CI/CD: GitHub Actions green, APK builds and installs on device
- ✅ Device verified: Samsung S22 Ultra (R3CT10HETMM)

### Codebase Metrics
- 132 Kotlin files
- Version: 1.1.9 (Build 35)
- Min SDK: 31 (Android 12)
- Package: `ai.ondevice.app`

## Next Step

Begin Phase 1 feature development per the OnDevice AI OpenSpec.
See `OnDeviceAI-OpenSpec/` for the full specification.
