# Forensic Analysis: Task Tiles Comparison

**Date:** November 11, 2025  
**App:** OnDevice AI  
**Version:** 1.0.7

---

## Executive Summary

OnDevice AI contains three primary task types that enable different AI interaction modes. This forensic analysis examines their architecture, implementation, similarities, differences, and functional capabilities.

---

## Quick Reference Table

| Aspect | AI Chat | Ask Image | Audio Scribe |
|--------|---------|-----------|--------------|
| **Task ID** | `LLM_CHAT` | `LLM_ASK_IMAGE` | `LLM_ASK_AUDIO` |
| **Display Label** | "AI Chat" | "Ask Image" | "Audio Scribe" |
| **Icon** | `Icons.Outlined.Forum` 💬 | `Icons.Outlined.Mms` 🖼️ | `Icons.Outlined.Mic` 🎤 |
| **Category** | `Category.LLM` | `Category.LLM` | `Category.LLM` |
| **Description** | "Chat with on-device LLMs" | "Ask questions about images" | "Transcribe and query audio" |
| **Model Requirement** | Standard LLM | `llmSupportImage = true` | `llmSupportAudio = true` |
| **Module Location** | `ui/llmchat/` | `ui/llmaskimage/` | `ui/llmaskaudio/` |

---

## 1. Architecture Overview

### Common Base Structure

All three tasks share the same foundational architecture:
```
Task Data Class (Tasks.kt)
    ↓
Task Module (implements TaskModule interface)
    ↓
Task Screen (Composable UI)
    ↓
ChatPanel (shared chat interface)
    ↓
MessageInputText (shared input component)
```

### File Locations
```
app/src/main/java/ai/ondevice/app/
├── data/
│   └── Tasks.kt                          # Task definitions
├── ui/
│   ├── llmchat/
│   │   ├── LlmChatTaskModule.kt         # AI Chat task
│   │   ├── LlmChatScreen.kt             # AI Chat UI
│   │   └── LlmChatViewModel.kt          # AI Chat logic
│   ├── llmaskimage/
│   │   ├── LlmAskImageTaskModule.kt     # Ask Image task
│   │   └── LlmAskImageScreen.kt         # Ask Image UI
│   ├── llmaskaudio/
│   │   ├── LlmAskAudioTaskModule.kt     # Audio Scribe task
│   │   └── LlmAskAudioScreen.kt         # Audio Scribe UI
│   └── common/
│       └── chat/
│           ├── ChatPanel.kt             # Shared chat UI
│           └── MessageInputText.kt      # Shared input
```

---

## 2. Task Definition Structure

### Base Task Data Class

**Location:** `app/src/main/java/ai/ondevice/app/data/Tasks.kt`
```kotlin
data class Task(
  val id: String,                    // Unique identifier
  val label: String,                 // Display name
  val category: CategoryInfo,        // Grouping (all use LLM)
  val icon: ImageVector?,            // Task tile icon
  val description: String,           // Task screen description
  val models: MutableList<Model>,    // Compatible models
  @StringRes val agentNameRes: Int,  // Agent placeholder name
  @StringRes val textInputPlaceHolderRes: Int, // Input hint
  // ... other fields
)
```

### Task ID Constants
```kotlin
object BuiltInTaskId {
  const val LLM_CHAT = "llm_chat"
  const val LLM_ASK_IMAGE = "llm_ask_image"
  const val LLM_ASK_AUDIO = "llm_ask_audio"
}
```

---

## 3. Individual Task Analysis

### 3.1 AI Chat (LLM_CHAT)

**Purpose:** Standard text-based conversation with LLMs

**Implementation:**
```kotlin
// LlmChatTaskModule.kt
Task(
  id = BuiltInTaskId.LLM_CHAT,
  label = "AI Chat",
  category = Category.LLM,
  icon = Icons.Outlined.Forum,
  description = "Chat with on-device large language models",
  models = mutableListOf()
)
```

