package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class RemoteUpdateInfo(
    val versionCode: Int = -1,
    val versionName: String = "",
    val apkUrl: String = ""
)

sealed interface AppUpdateUiState {
    data object Idle : AppUpdateUiState
    data object Checking : AppUpdateUiState
    data object UpToDate : AppUpdateUiState
    data class UpdateAvailable(val info: RemoteUpdateInfo) : AppUpdateUiState
    data class Downloading(
        val info: RemoteUpdateInfo,
        val progress: Float?,
        val percentage: Int
    ) : AppUpdateUiState
    data class ReadyToInstall(val info: RemoteUpdateInfo, val apkFile: File) : AppUpdateUiState
    data class Error(
        val message: String,
        val retryInfo: RemoteUpdateInfo? = null
    ) : AppUpdateUiState
}

object AppUpdateManager {
    const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/busawamwenga-cpu/Movie-sw/main/update.json"
    const val ERROR_CONNECTION_MSG = "Impossible de vérifier. Vérifiez votre connexion."

    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private val updateAdapter by lazy { moshi.adapter(RemoteUpdateInfo::class.java) }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun parseUpdateJson(jsonText: String): RemoteUpdateInfo {
        val parsed = updateAdapter.fromJson(jsonText.trim())
            ?: throw IllegalArgumentException("Empty or unreadable update JSON")
        val versionName = parsed.versionName.trim()
        val apkUrl = parsed.apkUrl.trim()
        if (parsed.versionCode <= 0 || versionName.isBlank() ||
            !(apkUrl.startsWith("http://") || apkUrl.startsWith("https://"))
        ) {
            throw IllegalArgumentException("Invalid or missing required update JSON fields")
        }
        return RemoteUpdateInfo(
            versionCode = parsed.versionCode,
            versionName = versionName,
            apkUrl = apkUrl
        )
    }

    fun isUpdateAvailable(remoteVersionCode: Int, installedVersionCode: Int): Boolean {
        return remoteVersionCode > installedVersionCode
    }

    suspend fun fetchUpdateInfo(url: String = UPDATE_JSON_URL): RemoteUpdateInfo =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .header("Cache-Control", "no-cache")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("HTTP ${response.code}")
                }
                val body = response.body?.string()
                    ?: throw IllegalStateException("Empty response body")
                parseUpdateJson(body)
            }
        }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float?, Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val tmpFile = File(updatesDir, "movie_sw_update.apk.tmp")
        val finalFile = File(updatesDir, "movie_sw_update.apk")

        val request = Request.Builder()
            .url(apkUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("Empty APK body")
            val contentLength = body.contentLength()
            onProgress(if (contentLength > 0L) 0f else null, 0)

            body.byteStream().use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var totalRead = 0L
                    var lastPct = -1
                    var lastUpdateMs = 0L

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        totalRead += read

                        val now = System.currentTimeMillis()
                        if (contentLength > 0L) {
                            val ratio = (totalRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            val pct = (ratio * 100f).toInt().coerceIn(0, 100)
                            if (pct != lastPct && (now - lastUpdateMs >= 80L || pct == 100)) {
                                lastPct = pct
                                lastUpdateMs = now
                                onProgress(ratio, pct)
                            }
                        } else if (now - lastUpdateMs >= 200L) {
                            lastUpdateMs = now
                            onProgress(null, 0)
                        }
                    }
                    output.flush()
                }
            }
        }

        if (finalFile.exists()) {
            finalFile.delete()
        }
        if (!tmpFile.renameTo(finalFile)) {
            tmpFile.copyTo(finalFile, overwrite = true)
            tmpFile.delete()
        }
        onProgress(1f, 100)
        finalFile
    }

    fun launchApkInstaller(context: Context, apkFile: File): Result<Unit> = runCatching {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
