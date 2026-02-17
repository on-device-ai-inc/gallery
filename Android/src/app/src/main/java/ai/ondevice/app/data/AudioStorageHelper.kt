package ai.ondevice.app.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class for saving and loading chat audio to/from internal storage.
 */
object AudioStorageHelper {
    private const val TAG = "AudioStorageHelper"
    private const val CHAT_AUDIO_DIR = "chat_audio"

    /**
     * Save audio data to internal storage for a specific thread.
     * Returns the file path, or null if save failed.
     */
    fun saveAudio(
        context: Context,
        threadId: Long,
        audioData: ByteArray,
        sampleRate: Int
    ): String? {
        if (audioData.isEmpty()) return null

        val threadDir = File(context.filesDir, "$CHAT_AUDIO_DIR/thread_$threadId")
        threadDir.mkdirs()

        return try {
            val timestamp = System.currentTimeMillis()
            val fileName = "audio_${timestamp}_${sampleRate}hz.wav"
            val file = File(threadDir, fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(audioData)
            }

            Log.d(TAG, "Saved audio: ${audioData.size} bytes, $sampleRate Hz")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save audio", e)
            null
        }
    }

    /**
     * Load audio data from a file path.
     * Returns AudioData with byte array and sample rate, or null if load failed.
     */
    fun loadAudio(audioUri: String?): AudioData? {
        if (audioUri.isNullOrBlank()) return null

        return try {
            val file = File(audioUri)
            if (!file.exists()) {
                Log.w(TAG, "Audio file not found: $audioUri")
                return null
            }

            // Extract sample rate from filename (e.g., "audio_123456_16000hz.wav")
            val sampleRate = extractSampleRateFromFilename(file.name)

            val audioBytes = file.readBytes()
            Log.d(TAG, "Loaded audio: ${audioBytes.size} bytes, $sampleRate Hz")

            AudioData(audioBytes, sampleRate)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio: $audioUri", e)
            null
        }
    }

    /**
     * Delete all audio files for a specific thread.
     */
    fun deleteThreadAudio(context: Context, threadId: Long) {
        try {
            val threadDir = File(context.filesDir, "$CHAT_AUDIO_DIR/thread_$threadId")
            if (threadDir.exists()) {
                threadDir.deleteRecursively()
                Log.d(TAG, "Deleted audio for thread $threadId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete thread audio", e)
        }
    }

    /**
     * Clean up orphaned audio files (for threads that no longer exist).
     * Should be called periodically.
     */
    fun cleanupOrphanedAudio(context: Context, validThreadIds: Set<Long>) {
        try {
            val chatAudioDir = File(context.filesDir, CHAT_AUDIO_DIR)
            if (!chatAudioDir.exists()) return

            chatAudioDir.listFiles()?.forEach { threadDir ->
                if (threadDir.isDirectory) {
                    val threadIdStr = threadDir.name.removePrefix("thread_")
                    val threadId = threadIdStr.toLongOrNull()

                    if (threadId == null || threadId !in validThreadIds) {
                        threadDir.deleteRecursively()
                        Log.d(TAG, "Cleaned up orphaned audio: ${threadDir.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup orphaned audio", e)
        }
    }

    /**
     * Extract sample rate from filename pattern: "audio_timestamp_XXXXXhz.wav"
     * Returns 16000 as default if parsing fails.
     */
    private fun extractSampleRateFromFilename(filename: String): Int {
        return try {
            val regex = """(\d+)hz""".toRegex()
            val match = regex.find(filename)
            match?.groupValues?.get(1)?.toIntOrNull() ?: 16000
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse sample rate from filename: $filename, using default 16000")
            16000
        }
    }

    /**
     * Data class to hold loaded audio data and metadata.
     */
    data class AudioData(
        val audioBytes: ByteArray,
        val sampleRate: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AudioData

            if (!audioBytes.contentEquals(other.audioBytes)) return false
            if (sampleRate != other.sampleRate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = audioBytes.contentHashCode()
            result = 31 * result + sampleRate
            return result
        }
    }
}
