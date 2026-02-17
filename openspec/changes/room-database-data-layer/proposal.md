# Proposal: room-database-data-layer

## Summary
Add Room database persistence layer for conversation history — 3 entities, 1 DAO, migrations file, and DB providers added to existing AppModule.

## Motivation
The app currently has no persistence. Every chat is lost when the user leaves. The target product has full conversation history with search, star, rename, and export. This data layer is the prerequisite for ALL history features — nothing else (ConversationListScreen, ConversationDetailScreen, chat resumption) can be built without it.

## Scope

### Included
- `AppDatabase` — Room database v1, 3 entities, 1 DAO, named `"ondevice_database"`
- `ConversationThread` entity — with `updatedAt` index
- `ConversationMessage` entity — with FK + threadId index
- `ConversationState` entity — compaction state (1:1 with ConversationThread)
- `ConversationDao` — all CRUD + search + Flow queries
- `DatabaseMigrations.kt` — empty `ALL_MIGRATIONS` array for v1 (ready for future migrations)
- `AppModule.kt` — extend existing module with `provideAppDatabase` and `provideConversationDao`
- Room + KSP dependencies in `libs.versions.toml` and `build.gradle.kts`
- Unit tests for DAO using in-memory database

### NOT Included
- `ConversationRepository` — target injects DAO directly into ViewModels (no repository wrapper)
- A separate `DatabaseModule` — DB providers added to existing `AppModule.kt`
- Any UI changes
- Chat screen wiring to database (future phase)
- Context compaction logic (future phase)

## Acceptance Criteria
- [ ] `AppDatabase` v1 builds without errors (3 entities, 1 DAO)
- [ ] `ConversationThread` has `updatedAt` index
- [ ] `ConversationMessage` has FK to threads + `threadId` index with CASCADE DELETE
- [ ] `ConversationState` has `threadId` as PK
- [ ] `ConversationDao` has all CRUD + Flow + search + upsert operations
- [ ] `DatabaseMigrations.kt` has empty `ALL_MIGRATIONS = arrayOf()`
- [ ] `AppModule.kt` provides `AppDatabase` and `ConversationDao` as singletons
- [ ] Room dependencies in `libs.versions.toml` + `build.gradle.kts`
- [ ] DAO unit tests pass with in-memory database
- [ ] CI GREEN, no existing functionality broken

## Technical Approach

### Database
- Name: `"ondevice_database"`
- Version: **1** (we start fresh, migrations added as schema evolves)
- `exportSchema = false`

### Entity: ConversationThread (`conversation_threads`)
```kotlin
@Entity(
    tableName = "conversation_threads",
    indices = [Index(value = ["updatedAt"], name = "index_threads_updated_at")]
)
data class ConversationThread(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelId: String,
    val taskId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false
)
```

### Entity: ConversationMessage (`conversation_messages`)
```kotlin
@Entity(
    tableName = "conversation_messages",
    foreignKeys = [ForeignKey(
        entity = ConversationThread::class,
        parentColumns = ["id"],
        childColumns = ["threadId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("threadId")]
)
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUris: String? = null,
    val audioUri: String? = null,
    val audioSampleRate: Int? = null,
    val messageType: String = "TEXT" // TEXT | IMAGE | TEXT_WITH_IMAGE | AUDIO_CLIP
)
```

### Entity: ConversationState (`conversation_state`)
```kotlin
@Entity(tableName = "conversation_state")
data class ConversationState(
    @PrimaryKey val threadId: Long,
    val runningSummary: String,
    val turnsSummarized: Int,
    val lastCompactionTime: Long
)
```

### DatabaseMigrations.kt
```kotlin
val ALL_MIGRATIONS = arrayOf<Migration>()
// Future migrations added here as schema evolves
```

### AppModule.kt additions
```kotlin
@Provides @Singleton
fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "ondevice_database")
        .addMigrations(*ALL_MIGRATIONS)
        .build()
}

@Provides @Singleton
fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()
```

### Dependencies
```toml
# libs.versions.toml
androidx-room = "2.6.1"
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "androidx-room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "androidx-room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "androidx-room" }
```
```kts
# build.gradle.kts
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
```

## References
- OpenSpec: `OnDeviceAI-OpenSpec/OPENSPEC-FOUNDATION.md` §4.1
- Target reference: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/data/`
- Target DI: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/di/AppModule.kt`
