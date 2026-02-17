# Spec Delta: navigation-refactor

## MODIFIED

### `ui/navigation/GalleryNavGraph.kt` (full replacement)
```diff
- 3 routes: ROUTE_HOMESCREEN, ROUTE_MODEL_LIST, ROUTE_MODEL
+ 10 routes: ROUTE_PLACEHOLDER, ROUTE_SETTINGS, ROUTE_CUSTOM_INSTRUCTIONS,
+            ROUTE_PRIVACY_CENTER, ROUTE_STORAGE_MANAGEMENT, ROUTE_MODEL_PARAMETERS,
+            ROUTE_MODEL_SELECTION, ROUTE_CONVERSATION_LIST, ROUTE_CONVERSATION_DETAIL,
+            ROUTE_MODEL

- startDestination = ROUTE_HOMESCREEN
+ startDestination = ROUTE_PLACEHOLDER

- No TOS dialog
+ TosDialog overlay (mandatory accept, no dismiss) shown before auto-routing

- No auto-routing LaunchedEffect
+ LaunchedEffect(modelDownloadStatus, showTosDialog):
+   model downloaded → navigate("$ROUTE_MODEL/$taskId/$modelName")
+   no model        → navigate(ROUTE_MODEL_SELECTION)

- BackHandler inside CustomTaskScreen only
+ BackHandler(enabled = currentRoute == ROUTE_PLACEHOLDER) { activity?.finish() }

- CustomTaskScreen: hideTopBar, useThemeColor, customNavigateUpCallback params
+ CustomTaskScreen: simpler GoraAI version (no hideTopBar/useThemeColor/customNavigateUpCallback)

- isLegacyTasks() call in ROUTE_MODEL composable
+ isBuiltInTask() call

- CustomTaskDataForBuiltinTask(modelManagerViewModel, onNavUp)
+ CustomTaskDataForBuiltinTask(modelManagerViewModel, onNavUp, onNavigateToConversationHistory,
+   onNavigateToSettings, loadConversationId, onConversationLoaded)

- enableHomeScreenAnimation, enableModelListAnimation state
+ removed (no longer needed without HOME/MODEL_LIST routes)
```

### `data/Tasks.kt` (align with GoraAI)
```diff
  object BuiltInTaskId {
    const val LLM_CHAT = "llm_chat"
    const val LLM_PROMPT_LAB = "llm_prompt_lab"
    const val LLM_ASK_IMAGE = "llm_ask_image"
    const val LLM_ASK_AUDIO = "llm_ask_audio"
+   const val IMAGE_GENERATION = "image_generation"
-   const val LLM_MOBILE_ACTIONS = "llm_mobile_actions"  // if present - REMOVED
-   const val LLM_TINY_GARDEN = "llm_tiny_garden"         // if present - REMOVED
  }

- fun isLegacyTasks(id: String): Boolean { ... }  // if GoraAI removes it - REMOVED
+ fun isBuiltInTask(id: String): Boolean { ... }   // GoraAI's version
```

### `customtasks/common/CustomTaskData.kt` (align with GoraAI)
```diff
  data class CustomTaskData(
    val modelManagerViewModel: ModelManagerViewModel,
    val bottomPadding: Dp = 0.dp,
    val setAppBarControlsDisabled: (Boolean) -> Unit = {},
-   val setTopBarVisible: (Boolean) -> Unit = {},         // REMOVED (TinyGarden deleted)
-   val setCustomNavigateUpCallback: ((() -> Unit)?) -> Unit = {}, // REMOVED (TinyGarden deleted)
  )

  data class CustomTaskDataForBuiltinTask(
    val modelManagerViewModel: ModelManagerViewModel,
    val onNavUp: () -> Unit,
+   val onNavigateToConversationHistory: () -> Unit = {},
+   val onNavigateToSettings: () -> Unit = {},
+   val loadConversationId: Long? = null,
+   val onConversationLoaded: () -> Unit = {},
  )
```

## ADDED

### `ui/settings/` (9 new files from GoraAI)
- `SettingsScreen.kt` — main settings hub (nav to sub-screens)
- `CustomInstructionsScreen.kt` — system prompt editor (Story 8)
- `PrivacyCenterScreen.kt` — privacy controls (Story 10)
- `StorageManagementScreen.kt` — model storage management (Story 9)
- `ModelParametersScreen.kt` — temperature, topK, topP, maxTokens sliders (Story 7)
- `ModelParameterPresets.kt` — preset parameter configurations
- `SettingsViewModel.kt` — state management for settings
- `LicenseViewerScreen.kt` — open source license display
- `AboutSection.kt` — app version and info

### `ui/modelselection/` (1 new file from GoraAI)
- `ModelSelectionScreen.kt` — first-launch model selection/download

### `ui/conversationlist/` (3 new files from GoraAI)
- `ConversationListScreen.kt` — list of past conversation threads
- `ConversationListViewModel.kt` — state management, reads from Room DAO
- `ConversationListUiState.kt` — UI state data class

### `ui/conversationdetail/` (3 new files from GoraAI)
- `ConversationDetailScreen.kt` — message history for single thread
- `ConversationDetailViewModel.kt` — loads thread + messages from Room DAO
- `ConversationDetailUiState.kt` — UI state data class

### `ui/home_archived/` (moved, not new)
- `ui/home_archived/home/HomeScreen.kt` — archived (moved from `ui/home/`)
- `ui/home_archived/home/SettingsDialog.kt` — archived (if present)
- Other files from `ui/home/` — archived alongside

## REMOVED

### `customtasks/tinygarden/` — entire package deleted
- Not in GoraAI target state
- No replacement (feature cut)

### `customtasks/mobileactions/` — entire package deleted
- Not in GoraAI target state
- No replacement (feature cut)

### From nav graph:
- `ROUTE_HOMESCREEN` — replaced by ROUTE_PLACEHOLDER + auto-routing
- `ROUTE_MODEL_LIST` — removed (model selection done via ROUTE_MODEL_SELECTION)

## NOT CHANGED

- All existing `ui/llmchat/` files (LlmChatScreen, LlmChatViewModel, LlmChatTaskModule)
- All existing `ui/llmsingleturn/` files
- All existing `ui/common/` files (shared components)
- `di/AppModule.kt` (already has Room providers from room-database-data-layer spec)
- All existing Room data layer files (ConversationThread, ConversationMessage, etc.)
- `customtasks/examplecustomtask/` — kept (matches GoraAI)
- `ui/theme/` — kept unchanged
