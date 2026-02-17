package ai.ondevice.app.conversation

import ai.ondevice.app.data.ConversationMessage
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for TokenEstimator.
 * TDD: Write failing tests first, then implement.
 */
class TokenEstimatorTest {

    @Test
    fun `estimate returns 1 for empty string`() {
        val result = TokenEstimator.estimate("")
        assertEquals(1, result)
    }

    @Test
    fun `estimate returns correct token count for hello world`() {
        // "hello world" = 11 chars → 11/4 = 2.75 → 2 tokens
        val result = TokenEstimator.estimate("hello world")
        assertEquals(2, result)
    }

    @Test
    fun `estimate returns correct token count for longer text`() {
        // 40 chars exactly → 40/4 = 10 tokens
        val text = "1234567890123456789012345678901234567890" // Exactly 40 chars
        val result = TokenEstimator.estimate(text)
        assertEquals(10, result)
    }

    @Test
    fun `estimate list sums all message tokens`() {
        val messages = listOf(
            ConversationMessage(
                id = 1,
                threadId = 1,
                content = "hello world", // 11 chars → 2 tokens
                isUser = true,
                timestamp = 0
            ),
            ConversationMessage(
                id = 2,
                threadId = 1,
                content = "test message 1234", // 17 chars → 4 tokens
                isUser = false,
                timestamp = 0
            )
        )

        val result = TokenEstimator.estimate(messages)
        // 2 + 4 = 6 tokens
        assertEquals(6, result)
    }
}
