# Proposal: Infinite Conversation via Self-Summarization

## Summary
Enable conversations to exceed Gemma 2B's 8K token context limit by automatically summarizing older messages using the LLM itself, following the battle-tested LangChain ConversationSummaryBufferMemory pattern.

## Motivation

**Validated User Pain Point**: Users hit context limits and must start new chats, wasting tokens to re-explain their entire context. This is the #1 user complaint.

**Current Behavior**:
1. User chats until context fills (~8,192 tokens for Gemma 2B)
2. Model becomes slower or starts erroring
3. User must manually start NEW chat
4. User must re-explain EVERYTHING from scratch
5. Wastes tokens and creates frustration

**Desired Behavior**:
1. System automatically detects context pressure (75% = 3,072 tokens)
2. System summarizes oldest messages using existing LLM
3. Session is reset with summary injected as context
4. User continues seamlessly without re-explaining

## Scope

### ✅ What's Included
- Token estimation using simple heuristic (chars/4)
- Automatic compaction trigger at 75% context (3,072 tokens)
- Self-summarization using existing Gemma LLM (no new models)
- ConversationState entity in Room database to store summaries
- Session reset with summary injection via PersonaManager
- Target: 40% context after compaction (1,638 tokens)
- UI feedback: "Conversation optimized" toast (optional)

### ❌ What's NOT Included (Phase 2+)
- LLMLingua-2 or other compression models
- Protocol Buffers or new serialization
- Entity extraction or NER models
- Embedding-based topic detection
- BPE tokenizer library (use heuristic for MVP)
- User-configurable compression thresholds
- Archive search/recall features
- Commitment tracking
- Multi-summary merging strategies

## Acceptance Criteria
- [ ] Conversation can exceed 50 turns without crashing
- [ ] Context remains coherent after compaction (summary captures key facts)
- [ ] No new dependencies added to build.gradle
- [ ] Total new code ≤ 250 lines
- [ ] Token counter tracks conversation size accurately (±20% tolerance)
- [ ] Compaction triggers automatically at 75% context
- [ ] Summary stored in database and persists across app restarts
- [ ] Session recreation works seamlessly (no user re-explanation needed)
- [ ] Existing tests continue to pass
- [ ] Manual test: 30+ turn conversation triggers compaction successfully

## Technical Approach

### Pattern: LangChain ConversationSummaryBufferMemory

