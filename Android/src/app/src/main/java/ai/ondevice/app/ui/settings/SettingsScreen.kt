/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Modifications are proprietary and confidential.
 *
 * Originally Copyright 2025 Google LLC
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

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ai.ondevice.app.BuildConfig
import ai.ondevice.app.proto.AutoCleanup
import ai.ondevice.app.proto.TextSize
import ai.ondevice.app.proto.Theme
import ai.ondevice.app.ui.common.tos.TosDialog
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.theme.ThemeSettings
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

/**
 * Epic 5: Settings & Data Management Screen
 * Full settings screen implementing Stories 5.1-5.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modelManagerViewModel: ModelManagerViewModel,
    onNavigateToModelSelection: () -> Unit = {},
    onNavigateToCustomInstructions: () -> Unit = {},
    onNavigateToPrivacyCenter: () -> Unit = {},
    onNavigateToStorageManagement: () -> Unit = {},
    onNavigateToModelParameters: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var showTos by remember { mutableStateOf(false) }

    // Run auto-cleanup on screen load
    LaunchedEffect(Unit) {
        viewModel.runAutoCleanup()
    }

    // Observe one-shot events from ViewModel (export share intent, errors)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.SettingsEvent.ShareConversations -> {
                    context.startActivity(event.intent)
                }
                is SettingsViewModel.SettingsEvent.ExportError -> {
                    // Error state is already reflected in uiState.lastExportSuccess
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // App Info Section
            AppInfoSection()

            HorizontalDivider()

            // Profile Section
            ProfileSection(
                fullName = uiState.userFullName,
                nickname = uiState.userNickname,
                onFullNameChanged = viewModel::updateUserFullName,
                onNicknameChanged = viewModel::updateUserNickname
            )

            HorizontalDivider()

            // AI Settings Section - NEW
            AISettingsSection(
                onNavigateToCustomInstructions = onNavigateToCustomInstructions
            )

            HorizontalDivider()

            // Appearance Section
            AppearanceSection(
                currentTextSize = uiState.textSize,
                onTextSizeChanged = viewModel::setTextSize,
                modelManagerViewModel = modelManagerViewModel
            )

            HorizontalDivider()

            // Storage & Data Section
            StorageDataSection(
                uiState = uiState,
                warningLevel = viewModel.getStorageWarningLevel(),
                onExportClick = { showExportDialog = true },
                onAutoCleanupChanged = viewModel::setAutoCleanup,
                onNavigateToStorageManagement = onNavigateToStorageManagement
            )

            HorizontalDivider()

            // Model Manager Section
            ModelManagerSection(
                modelManagerViewModel = modelManagerViewModel,
                onNavigateToModelSelection = onNavigateToModelSelection,
                onNavigateToModelParameters = onNavigateToModelParameters
            )

            HorizontalDivider()

            // Privacy Section - NEW
            PrivacySection(
                onNavigateToPrivacyCenter = onNavigateToPrivacyCenter
            )

            HorizontalDivider()

            // Legal Section
            LegalSection(
                context = context,
                onShowTos = { showTos = true }
            )

            // Version info at bottom
            Text(
                text = "App version: ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                viewModel.exportConversations(format)
                showExportDialog = false
            }
        )
    }

    // ToS Dialog
    if (showTos) {
        TosDialog(
            onDismiss = { showTos = false },
            onAccept = { showTos = false }
        )
    }

    // Export status snackbar
    LaunchedEffect(uiState.lastExportSuccess) {
        uiState.lastExportSuccess?.let {
            // Show toast or snackbar
            viewModel.clearExportStatus()
        }
    }
}

@Composable
private fun AppInfoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "OnDevice AI",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Private & Local - All processing stays on your device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSection(
    currentTextSize: TextSize,
    onTextSizeChanged: (TextSize) -> Unit,
    modelManagerViewModel: ModelManagerViewModel
) {
    val context = LocalContext.current
    val currentTheme by ThemeSettings.themeOverride.collectAsState()
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Theme selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val themes = listOf(Theme.THEME_AUTO, Theme.THEME_LIGHT, Theme.THEME_DARK)
                themes.forEachIndexed { index, theme ->
                    SegmentedButton(
                        selected = selectedTheme == theme,
                        onClick = {
                            selectedTheme = theme
                            ThemeSettings.setTheme(theme)
                            modelManagerViewModel.saveThemeOverride(theme)

                            // Update UI mode for other activities
                            val uiModeManager = context.applicationContext
                                .getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                            when (theme) {
                                Theme.THEME_AUTO -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
                                Theme.THEME_LIGHT -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
                                Theme.THEME_DARK -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
                                else -> {}
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, themes.size)
                    ) {
                        Text(
                            when (theme) {
                                Theme.THEME_AUTO -> "Auto"
                                Theme.THEME_LIGHT -> "Light"
                                Theme.THEME_DARK -> "Dark"
                                else -> ""
                            }
                        )
                    }
                }
            }
        }

        // Story 5.4: Text Size selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Text Size",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val sizes = listOf(TextSize.TEXT_SIZE_SMALL, TextSize.TEXT_SIZE_MEDIUM, TextSize.TEXT_SIZE_LARGE)
                sizes.forEachIndexed { index, size ->
                    SegmentedButton(
                        selected = currentTextSize == size,
                        onClick = { onTextSizeChanged(size) },
                        shape = SegmentedButtonDefaults.itemShape(index, sizes.size)
                    ) {
                        Text(
                            when (size) {
                                TextSize.TEXT_SIZE_SMALL -> "Small"
                                TextSize.TEXT_SIZE_MEDIUM -> "Medium"
                                TextSize.TEXT_SIZE_LARGE -> "Large"
                                else -> ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageDataSection(
    uiState: SettingsUiState,
    warningLevel: StorageWarningLevel,
    onExportClick: () -> Unit,
    onAutoCleanupChanged: (AutoCleanup) -> Unit,
    onNavigateToStorageManagement: () -> Unit = {}
) {
    var expandedCleanup by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Storage & Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Storage info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (warningLevel) {
                    StorageWarningLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                    StorageWarningLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                    StorageWarningLevel.NONE -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storage Used",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatBytes(uiState.estimatedStorageBytes),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${uiState.conversationCount} conversations • ${uiState.totalMessageCount} messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Story 5.2: Storage warning
                if (warningLevel != StorageWarningLevel.NONE) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (warningLevel == StorageWarningLevel.CRITICAL)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (warningLevel == StorageWarningLevel.CRITICAL)
                                "Storage almost full! Consider exporting and deleting old chats."
                            else "Storage usage is high. Consider cleaning up old conversations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (warningLevel == StorageWarningLevel.CRITICAL)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // Story 5.1: Export button
        OutlinedButton(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Export Conversations")
        }

        // Story 5.3: Auto-cleanup dropdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Auto-delete old conversations",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Box {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCleanup = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(autoCleanupLabel(uiState.autoCleanup))
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedCleanup,
                    onDismissRequest = { expandedCleanup = false }
                ) {
                    val options = listOf(
                        AutoCleanup.AUTO_CLEANUP_NEVER,
                        AutoCleanup.AUTO_CLEANUP_30_DAYS,
                        AutoCleanup.AUTO_CLEANUP_90_DAYS,
                        AutoCleanup.AUTO_CLEANUP_1_YEAR
                    )
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(autoCleanupLabel(option)) },
                            onClick = {
                                onAutoCleanupChanged(option)
                                expandedCleanup = false
                            },
                            leadingIcon = if (option == uiState.autoCleanup) {
                                { Icon(Icons.Rounded.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }

            Text(
                text = "Starred conversations are never auto-deleted",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModelManagerSection(
    modelManagerViewModel: ModelManagerViewModel,
    onNavigateToModelSelection: () -> Unit,
    onNavigateToModelParameters: () -> Unit = {}
) {
    val uiState by modelManagerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var modelToDelete by remember { mutableStateOf<ai.ondevice.app.data.Model?>(null) }

    // Get all downloaded models from LLM Chat task
    val llmChatTask = remember(uiState.tasks) {
        uiState.tasks.find { it.id == ai.ondevice.app.data.BuiltInTaskId.LLM_CHAT }
    }

    val downloadedModels = remember(uiState.modelDownloadStatus, llmChatTask) {
        llmChatTask?.models?.filter { model ->
            uiState.modelDownloadStatus[model.name]?.status == ai.ondevice.app.data.ModelDownloadStatusType.SUCCEEDED
        } ?: emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Model Manager",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Manage your downloaded AI models",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Downloaded models list
        if (downloadedModels.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No models downloaded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            downloadedModels.forEach { model ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = model.displayName.ifEmpty { model.name },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            val sizeText = formatModelSize(model.sizeInBytes)
                            val capabilityText = when {
                                model.llmSupportImage && model.llmSupportAudio -> "Text, vision & voice"
                                model.llmSupportImage -> "Text & vision"
                                else -> "Text only"
                            }
                            Text(
                                text = "$capabilityText • $sizeText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                modelToDelete = model
                                showDeleteDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete model",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Download more models button
        OutlinedButton(
            onClick = onNavigateToModelSelection,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Rounded.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Download More Models")
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && modelToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                modelToDelete = null
            },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Model?") },
            text = {
                Text("Are you sure you want to delete ${modelToDelete?.displayName ?: modelToDelete?.name}? This will free up ${formatModelSize(modelToDelete?.sizeInBytes ?: 0L)} of storage.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        modelToDelete?.let { model ->
                            modelManagerViewModel.deleteModel(llmChatTask!!, model)
                        }
                        showDeleteDialog = false
                        modelToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        modelToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LegalSection(
    context: Context,
    onShowTos: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Legal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Third-party licenses
        OutlinedButton(
            onClick = {
                val intent = Intent(context, OssLicensesMenuActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Open Source Licenses")
        }

        // Terms of Service
        OutlinedButton(
            onClick = onShowTos,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Terms of Service")
        }
    }
}

@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Conversations") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose export format:")
                Text(
                    "Your conversations will be shared via the system share sheet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onExport(ExportFormat.MARKDOWN) }) {
                    Text("Markdown")
                }
                Button(onClick = { onExport(ExportFormat.JSON) }) {
                    Text("JSON")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun autoCleanupLabel(autoCleanup: AutoCleanup): String {
    return when (autoCleanup) {
        AutoCleanup.AUTO_CLEANUP_NEVER, AutoCleanup.AUTO_CLEANUP_UNSPECIFIED -> "Never"
        AutoCleanup.AUTO_CLEANUP_30_DAYS -> "After 30 days"
        AutoCleanup.AUTO_CLEANUP_90_DAYS -> "After 90 days"
        AutoCleanup.AUTO_CLEANUP_1_YEAR -> "After 1 year"
        else -> "Never"
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatModelSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)

    return when {
        gb >= 1.0 -> String.format("%.1fGB", gb)
        mb >= 1.0 -> String.format("%.0fMB", mb)
        else -> "${bytes}B"
    }
}

// NEW AI Settings Section
@Composable
private fun AISettingsSection(
    onNavigateToCustomInstructions: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "AI Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Customize how AI models behave and respond",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Custom Instructions
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToCustomInstructions)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Custom Instructions",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Set persistent instructions for all AI conversations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// NEW Privacy Section
@Composable
private fun PrivacySection(
    onNavigateToPrivacyCenter: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Privacy & Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Privacy Center
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToPrivacyCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Column {
                        Text(
                            text = "Privacy Center",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "View data processing, export your data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Story 10: Competitive comparison highlighting on-device advantages
        ai.ondevice.app.ui.common.CompetitiveComparisonCard(
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfileSection(
    fullName: String,
    nickname: String,
    onFullNameChanged: (String) -> Unit,
    onNicknameChanged: (String) -> Unit
) {
    // Separate UI state (TextFieldValue) from persisted state (String)
    // This preserves cursor position during editing
    var fullNameUI by remember(fullName) {
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(
            text = fullName,
            selection = androidx.compose.ui.text.TextRange(fullName.length)
        ))
    }

    var nicknameUI by remember(nickname) {
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(
            text = nickname,
            selection = androidx.compose.ui.text.TextRange(nickname.length)
        ))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Full Name Input
        OutlinedTextField(
            value = fullNameUI,
            onValueChange = { newValue ->
                fullNameUI = newValue
            },
            label = { Text("Full Name") },
            placeholder = { Text("John Doe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onNext = {
                    // Save when moving to next field
                    onFullNameChanged(fullNameUI.text)
                }
            )
        )

        // Nickname Input
        OutlinedTextField(
            value = nicknameUI,
            onValueChange = { newValue ->
                nicknameUI = newValue
            },
            label = { Text("Nickname") },
            placeholder = { Text("Johnny") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = {
                    // Save when pressing Done
                    onNicknameChanged(nicknameUI.text)
                    onFullNameChanged(fullNameUI.text) // Also save fullName in case user forgot to press Next
                }
            ),
            supportingText = {
                Text(
                    text = "We'll use your nickname in greetings",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}
