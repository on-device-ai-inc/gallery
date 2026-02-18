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

package ai.ondevice.app.ui.settings

/**
 * Model parameter presets for different use cases
 */
enum class ModelPreset(
    val displayName: String,
    val description: String,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxTokens: Int
) {
    BALANCED(
        displayName = "Balanced",
        description = "Good balance between creativity and consistency. Best for general conversations.",
        temperature = 1.0f,
        topK = 64,
        topP = 0.95f,
        maxTokens = 2048
    ),

    CREATIVE(
        displayName = "Creative",
        description = "More varied and imaginative responses. Great for brainstorming and creative writing.",
        temperature = 1.3f,
        topK = 80,
        topP = 0.98f,
        maxTokens = 3072
    ),

    PRECISE(
        displayName = "Precise",
        description = "Focused and deterministic answers. Best for factual questions and coding.",
        temperature = 0.7f,
        topK = 40,
        topP = 0.90f,
        maxTokens = 2048
    ),

    CUSTOM(
        displayName = "Custom",
        description = "Manual control over all parameters",
        temperature = 1.0f,
        topK = 64,
        topP = 0.95f,
        maxTokens = 2048
    );

    companion object {
        fun fromValues(temp: Float, k: Int, p: Float, tokens: Int): ModelPreset? {
            return values().firstOrNull {
                it != CUSTOM &&
                it.temperature == temp &&
                it.topK == k &&
                it.topP == p &&
                it.maxTokens == tokens
            }
        }
    }
}

/**
 * Parameter info for help dialogs
 */
data class ParameterInfo(
    val name: String,
    val description: String,
    val technicalDetails: String,
    val examples: String
)

object ParameterExplanations {
    val TEMPERATURE = ParameterInfo(
        name = "Temperature",
        description = "Controls randomness in the AI's responses. Higher values make output more creative and varied, lower values make it more focused and deterministic.",
        technicalDetails = "Temperature scales the probability distribution. Values above 1.0 flatten the distribution (more randomness), values below 1.0 sharpen it (more predictable).",
        examples = "• Low (0.7): \"The capital of France is Paris.\"\n• High (1.3): \"The capital of France? That would be the beautiful city of Paris, known for its art and cuisine!\""
    )

    val TOP_K = ParameterInfo(
        name = "Top-K",
        description = "Limits how many word choices the AI considers. Lower values make responses more focused, higher values allow more variety.",
        technicalDetails = "At each step, the model only samples from the K most likely next tokens. This prevents very unlikely words from being chosen.",
        examples = "• Low (20): More predictable, fewer vocabulary choices\n• High (80): More diverse language, may include unusual words"
    )

    val TOP_P = ParameterInfo(
        name = "Top-P (Nucleus Sampling)",
        description = "Considers only the most likely words that add up to this probability. Higher values allow more variety.",
        technicalDetails = "Cumulative probability threshold. The model samples from the smallest set of tokens whose cumulative probability exceeds this value.",
        examples = "• 0.90: Only considers most confident predictions\n• 0.98: Allows more creative word choices"
    )

    val MAX_TOKENS = ParameterInfo(
        name = "Max Tokens",
        description = "Maximum length of the AI's response. One token ≈ 4 characters or ¾ of a word.",
        technicalDetails = "Limits the generation length. Responses will stop when this limit is reached or the model decides to finish naturally.",
        examples = "• 512 tokens: ~1-2 paragraphs\n• 2048 tokens: ~1-2 pages\n• 4096 tokens: ~3-4 pages"
    )
}