**Source**: [LangChain Conversational Memory](https://www.pinecone.io/learn/series/langchain/langchain-conversational-memory/)

**How it works**:
1. Keep recent messages verbatim (high fidelity)
2. Summarize oldest messages when token limit approached
3. Use LLM itself for summarization (no additional model)
4. Store running summary + keep recent context
5. Inject summary into next inference

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  User sends message                                         │
│         ↓                                                   │
│  TokenEstimator.estimate(messages) → 6,500 tokens          │
│         ↓                                                   │
│  CompactionTrigger.shouldCompact() → YES (>75%)            │
│         ↓                                                   │
│  CompactionManager.executeCompaction():                     │
│    1. Identify oldest messages to evict (until 40%)        │
│    2. ConversationSummarizer.summarize(evicted)            │
│    3. Merge with existing summary                          │
│    4. Save ConversationState to Room                       │
│    5. Mark evicted messages as archived                    │
│         ↓                                                   │
│  ContextBuilder.buildContext():                             │
│    - Inject summary: <previous_context>...</>              │
│    - Add recent messages verbatim                          │
│         ↓                                                   │
│  LlmChatModelHelper.runInference(context)                  │
│         ↓                                                   │
│  Response generated with full context awareness            │
└─────────────────────────────────────────────────────────────┘
```

### Components (~200 lines total)

1. **TokenEstimator** (10 lines)
   - Simple heuristic: `text.length / 4`
   - Good enough for MVP (±15% error absorbed by buffer)
   - No dependencies

2. **CompactionTrigger** (15 lines)
   - Threshold: 75% of 8,192 = 6,144 tokens
   - Target: 40% of 8,192 = 3,276 tokens
   - Returns messages to evict

3. **ConversationSummarizer** (20 lines)
   - Uses existing LLM (no new model!)
   - Prompt: "Summarize this conversation in 150 words..."
   - Returns summary string

4. **ConversationState Entity + DAO** (30 lines)
   - Room entity with threadId, runningSummary, turnsSummarized, lastCompactionTime
   - DAO with getState() and saveState()
   - Migration to add table

5. **CompactionManager** (60 lines)
   - Orchestrates: trigger check → summarize → save state → archive messages
   - Returns CompactionResult (Success/NotNeeded)

6. **ContextBuilder** (30 lines)
   - Injects summary: `<previous_context>summary</previous_context>`
   - Adds recent messages
   - Returns formatted context string

7. **Integration in ChatViewModel** (20 lines)
   - Call compaction before generating response
   - Update token count after each message
   - Show toast if compaction occurred

8. **Database Migration** (15 lines)
   - Add conversation_state table
   - Add archived flag to conversation_messages

### Use ONLY Existing Resources

| Resource | Location | Purpose |
|----------|----------|---------|
| LiteRT LLM | LlmChatModelHelper | Summarization (self-summarize!) |
| Room Database | AppDatabase | Store ConversationState |
| ConversationMessage | data/ConversationMessage.kt | Archive evicted messages |
| estimatedTokens field | ConversationThread | EXISTS (wire it up!) |
| resetConversation() | LlmChatModelHelper | Session recreation |
| PersonaManager | persona/PersonaManager.kt | Context injection |

**Zero new dependencies.**

### Validation Results

✅ **Q1: Can Gemma summarize itself?**
- Answer: YES
- Confidence: 95%
- Source: LangChain ConversationSummaryBufferMemory pattern (proven at 100k+ users)

✅ **Q2: What's the context limit?**
- Answer: 8,192 tokens (Gemma 2B spec)
- Confidence: 100%
- Source: [Hugging Face Gemma 2B Model Card](https://huggingface.co/google/gemma-2b)

✅ **Q3: How to count tokens?**
- Answer: chars/4 heuristic
- Confidence: 85%
- Good enough for MVP, refine in Phase 2

✅ **Q4: Does resetConversation() clear cache?**
- Answer: YES (verified in code)
- Confidence: 100%
- Location: LlmChatModelHelper.kt:132-177

## Implementation Phases

### Phase 1: MVP (This Proposal) - 2-3 days
- Token estimation
- Compaction trigger
- Self-summarization
- State persistence
- Session reset with summary
- Manual testing

### Phase 2: Enhancement (Future) - 1-2 days
- BPE tokenizer for accuracy
- User-configurable thresholds
- Better prompt engineering
- Analytics (compaction frequency, summary quality)

### Phase 3: Advanced (Future) - 2-3 days
- Entity extraction (if summaries too lossy)
- Topic detection (if timing feels wrong)
- Archive search/recall
- Commitment tracking

## References
- Pattern: [LangChain ConversationSummaryBufferMemory](https://www.pinecone.io/learn/series/langchain/langchain-conversational-memory/)
- Model: [Gemma 2B Specification](https://huggingface.co/google/gemma-2b)
- Existing: ConversationDao.kt, PersonaManager.kt, LlmChatModelHelper.kt
- Research: /tmp/claude-1000/.../party_mode_findings.md
- Validation: /tmp/claude-1000/.../validation_complete.md

## Anti-Patterns to Avoid

Based on CLAUDE.md: "🔥 CRITICAL: Copy Battle-Tested Implementations"

- ❌ "Let me add LLMLingua-2 for better compression" → NO, use existing LLM
- ❌ "Let me add embeddings for topic detection" → NO, token count is enough
- ❌ "Let me add Protocol Buffers" → NO, Room is fine
- ❌ "Let me create an abstraction layer" → NO, direct implementation
- ❌ "Let me build my own summarization" → NO, clone LangChain pattern

## Success Criteria

**Functional**:
- 50+ turn conversation works without crash
- Summary captures key context (manual review)
- Session continues seamlessly after compaction

**Technical**:
- Zero new dependencies
- ≤250 lines of code
- All existing tests pass
- CI remains green

**User Experience**:
- No manual intervention required
- Context remains coherent
- No token waste from re-explaining

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Gemma poor at summarization | Test early, refine prompt, fallback to MiniLM in Phase 2 |
| Token estimate inaccurate | 75% trigger has buffer, 15% error absorbed |
| Summary loses critical context | Keep last 5-10 messages verbatim (hybrid approach) |
| Performance impact | Summarization happens on background thread |
| Database migration fails | Comprehensive testing, rollback plan |

## Next Steps

1. **Review this proposal** - Does scope/approach make sense?
2. **Approve or iterate** - Say "approved" or request changes
3. **Then /openspec-apply** - Implement according to approved specs
4. **NO CODE UNTIL APPROVED** - Spec-driven development
