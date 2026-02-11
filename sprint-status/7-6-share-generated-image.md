# Story 7.6: Share Generated Image

Status: drafted

## Story

As a **user**,
I want **to share generated images**,
So that **I can send them to others**.

## Acceptance Criteria

1. **AC1:** Given a generated image is displayed, when I tap the Share button, then the system share sheet opens with the image

2. **AC2:** Given the share sheet is open, when I select an app (WhatsApp, Telegram, email, etc.), then I can share the image via that app

3. **AC3:** Given the image is shared, when viewing the shared image, then it includes metadata: "Generated with OnDevice AI"

## Tasks / Subtasks

- [ ] Task 1: Implement FileProvider for secure URI sharing (AC: #1, #2)
  - [ ] 1.1: Add FileProvider declaration to AndroidManifest.xml
  - [ ] 1.2: Create file_paths.xml in res/xml/ to define cache directory for shared images
  - [ ] 1.3: Configure FileProvider with authority: "ai.ondevice.app.fileprovider"
  - [ ] 1.4: Test FileProvider grants read access to external apps

- [ ] Task 2: Create share intent with image (AC: #1, #2)
  - [ ] 2.1: Save bitmap to temporary file in cache directory
  - [ ] 2.2: Generate content URI using FileProvider.getUriForFile()
  - [ ] 2.3: Create Intent with ACTION_SEND and type "image/png"
  - [ ] 2.4: Add URI with Intent.EXTRA_STREAM
  - [ ] 2.5: Grant read URI permission with Intent.FLAG_GRANT_READ_URI_PERMISSION
  - [ ] 2.6: Start share chooser with Intent.createChooser()

- [ ] Task 3: Add metadata to shared image (AC: #3)
  - [ ] 3.1: Add Intent.EXTRA_TEXT with "Generated with OnDevice AI"
  - [ ] 3.2: Alternatively, embed metadata in PNG file using bitmap metadata API
  - [ ] 3.3: Test metadata appears when sharing via different apps
  - [ ] 3.4: Verify metadata is readable in receiving apps

- [ ] Task 4: Add Share button UI to image display (AC: #1)
  - [ ] 4.1: Modify ImageGenerationScreen.kt FinalImageDisplay composable
  - [ ] 4.2: Add IconButton with Icons.Default.Share next to Save button
  - [ ] 4.3: Add "Share" text label for clarity
  - [ ] 4.4: Call shareImage() function on button click
  - [ ] 4.5: Arrange Save and Share buttons in Row for horizontal layout

- [ ] Task 5: Update ViewModel for share functionality (AC: #1, #2, #3)
  - [ ] 5.1: Add `shareImage(context: Context, bitmap: Bitmap)` function to ImageGenerationViewModel
  - [ ] 5.2: Implement temporary file creation in cache directory
  - [ ] 5.3: Implement FileProvider URI generation
  - [ ] 5.4: Implement share intent creation and launch
  - [ ] 5.5: Add error handling for share failures

- [ ] Task 6: Clean up temporary shared files (AC: #2)
  - [ ] 6.1: Add cache cleanup logic after share completes
  - [ ] 6.2: Use cache directory for automatic cleanup on low storage
  - [ ] 6.3: Consider adding manual cleanup for old temp files
  - [ ] 6.4: Test cache directory doesn't accumulate old files

- [ ] Task 7: Integration testing and validation (AC: #1-3)
  - [ ] 7.1: Test share to WhatsApp (verify image appears in chat)
  - [ ] 7.2: Test share to Telegram (verify image quality)
  - [ ] 7.3: Test share to Email (verify image attachment works)
  - [ ] 7.4: Test share to other apps (Gallery, Drive, etc.)
  - [ ] 7.5: Verify metadata "Generated with OnDevice AI" appears where supported
  - [ ] 7.6: Test error scenarios (no apps support image sharing)
  - [ ] 7.7: Test FileProvider URI permission grants work correctly

## Dev Notes

### Learnings from Previous Story

**From Story 7-5-save-generated-image-to-gallery (Status: drafted)**

This story builds directly on Story 7.5's work. Key integration points:

- **Save Functionality as Reference**: Story 7.5 implements saving bitmap to storage
  - Reuse bitmap handling patterns from save implementation
  - Share requires temporary file creation (similar to save but in cache dir)
  - Follow same error handling patterns

- **FinalImageDisplay Extension**: Story 7.5 adds Save button to FinalImageDisplay
  - Story 7.6 adds Share button alongside Save button
  - Arrange buttons in Row for horizontal layout
  - Share button: `Icons.Default.Share`

- **ViewModel Pattern Established**: Story 7.5 extends ImageGenerationViewModel
  - Add `shareImage()` function following same pattern as `saveImage()`
  - Use viewModelScope.launch for async operations
  - Consider adding `isSharing: Boolean` to ImageGenerationUiState if needed

**From Story 7-4-image-generation-screen-ui (Status: review)**

- **Screen Structure**: `ImageGenerationScreen.kt` with `FinalImageDisplay` composable
  - Location: `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt:262-316`
  - Extend with Share button alongside Save button (from Story 7.5)

- **ViewModel Available**: `ImageGenerationViewModel` manages state
  - Add `shareImage()` function to handle share logic
  - Bitmap available via `uiState.finalBitmap`

[Source: sprint status/7-4-image-generation-screen-ui.md]

### Technical Context

**FileProvider Setup:**

FileProvider enables secure URI sharing to external apps (required for Android 7+).

1. **AndroidManifest.xml:**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="ai.ondevice.app.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

2. **res/xml/file_paths.xml (create):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="shared_images" path="shared/" />
</paths>
```

**Share Function Pattern:**
```kotlin
// In ImageGenerationViewModel
fun shareImage(context: Context, bitmap: Bitmap) {
    viewModelScope.launch {
        try {
            // 1. Create temporary file in cache directory
            val cacheDir = File(context.cacheDir, "shared")
            cacheDir.mkdirs()
            val file = File(cacheDir, "shared_image_${System.currentTimeMillis()}.png")

            // 2. Write bitmap to file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // 3. Get content URI via FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "ai.ondevice.app.fileprovider",
                file
            )

            // 4. Create share intent
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Generated with OnDevice AI")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 5. Launch share chooser
            context.startActivity(Intent.createChooser(intent, "Share Image"))

        } catch (e: Exception) {
            Log.e("ImageGeneration", "Failed to share image", e)
            // Error toast handled by caller
        }
    }
}
```

**Integration with Story 3.4 Pattern:**

Story 3.4 (Long-press context menu) implements sharing for messages. Reuse patterns:
- `Intent.ACTION_SEND` for sharing
- `Intent.createChooser()` for app selection
- Similar error handling approach

**Metadata Handling:**

- **Option 1** (Simple): Add text via `Intent.EXTRA_TEXT = "Generated with OnDevice AI"`
  - Works with most apps (WhatsApp, Telegram show text alongside image)

- **Option 2** (Advanced): Embed metadata in PNG file
  - Use PNG text chunks (tEXt, iTXt)
  - Requires bitmap metadata API (more complex, optional enhancement)

**Cache Cleanup:**

- Android automatically cleans cache directory when storage is low
- Temporary share files stored in `context.cacheDir/shared/`
- Optional: Manual cleanup of old files (e.g., delete files older than 24 hours)

**Button Layout:**

Arrange Save and Share buttons horizontally:
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    Button(
        onClick = onSave,
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.SaveAlt, ...)
        Text("Save to Gallery")
    }

    Button(
        onClick = onShare,
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.Share, ...)
        Text("Share")
    }
}
```

### Project Structure Notes

**Files to Create:**
- `app/src/main/res/xml/file_paths.xml` - FileProvider paths configuration

**Files to Modify:**
- `app/src/main/AndroidManifest.xml`
  - Add FileProvider declaration

- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationScreen.kt`
  - Update `FinalImageDisplay` composable to add Share button alongside Save button (from Story 7.5)
  - Add `onShare: () -> Unit` callback parameter

- `app/src/main/java/ai/ondevice/app/ui/imagegeneration/ImageGenerationViewModel.kt`
  - Add `shareImage(context: Context, bitmap: Bitmap)` function
  - Add helper function for temporary file creation

**Existing Patterns to Reference:**
- Share intent: Story 3.4 `Long-Press Context Menu` (shares message text)
- File operations: Story 7.5 `Save Generated Image` (bitmap to file handling)
- Async operations: `ImageGenerationViewModel.startGeneration()` (viewModelScope.launch pattern)

### References

- [Epic 7 Story 7.6](docs/epics.md#Story-76-Share-Generated-Image)
- [Previous Story: 7.5 Save Generated Image to Gallery](sprint status/7-5-save-generated-image-to-gallery.md)
- [Story 3.4: Long-Press Context Menu](sprint status/3-4-long-press-context-menu.md) - Share pattern reference
- [Architecture: Project Structure](docs/architecture.md#Project-Structure)
- [Android FileProvider Guide](https://developer.android.com/reference/androidx/core/content/FileProvider)
- [Android Sharing Content](https://developer.android.com/training/sharing/send)

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
