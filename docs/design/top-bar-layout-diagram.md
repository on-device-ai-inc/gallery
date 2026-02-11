# ModelPageAppBar Layout Diagram

## Visual Layout

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         ModelPageAppBar (Top Bar)                          │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│  ┌───┐ ┌──┐              ┌──────────────┐                ┌──┐  ┌───┐     │
│  │ ☰ │ │🔒│              │  Gemini 2.0  │                │⚙️│  │💬 │     │
│  └───┘ └──┘              └──────────────┘                └──┘  └───┘     │
│   (1)   (2)                    (3)                         (4)   (5)      │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### LEFT SIDE (navigationIcon)

#### (1) Hamburger Menu Button
- **Icon:** `Icons.Rounded.Menu` (☰)
- **Type:** `IconButton`
- **Size:** 48×48dp (default IconButton size)
- **Function:** Opens navigation drawer/menu
- **State:** Disabled when model initializing or inference in progress
- **File Reference:** `ModelPageAppBar.kt:118-120`

#### (2) Lock Icon
- **Icon:** `Icons.Filled.Lock` (🔒) OR `Icons.Filled.LockOpen` (🔓)
- **Type:** `Icon` (not clickable)
- **Size:** 18×18dp
- **Padding:** 4dp start padding
- **Color:**
  - 🔒 `MaterialTheme.colorScheme.primary` (blue) when web search disabled
  - 🔓 `MaterialTheme.colorScheme.tertiary` (orange) when web search enabled
- **Opacity:** 0.8f
- **Function:** Visual indicator of web search status
- **File Reference:** `ModelPageAppBar.kt:122-138`

### CENTER (title)

#### (3) Model Bubble (ModelPickerChip)
- **Component:** `ModelPickerChip` composable
- **Type:** Pill-shaped button with model name
- **Offset:** +11dp to the right (compensates for lock icon's 22dp visual weight)
- **Function:** Opens model selection dialog
- **State:** Disabled when model initializing or inference in progress
- **Contains:** Current model name (e.g., "Gemini 2.0")
- **File Reference:** `ModelPageAppBar.kt:100-108`

**Why the offset?**
```
Without offset:                  With 11dp offset:
┌─────────────┐                  ┌─────────────┐
│☰ 🔒  [Model]│                  │☰ 🔒    [Model]│
└─────────────┘                  └─────────────┘
     ↑                                  ↑
  Off-center                      Optically centered
  (lock icon                      (11dp = half of
   adds 22dp                       lock's 22dp
   visual weight)                  visual weight)
```

### RIGHT SIDE (actions)

#### (4) Config Button (Tune)
- **Icon:** `Icons.Rounded.Tune` (⚙️)
- **Type:** `IconButton`
- **Size:** 20×20dp icon inside 42×42dp button
- **Function:** Opens model configuration dialog
- **Visibility:** Only shown when `model.configs.isNotEmpty() && downloadSucceeded`
- **State:** Disabled when model initializing, in progress, or not initialized
- **Opacity:** 0.5f when disabled, 1.0f when enabled
- **Special Positioning:** Offset -40dp to the left when BOTH config and new chat buttons show
- **File Reference:** `ModelPageAppBar.kt:151-165`

#### (5) New Chat Button (Reset Session)
- **Icon:** `Icons.Rounded.MapsUgc` (💬)
- **Type:** `IconButton`
- **Size:** 20×20dp icon inside 32×32dp circular background
- **Background:** `MaterialTheme.colorScheme.surfaceContainer` with `CircleShape`
- **Function:** Resets conversation session (creates new chat)
- **Visibility:** Only shown when `canShowResetSessionButton && downloadSucceeded`
- **State:**
  - Shows `CircularProgressIndicator` (16dp) when `isResettingSession`
  - Disabled when model initializing, preparing, or not initialized
- **Opacity:** 0.5f when disabled, 1.0f when enabled
- **File Reference:** `ModelPageAppBar.kt:167-197`

## Layout Hierarchy

```
CenterAlignedTopAppBar {
    navigationIcon = {
        Row(horizontalArrangement = Start, verticalAlignment = CenterVertically) {
            IconButton { Icon(Menu) }        // (1) Hamburger
            Icon(Lock/LockOpen)              // (2) Lock
        }
    }

    title = {
        Box(modifier = Offset(x = 11.dp)) {
            ModelPickerChip(...)             // (3) Model Bubble
        }
    }

    actions = {
        Box(size = 42.dp, contentAlignment = Center) {
            if (showConfigButton) {
                IconButton(offset = configButtonOffset) {
                    Icon(Tune)               // (4) Config
                }
            }
            if (showResetSessionButton) {
                if (isResettingSession) {
                    CircularProgressIndicator()
                } else {
                    IconButton {
                        Box(32.dp, CircleShape) {
                            Icon(MapsUgc)    // (5) New Chat
                        }
                    }
                }
            }
        }
    }
}
```

## State Management

### Disabled States
All interactive elements (Hamburger, ModelPickerChip, Config, NewChat) are disabled when:
- `modelInitializationStatus == INITIALIZING`
- `inProgress == true` (model generating response)

Config button additionally requires:
- `modelInitializationStatus == INITIALIZED`

NewChat button additionally requires:
- `!modelPreparing`

### Visibility Conditions

| Button | Condition |
|--------|-----------|
| Hamburger | Always visible |
| Lock | Always visible |
| Model Bubble | Always visible |
| Config | `model.configs.isNotEmpty() && downloadSucceeded` |
| NewChat | `canShowResetSessionButton && downloadSucceeded` |

### Special Layout: Both Buttons Showing

When BOTH config and new chat buttons are visible:

```
WITHOUT offset:                  WITH -40dp offset:
┌──────────────┐                 ┌──────────────┐
│         ⚙️ 💬│                 │       ⚙️   💬│
└──────────────┘                 └──────────────┘
         ↑                                ↑
    Overlapping                    Properly spaced
                                   (config moved 40dp left)
```

Code:
```kotlin
var configButtonOffset = 0.dp
if (showConfigButton && canShowResetSessionButton) {
    configButtonOffset = (-40).dp
}
```

## File Reference

**Primary File:** `app/src/main/java/ai/ondevice/app/ui/common/ModelPageAppBar.kt`

**Key Line Numbers:**
- Lines 94-200: Full `CenterAlignedTopAppBar` implementation
- Lines 112-140: Navigation icon (hamburger + lock)
- Lines 95-109: Title (model bubble with offset)
- Lines 142-199: Actions (config + new chat with conditional logic)

## Design Rationale

1. **Lock Icon Placement:** Next to hamburger menu for visibility without being interactive
2. **Model Bubble Offset:** Compensates for asymmetric left side (hamburger + lock)
3. **Config Button Offset:** Prevents overlap when both action buttons are visible
4. **New Chat Circle Background:** Visual distinction from config button
5. **Disabled Opacity:** 0.5f provides clear visual feedback for disabled state
6. **Progress Indicator:** Replaces new chat button during reset to show action in progress

## Usage Example

This top bar is used in:
- AI Chat screen
- Ask Image screen
- Audio Scribe screen

All three screens share the same `ModelPageAppBar` composable with different `task` and `model` parameters.
