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

    fun checkForUpdate(isManualCheck: Boolean = false) {
        scope.launch {
            _updateState.value = AppUpdateState.Checking
            val result = repository.checkForUpdate()

            result.fold(
                onSuccess = { updateInfo ->
                    if (updateInfo != null) {
                        _updateState.value = AppUpdateState.UpdateAvailable(updateInfo)
                    } else {
                        _updateState.value = AppUpdateState.UpToDate(BuildConfig.VERSION_NAME)
                    }
                },
                onFailure = { error ->
                    if (isManualCheck) {
                        _updateState.value = AppUpdateState.Error(
                            error.message ?: "Failed to check for updates"
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
        scope.launch {
            _updateState.value = AppUpdateState.Downloading(
                info = info,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = info.fileSizeBytes
            )

            try {
                val apkFile = downloadApk(info.downloadUrl, info.fileName) { progress, downloaded, total ->
                    _updateState.value = AppUpdateState.Downloading(
                        info = info,
                        progress = progress,
                        downloadedBytes = downloaded,
                        totalBytes = total
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
                    e.message ?: "Download failed. Please try again."
                )
            }
        }
    }

    fun installUpdate(apkFile: File) {
        if (!AppInstallHelper.canInstallApks(context)) {
            AppInstallHelper.openInstallPermissionSettings(context)
            return
        }
        val result = AppInstallHelper.installApk(context, apkFile)
        result.onFailure {
            _updateState.value = AppUpdateState.Error("Installation could not be started: ${it.message}")
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
        // Destination directory in app external downloads
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.cacheDir
        if (!downloadDir.exists()) downloadDir.mkdirs()

        val destinationFile = File(downloadDir, fileName)
        if (destinationFile.exists()) destinationFile.delete()

        val url = URL(downloadUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "GymFlow-Android-App")
        }

        // Handle possible GitHub 302 redirects to Amazon S3 CDN
        val responseCode = connection.responseCode
        val effectiveConnection = if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
            responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == 307 || responseCode == 308
        ) {
            val redirectUrl = connection.getHeaderField("Location")
            (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "GymFlow-Android-App")
            }
        } else {
            connection
        }

        val contentLength = effectiveConnection.contentLengthLong

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = effectiveConnection.inputStream
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8192)
            var downloadedBytes = 0L
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                downloadedBytes += read

                val progress = if (contentLength > 0L) {
                    (downloadedBytes.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                } else {
                    0.5f
                }

                withContext(Dispatchers.Main) {
                    onProgress(progress, downloadedBytes, contentLength)
                }
            }

            outputStream.flush()
            destinationFile
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }
}
