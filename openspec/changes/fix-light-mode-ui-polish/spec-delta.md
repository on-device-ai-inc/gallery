# Spec Delta: Fix Light Mode UI Polish Issues

## MODIFIED

### ui-spec.md (Dialog Contrast Requirements)

```diff
  ## Dialogs and Overlays

+ ### Light Mode Contrast Requirements
+
+ **CRITICAL**: All dialogs and overlays MUST have sufficient contrast in light mode.
+
+ #### AlertDialog
+ ```kotlin
+ AlertDialog(
+     containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
+     // Ensures solid background with good contrast in both themes
+     // ...
+ )
+ ```
+
+ #### ModalBottomSheet
+ ```kotlin
+ ModalBottomSheet(
+     containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
+     scrimColor = MaterialTheme.colorScheme.scrim,
+     // ...
+ )
+ ```
+
+ **Testing**: ALWAYS test dialogs in both light and dark modes before shipping.

  ### Terms of Service Dialog

- - Uses default AlertDialog styling
+ - Uses MaterialTheme.colorScheme.surfaceContainerHighest for proper contrast
+ - Tested in both light and dark modes
```

### ui-spec.md (Icon Styling)

```diff
  ## Model Icons

  ### Consistency Rule

+ **CRITICAL**: Model icons MUST NOT have borders.
+
+ Icons should match the clean aesthetic of the app logo shown in empty chat:
+ - No border modifiers
+ - No stroke/outline
+ - Clean, flat appearance
+
  #### Empty Chat Logo (Reference)
  ```kotlin
  Image(
      painter = painterResource(id = R.mipmap.ic_launcher_foreground),
      contentDescription = "OnDevice Logo",
      modifier = Modifier.size(160.dp)
+     // Note: No border, clean appearance
  )
  ```

  #### Model Selector Icons (Must Match)
  ```kotlin
- // WRONG: Has borders
- Icon(
-     modifier = Modifier.border(...)
- )
-
  // CORRECT: Clean, no borders
  Icon(
      painter = painterResource(id = task.iconResId),
+     modifier = Modifier.size(48.dp)
+     // No border modifier
  )
  ```
```

### ui-spec.md (Download Progress Indicators)

```diff
  ## Download Progress Indicators

  ### Context-Specific Display Rules

+ **Model Picker (Top Center)**:
+ - NEVER show rotating logo during download
+ - Reason: Creates visual noise, progress shown elsewhere
+ - User selects model → picker closes → progress shown in main UI
+
+ ```kotlin
+ // Model picker dropdown - NO rotating loader
+ ModelSelector(
+     model = model,
+     // Do not show RotationalLoader here
+ )
+ ```
+
  **Model Initialization Status Chip**:
  - Shows rotating loader (14dp)
+ - This is CORRECT - different context (in-chat status)

  **Model Download Panel**:
  - Shows large rotating loader (160dp) + progress text
+ - This is CORRECT - dedicated download screen

+ **Long Response Status**:
+ - Shows small rotating loader (16dp)
+ - This is CORRECT - indicates LLM is working
```

## Files to Modify

### 1. TermsOfServiceDialog.kt
```diff
  AlertDialog(
      onDismissRequest = onDismiss,
+     containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
      title = { ... },
```

### 2. ConfigDialog.kt (if exists - audit for light mode)
```diff
  // Check and add containerColor if needed
+ containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
```

### 3. Model Selector Icon (location TBD - investigate)
```diff
- Icon(
-     modifier = Modifier.border(1.dp, ...)
- )
+ Icon(
+     modifier = Modifier.size(48.dp)
+     // No border
+ )
```

### 4. Model Picker Download Indicator (location TBD - investigate)
```diff
- // Remove rotating logo from model picker during download
- if (downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS) {
-     RotationalLoader(size = 14.dp)
- }
+ // Model picker should not show download indicator
+ // Progress is shown in main UI after picker closes
```

## Investigation Required

Before implementation, need to locate:
1. ✅ ToS dialog - `TermsOfServiceDialog.kt` (found)
2. ❓ Model selector icon border code (investigate ModelItem.kt, StatusIcon.kt)
3. ❓ Model picker rotating logo code (investigate ModelSelector.kt, ChatView.kt)
4. ❓ Other dialogs to audit (search for AlertDialog, ModalBottomSheet)

## Testing Requirements

### Light Mode Test Cases
- [ ] ToS dialog: Open, verify legibility, good contrast
- [ ] Config dialog: Open, verify legibility
- [ ] Delete model dialog: Open, verify legibility
- [ ] Model selector: Verify icons have no borders
- [ ] Model picker: Select model, start download, verify NO rotating logo

### Dark Mode Regression Tests
- [ ] ToS dialog: Verify still looks good
- [ ] All other dialogs: Verify no regressions
- [ ] Model selector: Verify icons still look good
- [ ] Model picker: Verify NO rotating logo (same as light mode)

### Preserve Existing Behavior
- [ ] Model initialization status chip: STILL shows rotating loader (correct)
- [ ] Model download panel: STILL shows large rotating loader (correct)
- [ ] Long response status: STILL shows rotating loader (correct)
