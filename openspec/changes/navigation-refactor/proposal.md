# Proposal: navigation-refactor

## Summary
Replace the 3-route Edge Gallery nav graph with GoraAI's 10-route nav graph. Source of truth: GoraAI target at `/tmp/goraai-target/`. This removes the task-picker home screen and replaces it with a chat-first flow: TOS → Model Selection (first launch) or direct to chat (returning user).

## Motivation
Our current app is an Edge Gallery clone: task-picker home screen, model list, TinyGarden, Mobile Actions. The GoraAI target is a focused chat-first app with conversation history, settings, and no task picker. The navigation graph is the structural backbone — until this refactor is complete, no GoraAI features (settings, conversation history, first-launch flow) can be wired in.

## Scope

### Included

**1. Replace `GalleryNavGraph.kt`** — verbatim copy from GoraAI target
- `ROUTE_PLACEHOLDER` as `startDestination` (blank composable, auto-routes away)
- `ROUTE_MODEL/{taskId}/{modelName}` — updated for `loadThreadId`, new nav callbacks
- `ROUTE_SETTINGS`, `ROUTE_CUSTOM_INSTRUCTIONS`, `ROUTE_PRIVACY_CENTER`, `ROUTE_STORAGE_MANAGEMENT`, `ROUTE_MODEL_PARAMETERS`
- `ROUTE_MODEL_SELECTION` (first-launch onboarding)
- `ROUTE_CONVERSATION_LIST`, `ROUTE_CONVERSATION_DETAIL/{threadId}`
- TOS dialog overlay using existing `TosDialog` + `TosViewModel`
- Auto-routing `LaunchedEffect`: TOS accepted + model downloaded → chat; else → model selection
- `BackHandler` at `ROUTE_PLACEHOLDER` exits app
- Deep link: `ai.ondevice.app://model/{taskId}/{modelName}`
- `CustomTaskScreen` composable — use GoraAI's simpler version

**2. Copy 16 new screen files from GoraAI** (verbatim + copyright header update)
- `ui/settings/` — 9 files: SettingsScreen, CustomInstructionsScreen, PrivacyCenterScreen, StorageManagementScreen, ModelParametersScreen, SettingsViewModel, AboutSection, LicenseViewerScreen, ModelParameterPresets
- `ui/modelselection/ModelSelectionScreen.kt`
- `ui/conversationlist/` — 3 files: ConversationListScreen, ConversationListViewModel, ConversationListUiState
- `ui/conversationdetail/` — 3 files: ConversationDetailScreen, ConversationDetailViewModel, ConversationDetailUiState

**3. Update `CustomTaskData.kt`** — align `CustomTaskDataForBuiltinTask` with GoraAI:
- Add: `onNavigateToConversationHistory: () -> Unit = {}`
- Add: `onNavigateToSettings: () -> Unit = {}`
- Add: `loadConversationId: Long? = null`
- Add: `onConversationLoaded: () -> Unit = {}`
- Remove: `setTopBarVisible` and `setCustomNavigateUpCallback` from `CustomTaskData` (GoraAI does not have these)
  - NOTE: TinyGarden uses `setTopBarVisible` — it will stop compiling. Resolution: TinyGarden is NOT in the GoraAI target state, so it will be deleted as part of this refactor.

**4. Delete non-GoraAI custom tasks** — these do not exist in target state:
- `customtasks/tinygarden/` — entire package deleted
- `customtasks/mobileactions/` — entire package deleted

**5. Update `Tasks.kt`** to match GoraAI:
- Add `isBuiltInTask()` function (GoraAI's version, using GoraAI's `allBuiltInTaskIds` set)
- Remove `isLegacyTasks()` and `LLM_MOBILE_ACTIONS`, `LLM_TINY_GARDEN` from `BuiltInTaskId` if they exist in GoraAI's version
- Check GoraAI's exact `Tasks.kt` and copy verbatim

