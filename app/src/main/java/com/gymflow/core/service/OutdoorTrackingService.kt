package com.gymflow.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gymflow.app.MainActivity
import com.gymflow.core.step.OutdoorWorkoutEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OutdoorTrackingService : Service() {

    @Inject
    lateinit var outdoorWorkoutEngine: OutdoorWorkoutEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var collectorJob: Job? = null

    companion object {
        const val CHANNEL_ID = "gymflow_outdoor_tracking_channel"
        const val NOTIFICATION_ID = 2002

        const val ACTION_START = "com.gymflow.action.START_OUTDOOR"
        const val ACTION_PAUSE = "com.gymflow.action.PAUSE_OUTDOOR"
        const val ACTION_RESUME = "com.gymflow.action.RESUME_OUTDOOR"
        const val ACTION_FINISH = "com.gymflow.action.FINISH_OUTDOOR"
        const val EXTRA_WORKOUT_TYPE = "extra_workout_type"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val type = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "RUN"
                startForegroundTracking(type)
            }
            ACTION_PAUSE -> {
                outdoorWorkoutEngine.pauseWorkout()
            }
            ACTION_RESUME -> {
                outdoorWorkoutEngine.resumeWorkout()
            }
            ACTION_FINISH -> {
                outdoorWorkoutEngine.finishWorkout()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundTracking(workoutType: String) {
        val initialNotification = buildNotification("Starting GPS...", "0.00 km · 0 steps")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            }
            startForeground(NOTIFICATION_ID, initialNotification, fgsTypes)
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        outdoorWorkoutEngine.startWorkout(workoutType)

        collectorJob?.cancel()
        collectorJob = scope.launch {
            outdoorWorkoutEngine.state.collectLatest { state ->
                if (!state.isTracking) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collectLatest
                }

                val title = "${state.workoutType} in Progress — ${state.formattedDuration}"
                val content = "${state.formattedDistanceKm} km · Pace ${state.formattedPace} /km · ${state.stepsCount} steps"
                val notification = buildNotification(title, content)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, OutdoorTrackingService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val finishIntent = Intent(this, OutdoorTrackingService::class.java).apply { action = ACTION_FINISH }
        val finishPendingIntent = PendingIntent.getService(this, 2, finishIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Finish", finishPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Outdoor Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live GPS distance, steps, and pace during outdoor cardio workouts"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        collectorJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

