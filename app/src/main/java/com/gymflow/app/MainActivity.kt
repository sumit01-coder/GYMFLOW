package com.gymflow.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import com.gymflow.core.service.OutdoorTrackingService
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.updater.AppUpdateManager
import com.gymflow.core.updater.ui.AppUpdateDialog
import com.gymflow.feature.navigation.AppNavGraph
import com.gymflow.feature.widget.GymFlowWidgetUpdater
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

        handleWidgetIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Ensure home screen widgets reflect any workout/step changes made inside the app
        GymFlowWidgetUpdater.updateAllWidgets(this)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val action = intent?.getStringExtra(GymFlowWidgetUpdater.EXTRA_WIDGET_ACTION) ?: return
        when (action) {
            GymFlowWidgetUpdater.ACTION_START_RUN -> launchOutdoorIfPermitted("RUN")
            GymFlowWidgetUpdater.ACTION_START_WALK -> launchOutdoorIfPermitted("WALK")
        }
    }

    private fun launchOutdoorIfPermitted(type: String) {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine) {
            val serviceIntent = Intent(this, OutdoorTrackingService::class.java).apply {
                this.action = OutdoorTrackingService.ACTION_START
                putExtra(OutdoorTrackingService.EXTRA_WORKOUT_TYPE, type)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}
