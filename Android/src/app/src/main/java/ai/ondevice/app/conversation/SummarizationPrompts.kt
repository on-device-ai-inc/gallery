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

package ai.ondevice.app.conversation

import ai.ondevice.app.data.ConversationMessage

/**
 * Summarization prompts cloned from LangChain ConversationSummaryBufferMemory.
 * Source: https://github.com/langchain-ai/langchain/blob/master/libs/langchain/langchain/memory/summary_buffer.py
 */
object SummarizationPrompts {

    /**
     * Progressive summarization: add new conversations onto existing summary.
     * This is the EXACT pattern from LangChain (battle-tested with 100k+ users).
     */
    fun buildProgressiveSummary(
        existingSummary: String,
        newMessages: List<ConversationMessage>
    ): String {
        val conversationText = newMessages.joinToString("\n") {
            "${if (it.isUser) "User" else "Assistant"}: ${it.content}"
        }

        return """
Progressively summarize the lines of conversation provided, adding onto the previous summary returning a new summary. Focus on key facts, decisions, and commitments.

EXAMPLE
Current summary:
The user asked about building an Android app. The assistant suggested using Kotlin and MVVM architecture.

New lines of conversation:
User: What about dependency injection?
Assistant: I recommend using Hilt for dependency injection in Android.

New summary:
The user asked about building an Android app. The assistant suggested using Kotlin, MVVM architecture, and Hilt for dependency injection.
END OF EXAMPLE

Current summary:
${existingSummary.ifEmpty { "This is the start of the conversation." }}

New lines of conversation:
$conversationText

New summary:
        """.trimIndent()
    }
}
