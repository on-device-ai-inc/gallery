/*
 * Copyright 2025 On Device AI Inc.
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

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AnalyticsTracker"

@Singleton
class AnalyticsTracker @Inject constructor(
    private val analyticsDao: AnalyticsDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun trackMessageSent(source: String, responseLatencyMs: Long = 0) {
        scope.launch {
            try {
                val payload = JSONObject().apply {
                    put("source", source)
                    put("response_latency_ms", responseLatencyMs)
                }.toString()
                analyticsDao.insert(AnalyticsEvent(
                    event = "message_sent",
                    payload = payload
                ))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to track message_sent", e)
            }
        }
    }

    fun trackSessionStart(modelName: String) {
        scope.launch {
            try {
                val payload = JSONObject().apply {
                    put("model_name", modelName)
                }.toString()
                analyticsDao.insert(AnalyticsEvent(
                    event = "session_start",
                    payload = payload
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track session_start", e)
            }
        }
    }
}
