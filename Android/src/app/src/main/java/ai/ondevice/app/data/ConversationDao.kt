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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO for conversation history operations.
 * All functions are suspend for non-blocking database access.
 */
@Dao
interface ConversationDao {

    @Insert
    suspend fun insertThread(thread: ConversationThread): Long

    @Insert
    suspend fun insertMessage(message: ConversationMessage): Long

    @Update
    suspend fun updateThread(thread: ConversationThread)

    @Query("UPDATE conversation_messages SET content = :content WHERE id = :messageId")
    suspend fun updateMessageContent(messageId: Long, content: String)

    @Query("SELECT * FROM conversation_threads ORDER BY updatedAt DESC")
    suspend fun getAllThreads(): List<ConversationThread>

    @Query("SELECT * FROM conversation_threads ORDER BY updatedAt DESC")
    fun getAllThreadsFlow(): Flow<List<ConversationThread>>

    @Query("SELECT * FROM conversation_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getMessagesForThread(threadId: Long): List<ConversationMessage>

    @Query("SELECT * FROM conversation_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThreadFlow(threadId: Long): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversation_threads WHERE id = :threadId")
    suspend fun getThreadById(threadId: Long): ConversationThread?

    @Query("DELETE FROM conversation_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: Long)

    @Query("""
        SELECT DISTINCT t.* FROM conversation_threads t
        LEFT JOIN conversation_messages m ON t.id = m.threadId
        WHERE t.title LIKE '%' || :query || '%'
           OR m.content LIKE '%' || :query || '%'
        ORDER BY t.updatedAt DESC
    """)
    suspend fun searchThreads(query: String): List<ConversationThread>

    @Query("UPDATE conversation_threads SET isStarred = :isStarred WHERE id = :threadId")
    suspend fun updateStarred(threadId: Long, isStarred: Boolean)

    @Query("UPDATE conversation_threads SET title = :title WHERE id = :threadId")
    suspend fun updateTitle(threadId: Long, title: String)

    @Query("DELETE FROM conversation_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: Long)

    @Query("DELETE FROM conversation_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("SELECT * FROM conversation_state WHERE threadId = :threadId")
    suspend fun getConversationState(threadId: Long): ConversationState?

    @Upsert
    suspend fun saveConversationState(state: ConversationState)
}
