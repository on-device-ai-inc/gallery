# Tasks: room-database-data-layer

## Implementation Tasks

- [x] **Task 1**: Add Room dependencies to `libs.versions.toml`
  - Add `androidx-room` version `2.6.1`
  - Add `androidx-room-runtime`, `androidx-room-ktx`, `androidx-room-compiler` entries
  - Acceptance: `libs.versions.toml` has all 3 Room library entries

- [x] **Task 2**: Add Room dependencies to `app/build.gradle.kts`
  - Add `implementation(libs.androidx.room.runtime)`
  - Add `implementation(libs.androidx.room.ktx)`
  - Add `ksp(libs.androidx.room.compiler)`
  - Acceptance: Gradle sync succeeds (CI build passes)

- [x] **Task 3**: Create `ConversationThread` entity
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/ConversationThread.kt`
  - Fields: id, title, modelId, taskId, createdAt, updatedAt, isStarred
  - Table name: `conversation_threads`
  - Index on `updatedAt` named `index_threads_updated_at`
  - Acceptance: Compiles, all fields annotated correctly, index defined

- [x] **Task 4**: Create `ConversationMessage` entity
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/ConversationMessage.kt`
  - Fields: id, threadId (FK+index), content, isUser, timestamp, imageUris, audioUri, audioSampleRate, messageType
  - Table name: `conversation_messages`
  - Foreign key: threadId → conversation_threads(id) ON DELETE CASCADE
  - Acceptance: Compiles, FK constraint and index defined correctly

- [x] **Task 5**: Create `ConversationState` entity
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/ConversationState.kt`
  - Fields: threadId (PK), runningSummary, turnsSummarized, lastCompactionTime
  - Table name: `conversation_state`
  - Acceptance: Compiles, threadId is PK

- [x] **Task 6**: Create `ConversationDao` interface
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/ConversationDao.kt`
  - All CRUD operations + Flow variants for list queries
  - `@Upsert` for ConversationState (Room 2.5+)
  - Acceptance: All operations compile, Flow imports present

- [x] **Task 7**: Create `AppDatabase` class
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/AppDatabase.kt`
  - `@Database(entities = [...], version = 1, exportSchema = false)`
  - Abstract function `conversationDao(): ConversationDao`
  - Acceptance: Compiles, Room generates the implementation

- [x] **Task 8**: Create `DatabaseMigrations.kt`
  - File: `Android/src/app/src/main/java/ai/ondevice/app/data/DatabaseMigrations.kt`
  - `val ALL_MIGRATIONS = arrayOf<Migration>()`
  - Acceptance: File compiles, array is empty (v1 needs no migrations)

- [x] **Task 9**: Extend `AppModule.kt` with DB providers
  - File: `Android/src/app/src/main/java/ai/ondevice/app/di/AppModule.kt`
  - Add `@Provides @Singleton fun provideAppDatabase(...)` using `Room.databaseBuilder` + `addMigrations(*ALL_MIGRATIONS)`
  - Add `@Provides @Singleton fun provideConversationDao(...)` returning `database.conversationDao()`
  - Acceptance: Hilt can inject both AppDatabase and ConversationDao, no separate DatabaseModule file

## Testing Tasks

- [x] **Task 10**: Write DAO unit tests using in-memory database
  - File: `Android/src/app/src/androidTest/java/ai/ondevice/app/data/ConversationDaoTest.kt`
  - Test: insert thread → retrieve by id ✓
  - Test: insert message → retrieve for thread ✓
  - Test: delete thread → messages cascade deleted ✓
  - Test: searchThreads by title ✓
  - Test: updateStarred toggles correctly ✓
  - Test: saveConversationState upserts correctly ✓
  - Acceptance: All tests pass (via CI androidTest)

## CI/Build Tasks

- [x] **Task 11**: Push and verify CI is GREEN
  - CI run: 22106724534 (Build APK/AAB) ✅ SUCCESS
  - CI run: 22106724550 (Android CI) ✅ SUCCESS
  - APK installed: adb install Success
  - App launched: mFocusedApp=ai.ondevice.app/.MainActivity (no crash)

## Documentation Tasks

- [x] **Task 12**: Update `CODE_INDEX.md` with new files
  - Add: ConversationThread.kt, ConversationMessage.kt, ConversationState.kt
  - Add: ConversationDao.kt, AppDatabase.kt, DatabaseMigrations.kt
  - Note: AppModule.kt modified (not new)
  - Acceptance: Index reflects new data layer files

## Completion Checklist

- [x] All 9 implementation tasks done
- [x] DAO unit tests written
- [x] CI GREEN (both workflows)
- [x] No existing functionality broken (app installs, launches, MainActivity running)
- [x] CODE_INDEX.md updated
- [x] Ready for `/openspec-archive room-database-data-layer`
