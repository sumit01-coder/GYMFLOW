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
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Android Foreground Service managing persistent notification and system-level lifecycle
 * for ongoing GYMFLOW workout sessions and rest timers.
 */
class WorkoutTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "gymflow_workout_session_channel"
        const val CHANNEL_NAME = "GYMFLOW Workout Session"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.gymflow.action.START_SERVICE"
        const val ACTION_STOP = "com.gymflow.action.STOP_SERVICE"
        const val ACTION_PAUSE = "com.gymflow.action.PAUSE_WORKOUT"
        const val ACTION_RESUME = "com.gymflow.action.RESUME_WORKOUT"
        const val ACTION_COMPLETE_SET = "com.gymflow.action.COMPLETE_SET"
        const val ACTION_SKIP_REST = "com.gymflow.action.SKIP_REST"

        fun start(context: Context) {
            val intent = Intent(context, WorkoutTimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WorkoutTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var notificationUpdaterJob: Job? = null
    private val workoutEngine = WorkoutEngine.instance

    override fun onCreate() {
        super.onCreate()
        workoutEngine.init(applicationContext)
        createNotificationChannel()
        observeWorkoutEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> workoutEngine.pauseWorkout()
            ACTION_RESUME -> workoutEngine.resumeWorkout()
            ACTION_COMPLETE_SET -> {
                val currentSnapshot = workoutEngine.snapshot.value
                val activeSet = currentSnapshot.activeSet
                val reps = activeSet?.targetReps ?: 10
                val weight = activeSet?.targetWeightKg ?: 20f
                workoutEngine.completeSet(reps, weight)
            }
            ACTION_SKIP_REST -> workoutEngine.skipRest()
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
        }

        startForegroundWithNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        notificationUpdaterJob?.cancel()
        super.onDestroy()
    }

    private fun startForegroundWithNotification() {
        val notification = buildWorkoutNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startNotificationLoop()
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun observeWorkoutEngine() {
        // Observe rest completion to trigger haptic vibration
        serviceScope.launch {
            workoutEngine.restFinishedEvent.collectLatest {
                triggerRestVibration()
            }
        }

        // Observe state changes
        serviceScope.launch {
            workoutEngine.snapshot.collectLatest { snapshot ->
                if (snapshot.engineState == WorkoutEngineState.COMPLETED ||
                    snapshot.engineState == WorkoutEngineState.NOT_STARTED
                ) {
                    stopForegroundService()
                } else {
                    updateNotification()
                }
            }
        }
    }

    private fun startNotificationLoop() {
        notificationUpdaterJob?.cancel()
        notificationUpdaterJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildWorkoutNotification())
    }

    private fun buildWorkoutNotification(): Notification {
        val snapshot = workoutEngine.snapshot.value
        val formattedDuration = formatDuration(snapshot.elapsedSeconds)
        val exerciseName = snapshot.activeExercise?.exerciseName ?: "Workout in Progress"
        val setInfo = if (snapshot.activeExercise != null) {
            "Set ${snapshot.currentSetIndex + 1} of ${snapshot.activeExercise.targetSets}"
        } else {
            "Active Session"
        }

        val title = when (snapshot.engineState) {
            WorkoutEngineState.RESTING -> "Resting: ${formatDuration(snapshot.restRemainingSeconds.toLong())} remaining"
            WorkoutEngineState.PAUSED -> "Workout Paused ($formattedDuration)"
            else -> "$exerciseName · $formattedDuration"
        }

        val contentText = when (snapshot.engineState) {
            WorkoutEngineState.RESTING -> "Next: $exerciseName ($setInfo)"
            else -> "$setInfo · Vol: ${snapshot.totalVolumeKg.toInt()}kg · ${snapshot.estimatedCalories} kcal"
        }

        // PendingIntent to launch app
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        // Add action buttons
        if (snapshot.engineState == WorkoutEngineState.RESTING) {
            val skipRestIntent = createServicePendingIntent(ACTION_SKIP_REST, 1)
            builder.addAction(
                android.R.drawable.ic_media_next,
                "Skip Rest",
                skipRestIntent
            )
        } else if (snapshot.engineState == WorkoutEngineState.PAUSED) {
            val resumeIntent = createServicePendingIntent(ACTION_RESUME, 2)
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Resume",
                resumeIntent
            )
        } else if (snapshot.engineState == WorkoutEngineState.RUNNING) {
            val pauseIntent = createServicePendingIntent(ACTION_PAUSE, 3)
            val completeSetIntent = createServicePendingIntent(ACTION_COMPLETE_SET, 4)

            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                pauseIntent
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Complete Set",
                completeSetIntent
            )
        }

        return builder.build()
    }

    private fun createServicePendingIntent(actionName: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, WorkoutTimerService::class.java).apply {
            action = actionName
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun triggerRestVibration() {
        try {
            val vibrationPattern = longArrayOf(0, 300, 150, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(vibrationPattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(vibrationPattern, -1)
                }
            }
        } catch (e: Exception) {
            // In case vibration permission is unavailable in environment
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live fitness workout tracking timer and set controls"
                setShowBadge(false)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
