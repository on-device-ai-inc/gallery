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

package ai.ondevice.app.data

import androidx.datastore.core.DataStore
import ai.ondevice.app.proto.AccessTokenData
import ai.ondevice.app.proto.AutoCleanup
import ai.ondevice.app.proto.ImportedModel
import ai.ondevice.app.proto.Settings
import ai.ondevice.app.proto.TextSize
import ai.ondevice.app.proto.Theme
import ai.ondevice.app.proto.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface DataStoreRepository {
  fun saveTextInputHistory(history: List<String>)

  fun readTextInputHistory(): List<String>

  fun saveTheme(theme: Theme)

  fun readTheme(): Theme

  fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long)

  fun clearAccessTokenData()

  fun readAccessTokenData(): AccessTokenData?

  fun saveImportedModels(importedModels: List<ImportedModel>)

  fun readImportedModels(): List<ImportedModel>

  fun isTosAccepted(): Boolean

  fun acceptTos()

  // Self-hosted Gemma: Terms acceptance tracking
  fun isGemmaTermsAccepted(): Boolean

  fun acceptGemmaTerms()

  fun getGemmaTermsAcceptanceTimestamp(): Long

  // Epic 5: Settings & Data Management

  // Story 5.4: Text size adjustment
  fun saveTextSize(textSize: TextSize)
  fun readTextSize(): TextSize

  // Story 5.3: Auto-cleanup configuration
  fun saveAutoCleanup(autoCleanup: AutoCleanup)
  fun readAutoCleanup(): AutoCleanup

  // Story 5.2: Storage budget
  fun saveStorageBudget(budgetBytes: Long)
  fun readStorageBudget(): Long

  // Story 8: Custom Instructions
  fun saveCustomInstructions(instructions: String)
  fun readCustomInstructions(): String

  // User Profile - Personalized greetings
  fun saveUserFullName(fullName: String)
  fun readUserFullName(): String
  fun saveUserNickname(nickname: String)
  fun readUserNickname(): String
}

/**
 * Repository for managing data using Proto DataStore and EncryptedSharedPreferences.
 *
 * ANR fix: All reads are served from in-memory [StateFlow] snapshots — O(1), never blocking the
 * calling thread. Writes are fire-and-forget on [Dispatchers.IO]. The StateFlow is populated
 * eagerly when this repository is created (via Hilt), so values are available almost immediately.
 * On the rare first read before disk-load completes, the DataStore default instance is returned
 * (empty/unspecified values), which are all handled safely by the enum defaulting below.
 */
