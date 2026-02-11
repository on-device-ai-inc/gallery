# Web Search Testing Guide - Fresh Perplexica Implementation

## 📦 Build Artifacts

**Branch**: `feature/web-search-fresh-implementation`
**PR**: #4 (https://github.com/on-device-ai-inc/on-device-ai/pull/4)
**CI Run**: 21307796856 ✅ PASSED
**APK**: `app-debug.apk` (227MB) - Downloaded and ready

## 🎯 What Was Implemented

### New Components
1. **SearchPromptTemplate.kt** - Perplexica XML-style prompts with CRITICAL instructions
2. **CircuitBreaker.kt** - Resilience pattern (5-failure threshold, 60s timeout)
3. **DuckDuckGoClient.kt** - Fallback provider (stubbed for MVP)
4. **CitationFormatter.kt** - Infrastructure for "Sources:" section
5. **Updated SearchRepository.kt** - Multi-provider fallback chain
6. **Updated LlmChatViewModel.kt** - Integration with SearchResponse

### Test Coverage
- ✅ 25 unit tests (all passing)
- ✅ SearchPromptTemplateTest.kt (6 tests)
- ✅ CircuitBreakerTest.kt (10 tests)
- ✅ CitationFormatterTest.kt (9 tests)

## 🚀 Installation Steps

### 1. Connect Device
```bash
# Physical device (Samsung S22 Ultra)
adb devices
# Should show: R3CT10HETMM

# OR start Waydroid emulator
waydroid session start
waydroid show-full-ui
```

### 2. Install APK
```bash
# Install the APK
adb install -r app-debug.apk

# Launch app
adb shell am start -n ai.ondevice.app/.MainActivity
```

## 🧪 Manual Testing Scenarios

### Test 1: Basic Web Search (Current Events)
**Query**: "What's the weather in New York today?"

**Expected Behavior**:
- ✅ Web search executes (Brave Search API)
- ✅ LLM response uses current weather data from search results
- ✅ Response includes citations like [1], [2]
- ✅ Current date is mentioned in response
- ⚠️ "Sources:" section NOT yet appended (MVP - infrastructure only)

**Validation**:
1. Check logcat for `[WEB SEARCH]` tags:
   ```bash
   adb logcat | grep "WEB SEARCH"
   ```
2. Response should reference specific weather conditions (not generic)
3. Response should mention today's date (2026-01-23)

### Test 2: Fallback to DuckDuckGo
**Pre-requisite**: Trigger Brave circuit breaker (simulate 5 failures)

**Query**: "Latest news about AI"

**Expected Behavior**:
- ⚠️ Brave circuit breaker opens (if threshold reached)
- ✅ Falls back to DuckDuckGo
- ⚠️ DuckDuckGo stubbed for MVP - returns empty results
- ✅ Logs show fallback attempt

**Validation**:
```bash
# Check circuit breaker logs
adb logcat | grep "Circuit breaker"
```

### Test 3: Rate Limiting (5 searches/day)
**Pre-requisite**: Perform 5 web searches

**Query**: "6th search query"

**Expected Behavior**:
- ❌ Rate limit reached
- ✅ Log shows: `[WEB SEARCH] Rate limit reached, cannot perform search`
- ✅ LLM responds without web search data (uses training data)

**Validation**:
```bash
adb logcat | grep "Rate limit"
```

### Test 4: Web Search Disabled
**Steps**:
1. Open app settings
2. Disable web search toggle
3. Send query: "Current stock price of AAPL"

**Expected Behavior**:
- ✅ No web search API call
- ✅ LLM responds using training data (outdated info)
- ✅ No `[WEB SEARCH]` logs

### Test 5: LLM Compliance (Perplexica Pattern)
**Query**: "Who won the last World Cup?"

**Expected Behavior**:
- ✅ Web search fetches current results
- ✅ LLM uses search results (not training data from 2022)
- ✅ Response cites sources with [1], [2] format
- ✅ Response prioritizes web data over training data

**Validation**:
- Response should reference the most recent World Cup (2026 or later)
- Response should include specific details from search results
- Compliance rate: >80% (per Perplexica benchmarks)

## 📊 Success Criteria

### Core Functionality
- [x] CI/CD build passes ✅
- [x] Unit tests pass (25/25) ✅
- [ ] APK installs without errors
- [ ] App launches successfully
- [ ] Web search executes on query
- [ ] LLM uses search results in response
- [ ] Rate limiting works (5/day)
- [ ] Circuit breaker prevents hammering

