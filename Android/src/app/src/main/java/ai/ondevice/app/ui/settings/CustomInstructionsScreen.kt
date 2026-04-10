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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ai.ondevice.app.data.DataStoreRepository

/**
 * Custom Instructions Screen
 * Allows users to define persistent instructions that prepend to every AI prompt
 *
 * Use cases:
 * - Role-playing: "You are a creative writing assistant"
 * - Format preferences: "Always respond in bullet points"
 * - Tone control: "Use a casual, friendly tone"
 * - Domain expertise: "I'm a software engineer, use technical language"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInstructionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val dataStoreRepository = remember { viewModel.getDataStoreRepository() }
    var instructions by remember { mutableStateOf(dataStoreRepository.readCustomInstructions()) }
    var showPreview by remember { mutableStateOf(false) }
    var showExamples by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val originalInstructions = remember { dataStoreRepository.readCustomInstructions() }

    // Detect changes
    LaunchedEffect(instructions) {
        hasUnsavedChanges = instructions != originalInstructions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Instructions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (instructions.isNotEmpty()) {
                        IconButton(onClick = { showPreview = !showPreview }) {
                            Icon(
                                if (showPreview) Icons.Rounded.Edit else Icons.Rounded.Visibility,
                                if (showPreview) "Edit" else "Preview"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            instructions = ""
                            dataStoreRepository.saveCustomInstructions("")
                            hasUnsavedChanges = false
                        },
                        enabled = instructions.isNotEmpty()
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, "Clear instructions")
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = hasUnsavedChanges) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "You have unsaved changes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    instructions = originalInstructions
                                    hasUnsavedChanges = false
                                }
                            ) {
                                Text("Discard")
                            }
                            Button(
                                onClick = {
                                    dataStoreRepository.saveCustomInstructions(instructions)
                                    hasUnsavedChanges = false
                                }
                            ) {
                                Icon(Icons.Rounded.Save, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "What are custom instructions?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Instructions added here will be prepended to every conversation. Use this to set the AI's behavior, tone, or expertise level.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Examples section (collapsible)
            OutlinedCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Example Instructions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = { showExamples = !showExamples }) {
                            Icon(
                                if (showExamples) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                if (showExamples) "Hide examples" else "Show examples"
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showExamples,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExampleInstructionChip(
                                title = "Creative Writing Assistant",
                                example = "You are a creative writing coach. Help me develop characters, plot lines, and writing style. Be encouraging and constructive.",
                                onClick = { instructions = it }
                            )
                            ExampleInstructionChip(
                                title = "Technical Expert",
                                example = "I'm a software engineer. Use technical terminology, provide code examples, and explain architectural trade-offs.",
                                onClick = { instructions = it }
                            )
                            ExampleInstructionChip(
                                title = "Concise Responses",
                                example = "Keep responses brief and to the point. Use bullet points when listing multiple items. Avoid unnecessary explanations.",
                                onClick = { instructions = it }
                            )
                            ExampleInstructionChip(
                                title = "ELI5 (Explain Like I'm 5)",
                                example = "Explain concepts in simple terms that a beginner can understand. Avoid jargon and use analogies.",
                                onClick = { instructions = it }
                            )
                        }
                    }
                }
            }

            // Main input area
            if (!showPreview) {
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Your custom instructions") },
                    placeholder = {
                        Text(
                            "Example: You are a helpful coding assistant. Provide clear explanations and practical examples.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    minLines = 8,
                    maxLines = 12,
                    supportingText = {
                        Text("${instructions.length} characters")
                    }
                )
            } else {
                // Preview mode
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Visibility,
                                null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Preview: How the AI will see your message",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        HorizontalDivider()

                        // System instructions
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Settings,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "SYSTEM INSTRUCTIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    instructions,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Example user message
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Person,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "YOUR MESSAGE (example)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "What is the difference between async and await in JavaScript?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            "The AI will see your custom instructions before every message you send",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Privacy note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "Custom instructions are stored locally on your device and never leave your phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExampleInstructionChip(
    title: String,
    example: String,
    onClick: (String) -> Unit
) {
    OutlinedCard(
        onClick = { onClick(example) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Rounded.ChevronRight,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
