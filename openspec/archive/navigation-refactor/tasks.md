# Tasks: navigation-refactor

## Implementation Tasks

- [x] **Task 1**: Delete TinyGarden package
  - Delete: `Android/src/app/src/main/java/ai/ondevice/app/customtasks/tinygarden/` (all files)
  - Acceptance: package no longer exists; no lingering imports elsewhere

- [x] **Task 2**: Delete Mobile Actions package
  - Delete: `Android/src/app/src/main/java/ai/ondevice/app/customtasks/mobileactions/` (all files)
  - Acceptance: package no longer exists; no lingering imports elsewhere

- [x] **Task 3**: Update `Tasks.kt` to match GoraAI
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/data/Tasks.kt`
  - Copy verbatim (remove LLM_MOBILE_ACTIONS, LLM_TINY_GARDEN from BuiltInTaskId if present)
  - Add `isBuiltInTask()` as per GoraAI's version
  - Remove `isLegacyTasks()` if GoraAI removes it
  - Fix copyright header
  - Acceptance: `isBuiltInTask()` present, BuiltInTaskId matches GoraAI exactly

- [x] **Task 4**: Update `CustomTaskData.kt` to match GoraAI
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/customtasks/common/CustomTaskData.kt`
  - Copy verbatim (adds 4 new fields to `CustomTaskDataForBuiltinTask`, removes `setTopBarVisible`/`setCustomNavigateUpCallback` from `CustomTaskData`)
  - Fix copyright header
  - Acceptance: matches GoraAI's data class signatures exactly

- [x] **Task 5**: Archive old HomeScreen
  - Create dir: `Android/src/app/src/main/java/ai/ondevice/app/ui/home_archived/`
  - Move `ui/home/` → `ui/home_archived/home/` (matches GoraAI structure)
  - Acceptance: `ui/home_archived/home/HomeScreen.kt` exists; old `ui/home/` is gone

- [x] **Task 6**: Copy `ui/settings/` screen files (9 files)
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/settings/`
  - Dest: `Android/src/app/src/main/java/ai/ondevice/app/ui/settings/`
  - Files: SettingsScreen.kt, CustomInstructionsScreen.kt, PrivacyCenterScreen.kt, StorageManagementScreen.kt, ModelParametersScreen.kt, SettingsViewModel.kt, AboutSection.kt, LicenseViewerScreen.kt, ModelParameterPresets.kt
  - Fix copyright header in each file
  - Acceptance: all 9 files present with correct package declaration

- [x] **Task 7**: Copy `ui/modelselection/ModelSelectionScreen.kt`
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/modelselection/ModelSelectionScreen.kt`
  - Dest: `Android/src/app/src/main/java/ai/ondevice/app/ui/modelselection/ModelSelectionScreen.kt`
  - Fix copyright header
  - Acceptance: file present with correct package

- [x] **Task 8**: Copy `ui/conversationlist/` files (3 files)
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/conversationlist/`
  - Dest: `Android/src/app/src/main/java/ai/ondevice/app/ui/conversationlist/`
  - Files: ConversationListScreen.kt, ConversationListViewModel.kt, ConversationListUiState.kt
  - Fix copyright header
  - Acceptance: all 3 files present

- [x] **Task 9**: Copy `ui/conversationdetail/` files (3 files)
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/conversationdetail/`
  - Dest: `Android/src/app/src/main/java/ai/ondevice/app/ui/conversationdetail/`
  - Files: ConversationDetailScreen.kt, ConversationDetailViewModel.kt, ConversationDetailUiState.kt
  - Fix copyright header
  - Acceptance: all 3 files present

- [x] **Task 10**: Replace `GalleryNavGraph.kt` with GoraAI's version
  - Source: `/tmp/goraai-target/app/src/main/java/ai/ondevice/app/ui/navigation/GalleryNavGraph.kt`
  - Dest: `Android/src/app/src/main/java/ai/ondevice/app/ui/navigation/GalleryNavGraph.kt`
  - Fix copyright header
  - Acceptance: `startDestination = ROUTE_PLACEHOLDER`, 10 routes defined, TOS dialog present

- [x] **Task 11**: Fix compilation errors
  - After Tasks 1–10, check what fails to compile
  - Common issues: missing dependencies in copied files, removed symbols still referenced
  - Fix each error: copy missing file from GoraAI or create minimal stub
  - Acceptance: project compiles (gradle check passes or CI is green)

- [x] **Task 12**: Update `CODE_INDEX.md`
  - Remove: TinyGarden and Mobile Actions entries
  - Add: settings/, modelselection/, conversationlist/, conversationdetail/ entries
  - Update Recently Added table with today's date
  - Acceptance: CODE_INDEX.md accurate

## Testing Tasks

- [x] CI GREEN — both `Build APK/AAB` and `Android CI` workflows pass
- [x] App installs on device without crash
- [x] App launches (confirm via `adb shell dumpsys window | grep mCurrentFocus`)

## Documentation Tasks

- [x] Update `LESSONS_LEARNED.md` with any discoveries during implementation
