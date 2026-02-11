# Web Search Integration - Test Plan

## Overview
This document outlines the required test coverage for the web search integration feature to ensure models use current web data instead of training data.

---

## Unit Tests

### PersonaLibrary Tests
**File:** `app/src/test/java/ai/ondevice/app/persona/PersonaLibraryTest.kt`

```kotlin
class PersonaLibraryTest {

    @Test
    fun `getPersona with webSearchEnabled=false contains offline text`() {
        val persona = PersonaLibrary.getPersona(PersonaVariant.BALANCED, webSearchEnabled = false)

        assertTrue(persona.contains("Offline"))
        assertFalse(persona.contains("Web-assisted"))
    }

    @Test
    fun `getPersona with webSearchEnabled=true contains web-assisted text`() {
        val persona = PersonaLibrary.getPersona(PersonaVariant.BALANCED, webSearchEnabled = true)

        assertFalse(persona.contains("Offline"))
        assertTrue(persona.contains("Web-assisted"))
        assertTrue(persona.contains("live search"))
    }

    @Test
    fun `all persona variants support web-enabled mode`() {
        PersonaVariant.values().forEach { variant ->
            val webPersona = PersonaLibrary.getPersona(variant, webSearchEnabled = true)

            assertFalse(webPersona.contains("Offline"), "Variant $variant still contains 'Offline'")
            assertTrue(webPersona.contains("Web-assisted") || webPersona.contains("live search"),
                      "Variant $variant missing web-assisted text")
        }
    }
}
```

---

### PersonaManager Tests
**File:** `app/src/test/java/ai/ondevice/app/persona/PersonaManagerTest.kt`

```kotlin
class PersonaManagerTest {

    private lateinit var personaManager: PersonaManager

    @Before
    fun setup() {
        personaManager = PersonaManager()
    }

    @Test
    fun `formatSingleMessageWithPersona includes web-aware persona when enabled`() {
        val result = personaManager.formatSingleMessageWithPersona(
            message = "What's the weather?",
            variant = PersonaVariant.BALANCED,
            webSearchEnabled = true
        )

        assertTrue(result.contains("Web-assisted"))
        assertTrue(result.contains("User: What's the weather?"))
    }

    @Test
    fun `formatSingleMessageWithPersona includes offline persona when disabled`() {
        val result = personaManager.formatSingleMessageWithPersona(
            message = "What's the weather?",
            variant = PersonaVariant.BALANCED,
            webSearchEnabled = false
        )

        assertTrue(result.contains("Offline"))
        assertTrue(result.contains("User: What's the weather?"))
    }
}
```

---

### SearchRepository Tests
**File:** `app/src/test/java/ai/ondevice/app/data/SearchRepositoryTest.kt`

```kotlin
class SearchRepositoryTest {

    @Test
    fun `formatResults includes current date`() {
        val repository = SearchRepository(mockBraveService, mockPreferences)
        val results = listOf(
            SearchResult(title = "Weather Today", description = "Sunny, 72°F", url = "weather.com")
        )

        val formatted = repository.formatResults(results)  // Using reflection to test private method

        val currentDate = java.time.LocalDate.now().toString()
        assertTrue(formatted.contains(currentDate))
    }

    @Test
    fun `formatResults includes all search result fields`() {
        val repository = SearchRepository(mockBraveService, mockPreferences)
        val results = listOf(
            SearchResult(title = "Test Title", description = "Test description", url = "test.com")
        )

        val formatted = repository.formatResults(results)

        assertTrue(formatted.contains("Test Title"))
        assertTrue(formatted.contains("Test description"))
        assertTrue(formatted.contains("test.com"))
    }
}
```

---

## Integration Tests

