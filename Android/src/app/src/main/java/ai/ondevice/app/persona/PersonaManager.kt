package ai.ondevice.app.persona

import ai.ondevice.app.data.ConversationMessage

/**
 * Manages persona injection into conversation prompts for Gemma models.
 *
 * Since Gemma models don't support a system role, personas must be embedded
 * in the first user turn. This manager handles formatting conversations with
 * the appropriate persona variant while maintaining conversation history.
 *
 * Usage:
 * ```
 * val manager = PersonaManager()
 * val formattedPrompt = manager.formatWithPersona(messages, PersonaVariant.BALANCED)
 * ```
 */
class PersonaManager {

    /**
     * Formats a list of conversation messages with the specified persona variant.
     *
     * For Gemma models (which don't support system roles), this method:
     * 1. Prepends the persona to the first user message
     * 2. Formats all subsequent messages in alternating user/assistant format
     * 3. Ensures proper conversation flow for the model
     *
     * @param messages List of conversation messages to format
     * @param variant The persona variant to use (defaults to BALANCED)
     * @return Formatted prompt string ready for model input
     */
    fun formatWithPersona(
        messages: List<ConversationMessage>,
        variant: PersonaVariant = PersonaVariant.BALANCED
    ): String {
        if (messages.isEmpty()) {
            return ""
        }

        val persona = PersonaLibrary.getPersona(variant)
        val builder = StringBuilder()

        // Separate user and assistant messages
        val userMessages = messages.filter { it.isUser }
        val assistantMessages = messages.filter { !it.isUser }

        // For the first user message, prepend the persona
        if (userMessages.isNotEmpty()) {
            val firstUserMessage = userMessages.first()
            builder.append("${persona}\n\n")
            builder.append("User: ${firstUserMessage.content}\n\n")

            // Add subsequent messages in alternating format
            var userIndex = 1
            var assistantIndex = 0

            while (userIndex < userMessages.size || assistantIndex < assistantMessages.size) {
                // Add assistant response if available
                if (assistantIndex < assistantMessages.size) {
                    builder.append("Assistant: ${assistantMessages[assistantIndex].content}\n\n")
                    assistantIndex++
                }

                // Add next user message if available
                if (userIndex < userMessages.size) {
                    builder.append("User: ${userMessages[userIndex].content}\n\n")
                    userIndex++
                }
            }
        }

        return builder.toString().trim()
    }

    /**
     * Formats a single user message with persona (useful for new conversations).
     *
     * @param message The user's message content
     * @param variant The persona variant to use (defaults to BALANCED)
     * @return Formatted prompt string with persona prepended
     */
    fun formatSingleMessageWithPersona(
        message: String,
        variant: PersonaVariant = PersonaVariant.BALANCED
    ): String {
        val persona = PersonaLibrary.getPersona(variant)
        return "${persona}\n\nUser: ${message}"
    }

    /**
     * Formats messages for multi-turn conversation continuation.
     * This is optimized for ongoing conversations where the persona has already been established.
     *
     * @param messages List of conversation messages (persona assumed to be in first message)
     * @param newUserMessage New message from the user to append
     * @return Formatted prompt string for continuation
     */
    fun formatContinuation(
        messages: List<ConversationMessage>,
        newUserMessage: String
    ): String {
        val builder = StringBuilder()

        // Add all existing messages
        for (message in messages) {
            val role = if (message.isUser) "User" else "Assistant"
            builder.append("$role: ${message.content}\n\n")
        }

        // Add new user message
        builder.append("User: $newUserMessage")

        return builder.toString().trim()
    }

    /**
     * Extracts just the persona text for display or inspection.
     *
     * @param variant The persona variant to retrieve
     * @return The raw persona text
     */
    fun getPersonaText(variant: PersonaVariant = PersonaVariant.BALANCED): String {
        return PersonaLibrary.getPersona(variant)
    }

    /**
     * Gets information about a persona variant including token count and usage metrics.
     *
     * @param variant The persona variant to inspect
     * @return A formatted string with variant information
     */
    fun getVariantInfo(variant: PersonaVariant): String {
        return """
            Variant: ${variant.name}
            Description: ${variant.getDescription()}
            Token Count: ${variant.getTokenCount()}
            Context Usage (2K): ${variant.getContextUsagePercent2K()}%
        """.trimIndent()
    }

    /**
     * Validates that messages are properly formatted for persona injection.
     * Useful for debugging and ensuring conversation integrity.
     *
     * @param messages List of messages to validate
     * @return Pair of (isValid, errorMessage)
     */
    fun validateMessages(messages: List<ConversationMessage>): Pair<Boolean, String?> {
        if (messages.isEmpty()) {
            return Pair(false, "Message list is empty")
        }

        // Check that conversation starts with a user message
        if (!messages.first().isUser) {
            return Pair(false, "First message must be from user")
        }

        // Check for alternating pattern (relaxed - allows multiple user messages)
        var lastWasUser = false
        for (message in messages) {
            if (message.isUser) {
                lastWasUser = true
            } else {
                if (!lastWasUser) {
                    return Pair(false, "Assistant messages must follow user messages")
                }
                lastWasUser = false
            }
        }

        return Pair(true, null)
    }

    companion object {
        /**
         * Default persona variant used across the application.
         */
        val DEFAULT_VARIANT = PersonaVariant.BALANCED

        /**
         * Creates a PersonaManager instance with default settings.
         */
        fun create(): PersonaManager = PersonaManager()

        /**
         * Quickly format a simple prompt with the default persona.
         * Convenience method for single-turn interactions.
         *
         * @param message The user's message
         * @return Formatted prompt with default (BALANCED) persona
         */
        fun quickFormat(message: String): String {
            return PersonaManager().formatSingleMessageWithPersona(message, DEFAULT_VARIANT)
        }
    }
}
