package com.gymflow.core.updater

import android.net.Uri
import java.io.File

data class AppUpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val releaseTitle: String,
    val changelog: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val releasePageUrl: String,
    val patchDownloadUrl: String? = null,
    val patchFileName: String? = null,
    val patchSizeBytes: Long = 0L,
    val targetApkSha256: String? = null
) {
    val isDeltaPatchAvailable: Boolean
        get() = !patchDownloadUrl.isNullOrBlank() && patchSizeBytes > 0L
}

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class UpdateAvailable(val info: AppUpdateInfo) : AppUpdateState
    data class UpToDate(val currentVersion: String) : AppUpdateState
    data class Downloading(
        val info: AppUpdateInfo,
        val progress: Float, // 0.0f to 1.0f
        val downloadedBytes: Long,
        val totalBytes: Long,
        val isDeltaPatch: Boolean = false
    ) : AppUpdateState
    data class Patching(
        val info: AppUpdateInfo,
        val progress: Float
    ) : AppUpdateState
    data class ReadyToInstall(
        val info: AppUpdateInfo,
        val apkUri: Uri,
        val apkFile: File
    ) : AppUpdateState
    data class Error(val message: String) : AppUpdateState
}