class DefaultDataStoreRepository(
  private val dataStore: DataStore<Settings>,
  private val userDataDataStore: DataStore<UserData>,
  private val secureTokenStorage: SecureTokenStorage,
) : DataStoreRepository {

  // Single background scope for all async writes. SupervisorJob prevents one failure from
  // cancelling the scope, and Dispatchers.IO avoids any main-thread blocking.
  private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  // Eagerly-started StateFlow — subscribes to the DataStore immediately and caches the latest
  // value. All read methods below call .value (instant, non-blocking).
  private val settingsState: StateFlow<Settings> = dataStore.data
    .catch { emit(Settings.getDefaultInstance()) }
    .stateIn(repoScope, SharingStarted.Eagerly, Settings.getDefaultInstance())

  private val userDataState: StateFlow<UserData> = userDataDataStore.data
    .catch { emit(UserData.getDefaultInstance()) }
    .stateIn(repoScope, SharingStarted.Eagerly, UserData.getDefaultInstance())

  // ── Reads (all instant from in-memory StateFlow) ──────────────────────────

  override fun readTextInputHistory(): List<String> =
    settingsState.value.textInputHistoryList

  override fun readTheme(): Theme {
    val theme = settingsState.value.theme
    return if (theme == Theme.THEME_UNSPECIFIED) Theme.THEME_AUTO else theme
  }

  override fun readAccessTokenData(): AccessTokenData? {
    // Primary storage: EncryptedSharedPreferences (synchronous, non-blocking).
    val secure = secureTokenStorage.readTokens()
    if (secure != null) return secure

    // Migration path: tokens in old Proto DataStore. Read from cached StateFlow.
    val protoTokens = userDataState.value.accessTokenData
    if (protoTokens != null && protoTokens.accessToken.isNotEmpty()) {
      // Migrate to EncryptedSharedPreferences and clear old DataStore entry.
      secureTokenStorage.saveTokens(
        protoTokens.accessToken,
        protoTokens.refreshToken,
        protoTokens.expiresAtMs,
      )
      repoScope.launch {
        userDataDataStore.updateData { it.toBuilder().clearAccessTokenData().build() }
      }
      return protoTokens
    }
    return null
  }

  override fun readImportedModels(): List<ImportedModel> =
    settingsState.value.importedModelList

  override fun isTosAccepted(): Boolean =
    settingsState.value.isTosAccepted

  override fun isGemmaTermsAccepted(): Boolean =
    settingsState.value.gemmaTermsAcceptedTimestamp > 0

  override fun getGemmaTermsAcceptanceTimestamp(): Long =
    settingsState.value.gemmaTermsAcceptedTimestamp

  override fun readTextSize(): TextSize {
    val size = settingsState.value.textSize
    return if (size == TextSize.TEXT_SIZE_UNSPECIFIED) TextSize.TEXT_SIZE_MEDIUM else size
  }

  override fun readAutoCleanup(): AutoCleanup {
    val cleanup = settingsState.value.autoCleanup
    return if (cleanup == AutoCleanup.AUTO_CLEANUP_UNSPECIFIED) AutoCleanup.AUTO_CLEANUP_NEVER else cleanup
  }

  override fun readStorageBudget(): Long {
    val budget = settingsState.value.storageBudgetBytes
    return if (budget == 0L) DEFAULT_STORAGE_BUDGET_BYTES else budget
  }

  override fun readCustomInstructions(): String =
    settingsState.value.customInstructions

  override fun readUserFullName(): String =
    settingsState.value.userFullName

  override fun readUserNickname(): String =
    settingsState.value.userNickname

  // ── Writes (fire-and-forget on Dispatchers.IO) ───────────────────────────

  override fun saveTextInputHistory(history: List<String>) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().clearTextInputHistory().addAllTextInputHistory(history).build() }
    }
  }

  override fun saveTheme(theme: Theme) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setTheme(theme).build() }
    }
  }

  override fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long) {
    secureTokenStorage.saveTokens(accessToken, refreshToken, expiresAt)
    // Clear stale copies from old Proto DataStore locations.
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setAccessTokenData(AccessTokenData.getDefaultInstance()).build() }
      userDataDataStore.updateData { it.toBuilder().clearAccessTokenData().build() }
    }
  }

  override fun clearAccessTokenData() {
    secureTokenStorage.clearTokens()
    repoScope.launch {
      dataStore.updateData { it.toBuilder().clearAccessTokenData().build() }
      userDataDataStore.updateData { it.toBuilder().clearAccessTokenData().build() }
    }
  }

  override fun saveImportedModels(importedModels: List<ImportedModel>) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().clearImportedModel().addAllImportedModel(importedModels).build() }
    }
  }

  override fun acceptTos() {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setIsTosAccepted(true).build() }
    }
  }

  override fun acceptGemmaTerms() {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setGemmaTermsAcceptedTimestamp(System.currentTimeMillis()).build() }
    }
  }

  override fun saveTextSize(textSize: TextSize) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setTextSize(textSize).build() }
    }
  }

  override fun saveAutoCleanup(autoCleanup: AutoCleanup) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setAutoCleanup(autoCleanup).build() }
    }
  }

  override fun saveStorageBudget(budgetBytes: Long) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setStorageBudgetBytes(budgetBytes).build() }
    }
  }

  override fun saveCustomInstructions(instructions: String) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setCustomInstructions(instructions).build() }
    }
  }

  override fun saveUserFullName(fullName: String) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setUserFullName(fullName).build() }
    }
  }

  override fun saveUserNickname(nickname: String) {
    repoScope.launch {
      dataStore.updateData { it.toBuilder().setUserNickname(nickname).build() }
    }
  }

  companion object {
    const val DEFAULT_STORAGE_BUDGET_BYTES = 4L * 1024 * 1024 * 1024  // 4GB
  }
}
