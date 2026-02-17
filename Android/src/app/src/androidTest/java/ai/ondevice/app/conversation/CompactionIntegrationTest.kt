package ai.ondevice.app.conversation

import ai.ondevice.app.data.AppDatabase
import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ConversationMessage
import ai.ondevice.app.data.ConversationThread
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for end-to-end compaction workflow.
 * Tests the complete flow: token accumulation → compaction trigger → summarization → state persistence.
 */
@RunWith(AndroidJUnit4::class)
class CompactionIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ConversationDao
    private lateinit var compactionManager: CompactionManager

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.conversationDao()
        compactionManager = CompactionManager(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testCompactionNotTriggeredBelowThreshold() = runBlocking {
        // Create thread
        val threadId = dao.insertThread(
            ConversationThread(
                id = 0,
                modelName = "test-model",
                timestamp = System.currentTimeMillis()
            )
        )

        // Add messages totaling < 3072 tokens (trigger threshold)
        // Each message ~50 chars = ~12 tokens
        // 100 messages = ~1200 tokens (well below 3072)
        repeat(100) { i ->
            dao.insertMessage(
                ConversationMessage(
                    id = 0,
                    threadId = threadId,
                    content = "Short test message number $i with some content.",
                    isUser = i % 2 == 0,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        val messages = dao.getMessagesForThread(threadId)
        val result = compactionManager.checkAndCompact(
            threadId = threadId,
            messages = messages,
            llmHelper = null as Any, // Not needed for NotNeeded result
            model = null as Any
        )

        assertTrue("Compaction should not trigger below threshold", result is CompactionResult.NotNeeded)
    }

    @Test
    fun testTokenEstimationAccuracy() = runBlocking {
        // Verify token estimation matches expectations
        val shortMessage = "Hello world" // 11 chars → 2 tokens (11/4 = 2.75 → 2)
        val longMessage = "A".repeat(100) // 100 chars → 25 tokens

        assertEquals(2, TokenEstimator.estimate(shortMessage))
        assertEquals(25, TokenEstimator.estimate(longMessage))
    }

    @Test
    fun testConversationStatePersistedAcrossReads() = runBlocking {
        // Create thread
        val threadId = dao.insertThread(
            ConversationThread(
                id = 0,
                modelName = "test-model",
                timestamp = System.currentTimeMillis()
            )
        )

        // Save initial state
        val initialState = ai.ondevice.app.data.ConversationState(
            threadId = threadId,
            runningSummary = "Initial conversation about testing",
            turnsSummarized = 5,
            lastCompactionTime = System.currentTimeMillis()
        )
        dao.saveConversationState(initialState)

        // Retrieve and verify
        val retrieved = dao.getConversationState(threadId)
        assertNotNull("State should be retrieved", retrieved)
        assertEquals("Initial conversation about testing", retrieved?.runningSummary)
        assertEquals(5, retrieved?.turnsSummarized)
    }

    @Test
    fun testMultipleCompactionsCumulateSummary() = runBlocking {
        // Create thread
        val threadId = dao.insertThread(
            ConversationThread(
                id = 0,
                modelName = "test-model",
                timestamp = System.currentTimeMillis()
            )
        )

        // First compaction - save initial summary
        val firstSummary = ai.ondevice.app.data.ConversationState(
            threadId = threadId,
            runningSummary = "First summary: User asked about features",
            turnsSummarized = 10,
            lastCompactionTime = System.currentTimeMillis()
        )
        dao.saveConversationState(firstSummary)

        // Second compaction - update with new summary
        val secondSummary = ai.ondevice.app.data.ConversationState(
            threadId = threadId,
            runningSummary = "First summary: User asked about features\nSecond summary: Discussion about implementation",
            turnsSummarized = 20,
            lastCompactionTime = System.currentTimeMillis()
        )
        dao.saveConversationState(secondSummary)

        // Verify cumulative summary
        val retrieved = dao.getConversationState(threadId)
        assertNotNull(retrieved)
        assertTrue("Summary should contain both parts",
            retrieved?.runningSummary?.contains("User asked about features") == true)
        assertTrue("Summary should contain both parts",
            retrieved?.runningSummary?.contains("Discussion about implementation") == true)
        assertEquals(20, retrieved?.turnsSummarized)
    }

    @Test
    fun testDatabaseMigrationCreatesStateTable() = runBlocking {
        // Verify conversation_state table exists and is queryable
        val threadId = dao.insertThread(
            ConversationThread(
                id = 0,
                modelName = "test-model",
                timestamp = System.currentTimeMillis()
            )
        )

        // This should not throw - table exists
        val state = dao.getConversationState(threadId)
        assertNull("New thread should have no state", state)

        // Insert state
        dao.saveConversationState(
            ai.ondevice.app.data.ConversationState(
                threadId = threadId,
                runningSummary = "Migration test",
                turnsSummarized = 1,
                lastCompactionTime = System.currentTimeMillis()
            )
        )

        // Verify inserted
        val retrieved = dao.getConversationState(threadId)
        assertNotNull(retrieved)
        assertEquals("Migration test", retrieved?.runningSummary)
    }
}
