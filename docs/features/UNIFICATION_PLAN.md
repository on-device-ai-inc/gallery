# Unification Plan: Single AI Chat Experience

## Vision
Transform from 3 separate tasks → 1 unified AI Chat that adapts to model capabilities

## Current State
```
Home Screen
├── AI Chat (text only, optional images)
├── Ask Image (images required)
└── Audio Scribe (audio required)
```

## Target State
```
Home Screen
└── AI Chat (text + images + audio based on model)
```

## Changes Required

### 1. Task Registration (HomeScreen.kt)
- Remove Ask Image and Audio Scribe from task list
- Keep only AI Chat
- Update auto-navigation logic

### 2. ChatPanel.kt
- Enable image picker for ALL image-capable models
- Enable audio for ALL audio-capable models
- Remove task.id checks, use only model capabilities

### 3. MessageInputText.kt
- Show Camera + Image when model supports images
- Show Record + WAV when model supports audio
- Show Files (future)
- Always show History

### 4. UI Updates
- AI Chat becomes "the" chat
- Model picker shows all model types
- Description updated to mention multi-modal

## Benefits
✅ Simpler user experience (one entry point)
✅ No task switching needed
✅ Model capabilities determine features
✅ Future-proof for multi-modal models
✅ Cleaner architecture

## Risks
⚠️ Users might not realize image/audio available
⚠️ Need clear UI hints about capabilities
⚠️ Backward compatibility with saved conversations

## Migration Strategy
1. Update task registration (hide other tasks)
2. Update capability logic (remove task checks)
3. Update UI text/descriptions
4. Test with different model types
5. Update documentation

