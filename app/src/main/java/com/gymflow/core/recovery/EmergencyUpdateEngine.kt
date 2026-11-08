package com.gymflow.core.recovery

import android.content.Context
import android.util.Log
import com.gymflow.BuildConfig
import com.gymflow.core.updater.AppInstallHelper
import com.gymflow.core.updater.PackageSignatureVerifier
import com.gymflow.core.updater.VersionComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class EmergencyUpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSizeMb: Float,
    val isNewer: Boolean
)

object EmergencyUpdateEngine {

    private const val TAG = "EmergencyUpdateEngine"
    private const val GITHUB_OWNER = "sumit01-coder"
    private const val GITHUB_REPO = "GYMFLOW"
    private const val RELEASES_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases"

    /**
     * Standalone HTTP check against GitHub Releases API without any DI or Room requirements.
     */
    suspend fun checkLatestRelease(): Result<EmergencyUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = URL(RELEASES_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GymFlow-Emergency-Recovery-App")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    Exception("Server returned HTTP ${connection.responseCode}: ${connection.responseMessage}")
                )
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val releasesArray = JSONArray(responseBody)
            if (releasesArray.length() == 0) {
                return@withContext Result.success(null)
            }

            // Inspect the latest non-draft release
            for (i in 0 until releasesArray.length()) {
                val rel = releasesArray.getJSONObject(i)
                val isDraft = rel.optBoolean("draft", false)
                if (isDraft) continue

                val tagName = rel.optString("tag_name", "").trim()
                val body = rel.optString("body", "Emergency Bug Fixes & Stability Improvements")
                val assets = rel.optJSONArray("assets") ?: JSONArray()

                var apkDownloadUrl: String? = null
                var apkSize: Long = 0L

                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        if (name.contains("release", ignoreCase = true)) {
                            break // Prioritize release APK
                        }
                    }
                }

                if (!apkDownloadUrl.isNullOrBlank() && tagName.isNotBlank()) {
                    val currentVersion = BuildConfig.VERSION_NAME
                    val isNewer = VersionComparator.isNewerVersion(tagName, currentVersion)
                    val sizeMb = if (apkSize > 0) apkSize / (1024f * 1024f) else 35.0f

                    return@withContext Result.success(
                        EmergencyUpdateInfo(
                            versionName = tagName,
                            releaseNotes = body,
                            downloadUrl = apkDownloadUrl,
                            apkSizeMb = sizeMb,
                            isNewer = isNewer
                        )
                    )
                }
            }

            Result.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Emergency update check failed", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK file to application cache directory with live progress stream,
     * verifies its cryptographic signature, and opens Android PackageInstaller.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        targetVersion: String,
        onProgress: (progressPercent: Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val appCtx = context.applicationContext
            val updatesDir = File(appCtx.cacheDir, "updates").apply { mkdirs() }
            val cleanVersion = targetVersion.removePrefix("v").removePrefix("V")
            val targetApk = File(updatesDir, "gymflow-v$cleanVersion-emergency.apk")

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "GymFlow-Emergency-Recovery-App")
            }

            val responseCode = connection.responseCode
            // Follow redirects if necessary (GitHub asset URLs redirect to AWS S3)
            val finalConnection = if (responseCode in 300..399) {
                val redirectUrl = connection.getHeaderField("Location")
                (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "GymFlow-Emergency-Recovery-App")
                }
            } else {
                connection
            }

            val totalBytes = finalConnection.contentLength.toLong()
            var downloadedBytes = 0L

            finalConnection.inputStream.use { input ->
                FileOutputStream(targetApk).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastReportedPercent = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt()
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                    output.flush()
                }
            }

            onProgress(100)

            // Verify signature compatibility
            val compat = PackageSignatureVerifier.verifyApkCompatibility(appCtx, targetApk)
            if (!compat.isCompatible) {
                return@withContext Result.failure(
                    Exception("APK verification failed: ${compat.errorMessage ?: "Cryptographic signature mismatch"}")
                )
            }

            // Launch system PackageInstaller
            withContext(Dispatchers.Main) {
                AppInstallHelper.installApk(appCtx, targetApk)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Emergency download/install failed", e)
            Result.failure(e)
        }
    }
}
