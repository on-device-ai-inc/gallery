#!/bin/bash
echo "=== OAuth Configuration Verification ==="
echo ""
echo "1. HuggingFace OAuth App:"
echo "   Go to: https://huggingface.co/settings/applications"
echo "   Verify Redirect URI: ai.ondevice.app:/oauth2redirect"
echo ""
echo "2. build.gradle.kts:"
grep "appAuthRedirectScheme" app/build.gradle.kts | sed 's/^/   /'
echo ""
echo "3. ProjectConfig.kt:"
grep "redirectUri" app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt | grep "const" | sed 's/^/   /'
echo ""
echo "4. local.properties:"
grep "hf.redirect.uri" local.properties | sed 's/^/   /'
echo ""
echo "✅ All THREE must show: ai.ondevice.app:/oauth2redirect"
echo "✅ Client ID must be the same in ProjectConfig.kt and local.properties"
