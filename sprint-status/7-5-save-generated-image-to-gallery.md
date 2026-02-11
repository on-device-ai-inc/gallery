# Story 7.5: Save Generated Image to Gallery

Status: drafted

## Story

As a **user**,
I want **to save generated images to my device**,
So that **I can use them elsewhere**.

## Acceptance Criteria

1. **AC1:** Given a generated image is displayed, when I tap the Save button, then the image is saved to the device gallery/Pictures folder

2. **AC2:** Given the image is saved, when the save completes, then I see a confirmation toast: "Image saved to gallery"

3. **AC3:** Given the image is saved, when I check the filename, then it is named: `ondevice_ai_[timestamp].png` (e.g., `ondevice_ai_20251127_143052.png`)

4. **AC4:** Given the image is saved to gallery, when I open the device Photos/Gallery app, then the generated image appears in the gallery

## Tasks / Subtasks

- [ ] Task 1: Implement MediaStore save functionality for Android 10+ (AC: #1, #3, #4)
  - [ ] 1.1: Add MediaStore.Images.Media content values with display name, MIME type, and relative path
  - [ ] 1.2: Insert content via ContentResolver to get output URI
  - [ ] 1.3: Write bitmap to output stream using bitmap.compress(PNG, 100, outputStream)
  - [ ] 1.4: Close output stream and update MediaStore entry
  - [ ] 1.5: Test save operation on Android 10+ devices (API 29+)

- [ ] Task 2: Add legacy storage support for Android 9 and below (AC: #1, #3, #4)
  - [ ] 2.1: Check Android version - use MediaStore API for Q+ (API 29+), legacy for older
  - [ ] 2.2: For legacy: Add WRITE_EXTERNAL_STORAGE permission to AndroidManifest.xml
  - [ ] 2.3: Request runtime permission for API 23-28 if not granted
  - [ ] 2.4: Save directly to Environment.DIRECTORY_PICTURES using File API
  - [ ] 2.5: Broadcast MediaScannerConnection.scanFile to update gallery
  - [ ] 2.6: Test on Android 9 (API 28) device/emulator

- [ ] Task 3: Add Save button UI to image display (AC: #1)
  - [ ] 3.1: Modify ImageGenerationScreen.kt FinalImageDisplay composable
  - [ ] 3.2: Add IconButton with Icons.Default.SaveAlt below "Generate Again" button
  - [ ] 3.3: Add "Save to Gallery" text label for clarity
  - [ ] 3.4: Call saveImage() function on button click
  - [ ] 3.5: Disable button if save is in progress (prevent duplicate saves)

- [ ] Task 4: Generate filename with timestamp (AC: #3)
  - [ ] 4.1: Create filename generation function: `generateImageFilename()`
  - [ ] 4.2: Use SimpleDateFormat("yyyyMMdd_HHmmss") for timestamp
  - [ ] 4.3: Format: "ondevice_ai_${timestamp}.png"
  - [ ] 4.4: Ensure filename is unique (timestamp ensures uniqueness)

- [ ] Task 5: Show save confirmation toast (AC: #2)
  - [ ] 5.1: Add success callback to save function
  - [ ] 5.2: Display Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT)
  - [ ] 5.3: Add error handling with "Failed to save image" toast on error
  - [ ] 5.4: Test toast appears after successful save

- [ ] Task 6: Update ViewModel for save state management (AC: #1, #2)
  - [ ] 6.1: Add `isSaving: Boolean` to ImageGenerationUiState
  - [ ] 6.2: Add `saveImage()` function to ImageGenerationViewModel
  - [ ] 6.3: Update state to isSaving=true during save operation
  - [ ] 6.4: Update state to isSaving=false on completion/error
  - [ ] 6.5: Expose save status to UI for button disable state

- [ ] Task 7: Integration testing and validation (AC: #1-4)
  - [ ] 7.1: Test save on Android 10+ device (MediaStore path)
  - [ ] 7.2: Test save on Android 9 device (legacy path with permission)
  - [ ] 7.3: Verify image appears in Photos/Gallery app after save
  - [ ] 7.4: Verify filename format matches ondevice_ai_[timestamp].png
  - [ ] 7.5: Verify confirmation toast displays
  - [ ] 7.6: Test permission denial scenario (user denies permission)
  - [ ] 7.7: Test error scenarios (no storage space, write failure)

## Dev Notes

### Learnings from Previous Story

**From Story 7-4-image-generation-screen-ui (Status: review)**

- **Screen Structure Created**: `ImageGenerationScreen.kt` provides complete UI with final image display
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt`
  - `FinalImageDisplay` composable (lines 262-316) shows generated image and "Generate Again" button
  - This is where the Save button should be added - extend FinalImageDisplay with save functionality
  - Current display shows image in Card with aspectRatio(1f) for 512x512 sizing

- **ViewModel Integration Ready**: `ImageGenerationViewModel` manages generation state
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt`
  - Exposes `StateFlow<ImageGenerationUiState>` with finalBitmap available
  - Add `saveImage(context: Context, bitmap: Bitmap)` function to ViewModel
  - Follow existing pattern: viewModelScope.launch for async operations

- **UI State Model**: `ImageGenerationUiState` holds current state
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationUiState.kt`
  - Add `isSaving: Boolean` field to track save operation state
  - Use for disabling Save button during save operation

- **Bitmap Available**: Final generated bitmap accessible via `uiState.finalBitmap`
  - Ready to compress and save as PNG
  - Use `bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)`

[Source: sprint status/7-4-image-generation-screen-ui.md#Completion-Notes-List]

### Technical Context

**Android Storage Best Practices:**
- **Android 10+ (API 29+)**: Use MediaStore API (scoped storage)
  - No permissions needed for saving to Pictures directory
  - Use `MediaStore.Images.Media.getContentUri("external")`
  - Set `MediaStore.Images.Media.DISPLAY_NAME`, `MIME_TYPE`, `RELATIVE_PATH`

- **Android 9 and below (API 28-)**: Use legacy File API
  - Requires `WRITE_EXTERNAL_STORAGE` permission in manifest
  - Request runtime permission for API 23+
  - Use `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)`
  - Notify media scanner: `MediaScannerConnection.scanFile()` to update gallery

**Save Function Pattern:**
```kotlin
// In ImageGenerationViewModel
fun saveImage(context: Context, bitmap: Bitmap) {
    viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true) }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageMediaStore(context, bitmap)
            } else {
                saveImageLegacy(context, bitmap)
            }
            // Success toast handled by caller
        } catch (e: Exception) {
            Log.e("ImageGeneration", "Failed to save image", e)
            // Error toast handled by caller
        } finally {
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
```

**Filename Generation:**
```kotlin
fun generateImageFilename(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(Date())
    return "ondevice_ai_$timestamp.png"
}
```

**Permission Handling:**
- Add to AndroidManifest.xml: `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />`
- Request at runtime for API 23-28:
  ```kotlin
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      // Request WRITE_EXTERNAL_STORAGE permission
  }
  ```

**Gallery Integration:**
- MediaStore approach (Android 10+) automatically updates gallery
- Legacy approach requires: `MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)`

### Project Structure Notes

**Files to Modify:**
- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt`
  - Update `FinalImageDisplay` composable to add Save button
  - Add `onSave: () -> Unit` callback parameter

- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt`
  - Add `saveImage(context: Context, bitmap: Bitmap)` function
  - Add helper functions: `saveImageMediaStore()`, `saveImageLegacy()`, `generateImageFilename()`

- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationUiState.kt`
  - Add `isSaving: Boolean = false` field

**Files to Create (if needed):**
- None - all functionality fits in existing files from Story 7.4

**Existing Patterns to Reference:**
- Async operations: `ImageGenerationViewModel.startGeneration()` (Story 7.3) - follow viewModelScope.launch pattern
- File operations: `DownloadWorker.kt` may have file handling examples
- Permissions: Check if app has permission handling patterns elsewhere

### References

- [Epic 7 Story 7.5](docs/epics.md#Story-75-Save-Generated-Image-to-Gallery)
- [Previous Story: 7.4 Image Generation Screen UI](sprint status/7-4-image-generation-screen-ui.md)
- [Architecture: Project Structure](docs/architecture.md#Project-Structure)
- [Android MediaStore Guide](https://developer.android.com/training/data-storage/shared/media)
- [Android Scoped Storage](https://developer.android.com/about/versions/11/privacy/storage)

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2025-11-27 | Story created from Epic 7 breakdown | Claude (create-story workflow) |
