#!/bin/bash
set -e

# ============================================================================
# OnDevice AI - Monetization Preparation FIX
# ============================================================================
# This fixes the directory creation issue and completes the monetization prep
# ============================================================================

echo "🔧 OnDevice AI - Fixing Monetization Preparation"
echo "================================================"
echo ""

cd /home/nashie/Downloads/gallery-1.0.7/Android/src

PACKAGE_PATH="ai/ondevice/app"

# Create missing directories
echo "📁 Creating missing directories..."
mkdir -p "app/src/main/java/$PACKAGE_PATH/ui/settings"
mkdir -p "app/src/main/java/$PACKAGE_PATH/ui/home"
echo "✓ Directories created"
echo ""

# ============================================================================
# Create LicenseViewerScreen.kt
# ============================================================================

echo "📄 Creating LicenseViewerScreen.kt..."

cat > "app/src/main/java/$PACKAGE_PATH/ui/settings/LicenseViewerScreen.kt" << 'EOF'
package ai.ondevice.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseViewerScreen(
    licenseType: String,  // "LICENSE", "NOTICE", or "ATTRIBUTIONS"
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var licenseText by remember { mutableStateOf("Loading...") }
    
    LaunchedEffect(licenseType) {
        try {
            val filename = when (licenseType) {
                "LICENSE" -> "legal/LICENSE.txt"
                "NOTICE" -> "legal/NOTICE.txt"
                "ATTRIBUTIONS" -> "legal/ATTRIBUTIONS.txt"
                else -> "legal/LICENSE.txt"
            }
            licenseText = context.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            licenseText = "Error loading license: ${e.message}"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (licenseType) {
                        "LICENSE" -> "Open Source License"
                        "NOTICE" -> "Legal Notices"
                        "ATTRIBUTIONS" -> "Third-Party Attributions"
                        else -> "License"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = licenseText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
EOF

echo "✓ Created LicenseViewerScreen.kt"

# ============================================================================
# Create AboutSection.kt
# ============================================================================

echo "📄 Creating AboutSection.kt..."

cat > "app/src/main/java/$PACKAGE_PATH/ui/settings/AboutSection.kt" << 'EOF'
package ai.ondevice.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutSection(
    onOpenLicense: (String) -> Unit,
    onOpenWebsite: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "About OnDevice AI",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Version 1.0.7",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "OnDevice AI is built on open-source technology (Apache 2.0). " +
                    "Your purchase supports professional packaging, testing, updates, and customer support.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legal buttons
                OutlinedButton(
                    onClick = { onOpenLicense("LICENSE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Open Source License")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onOpenLicense("NOTICE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Legal Notices")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onOpenLicense("ATTRIBUTIONS") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Third-Party Attributions")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // External links
                TextButton(
                    onClick = { onOpenWebsite("https://github.com/google-ai-edge/gallery") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Original Source Code")
                }
                
                TextButton(
                    onClick = { onOpenWebsite("https://on-device-ai-inc.github.io/ondevice-legal-pages/terms.html") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terms of Service")
                }
                
                TextButton(
                    onClick = { onOpenWebsite("https://on-device-ai-inc.github.io/ondevice-legal-pages/privacy.html") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Privacy Policy")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Support: support@ondevice.ai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
EOF

echo "✓ Created AboutSection.kt"

# ============================================================================
# Verify TermsOfServiceDialog.kt exists
# ============================================================================

echo ""
echo "📄 Verifying TermsOfServiceDialog.kt..."

if [ -f "app/src/main/java/$PACKAGE_PATH/ui/home/TermsOfServiceDialog.kt" ]; then
    echo "✓ TermsOfServiceDialog.kt exists"
else
    echo "Creating TermsOfServiceDialog.kt..."
    
cat > "app/src/main/java/$PACKAGE_PATH/ui/home/TermsOfServiceDialog.kt" << 'EOF'
package ai.ondevice.app.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "OnDevice AI App Terms of Service",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "By using OnDevice AI, you accept our Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "OnDevice AI is built on open-source technology (Apache 2.0). " +
                    "Your purchase supports professional packaging, testing, updates, and customer support.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "AI Model Terms",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Some AI models have specific usage terms:\n\n" +
                    "• Gemma models: Subject to Google's Gemma Terms of Use and Prohibited Use Policy\n" +
                    "• Other models: Subject to their respective licenses\n\n" +
                    "By using these models, you agree to their terms.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://on-device-ai-inc.github.io/ondevice-legal-pages/terms.html"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Terms of Service")
                }
                
                TextButton(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://on-device-ai-inc.github.io/ondevice-legal-pages/privacy.html"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Privacy Policy")
                }
                
                TextButton(
                    onClick = { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.google.dev/gemma/terms"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Gemma Terms of Use")
                }
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Accept and Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
EOF
    
    echo "✓ Created TermsOfServiceDialog.kt"
fi

# ============================================================================
# Update strings.xml to remove LiteRT reference
# ============================================================================

echo ""
echo "🎨 Updating UI Text (Remove Google/LiteRT References)..."

STRINGS_FILE="app/src/main/res/values/strings.xml"

if [ -f "$STRINGS_FILE" ]; then
    # Check if already updated
    if grep -q "LiteRT community" "$STRINGS_FILE"; then
        # Backup original
        cp "$STRINGS_FILE" "${STRINGS_FILE}.backup"
        
        # Replace LiteRT community text
        sed -i 's/Explore a world of amazing on-device models from LiteRT community/Run powerful AI models directly on your device/g' "$STRINGS_FILE"
        
        # Verify
        if grep -q "LiteRT community" "$STRINGS_FILE"; then
            echo "⚠  LiteRT reference still present - may need manual update"
        else
            echo "✓ Removed LiteRT community reference from strings.xml"
        fi
    else
        echo "✓ strings.xml already updated (no LiteRT reference)"
    fi
else
    echo "⚠  strings.xml not found at expected location"
fi

# ============================================================================
# Create GitHub Pages directory
# ============================================================================

echo ""
echo "📄 Creating GitHub Pages Legal Documents..."

PAGES_DIR="$HOME/ondevice-legal-pages"
mkdir -p "$PAGES_DIR"

# Check if files already exist
if [ -f "$PAGES_DIR/index.html" ] && [ -f "$PAGES_DIR/terms.html" ] && [ -f "$PAGES_DIR/privacy.html" ]; then
    echo "✓ GitHub Pages files already exist in $PAGES_DIR"
else
    echo "✓ Created GitHub Pages directory: $PAGES_DIR"
    echo "  (Files will be created by main script or follow GitHub Pages guide)"
fi

echo ""
echo "✅ MONETIZATION PREPARATION COMPLETE!"
echo "===================================="
echo ""
echo "📁 Files Created:"
echo "  ✓ app/src/main/assets/legal/LICENSE.txt"
echo "  ✓ app/src/main/assets/legal/NOTICE.txt"
echo "  ✓ app/src/main/assets/legal/ATTRIBUTIONS.txt"
echo "  ✓ app/src/main/java/$PACKAGE_PATH/ui/home/TermsOfServiceDialog.kt"
echo "  ✓ app/src/main/java/$PACKAGE_PATH/ui/settings/LicenseViewerScreen.kt"
echo "  ✓ app/src/main/java/$PACKAGE_PATH/ui/settings/AboutSection.kt"
echo ""
echo "🚀 Next Steps:"
echo ""
echo "1. Check git status:"
echo "   git status"
echo ""
echo "2. Build and test:"
echo "   ./gradlew clean assembleDebug"
echo ""
echo "3. If build succeeds, commit:"
echo "   git add -A"
echo "   git commit -m 'Add monetization components'"
echo "   git push origin monetization-prep"
echo ""
echo "4. Follow the guides:"
echo "   - GITHUB_PAGES_SETUP_GUIDE.md (set up legal docs hosting)"
echo "   - INTEGRATION_GUIDE.md (integrate into your app)"
echo "   - PLAY_STORE_SUBMISSION.md (submit to Play Store)"
echo ""
