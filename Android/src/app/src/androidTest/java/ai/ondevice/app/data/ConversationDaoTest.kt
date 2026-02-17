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

package ai.ondevice.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for ConversationDao using an in-memory Room database.
 * Covers all CRUD operations defined in the acceptance criteria.
 */
@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

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
    fun insertThread_retrieveById() = runBlocking {
        val thread = ConversationThread(title = "Test Chat", modelId = "gemma-3n", taskId = "chat")
        val id = dao.insertThread(thread)

        val retrieved = dao.getThreadById(id)

        assertNotNull(retrieved)
        assertEquals("Test Chat", retrieved?.title)
        assertEquals("gemma-3n", retrieved?.modelId)
    }

    @Test
    fun insertMessage_retrieveForThread() = runBlocking {
        val thread = ConversationThread(title = "Chat", modelId = "gemma-3n", taskId = "chat")
        val threadId = dao.insertThread(thread)

        val message = ConversationMessage(
            threadId = threadId,
            content = "Hello!",
            isUser = true
        )
        dao.insertMessage(message)

        val messages = dao.getMessagesForThread(threadId)

        assertEquals(1, messages.size)
        assertEquals("Hello!", messages[0].content)
        assertTrue(messages[0].isUser)
    }

    @Test
    fun deleteThread_messagesCascadeDeleted() = runBlocking {
        val thread = ConversationThread(title = "Chat", modelId = "gemma-3n", taskId = "chat")
        val threadId = dao.insertThread(thread)

        dao.insertMessage(ConversationMessage(threadId = threadId, content = "msg1", isUser = true))
        dao.insertMessage(ConversationMessage(threadId = threadId, content = "msg2", isUser = false))

        dao.deleteThread(threadId)

        val messages = dao.getMessagesForThread(threadId)
        assertTrue(messages.isEmpty())

        val retrievedThread = dao.getThreadById(threadId)
        assertNull(retrievedThread)
    }

    @Test
    fun searchThreads_byTitle() = runBlocking {
        dao.insertThread(ConversationThread(title = "Kotlin tips", modelId = "m", taskId = "chat"))
        dao.insertThread(ConversationThread(title = "Android help", modelId = "m", taskId = "chat"))

        val results = dao.searchThreads("Kotlin")

        assertEquals(1, results.size)
        assertEquals("Kotlin tips", results[0].title)
    }

    @Test
    fun updateStarred_togglesCorrectly() = runBlocking {
        val thread = ConversationThread(title = "Chat", modelId = "m", taskId = "chat")
        val id = dao.insertThread(thread)

        dao.updateStarred(id, true)
        val starred = dao.getThreadById(id)
        assertTrue(starred?.isStarred == true)

        dao.updateStarred(id, false)
        val unstarred = dao.getThreadById(id)
        assertTrue(unstarred?.isStarred == false)
    }

    @Test
    fun saveConversationState_upsertsCorrectly() = runBlocking {
        val state1 = ConversationState(
            threadId = 1L,
            runningSummary = "First summary",
            turnsSummarized = 3,
            lastCompactionTime = System.currentTimeMillis()
        )
        dao.saveConversationState(state1)

        val state2 = state1.copy(runningSummary = "Updated summary", turnsSummarized = 7)
        dao.saveConversationState(state2)

        val retrieved = dao.getConversationState(1L)

        assertNotNull(retrieved)
        assertEquals("Updated summary", retrieved?.runningSummary)
        assertEquals(7, retrieved?.turnsSummarized)
    }
}