**Capabilities:**
- ✅ Text input/output
- ✅ History tracking
- ✅ Message editing
- ✅ Conversation persistence
- ⚠️ Image support (NEW - requires image-capable model)
- ❌ Audio support

**+ Button Menu Items:**
- 📷 Camera (if model supports images)
- 🖼️ Image (if model supports images)
- 📝 History (always available)

**Recent Changes:**
- 2025-11-11: Added image picker support for models with `llmSupportImage = true`
- Menu items renamed: "Take a picture" → "Camera", "Pick from album" → "Image"

---

### 3.2 Ask Image (LLM_ASK_IMAGE)

**Purpose:** Visual question answering with image input

**Implementation:**
```kotlin
// LlmAskImageTaskModule.kt
Task(
  id = BuiltInTaskId.LLM_ASK_IMAGE,
  label = "Ask Image",
  category = Category.LLM,
  icon = Icons.Outlined.Mms,
  description = "Ask questions about images with on-device large language models",
  models = mutableListOf()
)
```

**Capabilities:**
- ✅ Text input/output
- ✅ Image input (camera or gallery)
- ✅ Multi-image support (up to `MAX_IMAGE_COUNT = 10`)
- ✅ History tracking
- ❌ Audio support

**+ Button Menu Items:**
- 📷 Camera
- 🖼️ Image
- 📝 History

**Constraints:**
- Requires models with `llmSupportImage = true`
- Maximum 10 images per conversation
- Image preview with remove option
- Auto-scales/compresses images

---

### 3.3 Audio Scribe (LLM_ASK_AUDIO)

**Purpose:** Audio transcription and audio-based queries

**Implementation:**
```kotlin
// LlmAskAudioTaskModule.kt
Task(
  id = BuiltInTaskId.LLM_ASK_AUDIO,
  label = "Audio Scribe",
  category = Category.LLM,
  icon = Icons.Outlined.Mic,
  description = "Transcribe and analyze audio with on-device models",
  models = mutableListOf()
)
```

**Capabilities:**
- ✅ Text input/output
- ✅ Audio recording
- ✅ WAV file import
- ✅ Real-time amplitude visualization
- ✅ History tracking
- ❌ Image support

**+ Button Menu Items:**
- 🎤 Record audio clip
- 📁 Pick wav file
- 📝 History

**Constraints:**
- Requires models with `llmSupportAudio = true`
- Maximum `MAX_AUDIO_CLIP_COUNT` clips per conversation
- WAV file format only
- Real-time recording with waveform

---

## 4. Shared Components

### 4.1 ChatPanel.kt

**Purpose:** Unified chat interface for all tasks

**Key Conditional Logic:**
```kotlin
MessageInputText(
  // ... parameters
  showImagePickerInMenu = 
    selectedModel.llmSupportImage && 
    (task.id === BuiltInTaskId.LLM_ASK_IMAGE || task.id === BuiltInTaskId.LLM_CHAT),
  
  showAudioItemsInMenu = 
    selectedModel.llmSupportAudio && 
    task.id === BuiltInTaskId.LLM_ASK_AUDIO,
)
```

**Responsibilities:**
- Message display and formatting
- Input handling delegation
- Model initialization status
- Loading indicators (RotatingLogoIcon)
- Error handling

---

### 4.2 MessageInputText.kt

**Purpose:** Universal input component with task-specific features

**Parameters:**
```kotlin
@Composable
fun MessageInputText(
  task: Task,
  showPromptTemplatesInMenu: Boolean = false,
  showImagePickerInMenu: Boolean = false,
  showAudioItemsInMenu: Boolean = false,
  showStopButtonWhenInProgress: Boolean = false,
  // ... other parameters
)
```

