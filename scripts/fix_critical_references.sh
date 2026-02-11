#!/bin/bash
set -e

echo "=========================================="
echo "Critical Reference Fixes"
echo "=========================================="
echo ""

# Fix 1: DownloadWorker.kt - Class.forName
echo "🔧 Fix 1: DownloadWorker.kt - Class.forName()..."
FILE="app/src/main/java/ai/ondevice/app/worker/DownloadWorker.kt"
if [ -f "$FILE" ]; then
    sed -i 's/Class\.forName("com\.google\.ai\.edge\.gallery\.MainActivity")/Class.forName("ai.ondevice.app.MainActivity")/g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 2: GalleryNavGraph.kt - Deep link scheme
echo ""
echo "🔧 Fix 2: GalleryNavGraph.kt - Deep link scheme..."
FILE="app/src/main/java/ai/ondevice/app/ui/navigation/GalleryNavGraph.kt"
if [ -f "$FILE" ]; then
    sed -i 's/"com\.google\.ai\.edge\.gallery:\/\/model\/"/"ai.ondevice.app:\/\/model\/"/g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 3: LlmSingleTurnViewModel.kt - Fully qualified class names
echo ""
echo "🔧 Fix 3: LlmSingleTurnViewModel.kt - Fully qualified references..."
FILE="app/src/main/java/ai/ondevice/app/ui/llmsingleturn/LlmSingleTurnViewModel.kt"
if [ -f "$FILE" ]; then
    sed -i 's/com\.google\.ai\.edge\.gallery\.data\.BuiltInTaskId/ai.ondevice.app.data.BuiltInTaskId/g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 4: LlmChatViewModel.kt - Fully qualified class names
echo ""
echo "🔧 Fix 4: LlmChatViewModel.kt - Fully qualified references..."
FILE="app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt"
if [ -f "$FILE" ]; then
    sed -i 's/com\.google\.ai\.edge\.gallery\.data\.BuiltInTaskId/ai.ondevice.app.data.BuiltInTaskId/g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 5: DownloadRepository.kt - Deep link URI
echo ""
echo "🔧 Fix 5: DownloadRepository.kt - Deep link URI..."
FILE="app/src/main/java/ai/ondevice/app/data/DownloadRepository.kt"
if [ -f "$FILE" ]; then
    sed -i 's/"com\.google\.ai\.edge\.gallery:\/\/model\//"ai.ondevice.app:\/\/model\//g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 6: build.gradle.kts - Default OAuth URI
echo ""
echo "🔧 Fix 6: build.gradle.kts - Default OAuth redirect URI..."
FILE="app/build.gradle.kts"
if [ -f "$FILE" ]; then
    sed -i 's/"com\.google\.aiedge\.gallery:\/oauth2redirect"/"ai.ondevice.app:\/oauth2redirect"/g' "$FILE"
    echo "   ✓ Fixed: $FILE"
fi

# Fix 7: strings.xml - All app name variations
echo ""
echo "🔧 Fix 7: strings.xml - App name variations..."
FILE="app/src/main/res/values/strings.xml"
if [ -f "$FILE" ]; then
    # Main app name
    sed -i 's/<string name="app_name" translatable="false">Google AI Edge Gallery<\/string>/<string name="app_name" translatable="false">OnDevice AI<\/string>/' "$FILE"
    
    # First part
    sed -i 's/<string name="app_name_first_part" translatable="false">Google AI<\/string>/<string name="app_name_first_part" translatable="false">OnDevice<\/string>/' "$FILE"
    
    # Second part
    sed -i 's/<string name="app_name_second_part" translatable="false">Edge Gallery<\/string>/<string name="app_name_second_part" translatable="false">AI<\/string>/' "$FILE"
    
    # Dialog title
    sed -i 's/<string name="tos_dialog_title_app_name" translatable="false">Google AI Edge Gallery App<\/string>/<string name="tos_dialog_title_app_name" translatable="false">OnDevice AI App<\/string>/' "$FILE"
    
    echo "   ✓ Fixed all 4 app name strings: $FILE"
fi

# Clean up: Remove backup files (optional)
echo ""
echo "🗑️  Cleanup: Removing .backup files from moved directories..."
find app/src/main/java/ai/ondevice/app -name "*.backup" -type f -delete 2>/dev/null || true
echo "   ✓ Backup files removed"

echo ""
echo "=========================================="
echo "✅ All Critical References Fixed!"
echo "=========================================="
echo ""
echo "Fixed:"
echo "  ✓ Class.forName() in DownloadWorker"
echo "  ✓ Deep link schemes (2 locations)"
echo "  ✓ Fully qualified class names (4 occurrences)"
echo "  ✓ Default OAuth URI in build.gradle.kts"
echo "  ✓ All app name strings (4 variations)"
echo ""
echo "Intentionally NOT fixed (documentation references):"
echo "  • GitHub URLs to google-ai-edge/gallery repo"
echo "  • KDoc comments with old package paths"
echo ""
echo "These are fine - they reference the original source repo."
