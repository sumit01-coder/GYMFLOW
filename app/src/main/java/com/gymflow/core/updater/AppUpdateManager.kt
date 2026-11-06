package com.gymflow.core.updater

import android.content.Context
import android.os.Environment
import com.gymflow.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppUpdateRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val updateState: StateFlow<AppUpdateState> = _updateState.asStateFlow()

    private var lastUpdateInfo: AppUpdateInfo? = null

    fun checkForUpdate(isManualCheck: Boolean = false) {
        scope.launch {
            _updateState.value = AppUpdateState.Checking
            val result = repository.checkForUpdate(forceRefresh = isManualCheck)

            result.fold(
                onSuccess = { updateInfo ->
                    lastUpdateInfo = updateInfo
                    if (updateInfo != null) {
                        _updateState.value = AppUpdateState.UpdateAvailable(updateInfo)
                    } else {
                        _updateState.value = AppUpdateState.UpToDate(BuildConfig.VERSION_NAME)
                    }
                },
                onFailure = { error ->
                    if (isManualCheck) {
                        _updateState.value = AppUpdateState.Error(
                            error.message ?: "Failed to check for updates. Please try again."
                        )
                    } else {
                        // Silent fallback for automatic startup check
                        _updateState.value = AppUpdateState.Idle
                    }
                }
            )
        }
    }

    fun startDownload(info: AppUpdateInfo) {
        lastUpdateInfo = info
        scope.launch {
            // Attempt fast delta patch download & reconstruction if available
            if (info.isDeltaPatchAvailable) {
                val baseApk = ApkDeltaPatcher.getInstalledBaseApk(context)
                val patchUrl = info.patchDownloadUrl
                val patchName = info.patchFileName ?: "GymFlow-${info.latestVersion}.patch"

                if (baseApk != null && patchUrl != null) {
                    try {
                        _updateState.value = AppUpdateState.Downloading(
                            info = info,
                            progress = 0f,
                            downloadedBytes = 0L,
                            totalBytes = info.patchSizeBytes,
                            isDeltaPatch = true
                        )

                        val patchFile = downloadApk(patchUrl, patchName) { progress, downloaded, total ->
                            _updateState.value = AppUpdateState.Downloading(
                                info = info,
                                progress = progress,
                                downloadedBytes = downloaded,
                                totalBytes = total,
                                isDeltaPatch = true
                            )
                        }

                        _updateState.value = AppUpdateState.Patching(info = info, progress = 0.5f)

                        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            ?: context.cacheDir
                        val targetApk = File(downloadDir, info.fileName)

                        val patchResult = ApkDeltaPatcher.applyPatch(
                            sourceApkFile = baseApk,
                            patchFile = patchFile,
                            targetApkFile = targetApk,
                            expectedSha256 = info.targetApkSha256
                        ) { patchProgress ->
                            _updateState.value = AppUpdateState.Patching(info = info, progress = patchProgress)
                        }

                        if (patchResult.isSuccess) {
                            val readyApk = patchResult.getOrThrow()
                            val authority = "${context.packageName}.fileprovider"
                            val apkUri = androidx.core.content.FileProvider.getUriForFile(context, authority, readyApk)

                            _updateState.value = AppUpdateState.ReadyToInstall(
                                info = info,
                                apkUri = apkUri,
                                apkFile = readyApk
                            )
                            return@launch
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        // Seamlessly fall through to full APK download
                    }
                }
            }

            // Standard full APK download (fallback or baseline)
            _updateState.value = AppUpdateState.Downloading(
                info = info,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = info.fileSizeBytes,
                isDeltaPatch = false
            )

            try {
                val apkFile = downloadApk(info.downloadUrl, info.fileName) { progress, downloaded, total ->
                    _updateState.value = AppUpdateState.Downloading(
                        info = info,
                        progress = progress,
                        downloadedBytes = downloaded,
                        totalBytes = total,
                        isDeltaPatch = false
                    )
                }

                val authority = "${context.packageName}.fileprovider"
                val apkUri = androidx.core.content.FileProvider.getUriForFile(context, authority, apkFile)

                _updateState.value = AppUpdateState.ReadyToInstall(
                    info = info,
                    apkUri = apkUri,
                    apkFile = apkFile
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _updateState.value = AppUpdateState.Error(
                    e.message ?: "Download could not be completed. Please tap Retry."
                )
            }
        }
    }

    fun installUpdate(apkFile: File) {
        if (!apkFile.exists() || apkFile.length() < 1000000L) {
            _updateState.value = AppUpdateState.Error("Downloaded update file is missing or corrupted. Please retry download.")
            return
        }

        if (!AppInstallHelper.canInstallApks(context)) {
            AppInstallHelper.openInstallPermissionSettings(context)
            return
        }

        val result = AppInstallHelper.installApk(context, apkFile)
        result.onFailure {
            _updateState.value = AppUpdateState.Error("Installation could not be started: ${it.message}")
        }
    }

    fun retry() {
        val current = _updateState.value
        when {
            lastUpdateInfo != null -> {
                startDownload(lastUpdateInfo!!)
            }
            else -> {
                checkForUpdate(isManualCheck = true)
            }
        }
    }

    fun dismissUpdate() {
        _updateState.value = AppUpdateState.Idle
    }

    private suspend fun downloadApk(
        downloadUrl: String,
        fileName: String,
        onProgress: (progress: Float, downloaded: Long, total: Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.cacheDir
        if (!downloadDir.exists()) downloadDir.mkdirs()

        val destinationFile = File(downloadDir, fileName)
        if (destinationFile.exists()) destinationFile.delete()

        // Robust multi-redirect follower (handles GitHub -> S3 CDN redirects)
        var currentUrl = downloadUrl
        var effectiveConnection: HttpURLConnection
        var redirects = 0
        val maxRedirects = 8

        while (true) {
            val url = URL(currentUrl)
            effectiveConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20000
                readTimeout = 20000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) GymFlow-Android-App")
            }

            val responseCode = effectiveConnection.responseCode
            if (responseCode in 300..399) {
                val redirectUrl = effectiveConnection.getHeaderField("Location")
                    ?: throw IllegalStateException("Redirected with status $responseCode but missing Location header")
                effectiveConnection.disconnect()
                currentUrl = redirectUrl
                redirects++
                if (redirects > maxRedirects) {
                    throw IllegalStateException("Too many redirects downloading APK")
                }
                continue
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStreamText = try {
                    effectiveConnection.errorStream?.bufferedReader()?.use { it.readText() }
                } catch (_: Exception) { null }
                throw IllegalStateException("Server returned HTTP $responseCode: ${errorStreamText ?: "Download unavailable"}")
            }

            break
        }

        val contentLength = effectiveConnection.contentLengthLong

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = effectiveConnection.inputStream
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(16384) // 16 KB buffer
            var downloadedBytes = 0L
            var read: Int
            var lastProgressUpdate = 0L

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloadedBytes += read

                val now = System.currentTimeMillis()
                if (now - lastProgressUpdate > 50 || downloadedBytes == contentLength) {
                    lastProgressUpdate = now
                    val progress = if (contentLength > 0L) {
                        (downloadedBytes.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }

                    withContext(Dispatchers.Main) {
                        onProgress(progress, downloadedBytes, contentLength)
                    }
                }
            }

            outputStream.flush()
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            effectiveConnection.disconnect()
        }

        // Verify downloaded APK package integrity
        if (!destinationFile.exists() || destinationFile.length() < 1000000L) {
            destinationFile.delete()
            throw IllegalStateException("Downloaded file is incomplete. Please check connection and retry.")
        }

        val archiveInfo = context.packageManager.getPackageArchiveInfo(destinationFile.absolutePath, 0)
        if (archiveInfo == null) {
            destinationFile.delete()
            throw IllegalStateException("Downloaded update package is corrupted. Please retry.")
        }

        destinationFile
    }
}
