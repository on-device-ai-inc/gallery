# Spec Delta: profile-settings-cursor-fix

This document describes the specification changes resulting from fixing the profile settings cursor bug.

---

## MODIFIED

### error-handling-patterns.md (Add TextField Best Practices)

**Location**: `openspec/specs/error-handling-patterns.md`

```diff
# Error Handling Patterns Specification

## Overview
Standard patterns for error handling across OnDevice AI to ensure consistent user experience and debuggability.

+ ## UI Component Best Practices
+
+ ### TextField Cursor Position Management
+
+ **Problem**: Using `String` state with TextField causes cursor to reset to beginning after each character input.
+
+ **Root Cause**: TextField needs `TextFieldValue` to preserve cursor position (selection) across recompositions.
+
+ #### ✅ CORRECT Pattern - Composable State
+
+ ```kotlin
+ @Composable
+ fun ProfileSettingsScreen() {
+     // Use TextFieldValue, not String
+     var fullName by remember { mutableStateOf(TextFieldValue("")) }
+
+     TextField(
+         value = fullName,
+         onValueChange = { newValue ->
+             fullName = newValue  // Preserves selection (cursor position)
+         },
+         label = { Text("Full Name") }
+     )
+ }
+ ```
+
+ #### ❌ WRONG Pattern - String State
+
+ ```kotlin
+ @Composable
+ fun ProfileSettingsScreen() {
+     // ❌ Using String loses cursor position
+     var fullName by remember { mutableStateOf("") }
+
+     TextField(
+         value = fullName,
+         onValueChange = { fullName = it },  // Cursor resets to start!
+         label = { Text("Full Name") }
+     )
+ }
+ ```
+
+ #### ✅ CORRECT Pattern - ViewModel State
+
+ **ViewModel**:
+ ```kotlin
+ class SettingsViewModel : ViewModel() {
+     private val _fullName = MutableStateFlow(TextFieldValue(""))
+     val fullName: StateFlow<TextFieldValue> = _fullName.asStateFlow()
+
+     fun updateFullName(value: TextFieldValue) {
+         _fullName.value = value  // Store entire TextFieldValue with selection
+     }
+
+     fun saveFullName() {
+         // Extract text when persisting
+         dataStore.saveFullName(_fullName.value.text)
+     }
+ }
+ ```
+
+ **UI**:
+ ```kotlin
+ @Composable
+ fun ProfileSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
+     val fullName by viewModel.fullName.collectAsState()
+
+     TextField(
+         value = fullName,
+         onValueChange = { viewModel.updateFullName(it) },
+         keyboardActions = KeyboardActions(
+             onDone = { viewModel.saveFullName() }
+         )
+     )
+ }
+ ```
+
+ #### ✅ CORRECT Pattern - Separate UI and Persisted State
+
+ Use when you need to save to DataStore/Room but want UI state independence:
+
+ ```kotlin
+ @Composable
+ fun ProfileSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
+     // Load saved value once
+     val savedFullName by viewModel.fullName.collectAsState()
+
+     // Separate UI state (preserves cursor during editing)
+     var fullNameUI by remember(savedFullName) {
+         mutableStateOf(TextFieldValue(savedFullName))
+     }
+
+     TextField(
+         value = fullNameUI,
+         onValueChange = { fullNameUI = it },  // Update UI state only
+         keyboardActions = KeyboardActions(
+             onDone = {
+                 // Save to persistence when done editing
+                 viewModel.saveFullName(fullNameUI.text)
+             }
+         )
+     )
+ }
+ ```
+
+ #### Loading Initial Values
+
+ When loading saved text into TextField:
+
+ ```kotlin
+ // ✅ CORRECT - Cursor at end
+ val initialValue = savedFullName
+ var fullName by remember(initialValue) {
+     mutableStateOf(TextFieldValue(
+         text = initialValue,
+         selection = TextRange(initialValue.length)  // Cursor at end
+     ))
+ }
+
+ // ❌ WRONG - Cursor at start
+ var fullName by remember {
+     mutableStateOf(TextFieldValue(savedFullName))
+ }
+ ```
+
+ #### When to Save to DataStore
+
+ **✅ CORRECT - Save on Done/Blur**:
+ - User completes editing (Done button)
+ - User leaves field (focus lost)
+ - Minimizes writes, prevents cursor interference
+
+ **❌ WRONG - Save on Every Keystroke**:
+ - Creates performance issues (excessive writes)
+ - Risk of read-back resetting cursor
+ - No benefit (user hasn't finished typing)
+
+ #### Testing Checklist
+
+ For any TextField component:
+ - [ ] Type "Nathan" → displays "Nathan" (not "nahtaN")
+ - [ ] Backspace deletes from end, not beginning
+ - [ ] Cursor stays at end after each character
+ - [ ] Long text (30+ chars) works correctly
+ - [ ] Saved values load with cursor at end
+
 ## Principles

 1. **No Silent Failures**: Every error visible to user OR logged
 2. **Graceful Degradation**: Features fail without blocking core functionality
 3. **User-Friendly Messages**: Technical errors translated to actionable messages
 4. **Debug Context**: All errors logged with full context for troubleshooting
```