### LlmChatViewModel Integration Tests
**File:** `app/src/androidTest/java/ai/ondevice/app/ui/llmchat/LlmChatViewModelIntegrationTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class LlmChatViewModelIntegrationTest {

    private lateinit var viewModel: LlmChatViewModel
    private lateinit var mockSearchRepository: SearchRepository
    private lateinit var mockPersonaManager: PersonaManager

    @Before
    fun setup() {
        mockSearchRepository = mock()
        mockPersonaManager = PersonaManager()

        viewModel = LlmChatViewModel(
            context = ApplicationProvider.getApplicationContext(),
            conversationDao = mockDao,
            personaManager = mockPersonaManager,
            tokenMonitor = mockTokenMonitor,
            searchRepository = mockSearchRepository,
            webSearchPreferences = mockWebPreferences
        )
    }

    @Test
    fun `web search results are appended after persona injection`() {
        // Given: Web search returns current data
        val currentDate = java.time.LocalDate.now().toString()
        whenever(mockSearchRepository.search(any())).thenReturn(
            Result.success("""Current date: $currentDate
[1] Weather Today
    Sunny, 72°F in San Francisco
    Source: weather.com""")
        )
        whenever(mockWebPreferences.getPreferences()).thenReturn(
            WebSearchPreferences(enabled = true, dailyCount = 0)
        )

        // When: User asks about weather on first message
        viewModel.generateResponse(mockModel, "what's the weather today", onError = {})

        // Then: Enhanced input contains persona + web results + strong instructions
        val captor = argumentCaptor<String>()
        verify(mockInference).runInference(eq(mockModel), captor.capture(), any())

        val enhancedInput = captor.firstValue

        // Verify order: Persona FIRST, then web results
        val personaIndex = enhancedInput.indexOf("Web-assisted")
        val webResultsIndex = enhancedInput.indexOf("<<CRITICAL - WEB SEARCH RESULTS>>")
        val dateIndex = enhancedInput.indexOf(currentDate)
        val weatherIndex = enhancedInput.indexOf("72°F")
        val instructionIndex = enhancedInput.indexOf("YOU MUST answer using ONLY the web search results")

        assertTrue("Persona should appear first", personaIndex < webResultsIndex)
        assertTrue("Date should be in web results", dateIndex > webResultsIndex)
        assertTrue("Weather data should be in web results", weatherIndex > webResultsIndex)
        assertTrue("Strong instructions should follow web results", instructionIndex > dateIndex)
    }

    @Test
    fun `offline persona used when web search disabled`() {
        // Given: Web search disabled
        whenever(mockWebPreferences.getPreferences()).thenReturn(
            WebSearchPreferences(enabled = false, dailyCount = 0)
        )

        // When: User asks question on first message
        viewModel.generateResponse(mockModel, "what's the weather today", onError = {})

        // Then: Persona contains "Offline"
        val captor = argumentCaptor<String>()
        verify(mockInference).runInference(eq(mockModel), captor.capture(), any())

        val enhancedInput = captor.firstValue
        assertTrue(enhancedInput.contains("Offline"))
        assertFalse(enhancedInput.contains("Web-assisted"))
        assertFalse(enhancedInput.contains("<<CRITICAL - WEB SEARCH RESULTS>>"))
    }

    @Test
    fun `subsequent messages do not include persona or web search`() {
        // Given: First message already sent
        val existingMessages = listOf(
            ChatMessageText(content = "First message", side = ChatSide.USER, latencyMs = -1f)
        )
        // Mock uiState to have existing messages

        // When: User sends second message
        viewModel.generateResponse(mockModel, "follow-up question", onError = {})

        // Then: Enhanced input is just the user's message (no persona, no web wrapper)
        val captor = argumentCaptor<String>()
        verify(mockInference).runInference(eq(mockModel), captor.capture(), any())

        val enhancedInput = captor.firstValue
        assertEquals("follow-up question", enhancedInput)
    }
}
```

---

## Manual Testing Checklist

### Prerequisites
- [ ] Device/emulator with internet connection
- [ ] Brave Search API key configured (`BuildConfig.BRAVE_API_KEY`)
- [ ] Web search enabled in app settings
- [ ] Daily search limit not exceeded (< 5 searches)

### Test Cases

