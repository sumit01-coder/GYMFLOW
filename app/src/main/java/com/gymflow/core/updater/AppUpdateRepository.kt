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
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun checkForUpdate(
        customEndpoint: String? = null
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val endpoint = customEndpoint ?: LATEST_RELEASE_URL
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GymFlow-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // No releases published yet on GitHub repository
                return@withContext Result.success(null)
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    IllegalStateException("GitHub API responded with HTTP $responseCode")
                )
            }

            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use {
                it.readText()
            }

            val release = json.decodeFromString<GitHubReleaseDto>(responseBody)

            if (release.draft) {
                return@withContext Result.success(null)
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val remoteTag = release.tagName.ifEmpty { release.name ?: "" }

            val isNewer = VersionComparator.isNewerVersion(remoteTag, currentVersion)
            if (!isNewer) {
                return@withContext Result.success(null)
            }

            // Locate APK asset
            val apkAsset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true) ||
                        it.contentType == "application/vnd.android.package-archive"
            }

            val downloadUrl = apkAsset?.downloadUrl ?: release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"
            val fileName = apkAsset?.name ?: "GymFlow-$remoteTag.apk"
            val fileSizeBytes = apkAsset?.size ?: 0L

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

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
