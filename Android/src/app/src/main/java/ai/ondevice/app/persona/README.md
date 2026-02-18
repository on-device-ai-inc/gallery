# Persona System for Edge Gallery

This package contains the persona system for the Edge Gallery Android app, providing five different persona variants optimized for on-device AI interactions with Gemma models.

## Overview

The persona system consists of three main components:

1. **PersonaVariant.kt** - Enum defining 5 persona variants (MAXIMUM, COMPREHENSIVE, BALANCED, STREAMLINED, MINIMAL)
2. **PersonaLibrary.kt** - Contains the actual persona text for each variant
3. **PersonaManager.kt** - Manages persona injection into conversation prompts

## Persona Variants

| Variant | Tokens | 2K Context % | Best For |
|---------|--------|--------------|----------|
| MAXIMUM | 620 | 31% | Complex development tasks, professional use cases |
| COMPREHENSIVE | 340 | 17% | General development work, balanced performance |
| BALANCED (default) | 230 | 11.5% | General use, resource-constrained devices |
| STREAMLINED | 170 | 8.5% | Simple tasks, very resource-constrained devices |
| MINIMAL | 110 | 5.5% | Ultra-lightweight deployment, maximum context |

## Usage Examples

### Basic Usage

```kotlin
// Create a persona manager
val personaManager = PersonaManager()

// Format a single message with the default (BALANCED) persona
val prompt = personaManager.formatSingleMessageWithPersona(
    message = "What is Kotlin?",
    variant = PersonaVariant.BALANCED
)
```

### Multi-turn Conversation

```kotlin
// Format a list of conversation messages
val messages: List<ConversationMessage> = conversationDao.getMessagesForThread(threadId)
val formattedPrompt = personaManager.formatWithPersona(
    messages = messages,
    variant = PersonaVariant.BALANCED
)

// Send to model...
```

### Quick Format (Convenience Method)

```kotlin
// For single-turn interactions with default persona
val prompt = PersonaManager.quickFormat("Explain coroutines in Kotlin")
```

### Continuing a Conversation

```kotlin
// When adding a new message to an existing conversation
val existingMessages: List<ConversationMessage> = getConversationHistory()
val newMessage = "Can you explain more about that?"

val prompt = personaManager.formatContinuation(
    messages = existingMessages,
    newUserMessage = newMessage
)
```

### Inspecting Persona Variants

```kotlin
// Get information about a variant
val info = personaManager.getVariantInfo(PersonaVariant.MAXIMUM)
println(info)
// Output:
// Variant: MAXIMUM
// Description: Most comprehensive guidance for complex tasks
// Token Count: 620
// Context Usage (2K): 31.0%

// Get raw persona text
val personaText = personaManager.getPersonaText(PersonaVariant.BALANCED)
```

### Validating Messages

```kotlin
// Validate message structure before formatting
val (isValid, errorMessage) = personaManager.validateMessages(messages)
if (!isValid) {
    Log.e(TAG, "Invalid message structure: $errorMessage")
    return
}

val formattedPrompt = personaManager.formatWithPersona(messages)
```

## Integration with ViewModels

Example integration in a chat ViewModel:

```kotlin
class LlmChatViewModel(
    @ApplicationContext private val context: Context,
    private val conversationDao: ConversationDao
) : ChatViewModel(context, conversationDao) {

    private val personaManager = PersonaManager()

    fun sendMessage(userMessage: String, variant: PersonaVariant = PersonaVariant.BALANCED) {
        viewModelScope.launch {
            // Get conversation history
            val messages = conversationDao.getMessagesForThread(currentThreadId)

            // Format with persona
            val prompt = if (messages.isEmpty()) {
                // New conversation - use single message format
                personaManager.formatSingleMessageWithPersona(userMessage, variant)
            } else {
                // Continuing conversation
                personaManager.formatContinuation(messages, userMessage)
            }

            // Send to model
            generateResponse(prompt)
        }
    }
}
```

## Design for Gemma Models

The persona system is specifically designed for Gemma models, which:

1. **Don't support system roles** - Unlike some models that have a dedicated system message, Gemma requires persona instructions to be embedded in the user's first message
2. **Use alternating user/assistant format** - The PersonaManager formats conversations in this expected pattern
3. **Benefit from concise personas** - All variants are optimized to minimize token overhead while maximizing guidance quality

## Choosing the Right Variant

### Use BALANCED (default) when:
- General purpose chat interactions
- Resource-constrained devices (2K context window)
- You want the best balance of quality and efficiency

### Use MAXIMUM when:
- Complex coding tasks requiring detailed guidance
- Professional use cases needing consistent high quality
- Devices with 8K+ context windows

### Use COMPREHENSIVE when:
- General development work
- You need strong guidance but want better efficiency than MAXIMUM

### Use STREAMLINED when:
- Simple conversational tasks
- Very resource-constrained devices
- Casual use cases

### Use MINIMAL when:
- Ultra-lightweight deployment
- Maximum context needed for long conversations
- Prototyping or experimental features

## Future Enhancements

Potential areas for expansion:

1. **Database Integration** - Store user's preferred persona variant in settings
2. **UI Controls** - Add settings UI to let users choose their preferred variant
3. **Per-Thread Variants** - Allow different variants for different conversation threads
4. **Dynamic Switching** - Automatically adjust variant based on device resources or conversation length
5. **Custom Personas** - Allow users to create their own custom persona variants

## Testing

Example unit test:

```kotlin
class PersonaManagerTest {
    private lateinit var personaManager: PersonaManager

    @Before
    fun setup() {
        personaManager = PersonaManager()
    }

    @Test
    fun `formatSingleMessageWithPersona includes persona and message`() {
        val message = "Hello, world!"
        val result = personaManager.formatSingleMessageWithPersona(
            message,
            PersonaVariant.BALANCED
        )

        assertTrue(result.contains(PersonaLibrary.BALANCED))
        assertTrue(result.contains("User: $message"))
    }

    @Test
    fun `validateMessages rejects empty list`() {
        val (isValid, error) = personaManager.validateMessages(emptyList())
        assertFalse(isValid)
        assertEquals("Message list is empty", error)
    }
}
```

## License

Copyright 2025 Google LLC - Apache License 2.0
