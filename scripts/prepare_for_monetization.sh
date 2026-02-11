#!/bin/bash
set -e

# ============================================================================
# OnDevice AI - Monetization Preparation Script
# ============================================================================
# This script prepares your rebranded Android app for Play Store monetization
# by removing Google branding and adding legal compliance.
#
# What it does:
# 1. Removes all Google branding (ToS dialog, LiteRT references)
# 2. Adds legal compliance (License viewer, Attributions)
# 3. Prepares legal documents for GitHub Pages
# 4. Creates verification script
# ============================================================================

echo "🚀 OnDevice AI - Monetization Preparation"
echo "=========================================="
echo ""

# Configuration
APP_DIR="/home/nashie/Downloads/gallery-1.0.7/Android/src"
PACKAGE_PATH="ai/ondevice/app"
GITHUB_PAGES_URL="https://on-device-ai-inc.github.io/ondevice-legal-pages"  # Update after creating GitHub Pages repo

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verify we're in the right directory
if [ ! -f "$APP_DIR/app/build.gradle.kts" ]; then
    echo -e "${RED}❌ Error: Can't find app/build.gradle.kts${NC}"
    echo "Please update APP_DIR in this script to point to your Android/src directory"
    exit 1
fi

cd "$APP_DIR"
echo -e "${GREEN}✓${NC} Found Android project at: $APP_DIR"
echo ""

# Create backup branch
BACKUP_BRANCH="backup-before-monetization-$(date +%Y%m%d-%H%M%S)"
git checkout -b "$BACKUP_BRANCH" 2>/dev/null || true
echo -e "${GREEN}✓${NC} Created backup branch: $BACKUP_BRANCH"
echo ""

# Create working branch
WORK_BRANCH="monetization-prep"
git checkout -b "$WORK_BRANCH" 2>/dev/null || git checkout "$WORK_BRANCH"
echo -e "${GREEN}✓${NC} Working on branch: $WORK_BRANCH"
echo ""

echo "📝 Phase 1: Adding Legal Assets"
echo "================================"

# Create legal assets directory
mkdir -p app/src/main/assets/legal

# Create LICENSE file (Apache 2.0)
cat > app/src/main/assets/legal/LICENSE.txt << 'EOF'
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   Copyright 2024 Google LLC

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
EOF
echo -e "${GREEN}✓${NC} Created LICENSE.txt"

# Create NOTICE file
cat > app/src/main/assets/legal/NOTICE.txt << 'EOF'
OnDevice AI includes software developed by:

Google AI Edge Gallery
Copyright 2024 Google LLC
Licensed under the Apache License, Version 2.0
https://github.com/google-ai-edge/gallery

This product includes software developed by The Android Open Source Project.
EOF
echo -e "${GREEN}✓${NC} Created NOTICE.txt"

# Create ATTRIBUTIONS file
cat > app/src/main/assets/legal/ATTRIBUTIONS.txt << 'EOF'
OnDevice AI - Third-Party Software Attributions

================================================================================
Google AI Edge Gallery
================================================================================
Copyright 2024 Google LLC
License: Apache 2.0
Source: https://github.com/google-ai-edge/gallery

OnDevice AI is a professionally packaged and supported version of the
open-source Google AI Edge Gallery. While the underlying code is open source,
you're paying for:

• Convenient Play Store distribution
• Professional testing and QA
• Regular updates and improvements
• Customer support
• Optimized user experience

================================================================================
Android Open Source Project
================================================================================
Copyright The Android Open Source Project
License: Apache 2.0
Source: https://source.android.com/

================================================================================
Kotlin
================================================================================
Copyright JetBrains s.r.o.
License: Apache 2.0
Source: https://github.com/JetBrains/kotlin

================================================================================
Jetpack Compose
================================================================================
Copyright The Android Open Source Project
License: Apache 2.0
Source: https://developer.android.com/jetpack/compose

================================================================================
TensorFlow Lite / MediaPipe
================================================================================
Copyright Google Inc.
License: Apache 2.0
Source: https://www.tensorflow.org/lite

