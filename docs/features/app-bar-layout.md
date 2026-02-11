# ModelPageAppBar Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  [≡] [🔒]          [ Gemma 2B 2.6 ▼ ]           [⚙️] [💬]       │
│   │    │                    │                      │    │        │
│   │    │                    │                      │    │        │
│   │    │                    │                      │    └─ Reset Session
│   │    │                    │                      └────── Model Config (Tune)
│   │    │                    └───────────────────────────── Model Picker Chip (centered)
│   │    └──────────────────────────────────────────────────── Lock Icon (web search indicator)
│   └───────────────────────────────────────────────────────────── Hamburger Menu
└─────────────────────────────────────────────────────────────────┘
```

## Lock Icon States

### When Web Search is DISABLED (default)
```
[🔒] - MaterialTheme.colorScheme.primary
```
- Uses the theme's **primary color** (not necessarily blue - depends on app theme)
- Icon: `android.R.drawable.ic_secure` (closed lock)
- Meaning: "Secure - on-device only, queries stay private"

### When Web Search is ENABLED
```
[🔓] - MaterialTheme.colorScheme.tertiary
```
- Uses the theme's **tertiary color** (typically green/teal/amber in Material 3)
- Icon: `android.R.drawable.ic_lock_lock` (open lock or unlocked state)
- Meaning: "Web search enabled - queries sent to Brave API"

## Bottom Input Area Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  [Message input area...]                                        │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 🌐 Web Search  [Toggle Switch: ON/OFF]                   │  │
│  │                                                           │  │
│  │ 📊 3/5 searches used today  ← Only shows when toggle ON  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Color Clarification

I said "blue" but that was imprecise. The actual colors are:

- **Disabled (on-device)**: `MaterialTheme.colorScheme.primary`
  - This is whatever primary color your app theme uses
  - Could be blue, purple, red, etc. depending on theme

- **Enabled (web search)**: `MaterialTheme.colorScheme.tertiary`
  - Material 3 tertiary colors are typically green/teal/amber
  - Used to indicate "caution" or "different mode"

The key is the **color contrast**:
- Primary color = normal/safe mode (private)
- Tertiary color = special mode (queries leave device)