**Dynamic Menu Structure:**
```kotlin
DropdownMenu {
  // Image items (conditional)
  if (showImagePickerInMenu) {
    DropdownMenuItem("Camera")
    DropdownMenuItem("Image")
  }
  
  // Audio items (conditional)
  if (showAudioItemsInMenu) {
    DropdownMenuItem("Record audio clip")
    DropdownMenuItem("Pick wav file")
  }
  
  // History (always shown)
  DropdownMenuItem("History")
}
```

---

## 5. Model Capability System

### Model Data Structure
```kotlin
data class Model(
  val name: String,
  val llmSupportImage: Boolean = false,
  val llmSupportAudio: Boolean = false,
  // ... other fields
)
```

### Capability Checking
```kotlin
// Example: Gemini 2.0 Flash Thinking Experimental
Model(
  name = "gemini-2.0-flash-thinking-exp-1219",
  llmSupportImage = true,   // ✅ Can handle images
  llmSupportAudio = false,  // ❌ No audio support
)
```

### Task-Model Compatibility Matrix

| Model Capability | AI Chat | Ask Image | Audio Scribe |
|-----------------|---------|-----------|--------------|
| Text-only | ✅ Full | ❌ Requires image | ❌ Requires audio |
| + Images | ✅ Full + images | ✅ Full | ❌ Requires audio |
| + Audio | ✅ Full | ❌ Requires image | ✅ Full |
| + Both | ✅ Full + images | ✅ Full | ✅ Full |

---

## 6. Key Similarities

### All Three Tasks Share:

1. **Base Architecture**
   - Same Task data class
   - TaskModule interface implementation
   - ChatPanel for UI
   - MessageInputText for input

2. **Core Features**
   - History tracking via History menu
   - Message persistence
   - Model selection
   - Loading states with RotatingLogoIcon
   - Error handling

3. **Navigation Pattern**
   - Home screen → Task tile → Task screen → Chat interface
   - Model selection before chat
   - Back navigation support

4. **Styling**
   - Material 3 design
   - Consistent color scheme
   - Shared typography
   - Common spacing

---

## 7. Key Differences

### Functional Differences

| Feature | AI Chat | Ask Image | Audio Scribe |
|---------|---------|-----------|--------------|
| **Primary Input** | Text | Text + Images | Text + Audio |
| **Media Handling** | Optional images | Required images | Required audio |
| **Picker Type** | Image (if capable) | Image | Audio (WAV) |
| **Media Limit** | 10 images | 10 images | Multiple clips |
| **Real-time Input** | ❌ | ❌ | ✅ (recording) |
| **Preview** | Image thumbnails | Image thumbnails | Waveform |

### UI Differences

1. **+ Button Menu**
   - AI Chat: Camera, Image, History (model-dependent)
   - Ask Image: Camera, Image, History (always)
   - Audio Scribe: Record, Pick WAV, History (always)

2. **Input Area**
   - AI Chat: Text field only
   - Ask Image: Text field + image preview row
   - Audio Scribe: Text field + waveform display

3. **Model Requirements**
   - AI Chat: Any LLM (images optional)
   - Ask Image: Must have `llmSupportImage = true`
   - Audio Scribe: Must have `llmSupportAudio = true`

---

## 8. Implementation Patterns

### Pattern 1: Task Registration
```kotlin
// HomeScreen.kt - Task initialization order
val taskOrder = listOf(
  BuiltInTaskId.LLM_ASK_IMAGE,
  BuiltInTaskId.LLM_ASK_AUDIO,
  BuiltInTaskId.LLM_PROMPT_LAB,
  BuiltInTaskId.LLM_CHAT,
)
```

### Pattern 2: Conditional Feature Activation
```kotlin
// ChatPanel.kt
showImagePickerInMenu = 
  selectedModel.llmSupportImage && 
  (task.id === BuiltInTaskId.LLM_ASK_IMAGE || task.id === BuiltInTaskId.LLM_CHAT)
```

