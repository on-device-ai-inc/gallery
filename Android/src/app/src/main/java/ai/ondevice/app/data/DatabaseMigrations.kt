/*
 * Copyright 2025 Google LLC
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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for the OnDevice app.
 * These migrations ensure user data is preserved when the database schema changes.
 */

/**
 * Migration from version 1 to version 2.
 * Adds image support to conversation messages.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add imageUris column to store comma-separated file paths
        database.execSQL(
            "ALTER TABLE conversation_messages ADD COLUMN imageUris TEXT DEFAULT NULL"
        )

        // Add messageType column to distinguish message types
        database.execSQL(
            "ALTER TABLE conversation_messages ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'"
        )
    }
}

/**
 * Migration from version 2 to version 3.
 * Adds audio support to conversation messages.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add audioUri column to store audio file path
        database.execSQL(
            "ALTER TABLE conversation_messages ADD COLUMN audioUri TEXT DEFAULT NULL"
        )

        // Add audioSampleRate column to store audio metadata
        database.execSQL(
            "ALTER TABLE conversation_messages ADD COLUMN audioSampleRate INTEGER DEFAULT NULL"
        )

        // Add index on updatedAt for better sorting performance
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_threads_updated_at ON conversation_threads(updatedAt)"
        )
    }
}

/**
 * Migration from version 3 to version 4.
 * Adds prompt engineering support: persona variants and token monitoring.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add persona variant column to track which persona is being used
        database.execSQL(
            "ALTER TABLE conversation_threads ADD COLUMN personaVariant TEXT NOT NULL DEFAULT 'BALANCED'"
        )

        // Add estimated tokens column for context monitoring
        database.execSQL(
            "ALTER TABLE conversation_threads ADD COLUMN estimatedTokens INTEGER NOT NULL DEFAULT 0"
        )

        // Add last token update timestamp
        database.execSQL(
            "ALTER TABLE conversation_threads ADD COLUMN lastTokenUpdate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
        )
    }
}

/**
 * Migration from version 4 to version 5.
 * No-op migration (compression fields removed before release).
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // No schema changes - version bump only
    }
}

/**
 * Migration from version 5 to version 6.
 * Removes unused compression fields from conversation_messages.
 * (Cleanup for users who upgraded to version 5)
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Note: SQLite doesn't support DROP COLUMN before 3.35.0
        // We need to recreate the table without compression fields

        // Create new table without compression fields
        database.execSQL(
            """
            CREATE TABLE conversation_messages_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                threadId INTEGER NOT NULL,
                content TEXT NOT NULL,
                isUser INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                imageUris TEXT,
                audioUri TEXT,
                audioSampleRate INTEGER,
                messageType TEXT NOT NULL DEFAULT 'TEXT',
                FOREIGN KEY(threadId) REFERENCES conversation_threads(id) ON DELETE CASCADE
            )
            """
        )

        // Copy data (excluding compression fields)
        database.execSQL(
            """
            INSERT INTO conversation_messages_new
            (id, threadId, content, isUser, timestamp, imageUris, audioUri, audioSampleRate, messageType)
            SELECT id, threadId, content, isUser, timestamp, imageUris, audioUri, audioSampleRate, messageType
            FROM conversation_messages
            """
        )

        // Drop old table
        database.execSQL("DROP TABLE conversation_messages")

        // Rename new table
        database.execSQL("ALTER TABLE conversation_messages_new RENAME TO conversation_messages")

        // Recreate index
        database.execSQL("CREATE INDEX index_conversation_messages_threadId ON conversation_messages(threadId)")
    }
}

/**
 * Migration from version 6 to version 7.
 * Removes non-working token counting fields from conversation_threads.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Note: SQLite doesn't support DROP COLUMN before 3.35.0
        // We need to recreate the table without token/persona fields

        // Create new table without token/persona fields
        database.execSQL(
            """
            CREATE TABLE conversation_threads_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                modelId TEXT NOT NULL,
                taskId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isStarred INTEGER NOT NULL DEFAULT 0
            )
            """
        )

        // Copy data (excluding removed fields: personaVariant, estimatedTokens, lastTokenUpdate)
        database.execSQL(
            """
            INSERT INTO conversation_threads_new
            (id, title, modelId, taskId, createdAt, updatedAt, isStarred)
            SELECT id, title, modelId, taskId, createdAt, updatedAt, isStarred
            FROM conversation_threads
            """
        )

        // Drop old table
        database.execSQL("DROP TABLE conversation_threads")

        // Rename new table
        database.execSQL("ALTER TABLE conversation_threads_new RENAME TO conversation_threads")

        // Recreate index
        database.execSQL("CREATE INDEX IF NOT EXISTS index_threads_updated_at ON conversation_threads(updatedAt)")
    }
}

/**
 * Migration from version 7 to version 8.
 * Adds conversation_state table for context compression.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create conversation_state table for storing conversation summaries
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS conversation_state (
                threadId INTEGER PRIMARY KEY NOT NULL,
                runningSummary TEXT NOT NULL,
                turnsSummarized INTEGER NOT NULL,
                lastCompactionTime INTEGER NOT NULL
            )
            """
        )
    }
}

/**
 * All migrations in order.
 * Add new migrations to this array when database schema changes.
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8
)
