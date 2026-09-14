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
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val GITHUB_RELEASE_URL =
            "https://api.github.com/repos/adandavid98/Dominotes/releases/latest"
        const val DEFAULT_DOWNLOAD_URL =
            "https://github.com/adandavid98/Dominotes/releases/latest/download/AnotadorDomino.apk"
    }

    suspend fun checkForUpdates(currentBuildTimestamp: Long): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASE_URL)
                .header("User-Agent", "Dominotes-Android")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val bodyString = response.body?.string() ?: return@withContext null
            val json = JSONObject(bodyString)

            val title = json.optString("name", "Nueva versión disponible")
            val notes = json.optString("body", "")
            val publishedAtStr = json.optString("published_at", "")

            var publishedMillis: Long = 0
            if (publishedAtStr.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    publishedMillis = sdf.parse(publishedAtStr)?.time ?: 0
                } catch (e: Exception) {
                    publishedMillis = 0
                }
            }

            var apkUrl = DEFAULT_DOWNLOAD_URL
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", DEFAULT_DOWNLOAD_URL)
                        break
                    }
                }
            }

            val isNewer = publishedMillis > (currentBuildTimestamp + 60_000L)

            UpdateInfo(
                hasUpdate = isNewer,
                releaseTitle = title,
                releaseNotes = notes,
                downloadUrl = apkUrl,
                publishedAtMillis = publishedMillis
            )
        } catch (e: Exception) {
            null
        }
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