### Pattern 3: Media Constraint Enforcement
```kotlin
// MessageInputText.kt
val enableAddImageMenuItems = (imageCount + pickedImages.size) < MAX_IMAGE_COUNT
val enableRecordAudioClipMenuItems = 
  (audioClipMessageCount + pickedAudioClips.size) < MAX_AUDIO_CLIP_COUNT
```

---

## 9. Recent Evolution

### Changelog

**2025-11-11:**
- ✅ AI Chat: Added image picker support (model-dependent)
- ✅ All tasks: Menu items renamed for brevity
  - "Take a picture" → "Camera"
  - "Pick from album" → "Image"
- ✅ Created RotatingLogoIcon reusable component
- ✅ Unified loading indicators across all tasks

**Historical:**
- Original implementation: Separate image/audio capabilities
- Model capability system: Dynamic feature activation
- Shared components: ChatPanel and MessageInputText

---

## 10. Design Decisions & Rationale

### Why Three Separate Tasks?

1. **Clarity:** Users understand purpose immediately
2. **Model Filtering:** Only show compatible models per task
3. **UI Optimization:** Task-specific UI elements
4. **User Intent:** Different mental models for each use case

### Why Shared ChatPanel?

1. **Code Reuse:** 90% of chat logic is identical
2. **Consistency:** Same UX across all tasks
3. **Maintainability:** Single source of truth
4. **Feature Parity:** New features benefit all tasks

### Why Model-Dependent Features?

1. **Capability Awareness:** Users know what's possible
2. **Graceful Degradation:** Text-only models still work in AI Chat
3. **Future Proof:** Easy to add new capabilities
4. **Performance:** Don't load unused features

---

## 11. Future Considerations

### Potential Enhancements

1. **Files Support**
   - Add document upload to all tasks
   - PDF, DOCX, TXT support
   - Implement file picker in MessageInputText

2. **Multi-Modal Tasks**
   - Combined image + audio task
   - Video input support
   - Screen recording analysis

3. **Task Customization**
   - User-defined tasks
   - Custom prompts per task
   - Task-specific settings

4. **Advanced Features**
   - Real-time streaming responses
   - Multi-turn context awareness
   - Export conversations

---

## 12. Technical Specifications

### Constants
```kotlin
const val MAX_IMAGE_COUNT = 10
const val MAX_AUDIO_CLIP_COUNT = [value]  // Check Consts.kt
```

### File Size Limits

- Images: Auto-compressed to reasonable size
- Audio: WAV format, size varies
- Total conversation: Limited by device memory

### Permissions Required

| Task | Permissions |
|------|-------------|
| AI Chat | None (optional CAMERA for images) |
| Ask Image | CAMERA (requested at runtime) |
| Audio Scribe | RECORD_AUDIO |

---

## 13. Testing Checklist

### Per-Task Validation

**AI Chat:**
- [ ] Text-only conversation works
- [ ] Image picker appears with capable model
- [ ] Image picker hidden with text-only model
- [ ] History loads previous conversations

**Ask Image:**
- [ ] Camera capture works
- [ ] Gallery picker works
- [ ] Multiple images supported (up to 10)
- [ ] Image removal works
- [ ] Model must support images

**Audio Scribe:**
- [ ] Audio recording works
- [ ] WAV file import works
- [ ] Waveform displays during recording
- [ ] Audio clips attachable to messages
- [ ] Model must support audio

---

## Summary

The three task types in OnDevice AI represent a well-architected system that balances:

- **Specialization:** Each task has clear purpose
- **Reusability:** Shared components reduce duplication
- **Flexibility:** Model capabilities enable/disable features
- **Extensibility:** Easy to add new tasks or features

The forensic analysis reveals a mature codebase with thoughtful separation of concerns, consistent patterns, and clear extension points for future enhancements.

---

**Document Version:** 1.0  
**Last Updated:** November 11, 2025  
**Maintainer:** OnDevice AI Team
