# Proposal: Remove Token Counting and Reset Infrastructure

## Summary
Remove all non-working token counting, automatic reset logic, and related infrastructure to provide a clean slate for implementing a new battle-tested context management system.

## Motivation
The current token counting and reset infrastructure **does not actually work**:
- Character/4 estimation is inaccurate (±20% error)
- Reset at 3200/4096 tokens never triggers correctly
- User notification code path is unreachable
- Database fields (`estimatedTokens`, `lastTokenUpdate`) are unused/misleading
- Creates false confidence that context is being managed

**Goal**: Clean slate before implementing a proven context management approach (e.g., from Perplexica or other battle-tested systems).

## Scope

### IN SCOPE
- Remove token counting logic from `LlmChatViewModel.kt` (lines 97-108)
- Remove database fields: `estimatedTokens`, `lastTokenUpdate` from `ConversationThread`
- Remove database methods: `updateTokenCount()` from `ConversationDao`
- Remove reset notification code
- Remove `personaVariant` field (unused after compression removal)
- Create database migration for schema cleanup
- Update all references to removed fields

### OUT OF SCOPE
- Implementing new context management (separate change)
- Modifying MediaPipe `Conversation` object behavior
- Changing message persistence logic
- Modifying UI components

## Acceptance Criteria
- [ ] No token counting logic remains in ViewModel
- [ ] Database schema cleaned (migration created)
- [ ] No compilation errors after removal
- [ ] Existing conversations still load correctly
- [ ] CI passes (lint + tests)
- [ ] No references to removed fields in codebase

## Technical Approach

### 1. Remove ViewModel Logic
**File**: `app/src/main/java/ai/ondevice/app/feature/llmchat/LlmChatViewModel.kt`

Remove lines 97-108 (token counting and reset logic):
```kotlin
// DELETE THIS BLOCK
val conversationMessages = conversationDao.getMessagesForThread(threadId)
if (conversationMessages.isNotEmpty()) {
    val totalChars = conversationMessages.sumOf { it.content.length }
    val estimatedTokens = totalChars / 4
    val MAX_TOKENS = 4096
    val RESET_THRESHOLD = 3200

    if (estimatedTokens > RESET_THRESHOLD) {
        // Reset logic...
    }
}
```

### 2. Remove Database Fields
**File**: `app/src/main/java/ai/ondevice/app/data/entity/ConversationThread.kt`

Remove fields:
```kotlin
val personaVariant: String = "BALANCED",  // DELETE
val estimatedTokens: Int = 0,              // DELETE
val lastTokenUpdate: Long = System.currentTimeMillis()  // DELETE
```

### 3. Remove DAO Methods
**File**: `app/src/main/java/ai/ondevice/app/data/dao/ConversationDao.kt`

Remove:
```kotlin
@Query("UPDATE conversation_threads SET estimatedTokens = :tokens, lastTokenUpdate = :timestamp WHERE id = :threadId")
suspend fun updateTokenCount(threadId: Long, tokens: Int, timestamp: Long = System.currentTimeMillis())

@Query("UPDATE conversation_threads SET personaVariant = :variant WHERE id = :threadId")
suspend fun updatePersonaVariant(threadId: Long, variant: String)
```

### 4. Create Database Migration
**File**: `app/src/main/java/ai/ondevice/app/data/AppDatabase.kt`

Add migration 6→7:
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
        database.execSQL("CREATE INDEX IF NOT EXISTS index_conversation_threads_modelId ON conversation_threads(modelId)")
    }
}
```

### 5. Search and Remove References
Search for:
- `estimatedTokens`
- `lastTokenUpdate`
- `personaVariant`
- `updateTokenCount`
- `updatePersonaVariant`
- `RESET_THRESHOLD`
- `MAX_TOKENS` (if only used for reset)

### 6. Verify PersonaManager Impact
Check if `PersonaManager` is still used elsewhere:
- If only used for token-based persona selection → remove
- If used for other features → keep but remove variant persistence

## Impact Analysis

### Files to Modify
1. `LlmChatViewModel.kt` - Remove token counting logic
2. `ConversationThread.kt` - Remove fields
3. `ConversationDao.kt` - Remove methods
4. `AppDatabase.kt` - Add migration, bump version to 7
5. `PersonaVariant.kt` - Check if still needed
6. Any test files referencing removed fields

### Breaking Changes
- None (fields were unused/broken)
- Database migration preserves existing conversations

### Risk Assessment
- **Low risk**: Removing non-functional code
- **Migration tested**: Similar to MIGRATION_5_6 (previous compression cleanup)
- **Rollback**: Can revert migration if issues found

## References
- Previous cleanup: Commits `fb8e376`, `cae5eef` (compression removal)
- Similar migration: `MIGRATION_5_6` in `AppDatabase.kt`
- Forensic analysis: See conversation above (context compression investigation)

## Success Metrics
- [ ] Codebase search shows zero references to removed fields
- [ ] CI green (all tests pass)
- [ ] Database migration runs successfully
- [ ] App launches and loads conversations normally
- [ ] No crashes or errors in logs

## Follow-Up Work
After this cleanup, implement battle-tested context management:
- Research Perplexica's approach (27.7k stars)
- Copy proven implementation with minimal changes
- Create separate OpenSpec proposal for new system
