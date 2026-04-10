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

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.DataStoreRepository
import ai.ondevice.app.data.DefaultDataStoreRepository
import ai.ondevice.app.proto.AutoCleanup
import ai.ondevice.app.proto.TextSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

private const val TAG = "AGSettingsViewModel"

/**
 * Epic 5: Settings & Data Management ViewModel
 * Handles settings state and operations for Stories 5.1-5.4
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val conversationDao: ConversationDao
) : ViewModel() {

    sealed interface SettingsEvent {
        data class ShareConversations(val intent: Intent) : SettingsEvent
        data class ExportError(val message: String) : SettingsEvent
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>(replay = 1)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadSettings()
        loadStorageInfo()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val textSize = dataStoreRepository.readTextSize()
            val autoCleanup = dataStoreRepository.readAutoCleanup()
            val storageBudget = dataStoreRepository.readStorageBudget()
            val fullName = dataStoreRepository.readUserFullName()
            val nickname = dataStoreRepository.readUserNickname()

            _uiState.update {
                it.copy(
                    textSize = textSize,
                    autoCleanup = autoCleanup,
                    storageBudgetBytes = storageBudget,
                    userFullName = fullName,
                    userNickname = nickname
                )
            }
        }
    }

    private fun loadStorageInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val threads = conversationDao.getAllThreads()
                val totalMessages = conversationDao.getTotalMessageCount()
                val estimatedSize = conversationDao.getTotalEstimatedStorageBytes()

                _uiState.update {
                    it.copy(
                        conversationCount = threads.size,
                        totalMessageCount = totalMessages,
                        estimatedStorageBytes = estimatedSize
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error loading storage info", e)
            }
        }
    }

    // Story 5.4: Text Size Adjustment
    fun setTextSize(textSize: TextSize) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveTextSize(textSize)
            _uiState.update { it.copy(textSize = textSize) }
        }
    }

    // Story 5.3: Auto-Cleanup Configuration
    fun setAutoCleanup(autoCleanup: AutoCleanup) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveAutoCleanup(autoCleanup)
            _uiState.update { it.copy(autoCleanup = autoCleanup) }
        }
    }

    // Story 5.2: Storage Budget
    fun setStorageBudget(budgetBytes: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveStorageBudget(budgetBytes)
            _uiState.update { it.copy(storageBudgetBytes = budgetBytes) }
        }
    }

    // Story 5.3: Run cleanup based on current auto-cleanup setting
    fun runAutoCleanup() {
        viewModelScope.launch(Dispatchers.IO) {
            val autoCleanup = _uiState.value.autoCleanup
            if (autoCleanup == AutoCleanup.AUTO_CLEANUP_NEVER ||
                autoCleanup == AutoCleanup.AUTO_CLEANUP_UNSPECIFIED) {
                return@launch
            }

            val daysThreshold = when (autoCleanup) {
                AutoCleanup.AUTO_CLEANUP_30_DAYS -> 30L
                AutoCleanup.AUTO_CLEANUP_90_DAYS -> 90L
                AutoCleanup.AUTO_CLEANUP_1_YEAR -> 365L
                else -> return@launch
            }

            val cutoffTime = Instant.now().minus(daysThreshold, ChronoUnit.DAYS).toEpochMilli()
            val threads = conversationDao.getAllThreads()
            var deletedCount = 0

            for (thread in threads) {
                if (thread.updatedAt < cutoffTime && !thread.isStarred) {
                    conversationDao.deleteThread(thread.id)
                    deletedCount++
                }
            }

            if (deletedCount > 0) {
                Log.d(TAG, "Auto-cleanup: deleted $deletedCount old conversations")
                loadStorageInfo()
            }
        }
    }

    // User Profile

    fun updateUserFullName(fullName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserFullName(fullName)
            _uiState.update { it.copy(userFullName = fullName) }
        }
    }

    fun updateUserNickname(nickname: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserNickname(nickname)
            _uiState.update { it.copy(userNickname = nickname) }
        }
    }

    // Story 5.1: Export Conversations
    fun exportConversations(format: ExportFormat) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isExporting = true) }

                val threads = conversationDao.getAllThreads()
                val threadIds = threads.map { it.id }
                val allMessages = conversationDao.getMessagesForThreads(threadIds)
                val messagesByThread = allMessages.groupBy { it.threadId }

                val exportData = threads.map { thread ->
                    val messages = messagesByThread[thread.id].orEmpty()
                    ExportThread(
                        id = thread.id,
                        title = thread.title,
                        createdAt = thread.createdAt,
                        updatedAt = thread.updatedAt,
                        isStarred = thread.isStarred,
                        messages = messages.map { msg ->
                            ExportMessage(
                                role = if (msg.isUser) "user" else "assistant",
                                content = msg.content,
                                timestamp = msg.timestamp
                            )
                        }
                    )
                }

                val content = when (format) {
                    ExportFormat.JSON -> exportToJson(exportData)
                    ExportFormat.MARKDOWN -> exportToMarkdown(exportData)
                }

                val mimeType = when (format) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.MARKDOWN -> "text/markdown"
                }

                val fileName = when (format) {
                    ExportFormat.JSON -> "ondevice_conversations.json"
                    ExportFormat.MARKDOWN -> "ondevice_conversations.md"
                }

                // Build share intent and emit as event (no Context needed)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_TEXT, content)
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                }
                _events.emit(
                    SettingsEvent.ShareConversations(
                        Intent.createChooser(shareIntent, "Export Conversations")
                    )
                )

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportSuccess = true
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Export failed", e)
                _events.emit(SettingsEvent.ExportError(e.message ?: "Export failed"))
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportSuccess = false
                    )
                }
            }
        }
    }

    private fun exportToJson(data: List<ExportThread>): String {
        val json = Json { prettyPrint = true }
        return json.encodeToString(data)
    }

    private fun exportToMarkdown(data: List<ExportThread>): String {
        val sb = StringBuilder()
        sb.appendLine("# OnDevice Conversations Export")
        sb.appendLine()
        sb.appendLine("Exported: ${java.time.LocalDateTime.now()}")
        sb.appendLine()

        for (thread in data) {
            sb.appendLine("## ${thread.title}")
            sb.appendLine()
            sb.appendLine("*Created: ${formatTimestamp(thread.createdAt)} | Updated: ${formatTimestamp(thread.updatedAt)}*")
            if (thread.isStarred) {
                sb.appendLine("*⭐ Starred*")
            }
            sb.appendLine()

            for (message in thread.messages) {
                val rolePrefix = if (message.role == "user") "**You:**" else "**AI:**"
                sb.appendLine("$rolePrefix ${message.content}")
                sb.appendLine()
            }

            sb.appendLine("---")
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun formatTimestamp(epochMilli: Long): String {
        val instant = Instant.ofEpochMilli(epochMilli)
        val dateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        return dateTime.toString().replace("T", " ").substringBefore(".")
    }

    // Story 5.2: Get storage warning level
    fun getStorageWarningLevel(): StorageWarningLevel {
        val used = _uiState.value.estimatedStorageBytes
        val budget = _uiState.value.storageBudgetBytes
        if (budget <= 0) return StorageWarningLevel.NONE

        val usagePercent = (used.toDouble() / budget.toDouble()) * 100
        return when {
            usagePercent >= 95 -> StorageWarningLevel.CRITICAL
            usagePercent >= 80 -> StorageWarningLevel.WARNING
            else -> StorageWarningLevel.NONE
        }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(lastExportSuccess = null) }
    }

    // Story 5.1: Clear All Data (for Privacy Center)
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val threads = conversationDao.getAllThreads()
                for (thread in threads) {
                    conversationDao.deleteThread(thread.id)
                }
                Log.d(TAG, "Cleared all conversation data")
                loadStorageInfo()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error clearing data", e)
            }
        }
    }

    // Story 8: Expose DataStoreRepository for Custom Instructions Screen
    fun getDataStoreRepository(): DataStoreRepository = dataStoreRepository
}

data class SettingsUiState(
    val textSize: TextSize = TextSize.TEXT_SIZE_MEDIUM,
    val autoCleanup: AutoCleanup = AutoCleanup.AUTO_CLEANUP_NEVER,
    val storageBudgetBytes: Long = DefaultDataStoreRepository.DEFAULT_STORAGE_BUDGET_BYTES,
    val conversationCount: Int = 0,
    val totalMessageCount: Int = 0,
    val estimatedStorageBytes: Long = 0L,
    val isExporting: Boolean = false,
    val lastExportSuccess: Boolean? = null,
    val userFullName: String = "",
    val userNickname: String = ""
)

enum class ExportFormat {
    JSON, MARKDOWN
}

enum class StorageWarningLevel {
    NONE, WARNING, CRITICAL
}

@Serializable
data class ExportThread(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isStarred: Boolean,
    val messages: List<ExportMessage>
)

@Serializable
data class ExportMessage(
    val role: String,
    val content: String,
    val timestamp: Long
)
