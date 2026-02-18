/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.ondevice.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel

/**
 * Privacy Center - Competitive Differentiator
 *
 * No cloud AI service offers this level of transparency about data processing.
 * This screen positions OnDevice AI as the privacy-first alternative.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onNavigateBack: () -> Unit,
    modelManagerViewModel: ModelManagerViewModel,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Center") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section: On-Device Processing
            PrivacyHeroCard()

            HorizontalDivider()

            // Processing Location
            ProcessingLocationSection()

            HorizontalDivider()

            // Data Retention Policy
            DataRetentionSection()

            HorizontalDivider()

            // Your Data Section
            YourDataSection(
                conversationCount = uiState.conversationCount,
                messageCount = uiState.totalMessageCount,
                storageUsed = uiState.estimatedStorageBytes,
                onExportClick = { showExportDialog = true },
                onClearAllClick = { showClearDataDialog = true }
            )

            HorizontalDivider()

            // Privacy Guarantees
            PrivacyGuaranteesSection()
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear All Data?") },
            text = {
                Text("This will permanently delete all conversations and messages. This action cannot be undone.\n\nConsider exporting your data first.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog (reuse existing)
    if (showExportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Conversations") },
            text = { Text("Your conversation data will be exported as JSON. No data is uploaded anywhere - the file stays on your device.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.exportConversations(context, ExportFormat.JSON)
                    showExportDialog = false
                }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PrivacyHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Illustration (using emoji for MVP)
            Text(
                text = "🔒",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Your Data Never Leaves\nYour Device",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Every conversation, every message, and every AI response is processed entirely on your phone. Zero cloud uploads. Zero tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProcessingLocationSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Processing Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        PrivacyFactCard(
            icon = Icons.Outlined.PhoneAndroid,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "On-Device Only",
            description = "All AI inference runs locally on your device's CPU/GPU. No servers involved.",
            badge = "✓ Verified"
        )

        PrivacyFactCard(
            icon = Icons.Outlined.CloudOff,
            iconTint = MaterialTheme.colorScheme.tertiary,
            title = "No Cloud Sync",
            description = "Your conversations are never uploaded to cloud storage or synchronized across devices.",
            badge = "✓ Verified"
        )

        PrivacyFactCard(
            icon = Icons.Outlined.WifiOff,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = "Works Offline",
            description = "Internet connection is only needed to download models. All chat features work with airplane mode enabled.",
            badge = "✓ Verified"
        )
    }
}

@Composable
private fun DataRetentionSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Data Retention Policy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Stored Forever (Unless You Delete)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Conversations remain on your device until you manually delete them. We don't auto-delete anything unless you enable auto-cleanup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Stored in: SQLite Database",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Conversations are saved in an encrypted SQLite database in your app's private storage. Other apps cannot access this data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YourDataSection(
    conversationCount: Int,
    messageCount: Int,
    storageUsed: Long,
    onExportClick: () -> Unit,
    onClearAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Your Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Data Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Conversations",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        conversationCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Messages",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        messageCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Storage Used",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        formatBytes(storageUsed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExportClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Export")
            }

            OutlinedButton(
                onClick = onClearAllClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Clear All")
            }
        }
    }
}

@Composable
private fun PrivacyGuaranteesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Privacy Guarantees",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrivacyGuaranteeItem("✓ No account required")
                PrivacyGuaranteeItem("✓ No analytics or tracking")
                PrivacyGuaranteeItem("✓ No advertising IDs collected")
                PrivacyGuaranteeItem("✓ No usage telemetry sent to servers")
                PrivacyGuaranteeItem("✓ Open-source AI models (Apache 2.0)")
                PrivacyGuaranteeItem("✓ You can delete everything anytime")
            }
        }
    }
}

@Composable
private fun PrivacyGuaranteeItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onTertiaryContainer
    )
}

@Composable
private fun PrivacyFactCard(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    badge: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function
private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
