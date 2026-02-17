package ai.ondevice.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a conversation thread for chat history.
 * Each thread contains multiple messages.
 */
@Entity(
    tableName = "conversation_threads",
    indices = [Index(value = ["updatedAt"], name = "index_threads_updated_at")]
)
data class ConversationThread(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val modelId: String,
    val taskId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    /** Update #7: Star/pin conversations to top */
    val isStarred: Boolean = false
)