#### TC1: Web Search Returns Current Data (P0)
**Steps:**
1. Enable web search (lock icon shows 🔓)
2. Start new conversation
3. Ask: "What's the weather in San Francisco today?"
4. Observe response

**Expected:**
- ✅ Response includes current date (December 31, 2025)
- ✅ Response includes current weather data from web search
- ✅ Response does NOT mention 2023 or training cutoff date
- ✅ Lock icon remains 🔓 (web-assisted mode)

**Actual:** _______________

---

#### TC2: Offline Mode Uses Training Data (P1)
**Steps:**
1. Disable web search (lock icon shows 🔒)
2. Start new conversation
3. Ask: "What's the weather in San Francisco today?"
4. Observe response

**Expected:**
- ✅ Response uses training data (may be outdated)
- ✅ Response acknowledges "I don't have current information"
- ✅ Lock icon remains 🔒 (offline mode)

**Actual:** _______________

---

#### TC3: Cross-Model Compatibility (P0)
**Test with:**
- [ ] Gemini 2B (2.7B parameters)
- [ ] Gemini 9B (9.0B parameters)
- [ ] Future models (if available)

**Steps:**
1. Enable web search
2. Ask same question on each model: "Who won the latest Super Bowl?"
3. Compare responses

**Expected:**
- ✅ All models use web search results
- ✅ Responses include current year data
- ✅ No model reverts to training data

**Actual:** _______________

---

#### TC4: Web Search Error Handling (P1)
**Steps:**
1. Enable web search
2. Exceed daily rate limit (send 6+ searches)
3. Ask: "What's the weather today?"
4. Observe response and logs

**Expected:**
- ✅ Log shows: "Web search rate limit reached"
- ✅ Model continues with offline mode (no crash)
- ✅ Response uses training data (graceful degradation)

**Actual:** _______________

---

#### TC5: Model Bubble Centering (P2 - Visual)
**Steps:**
1. Open AI Chat screen
2. Observe top bar layout
3. Note model bubble position relative to hamburger menu and lock icon

**Expected:**
- ✅ Model bubble visually centered (not too far right)
- ✅ Offset is 7dp from mathematical center
- ✅ Looks balanced with hamburger (48dp) and lock (18dp) on left

**Actual:** _______________

---

## Performance Testing

### Token Usage Validation
**Goal:** Ensure prompt doesn't exceed context window

**Test:**
1. Enable web search
2. Send first message with 3 web results
3. Measure token count:
   - Persona: ~230 tokens (BALANCED)
   - Web results: ~300-500 tokens (3 results)
   - Strong instructions: ~50 tokens
   - User query: ~20 tokens
   - **Total: ~600-800 tokens**

**Expected:** < 1000 tokens (well under 4K limit)

---

## Acceptance Criteria

### Must Pass Before Release
- [ ] All unit tests pass (100% coverage for PersonaLibrary, PersonaManager)
- [ ] Integration test TC1 passes (web search returns current data)
- [ ] Integration test TC3 passes (cross-model compatibility)
- [ ] Manual test TC1 passes on physical device
- [ ] Manual test TC3 passes for all available models
- [ ] No crashes during web search errors (TC4)

### Nice to Have
- [ ] Integration test coverage > 80%
- [ ] Manual test TC5 passes (visual QA approval)
- [ ] Performance test shows < 800 tokens for typical query

---

## Test Execution Log

| Test ID | Date | Tester | Result | Notes |
|---------|------|--------|--------|-------|
| TC1 | ___ | ___ | ⬜ PASS / FAIL | |
| TC2 | ___ | ___ | ⬜ PASS / FAIL | |
| TC3 | ___ | ___ | ⬜ PASS / FAIL | |
| TC4 | ___ | ___ | ⬜ PASS / FAIL | |
| TC5 | ___ | ___ | ⬜ PASS / FAIL | |

---

**Test Plan Version:** 1.0
**Last Updated:** 2025-12-31
**Status:** ⏳ Ready for execution after code implementation
