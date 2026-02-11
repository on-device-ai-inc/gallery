# Proposal: profile-settings-cursor-fix

## Summary
Fix cursor jumping bug in profile settings text input fields (Full Name and Nickname) where cursor moves to beginning after each character input.

## Motivation

**User Report**: "Profile settings text input fields for Full Name and Nickname are broken, when typing name, cursor moves back making it hard to type your name e.g you type N, instead of cursor remaining in front and waiting for next input it move back, so when user inputs a next, expectation is Na but reality is aN"

**Current State**:
- ✅ Profile settings screen exists with Full Name and Nickname fields
- ❌ Cursor position resets to beginning after each character
- ❌ Expected input "Nathan" becomes "nahtaN" or worse
- ❌ Makes profile editing extremely frustrating

**Impact**:
- **User Experience**: P0 - Breaks basic profile editing functionality
- **User Trust**: Users may think app is fundamentally broken
- **First-Run Experience**: Users set profile during onboarding (first impression issue)
- **Severity**: Critical - Core functionality broken

**Business Priority**: P0 (Critical) - Blocks basic user profile setup

## Scope

### IN SCOPE
- ✅ Fix cursor position logic in Full Name text field
- ✅ Fix cursor position logic in Nickname text field
- ✅ Verify fix doesn't break other text input fields in app
- ✅ Add visual test to prevent regression