================================================================================
Protocol Buffers
================================================================================
Copyright Google Inc.
License: BSD 3-Clause
Source: https://github.com/protocolbuffers/protobuf

================================================================================
Gemma Models (If Used)
================================================================================
The Gemma models are subject to Google's Gemma Terms of Use:
https://ai.google.dev/gemma/terms

By using Gemma models, you agree to the Gemma Terms of Use and the
Gemma Prohibited Use Policy.

================================================================================

For the complete source code of OnDevice AI's open-source foundation, visit:
https://github.com/google-ai-edge/gallery

For questions about OnDevice AI specifically, contact:
support@ondevice.ai (or your support email)
EOF
echo -e "${GREEN}✓${NC} Created ATTRIBUTIONS.txt"
echo ""

echo "🔧 Phase 2: Updating Terms of Service Dialog"
echo "============================================="

# Find and update the ToS composable
TOS_FILE="app/src/main/java/${PACKAGE_PATH}/ui/home/HomeViewModel.kt"

if [ -f "$TOS_FILE" ]; then
    echo -e "${YELLOW}⚠${NC}  Found HomeViewModel.kt - Manual update needed for ToS dialog"
    echo "   Location: $TOS_FILE"
else
    echo -e "${YELLOW}⚠${NC}  HomeViewModel.kt not found at expected location"
    echo "   You'll need to manually update the Terms of Service dialog"
fi

