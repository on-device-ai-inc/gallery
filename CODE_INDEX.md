# CODE_INDEX.md - Capability Tracking

> **Check this before creating new code to avoid duplication!**

---

## Data Layer

### Repositories
| Repository | Purpose | Location |
|------------|---------|----------|
| `ModelRepository` | Model download/management | `data/repository/` |
| `ChatRepository` | Chat history | `data/repository/` |
| `SettingsRepository` | User preferences | `data/repository/` |

### Data Sources
| Source | Purpose | Location |
|--------|---------|----------|
| `LocalModelDataSource` | Local model storage | `data/local/` |
| `RemoteModelDataSource` | Model download | `data/remote/` |

---

## Conversation Management

### Context Compression
| Component | Purpose | Location |
|-----------|---------|----------|
| `TokenEstimator` | Estimate token count from text | `conversation/` |
| `CompactionManager` | Orchestrate conversation compaction | `conversation/` |
| `ContextBuilder` | Build inference context with summary | `conversation/` |
| `SummarizationPrompts` | LangChain-style progressive summarization | `conversation/` |

### State Management
| Component | Purpose | Location |
|-----------|---------|----------|
| `ConversationState` | Room entity for summary state | `data/` |
| `CompactionResult` | Sealed class for compaction outcomes | `conversation/` |

**Pattern:** LangChain ConversationSummaryBufferMemory
**Trigger:** 75% of context limit (3,072 tokens for 4,096 limit)
**Target:** 40% after compaction (1,638 tokens)
**Summarization:** Self-summarization using existing LLM

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
| `ChatScreen` | Chat interface | `ui/chat/` |
| `ModelSelectionScreen` | Browse models | `ui/models/` |
| `SettingsScreen` | Settings | `ui/settings/` |

### ViewModels
| ViewModel | Purpose | Location |
|-----------|---------|----------|
| `ChatViewModel` | Chat state | `ui/chat/` |
| `ModelViewModel` | Model state | `ui/models/` |
| `SettingsViewModel` | Settings state | `ui/settings/` |

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
| 2026-02-04 | `conversation/` package | Context compression with self-summarization (infinite conversation) |
| 2026-02-04 | `TokenEstimator` | chars/4 heuristic for token counting |
| 2026-02-04 | `CompactionManager` | Async LLM-based conversation summarization |
| 2026-02-04 | `ContextBuilder` | Inject summary into inference context |
| 2026-02-04 | `ConversationState` | Room entity for summary persistence |
| 2026-02-04 | Database v8 | Added conversation_state table (MIGRATION_7_8) |

---

## Deprecated

| Component | Reason | Replacement |
|-----------|--------|-------------|
| | | |

---

## How to Update

When adding new code:
1. Add entry to appropriate section
2. Include location
3. Add to "Recently Added" with date

When removing/replacing:
1. Move to "Deprecated" section
2. Note replacement
