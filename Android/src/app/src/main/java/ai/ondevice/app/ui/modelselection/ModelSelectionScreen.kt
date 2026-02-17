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

package ai.ondevice.app.ui.modelselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ai.ondevice.app.R
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Model
import ai.ondevice.app.ui.common.ClickableLink
import ai.ondevice.app.ui.common.MemoryWarningAlert
import ai.ondevice.app.ui.common.RevealingText
import ai.ondevice.app.ui.common.TaskIcon
import ai.ondevice.app.ui.common.getTaskBgColor
import ai.ondevice.app.ui.common.getTaskBgGradientColors
import ai.ondevice.app.ui.common.isMemoryLow
import ai.ondevice.app.ui.common.modelitem.ModelItem
import ai.ondevice.app.ui.common.rememberDelayedAnimationProgress
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.theme.bodyLargeNarrow
import ai.ondevice.app.ui.theme.headlineLargeMedium

private const val TAG = "AGModelSelectionScreen"
private val CONTENT_ANIMATION_OFFSET = 16.dp
private const val ANIMATION_INIT_DELAY = 80L
private const val TASK_DESCRIPTION_SECTION_ANIMATION_START = 400
private const val MODEL_LIST_ANIMATION_START = TASK_DESCRIPTION_SECTION_ANIMATION_START + 150
private const val DEFAULT_ANIMATION_DURATION = 700
private const val TASK_ICON_ANIMATION_DURATION = 1100

/**
 * First-launch model selection screen.
 * Uses the same design as ModelList for consistency.
 */