**6. Archive old `HomeScreen.kt` and `ModelManager.kt` entry point**:
- Move `ui/home/HomeScreen.kt` → `ui/home_archived/home/HomeScreen.kt` (matches GoraAI pattern)
- These files are not deleted but are detached from nav and archived

**7. Fix any compilation errors** from removed dependencies

### NOT Included
- Persona system (`persona/` package) — separate spec
- Long context management (`conversation/CompactionManager`, etc.) — separate spec
- Permissions system (`ui/common/permissions/`) — separate spec
- Feature flags (`config/FeatureFlags.kt`) — separate spec
- Image generation task (`ui/imagegeneration/`) — separate spec
- Wiring conversation screens to real Room data beyond what GoraAI provides in the copied files
- Logic changes to copied screen files beyond what GoraAI target contains

## Acceptance Criteria
- [ ] Build compiles without errors
- [ ] `ROUTE_PLACEHOLDER` is `startDestination`; `BackHandler` exits app from there
- [ ] TOS dialog appears on first launch; accepting TOS persists via `TosViewModel`
- [ ] Auto-routing: no model downloaded → `ROUTE_MODEL_SELECTION`
- [ ] Auto-routing: model downloaded → `ROUTE_MODEL/{taskId}/{modelName}`
- [ ] `ROUTE_SETTINGS` and all 4 sub-screens navigable
- [ ] `ROUTE_CONVERSATION_LIST` and `ROUTE_CONVERSATION_DETAIL` reachable from chat
- [ ] TinyGarden and Mobile Actions packages deleted (no lingering references)
- [ ] `CustomTaskDataForBuiltinTask` has all 4 new fields with safe defaults
- [ ] `isBuiltInTask()` aligned with GoraAI's `Tasks.kt`
- [ ] CI GREEN, no regressions on remaining tasks (LLM_CHAT, LLM_ASK_IMAGE, LLM_ASK_AUDIO, LLM_PROMPT_LAB)
- [ ] App installs and launches without crash

## Technical Approach

### Copy Strategy
All files copied verbatim from GoraAI target. Only change: copyright header "Google LLC" → "OnDevice Inc.". If a copied file imports a GoraAI-only dependency, either copy that dependency too or create a minimal stub.

### Nav Flow
```
ROUTE_PLACEHOLDER (blank Text(""), startDestination)
    │
    ├─ TosDialog overlay (mandatory, no dismiss)
    │    └─ onAccept → tosViewModel.acceptTos() → showTosDialog = false
    │
    └─ LaunchedEffect (auto-routing, runs after TOS accepted)
         ├─ model downloaded → navigate("$ROUTE_MODEL/$taskId/$modelName")
         └─ no model       → navigate(ROUTE_MODEL_SELECTION)

ROUTE_MODEL_SELECTION → ROUTE_MODEL (popUpTo PLACEHOLDER inclusive)
ROUTE_MODEL           → ROUTE_CONVERSATION_LIST ↔ ROUTE_CONVERSATION_DETAIL
ROUTE_MODEL           → ROUTE_SETTINGS → sub-screens
```

### TinyGarden / Mobile Actions Deletion
Both packages removed. Their Hilt `@IntoSet` modules are deleted. Since Hilt uses a Set<CustomTask>, removal of these modules simply reduces the set — no other changes needed.

### CustomTaskData Changes
GoraAI removes `setTopBarVisible` and `setCustomNavigateUpCallback` from `CustomTaskData`. Since TinyGarden (the only consumer) is being deleted, these can be safely removed.

## References
- Source of truth: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/navigation/GalleryNavGraph.kt`
- GoraAI screen files: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/settings/`
- GoraAI Tasks: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/data/Tasks.kt`
- GoraAI CustomTaskData: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/customtasks/common/CustomTaskData.kt`
- Previous archived spec: `openspec/archive/room-database-data-layer/` (Room DB prerequisite ✅ complete)
