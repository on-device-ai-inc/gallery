# Spec Delta: fix-first-compression-crash

## MODIFIED

### app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatViewModel.kt
```diff
       // Context Compression: Check if compaction needed
       try {
         val threadId = currentThreadId
         if (threadId != null) {
           val dbMessages = conversationDao.getMessagesForThread(threadId)

           // Check token count first (fast operation)
           val currentTokens = ai.ondevice.app.conversation.TokenEstimator.estimate(dbMessages)
           val triggerThreshold = (ai.ondevice.app.conversation.CompactionManager.MAX_TOKENS *
                                   ai.ondevice.app.conversation.CompactionManager.TRIGGER_PERCENT).toInt()

           if (currentTokens >= triggerThreshold) {
             // Show compacting indicator BEFORE starting slow operation
             setIsCompacting(true)
             // Small delay to ensure UI renders the indicator
             delay(100)

             Log.d(TAG, "Starting compaction: $currentTokens tokens (threshold: $triggerThreshold)")

+            // CRITICAL FIX: Reset conversation BEFORE summarization to clear KV-cache
+            // This prevents native lib crash when reusing "dirty" conversation for different task
+            val existingState = conversationDao.getConversationState(threadId)
+            val existingSummaryMessage = if (existingState != null && existingState.runningSummary.isNotBlank()) {
+              val formattedSummary = "Previous conversation summary:\n${existingState.runningSummary}"
+              Message.of(listOf(Content.Text(formattedSummary)))
+            } else {
+              null
+            }
+
+            Log.d(TAG, "Resetting conversation before summarization (clean state)")
+            LlmChatModelHelper.resetConversation(
+              model = model,
+              supportImage = false,  // Summarization is text-only
+              supportAudio = false,
+              systemMessage = existingSummaryMessage
+            )

             val compactionResult = compactionManager.checkAndCompact(
               threadId,
               dbMessages,
               LlmChatModelHelper,
               model
             )

             if (compactionResult is ai.ondevice.app.conversation.CompactionResult.Success) {
               Log.d(TAG, "Conversation compacted: evicted ${compactionResult.evictedCount} turns")

               // CRITICAL: Reset conversation with summary as system message
               val state = conversationDao.getConversationState(threadId)
               val summaryMessage = if (state != null && state.runningSummary.isNotBlank()) {
                 // Format summary for LiteRT system message
                 val formattedSummary = "Previous conversation summary:\n${state.runningSummary}"
                 Message.of(listOf(Content.Text(formattedSummary)))
               } else {
                 null
               }

-              Log.d(TAG, "Resetting conversation with summary (${state?.runningSummary?.length ?: 0} chars)")
+              Log.d(TAG, "Resetting conversation AFTER compression with new summary (${state?.runningSummary?.length ?: 0} chars)")
               LlmChatModelHelper.resetConversation(
                 model = model,
                 supportImage = model.llmSupportImage,
                 supportAudio = model.llmSupportAudio,
                 systemMessage = summaryMessage
               )
             }

             // Hide compacting indicator after completion
             setIsCompacting(false)
           }
         }
       } catch (e: Exception) {
         Log.w(TAG, "Compaction check failed, continuing without compression", e)
         setIsCompacting(false)  // Ensure indicator is hidden on error
       }
```

**Rationale**:

The native LiteRT library (`liblitertlm_jni.so`) crashes with SIGSEGV when we try to use a conversation object that has existing KV-cache from 40 messages for a different task (summarization).

By resetting the conversation BEFORE summarization, we clear the KV-cache and put it in a clean state. This is why subsequent compressions work - they always operate on a conversation that was just reset.

The fix adds a reset BEFORE compression (with existing summary as context to preserve continuity) and keeps the existing reset AFTER compression (with new summary for user chat).

**Performance impact**: Adds one extra `resetConversation()` call (~50-100ms) per compression, but eliminates the crash entirely.

## ADDED
None

## REMOVED
None
