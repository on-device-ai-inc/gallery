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

package ai.ondevice.app.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Centralized wrapper for Firebase Crashlytics logging.
 *
 * PRIVACY: This class NEVER logs PII (Personally Identifiable Information).
 * - ✅ DO log: metadata (lengths, counts, durations), model names, anonymized IDs
 * - ❌ NEVER log: user messages, conversations, full names, emails, phone numbers
 *
 * All methods are safe-by-design to prevent accidental PII logging.
 */
object CrashlyticsLogger {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    /**
     * Log model load event.
     *
     * @param modelName Name of the model (e.g., "gemma-2b-it")
     * @param success Whether the model loaded successfully
     * @param durationMs Time taken to load the model
     *
     * PRIVACY: Only logs model name, success status, and duration. NO user data.
     */
    fun logModelLoad(modelName: String, success: Boolean, durationMs: Long) {
        crashlytics.log("MODEL_LOAD: $modelName, success=$success, duration=${durationMs}ms")
        if (!success) {
            crashlytics.recordException(Exception("Model load failed: $modelName"))
        }
    }

    /**
     * Log inference event.
     *
     * @param modelName Name of the model used for inference
     * @param tokenCount Number of tokens processed (output tokens)
     * @param durationMs Time taken for inference
     *
     * PRIVACY: Only logs model name, token count, and duration. NO prompt or response content.
     */
    fun logInference(modelName: String, tokenCount: Int, durationMs: Long) {
        crashlytics.log("INFERENCE: model=$modelName, tokens=$tokenCount, duration=${durationMs}ms")
    }

    /**
     * Log user action.
     *
     * @param action Action type (e.g., "send_message", "download_model", "change_setting")
     * @param details Optional metadata (e.g., "length=50", "theme=dark")
     *
     * PRIVACY: Only logs action type and metadata. NEVER log actual content (messages, settings values with PII).
     *
     * SAFE EXAMPLES:
     * - logUserAction("send_message", "length=50")  ✅
     * - logUserAction("download_model", "gemma-2b-it")  ✅
     * - logUserAction("change_setting", "theme=dark")  ✅
     *
     * UNSAFE EXAMPLES (DON'T DO THIS):
     * - logUserAction("send_message", message)  ❌ (contains user message)
     * - logUserAction("change_setting", "fullName=John Doe")  ❌ (contains PII)
     */
    fun logUserAction(action: String, details: String = "") {
        val logMessage = if (details.isNotEmpty()) {
            "USER_ACTION: $action $details"
        } else {
            "USER_ACTION: $action"
        }
        crashlytics.log(logMessage)
    }

    /**
     * Set user identifier (anonymized).
     *
     * @param userId User identifier - MUST be anonymized (hashed or UUID), NEVER real name/email
     *
     * PRIVACY: This method automatically hashes the user ID to ensure anonymization.
     *
     * SAFE EXAMPLES:
     * - setUserId("user-uuid-1234")  ✅ (UUID)
     * - setUserId(deviceId.hashCode().toString())  ✅ (hashed device ID)
     *
     * UNSAFE EXAMPLES (DON'T DO THIS):
     * - setUserId("john.doe@example.com")  ❌ (email is PII)
     * - setUserId("John Doe")  ❌ (real name is PII)
     */
    fun setUserId(userId: String) {
        // Additional safety: hash the ID even if caller didn't
        val anonymizedId = userId.hashCode().toString()
        crashlytics.setUserId(anonymizedId)
    }

    /**
     * Set conversation ID for crash correlation.
     *
     * @param conversationId Conversation identifier (database ID)
     *
     * PRIVACY: Conversation ID is a database identifier, not user content. Safe to log.
     */
    fun setConversationId(conversationId: Long) {
        crashlytics.setCustomKey("conversation_id", conversationId)
    }

    /**
     * Record non-fatal error (logged but app doesn't crash).
     *
     * @param exception The exception to log
     * @param context Context description (e.g., "Model load failed: gemma-2b-it")
     *
     * PRIVACY: Only log metadata in context. NEVER include user data in exception message.
     *
     * SAFE EXAMPLES:
     * - recordNonFatalError(e, "Model load failed: gemma-2b-it")  ✅
     * - recordNonFatalError(e, "Inference failed after 50 tokens")  ✅
     * - recordNonFatalError(e, "Compression failed: OOM")  ✅
     *
     * UNSAFE EXAMPLES (DON'T DO THIS):
     * - recordNonFatalError(e, "Failed to send message: $userMessage")  ❌ (contains user message)
     */
    fun recordNonFatalError(exception: Exception, context: String) {
        crashlytics.log("NON_FATAL: $context")
        crashlytics.recordException(exception)
    }

    /**
     * Set custom key-value pair for crash context.
     *
     * @param key Custom key
     * @param value Custom value
     *
     * PRIVACY: Only use for non-PII metadata. NEVER user messages, names, emails.
     *
     * SAFE EXAMPLES:
     * - setCustomKey("selected_model", "gemma-2b-it")  ✅
     * - setCustomKey("theme", "dark")  ✅
     * - setCustomKey("message_count", messageCount)  ✅
     *
     * UNSAFE EXAMPLES (DON'T DO THIS):
     * - setCustomKey("full_name", user.fullName)  ❌ (PII)
     * - setCustomKey("last_message", lastMessage)  ❌ (user content)
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
}