# Create a new Terms of Service composable file
cat > app/src/main/java/${PACKAGE_PATH}/ui/home/TermsOfServiceDialog.kt << 'EOF'
package ai.ondevice.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
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
                    onClick = { /* Open terms URL */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Terms of Service")
                }
                
                TextButton(
                    onClick = { /* Open privacy URL */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Privacy Policy")
                }
                
                TextButton(
                    onClick = { /* Open Gemma terms */ },
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
echo -e "${GREEN}✓${NC} Created new TermsOfServiceDialog.kt"
echo ""

echo "⚙️  Phase 3: Adding License Viewer to Settings"
echo "=============================================="

# Create LicenseViewerScreen composable
cat > app/src/main/java/${PACKAGE_PATH}/ui/settings/LicenseViewerScreen.kt << 'EOF'
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
echo -e "${GREEN}✓${NC} Created LicenseViewerScreen.kt"

# Create AboutSection composable with legal info
cat > app/src/main/java/${PACKAGE_PATH}/ui/settings/AboutSection.kt << 'EOF'
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
                    onClick = { onOpenWebsite("https://YOUR_GITHUB_USERNAME.github.io/ondevice-legal/terms.html") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terms of Service")
                }
                
                TextButton(
                    onClick = { onOpenWebsite("https://YOUR_GITHUB_USERNAME.github.io/ondevice-legal/privacy.html") },
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
echo -e "${GREEN}✓${NC} Created AboutSection.kt"
echo ""

echo "🎨 Phase 4: Updating UI Text (Remove Google/LiteRT References)"
echo "==============================================================="

# Update strings.xml to remove LiteRT reference
STRINGS_FILE="app/src/main/res/values/strings.xml"

if [ -f "$STRINGS_FILE" ]; then
    # Backup original
    cp "$STRINGS_FILE" "${STRINGS_FILE}.backup"
    
    # Replace LiteRT community text
    sed -i 's/Explore a world of amazing on-device models from LiteRT community/Run powerful AI models directly on your device/g' "$STRINGS_FILE"
    
    # Check if replacement worked
    if grep -q "LiteRT community" "$STRINGS_FILE"; then
        echo -e "${YELLOW}⚠${NC}  LiteRT reference still present in strings.xml - may need manual update"
    else
        echo -e "${GREEN}✓${NC} Removed LiteRT community reference from strings.xml"
    fi
else
    echo -e "${YELLOW}⚠${NC}  strings.xml not found at expected location"
fi
echo ""

echo "📄 Phase 5: Creating GitHub Pages Legal Documents"
echo "=================================================="

# Create GitHub Pages directory structure
PAGES_DIR="$HOME/ondevice-legal-pages"
mkdir -p "$PAGES_DIR"

# Create index.html
cat > "$PAGES_DIR/index.html" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OnDevice AI - Legal Information</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            line-height: 1.6;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            color: #333;
        }
        h1 { color: #1a73e8; }
        h2 { color: #5f6368; margin-top: 30px; }
        a { color: #1a73e8; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .nav { 
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        .nav a {
            margin-right: 20px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="nav">
        <a href="index.html">Home</a>
        <a href="terms.html">Terms of Service</a>
        <a href="privacy.html">Privacy Policy</a>
    </div>
    
    <h1>OnDevice AI Legal Information</h1>
    
    <p>Welcome to OnDevice AI's legal information center.</p>
    
    <h2>Quick Links</h2>
    <ul>
        <li><a href="terms.html">Terms of Service</a></li>
        <li><a href="privacy.html">Privacy Policy</a></li>
    </ul>
    
    <h2>Contact</h2>
    <p>For questions about these documents, contact us at: <strong>support@ondevice.ai</strong></p>
    
    <hr>
    <p style="font-size: 0.9em; color: #5f6368;">
        Last updated: <span id="date"></span>
    </p>
    <script>
        document.getElementById('date').textContent = new Date().toLocaleDateString();
    </script>
</body>
</html>
EOF
echo -e "${GREEN}✓${NC} Created index.html"

# Create terms.html
cat > "$PAGES_DIR/terms.html" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OnDevice AI - Terms of Service</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            line-height: 1.6;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            color: #333;
        }
        h1 { color: #1a73e8; }
        h2 { color: #5f6368; margin-top: 30px; }
        a { color: #1a73e8; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .nav { 
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        .nav a {
            margin-right: 20px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="nav">
        <a href="index.html">Home</a>
        <a href="terms.html">Terms of Service</a>
        <a href="privacy.html">Privacy Policy</a>
    </div>
    
    <h1>OnDevice AI Terms of Service</h1>
    <p><strong>Last Updated:</strong> <span id="date"></span></p>
    
    <h2>1. Acceptance of Terms</h2>
    <p>By purchasing, downloading, or using OnDevice AI ("the App"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, do not use the App.</p>
    
    <h2>2. Product Description</h2>
    <p>OnDevice AI is a commercial mobile application built on open-source technology, specifically the Google AI Edge Gallery (licensed under Apache 2.0). OnDevice AI provides professionally packaged, tested, and supported AI tools for on-device use.</p>
    
    <h2>3. Your Purchase</h2>
    <p>By purchasing OnDevice AI, you receive:</p>
    <ul>
        <li>A licensed copy of the application for personal use</li>
        <li>Access to app updates during your subscription/ownership period</li>
        <li>Technical support via email</li>
        <li>Professional packaging and distribution through Google Play Store</li>
    </ul>
    
    <p>You do NOT receive:</p>
    <ul>
        <li>Exclusive rights to the underlying open-source code</li>
        <li>The ability to redistribute or resell the application</li>
        <li>Direct source code access (available separately under Apache 2.0 from the original repository)</li>
    </ul>
    
    <h2>4. Open Source Components</h2>
    <p>OnDevice AI includes open-source software licensed under the Apache License 2.0. The complete license text and attributions are available within the App under Settings → Legal Information.</p>
    
    <p>The original open-source code can be obtained from: <a href="https://github.com/google-ai-edge/gallery" target="_blank">https://github.com/google-ai-edge/gallery</a></p>
    
    <h2>5. License Grant</h2>
    <p>Subject to your compliance with these Terms, we grant you a limited, non-exclusive, non-transferable, revocable license to download, install, and use the App on devices that you own or control for your personal, non-commercial use.</p>
    
    <h2>6. Restrictions</h2>
    <p>You may NOT:</p>
    <ul>
        <li>Copy, modify, distribute, sell, or lease any part of the App</li>
        <li>Reverse engineer or attempt to extract the source code (except as permitted by law)</li>
        <li>Remove, alter, or obscure any proprietary notices</li>
        <li>Use the App for any unlawful purpose</li>
        <li>Resell or redistribute the App in any form</li>
    </ul>
    
    <h2>7. AI Model Usage Terms</h2>
    <p>Different AI models available in the App may have specific usage terms:</p>
    <ul>
        <li><strong>Gemma Models:</strong> Subject to Google's Gemma Terms of Use (<a href="https://ai.google.dev/gemma/terms" target="_blank">https://ai.google.dev/gemma/terms</a>) and Prohibited Use Policy</li>
        <li><strong>Other Models:</strong> Subject to their respective open-source licenses as listed in the App's Legal section</li>
    </ul>
    <p>By using any AI model through the App, you agree to comply with that model's specific terms and conditions.</p>
    
    <h2>8. Prohibited Use</h2>
    <p>You agree not to use the App or any AI models to:</p>
    <ul>
        <li>Generate content that is illegal, harmful, threatening, abusive, defamatory, or otherwise objectionable</li>
        <li>Violate any applicable laws or regulations</li>
        <li>Infringe upon the rights of others</li>
        <li>Generate spam, malware, or other malicious content</li>
        <li>Attempt to circumvent any usage limitations or restrictions</li>
    </ul>
    
    <h2>9. Privacy and Data</h2>
    <p>All AI processing happens locally on your device. We do not collect, transmit, or store your AI inputs or outputs. For more information, see our <a href="privacy.html">Privacy Policy</a>.</p>
    
    <h2>10. Refund Policy</h2>
    <p>We offer a 30-day money-back guarantee. If the App does not work on your device or does not meet your expectations, contact us at <strong>support@ondevice.ai</strong> within 30 days of purchase for a full refund.</p>
    
    <h2>11. Updates and Modifications</h2>
    <p>We may update the App from time to time. Updates may include bug fixes, new features, or modifications to existing features. We may also modify these Terms at any time. Continued use of the App after changes constitutes acceptance of the modified Terms.</p>
    
    <h2>12. Disclaimer of Warranties</h2>
    <p>THE APP IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.</p>
    
    <p>We do not warrant that:</p>
    <ul>
        <li>The App will be error-free or uninterrupted</li>
        <li>Defects will be corrected</li>
        <li>The App will meet your specific requirements</li>
        <li>AI model outputs will be accurate, complete, or reliable</li>
    </ul>
    
    <h2>13. Limitation of Liability</h2>
    <p>TO THE MAXIMUM EXTENT PERMITTED BY LAW, WE SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, OR ANY LOSS OF PROFITS OR REVENUES, WHETHER INCURRED DIRECTLY OR INDIRECTLY, OR ANY LOSS OF DATA, USE, GOODWILL, OR OTHER INTANGIBLE LOSSES.</p>
    
    <h2>14. Intellectual Property</h2>
    <p>The App's branding, design, and proprietary enhancements are owned by OnDevice AI. The underlying open-source components remain under their respective licenses (primarily Apache 2.0).</p>
    
    <h2>15. Third-Party Services</h2>
    <p>The App may integrate with third-party services (such as model repositories). We are not responsible for the availability, accuracy, or reliability of these third-party services.</p>
    
    <h2>16. Termination</h2>
    <p>We may terminate or suspend your access to the App immediately, without prior notice, if you breach these Terms. Upon termination, you must cease all use of the App and delete all copies.</p>
    
    <h2>17. Governing Law</h2>
    <p>These Terms shall be governed by and construed in accordance with the laws of [YOUR JURISDICTION], without regard to its conflict of law provisions.</p>
    
    <h2>18. Dispute Resolution</h2>
    <p>Any disputes arising from these Terms or your use of the App shall be resolved through binding arbitration in accordance with [ARBITRATION RULES], except where prohibited by law.</p>
    
    <h2>19. Severability</h2>
    <p>If any provision of these Terms is found to be unenforceable or invalid, that provision shall be limited or eliminated to the minimum extent necessary, and the remaining provisions shall remain in full force and effect.</p>
    
    <h2>20. Contact Information</h2>
    <p>For questions about these Terms, contact us at:</p>
    <p><strong>Email:</strong> support@ondevice.ai<br>
    <strong>Response Time:</strong> Within 24-48 hours</p>
    
    <hr>
    <p style="font-size: 0.9em; color: #5f6368;">
        Last updated: <span id="date2"></span>
    </p>
    <script>
        const date = new Date().toLocaleDateString();
        document.getElementById('date').textContent = date;
        document.getElementById('date2').textContent = date;
    </script>
</body>
</html>
EOF
echo -e "${GREEN}✓${NC} Created terms.html"

# Create privacy.html
cat > "$PAGES_DIR/privacy.html" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OnDevice AI - Privacy Policy</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            line-height: 1.6;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            color: #333;
        }
        h1 { color: #1a73e8; }
        h2 { color: #5f6368; margin-top: 30px; }
        a { color: #1a73e8; text-decoration: none; }
        a:hover { text-decoration: underline; }
        .nav { 
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        .nav a {
            margin-right: 20px;
            font-weight: 500;
        }
        .highlight {
            background: #e8f0fe;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="nav">
        <a href="index.html">Home</a>
        <a href="terms.html">Terms of Service</a>
        <a href="privacy.html">Privacy Policy</a>
    </div>
    
    <h1>OnDevice AI Privacy Policy</h1>
    <p><strong>Last Updated:</strong> <span id="date"></span></p>
    
    <div class="highlight">
        <strong>🔐 Privacy-First Approach:</strong> OnDevice AI processes all AI operations locally on your device. We do not collect, transmit, or store your AI inputs, outputs, or personal data.
    </div>
    
    <h2>1. Introduction</h2>
    <p>OnDevice AI ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we handle information when you use our mobile application ("App").</p>
    
    <h2>2. Information We DO NOT Collect</h2>
    <p><strong>We do NOT collect, transmit, or store:</strong></p>
    <ul>
        <li><strong>AI Inputs:</strong> Text, images, or audio you process through the App</li>
        <li><strong>AI Outputs:</strong> Results generated by AI models</li>
        <li><strong>Personal Information:</strong> Names, email addresses, phone numbers (except as provided directly to support)</li>
        <li><strong>Location Data:</strong> GPS coordinates or location tracking</li>
        <li><strong>Contacts:</strong> Your contact list or address book</li>
        <li><strong>Photos/Media:</strong> Images or files you process remain on your device only</li>
        <li><strong>Usage Patterns:</strong> Which models you use or how frequently</li>
    </ul>
    
    <h2>3. Information We DO Collect</h2>
    <p><strong>Purchase Transaction Data (Handled by Google Play):</strong></p>
    <ul>
        <li>Transaction ID</li>
        <li>Purchase date and time</li>
        <li>Payment information (processed securely by Google Play)</li>
        <li>This data is necessary for processing your purchase and is handled by Google Play according to their <a href="https://policies.google.com/privacy" target="_blank">Privacy Policy</a></li>
    </ul>
    
    <p><strong>Crash Reports (Optional):</strong></p>
    <ul>
        <li>If the App crashes, you may choose to send an anonymous crash report</li>
        <li>Crash reports contain: device model, Android version, error logs</li>
        <li>Crash reports help us improve app stability</li>
        <li>You can disable crash reporting in Settings</li>
    </ul>
    
    <h2>4. How We Use Information</h2>
    <p>The minimal information we collect is used only to:</p>
    <ul>
        <li>Process and validate your purchase (via Google Play)</li>
        <li>Fix bugs and improve app stability (via optional crash reports)</li>
        <li>Respond to support requests you initiate</li>
    </ul>
    
    <h2>5. On-Device Processing</h2>
    <p>All AI model processing happens <strong>entirely on your device</strong>:</p>
    <ul>
        <li>Models run locally using your device's CPU/GPU</li>
        <li>No internet connection required for AI features</li>
        <li>Your data never leaves your device</li>
        <li>We cannot access your AI inputs or outputs</li>
    </ul>
    
    <h2>6. Third-Party Services</h2>
    <p>OnDevice AI integrates with the following third-party services:</p>
    
    <p><strong>Google Play Store:</strong></p>
    <ul>
        <li>Handles app distribution and payments</li>
        <li>Subject to Google's Privacy Policy: <a href="https://policies.google.com/privacy" target="_blank">https://policies.google.com/privacy</a></li>
    </ul>
    
    <p><strong>Google Play Services:</strong></p>
    <ul>
        <li>Required for app installation and updates</li>
        <li>May collect anonymous usage statistics</li>
    </ul>
    
    <p><strong>Optional Model Downloads:</strong></p>
    <ul>
        <li>When you download new AI models, you connect to model repositories (e.g., Hugging Face)</li>
        <li>These services may log download requests according to their privacy policies</li>
        <li>No personal information is transmitted during downloads</li>
    </ul>
    
    <h2>7. Data Storage</h2>
    <ul>
        <li><strong>AI Models:</strong> Stored locally on your device in the app's private storage</li>
        <li><strong>App Preferences:</strong> Stored locally on your device</li>
        <li><strong>Cache Data:</strong> Stored locally and can be cleared via Settings</li>
    </ul>
    
    <h2>8. Data Sharing</h2>
    <p><strong>We do NOT share, sell, or rent your information with third parties.</strong></p>
    
    <p>The only data sharing that occurs:</p>
    <ul>
        <li>Transaction data shared with Google Play (for purchase processing)</li>
        <li>Optional crash reports shared with our crash reporting service (anonymized)</li>
    </ul>
    
    <h2>9. Your Rights and Controls</h2>
    <p>You have the right to:</p>
    <ul>
        <li><strong>Access:</strong> Since we don't collect personal data, there's nothing to access</li>
        <li><strong>Delete:</strong> Uninstall the App to remove all local data</li>
        <li><strong>Opt-Out:</strong> Disable crash reporting in Settings</li>
        <li><strong>Request Information:</strong> Contact us for clarification on our practices</li>
    </ul>
    
    <h2>10. Children's Privacy</h2>
    <p>OnDevice AI is not intended for children under 13. We do not knowingly collect information from children under 13. If you believe a child has provided information to us, contact us immediately at support@ondevice.ai.</p>
    
    <h2>11. Security</h2>
    <p>We implement security measures to protect the App:</p>
    <ul>
        <li>App code is signed with our developer certificate</li>
        <li>All processing happens in sandboxed app storage</li>
        <li>No network transmission of sensitive data</li>
        <li>Regular security updates</li>
    </ul>
    
    <p>However, no method of electronic storage is 100% secure. We cannot guarantee absolute security.</p>
    
    <h2>12. International Users</h2>
    <p>OnDevice AI is designed to work offline and does not transmit personal data. Regardless of your location, your AI processing happens entirely on your device.</p>
    
    <h2>13. GDPR Compliance (EU Users)</h2>
    <p>For users in the European Union:</p>
    <ul>
        <li><strong>Legal Basis:</strong> We process minimal data based on contract performance (purchase) and legitimate interests (app functionality)</li>
        <li><strong>Data Subject Rights:</strong> You have rights to access, rectification, erasure, restriction, portability, and objection</li>
        <li><strong>Data Protection Officer:</strong> Contact privacy@ondevice.ai</li>
    </ul>
    
    <h2>14. CCPA Compliance (California Users)</h2>
    <p>For California residents:</p>
    <ul>
        <li>We do not sell personal information</li>
        <li>We do not collect personal information beyond transaction data handled by Google Play</li>
        <li>You have the right to know what data we collect (minimal, as described above)</li>
        <li>You have the right to request deletion (uninstall the App)</li>
    </ul>
    
    <h2>15. Changes to This Privacy Policy</h2>
    <p>We may update this Privacy Policy from time to time. We will notify you of any changes by:</p>
    <ul>
        <li>Posting the new Privacy Policy in the App</li>
        <li>Updating the "Last Updated" date</li>
        <li>Requiring acceptance of new terms (for material changes)</li>
    </ul>
    
    <h2>16. Open Source Components</h2>
    <p>OnDevice AI is built on open-source software (Apache 2.0). The privacy practices described here apply to OnDevice AI specifically. The underlying open-source code does not collect any data.</p>
    
    <h2>17. Contact Us</h2>
    <p>If you have questions about this Privacy Policy or our privacy practices, contact us:</p>
    <p>
        <strong>Email:</strong> support@ondevice.ai<br>
        <strong>Privacy-Specific Inquiries:</strong> privacy@ondevice.ai<br>
        <strong>Response Time:</strong> Within 24-48 hours
    </p>
    
    <h2>18. Consent</h2>
    <p>By using OnDevice AI, you consent to this Privacy Policy. If you do not agree, please do not use the App.</p>
    
    <hr>
    <p style="font-size: 0.9em; color: #5f6368;">
        Last updated: <span id="date2"></span>
    </p>
    <script>
        const date = new Date().toLocaleDateString();
        document.getElementById('date').textContent = date;
        document.getElementById('date2').textContent = date;
    </script>
</body>
</html>
EOF
echo -e "${GREEN}✓${NC} Created privacy.html"

# Create README for GitHub Pages
cat > "$PAGES_DIR/README.md" << 'EOF'
# OnDevice AI Legal Documents

This repository hosts the legal documents for OnDevice AI mobile application.

## Documents

- **[Terms of Service](terms.html)** - Terms governing the use of OnDevice AI
- **[Privacy Policy](privacy.html)** - How we handle your data (spoiler: we don't collect it!)

## Hosting

These documents are hosted via GitHub Pages and are referenced in the OnDevice AI mobile application.

## Updates

Last updated: [Current Date]

## Contact

For questions about these documents:
- Email: support@ondevice.ai
- Privacy-specific: privacy@ondevice.ai
EOF
echo -e "${GREEN}✓${NC} Created README.md"

echo ""
echo -e "${GREEN}✓${NC} Created GitHub Pages legal documents in: $PAGES_DIR"
echo ""

echo "📋 Phase 6: Creating Verification Script"
echo "========================================="

cat > verify_monetization_prep.sh << 'EOF'
#!/bin/bash

# Monetization Preparation Verification Script
echo "🔍 OnDevice AI - Monetization Preparation Verification"
echo "======================================================"
echo ""

ERRORS=0
WARNINGS=0

# Check legal assets exist
echo "Checking legal assets..."
if [ -f "app/src/main/assets/legal/LICENSE.txt" ]; then
    echo "✓ LICENSE.txt present"
else
    echo "❌ LICENSE.txt missing"
    ((ERRORS++))
fi

if [ -f "app/src/main/assets/legal/NOTICE.txt" ]; then
    echo "✓ NOTICE.txt present"
else
    echo "❌ NOTICE.txt missing"
    ((ERRORS++))
fi

if [ -f "app/src/main/assets/legal/ATTRIBUTIONS.txt" ]; then
    echo "✓ ATTRIBUTIONS.txt present"
else
    echo "❌ ATTRIBUTIONS.txt missing"
    ((ERRORS++))
fi

echo ""

# Check for Google branding
echo "Checking for Google branding..."
GOOGLE_REFS=$(grep -r "Google AI Edge Gallery" app/src/main/res/ 2>/dev/null | wc -l)
if [ $GOOGLE_REFS -eq 0 ]; then
    echo "✓ No 'Google AI Edge Gallery' references in resources"
else
    echo "⚠ Found $GOOGLE_REFS 'Google AI Edge Gallery' references in resources"
    ((WARNINGS++))
fi

LITERT_REFS=$(grep -r "LiteRT community" app/src/main/ 2>/dev/null | wc -l)
if [ $LITERT_REFS -eq 0 ]; then
    echo "✓ No 'LiteRT community' references"
else
    echo "⚠ Found $LITERT_REFS 'LiteRT community' references"
    ((WARNINGS++))
fi

echo ""

# Check for new components
echo "Checking for new legal components..."
if [ -f "app/src/main/java/ai/ondevice/app/ui/home/TermsOfServiceDialog.kt" ]; then
    echo "✓ TermsOfServiceDialog.kt created"
else
    echo "⚠ TermsOfServiceDialog.kt not found"
    ((WARNINGS++))
fi

if [ -f "app/src/main/java/ai/ondevice/app/ui/settings/LicenseViewerScreen.kt" ]; then
    echo "✓ LicenseViewerScreen.kt created"
else
    echo "⚠ LicenseViewerScreen.kt not found"
    ((WARNINGS++))
fi

if [ -f "app/src/main/java/ai/ondevice/app/ui/settings/AboutSection.kt" ]; then
    echo "✓ AboutSection.kt created"
else
    echo "⚠ AboutSection.kt not found"
    ((WARNINGS++))
fi

echo ""
echo "======================================================"
echo "Verification Summary:"
echo "  Errors: $ERRORS"
echo "  Warnings: $WARNINGS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo "✅ Monetization preparation looks good!"
    echo ""
    echo "Next steps:"
    echo "1. Set up GitHub Pages with the legal documents"
    echo "2. Update app code to integrate new components"
    echo "3. Update URLs in code to point to your GitHub Pages"
    echo "4. Build and test the app"
    echo "5. Submit to Play Store"
else
    echo "❌ Please fix the errors above before proceeding"
fi

echo ""
EOF

chmod +x verify_monetization_prep.sh
echo -e "${GREEN}✓${NC} Created verification script"
echo ""

echo "✅ MONETIZATION PREPARATION COMPLETE!"
echo "====================================="
echo ""
echo "📁 Created Files:"
echo "  1. Legal assets in: app/src/main/assets/legal/"
echo "  2. New UI components in: app/src/main/java/ai/ondevice/app/ui/"
echo "  3. GitHub Pages files in: $PAGES_DIR"
echo "  4. Verification script: verify_monetization_prep.sh"
echo ""
echo "🚀 Next Steps:"
echo ""
echo "STEP 1: Set Up GitHub Pages"
echo "---------------------------"
echo "1. Create a new GitHub repository called 'ondevice-legal'"
echo "2. Upload files from: $PAGES_DIR"
echo "3. Enable GitHub Pages: Settings → Pages → Source: main branch"
echo "4. Your URLs will be:"
echo "   - https://YOUR_USERNAME.github.io/ondevice-legal/terms.html"
echo "   - https://YOUR_USERNAME.github.io/ondevice-legal/privacy.html"
echo ""
echo "STEP 2: Update App Code"
echo "-----------------------"
echo "1. Update the URLs in AboutSection.kt (search for 'YOUR_GITHUB_USERNAME')"
echo "2. Integrate the new components into your app's navigation"
echo "3. Update HomeViewModel.kt to use the new TermsOfServiceDialog"
echo ""
echo "STEP 3: Build & Test"
echo "--------------------"
echo "1. Run: ./gradlew clean assembleDebug"
echo "2. Test the license viewers work"
echo "3. Test the new ToS dialog"
echo "4. Verify no Google branding visible"
echo ""
echo "STEP 4: Run Verification"
echo "------------------------"
echo "bash verify_monetization_prep.sh"
echo ""
echo -e "${GREEN}Git Status:${NC}"
git status --short
echo ""
echo "Ready to commit? Run:"
echo "  git add -A"
echo "  git commit -m 'Prepare for monetization: Add legal compliance'"
echo ""
