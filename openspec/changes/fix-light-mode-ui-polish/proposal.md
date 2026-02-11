# Proposal: Fix Light Mode UI Polish Issues

## Summary
Fix three UX polish issues: ToS dialog transparency in light mode, model selector icon borders, and remove unnecessary rotating logo during model download in model picker.

## Motivation

### Issue 1: ToS Dialog Too Transparent in Light Mode
- **Current**: AlertDialog uses default Material3 styling which may have insufficient contrast in light mode
- **Problem**: Users in light mode find the ToS dialog hard to read due to transparency/low contrast
- **Impact**: Poor first-run experience, accessibility issues

### Issue 2: Model Selector Icon Has Borders
- **Current**: Model icons in selector screen have visible borders
- **Problem**: Inconsistent with the clean borderless logo shown in empty chat screen
- **Expected**: Clean icon without borders, matching the aesthetic of `R.mipmap.ic_launcher_foreground` in empty chat
- **Impact**: Visual inconsistency, less polished appearance

### Issue 3: Rotating Logo During Model Download in Model Picker
- **Current**: When selecting a model from model picker (top center), a rotating logo appears during download
- **Problem**: Unnecessary visual noise - download progress is already shown elsewhere
- **Expected**: No rotating logo in model picker during download
- **Impact**: Cleaner UI, less distraction

## Scope

### Included
- ✅ Fix ToS dialog background/scrim opacity for light mode
- ✅ Audit ALL dialogs/overlays for light mode visibility
- ✅ Remove borders from model selector icons
- ✅ Remove RotationalLoader from model picker during download
- ✅ Test in both light and dark modes

### NOT Included
- ❌ Redesigning dialog layouts
- ❌ Changing dark mode appearance (already works)
- ❌ Modifying model download progress indicators elsewhere (those are needed)

## Acceptance Criteria
- [ ] ToS dialog is fully legible in light mode (solid background, good contrast)
- [ ] All dialogs/overlays checked and fixed for light mode visibility
- [ ] Model selector icons have no borders (clean like empty chat logo)
- [ ] Model picker shows NO rotating logo during download
- [ ] Model initialization status chip still shows rotating loader (this is fine - different context)
- [ ] All changes tested in both light and dark modes
- [ ] No regressions in dark mode

## Technical Approach

### Fix 1: ToS Dialog Light Mode Contrast

**Root Cause**: `AlertDialog` may not have sufficient background opacity in light mode.

**Solution**:
```kotlin
// TermsOfServiceDialog.kt
AlertDialog(
    // Add explicit surface container highest for better contrast
    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    // ... rest of dialog
)
```

**Alternative**: Check if scrim color needs adjustment too.

### Fix 2: Audit All Dialogs/Overlays

Files to check:
- `TermsOfServiceDialog.kt` ✓
- `ConfigDialog.kt`
- `ConfirmDeleteModelDialog.kt`
- Any `ModalBottomSheet` usages
- Any custom overlays

Ensure all use theme-aware colors with sufficient contrast.

### Fix 3: Reuse Empty Chat Icon for Model Selector

**Approach**: Don't fix the existing icon - just replace it with the same clean icon from empty chat.

**Solution**: Reuse `R.mipmap.ic_launcher_foreground` from empty chat:
```kotlin
// Empty chat logo (reference)
Image(
    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
    contentDescription = "OnDevice Logo",
    modifier = Modifier.size(160.dp)  // or appropriate size for selector
)
```

**Simple**: No need to investigate borders - just use the proven working icon.

### Fix 4: Remove Rotating Logo from Model Picker

**File**: Likely in `ChatView.kt` or `ModelSelector.kt` where model picker dropdown is rendered.

**Investigation**: Find where `RotatingLogoIcon` or `RotationalLoader` is shown during download in the top center model picker.

**Solution**:
```kotlin
// BEFORE: Shows rotating logo during download
if (downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS) {
    RotationalLoader(size = 14.dp)
}

// AFTER: Remove the rotating loader entirely
// Model picker should just show model name, no download indicator
```

**Keep**: `ModelInitializationStatusChip` - this is a different component (in-chat status, not model picker).

## Files to Investigate

1. **ToS Dialog**:
   - `app/src/main/java/ai/ondevice/app/ui/home_archived/home/TermsOfServiceDialog.kt`

2. **Model Selector Icon**:
   - `app/src/main/java/ai/ondevice/app/ui/common/modelitem/ModelItem.kt`
   - `app/src/main/java/ai/ondevice/app/ui/common/modelitem/StatusIcon.kt`
   - `app/src/main/java/ai/ondevice/app/ui/common/modelitem/ModelNameAndStatus.kt`

3. **Model Picker Download Icon**:
   - `app/src/main/java/ai/ondevice/app/ui/common/chat/ModelSelector.kt`
   - `app/src/main/java/ai/ondevice/app/ui/common/chat/ChatView.kt`
   - Look for `RotatingLogoIcon` or `RotationalLoader` in model picker context

4. **Other Dialogs to Audit**:
   - `app/src/main/java/ai/ondevice/app/ui/common/ConfigDialog.kt`
   - `app/src/main/java/ai/ondevice/app/ui/common/modelitem/ConfirmDeleteModelDialog.kt`

## Risk Assessment

**Low Risk**:
- UI polish only, no functionality changes
- Easy to verify visually
- Easy to revert if issues arise
- No impact on core features

## References

- Related: `openspec/changes/fix-light-mode-drawer-topic-extraction` (similar light mode fixes)
- Empty chat logo reference: `R.mipmap.ic_launcher_foreground` in ChatPanel.kt line 336