### OUT OF SCOPE
- ❌ Adding new profile fields
- ❌ Changing profile settings UI layout
- ❌ Profile picture upload feature
- ❌ Profile validation (that's a separate feature)

## Acceptance Criteria

### Functional Requirements
- [ ] **AC1**: User can type "Nathan" in Full Name field and it displays "Nathan" (not "nahtaN")
- [ ] **AC2**: User can type "Nick" in Nickname field and it displays "Nick" (not "kciN")
- [ ] **AC3**: Cursor remains at end of text after each character input
- [ ] **AC4**: Backspace removes character from end (not beginning)
- [ ] **AC5**: Long names (>20 characters) work correctly
- [ ] **AC6**: Other text fields in app (e.g., chat input) remain unaffected

### Testing Requirements
- [ ] **AC7**: Manual test - Type full name character by character, verify correct order
- [ ] **AC8**: Manual test - Use backspace, verify correct deletion
- [ ] **AC9**: DroidRun visual verification - Type name, verify display correct
- [ ] **AC10**: Regression test - Chat input still works correctly

### Quality Requirements
- [ ] **AC11**: No crashes when editing profile fields
- [ ] **AC12**: No performance degradation (typing feels responsive)

## Root Cause Hypothesis

### Likely Cause: State Update Triggering Cursor Reset

**Hypothesis 1: TextField value state triggers recomposition with selection reset**
```kotlin
// WRONG (likely current code):
var fullName by remember { mutableStateOf("") }

TextField(
    value = fullName,
    onValueChange = { fullName = it }  // Recomposition loses cursor position
)
```

**Expected Fix**:
```kotlin
// CORRECT:
var fullName by remember { mutableStateOf(TextFieldValue("")) }

TextField(
    value = fullName,
    onValueChange = { newValue ->
        fullName = newValue  // Preserves selection (cursor position)
    }
)
```

**Hypothesis 2: ViewModel state update pattern incorrect**
```kotlin
// WRONG (if this is the pattern):
private val _fullName = MutableStateFlow("")
val fullName: StateFlow<String> = _fullName

fun updateFullName(name: String) {
    _fullName.value = name  // State updates but TextFieldValue selection lost
}
```

**Expected Fix**:
```kotlin
// CORRECT:
private val _fullName = MutableStateFlow(TextFieldValue(""))
val fullName: StateFlow<TextFieldValue> = _fullName

fun updateFullName(value: TextFieldValue) {
    _fullName.value = value  // Preserves selection
}
```

**Hypothesis 3: DataStore updates interfering**
If profile is saved to DataStore on every keystroke, the read-back might reset cursor:
```kotlin
// WRONG:
onValueChange = {
    fullName = it
    viewModel.saveFullName(it)  // Async save + read-back loses cursor
}
```

**Expected Fix**:
```kotlin
// CORRECT:
onValueChange = {
    fullName = it  // Update UI immediately
}
onDone = {
    viewModel.saveFullName(fullName.text)  // Save only when done editing
}
```

## Technical Approach

### Phase 1: Investigation (1 hour)

**Step 1: Locate the profile settings UI code**
- Find SettingsScreen.kt or ProfileScreen.kt
- Identify Full Name and Nickname TextField components
- Read current implementation

**Step 2: Identify the root cause**
- Check if using `String` instead of `TextFieldValue`
- Check ViewModel state pattern (StateFlow<String> vs StateFlow<TextFieldValue>)
- Check if DataStore updates happening on every keystroke
- Add logs to track cursor position: `Log.d("ProfileSettings", "Cursor at: ${textFieldValue.selection}")`

**Step 3: Verify hypothesis**
- Type single character "N" → check Logcat for cursor position
- Type second character "a" → check if cursor jumped to 0
- Confirm which hypothesis is correct

### Phase 2: Fix Implementation (1 hour)

**If Hypothesis 1 (String state):**
```kotlin
// Change from:
var fullName by remember { mutableStateOf("") }

// To:
var fullName by remember { mutableStateOf(TextFieldValue("")) }

TextField(
    value = fullName,
    onValueChange = { newValue -> fullName = newValue }
)
```

**If Hypothesis 2 (ViewModel pattern):**
```kotlin
// ViewModel - change StateFlow type
private val _fullName = MutableStateFlow(TextFieldValue(""))
val fullName: StateFlow<TextFieldValue> = _fullName.asStateFlow()

fun updateFullName(value: TextFieldValue) {
    _fullName.value = value
}

// UI - collect as TextFieldValue
val fullName by viewModel.fullName.collectAsState()

TextField(
    value = fullName,
    onValueChange = { viewModel.updateFullName(it) }
)
```

**If Hypothesis 3 (DataStore interference):**
```kotlin
// Separate UI state from persisted state
var fullNameUI by remember { mutableStateOf(TextFieldValue(initialValue)) }

TextField(
    value = fullNameUI,
    onValueChange = { fullNameUI = it },
    keyboardActions = KeyboardActions(
        onDone = { viewModel.saveFullName(fullNameUI.text) }
    )
)
```

**Common Pattern: Load initial value correctly**
```kotlin
val savedFullName by viewModel.fullName.collectAsState()

var fullNameUI by remember(savedFullName) {
    mutableStateOf(TextFieldValue(savedFullName))
}
```

### Phase 3: Testing & Validation (1 hour)

**Manual Testing**:
1. Type "Nathan" character by character → verify displays "Nathan"
2. Backspace → verify deletes from end
3. Type long name (25+ chars) → verify works
4. Save and reload settings → verify value persisted
5. Test Nickname field → verify same fix works

**Visual Verification**:
```bash
droid "Open ai.ondevice.app, go to settings, tap Full Name field, type 'Nathan', verify it displays 'Nathan' not scrambled, then stop"
```

**Regression Testing**:
- Test chat input field → verify still works correctly
- Test other text inputs in app → verify no side effects

### Phase 4: Prevention (30 minutes)

**Add to error-handling-patterns.md**:
```markdown
## TextField Cursor Position Best Practices

### Always use TextFieldValue for editable text
```kotlin
// ✅ CORRECT
var text by remember { mutableStateOf(TextFieldValue("")) }

// ❌ WRONG (loses cursor position)
var text by remember { mutableStateOf("") }
```

### When using ViewModel state
```kotlin
// ViewModel
private val _text = MutableStateFlow(TextFieldValue(""))
val text: StateFlow<TextFieldValue> = _text.asStateFlow()

// UI
val text by viewModel.text.collectAsState()
TextField(value = text, onValueChange = { viewModel.updateText(it) })
```

### Separate UI state from persisted state
Save to DataStore only when editing complete (onDone), not on every keystroke.
```

## Implementation Files

### Files to Investigate
1. `SettingsScreen.kt` or `ProfileScreen.kt` - UI with text fields
2. `SettingsViewModel.kt` - State management
3. `ProfileDataStore.kt` or similar - Persistence layer

### Files to Modify
1. **UI file** (SettingsScreen.kt):
   - Change `mutableStateOf(String)` → `mutableStateOf(TextFieldValue)`
   - Update TextField value/onValueChange

2. **ViewModel** (SettingsViewModel.kt):
   - Change `StateFlow<String>` → `StateFlow<TextFieldValue>` (if needed)
   - Update state update methods

3. **Persistence** (ProfileDataStore.kt):
   - Ensure saves only on Done, not every keystroke (if applicable)

### Files to Create
1. `ProfileSettingsTest.kt` - Unit test for state management
2. `.maestro/flows/profile-settings-test.yaml` - Visual regression test (backup)

## References

### Existing Implementation
- Profile settings: `app/src/main/java/ai/ondevice/app/ui/settings/SettingsScreen.kt` (likely location)
- ViewModel: `app/src/main/java/ai/ondevice/app/ui/settings/SettingsViewModel.kt`

### Jetpack Compose TextFieldValue Documentation
- [TextFieldValue API](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/TextFieldValue)
- [TextField Best Practices](https://developer.android.com/jetpack/compose/text#textfield)

### Related Issues
- Similar bugs in other apps: [Stack Overflow](https://stackoverflow.com/questions/64181930/jetpack-compose-textfield-cursor-jumps-to-start)

## Timeline

| Phase | Duration | Output |
|-------|----------|--------|
| **Investigation** | 1 hour | Root cause identified with logs |
| **Fix** | 1 hour | Code changes complete |
| **Testing** | 1 hour | Manual + visual verification complete |
| **Prevention** | 30 min | Patterns documented |

**Total Effort**: 3.5 hours

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| User can type name correctly | 100% | Manual test (type "Nathan" → displays "Nathan") |
| Cursor stays at end | 100% | Visual verification |
| No regression in other fields | 100% | Test chat input field |
| Fix prevents future similar bugs | N/A | Pattern added to error-handling-patterns.md |

## Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Fix breaks other text fields | Low | High | Test all text inputs in app before shipping |
| ViewModel pattern requires breaking change | Medium | Medium | Accept TextFieldValue in ViewModel, extract .text when persisting |
| DataStore write frequency issue | Low | Low | Add debounce or save only onDone |

---

**Status**: PROPOSAL - Awaiting approval
**Next Step**: User reviews and approves → then `/openspec-apply profile-settings-cursor-fix`
