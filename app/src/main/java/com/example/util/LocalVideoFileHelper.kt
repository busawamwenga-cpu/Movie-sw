package com.example.util

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PickedMediaInfo(
    val uri: Uri,
    val displayName: String,
    val sizeMb: Float,
    val sizeBytes: Long = 0L,
    val durationSeconds: Int = 0
)

object LocalVideoFileHelper {

    fun queryPickedFile(context: Context, uri: Uri): PickedMediaInfo {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Transient picker URI permission is fine because we upload or copy immediately.
        } catch (_: IllegalArgumentException) {
        }

        var name = "video_${System.currentTimeMillis()}.mp4"
        var sizeBytes = 0L

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        var durationSeconds = 0
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durMs = durStr?.toLongOrNull() ?: 0L
            durationSeconds = (durMs / 1000L).toInt().coerceAtLeast(0)
        } catch (_: Exception) {
        } finally {
            runCatching { retriever.release() }
        }

        val sizeMb = if (sizeBytes > 0L) {
            (sizeBytes / (1024f * 1024f)).coerceAtLeast(0.1f)
        } else {
            12.0f
        }

        return PickedMediaInfo(
            uri = uri,
            displayName = name,
            sizeMb = sizeMb,
            sizeBytes = sizeBytes,
            durationSeconds = durationSeconds
        )
    }

    suspend fun copyVideoToAppStorage(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        expectedTotalBytes: Long,
        onProgress: (copiedBytes: Long, totalBytes: Long, percentage: Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "moviesw_videos")
        if (!dir.exists()) dir.mkdirs()

        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destFile = File(dir, "${System.currentTimeMillis()}_$safeName")

        val resolver = context.contentResolver
        val total = if (expectedTotalBytes > 0L) expectedTotalBytes else (10L * 1024L * 1024L)
        var copied = 0L

        resolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                var lastReportedPct = -1
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    copied += read
                    val pct = ((copied * 100L) / total.coerceAtLeast(copied)).toInt().coerceIn(1, 99)
                    if (pct != lastReportedPct) {
                        lastReportedPct = pct
                        onProgress(copied, total.coerceAtLeast(copied), pct)
                    }
                }
                output.flush()
            }
        } ?: throw IllegalStateException("Imeshindwa kufungua faili lililochaguliwa")

        onProgress(copied, copied.coerceAtLeast(1L), 100)
        Uri.fromFile(destFile).toString()
    }

    suspend fun copyPosterToAppStorage(
        context: Context,
        sourceUri: Uri
    ): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "moviesw_posters")
            if (!dir.exists()) dir.mkdirs()
            val destFile = File(dir, "poster_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun copyApkToReleaseStorage(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        expectedTotalBytes: Long,
        onProgress: (copiedBytes: Long, totalBytes: Long, percentage: Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "apk_releases")
        if (!dir.exists()) dir.mkdirs()

        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            .let { if (it.endsWith(".apk", ignoreCase = true)) it else "$it.apk" }
        val destFile = File(dir, "${System.currentTimeMillis()}_$safeName")

        val resolver = context.contentResolver
        val total = if (expectedTotalBytes > 0L) expectedTotalBytes else (15L * 1024L * 1024L)
        var copied = 0L

        resolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                var lastReportedPct = -1
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    copied += read
                    val pct = ((copied * 100L) / total.coerceAtLeast(copied)).toInt().coerceIn(1, 99)
                    if (pct != lastReportedPct) {
                        lastReportedPct = pct
                        onProgress(copied, total.coerceAtLeast(copied), pct)
                    }
                }
                output.flush()
            }
        } ?: throw IllegalStateException("Imeshindwa kusoma faili la APK")

        onProgress(copied, copied.coerceAtLeast(1L), 100)
        Uri.fromFile(destFile).toString()
    }
}
