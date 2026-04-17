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

package ai.ondevice.app.conversation

import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ConversationMessage
import ai.ondevice.app.data.ConversationState
import ai.ondevice.app.data.Model
import ai.ondevice.app.ui.llmchat.LlmChatModelHelper
import android.util.Log
import androidx.core.os.bundleOf
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Manages conversation compaction using self-summarization.
 * Based on LangChain ConversationSummaryBufferMemory pattern.
 */
class CompactionManager(
    private val conversationDao: ConversationDao
) {
    companion object {
        private const val TAG = "CompactionManager"
        const val MAX_TOKENS = 4096
        const val TRIGGER_PERCENT = 0.75f  // 3,072 tokens
        const val TARGET_PERCENT = 0.40f   // 1,638 tokens
    }

    /**
     * Checks if compaction is needed and executes it if so.
     * @return CompactionResult indicating whether compaction was performed
     */
    suspend fun checkAndCompact(
        threadId: Long,
        messages: List<ConversationMessage>,
        llmHelper: LlmChatModelHelper,
        model: Model
    ): CompactionResult {
        val currentTokens = TokenEstimator.estimate(messages)
        val triggerThreshold = (MAX_TOKENS * TRIGGER_PERCENT).toInt()

        if (currentTokens < triggerThreshold) {
            return CompactionResult.NotNeeded
        }

        Log.d(TAG, "Compaction triggered: $currentTokens tokens (threshold: $triggerThreshold)")
        return executeCompaction(threadId, messages, llmHelper, model)
    }

    /**
     * Executes the compaction process:
     * 1. Identifies messages to evict
     * 2. Summarizes them using the LLM
     * 3. Saves the summary
     * 4. Deletes evicted messages
     */
    private suspend fun executeCompaction(
        threadId: Long,
        messages: List<ConversationMessage>,
        llmHelper: LlmChatModelHelper,
        model: Model
    ): CompactionResult {
        // Find messages to evict (oldest first, until target reached)
        val targetTokens = (MAX_TOKENS * TARGET_PERCENT).toInt()
        val toEvict = mutableListOf<ConversationMessage>()
        var currentTotal = TokenEstimator.estimate(messages)

        for (msg in messages) {
            if (currentTotal <= targetTokens) break
            toEvict.add(msg)
            currentTotal -= TokenEstimator.estimate(msg.content)
        }

        if (toEvict.isEmpty()) {
            Log.d(TAG, "No messages to evict")
            return CompactionResult.NotNeeded
        }

        // Get existing summary
        val existingState = conversationDao.getConversationState(threadId)
        val existingSummary = existingState?.runningSummary ?: ""

        // Generate new summary using EXISTING LLM (async with callback)
        val prompt = SummarizationPrompts.buildProgressiveSummary(existingSummary, toEvict)

        val newSummary = try {
            summarizeAsync(llmHelper, model, prompt)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Summarization failed", e)
            return CompactionResult.Failed(e.message ?: "Unknown error")
        }

        // Save state
        conversationDao.saveConversationState(
            ConversationState(
                threadId = threadId,
                runningSummary = newSummary.take(1500), // Hard limit
                turnsSummarized = (existingState?.turnsSummarized ?: 0) + toEvict.size,
                lastCompactionTime = System.currentTimeMillis()
            )
        )

        // Delete evicted messages
        toEvict.forEach { msg ->
            conversationDao.deleteMessage(msg.id)
        }

        val messagesAfter = messages.size - toEvict.size
        trace.putMetric("messages_after", messagesAfter.toLong())
        if (messagesAfter > 0) {
            val compressionRatio = (messagesBefore.toFloat() / messagesAfter * 100).toLong()
            trace.putMetric("compression_ratio_percent", compressionRatio)
        }
        trace.stop()

        Log.d(TAG, "Compaction complete: evicted ${toEvict.size} turns, summary: ${newSummary.length} chars")
        return CompactionResult.Success(
            evictedCount = toEvict.size,
            summaryTokens = TokenEstimator.estimate(newSummary)
        )
    }

    /**
     * Handles async summarization using LLM callback pattern.
     * Accumulates streaming results until done.
     */
    private suspend fun summarizeAsync(
        llmHelper: LlmChatModelHelper,
        model: Model,
        prompt: String
    ): String = suspendCancellableCoroutine { continuation ->
        var accumulated = ""
        var hasResumed = false

        continuation.invokeOnCancellation {
            // Mark as resumed so the callbacks don't attempt to resume a cancelled coroutine.
            hasResumed = true
        }

        llmHelper.runInference(
            model = model,
            input = prompt,
            resultListener = { partial, done ->
                if (!done) {
                    accumulated += partial
                } else if (!hasResumed) {
                    hasResumed = true
                    continuation.resume(accumulated)
                }
            },
            cleanUpListener = {},
            onError = { error ->
                if (!hasResumed) {
                    hasResumed = true
                    continuation.resumeWithException(Exception(error))
                }
            }
        )
    }
}

/**
 * Result of compaction check/execution.
 */
sealed class CompactionResult {
    object NotNeeded : CompactionResult()
    data class Success(val evictedCount: Int, val summaryTokens: Int) : CompactionResult()
    data class Failed(val error: String) : CompactionResult()
}
