# Prompt Engineering System Specification

## Purpose
This specification covers the persona injection and token monitoring systems that optimize on-device LLM performance within Gemma's 4K context window constraints while maintaining conversation quality.

## Requirements

### Requirement: Persona Variant Selection
The system SHALL provide five persona variants with different token costs to balance conversation quality with context window usage.

#### Scenario: User needs maximum guidance
- WHEN conversation requires comprehensive instructions and detailed examples
- THEN MAXIMUM persona variant SHALL be available
- AND it SHALL consume 620 tokens (31% of 2K usable context)

#### Scenario: User needs balanced approach
- WHEN conversation requires good quality without excessive context consumption
- THEN BALANCED persona variant SHALL be used as default
- AND it SHALL consume 230 tokens (11.5% of 2K usable context)

#### Scenario: User needs minimal overhead
- WHEN conversation requires maximum context window for actual conversation
- THEN MINIMAL persona variant SHALL be available
- AND it SHALL consume 110 tokens (5.5% of 2K usable context)

### Requirement: First Message Persona Injection
The system SHALL inject the selected persona into the first user message to guide model behavior, working around Gemma's lack of system role support.

#### Scenario: New conversation starts
- WHEN user sends the first message in a new conversation
- THEN the BALANCED persona SHALL be prepended to the user's message
- AND the enhanced message SHALL be logged with "Persona injected into first message (BALANCED variant, ~230 tokens)"
- AND subsequent messages SHALL NOT include persona to avoid repetition

#### Scenario: Continuing existing conversation
- WHEN user sends a message in an ongoing conversation
- THEN NO persona SHALL be injected (persona only applies to first message)
- AND the user's message SHALL be sent as-is to the model

#### Scenario: Persona injection fails
- WHEN persona injection encounters an exception
- THEN the system SHALL log a warning: "Failed to inject persona, using input as-is"
- AND the user's original message SHALL be sent without persona
- AND the conversation SHALL continue normally

### Requirement: Context Window Limits
The system SHALL enforce Gemma 2B's context window limits to prevent truncation and maintain conversation coherence.

#### Scenario: Total context calculation
- WHEN calculating context usage
- THEN the system SHALL use MAX_CONTEXT_TOKENS = 4096 as the hard limit
- AND it SHALL reserve RESPONSE_BUFFER_TOKENS = 512 for model responses
- AND it SHALL calculate MAX_USABLE_TOKENS = 3584 (4K - 10% safety buffer)

#### Scenario: Approaching context limit
- WHEN conversation reaches 84% of usable context (3,010 tokens)
- THEN the system SHALL flag WarningLevel.APPROACHING
- AND it SHALL log a warning about context usage

#### Scenario: Critical context usage
- WHEN conversation reaches 95% of usable context (3,405 tokens)
- THEN the system SHALL flag WarningLevel.CRITICAL
- AND it SHALL log "Context usage critical! Consider implementing compression."

### Requirement: Token Estimation
The system SHALL estimate token counts for different message types to track context usage without requiring LiteRT-LM sizeInTokens() API.

#### Scenario: Text message estimation
- WHEN estimating tokens for text content
- THEN the system SHALL use CHARS_PER_TOKEN = 4.0 as approximation for Gemma
- AND it SHALL calculate: tokens = text.length / 4

#### Scenario: Image message estimation
- WHEN estimating tokens for images
- THEN the system SHALL use TOKENS_PER_IMAGE = 257 per image
- AND it SHALL multiply by the number of images in the message

#### Scenario: Audio message estimation
- WHEN estimating tokens for audio clips
- THEN the system SHALL use AUDIO_MS_PER_TOKEN = 150 (150ms audio = 1 token)
- AND it SHALL calculate: tokens = duration_ms / 150

### Requirement: Database Schema for Persona Tracking
The system SHALL persist persona variant and token counts in the ConversationThread table for conversation continuity.

#### Scenario: New conversation created
- WHEN a new conversation thread is created
- THEN personaVariant field SHALL be initialized to "BALANCED"
- AND estimatedTokens field SHALL be initialized to 0
- AND lastTokenUpdate field SHALL be set to current timestamp

#### Scenario: Persona variant changed
- WHEN user selects a different persona variant
- THEN the system SHALL update the personaVariant field in the database
- AND it SHALL trigger re-calculation of context usage with new persona overhead

### Requirement: Thread-Safe Context Calculations
The system SHALL perform all token counting and context calculations in a thread-safe manner to avoid race conditions.

#### Scenario: Multiple rapid messages
- WHEN user sends multiple messages in quick succession
- THEN token calculations SHALL be performed on Dispatchers.IO
- AND database updates SHALL be atomic
- AND calculations SHALL not block UI thread