@Composable
fun ModelSelectionScreen(
    modelManagerViewModel: ModelManagerViewModel,
    onModelSelected: (Model) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by modelManagerViewModel.uiState.collectAsState()

    var showMemoryWarning by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf<Model?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val activity = androidx.activity.compose.LocalActivity.current

    // Handle back button during onboarding - show confirmation dialog
    BackHandler {
        showExitDialog = true
    }

    // Get LLM Chat task
    val llmChatTask = remember(uiState.tasks) {
        uiState.tasks.find { it.id == BuiltInTaskId.LLM_CHAT }
    }

    val availableModels = remember(llmChatTask) {
        llmChatTask?.models?.filter { !it.imported } ?: emptyList()
    }

    val listState = rememberLazyListState()

    val taskIconProgress = rememberDelayedAnimationProgress(
        initialDelay = ANIMATION_INIT_DELAY,
        animationDurationMs = TASK_ICON_ANIMATION_DURATION,
        animationLabel = "task icon",
    )

    val taskLabelProgress = rememberDelayedAnimationProgress(
        initialDelay = ANIMATION_INIT_DELAY + 300,
        animationDurationMs = TASK_ICON_ANIMATION_DURATION,
        animationLabel = "task label",
    )

    val descriptionProgress = rememberDelayedAnimationProgress(
        initialDelay = ANIMATION_INIT_DELAY + TASK_DESCRIPTION_SECTION_ANIMATION_START,
        animationDurationMs = DEFAULT_ANIMATION_DURATION,
        animationLabel = "description",
    )

    val modelListProgress = rememberDelayedAnimationProgress(
        initialDelay = ANIMATION_INIT_DELAY + MODEL_LIST_ANIMATION_START,
        animationDurationMs = DEFAULT_ANIMATION_DURATION,
        animationLabel = "model_list",
    )

    if (llmChatTask != null) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxSize()
                .background(color = getTaskBgColor(task = llmChatTask)),
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState,
            ) {
                // Task header area
                item(key = "taskHeader") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    ) {
                        // Task icon
                        TaskIcon(task = llmChatTask, width = 64.dp, animationProgress = taskIconProgress)

                        // Task name - Override for model selection screen
                        Box(modifier = Modifier.offset(x = (20f * (1f - taskIconProgress)).dp)) {
                            RevealingText(
                                text = "Select Your On-Device AI Model",
                                style = headlineLargeMedium.copy(
                                    brush = Brush.linearGradient(getTaskBgGradientColors(task = llmChatTask))
                                ),
                                textAlign = TextAlign.Center,
                                animationProgress = taskIconProgress,
                            )
                            RevealingText(
                                text = "Select Your On-Device AI Model",
                                style = headlineLargeMedium,
                                textAlign = TextAlign.Center,
                                animationProgress = taskLabelProgress,
                            )
                        }

                        // Description
                        Text(
                            llmChatTask.description,
                            textAlign = TextAlign.Center,
                            style = bodyLargeNarrow,
                            modifier = Modifier.graphicsLayer {
                                alpha = descriptionProgress
                                translationY = (CONTENT_ANIMATION_OFFSET * (1 - descriptionProgress)).toPx()
                            },
                        )

                        // API Documentation / Example code links
                        if (llmChatTask.docUrl.isNotEmpty() || llmChatTask.sourceCodeUrl.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .graphicsLayer {
                                        alpha = descriptionProgress
                                        translationY = (CONTENT_ANIMATION_OFFSET * (1 - descriptionProgress)).toPx()
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    if (llmChatTask.docUrl.isNotEmpty()) {
                                        ClickableLink(
                                            url = llmChatTask.docUrl,
                                            linkText = "API Documentation",
                                            icon = Icons.Outlined.Description,
                                        )
                                    }
                                    if (llmChatTask.sourceCodeUrl.isNotEmpty()) {
                                        ClickableLink(
                                            url = llmChatTask.sourceCodeUrl,
                                            linkText = "Example code",
                                            icon = Icons.Outlined.Code,
                                        )
                                    }
                                }
                            }
                        }

                        // Models available count with scroll hint
                        val resources = LocalContext.current.resources
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = descriptionProgress * 0.6f
                                    translationY = (CONTENT_ANIMATION_OFFSET * (1 - descriptionProgress)).toPx()
                                }
                        ) {
                            Text(
                                resources.getQuantityString(
                                    R.plurals.model_list_number_of_models_available,
                                    availableModels.size,
                                    availableModels.size,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.alpha(0.6f)
                            )
                            if (availableModels.size > 2) {
                                Text(
                                    "↓ Scroll to see all",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.alpha(0.8f)
                                )
                            }
                        }
                    }
                }

                // "Recommended models" section title
                if (availableModels.isNotEmpty()) {
                    item(key = "recommendedModelsTitle") {
                        Text(
                            stringResource(R.string.model_list_recommended_models_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .graphicsLayer {
                                    alpha = modelListProgress
                                    translationY = (CONTENT_ANIMATION_OFFSET * (1 - modelListProgress)).toPx()
                                },
                        )
                    }
                }

                // Model list
                items(items = availableModels, key = { it.name }) { model ->
                    ModelItem(
                        model = model,
                        task = llmChatTask,
                        modelManagerViewModel = modelManagerViewModel,
                        onModelClicked = {
                            // Check RAM before proceeding
                            if (isMemoryLow(context = context, model = model)) {
                                selectedModel = model
                                showMemoryWarning = true
                            } else {
                                onModelSelected(model)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = modelListProgress
                                translationY = (CONTENT_ANIMATION_OFFSET * (1 - modelListProgress)).toPx()
                            },
                    )
                }

                // Footer hint
                item(key = "footer") {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = modelListProgress
                            },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "You can change models later in Settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Memory warning dialog
    if (showMemoryWarning && selectedModel != null) {
        MemoryWarningAlert(
            onProceeded = {
                selectedModel?.let { onModelSelected(it) }
                showMemoryWarning = false
            },
            onDismissed = {
                showMemoryWarning = false
                selectedModel = null
            }
        )
    }

    // Exit confirmation dialog for back button during onboarding
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text("Exit onboarding?")
            },
            text = {
                Text("You haven't selected a model yet. Exit anyway?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("Continue")
                }
            }
        )
    }
}