### LLM Compliance
- [ ] LLM cites search results with [1], [2]
- [ ] LLM prioritizes search data over training data
- [ ] Current date mentioned when relevant
- [ ] Specific facts from search results included
- [ ] Compliance rate >70% (good), >80% (excellent)

### Error Handling
- [ ] Graceful degradation when API fails
- [ ] Fallback to DuckDuckGo (stubbed for MVP)
- [ ] Rate limit enforced properly
- [ ] Circuit breaker opens after 5 failures

## 📸 Visual Verification Checklist

### Screenshot Locations
DroidRun saves to: `trajectories/[timestamp]/`

### Required Screenshots
1. **Web search enabled in settings** - Toggle ON
2. **Successful web search query** - Response with citations
3. **Rate limit reached** - Error message or fallback behavior
4. **Web search disabled** - Settings toggle OFF

### DroidRun Commands
```bash
# Basic verification
droid "Open ai.ondevice.app, send 'weather in New York', verify response mentions current conditions, then stop"

# Settings verification
droid "Open ai.ondevice.app, go to settings, verify web search toggle exists, then stop"

# Rate limit verification (after 5 searches)
droid "Open ai.ondevice.app, send 'test query', verify rate limit message, then stop"
```

## 🔍 Debugging

### Check Logs
```bash
# Web search specific logs
adb logcat | grep "WEB SEARCH"

# Circuit breaker logs
adb logcat | grep "CircuitBreaker"

# Citation formatter logs
adb logcat | grep "CitationFormatter"

# Full app logs
adb logcat | grep "ai.ondevice.app"
```

### Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| No web search results | API key missing | Check BuildConfig.BRAVE_API_KEY |
| Rate limit hit immediately | Counter not reset | Clear app data, reinstall |
| LLM ignores search results | Prompt formatting issue | Check SearchPromptTemplate.format() |
| Circuit breaker stuck open | Too many failures | Wait 60s or clear app data |

## 📝 Test Report Template

```markdown
## Web Search Test Report - [Date]

### Environment
- Device: [R3CT10HETMM / Waydroid / Other]
- APK: app-debug.apk (227MB)
- CI Run: 21307796856

### Test Results

#### Test 1: Basic Web Search
- Status: [ ] PASS / [ ] FAIL
- Query: "What's the weather in New York today?"
- Response included current weather: [ ] YES / [ ] NO
- Response included citations [1], [2]: [ ] YES / [ ] NO
- Notes:

#### Test 2: Fallback Behavior
- Status: [ ] PASS / [ ] FAIL
- Circuit breaker opened: [ ] YES / [ ] NO
- DuckDuckGo fallback attempted: [ ] YES / [ ] NO
- Notes:

#### Test 3: Rate Limiting
- Status: [ ] PASS / [ ] FAIL
- Rate limit enforced after 5 searches: [ ] YES / [ ] NO
- Error message shown: [ ] YES / [ ] NO
- Notes:

#### Test 4: LLM Compliance
- Status: [ ] PASS / [ ] FAIL
- LLM used search results: [ ] YES / [ ] NO
- Compliance rate estimate: ____%
- Notes:

### Screenshots
- [ ] Web search enabled
- [ ] Successful query with citations
- [ ] Rate limit reached
- [ ] Settings toggle

### Overall Assessment
- [ ] Ready for merge
- [ ] Needs fixes
- Issues found:
```

## 🎯 Next Steps After Testing

1. ✅ If all tests pass → Merge PR #4
2. ❌ If issues found → Document in GitHub issue, fix and retry
3. 📋 Update LESSONS_LEARNED.md with real-world findings
4. 🗂️ Run `/openspec-archive web-search-fresh-perplexica-pattern`
5. 🚀 Plan citation integration (CitationFormatter.appendSources)

## 📚 References

- **Perplexica**: https://github.com/ItzCrazyKns/Perplexica (27.7k⭐)
- **Research Files**: GitHub repo `/web-search-integration-research.md`, `/open-source-projects-web-search.md`
- **OpenSpec Proposal**: `openspec/changes/web-search-fresh-perplexica-pattern/proposal.md`
- **PR**: https://github.com/on-device-ai-inc/on-device-ai/pull/4
