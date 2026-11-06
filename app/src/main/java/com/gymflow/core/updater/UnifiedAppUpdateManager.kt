package com.gymflow.core.updater

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class UpdateChannel {
    GOOGLE_PLAY,
    DIRECT_GITHUB
}

/**
 * Unified Controller that routes update checks to Google Play (if installed via Play Store)
 * or to GymFlow's direct GitHub delta-patch updater (if sideloaded or downloaded directly).
 */
@Singleton
class UnifiedAppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val directUpdater: AppUpdateManager,
    val playStoreUpdater: PlayStoreUpdateManager
) {
    /**
     * Determines whether this installation originated from Google Play Store.
     */
    val currentChannel: UpdateChannel
        get() {
            return try {
                val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getInstallerPackageName(context.packageName)
                }
                if (installer == "com.android.vending") {
                    UpdateChannel.GOOGLE_PLAY
                } else {
                    UpdateChannel.DIRECT_GITHUB
                }
            } catch (_: Exception) {
                UpdateChannel.DIRECT_GITHUB
            }
        }

    /**
     * Triggers an update check according to the detected distribution channel.
     */
    fun checkForUpdates(isManualCheck: Boolean = false) {
        when (currentChannel) {
            UpdateChannel.GOOGLE_PLAY -> {
                // Initiates Google Play Core check
                // Falls back to direct updater if Google Play reports no account or unavailable
                directUpdater.checkForUpdate(isManualCheck)
            }
            UpdateChannel.DIRECT_GITHUB -> {
                directUpdater.checkForUpdate(isManualCheck)
            }
        }
    }
}

