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

import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ai.ondevice.app.data.Model
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Per-model storage breakdown with deletion and cache management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onNavigateBack: () -> Unit,
    modelManagerViewModel: ModelManagerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by modelManagerViewModel.uiState.collectAsState()

    var modelToDelete by remember { mutableStateOf<Model?>(null) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Calculate storage per model
    val modelStorage = remember(uiState.tasks) {
        uiState.tasks.flatMap { task ->
            task.models.mapNotNull { model ->
                val size = getModelStorageSize(context, model)
                if (size > 0) ModelStorageInfo(model, size) else null
            }
        }.sortedByDescending { it.sizeBytes }
    }

    val totalModelStorage = modelStorage.sumOf { it.sizeBytes }
    val deviceStats = remember { getDeviceStorageStats(context) }
    val cacheSize = remember { getCacheSize(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showClearCacheDialog = true }) {
                        Icon(Icons.Rounded.CleaningServices, "Clear cache")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage overview card
            item(key = "overview") {
                Spacer(Modifier.height(8.dp))
                StorageOverviewCard(
                    totalUsed = totalModelStorage,
                    deviceTotal = deviceStats.totalBytes,
                    deviceAvailable = deviceStats.availableBytes
                )
            }

            // Storage distribution chart
            if (modelStorage.isNotEmpty()) {
                item(key = "chart") {
                    StorageDistributionChart(
                        models = modelStorage,
                        totalSize = totalModelStorage
                    )
                }
            }

            // Cache section
            if (cacheSize > 0) {
                item(key = "cache") {
                    CacheCard(
                        size = cacheSize,
                        onClearCache = { showClearCacheDialog = true }
                    )
                }
            }

            // Per-model breakdown header
            item(key = "header") {
                Text(
                    text = "Downloaded Models",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Model list
            if (modelStorage.isEmpty()) {
                item(key = "empty") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No models downloaded",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(items = modelStorage, key = { it.model.name }) { modelInfo ->
                    ModelStorageItem(
                        modelInfo = modelInfo,
                        onDelete = { modelToDelete = it }
                    )
                }
            }

            item(key = "spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Delete confirmation dialog
    modelToDelete?.let { model ->
        AlertDialog(
            onDismissRequest = { modelToDelete = null },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete ${model.name}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This will permanently delete the model file from your device.")
                    Text(
                        "Size: ${formatBytes(getModelStorageSize(context, model))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val task = uiState.tasks.find { it.models.contains(model) }
                        if (task != null) {
                            modelManagerViewModel.deleteModel(task, model)
                        }
                        modelToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { modelToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear cache dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = { Icon(Icons.Rounded.CleaningServices, null) },
            title = { Text("Clear Cache?") },
            text = {
                Text("This will delete temporary files and download cache. Size: ${formatBytes(cacheSize)}")
            },
            confirmButton = {
                Button(onClick = {
                    clearCache(context)
                    showClearCacheDialog = false
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageOverviewCard(
    totalUsed: Long,
    deviceTotal: Long,
    deviceAvailable: Long
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Storage Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Models",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        formatBytes(totalUsed),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        formatBytes(deviceAvailable),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Device storage bar
            val usedPercent = (deviceTotal - deviceAvailable).toFloat() / deviceTotal
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { usedPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (usedPercent > 0.9f) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                )
                Text(
                    "${(usedPercent * 100).toInt()}% of device storage used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StorageDistributionChart(
    models: List<ModelStorageInfo>,
    totalSize: Long
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Storage Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                models.take(5).forEach { modelInfo ->
                    val percent = modelInfo.sizeBytes.toFloat() / totalSize
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percent)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Text(
                            "${(percent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                    Text(
                        "${modelInfo.model.name} - ${formatBytes(modelInfo.sizeBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CacheCard(size: Long, onClearCache: () -> Unit) {
    OutlinedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Cache & Temporary Files",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatBytes(size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onClearCache) {
                Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear")
            }
        }
    }
}

@Composable
private fun ModelStorageItem(
    modelInfo: ModelStorageInfo,
    onDelete: (Model) -> Unit
) {
    OutlinedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modelInfo.model.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatBytes(modelInfo.sizeBytes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (modelInfo.lastModified > 0) {
                    Text(
                        "Last used: ${formatDate(modelInfo.lastModified)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { onDelete(modelInfo.model) }) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete ${modelInfo.model.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Data classes and helpers
data class ModelStorageInfo(
    val model: Model,
    val sizeBytes: Long,
    val lastModified: Long = 0
)

data class DeviceStorageStats(
    val totalBytes: Long,
    val availableBytes: Long
)

private fun getModelStorageSize(context: android.content.Context, model: Model): Long {
    val externalFilesDir = context.getExternalFilesDir(null) ?: return 0
    val modelDir = File(externalFilesDir, model.normalizedName)
    return if (modelDir.exists()) {
        modelDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    } else {
        0L
    }
}

private fun getDeviceStorageStats(context: android.content.Context): DeviceStorageStats {
    val externalFilesDir = context.getExternalFilesDir(null) ?: return DeviceStorageStats(0, 0)
    val stat = StatFs(externalFilesDir.path)
    return DeviceStorageStats(
        totalBytes = stat.totalBytes,
        availableBytes = stat.availableBytes
    )
}

private fun getCacheSize(context: android.content.Context): Long {
    val cacheDir = context.cacheDir
    return if (cacheDir.exists()) {
        cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    } else {
        0L
    }
}

private fun clearCache(context: android.content.Context) {
    context.cacheDir.deleteRecursively()
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "%.2f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
