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

package ai.ondevice.app.ui.firstlaunch

import android.util.Log
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.ModelDownloadStatusType
import ai.ondevice.app.data.Task
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel

private const val TAG = "FirstLaunchManager"

/**
 * Handles first launch experience - checks if user needs to download a model
 */
object FirstLaunchManager {

    /**
     * Check if user needs first-launch model download
     * Returns the default model and task if needed
     */
    fun needsFirstLaunchSetup(
        modelManagerViewModel: ModelManagerViewModel
    ): Pair<Task, Model>? {
        val tasks = modelManagerViewModel.uiState.value.tasks
        val chatTask = tasks.find { it.id == BuiltInTaskId.LLM_CHAT } ?: return null

        // Check if user already has any downloaded models
        val hasDownloadedModel = chatTask.models.any { model ->
            val status = modelManagerViewModel.uiState.value.modelDownloadStatus[model.name]
            status?.status == ModelDownloadStatusType.SUCCEEDED
        }

        if (hasDownloadedModel) {
            Log.d(TAG, "User already has downloaded models")
            return null
        }

        // Find the best model for chat
        val defaultModel = chatTask.models.firstOrNull {
            it.bestForTaskIds.contains(BuiltInTaskId.LLM_CHAT)
        } ?: chatTask.models.firstOrNull()

        if (defaultModel == null) {
            Log.e(TAG, "No models available for chat task")
            return null
        }

        Log.d(TAG, "User needs to download model: ${defaultModel.name}")
        return Pair(chatTask, defaultModel)
    }
}
