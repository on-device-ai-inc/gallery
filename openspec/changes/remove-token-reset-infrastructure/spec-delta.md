# Spec Delta: Remove Token Counting and Reset Infrastructure

## REMOVED

### database-schema.md (ConversationThread fields)
```diff
@Entity(tableName = "conversation_threads")
data class ConversationThread(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelId: String,
    val taskId: String,
    val createdAt: Long,
    val updatedAt: Long,
-   val isStarred: Boolean = false,
-   val personaVariant: String = "BALANCED",
-   val estimatedTokens: Int = 0,
-   val lastTokenUpdate: Long = System.currentTimeMillis()
+   val isStarred: Boolean = false
)
```

### dao-layer.md (ConversationDao methods)
```diff
interface ConversationDao {
    // ... existing methods ...

-   @Query("UPDATE conversation_threads SET estimatedTokens = :tokens, lastTokenUpdate = :timestamp WHERE id = :threadId")
-   suspend fun updateTokenCount(threadId: Long, tokens: Int, timestamp: Long = System.currentTimeMillis())
-
-   @Query("UPDATE conversation_threads SET personaVariant = :variant WHERE id = :threadId")
-   suspend fun updatePersonaVariant(threadId: Long, variant: String)
}
```

### viewmodel-logic.md (Token counting and reset)
```diff
// LlmChatViewModel.kt - sendMessage()
suspend fun sendMessage(input: String, model: Model, ...) {
-   // Token counting and reset logic
-   if (currentThreadId != null) {
-       val conversationMessages = conversationDao.getMessagesForThread(threadId)
-       if (conversationMessages.isNotEmpty()) {
-           val totalChars = conversationMessages.sumOf { it.content.length }
-           val estimatedTokens = totalChars / 4
-           val MAX_TOKENS = 4096
-           val RESET_THRESHOLD = 3200
-
-           if (estimatedTokens > RESET_THRESHOLD) {
-               Log.w(TAG, "[TOKEN-LIMIT] Resetting conversation at ~$estimatedTokens tokens")
-               LlmChatModelHelper.resetConversation(...)
-               conversationDao.deleteMessagesForThread(threadId)
-               addMessage("Conversation reset - too many messages. Please send again.")
-               return@launch
-           }
-       }
-   }

    // Continue with normal inference...
}
```

### context-management.md (entire spec)
**REMOVE ENTIRE SPEC** (if it exists)

The context management approach is being completely removed. This spec documented:
- Character/4 token estimation
- 3200/4096 reset threshold
- Automatic conversation reset
- Token tracking in database

**Replacement**: New battle-tested approach (separate proposal)

## MODIFIED

### database-schema.md (Migration history)
```diff
Database Versions:
- Version 4: Initial schema
- Version 5: (Skipped - compression fields removed before release)
- Version 6: Cleanup - removed unused compression fields from ConversationMessage
+ Version 7: Cleanup - removed non-working token counting fields from ConversationThread
```

### database-schema.md (AppDatabase version)
```diff
@Database(
    entities = [ConversationThread::class, ConversationMessage::class],
-   version = 6,
+   version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao

    companion object {
        private val MIGRATION_5_6 = object : Migration(5, 6) { ... }
+       private val MIGRATION_6_7 = object : Migration(6, 7) { ... }

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(...)
                .addMigrations(
                    MIGRATION_5_6,
+                   MIGRATION_6_7
                )
                .build()
        }
    }
}
```

## ADDED

### database-schema.md (Migration 6→7)
```kotlin
private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create new table without token/persona fields
        database.execSQL("""
            CREATE TABLE conversation_threads_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                modelId TEXT NOT NULL,
                taskId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isStarred INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Copy data (excluding removed fields)
        database.execSQL("""
            INSERT INTO conversation_threads_new
            SELECT id, title, modelId, taskId, createdAt, updatedAt, isStarred
            FROM conversation_threads
        """)

        // Drop old table
        database.execSQL("DROP TABLE conversation_threads")

        // Rename new table
        database.execSQL("ALTER TABLE conversation_threads_new RENAME TO conversation_threads")

        // Recreate indices
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_conversation_threads_modelId
            ON conversation_threads(modelId)
        """)
    }
}
```

## IMPACT SUMMARY

### Breaking Changes
- None (removed fields were non-functional)

### Database Changes
- **Version**: 6 → 7
- **Migration**: Removes 3 fields from `conversation_threads`
- **Data preservation**: All conversations preserved (only metadata removed)

### Code Changes
- **Files modified**: 4-5 files
- **Lines removed**: ~50-60 lines
- **Lines added**: ~30 lines (migration only)
- **Net reduction**: ~20-30 lines

### Test Impact
- Remove tests for removed functionality
- Update tests referencing removed fields
- Add migration test for 6→7

### Documentation Impact
- Update LESSONS_LEARNED.md
- Update CODE_INDEX.md (if applicable)
- Document reasoning for removal