#### Scenario: Concurrent access to thread data
- WHEN multiple coroutines access conversation thread data
- THEN all calculations SHALL use immutable data snapshots
- AND no shared mutable state SHALL be accessed without synchronization

## Persona Content Requirements

### Requirement: Research-Backed Persona Text
The system SHALL use persona text patterns extracted from analysis of 26 leading AI tools' system prompts.

#### Scenario: Persona library provides text
- WHEN persona variant is requested
- THEN PersonaLibrary SHALL return pre-defined persona text
- AND the text SHALL follow patterns: clarity, conciseness, examples, structure, validation
- AND the text SHALL be optimized for Gemma model behavior

### Requirement: Persona Variant Characteristics
The system SHALL define each persona variant with specific token costs and use cases.

#### Scenario: MAXIMUM variant (620 tokens)
- WHEN comprehensive guidance needed for complex tasks
- THEN persona text SHALL include extensive examples and detailed instructions
- AND it SHALL consume 31% of 2K context window

#### Scenario: COMPREHENSIVE variant (340 tokens)
- WHEN strong guidance needed with moderate efficiency
- THEN persona text SHALL balance detail with context conservation
- AND it SHALL consume 17% of 2K context window

#### Scenario: BALANCED variant (230 tokens) - DEFAULT
- WHEN optimal balance of quality and efficiency needed
- THEN persona text SHALL provide essential guidance concisely
- AND it SHALL consume 11.5% of 2K context window

#### Scenario: STREAMLINED variant (170 tokens)
- WHEN efficient core behaviors sufficient
- THEN persona text SHALL include only essential instructions
- AND it SHALL consume 8.5% of 2K context window

#### Scenario: MINIMAL variant (110 tokens)
- WHEN maximum efficiency required
- THEN persona text SHALL provide minimal guidance
- AND it SHALL consume 5.5% of 2K context window

## Architecture Requirements

### Requirement: Dependency Injection
The system SHALL provide PersonaManager and TokenMonitor as Hilt singletons for consistent behavior across ViewModels.

#### Scenario: LlmChatViewModel creation
- WHEN LlmChatViewModel is instantiated by Hilt
- THEN PersonaManager SHALL be injected as singleton
- AND TokenMonitor SHALL be injected as singleton
- AND both SHALL be optional parameters (nullable) for testing

### Requirement: ViewModel Integration Pattern
The system SHALL integrate persona and token features into ChatViewModel hierarchy without breaking existing behavior.

#### Scenario: LlmChatViewModelBase construction
- WHEN base ViewModel is created
- THEN it SHALL accept optional PersonaManager parameter
- AND it SHALL accept optional TokenMonitor parameter
- AND it SHALL gracefully handle null values (no-op when null)

### Requirement: Non-Blocking Token Updates
The system SHALL update token counts asynchronously after inference completion without blocking response display.

#### Scenario: Inference completes successfully
- WHEN model finishes generating response
- THEN AI message SHALL be updated in database first
- AND token count update SHALL happen asynchronously on Dispatchers.IO
- AND any token update failures SHALL be logged but not surfaced to user

## Current Implementation Status

### ✅ Fully Implemented (v1.0)
- Persona variant enum with 5 variants (PersonaVariant.kt)
- Persona library with research-backed text (PersonaLibrary.kt)
- Persona manager with formatting methods (PersonaManager.kt)
- Token monitor with estimation methods (TokenMonitor.kt)
- Database schema updates (ConversationThread: +3 fields)
- Database migration (MIGRATION_3_4)
- Hilt providers for PersonaManager and TokenMonitor
- First message persona injection in LlmChatViewModel

### ⚠️ Partially Implemented
- Token count updates: Architecture requires protected access to ChatViewModel internals
  - Currently disabled in LlmChatViewModel (lines 209-210)
  - Requires making `currentThreadId` and `conversationDao` protected in ChatViewModel
  - Tracked in: openspec/changes/enable-token-monitoring/ (future work)

### ❌ Not Yet Implemented (Future Phases)
- Phase 2: Compression strategies when approaching context limits
- Phase 3: UI for persona selection and context usage display
- Phase 4: Testing across all 5 persona variants with metrics

## Related Specifications
- See `.bmad/docs/stories/story-prompt-engineering.md` for original requirements
- See `openspec/specs/conversation-history/spec.md` for database integration

## Version History
- **v1.0** (2025-12-31): Phase 1 implementation
  - Commit: c3a340b "Prompt Engineering - Phase 1: Persona System & Token Monitoring"
  - Commit: d74d1a5 "Fix: Compilation errors" (disabled token monitoring temporarily)
  - Database: Version 3 → 4 with persona/token fields
  - Status: Persona injection working, token monitoring architecture needs refactoring
