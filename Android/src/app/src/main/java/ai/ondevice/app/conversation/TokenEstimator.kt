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

import ai.ondevice.app.data.ConversationMessage

/**
 * Simple token estimator using char-count heuristic.
 * Accuracy: ±15% (acceptable with 75% trigger buffer).
 *
 * Uses 4 chars = 1 token approximation, which is standard for English text.
 */
object TokenEstimator {
    private const val AVG_CHARS_PER_TOKEN = 4

    /**
     * Estimates token count for a text string.
     * @param text Input text
     * @return Estimated token count (minimum 1)
     */
    fun estimate(text: String): Int =
        (text.length / AVG_CHARS_PER_TOKEN).coerceAtLeast(1)

    /**
     * Estimates total token count for a list of messages.
     * @param messages List of conversation messages
     * @return Sum of all message token estimates
     */
    fun estimate(messages: List<ConversationMessage>): Int =
        messages.sumOf { estimate(it.content) }
}
