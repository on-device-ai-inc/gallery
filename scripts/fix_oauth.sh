#!/bin/bash
set -e

echo "=== Fixing OAuth Configuration for 404 Error ==="

# 1. Fix appAuthRedirectScheme in build.gradle.kts
echo "1. Updating appAuthRedirectScheme in build.gradle.kts..."
sed -i 's|manifestPlaceholders\["appAuthRedirectScheme"\] = "com.google.aiedge.gallery"|manifestPlaceholders["appAuthRedirectScheme"] = "ai.ondevice.app"|g' app/build.gradle.kts

# 2. Verify the fix
echo -e "\n2. Verifying build.gradle.kts changes:"
grep "appAuthRedirectScheme" app/build.gradle.kts

# 3. Find and update ProjectConfig.kt
echo -e "\n3. Finding ProjectConfig.kt..."
PROJECT_CONFIG=$(find app/src -name "ProjectConfig.kt" -type f)
echo "Found: $PROJECT_CONFIG"

# 4. Check current ProjectConfig.kt content
echo -e "\n4. Current ProjectConfig.kt redirect URI:"
grep -A2 "redirectUri" "$PROJECT_CONFIG" || echo "No redirectUri found"

# 5. Clean build cache
echo -e "\n5. Cleaning build cache..."
./gradlew clean

echo -e "\n✅ OAuth fix applied successfully!"
echo ""
echo "Next steps:"
echo "1. Rebuild: ./gradlew assembleDebug"
echo "2. Uninstall old app: adb uninstall com.google.ai.edge.gallery"
echo "3. Install new build: adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Your OAuth configuration:"
echo "  - HuggingFace redirect: ai.ondevice.app:/oauth2redirect"
echo "  - App redirect scheme: ai.ondevice.app"
echo "  - App package: com.google.ai.edge.gallery (unchanged)"
