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

package ai.ondevice.app.helper

import ai.ondevice.app.data.Model
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.StatFs

/**
 * Pre-download validation for model downloads.
 * Checks WiFi, storage space, and network availability before
 * allowing the download to proceed via DownloadWorker.
 *
 * Note: actual downloading is handled by DownloadWorker/DownloadRepository.
 * This class only validates preconditions.
 */
class SecureModelDownloader(private val context: Context) {

    companion object {
        private const val STORAGE_BUFFER_PERCENTAGE = 0.20
    }

    /**
     * Validates if download can proceed.
     * Returns error message if validation fails, null if OK.
     */
    fun validateDownloadConditions(model: Model): String? {
        if (model.requiresWifi && !isOnWifi()) {
            return "This model requires WiFi. Please connect to WiFi and try again.\n\n" +
                   "Size: ${formatBytes(model.sizeInBytes)}\n" +
                   "Estimated time on 4G: ${estimateDownloadTime(model.sizeInBytes, false)}"
        }

        val requiredSpace = if (model.minFreeStorageBytes > 0) {
            model.minFreeStorageBytes
        } else {
            (model.sizeInBytes * (1 + STORAGE_BUFFER_PERCENTAGE)).toLong()
        }

        val availableSpace = getAvailableStorageSpace()
        if (availableSpace < requiredSpace) {
            return "Insufficient storage space.\n\n" +
                   "Required: ${formatBytes(requiredSpace)}\n" +
                   "Available: ${formatBytes(availableSpace)}\n" +
                   "Please free up ${formatBytes(requiredSpace - availableSpace)} and try again."
        }

        if (!isNetworkAvailable()) {
            return "No network connection. Please check your internet and try again."
        }

        return null
    }

    private fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getAvailableStorageSpace(): Long {
        val dir = context.getExternalFilesDir(null) ?: return 0L
        val stat = StatFs(dir.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1_000_000_000 -> "%.2f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.2f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.2f KB".format(bytes / 1_000.0)
        else -> "$bytes bytes"
    }

    private fun estimateDownloadTime(bytes: Long, isWifi: Boolean): String {
        val speedBps = if (isWifi) 10_000_000L else 2_000_000L
        val seconds = bytes / speedBps
        return when {
            seconds < 60 -> "< 1 minute"
            seconds < 3600 -> "${seconds / 60} minutes"
            else -> "${seconds / 3600} hours"
        }
    }
}
