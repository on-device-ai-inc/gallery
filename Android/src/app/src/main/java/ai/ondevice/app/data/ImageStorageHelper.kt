package ai.ondevice.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class for saving and loading chat images to/from internal storage.
 */
object ImageStorageHelper {
    private const val TAG = "ImageStorageHelper"
    private const val CHAT_IMAGES_DIR = "chat_images"

    /**
     * Save a list of bitmaps to internal storage for a specific thread.
     * Returns a comma-separated string of file paths.
     */
    fun saveImages(
        context: Context,
        threadId: Long,
        images: List<Bitmap>
    ): String {
        if (images.isEmpty()) return ""

        val threadDir = File(context.filesDir, "$CHAT_IMAGES_DIR/thread_$threadId")
        threadDir.mkdirs()

        val filePaths = images.mapIndexed { index, bitmap ->
            try {
                val timestamp = System.currentTimeMillis()
                val fileName = "image_${timestamp}_$index.jpg"
                val file = File(threadDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save image $index", e)
                null
            }
        }.filterNotNull()

        return filePaths.joinToString(",")
    }

    /**
     * Load bitmaps from a comma-separated string of file paths.
     * Returns list of successfully loaded bitmaps.
     */
    fun loadImages(imageUris: String?): List<Bitmap> {
        if (imageUris.isNullOrBlank()) return emptyList()

        val paths = imageUris.split(",")
        return paths.mapNotNull { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(path)
                } else {
                    Log.w(TAG, "Image file not found: $path")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image: $path", e)
                null
            }
        }
    }

    /**
     * Delete all images for a specific thread.
     */
    fun deleteThreadImages(context: Context, threadId: Long) {
        try {
            val threadDir = File(context.filesDir, "$CHAT_IMAGES_DIR/thread_$threadId")
            if (threadDir.exists()) {
                threadDir.deleteRecursively()
                Log.d(TAG, "Deleted images for thread $threadId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete thread images", e)
        }
    }

    /**
     * Clean up orphaned image files (for threads that no longer exist).
     * Should be called periodically.
     */
    fun cleanupOrphanedImages(context: Context, validThreadIds: Set<Long>) {
        try {
            val chatImagesDir = File(context.filesDir, CHAT_IMAGES_DIR)
            if (!chatImagesDir.exists()) return

            chatImagesDir.listFiles()?.forEach { threadDir ->
                if (threadDir.isDirectory) {
                    val threadIdStr = threadDir.name.removePrefix("thread_")
                    val threadId = threadIdStr.toLongOrNull()

                    if (threadId == null || threadId !in validThreadIds) {
                        threadDir.deleteRecursively()
                        Log.d(TAG, "Cleaned up orphaned images: ${threadDir.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup orphaned images", e)
        }
    }
}
