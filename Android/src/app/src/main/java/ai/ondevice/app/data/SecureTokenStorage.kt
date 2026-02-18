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

package ai.ondevice.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import ai.ondevice.app.proto.AccessTokenData

/**
 * Secure storage for OAuth tokens using EncryptedSharedPreferences.
 *
 * Uses AES256-GCM hardware-backed encryption to protect access tokens,
 * refresh tokens, and token expiry times. Complies with Android security
 * best practices (2025) for sensitive credential storage.
 */
class SecureTokenStorage(context: Context) {
  private val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

  private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
    context,
    PREFS_FILENAME,
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  /**
   * Saves OAuth tokens securely.
   *
   * @param accessToken The OAuth access token
   * @param refreshToken The OAuth refresh token
   * @param expiresAt Token expiration timestamp in milliseconds
   */
  fun saveTokens(accessToken: String, refreshToken: String, expiresAt: Long) {
    sharedPreferences.edit()
      .putString(KEY_ACCESS_TOKEN, accessToken)
      .putString(KEY_REFRESH_TOKEN, refreshToken)
      .putLong(KEY_EXPIRES_AT, expiresAt)
      .apply()
  }

  /**
   * Reads OAuth tokens from secure storage.
   *
   * @return AccessTokenData if tokens exist, null otherwise
   */
  fun readTokens(): AccessTokenData? {
    val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, 0L)

    return if (accessToken != null && refreshToken != null && expiresAt > 0L) {
      AccessTokenData.newBuilder()
        .setAccessToken(accessToken)
        .setRefreshToken(refreshToken)
        .setExpiresAtMs(expiresAt)
        .build()
    } else {
      null
    }
  }

  /**
   * Clears all stored tokens.
   */
  fun clearTokens() {
    sharedPreferences.edit()
      .remove(KEY_ACCESS_TOKEN)
      .remove(KEY_REFRESH_TOKEN)
      .remove(KEY_EXPIRES_AT)
      .apply()
  }

  companion object {
    private const val PREFS_FILENAME = "secure_oauth_tokens"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EXPIRES_AT = "expires_at_ms"
  }
}
