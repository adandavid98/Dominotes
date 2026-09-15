package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val publishedAtMillis: Long
)

class AppUpdateChecker(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    companion object {
        private const val GITHUB_RELEASE_API =
            "https://api.github.com/repos/adandavid98/Dominotes/releases/latest"
        const val DEFAULT_DOWNLOAD_URL =
            "https://github.com/adandavid98/Dominotes/releases/latest/download/AnotadorDomino.apk"
        private const val VERSION_JSON_URL =
            "https://github.com/adandavid98/Dominotes/releases/latest/download/version.json"
    }

    suspend fun checkForUpdates(currentBuildTimestamp: Long): UpdateInfo? = withContext(Dispatchers.IO) {
        // Strategy 1: Fast CDN version.json (No GitHub API rate limits)
        try {
            val vRequest = Request.Builder()
                .url(VERSION_JSON_URL)
                .header("User-Agent", "Dominotes-Android")
                .build()
            val vResponse = client.newCall(vRequest).execute()
            if (vResponse.isSuccessful) {
                val vBody = vResponse.body?.string()
                if (!vBody.isNullOrBlank()) {
                    val vJson = JSONObject(vBody)
                    val remoteTimestamp = vJson.optLong("timestamp", 0L)
                    val publishedAt = vJson.optString("publishedAt", "")
                    val dateMillis = if (remoteTimestamp > 0) remoteTimestamp else parseIsoDate(publishedAt)
                    if (dateMillis > 0) {
                        val isNewer = dateMillis > (currentBuildTimestamp + 60_000L)
                        return@withContext UpdateInfo(
                            hasUpdate = isNewer,
                            releaseTitle = "Última Versión (Auto-Release)",
                            releaseNotes = "Nueva versión disponible en GitHub.",
                            downloadUrl = DEFAULT_DOWNLOAD_URL,
                            publishedAtMillis = dateMillis
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        // Strategy 2: GitHub API Releases endpoint
        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASE_API)
                .header("User-Agent", "Dominotes-Android")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    val json = JSONObject(bodyString)
                    val title = json.optString("name", "Nueva versión disponible")
                    val notes = json.optString("body", "")
                    val publishedMillis = parseIsoDate(json.optString("published_at", ""))
                    val updatedMillis = parseIsoDate(json.optString("updated_at", ""))

                    var apkUrl = DEFAULT_DOWNLOAD_URL
                    var assetMillis = 0L
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url", DEFAULT_DOWNLOAD_URL)
                                val assetUpdated = parseIsoDate(asset.optString("updated_at", ""))
                                val assetCreated = parseIsoDate(asset.optString("created_at", ""))
                                assetMillis = maxOf(assetUpdated, assetCreated)
                                break
                            }
                        }
                    }

                    val latestRemoteMillis = maxOf(publishedMillis, updatedMillis, assetMillis)
                    val isNewer = latestRemoteMillis > (currentBuildTimestamp + 60_000L)

                    return@withContext UpdateInfo(
                        hasUpdate = isNewer,
                        releaseTitle = title,
                        releaseNotes = notes,
                        downloadUrl = apkUrl,
                        publishedAtMillis = latestRemoteMillis
                    )
                }
            }
        } catch (_: Exception) {}

        // Strategy 3: HTTP HEAD on the APK directly (Reads Last-Modified header, bypassing API limits)
        try {
            val headRequest = Request.Builder()
                .url(DEFAULT_DOWNLOAD_URL)
                .head()
                .header("User-Agent", "Dominotes-Android")
                .build()
            val headResponse = client.newCall(headRequest).execute()
            if (headResponse.isSuccessful) {
                val lastModifiedHeader = headResponse.header("Last-Modified")
                val lastModifiedMillis = parseHttpDate(lastModifiedHeader)
                if (lastModifiedMillis > 0) {
                    val isNewer = lastModifiedMillis > (currentBuildTimestamp + 60_000L)
                    return@withContext UpdateInfo(
                        hasUpdate = isNewer,
                        releaseTitle = "Última Versión (Auto-Release)",
                        releaseNotes = "Nueva versión disponible en GitHub.",
                        downloadUrl = DEFAULT_DOWNLOAD_URL,
                        publishedAtMillis = lastModifiedMillis
                    )
                }
            }
        } catch (_: Exception) {}

        null
    }

    private fun parseHttpDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM d HH:mm:ss yyyy"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("GMT")
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    private fun parseIsoDate(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    fun openDownloadUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
