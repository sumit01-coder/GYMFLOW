package com.gymflow.core.updater

import android.content.Context
import com.gymflow.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val GITHUB_OWNER = "sumit01-coder"
        const val GITHUB_REPO = "GYMFLOW"
        const val RELEASES_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases"
        private const val CACHE_EXPIRATION_MS = 10 * 60 * 1000L // 10 minutes cache
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var cachedUpdateInfo: AppUpdateInfo? = null
    private var lastCheckTimestamp: Long = 0L

    suspend fun checkForUpdate(
        customEndpoint: String? = null,
        forceRefresh: Boolean = false
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedUpdateInfo != null && (now - lastCheckTimestamp < CACHE_EXPIRATION_MS)) {
            return@withContext Result.success(cachedUpdateInfo)
        }

        try {
            val endpoint = customEndpoint ?: RELEASES_URL
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) GymFlow-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext Result.success(null)
            }

            if (responseCode == 403) {
                // Rate limited by GitHub unauthenticated API limit
                if (cachedUpdateInfo != null) {
                    return@withContext Result.success(cachedUpdateInfo)
                }
                return@withContext Result.failure(
                    IllegalStateException("Update check is temporarily busy. Please try again shortly.")
                )
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    IllegalStateException("GitHub service responded with HTTP $responseCode")
                )
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use {
                it.readText()
            }

            val releases = try {
                json.decodeFromString<List<GitHubReleaseDto>>(responseBody)
            } catch (_: Exception) {
                listOfNotNull(json.decodeFromString<GitHubReleaseDto>(responseBody))
            }

            val currentVersion = BuildConfig.VERSION_NAME

            for (release in releases) {
                if (release.draft) continue

                val remoteTag = release.tagName.ifEmpty { release.name ?: "" }
                val isNewer = VersionComparator.isNewerVersion(remoteTag, currentVersion)
                if (!isNewer) continue

                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true) ||
                            it.contentType == "application/vnd.android.package-archive"
                }

                if (apkAsset == null) continue

                val downloadUrl = apkAsset.downloadUrl.ifEmpty { release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases" }
                val fileName = apkAsset.name.ifEmpty { "GymFlow-$remoteTag.apk" }
                val fileSizeBytes = apkAsset.size

                val info = AppUpdateInfo(
                    currentVersion = currentVersion,
                    latestVersion = remoteTag.removePrefix("v").removePrefix("V"),
                    releaseTitle = release.name ?: "GymFlow $remoteTag",
                    changelog = release.body ?: "Performance improvements, UI refinements, and stability fixes.",
                    downloadUrl = downloadUrl,
                    fileName = fileName,
                    fileSizeBytes = fileSizeBytes,
                    releasePageUrl = release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"
                )
                cachedUpdateInfo = info
                lastCheckTimestamp = now
                return@withContext Result.success(info)
            }

            cachedUpdateInfo = null
            lastCheckTimestamp = now
            Result.success(null)
        } catch (e: Exception) {
            if (cachedUpdateInfo != null) {
                Result.success(cachedUpdateInfo)
            } else {
                Result.failure(e)
            }
        }
    }
}
