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

package ai.ondevice.app.data

import androidx.datastore.core.DataStore
import ai.ondevice.app.proto.AccessTokenData
import ai.ondevice.app.proto.AutoCleanup
import ai.ondevice.app.proto.ImportedModel
import ai.ondevice.app.proto.Settings
import ai.ondevice.app.proto.TextSize
import ai.ondevice.app.proto.Theme
import ai.ondevice.app.proto.UserData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// TODO(b/423700720): Change to async (suspend) functions
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

/** Repository for managing data using Proto DataStore and EncryptedSharedPreferences. */
class DefaultDataStoreRepository(
  private val dataStore: DataStore<Settings>,
  private val userDataDataStore: DataStore<UserData>,
  private val secureTokenStorage: SecureTokenStorage,
) : DataStoreRepository {
  override fun saveTextInputHistory(history: List<String>) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().clearTextInputHistory().addAllTextInputHistory(history).build()
      }
    }
  }

  override fun readTextInputHistory(): List<String> {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.textInputHistoryList
    }
  }

  override fun saveTheme(theme: Theme) {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setTheme(theme).build() }
    }
  }

  override fun readTheme(): Theme {
    return runBlocking {
      val settings = dataStore.data.first()
      val curTheme = settings.theme
      // Use "auto" as the default theme.
      if (curTheme == Theme.THEME_UNSPECIFIED) Theme.THEME_AUTO else curTheme
    }
  }

  override fun saveAccessTokenData(accessToken: String, refreshToken: String, expiresAt: Long) {
    // Save to EncryptedSharedPreferences (primary storage)
    secureTokenStorage.saveTokens(accessToken, refreshToken, expiresAt)

    runBlocking {
      // Clear tokens from old Proto DataStore (migration cleanup)
      dataStore.updateData { settings ->
        settings.toBuilder().setAccessTokenData(AccessTokenData.getDefaultInstance()).build()
      }
      userDataDataStore.updateData { userData ->
        userData.toBuilder().clearAccessTokenData().build()
      }
    }
  }

  override fun clearAccessTokenData() {
    // Clear from EncryptedSharedPreferences (primary storage)
    secureTokenStorage.clearTokens()

    runBlocking {
      // Also clear from old Proto DataStore (migration cleanup)
      dataStore.updateData { settings -> settings.toBuilder().clearAccessTokenData().build() }
      userDataDataStore.updateData { userData ->
        userData.toBuilder().clearAccessTokenData().build()
      }
    }
  }

  override fun readAccessTokenData(): AccessTokenData? {
    // Try reading from EncryptedSharedPreferences first (primary storage)
    val secureTokens = secureTokenStorage.readTokens()
    if (secureTokens != null) {
      return secureTokens
    }

    // Migration: Check if tokens exist in old Proto DataStore
    return runBlocking {
      val userData = userDataDataStore.data.first()
      val protoTokens = userData.accessTokenData

      // If tokens exist in Proto DataStore, migrate to EncryptedSharedPreferences
      if (protoTokens != null && protoTokens.accessToken.isNotEmpty()) {
        secureTokenStorage.saveTokens(
          protoTokens.accessToken,
          protoTokens.refreshToken,
          protoTokens.expiresAtMs
        )
        // Clear from Proto DataStore after migration
        userDataDataStore.updateData { data ->
          data.toBuilder().clearAccessTokenData().build()
        }
        return@runBlocking protoTokens
      }

      null
    }
  }

  override fun saveImportedModels(importedModels: List<ImportedModel>) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().clearImportedModel().addAllImportedModel(importedModels).build()
      }
    }
  }

  override fun readImportedModels(): List<ImportedModel> {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.importedModelList
    }
  }

  override fun isTosAccepted(): Boolean {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.isTosAccepted
    }
  }

  override fun acceptTos() {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setIsTosAccepted(true).build() }
    }
  }

  // Self-hosted Gemma: Terms acceptance tracking implementation

  override fun isGemmaTermsAccepted(): Boolean {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.gemmaTermsAcceptedTimestamp > 0
    }
  }

  override fun acceptGemmaTerms() {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder()
          .setGemmaTermsAcceptedTimestamp(System.currentTimeMillis())
          .build()
      }
    }
  }

  override fun getGemmaTermsAcceptanceTimestamp(): Long {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.gemmaTermsAcceptedTimestamp
    }
  }

  // Epic 5: Settings & Data Management implementation

  override fun saveTextSize(textSize: TextSize) {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setTextSize(textSize).build() }
    }
  }

  override fun readTextSize(): TextSize {
    return runBlocking {
      val settings = dataStore.data.first()
      val textSize = settings.textSize
      // Default to MEDIUM if unspecified
      if (textSize == TextSize.TEXT_SIZE_UNSPECIFIED) TextSize.TEXT_SIZE_MEDIUM else textSize
    }
  }

  override fun saveAutoCleanup(autoCleanup: AutoCleanup) {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setAutoCleanup(autoCleanup).build() }
    }
  }

  override fun readAutoCleanup(): AutoCleanup {
    return runBlocking {
      val settings = dataStore.data.first()
      val autoCleanup = settings.autoCleanup
      // Default to NEVER if unspecified
      if (autoCleanup == AutoCleanup.AUTO_CLEANUP_UNSPECIFIED) AutoCleanup.AUTO_CLEANUP_NEVER else autoCleanup
    }
  }

  override fun saveStorageBudget(budgetBytes: Long) {
    runBlocking {
      dataStore.updateData { settings -> settings.toBuilder().setStorageBudgetBytes(budgetBytes).build() }
    }
  }

  override fun readStorageBudget(): Long {
    return runBlocking {
      val settings = dataStore.data.first()
      val budget = settings.storageBudgetBytes
      // Default to 4GB if not set (0)
      if (budget == 0L) DEFAULT_STORAGE_BUDGET_BYTES else budget
    }
  }

  override fun saveCustomInstructions(instructions: String) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().setCustomInstructions(instructions).build()
      }
    }
  }

  override fun readCustomInstructions(): String {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.customInstructions
    }
  }

  // User Profile implementation

  override fun saveUserFullName(fullName: String) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().setUserFullName(fullName).build()
      }
    }
  }

  override fun readUserFullName(): String {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.userFullName
    }
  }

  override fun saveUserNickname(nickname: String) {
    runBlocking {
      dataStore.updateData { settings ->
        settings.toBuilder().setUserNickname(nickname).build()
      }
    }
  }

  override fun readUserNickname(): String {
    return runBlocking {
      val settings = dataStore.data.first()
      settings.userNickname
    }
  }

  companion object {
    const val DEFAULT_STORAGE_BUDGET_BYTES = 4L * 1024 * 1024 * 1024  // 4GB
  }
}
