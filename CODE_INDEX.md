# CODE_INDEX.md - Capability Tracking

> **Check this before creating new code to avoid duplication!**

---

## Data Layer

### Room Database (Conversation History)
| File | Purpose | Location |
|------|---------|----------|
| `AppDatabase` | Room DB v1, 3 entities | `data/AppDatabase.kt` |
| `ConversationThread` | Entity: thread metadata | `data/ConversationThread.kt` |
| `ConversationMessage` | Entity: individual messages | `data/ConversationMessage.kt` |
| `ConversationState` | Entity: compaction state | `data/ConversationState.kt` |
| `ConversationDao` | DAO: all CRUD + Flow + search | `data/ConversationDao.kt` |
| `DatabaseMigrations` | `ALL_MIGRATIONS` array (empty v1) | `data/DatabaseMigrations.kt` |

### Repositories
| Repository | Purpose | Location |
|------------|---------|----------|
| `DataStoreRepository` | Proto DataStore (settings/user data) | `data/DataStoreRepository.kt` |
| `DownloadRepository` | Model download/management | `data/DownloadRepository.kt` |

### DI
| Module | Purpose | Location |
|--------|---------|----------|
| `AppModule` | Provides DB, DAO, DataStores, lifecycle | `di/AppModule.kt` |

---

## Domain Layer

### Use Cases
| Use Case | Purpose | Location |
|----------|---------|----------|
| `DownloadModelUseCase` | Download ML model | `domain/usecase/` |
| `RunInferenceUseCase` | Run model inference | `domain/usecase/` |
| `GetModelsUseCase` | List available models | `domain/usecase/` |

### Inference
| Component | Purpose | Location |
|-----------|---------|----------|
| `InferenceEngine` | Core inference | `domain/inference/` |
| `ModelLoader` | Load models | `domain/inference/` |

---

## UI Layer

### Screens
| Screen | Purpose | Location |
|--------|---------|----------|
| `MainActivity` | Entry point | `ui/` |
| `LlmChatScreen` | Unified chat (text + image + audio) | `ui/llmchat/` |
| `ModelSelectionScreen` | First-launch model selection | `ui/modelselection/` |
| `SettingsScreen` | Settings hub | `ui/settings/` |
| `CustomInstructionsScreen` | System prompt editor | `ui/settings/` |
| `ModelParametersScreen` | Temp/topK/topP sliders | `ui/settings/` |
| `PrivacyCenterScreen` | Privacy controls | `ui/settings/` |
| `StorageManagementScreen` | Storage management | `ui/settings/` |
| `ConversationListScreen` | Past conversations list | `ui/conversationlist/` |
| `ConversationDetailScreen` | Single conversation view | `ui/conversationdetail/` |

### ViewModels
| ViewModel | Purpose | Location |
|-----------|---------|----------|
| `LlmChatViewModel` | Chat + inference state | `ui/llmchat/` |
| `SettingsViewModel` | Settings state | `ui/settings/` |
| `ConversationListViewModel` | Conversation list state | `ui/conversationlist/` |
| `ConversationDetailViewModel` | Conversation detail state | `ui/conversationdetail/` |

### Components
| Component | Purpose | Location |
|-----------|---------|----------|
| `ChatBubble` | Message display | `ui/components/` |
| `ModelCard` | Model list item | `ui/components/` |
| `DownloadProgress` | Download indicator | `ui/components/` |

---

## Utilities

### Extensions
| Extension | Purpose | Location |
|-----------|---------|----------|
| `StringExt` | String helpers | `util/` |
| `ContextExt` | Context helpers | `util/` |
| `FlowExt` | Flow helpers | `util/` |

### Helpers
| Helper | Purpose | Location |
|--------|---------|----------|
| `NetworkHelper` | Connectivity | `util/` |
| `StorageHelper` | File operations | `util/` |
| `PermissionHelper` | Permissions | `util/` |

---

## Recently Added

| Date | Component | Description |
|------|-----------|-------------|
| 2026-02-17 | Room DB layer | `AppDatabase`, 3 entities, `ConversationDao`, `DatabaseMigrations`: `data/` |
| 2026-02-17 | `ChatDisclaimerRow` | Disclaimer row after AI messages: `ui/common/chat/ChatDisclaimerRow.kt` |
| 2026-02-17 | `ErrorDialog` | Error dialog with support contact: `ui/common/ErrorDialog.kt` |
| 2026-02-17 | Legal assets | Privacy/Terms HTML: `assets/legal/privacy.html`, `assets/legal/terms.html` |
| 2026-02-17 | Navigation refactor | 10-route GoraAI nav graph; settings/, modelselection/, conversationlist/, conversationdetail/ |
| 2026-02-17 | `SettingsScreen` (+ 8 sub-screens) | Settings hub + CustomInstructions, ModelParams, Privacy, Storage: `ui/settings/` |
| 2026-02-17 | `ModelSelectionScreen` | First-launch model selection: `ui/modelselection/` |
| 2026-02-17 | `ConversationListScreen` + VM + State | Conversation history list: `ui/conversationlist/` |
| 2026-02-17 | `ConversationDetailScreen` + VM + State | Single conversation view: `ui/conversationdetail/` |

---

## Deprecated

| Component | Reason | Replacement |
|-----------|--------|-------------|
| `ui/home/HomeScreen.kt` | GoraAI chat-first flow removes task picker | Archived to `ui/home_archived/` |
| `customtasks/tinygarden/` | Not in GoraAI target state | Removed |
| `customtasks/mobileactions/` | Not in GoraAI target state | Removed |
| `BuiltInTaskId.LLM_TINY_GARDEN` | Removed with TinyGarden | N/A |
| `BuiltInTaskId.LLM_MOBILE_ACTIONS` | Removed with MobileActions | N/A |
| `isLegacyTasks()` | Replaced by GoraAI `isBuiltInTask()` | `isBuiltInTask()` in `data/Tasks.kt` |

---

## How to Update

When adding new code:
1. Add entry to appropriate section
2. Include location
3. Add to "Recently Added" with date

When removing/replacing:
1. Move to "Deprecated" section
2. Note replacement
