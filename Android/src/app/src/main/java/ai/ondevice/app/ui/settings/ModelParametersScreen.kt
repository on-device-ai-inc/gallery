/*
 * Copyright 2025 OnDevice Inc.
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.ondevice.app.data.Model

/**
 * Model Parameters Configuration Screen
 * Allows power users to fine-tune AI behavior with presets + custom controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelParametersScreen(
    model: Model,
    onNavigateBack: () -> Unit,
    onParametersChanged: (temperature: Float, topK: Int, topP: Float, maxTokens: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf(ModelPreset.BALANCED) }
    var temperature by remember { mutableStateOf(1.0f) }
    var topK by remember { mutableStateOf(64) }
    var topP by remember { mutableStateOf(0.95f) }
    var maxTokens by remember { mutableStateOf(2048) }

    var showParameterInfo by remember { mutableStateOf<ParameterInfo?>(null) }
    var showAdvancedControls by remember { mutableStateOf(false) }

    // Update parameters when preset changes
    LaunchedEffect(selectedPreset) {
        if (selectedPreset != ModelPreset.CUSTOM) {
            temperature = selectedPreset.temperature
            topK = selectedPreset.topK
            topP = selectedPreset.topP
            maxTokens = selectedPreset.maxTokens
            showAdvancedControls = false
        } else {
            showAdvancedControls = true
        }
    }

    // Detect if current values match a preset
    LaunchedEffect(temperature, topK, topP, maxTokens) {
        val matchingPreset = ModelPreset.fromValues(temperature, topK, topP, maxTokens)
        if (matchingPreset != null && selectedPreset != matchingPreset) {
            selectedPreset = matchingPreset
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Parameters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Reset to balanced defaults
                            selectedPreset = ModelPreset.BALANCED
                        }
                    ) {
                        Icon(Icons.Rounded.RestartAlt, "Reset to defaults")
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
            // Model info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure how the AI generates responses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Preset selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose a Preset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModelPreset.values().filter { it != ModelPreset.CUSTOM }.forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Preset description
                Text(
                    text = selectedPreset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Custom option
                FilterChip(
                    selected = selectedPreset == ModelPreset.CUSTOM,
                    onClick = { selectedPreset = ModelPreset.CUSTOM },
                    label = { Text("Custom - Manual Control") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // Advanced controls (shown when Custom selected)
            AnimatedVisibility(
                visible = showAdvancedControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Temperature slider
                    ParameterSlider(
                        label = "Temperature",
                        value = temperature,
                        range = 0.1f..2.0f,
                        steps = 38, // 0.05 increments
                        onValueChange = {
                            temperature = it
                            selectedPreset = ModelPreset.CUSTOM
                        },
                        onInfoClick = { showParameterInfo = ParameterExplanations.TEMPERATURE }
                    )

                    // Top-K slider
                    ParameterSlider(
                        label = "Top-K",
                        value = topK.toFloat(),
                        range = 10f..100f,
                        steps = 17, // increments of 5
                        onValueChange = {
                            topK = it.toInt()
                            selectedPreset = ModelPreset.CUSTOM
                        },
                        onInfoClick = { showParameterInfo = ParameterExplanations.TOP_K },
                        valueFormatter = { it.toInt().toString() }
                    )

                    // Top-P slider
                    ParameterSlider(
                        label = "Top-P",
                        value = topP,
                        range = 0.5f..1.0f,
                        steps = 49, // 0.01 increments
                        onValueChange = {
                            topP = it
                            selectedPreset = ModelPreset.CUSTOM
                        },
                        onInfoClick = { showParameterInfo = ParameterExplanations.TOP_P }
                    )

                    // Max Tokens slider
                    ParameterSlider(
                        label = "Max Tokens",
                        value = maxTokens.toFloat(),
                        range = 256f..4096f,
                        steps = 15, // 256 increments
                        onValueChange = {
                            maxTokens = (it / 256).toInt() * 256 // Round to 256
                            selectedPreset = ModelPreset.CUSTOM
                        },
                        onInfoClick = { showParameterInfo = ParameterExplanations.MAX_TOKENS },
                        valueFormatter = { it.toInt().toString() }
                    )
                }
            }

            // Apply button
            Button(
                onClick = {
                    onParametersChanged(temperature, topK, topP, maxTokens)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Changes")
            }
        }
    }

    // Parameter info dialog
    showParameterInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { showParameterInfo = null },
            icon = {
                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text(info.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider()

                    Text(
                        text = "Technical Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = info.technicalDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        text = "Examples",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = info.examples,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showParameterInfo = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onInfoClick: () -> Unit,
    valueFormatter: (Float) -> String = { "%.2f".format(it) }
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = "Info about $label",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
