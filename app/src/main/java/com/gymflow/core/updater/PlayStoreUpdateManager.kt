package com.gymflow.core.updater

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed interface PlayStoreUpdateStatus {
    data object Idle : PlayStoreUpdateStatus
    data object Checking : PlayStoreUpdateStatus
    data class Available(val updateInfo: AppUpdateInfo, val isFlexibleAllowed: Boolean, val isImmediateAllowed: Boolean) : PlayStoreUpdateStatus
    data class Downloading(val bytesDownloaded: Long, val totalBytesToDownload: Long) : PlayStoreUpdateStatus
    data object Downloaded : PlayStoreUpdateStatus
    data object UpToDate : PlayStoreUpdateStatus
    data class Failed(val errorCode: Int, val message: String) : PlayStoreUpdateStatus
}

/**
 * Manages official Google Play Store In-App Updates (Play Core API).
 * Used when the application is distributed via Google Play.
 * Supports both Flexible (silent background) and Immediate (blocking) update modes with native delta streaming.
 */
@Singleton
class PlayStoreUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _status = MutableStateFlow<PlayStoreUpdateStatus>(PlayStoreUpdateStatus.Idle)
    val status: StateFlow<PlayStoreUpdateStatus> = _status.asStateFlow()

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                _status.value = PlayStoreUpdateStatus.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                _status.value = PlayStoreUpdateStatus.Downloaded
            }
            InstallStatus.FAILED -> {
                _status.value = PlayStoreUpdateStatus.Failed(
                    errorCode = state.installErrorCode(),
                    message = "Google Play update failed with error code ${state.installErrorCode()}"
                )
            }
            else -> {}
        }
    }

    init {
        appUpdateManager.registerListener(installStateListener)
    }

    /**
     * Checks Google Play servers for an available in-app update.
     */
    suspend fun checkForUpdate(): Result<AppUpdateInfo?> = suspendCancellableCoroutine { cont ->
        _status.value = PlayStoreUpdateStatus.Checking

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val isFlexible = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                val isImmediate = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                _status.value = PlayStoreUpdateStatus.Available(info, isFlexible, isImmediate)
                cont.resume(Result.success(info))
            } else {
                _status.value = PlayStoreUpdateStatus.UpToDate
                cont.resume(Result.success(null))
            }
        }.addOnFailureListener { error ->
            _status.value = PlayStoreUpdateStatus.Failed(
                errorCode = -1,
                message = error.message ?: "Failed to check Google Play for updates"
            )
            cont.resume(Result.failure(error))
        }
    }

    /**
     * Starts the flexible background update flow in the given Activity.
     */
    fun startFlexibleUpdate(activity: Activity, info: AppUpdateInfo, requestCode: Int = 1001): Boolean {
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
        val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
        return appUpdateManager.startUpdateFlowForResult(info, activity, options, requestCode)
    }

    /**
     * Starts the immediate full-screen update flow in the given Activity.
     */
    fun startImmediateUpdate(activity: Activity, info: AppUpdateInfo, requestCode: Int = 1002): Boolean {
        if (!info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) return false
        val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        return appUpdateManager.startUpdateFlowForResult(info, activity, options, requestCode)
    }

    /**
     * Completes the flexible update once downloaded, restarting the app cleanly.
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}

