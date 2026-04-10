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

package ai.ondevice.app.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/**
 * Manages license activation and periodic verification.
 *
 * Activation flow:
 *   1. User purchases at on-device.org → receives email with deep link
 *      ai.ondevice.app://activate?order_id=<orderId>
 *   2. MainActivity extracts orderId and calls [activate]
 *   3. [activate] POSTs {order_id, device_fingerprint} to /api/license/activate
 *   4. On success, license_key is stored in EncryptedSharedPreferences
 *   5. On subsequent launches, [verify] confirms the license is still valid (log-only)
 *
 * All failures are log-only — the app never hard-blocks due to license issues.
 * This can be escalated to a soft-block UI in a future release.
 */
object LicenseManager {

    private const val TAG = "LicenseManager"
    private const val PREFS_NAME = "ondevice_license"
    private const val KEY_LICENSE_KEY = "lk"
    private const val KEY_ORDER_ID = "oid"
    private const val API_BASE = "https://on-device.org"
    private const val TIMEOUT_MS = 15_000

    /**
     * Returns a 64-char hex SHA-256 fingerprint unique to this device.
     * Combines ANDROID_ID (per-app-per-user install), hardware model, and manufacturer.
     * Matches the schema comment: SHA-256(ANDROID_ID+MODEL+MANUFACTURER).
     */
    fun getDeviceFingerprint(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val raw = "$androidId${Build.MODEL}${Build.MANUFACTURER}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Returns true if a license key is stored (device has been activated). */
    fun isActivated(context: Context): Boolean =
        getLicensePrefs(context).getString(KEY_LICENSE_KEY, null) != null

    /** Returns the stored license key, or null if not yet activated. */
    fun getLicenseKey(context: Context): String? =
        getLicensePrefs(context).getString(KEY_LICENSE_KEY, null)

    /**
     * Activates the license for the given [orderId].
     * Posts device fingerprint to /api/license/activate and stores the returned license_key.
     * Idempotent — safe to call again if the same device re-taps the activation link.
     *
     * @return Result.success(licenseKey) on activation, Result.failure on error.
     */
    suspend fun activate(context: Context, orderId: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val fingerprint = getDeviceFingerprint(context)
                val url = URL("$API_BASE/api/license/activate")
                val conn = url.openConnection() as HttpsURLConnection
                conn.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                val body = JSONObject().apply {
                    put("order_id", orderId)
                    put("device_fingerprint", fingerprint)
                }.toString().toByteArray(Charsets.UTF_8)

                conn.outputStream.use { it.write(body) }

                val code = conn.responseCode
                val responseText = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""

                if (code == 200) {
                    val licenseKey = JSONObject(responseText).getString("license_key")
                    getLicensePrefs(context).edit()
                        .putString(KEY_LICENSE_KEY, licenseKey)
                        .putString(KEY_ORDER_ID, orderId)
                        .apply()
                    Log.i(TAG, "License activated: order=$orderId")
                    Result.success(licenseKey)
                } else if (code == 409) {
                    // Device mismatch — same order already activated on a different device
                    val error = runCatching {
                        JSONObject(responseText).getString("error")
                    }.getOrDefault("DEVICE_MISMATCH")
                    Log.w(TAG, "License activation device mismatch (order=$orderId): $error")
                    Result.failure(Exception(error))
                } else {
                    val error = runCatching {
                        JSONObject(responseText).getString("error")
                    }.getOrDefault("HTTP_$code")
                    Log.w(TAG, "License activation failed (order=$orderId, code=$code): $error")
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "License activation exception (order=$orderId)", e)
                Result.failure(e)
            }
        }

    /**
     * Verifies the stored license key against the server.
     * Called on app startup. Fails open — network errors return true so the app keeps running.
     * Log-only mode: invalid/revoked licenses are logged but don't block the app.
     *
     * @return true if valid or if a network error occurred (fail-open), false if explicitly invalid.
     */
    suspend fun verify(context: Context): Boolean =
        withContext(Dispatchers.IO) {
            val licenseKey = getLicenseKey(context)
            if (licenseKey == null) {
                Log.i(TAG, "License verify: no license stored (not yet activated)")
                return@withContext true // not yet activated — fail-open
            }

            try {
                val fingerprint = getDeviceFingerprint(context)
                val url = URL("$API_BASE/api/license/verify")
                val conn = url.openConnection() as HttpsURLConnection
                conn.apply {
                    requestMethod = "GET"
                    setRequestProperty("x-license-key", licenseKey)
                    setRequestProperty("x-device-fingerprint", fingerprint)
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                }

                val code = conn.responseCode
                val responseText = conn.inputStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""

                val valid = code == 200 && JSONObject(responseText).optBoolean("valid", false)
                val reason = runCatching { JSONObject(responseText).optString("reason") }.getOrDefault("")

                if (valid) {
                    Log.i(TAG, "License verify: valid")
                } else {
                    Log.w(TAG, "License verify: invalid (code=$code, reason=$reason) — log-only, app continues")
                }
                valid
            } catch (e: Exception) {
                // Network unavailable, timeout, etc. — fail-open so offline users aren't blocked.
                Log.w(TAG, "License verify: network error (fail-open) — ${e.message}")
                true
            }
        }

    private fun getLicensePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
