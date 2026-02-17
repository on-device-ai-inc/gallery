package ai.ondevice.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single message in a conversation thread.
 */
@Entity(
    tableName = "conversation_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationThread::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("threadId")]
)
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),

    // Image support - stores comma-separated file paths to internal storage
    val imageUris: String? = null,

    // Audio support - stores file path to internal storage
    val audioUri: String? = null,
    val audioSampleRate: Int? = null,

    // Message type for proper restoration
    val messageType: String = "TEXT" // TEXT, IMAGE, TEXT_WITH_IMAGE, AUDIO_CLIP
)
