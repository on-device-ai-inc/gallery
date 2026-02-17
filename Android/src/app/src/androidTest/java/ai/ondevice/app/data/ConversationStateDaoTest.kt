package ai.ondevice.app.data

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
 * Unit tests for ConversationState DAO operations.
 */
@RunWith(AndroidJUnit4::class)
class ConversationStateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ConversationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.conversationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveAndRetrieveState() = runBlocking {
        val state = ConversationState(
            threadId = 1L,
            runningSummary = "Test summary",
            turnsSummarized = 5,
            lastCompactionTime = System.currentTimeMillis()
        )

        dao.saveConversationState(state)
        val retrieved = dao.getConversationState(1L)

        assertNotNull(retrieved)
        assertEquals("Test summary", retrieved?.runningSummary)
        assertEquals(5, retrieved?.turnsSummarized)
    }

    @Test
    fun upsertUpdatesExistingState() = runBlocking {
        val state1 = ConversationState(
            threadId = 1L,
            runningSummary = "First summary",
            turnsSummarized = 3,
            lastCompactionTime = System.currentTimeMillis()
        )

        dao.saveConversationState(state1)

        val state2 = ConversationState(
            threadId = 1L,
            runningSummary = "Updated summary",
            turnsSummarized = 7,
            lastCompactionTime = System.currentTimeMillis()
        )

        dao.saveConversationState(state2)
        val retrieved = dao.getConversationState(1L)

        assertEquals("Updated summary", retrieved?.runningSummary)
        assertEquals(7, retrieved?.turnsSummarized)
    }

    @Test
    fun returnsNullForNonExistentThread() = runBlocking {
        val retrieved = dao.getConversationState(999L)
        assertNull(retrieved)
    }
}
