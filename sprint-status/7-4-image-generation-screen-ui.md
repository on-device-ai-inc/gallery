# Story 7.4: Image Generation Screen UI

Status: review

## Story

As a **user**,
I want **a screen to enter prompts and see generated images**,
So that **I can create images from text**.

## Acceptance Criteria

1. **AC1:** Given I tap the Image Generation tile on home screen, when the screen loads, then I see a prompt input field and Generate button

2. **AC2:** Given I am on the image generation screen, when I enter a prompt (multi-line, max 500 chars), then I can adjust iterations via slider (5-50, default 20)

3. **AC3:** Given I have entered a prompt and tap Generate, when generation starts, then I see the progress display from Story 7.3 (step count, intermediate previews, cancel button, privacy indicator)

4. **AC4:** Given generation completes successfully, when the final image is ready, then it displays at 512x512 resolution with "Generate Again" button

5. **AC5:** Given I tap "Generate Again", when regeneration starts, then a new seed is used (produces different result from same prompt)

6. **AC6:** Given I am a first-time user without the model downloaded, when I open the screen, then I see: "Generate images from text, completely offline. Download the 1.9GB model to get started."

## Tasks / Subtasks

- [x] Task 1: Create ImageGenerationScreen composable (AC: #1, #2)
  - [x] 1.1: Create `ImageGenerationScreen.kt` in `ui/imagegeneration/`
  - [x] 1.2: Add Scaffold with TopAppBar (title: "Image Generation", back button)
  - [x] 1.3: Add multi-line TextField for prompt input (maxLines=5, maxLength=500 chars)
  - [x] 1.4: Add Slider for iterations (5-50 range, default 20, show current value)
  - [x] 1.5: Add "Generate" Button that calls viewModel.startGeneration()
  - [x] 1.6: Display model download prompt if model not available (AC6)

- [x] Task 2: Integrate progress display from Story 7.3 (AC: #3)
  - [x] 2.1: Use existing ImageGenerationViewModel from Story 7.3
  - [x] 2.2: Collect uiState.collectAsState() to observe generation progress
  - [x] 2.3: Show ImageGenerationProgressDisplay when isGenerating=true
  - [x] 2.4: Hide input fields during generation (disable interaction)

- [x] Task 3: Display final generated image (AC: #4)
  - [x] 3.1: Show final bitmap when uiState.finalBitmap != null
  - [x] 3.2: Display image in Card at 512x512 (use Modifier.aspectRatio(1f))
  - [x] 3.3: Add "Generate Again" button below image
  - [x] 3.4: Generate Again calls viewModel.clearResult() then startGeneration() with new random seed

- [x] Task 4: Model availability check and error handling (AC: #6)
  - [x] 4.1: Check if Stable Diffusion model is downloaded using modelManagerViewModel
  - [x] 4.2: Show informative message if model not available
  - [x] 4.3: Provide link/button to navigate to Model Manager
  - [x] 4.4: Handle error states from viewModel (show errorMessage in Snackbar or Alert)

- [x] Task 5: Navigation integration (AC: #1)
  - [x] 5.1: Verify ImageGenerationTask is registered in ImageGenerationTaskModule (done in Story 7.1)
  - [x] 5.2: Update MainScreen composable to route to ImageGenerationScreen (replace placeholder)
  - [x] 5.3: Pass modelManagerViewModel and navigateUp callback
  - [x] 5.4: Test navigation from task tile to screen

- [x] Task 6: Integration testing and validation (AC: #1-6)
  - [x] 6.1: Test full flow: Open screen → Enter prompt → Generate → View result
  - [x] 6.2: Test iteration slider (verify different iteration counts work)
  - [x] 6.3: Test "Generate Again" produces different images
  - [x] 6.4: Test model not downloaded scenario
  - [x] 6.5: Test cancellation during generation
  - [x] 6.6: Test error scenarios (empty prompt, invalid inputs)

## Dev Notes

### Learnings from Previous Story

**From Story 7-3-progress-display-and-cancel (Status: review)**

- **ImageGenerationViewModel Created**: Provides state management and generation control
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt`
  - `startGeneration(context, modelPath, prompt, iterations, seed)` - Launches generation
  - `cancelGeneration()` - Cancels ongoing generation
  - `clearResult()` - Clears final bitmap for new generation
  - Exposes `StateFlow<ImageGenerationUiState>` for UI observation

- **ImageGenerationProgressDisplay Created**: Ready-to-use progress UI component
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationProgressDisplay.kt`
  - Shows: Progress text, LinearProgressIndicator, intermediate previews, cancel button, privacy indicator
  - Usage: Pass currentStep, totalSteps, intermediateBitmap, onCancel callback
  - **Story 7.4 should reuse this component** - just wrap in conditional `if (uiState.isGenerating)`

- **ImageGenerationUiState Available**: Complete state model for UI
  - Fields: isGenerating, currentStep, totalSteps, intermediateBitmap, finalBitmap, errorMessage, cancelled
  - Collect from viewModel.uiState.collectAsState() in composable

- **ImageGenerationHelper Enhanced**: Emits progress during generation (from Story 7.2-7.3)
  - Returns `Flow<GenerationResult>` with Progress, Success, Error variants
  - ViewModel handles flow collection - screen just observes uiState

- **Integration Notes**:
  - Story 7.3 provides complete ViewModel and progress display
  - Story 7.4 adds: Prompt input, iteration slider, final image display, "Generate Again"
  - Story 7.5 will add Save button, Story 7.6 will add Share button

[Source: sprint status/7-3-progress-display-and-cancel.md#Dev-Agent-Record]

### Technical Context

**Screen Structure:**
- Follow existing task screen patterns (LlmChatScreen, LlmAskImageScreen)
- Scaffold with TopAppBar (back navigation)
- Column layout: Prompt input → Iteration slider → Generate button → Progress/Result display
- Conditional rendering: Input controls vs Progress display vs Final result

**Prompt Input:**
- Use `OutlinedTextField` with `maxLines = 5` for multi-line support
- Character counter: Show "X/500" below field
- Validate: Prompt must not be blank before allowing generation
- Placeholder text: "Describe the image you want to generate..."

**Iteration Slider:**
- Use `Slider` component with `valueRange = 5f..50f`, `steps = 44` (45 discrete values)
- Default value: 20 iterations
- Display current value: "Iterations: X" label above slider
- Tooltip: "More iterations = higher quality, longer generation time"

**Model Path Resolution:**
- Get model from `modelManagerViewModel.selectedModel` or default IMAGE_GENERATION model
- Call `model.getPath(context)` to get path to sd15/ directory
- Pass to `viewModel.startGeneration(context, modelPath, prompt, iterations, seed)`

**Seed Generation:**
- Use `kotlin.random.Random.nextInt()` for random seed
- Store current seed in ViewModel or local state
- "Generate Again": Generate new random seed, call startGeneration with same prompt/iterations

**State Management Flow:**
1. User enters prompt + iterations
2. Tap Generate → viewModel.startGeneration() → isGenerating=true
3. Show ImageGenerationProgressDisplay (replaces input controls)
4. Generation completes → finalBitmap available, isGenerating=false
5. Show final image + "Generate Again" button
6. "Generate Again" → clearResult() → startGeneration() with new seed

### Project Structure Notes

**Files to Create:**
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt` - Main screen composable

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationTaskModule.kt` - Update MainScreen to use ImageGenerationScreen instead of placeholder

**Existing Components to Reference:**
- `app/src/main/java/ai/ondevice/app/ui/llmchat/LlmChatScreen.kt` - Example of task screen with input and result display
- `app/src/main/java/ai/ondevice/app/ui/common/chat/MessageInputText.kt` - Example of multi-line text input
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt` - ViewModel from Story 7.3
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationProgressDisplay.kt` - Progress UI from Story 7.3

### References

- [Epic 7 Story 7.4](docs/epics.md#Story-74-Image-Generation-Screen-UI)
- [Previous Story: 7.3 Progress Display & Cancel](sprint status/7-3-progress-display-and-cancel.md)
- [Architecture: Task Screen Pattern](docs/architecture.md#Project-Structure)
- [ImageGenerationViewModel](app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt)
- [ImageGenerationProgressDisplay](app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationProgressDisplay.kt)

## Dev Agent Record

### Context Reference

- `sprint status/7-4-image-generation-screen-ui.context.xml`

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

**Implementation Summary:**

1. **Created ImageGenerationScreen.kt** - Complete image generation UI with all acceptance criteria
   - Scaffold with TopAppBar and back navigation (AC1)
   - Multi-line prompt TextField with 500 character limit and counter display (AC2)
   - Iteration Slider (5-50 range, default 20) with current value label (AC2)
   - Generate button disabled when prompt blank or model not downloaded (AC1, AC6)
   - Model availability check with first-time user message (AC6)
   - Progress display integration showing ImageGenerationProgressDisplay when isGenerating=true (AC3)
   - Final image display with "Generate Again" button using new random seed (AC4, AC5)
   - FinalImageDisplay composable showing 512x512 image with aspectRatio(1f)
   - Error handling via Snackbar with dismiss action
   - Helpful tips card with guidance for better prompts

2. **Updated ImageGenerationTaskModule.kt** - Navigation integration
   - Modified MainScreen composable to use ImageGenerationScreen instead of placeholder
   - Removed TODO comment from Story 7.4
   - Passes modelManagerViewModel and navigateUp callback correctly

**All Acceptance Criteria Satisfied:**
- ✅ AC1: Prompt input field and Generate button visible on screen load
- ✅ AC2: Multi-line prompt (500 char limit with counter) + iteration slider (5-50, default 20)
- ✅ AC3: Progress display from Story 7.3 shown when generation starts
- ✅ AC4: Final image displays at 512x512 with "Generate Again" button
- ✅ AC5: "Generate Again" uses new random seed (kotlin.random.Random.nextInt())
- ✅ AC6: First-time user message shown when model not downloaded

**Technical Implementation Details:**
- State management: Collects viewModel.uiState.collectAsState() for reactive updates
- Model path resolution: Uses model.getPath(context) from MODELS_IMAGE_GENERATION.first()
- Model availability: Checks File(modelPath).exists() with remember() for caching
- Conditional rendering: Three-state UI (input controls | progress display | final result)
- Seed generation: kotlin.random.Random.nextInt() for random seeds
- Image display: Card with Image composable, aspectRatio(1f) for 512x512, ContentScale.Fit
- Error handling: Snackbar with errorMessage from uiState, dismiss action
- Privacy indicator: Integrated via ImageGenerationProgressDisplay from Story 7.3

**Integration with Previous Stories:**
- Story 7.3 components fully integrated: ImageGenerationViewModel, ImageGenerationProgressDisplay, ImageGenerationUiState
- Story 7.2 inference wrapper used via ViewModel's startGeneration() method
- Story 7.1 task registration and model setup leveraged

### File List

**Created:**
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt` (317 lines)

**Modified:**
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationTaskModule.kt` (lines 84-91: Replaced ImageGenerationPlaceholderScreen with ImageGenerationScreen)

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2025-11-27 | Story created from Epic 7 breakdown | Claude (create-story workflow) |
