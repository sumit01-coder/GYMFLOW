package com.gymflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.updater.AppUpdateManager
import com.gymflow.core.updater.ui.AppUpdateDialog
import com.gymflow.feature.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Automatic background check for latest GitHub Release
        appUpdateManager.checkForUpdate(isManualCheck = false)

        setContent {
            GymFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GymFlowTheme.colors.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph()

                        val updateState by appUpdateManager.updateState.collectAsState()
                        AppUpdateDialog(
                            state = updateState,
                            onDownloadClick = { info -> appUpdateManager.startDownload(info) },
                            onInstallClick = { file -> appUpdateManager.installUpdate(file) },
                            onRetryClick = { appUpdateManager.retry() },
                            onDismiss = { appUpdateManager.dismissUpdate() }
                        )
                    }
                }
            }
        }
    }
}