---

## ADDED

### profile-settings.md (New Spec)

**Location**: `openspec/specs/profile-settings.md`

```markdown
# Profile Settings Specification

## Overview
User profile management including display name, nickname, and preferences.

## Components

### ProfileDataStore
- Stores user profile data (full name, nickname)
- Proto DataStore for type safety
- Provides Flow<ProfileData> for observability

### SettingsViewModel
- Manages profile state for UI
- Handles validation and persistence
- Uses TextFieldValue for cursor-preserving editable fields

### SettingsScreen
- Profile editing UI
- Real-time validation feedback
- Keyboard-aware layout

## Profile Schema

```proto
message ProfileData {
  string full_name = 1;        // User's full name (display name)
  string nickname = 2;         // Optional short name or handle
  int64 created_at = 3;        // Timestamp of profile creation
  int64 updated_at = 4;        // Timestamp of last update
}
```

## Functional Requirements

### FR-1: Profile Fields

#### Full Name
- **Type**: Text input (single line)
- **Max Length**: 50 characters
- **Validation**: At least 2 characters, letters/spaces/hyphens only
- **Required**: Yes (for first-time setup)
- **Persistence**: Saved when user taps Done or leaves field

#### Nickname
- **Type**: Text input (single line)
- **Max Length**: 20 characters
- **Validation**: Alphanumeric, no spaces
- **Required**: No (optional)
- **Persistence**: Saved when user taps Done or leaves field

### FR-2: Profile Editing

#### Text Input Behavior
- Cursor remains at end of text after each character input
- Backspace deletes from end (last character)
- Typing new character appends to end
- Long names scroll horizontally within field
- **CRITICAL**: Use `TextFieldValue` state (not `String`) to preserve cursor position

#### Save Timing
- Save when keyboard Done button pressed
- Save when user navigates away from field (focus lost)
- Do NOT save on every keystroke (performance + cursor interference risk)

#### Loading Existing Profile
- Load saved values on screen open
- Cursor positioned at end of existing text
- Empty fields show placeholder text

### FR-3: First-Time Setup

#### Onboarding Flow
- After model download, show profile setup
- Full Name required (block "Continue" if empty)
- Nickname optional (can skip)
- After setup, navigate to chat

#### Skip Behavior
- User can skip by leaving fields empty initially
- Prompted again on first chat open (non-blocking)

### FR-4: Validation

#### Full Name Validation
- Min 2 characters: "Name must be at least 2 characters"
- Max 50 characters: "Name too long (max 50)"
- Invalid chars: "Name can only contain letters, spaces, and hyphens"

#### Nickname Validation
- Max 20 characters: "Nickname too long (max 20)"
- Invalid chars: "Nickname can only contain letters and numbers"

#### Validation Timing
- Validate on Done/Blur (not every keystroke)
- Show error below field (red text)
- Clear error when user starts typing again

## Non-Functional Requirements

### NFR-1: Performance
- Profile loads <100ms
- Typing feels instant (no lag)
- Save operation <50ms (non-blocking)

### NFR-2: Reliability
- Profile persisted immediately on save (no data loss)
- Handles process death gracefully (saved data restored)
- No crashes on long input or special characters

### NFR-3: Accessibility
- Fields have content descriptions for screen readers
- Keyboard navigation works correctly
- High contrast support for text fields

## Implementation Guidelines

### ViewModel State Management

```kotlin
class SettingsViewModel @Inject constructor(
    private val profileDataStore: ProfileDataStore
) : ViewModel() {

    // UI state uses TextFieldValue to preserve cursor
    private val _fullName = MutableStateFlow(TextFieldValue(""))
    val fullName: StateFlow<TextFieldValue> = _fullName.asStateFlow()

    private val _nickname = MutableStateFlow(TextFieldValue(""))
    val nickname: StateFlow<TextFieldValue> = _nickname.asStateFlow()

    init {
        // Load saved profile
        viewModelScope.launch {
            profileDataStore.profileData.collect { profile ->
                _fullName.value = TextFieldValue(
                    text = profile.fullName,
                    selection = TextRange(profile.fullName.length)
                )
                _nickname.value = TextFieldValue(
                    text = profile.nickname,
                    selection = TextRange(profile.nickname.length)
                )
            }
        }
    }

    fun updateFullName(value: TextFieldValue) {
        _fullName.value = value
    }

    fun updateNickname(value: TextFieldValue) {
        _nickname.value = value
    }

    fun saveProfile() {
        viewModelScope.launch {
            profileDataStore.updateProfile(
                fullName = _fullName.value.text,
                nickname = _nickname.value.text
            )
        }
    }
}
```

### UI Implementation

```kotlin
@Composable
fun ProfileSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val fullName by viewModel.fullName.collectAsState()
    val nickname by viewModel.nickname.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {

        // Full Name Field
        TextField(
            value = fullName,
            onValueChange = { viewModel.updateFullName(it) },
            label = { Text("Full Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { /* Focus nickname field */ }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nickname Field
        TextField(
            value = nickname,
            onValueChange = { viewModel.updateNickname(it) },
            label = { Text("Nickname (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.saveProfile()
                    // Hide keyboard, navigate away, etc.
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button (optional if using onDone)
        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}
```

## Testing Requirements

### Unit Tests
- SettingsViewModelTest:
  - [ ] updateFullName preserves TextFieldValue
  - [ ] updateNickname preserves TextFieldValue
  - [ ] saveProfile persists text values to DataStore
  - [ ] Loading profile sets cursor at end of text

### UI Tests
- ProfileSettingsUITest:
  - [ ] Type "Nathan" in Full Name → displays "Nathan"
  - [ ] Type "Nick" in Nickname → displays "Nick"
  - [ ] Backspace deletes from end
  - [ ] Long names (30+ chars) scroll correctly
  - [ ] Save and reload preserves values

### Manual Tests
- [ ] Type full name character by character → correct order
- [ ] Use backspace → deletes from end
- [ ] Save → reload settings → values persisted
- [ ] Skip nickname → only full name saved
- [ ] Invalid chars → validation error shown

### DroidRun Visual Tests
```bash
droid "Open ai.ondevice.app, go to settings, tap Full Name, type 'Nathan', verify displays 'Nathan', then stop"
droid "Open ai.ondevice.app, go to settings, tap Nickname, type 'Nick', verify displays 'Nick', then stop"
```

## Acceptance Criteria

- [ ] User can type "Nathan" in Full Name → displays "Nathan" (not scrambled)
- [ ] User can type "Nick" in Nickname → displays "Nick"
- [ ] Cursor stays at end of text after each character
- [ ] Backspace deletes from end, not beginning
- [ ] Long names (50 chars) work correctly
- [ ] Saved profile loads with values intact
- [ ] Cursor positioned at end when loading existing text
- [ ] Validation messages display for invalid input
- [ ] No crashes on any input
- [ ] Performance: typing feels instant, no lag

## Known Issues

### ✅ FIXED: Cursor Jumping Bug
**Issue**: Cursor jumped to beginning after each character when typing name.

**Root Cause**: TextField state used `String` instead of `TextFieldValue`, losing cursor position on recomposition.

**Fix**: Changed to `TextFieldValue` state in ViewModel and UI.

**Fixed Date**: [To be filled on completion]

**Files Changed**:
- SettingsViewModel.kt (StateFlow types changed)
- SettingsScreen.kt (TextField state changed)

## References

- [TextFieldValue API](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/input/TextFieldValue)
- [TextField Best Practices](https://developer.android.com/jetpack/compose/text#textfield)
- error-handling-patterns.md (TextField cursor position best practices)
```

---

## REMOVED

**None** - This change only adds/modifies specs, no specs are removed.

---

## Summary of Changes

| Spec File | Type | Changes |
|-----------|------|---------|
| `error-handling-patterns.md` | MODIFIED | Added complete section on TextField cursor position management with ✅ CORRECT and ❌ WRONG patterns |
| `profile-settings.md` | ADDED | New comprehensive spec for profile settings including schema, validation, implementation guidelines, testing |

## Rationale

### Why Modify error-handling-patterns.md?
This cursor bug is a **UI component best practice**, not a profile-specific issue. Adding it to error-handling-patterns.md ensures:
- All developers know the correct TextField pattern
- Prevents same bug in other text input features
- Provides reference examples for future development

### Why Add profile-settings.md?
Currently no spec exists for profile settings. This new spec:
- Documents expected behavior (FR-1, FR-2)
- Specifies validation rules (FR-4)
- Provides implementation patterns (ViewModel + UI code)
- Includes acceptance criteria for testing
- Creates single source of truth for profile feature

## Migration Path

1. **Immediate**: Fix profile settings to match profile-settings.md spec
2. **Future**: Apply TextField best practices from error-handling-patterns.md to any new text input features
3. **Opportunistic**: Refactor existing text inputs to use TextFieldValue pattern (chat input, search, etc.)

## Validation

After implementation:
- [ ] SettingsViewModel.kt uses StateFlow<TextFieldValue> per profile-settings.md
- [ ] SettingsScreen.kt uses TextFieldValue state per profile-settings.md
- [ ] All TextField components match pattern in error-handling-patterns.md
- [ ] All acceptance criteria in profile-settings.md are met
- [ ] DroidRun visual tests pass
