#!/bin/bash
set -e

echo "=== Updating ProjectConfig.kt with OAuth credentials ==="

# Read values from local.properties
CLIENT_ID=$(grep "hf.client.id" local.properties | cut -d'=' -f2)
REDIRECT_URI=$(grep "hf.redirect.uri" local.properties | cut -d'=' -f2)

echo "Client ID: $CLIENT_ID"
echo "Redirect URI: $REDIRECT_URI"

# Update ProjectConfig.kt
sed -i "s|const val clientId = \"REPLACE_WITH_YOUR_CLIENT_ID_IN_HUGGINGFACE_APP\"|const val clientId = \"$CLIENT_ID\"|g" app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt

sed -i "s|const val redirectUri = \"REPLACE_WITH_YOUR_REDIRECT_URI_IN_HUGGINGFACE_APP\"|const val redirectUri = \"$REDIRECT_URI\"|g" app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt

echo -e "\n✅ ProjectConfig.kt updated!"
echo -e "\nVerifying changes:"
grep -A1 "const val clientId" app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt
grep -A1 "const val redirectUri" app/src/main/java/com/google/ai/edge/gallery/common/ProjectConfig.kt
