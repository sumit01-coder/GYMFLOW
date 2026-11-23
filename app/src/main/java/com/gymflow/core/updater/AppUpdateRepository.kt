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

        // Tier 1: Fast CDN Manifest (Raw GitHub Fastly CDN - zero rate limits, sub-100ms global latency)
        const val CDN_MANIFEST_URL = "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/main/public/version.json"

        // Tier 2: GitHub REST API Single Latest Release (Minimal payload, official GitHub releases)
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

        // Tier 3: GitHub Pages Edge CDN
        const val PAGES_MANIFEST_URL = "https://$GITHUB_OWNER.github.io/$GITHUB_REPO/version.json"

        // Tier 4: Fallback full releases list
        const val RELEASES_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases"

        private const val CACHE_EXPIRATION_MS = 5 * 60 * 1000L // 5 minutes cache
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

        // Custom endpoint override takes immediate precedence
        if (customEndpoint != null) {
            return@withContext tryQueryEndpoint(customEndpoint)
        }

        val currentVersion = BuildConfig.VERSION_NAME

        // Tier 1: Fast Raw CDN Manifest (Zero Rate-Limit)
        val cdnResult = fetchManifestFromUrl(CDN_MANIFEST_URL, currentVersion)
        if (cdnResult.isSuccess) {
            val info = cdnResult.getOrNull()
            cachedUpdateInfo = info
            lastCheckTimestamp = now
            return@withContext Result.success(info)
        }

        // Tier 2: GitHub REST API Latest Release (Single object)
        val latestResult = fetchLatestGitHubRelease(LATEST_RELEASE_URL, currentVersion)
        if (latestResult.isSuccess) {
            val info = latestResult.getOrNull()
            cachedUpdateInfo = info
            lastCheckTimestamp = now
            return@withContext Result.success(info)
        }

        // Tier 3: GitHub Pages Global Edge CDN
        val pagesResult = fetchManifestFromUrl(PAGES_MANIFEST_URL, currentVersion)
        if (pagesResult.isSuccess) {
            val info = pagesResult.getOrNull()
            cachedUpdateInfo = info
            lastCheckTimestamp = now
            return@withContext Result.success(info)
        }

        // Tier 4: Fallback full releases list
        val fullResult = fetchFromFullReleases(RELEASES_URL, currentVersion)
        if (fullResult.isSuccess) {
            val info = fullResult.getOrNull()
            cachedUpdateInfo = info
            lastCheckTimestamp = now
            return@withContext Result.success(info)
        }

        // If all tiers failed, return cached if present or the last error
        if (cachedUpdateInfo != null) {
            Result.success(cachedUpdateInfo)
        } else {
            Result.failure(cdnResult.exceptionOrNull() ?: latestResult.exceptionOrNull() ?: IllegalStateException("Update server unavailable. Please check your internet connection."))
        }
    }

    private fun fetchManifestFromUrl(urlStr: String, currentVersion: String): Result<AppUpdateInfo?> {
        return try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("User-Agent", "GymFlow-Android/${BuildConfig.VERSION_NAME}")
                setRequestProperty("Connection", "keep-alive")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return Result.failure(IllegalStateException("HTTP ${connection.responseCode} from $urlStr"))
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val manifest = json.decodeFromString<AppVersionManifestDto>(body)

            val remoteTag = manifest.tagName.ifEmpty { "v${manifest.version}" }
            val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()
            val isNewer = VersionComparator.isNewerVersion(remoteTag, currentVersion)

            if (!isNewer) {
                return Result.success(null) // Already on latest version
            }

            val downloadUrl = manifest.directCdnUrl ?: manifest.downloadUrl
            val fileName = manifest.fileName.ifEmpty { "GymFlow-$remoteTag.apk" }

            val info = AppUpdateInfo(
                currentVersion = currentVersion,
                latestVersion = cleanRemote,
                releaseTitle = manifest.releaseTitle.ifEmpty { "GymFlow $remoteTag" },
                changelog = manifest.changelog.ifEmpty { "Performance improvements, stability fixes, and design enhancements." },
                downloadUrl = downloadUrl,
                fileName = fileName,
                fileSizeBytes = manifest.fileSizeBytes,
                releasePageUrl = manifest.releasePageUrl.ifEmpty { "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases" },
                targetApkSha256 = manifest.sha256
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchLatestGitHubRelease(urlStr: String, currentVersion: String): Result<AppUpdateInfo?> {
        return try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GymFlow-Android/${BuildConfig.VERSION_NAME}")
                setRequestProperty("Connection", "keep-alive")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return Result.success(null)
            }

            if (connection.responseCode == 403) {
                return Result.failure(IllegalStateException("GitHub rate-limited"))
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return Result.failure(IllegalStateException("HTTP ${connection.responseCode}"))
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val release = json.decodeFromString<GitHubReleaseDto>(body)
            if (release.draft) return Result.success(null)

            val remoteTag = release.tagName.ifEmpty { release.name ?: "" }
            val isNewer = VersionComparator.isNewerVersion(remoteTag, currentVersion)
            if (!isNewer) return Result.success(null)

            val apkAsset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true) ||
                        it.contentType == "application/vnd.android.package-archive"
            } ?: return Result.success(null)

            val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").trim()
            val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()

            val patchAsset = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".patch", ignoreCase = true) &&
                        (asset.name.contains(cleanCurrent) || release.assets.count { it.name.endsWith(".patch", ignoreCase = true) } == 1)
            }

            val shaRegex = Regex("""(?i)\b([a-f0-9]{64})\b""")
            val targetSha256 = release.body?.let { b -> shaRegex.find(b)?.value }

            val info = AppUpdateInfo(
                currentVersion = currentVersion,
                latestVersion = cleanRemote,
                releaseTitle = release.name ?: "GymFlow $remoteTag",
                changelog = release.body ?: "Performance improvements, UI refinements, and stability fixes.",
                downloadUrl = apkAsset.downloadUrl.ifEmpty { release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases" },
                fileName = apkAsset.name.ifEmpty { "GymFlow-$remoteTag.apk" },
                fileSizeBytes = apkAsset.size,
                releasePageUrl = release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases",
                patchDownloadUrl = patchAsset?.downloadUrl?.ifEmpty { null },
                patchFileName = patchAsset?.name?.ifEmpty { null },
                patchSizeBytes = patchAsset?.size ?: 0L,
                targetApkSha256 = targetSha256
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFromFullReleases(urlStr: String, currentVersion: String): Result<AppUpdateInfo?> {
        return try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "GymFlow-Android/${BuildConfig.VERSION_NAME}")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return Result.failure(IllegalStateException("HTTP ${connection.responseCode}"))
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val releases = try {
                json.decodeFromString<List<GitHubReleaseDto>>(body)
            } catch (_: Exception) {
                listOfNotNull(json.decodeFromString<GitHubReleaseDto>(body))
            }

            for (release in releases) {
                if (release.draft) continue

                val remoteTag = release.tagName.ifEmpty { release.name ?: "" }
                val isNewer = VersionComparator.isNewerVersion(remoteTag, currentVersion)
                if (!isNewer) continue

                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true) ||
                            it.contentType == "application/vnd.android.package-archive"
                } ?: continue

                val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V").trim()
                val cleanRemote = remoteTag.removePrefix("v").removePrefix("V").trim()

                val patchAsset = release.assets.firstOrNull { asset ->
                    asset.name.endsWith(".patch", ignoreCase = true) &&
                            (asset.name.contains(cleanCurrent) || release.assets.count { it.name.endsWith(".patch", ignoreCase = true) } == 1)
                }

                val shaRegex = Regex("""(?i)\b([a-f0-9]{64})\b""")
                val targetSha256 = release.body?.let { b -> shaRegex.find(b)?.value }

                val info = AppUpdateInfo(
                    currentVersion = currentVersion,
                    latestVersion = cleanRemote,
                    releaseTitle = release.name ?: "GymFlow $remoteTag",
                    changelog = release.body ?: "Performance improvements, UI refinements, and stability fixes.",
                    downloadUrl = apkAsset.downloadUrl.ifEmpty { release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases" },
                    fileName = apkAsset.name.ifEmpty { "GymFlow-$remoteTag.apk" },
                    fileSizeBytes = apkAsset.size,
                    releasePageUrl = release.htmlUrl ?: "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases",
                    patchDownloadUrl = patchAsset?.downloadUrl?.ifEmpty { null },
                    patchFileName = patchAsset?.name?.ifEmpty { null },
                    patchSizeBytes = patchAsset?.size ?: 0L,
                    targetApkSha256 = targetSha256
                )
                return Result.success(info)
            }
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tryQueryEndpoint(endpoint: String): Result<AppUpdateInfo?> {
        return if (endpoint.endsWith(".json", ignoreCase = true)) {
            fetchManifestFromUrl(endpoint, BuildConfig.VERSION_NAME)
        } else {
            fetchFromFullReleases(endpoint, BuildConfig.VERSION_NAME)
        }
    }
}
