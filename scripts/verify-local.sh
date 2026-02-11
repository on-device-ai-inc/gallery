#!/bin/bash
set -e

echo "=== OnDevice AI Local Verification ==="
echo ""

echo "📝 Running ktlint..."
./gradlew ktlintCheck
echo "✅ Lint passed"
echo ""

echo "🧪 Running unit tests..."
./gradlew test
echo "✅ Tests passed"
echo ""

echo "=== All checks passed! ==="
echo "Ready to ship: /ship \"your message\""
