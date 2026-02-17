# Spec Delta: room-database-data-layer

## ADDED

### New Files

| File | Description |
|------|-------------|
| `data/ConversationThread.kt` | Room entity — conversation metadata |
| `data/ConversationMessage.kt` | Room entity — individual messages |
| `data/ConversationState.kt` | Room entity — compaction state |
| `data/ConversationDao.kt` | Room DAO — all CRUD + search + Flow |
| `data/AppDatabase.kt` | Room database — v1, 3 entities |
| `data/DatabaseMigrations.kt` | Empty migrations array for v1 |
| `data/ConversationDaoTest.kt` | Unit tests — in-memory Room DB |

### Database Schema (v1)

```
conversation_threads
├── id          INTEGER PRIMARY KEY AUTOINCREMENT
├── title       TEXT NOT NULL
├── modelId     TEXT NOT NULL
├── taskId      TEXT NOT NULL
├── createdAt   INTEGER NOT NULL
├── updatedAt   INTEGER NOT NULL  [INDEX: index_threads_updated_at]
└── isStarred   INTEGER NOT NULL DEFAULT 0

conversation_messages
├── id              INTEGER PRIMARY KEY AUTOINCREMENT
├── threadId        INTEGER NOT NULL → conversation_threads(id) CASCADE DELETE  [INDEX]
├── content         TEXT NOT NULL
├── isUser          INTEGER NOT NULL
├── timestamp       INTEGER NOT NULL
├── imageUris       TEXT (nullable)
├── audioUri        TEXT (nullable)
├── audioSampleRate INTEGER (nullable)
└── messageType     TEXT NOT NULL DEFAULT 'TEXT'

conversation_state
├── threadId            INTEGER PRIMARY KEY → conversation_threads(id)
├── runningSummary      TEXT NOT NULL
├── turnsSummarized     INTEGER NOT NULL
└── lastCompactionTime  INTEGER NOT NULL
```

## MODIFIED

### `Android/src/app/gradle/libs.versions.toml`
```diff
+ androidx-room = "2.6.1"

  [libraries]
+ androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "androidx-room" }
+ androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "androidx-room" }
+ androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "androidx-room" }
```

### `Android/src/app/build.gradle.kts`
```diff
+ implementation(libs.androidx.room.runtime)
+ implementation(libs.androidx.room.ktx)
+ ksp(libs.androidx.room.compiler)
```

### `Android/src/app/src/main/java/ai/ondevice/app/di/AppModule.kt`
```diff
+ @Provides @Singleton
+ fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
+     return Room.databaseBuilder(context, AppDatabase::class.java, "ondevice_database")
+         .addMigrations(*ALL_MIGRATIONS)
+         .build()
+ }
+
+ @Provides @Singleton
+ fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()
```

## REMOVED

- Nothing removed

## NOT IN THIS CHANGE

- `ConversationRepository` — not in target; DAO injected directly into ViewModels
- `di/DatabaseModule.kt` — not in target; DB providers go into existing `AppModule.kt`
