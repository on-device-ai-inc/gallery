package ai.ondevice.app.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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
    
    // NEW: Flow version for reactive updates
    @Query("SELECT * FROM conversation_threads ORDER BY updatedAt DESC")
    fun getAllThreadsFlow(): Flow<List<ConversationThread>>
    
    @Query("SELECT * FROM conversation_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getMessagesForThread(threadId: Long): List<ConversationMessage>
    
    // NEW: Flow version for reactive updates
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

    /** Update #7: Toggle star status */
    @Query("UPDATE conversation_threads SET isStarred = :isStarred WHERE id = :threadId")
    suspend fun updateStarred(threadId: Long, isStarred: Boolean)

    /** Update #7: Rename conversation */
    @Query("UPDATE conversation_threads SET title = :title WHERE id = :threadId")
    suspend fun updateTitle(threadId: Long, title: String)

    /** Delete all messages for a thread */
    @Query("DELETE FROM conversation_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: Long)

    /** Delete a single message by ID (for compaction) */
    @Query("DELETE FROM conversation_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    // Conversation State Management (for context compression)
    /** Get conversation state for compaction management */
    @Query("SELECT * FROM conversation_state WHERE threadId = :threadId")
    suspend fun getConversationState(threadId: Long): ConversationState?

    /** Save or update conversation state */
    @androidx.room.Upsert
    suspend fun saveConversationState(state: ConversationState)
}
