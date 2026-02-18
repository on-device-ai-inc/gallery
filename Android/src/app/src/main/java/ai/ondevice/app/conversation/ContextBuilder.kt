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

package ai.ondevice.app.conversation

import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ConversationMessage

/**
 * Builds inference context with injected summary from compaction.
 * Works alongside PersonaManager to provide complete context.
 */
class ContextBuilder(
    private val conversationDao: ConversationDao
) {
    /**
     * Builds context string with summary injection if available.
     * Format: <previous_context>summary</previous_context> + recent messages
     *
     * @param threadId Conversation thread ID
     * @param recentMessages Recent conversation messages
     * @param systemPrompt Optional system prompt to prepend
     * @return Complete context string ready for inference
     */
    suspend fun buildContext(
        threadId: Long,
        recentMessages: List<ConversationMessage>,
        systemPrompt: String = ""
    ): String = buildString {
        // Add system prompt if provided
        if (systemPrompt.isNotBlank()) {
            append(systemPrompt)
            append("\n\n")
        }

        // Inject summary if exists
        val state = conversationDao.getConversationState(threadId)
        state?.runningSummary?.takeIf { it.isNotBlank() }?.let { summary ->
            append("<previous_context>\n")
            append(summary)
            append("\n</previous_context>\n\n")
        }

        // Add recent messages in standard format
        recentMessages.forEach { msg ->
            val role = if (msg.isUser) "User" else "Assistant"
            append("$role: ${msg.content}\n")
        }
    }
}
