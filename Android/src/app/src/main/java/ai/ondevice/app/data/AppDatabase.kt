package ai.ondevice.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database for the application.
 * Contains conversation history data.
 *
 * Version 2: Added imageUris and messageType fields to ConversationMessage
 *            to support image persistence in chat history.
 * Version 3: Added audioUri and audioSampleRate fields to ConversationMessage
 *            to support audio message persistence in chat history.
 * Version 4: Added personaVariant, estimatedTokens, and lastTokenUpdate fields
 *            to ConversationThread for prompt engineering support.
 * Version 5: (Skipped - compression fields removed before release)
 * Version 6: Cleanup - removed unused compression fields from ConversationMessage.
 * Version 7: Cleanup - removed non-working token counting fields from ConversationThread
 *            (personaVariant, estimatedTokens, lastTokenUpdate).
 * Version 8: Added ConversationState entity for context compression via self-summarization.
 */
@Database(
    entities = [
        ConversationThread::class,
        ConversationMessage::class,
        ConversationState::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
