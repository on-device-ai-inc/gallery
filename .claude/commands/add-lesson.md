Add a lesson learned to LESSONS_LEARNED.md.

## When to Use - IMMEDIATELY when:

1. ✅ Something failed, then you figured out the solution
2. ✅ User redirected you to a better approach
3. ✅ A feature didn't work, retry with different method succeeded
4. ✅ Discovered something non-obvious

## Process

1. **Read current LESSONS_LEARNED.md**
2. **Determine which section to update:**
   - Failed approach → "🔴 CRITICAL - Never Do This"
   - Working approach → "🟢 ALWAYS Do This" or relevant pattern section
3. **Add detailed entry with code example**
4. **Add to Change Log at bottom**

## Example

If you learned "Use Maestro for taps, not adb coordinates":

**Add to "Never Do This":**
```markdown
- **NEVER use `adb shell input tap X Y`** - coordinates break across devices
```

**Add to "Maestro Patterns":**
```markdown
### Reliable Element Taps
- tapOn: "Button Text"
- tapOn:
    id: "element_id"
```

**Add to Change Log:**
```markdown
| 2025-01-07 | Use Maestro for taps, not adb coordinates | UI Automation |
```

## Categories

- UI Automation
- ADB/Device
- Build/Gradle
- Git
- Testing/TDD
- Debugging
- ARM/DGX
- Waydroid
- Maestro
- General
