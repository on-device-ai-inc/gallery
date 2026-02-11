# Prompt Engineering Implementation Roadmap
**Version:** 1.0
**Last Updated:** 2025-12-31
**Status:** Phase 1 Complete (90%), Phase 2-4 Pending

---

## Executive Summary

This document outlines the complete implementation roadmap for the prompt engineering system in the OnDevice Android app. The system enhances on-device LLM responses through intelligent persona injection, token monitoring, and context management.

**Current Status:**
- ✅ Phase 1: Core Infrastructure (90% complete - token persistence disabled)
- ⏳ Phase 2: Context Compression (0% - not started)
- ⏳ Phase 3: User Interface (0% - not started)
- ⏳ Phase 4: Testing & Validation (0% - not started)

**Priority:** HIGH - Persona injection is working but token monitoring is disabled, limiting context safety

---

## Table of Contents

1. [Phase 1: Core Infrastructure (Completion)](#phase-1)
2. [Phase 2: Context Compression](#phase-2)
3. [Phase 3: User Interface](#phase-3)
4. [Phase 4: Testing & Validation](#phase-4)
5. [Implementation Guidelines](#implementation-guidelines)
6. [File References](#file-references)

---

<a name="phase-1"></a>
## Phase 1: Core Infrastructure (Completion)

### Current Status: 90% Complete

**Completed Components:**
- ✅ PersonaVariant enum with 5 variants (MINIMAL to MAXIMUM)
- ✅ PersonaLibrary with research-backed persona text
- ✅ PersonaManager with formatting methods
- ✅ TokenMonitor with thread-safe estimation
- ✅ Database schema with persona/token fields
- ✅ Database migration (MIGRATION_3_4)
- ✅ Hilt dependency injection providers
- ✅ First message persona injection in LlmChatViewModel
- ✅ Web search integration (prepends before persona)

**Pending Work:**

### Task 1.1: Enable Token Count Persistence
**Priority:** HIGH
**Estimated Effort:** 2-4 hours

**Problem:**
Token monitoring is currently disabled in LlmChatViewModel (lines 244-245):
```kotlin
// Note: Token monitoring removed temporarily - requires architectural changes
// to ChatViewModel to expose currentThreadId and conversationDao
```

**Root Cause:**
- `ChatViewModel` doesn't expose `currentThreadId` as protected
- `ChatViewModel` doesn't expose `conversationDao` as protected
- `LlmChatViewModelBase` cannot access these fields to update token counts

**Solution:**
1. Modify `ChatViewModel.kt` to expose:
   ```kotlin
   protected var currentThreadId: Long? = null
   protected val conversationDao: ConversationDao
   ```

2. Re-enable token monitoring in `LlmChatViewModel.kt`:
   ```kotlin
   // After line 242 (when inference completes)
   if (tokenMonitor != null && currentThreadId != null) {
     viewModelScope.launch(Dispatchers.IO) {
       // Get all messages for this model
       val messages = uiState.value.messagesByModel[model.name] ?: listOf()

       // Estimate tokens
       var totalTokens = 0
       messages.forEach { message ->
         when (message) {
           is ChatMessageText -> totalTokens += tokenMonitor.estimateTokens(message.content)
           is ChatMessageImage -> totalTokens += tokenMonitor.estimateImageTokens(message.bitmaps.size)
           is ChatMessageAudioClip -> totalTokens += tokenMonitor.estimateAudioTokens(message.getDurationInSeconds())
         }
       }

       // Update database
       conversationDao.updateTokenCount(currentThreadId!!, totalTokens)
     }
   }
   ```

3. Add `updateTokenCount` method to `ConversationDao.kt`:
   ```kotlin
   @Query("UPDATE conversation_threads SET estimatedTokens = :tokens, lastTokenUpdate = :timestamp WHERE id = :threadId")
   suspend fun updateTokenCount(threadId: Long, tokens: Int, timestamp: Long = System.currentTimeMillis())
   ```

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/common/chat/ChatViewModel.kt`
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt`
- `app/src/main/java/ai/ondevice/app/data/ConversationDao.kt`

**Testing:**
- Send messages and verify `estimatedTokens` field updates in database
- Check `lastTokenUpdate` timestamp updates correctly
- Verify thread-safety with rapid message sending

---

### Task 1.2: Implement Token Count Loading on Thread Load
**Priority:** HIGH
**Estimated Effort:** 1-2 hours

**Problem:**
When user reopens an existing conversation, TokenMonitor starts at 0 instead of loading persisted count.

**Solution:**
1. Modify `ChatViewModel.loadThread()` to initialize TokenMonitor:
   ```kotlin
   fun loadThread(threadId: Long, model: Model) {
     viewModelScope.launch {
       val thread = conversationDao.getThreadById(threadId)
       if (thread != null) {
         currentThreadId = threadId

         // Initialize TokenMonitor with persisted count
         tokenMonitor?.reset()
         tokenMonitor?.let { monitor ->
           // Re-add all messages to token monitor
           thread.messages.forEach { message ->
             when (message) {
               is ChatMessageText -> monitor.addMessage(message.content)
               is ChatMessageImage -> monitor.addMessage(imageCount = message.bitmaps.size)
               is ChatMessageAudioClip -> monitor.addMessage(audioDuration = message.getDurationInSeconds())
             }
           }
         }

         // Load messages into UI...
       }
     }
   }
   ```

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/common/chat/ChatViewModel.kt`

**Testing:**
- Create conversation with 10+ messages
- Close app
- Reopen conversation
- Verify token count matches previous count (check logs)

---

<a name="phase-2"></a>
## Phase 2: Context Compression

### Status: Not Started (0%)

**Goal:** Automatically compress conversation history when approaching context window limits to prevent model degradation.

### Task 2.1: Implement Conversation Summarization
**Priority:** MEDIUM
**Estimated Effort:** 8-12 hours

**Requirements:**
- Detect when token usage exceeds 84% (APPROACHING warning level)
- Compress older messages while preserving recent context
- Maintain conversation coherence and key information

**Compression Strategy:**

1. **Sliding Window Approach** (Recommended for Phase 2)
   - Keep last N messages verbatim (e.g., last 5 exchanges = 10 messages)
   - Summarize or discard older messages
   - Preserve persona injection (always in first message)
   - Simple to implement, predictable behavior

2. **Intelligent Summarization** (Future enhancement)
   - Use on-device model to summarize conversation segments
   - Extract key facts, decisions, context
   - Requires additional inference overhead

**Implementation:**

Create `CompressionManager.kt`:
```kotlin
package ai.ondevice.app.compression

class CompressionManager @Inject constructor(
  private val tokenMonitor: TokenMonitor
) {

  /**
   * Compress conversation history using sliding window approach.
   *
   * @param messages Full conversation history
   * @param keepRecentCount Number of recent messages to keep verbatim (default: 10)
   * @return Compressed message list
   */
  fun compressConversation(
    messages: List<ChatMessage>,
    keepRecentCount: Int = 10
  ): Pair<List<ChatMessage>, CompressionResult> {

    val contextUsage = tokenMonitor.calculateContextUsage()

    // No compression needed
    if (contextUsage.warningLevel == WarningLevel.OK) {
      return Pair(messages, CompressionResult.NotNeeded)
    }

    // Always keep first message (contains persona)
    val firstMessage = messages.firstOrNull()

    // Keep last N messages
    val recentMessages = messages.takeLast(keepRecentCount)

    // Messages in the middle to compress
    val middleMessages = messages.drop(1).dropLast(keepRecentCount)

    if (middleMessages.isEmpty()) {
      return Pair(messages, CompressionResult.InsufficientHistory)
    }

    // Create compression summary
    val summaryText = createSummary(middleMessages)
    val summaryMessage = ChatMessageText(
      content = "[Previous conversation summary: $summaryText]",
      side = ChatSide.SYSTEM,
      latencyMs = -1f
    )

    // Rebuild conversation: first + summary + recent
    val compressedMessages = mutableListOf<ChatMessage>()
    if (firstMessage != null) compressedMessages.add(firstMessage)
    compressedMessages.add(summaryMessage)
    compressedMessages.addAll(recentMessages)

    // Calculate compression ratio
    val originalTokens = tokenMonitor.estimateTokens(messages.joinToString(" ") { it.toString() })
    val compressedTokens = tokenMonitor.estimateTokens(compressedMessages.joinToString(" ") { it.toString() })
    val savedTokens = originalTokens - compressedTokens

    return Pair(
      compressedMessages,
      CompressionResult.Success(
        originalCount = messages.size,
        compressedCount = compressedMessages.size,
        tokensSaved = savedTokens,
        compressionRatio = savedTokens.toFloat() / originalTokens
      )
    )
  }

  private fun createSummary(messages: List<ChatMessage>): String {
    // Simple extraction of key points
    // Future: Use on-device model for intelligent summarization
    val userQueries = messages.filterIsInstance<ChatMessageText>()
      .filter { it.side == ChatSide.USER }
      .take(3)
      .joinToString("; ") { it.content.take(100) }

    return "User discussed: $userQueries. ${messages.size} messages compressed."
  }
}

sealed class CompressionResult {
  object NotNeeded : CompressionResult()
  object InsufficientHistory : CompressionResult()
  data class Success(
    val originalCount: Int,
    val compressedCount: Int,
    val tokensSaved: Int,
    val compressionRatio: Float
  ) : CompressionResult()
}
```

**Integration Points:**
- Trigger compression before calling `generateResponse()` if `warningLevel != OK`
- Update database with compressed message history
- Log compression events for analytics

**Files to Create:**
- `app/src/main/java/ai/ondevice/app/compression/CompressionManager.kt`
- `app/src/main/java/ai/ondevice/app/compression/CompressionResult.kt`

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt` (add compression trigger)
- `app/src/main/java/ai/ondevice/app/di/AppModule.kt` (add Hilt provider)

**Testing:**
- Create conversation with 50+ messages
- Verify compression triggers at 84% threshold
- Verify compressed conversation maintains coherence
- Test edge cases: all images, all text, mixed media

---

### Task 2.2: Add Compression UI Feedback
**Priority:** LOW
**Estimated Effort:** 2-3 hours

**Requirements:**
- Show toast/snackbar when compression occurs
- Display compression stats (e.g., "Conversation compressed: 45 → 15 messages")
- Optional: Allow user to view compression summary

**Implementation:**
Add to `LlmChatViewModel.kt`:
```kotlin
// After compression
val result = compressionManager.compressConversation(messages)
if (result is CompressionResult.Success) {
  showCompressionNotification(
    "Conversation optimized: ${result.originalCount} → ${result.compressedCount} messages, " +
    "${result.tokensSaved} tokens saved"
  )
}
```

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatScreen.kt` (add snackbar)
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt` (add notification trigger)

---

<a name="phase-3"></a>
## Phase 3: User Interface

### Status: Not Started (0%)

**Goal:** Provide users with visibility into persona selection, token usage, and context management.

### Task 3.1: Add Persona Selection Dialog
**Priority:** MEDIUM
**Estimated Effort:** 6-8 hours

**Requirements:**
- Allow users to select persona variant per conversation thread
- Display token cost and description for each variant
- Persist selection in database (`personaVariant` field)
- Default to BALANCED variant for new conversations

**UI Design:**

Create `PersonaSelectionDialog.kt`:
```kotlin
@Composable
fun PersonaSelectionDialog(
  currentVariant: PersonaVariant,
  onDismiss: () -> Unit,
  onVariantSelected: (PersonaVariant) -> Unit,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select AI Persona") },
    text = {
      LazyColumn {
        items(PersonaVariant.values()) { variant ->
          PersonaVariantItem(
            variant = variant,
            selected = variant == currentVariant,
            onClick = { onVariantSelected(variant) }
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
    modifier = modifier
  )
}

@Composable
private fun PersonaVariantItem(
  variant: PersonaVariant,
  selected: Boolean,
  onClick: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp)
      .background(
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(12.dp)
  ) {
    RadioButton(
      selected = selected,
      onClick = onClick
    )
    Column(
      modifier = Modifier
        .padding(start = 12.dp)
        .weight(1f)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = variant.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${variant.getTokenCount()} tokens",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Text(
        text = variant.getDescription(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
      )
      // Context usage indicator
      LinearProgressIndicator(
        progress = variant.getContextUsagePercent2K() / 100f,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .height(4.dp),
        color = when {
          variant.getContextUsagePercent2K() > 20 -> MaterialTheme.colorScheme.error
          variant.getContextUsagePercent2K() > 10 -> MaterialTheme.colorScheme.tertiary
          else -> MaterialTheme.colorScheme.primary
        }
      )
      Text(
        text = "${variant.getContextUsagePercent2K()}% of 2K context",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}
```

**Integration:**
- Add "Persona" button to ModelPageAppBar (next to settings)
- Show dialog when button clicked
- Update `ConversationThread.personaVariant` on selection
- Reload conversation with new persona (requires reset)

**Files to Create:**
- `app/src/main/java/ai/ondevice/app/ui/common/PersonaSelectionDialog.kt`

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/common/ModelPageAppBar.kt` (add persona button)
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt` (add persona change handler)
- `app/src/main/java/ai/ondevice/app/data/ConversationDao.kt` (add update method)

---

### Task 3.2: Add Context Usage Indicator
**Priority:** MEDIUM
**Estimated Effort:** 4-6 hours

**Requirements:**
- Display real-time token usage in chat interface
- Color-coded indicator: Green (OK), Yellow (APPROACHING), Red (CRITICAL)
- Show remaining tokens and percentage
- Optionally show compression recommendation

**UI Design:**

Add to `LlmChatScreen.kt`:
```kotlin
@Composable
fun ContextUsageIndicator(
  contextUsage: ContextUsage,
  onCompressClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (contextUsage.warningLevel) {
        WarningLevel.OK -> MaterialTheme.colorScheme.primaryContainer
        WarningLevel.APPROACHING -> MaterialTheme.colorScheme.tertiaryContainer
        WarningLevel.CRITICAL -> MaterialTheme.colorScheme.errorContainer
      }
    )
  ) {
    Column(
      modifier = Modifier.padding(12.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Context Usage",
          style = MaterialTheme.typography.labelMedium
        )
        Text(
          text = "${contextUsage.usedTokens} / ${contextUsage.totalTokens} tokens",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold
        )
      }

      LinearProgressIndicator(
        progress = contextUsage.usagePercent / 100f,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .height(6.dp),
        color = when (contextUsage.warningLevel) {
          WarningLevel.OK -> MaterialTheme.colorScheme.primary
          WarningLevel.APPROACHING -> MaterialTheme.colorScheme.tertiary
          WarningLevel.CRITICAL -> MaterialTheme.colorScheme.error
        }
      )

      Text(
        text = contextUsage.getStatusMessage(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Show compression button if recommended
      if (contextUsage.shouldRecommendCompression()) {
        OutlinedButton(
          onClick = onCompressClicked,
          modifier = Modifier
            .align(Alignment.End)
            .padding(top = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Compress,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Optimize",
            modifier = Modifier.padding(start = 4.dp)
          )
        }
      }
    }
  }
}
```

**Integration:**
- Display below top app bar when `warningLevel != OK`
- Update in real-time as messages are sent
- Auto-hide when usage drops below threshold

**Files to Create:**
- `app/src/main/java/ai/ondevice/app/ui/common/ContextUsageIndicator.kt`

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatScreen.kt` (add indicator)
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt` (expose contextUsage state)

---

### Task 3.3: Add Settings Page for Prompt Engineering
**Priority:** LOW
**Estimated Effort:** 4-6 hours

**Requirements:**
- Global default persona variant setting
- Enable/disable token monitoring
- Enable/disable auto-compression
- Compression threshold slider (80-95%)
- Context window size override (for future model support)

**UI Location:**
- Add to existing Settings screen
- New section: "AI Behavior"

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/settings/SettingsScreen.kt`
- Create `PromptEngineeringPreferencesDataStore.kt` for persistence

---

<a name="phase-4"></a>
## Phase 4: Testing & Validation

### Status: Not Started (0%)

**Goal:** Validate prompt engineering system across all persona variants with comprehensive testing.

### Task 4.1: Unit Tests for Core Components
**Priority:** HIGH
**Estimated Effort:** 8-12 hours

**Test Coverage:**

1. **PersonaManager Tests** (`PersonaManagerTest.kt`)
   - Test all 5 variants format correctly
   - Test single message vs multi-message formatting
   - Test continuation formatting
   - Test message validation
   - Test null/empty inputs

2. **TokenMonitor Tests** (`TokenMonitorTest.kt`)
   - Test text token estimation accuracy
   - Test image token estimation (257 per image)
   - Test audio token estimation (1 per 150ms)
   - Test thread-safety with concurrent operations
   - Test warning level thresholds
   - Test context usage calculations

3. **CompressionManager Tests** (`CompressionManagerTest.kt`)
   - Test sliding window compression
   - Test compression ratio calculations
   - Test edge cases (no compression needed, insufficient history)
   - Test persona preservation in compression

**Test Framework:**
- JUnit 4
- MockK for mocking
- Coroutines Test for async testing

**Example Test:**
```kotlin
@Test
fun `formatSingleMessageWithPersona should prepend persona text`() {
  val manager = PersonaManager()
  val input = "What is the weather today?"
  val variant = PersonaVariant.BALANCED

  val result = manager.formatSingleMessageWithPersona(input, variant)

  assertTrue(result.startsWith(PersonaLibrary.getPersona(variant)))
  assertTrue(result.contains(input))
  assertTrue(result.contains("\n\n"))
}
```

**Files to Create:**
- `app/src/test/java/ai/ondevice/app/persona/PersonaManagerTest.kt`
- `app/src/test/java/ai/ondevice/app/compression/TokenMonitorTest.kt`
- `app/src/test/java/ai/ondevice/app/compression/CompressionManagerTest.kt`

---

### Task 4.2: Integration Tests
**Priority:** MEDIUM
**Estimated Effort:** 6-8 hours

**Test Scenarios:**

1. **End-to-End Conversation Flow**
   - Start new conversation
   - Send first message (verify persona injection)
   - Send 20+ messages (verify token monitoring)
   - Verify database updates
   - Trigger compression (verify compression works)
   - Verify conversation coherence after compression

2. **Persona Switching**
   - Create conversation with BALANCED variant
   - Switch to MAXIMUM variant mid-conversation
   - Verify reset occurs
   - Verify new persona applied

3. **Database Persistence**
   - Create conversation with custom persona
   - Close app
   - Reopen conversation
   - Verify persona and token counts persist

**Test Framework:**
- Espresso for UI testing
- Room in-memory database for data layer testing

**Files to Create:**
- `app/src/androidTest/java/ai/ondevice/app/LlmChatIntegrationTest.kt`
- `app/src/androidTest/java/ai/ondevice/app/PersonaPersistenceTest.kt`

---

### Task 4.3: Benchmark Tests for All Persona Variants
**Priority:** MEDIUM
**Estimated Effort:** 4-6 hours

**Benchmark Metrics:**

For each variant (MINIMAL, STREAMLINED, BALANCED, COMPREHENSIVE, MAXIMUM):
1. **Response Quality** (subjective - manual testing)
   - Coherence score (1-5)
   - Instruction following accuracy (%)
   - Creativity score (1-5)
   - Professionalism score (1-5)

2. **Performance Metrics** (automated)
   - Time to first token (ms)
   - Decode speed (tokens/s)
   - Total latency (ms)
   - Memory usage (MB)

3. **Token Efficiency**
   - Actual token count vs estimated
   - Context usage after 10/20/30 messages
   - Compression trigger frequency

**Test Suite:**
```kotlin
@Test
fun `benchmark all persona variants`() {
  val testPrompts = listOf(
    "Explain quantum computing in simple terms",
    "Write a function to reverse a string",
    "What are the health benefits of green tea?",
    "Summarize the plot of Romeo and Juliet"
  )

  PersonaVariant.values().forEach { variant ->
    testPrompts.forEach { prompt ->
      val result = benchmarkVariant(variant, prompt)

      Log.d("Benchmark", """
        Variant: ${variant.name}
        Prompt: $prompt
        Time to first token: ${result.ttft}ms
        Decode speed: ${result.decodeSpeed} tokens/s
        Total tokens: ${result.totalTokens}
        Memory: ${result.memoryMB}MB
      """.trimIndent())
    }
  }
}
```

**Deliverable:**
- Benchmark report with performance comparison table
- Recommendation for default variant based on data

**Files to Create:**
- `app/src/androidTest/java/ai/ondevice/app/PersonaBenchmarkTest.kt`

---

### Task 4.4: User Acceptance Testing (UAT)
**Priority:** HIGH
**Estimated Effort:** 8-12 hours (manual testing)

**Test Plan:**

1. **Scenario 1: First-Time User**
   - Install app
   - Download model
   - Start first conversation
   - Verify default persona (BALANCED) provides good experience
   - Send 10+ messages
   - Verify no errors or confusion

2. **Scenario 2: Power User**
   - Create conversation with MAXIMUM variant
   - Send 50+ messages
   - Verify compression triggers
   - Verify compression notification
   - Verify conversation quality maintained

3. **Scenario 3: Persona Switching**
   - Start with MINIMAL variant
   - Switch to COMPREHENSIVE mid-conversation
   - Verify reset warning shown
   - Verify new persona behavior

4. **Scenario 4: Context Limit Edge Case**
   - Send very long messages (1000+ characters each)
   - Add multiple images
   - Verify CRITICAL warning shown
   - Verify compression works
   - Verify model doesn't crash

**Deliverable:**
- UAT report with pass/fail for each scenario
- User feedback notes
- Bug reports for any issues found

---

<a name="implementation-guidelines"></a>
## Implementation Guidelines

### Development Workflow

1. **Branch Strategy:**
   - Create feature branch: `feature/prompt-engineering-phase-X`
   - One branch per phase
   - Merge to `main` after phase completion

2. **Code Review Checklist:**
   - [ ] All new code has KDoc comments
   - [ ] Thread-safety verified for token monitoring
   - [ ] Database migrations tested (no data loss)
   - [ ] UI responsive on small screens (compact spacing)
   - [ ] No hardcoded strings (use string resources)
   - [ ] Logging added for debugging
   - [ ] Error handling for null cases

3. **Testing Workflow:**
   - Write unit tests BEFORE implementation (TDD)
   - Run all tests before commit
   - Manual testing on physical device after each task
   - Update README.md with any API changes

### Architecture Principles

1. **Dependency Injection:**
   - All managers provided via Hilt as singletons
   - ViewModels receive dependencies via constructor
   - Graceful degradation if dependencies null

2. **Thread Safety:**
   - All token operations use `ReentrantReadWriteLock`
   - Database operations on `Dispatchers.IO`
   - UI updates on `Dispatchers.Main`

3. **Separation of Concerns:**
   - PersonaManager: Formatting only, no state
   - TokenMonitor: Estimation and tracking, no business logic
   - CompressionManager: Compression logic, no UI
   - ViewModels: Orchestration and state management

4. **Error Handling:**
   - Log all errors with TAG
   - Graceful fallback (e.g., no compression if compression fails)
   - User-friendly error messages (no stack traces in UI)

### Performance Considerations

1. **Token Estimation:**
   - Conservative 4:1 ratio to avoid underestimation
   - Future: Integrate actual tokenizer library for precision

2. **Database Updates:**
   - Batch updates (don't update on every message)
   - Update token count only when conversation pauses (2s debounce)

3. **Compression:**
   - Only compress when necessary (APPROACHING or CRITICAL)
   - Cache compression results to avoid redundant work

### Security & Privacy

1. **On-Device Only:**
   - All persona text embedded in APK (no network calls)
   - Token monitoring purely local
   - No telemetry for persona selection

2. **Data Privacy:**
   - Conversation data never leaves device
   - Token counts stored locally in Room database
   - No analytics tracking for prompt engineering features

---

<a name="file-references"></a>
## File References

### Existing Files (DO NOT DELETE)

**Specification & Documentation:**
- `/openspec/specs/prompt-engineering/spec.md` - Technical specification (v1.0)
- `/app/src/main/java/ai/ondevice/app/persona/README.md` - Usage guide

**Core Implementation:**
- `/app/src/main/java/ai/ondevice/app/persona/PersonaVariant.kt` - Enum with 5 variants
- `/app/src/main/java/ai/ondevice/app/persona/PersonaLibrary.kt` - Persona text storage
- `/app/src/main/java/ai/ondevice/app/persona/PersonaManager.kt` - Formatting manager
- `/app/src/main/java/ai/ondevice/app/compression/TokenMonitor.kt` - Token monitoring

**Data Layer:**
- `/app/src/main/java/ai/ondevice/app/data/ConversationThread.kt` - Database entity
- `/app/src/main/java/ai/ondevice/app/data/ConversationDao.kt` - Database DAO
- `/app/src/main/java/ai/ondevice/app/data/AppDatabase.kt` - Database migrations

**Dependency Injection:**
- `/app/src/main/java/ai/ondevice/app/di/AppModule.kt` - Hilt providers

**ViewModels:**
- `/app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt` - Main ViewModel
- `/app/src/main/java/ai/ondevice/app/ui/common/chat/ChatViewModel.kt` - Base ViewModel

### New Files to Create

**Phase 1 Completion:**
- No new files (modify existing only)

**Phase 2:**
- `/app/src/main/java/ai/ondevice/app/compression/CompressionManager.kt`
- `/app/src/main/java/ai/ondevice/app/compression/CompressionResult.kt`

**Phase 3:**
- `/app/src/main/java/ai/ondevice/app/ui/common/PersonaSelectionDialog.kt`
- `/app/src/main/java/ai/ondevice/app/ui/common/ContextUsageIndicator.kt`
- `/app/src/main/java/ai/ondevice/app/data/PromptEngineeringPreferencesDataStore.kt`

**Phase 4:**
- `/app/src/test/java/ai/ondevice/app/persona/PersonaManagerTest.kt`
- `/app/src/test/java/ai/ondevice/app/compression/TokenMonitorTest.kt`
- `/app/src/test/java/ai/ondevice/app/compression/CompressionManagerTest.kt`
- `/app/src/androidTest/java/ai/ondevice/app/LlmChatIntegrationTest.kt`
- `/app/src/androidTest/java/ai/ondevice/app/PersonaPersistenceTest.kt`
- `/app/src/androidTest/java/ai/ondevice/app/PersonaBenchmarkTest.kt`

---

## Priority Summary

### Immediate (Complete Phase 1)
1. ✅ **Task 1.1:** Enable token count persistence (2-4 hrs)
2. ✅ **Task 1.2:** Load token count on thread open (1-2 hrs)

### Short-Term (Phase 2 - Context Safety)
3. 🟡 **Task 2.1:** Implement compression (8-12 hrs)
4. 🟡 **Task 2.2:** Add compression UI feedback (2-3 hrs)

### Medium-Term (Phase 3 - User Control)
5. 🟡 **Task 3.1:** Persona selection dialog (6-8 hrs)
6. 🟡 **Task 3.2:** Context usage indicator (4-6 hrs)

### Long-Term (Phase 4 - Validation)
7. 🟢 **Task 4.1:** Unit tests (8-12 hrs)
8. 🟢 **Task 4.2:** Integration tests (6-8 hrs)
9. 🟢 **Task 4.3:** Benchmark tests (4-6 hrs)
10. 🟢 **Task 4.4:** User acceptance testing (8-12 hrs)

**Total Estimated Effort:** 59-87 hours

---

## Success Metrics

### Phase 1 Success Criteria
- [ ] Token counts persist correctly in database
- [ ] Token counts reload on thread open
- [ ] No crashes related to token monitoring
- [ ] Logs show accurate token estimates

### Phase 2 Success Criteria
- [ ] Compression triggers at correct thresholds
- [ ] Compressed conversations maintain coherence
- [ ] Compression ratio >= 40% (e.g., 45 → 15 messages)
- [ ] No data loss during compression

### Phase 3 Success Criteria
- [ ] Users can select persona variants
- [ ] Context usage visible in real-time
- [ ] UI responsive on all screen sizes
- [ ] Settings persist across app restarts

### Phase 4 Success Criteria
- [ ] 90%+ unit test coverage for core components
- [ ] All integration tests pass
- [ ] Benchmark report shows BALANCED variant optimal for most users
- [ ] UAT finds < 5 critical bugs

---

## Maintenance & Future Work

### Post-Launch Monitoring
- Monitor crash reports for persona/token-related errors
- Collect analytics on persona variant usage (if user consents)
- Monitor compression frequency and effectiveness

### Future Enhancements (Post-Phase 4)
- **Intelligent Summarization:** Use on-device model to create context summaries
- **Multi-Model Support:** Different personas for different model sizes
- **Custom Personas:** Allow users to write their own personas
- **Persona Templates:** Industry-specific personas (medical, legal, creative, etc.)
- **Advanced Compression:** Hierarchical summarization, importance scoring
- **Token Count Accuracy:** Integrate actual tokenizer library (model-specific)
- **Context Window Auto-Detection:** Detect model's actual context limit

---

## Questions & Support

**For questions about this implementation:**
- Review `/app/src/main/java/ai/ondevice/app/persona/README.md` for usage examples
- Review `/openspec/specs/prompt-engineering/spec.md` for technical details
- Check existing code for patterns (PersonaManager, TokenMonitor)

**Before starting implementation:**
- Read all existing persona/compression package code
- Review database schema and migration history
- Test on physical device with real model

---

**Document Version:** 1.0
**Last Updated:** 2025-12-31
**Next Review:** After Phase 1 completion
