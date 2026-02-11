#!/bin/bash
# find_rebrand_locations.sh - Detect all locations requiring rebrand updates
# Usage: bash find_rebrand_locations.sh <OLD_PACKAGE> <PROJECT_ROOT>

set -e

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <OLD_PACKAGE> <PROJECT_ROOT>"
    echo "Example: $0 com.google.ai.edge.gallery ."
    exit 1
fi

OLD_PACKAGE="$1"
PROJECT_ROOT="$2"

echo "=========================================="
echo "Android Rebrand Location Detector"
echo "=========================================="
echo "Searching for: $OLD_PACKAGE"
echo "In directory: $PROJECT_ROOT"
echo ""

# Check if directory exists
if [ ! -d "$PROJECT_ROOT" ]; then
    echo "Error: Directory $PROJECT_ROOT does not exist"
    exit 1
fi

cd "$PROJECT_ROOT"

echo "=== 1. Gradle Files ==="
grep -rn "$OLD_PACKAGE" --include="*.gradle" --include="*.gradle.kts" . 2>/dev/null | head -20 || echo "No matches found"

echo ""
echo "=== 2. AndroidManifest.xml ==="
find . -name "AndroidManifest.xml" -exec grep -Hn "$OLD_PACKAGE" {} \; 2>/dev/null || echo "No matches found"

echo ""
echo "=== 3. Kotlin/Java Source Files ==="
grep -rn "package $OLD_PACKAGE" --include="*.kt" --include="*.java" app/src/main/ 2>/dev/null | wc -l | xargs echo "Files with package declaration:"
grep -rn "import $OLD_PACKAGE" --include="*.kt" --include="*.java" app/src/main/ 2>/dev/null | wc -l | xargs echo "Files with imports:"

echo ""
echo "=== 4. XML Resource Files ==="
grep -rn "$OLD_PACKAGE" --include="*.xml" app/src/main/res/ 2>/dev/null | head -10 || echo "No matches found"

echo ""
echo "=== 5. OAuth/Config Files ==="
echo "Checking for OAuth redirect schemes..."
grep -rn "redirectUri\|appAuthRedirectScheme\|redirect_uri" --include="*.kt" --include="*.kts" --include="*.xml" app/ 2>/dev/null | grep -v "build/" || echo "No matches found"

echo ""
echo "=== 6. CI/CD Files ==="
if [ -d ".github/workflows" ]; then
    grep -rn "$OLD_PACKAGE\|oauth\|redirect" .github/workflows/ 2>/dev/null || echo "No matches found"
else
    echo "No .github/workflows directory found"
fi

echo ""
echo "=== 7. Properties Files ==="
if [ -f "local.properties" ]; then
    echo "local.properties found (check manually - may contain secrets)"
fi
if [ -f "gradle.properties" ]; then
    grep -n "$OLD_PACKAGE" gradle.properties 2>/dev/null || echo "No references in gradle.properties"
fi

echo ""
echo "=== 8. Directory Structure Check ==="
echo "Current package directory structure:"
find app/src/main/java -type d 2>/dev/null | head -10 || echo "No java directory found"
find app/src/main/kotlin -type d 2>/dev/null | head -10 || echo "No kotlin directory found"

echo ""
echo "=========================================="
echo "Detection Complete!"
echo "=========================================="
echo ""
echo "REBRAND SUMMARY:"
echo "  OLD Package: $OLD_PACKAGE"
echo "  NEW Package: ai.ondevice.app"
echo "  OLD App Name: Google AI Edge Gallery"
echo "  NEW App Name: OnDevice AI"
echo ""
echo "Next Steps:"
echo "1. Review all locations above"
echo "2. Open project in IntelliJ IDEA"
echo "3. Use Refactor → Rename on package structure"
echo "4. Manually update build.gradle.kts"
echo "5. Update strings.xml for app name"
echo "6. Run verification script"
