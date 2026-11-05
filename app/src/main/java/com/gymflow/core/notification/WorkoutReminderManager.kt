package com.gymflow.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gymflow.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GYMFLOW Scheduled Workout Reminders & Notification Engine.
 * Manages exact alarm scheduling via Android AlarmManager, notification channels,
 * and high-priority rich push notifications.
 */
@Singleton
class WorkoutReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val TAG = "WorkoutReminderManager"
        const val CHANNEL_ID = "gymflow_workout_reminders"
        const val CHANNEL_NAME = "Workout Reminders"
        const val ACTION_WORKOUT_REMINDER = "com.gymflow.action.WORKOUT_REMINDER"
        const val NOTIFICATION_ID = 2001
        const val REMINDER_REQUEST_CODE = 4001

        private const val PREFS_NAME = "gymflow_reminder_preferences"
        private const val KEY_REMINDER_ENABLED = "key_workout_reminder_enabled"
        private const val KEY_REMINDER_HOUR = "key_workout_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "key_workout_reminder_minute"

        const val DEFAULT_HOUR = 6
        const val DEFAULT_MINUTE = 30
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    init {
        createNotificationChannel()
    }

    /**
     * Initializes the High Importance Notification Channel with vibration enabled.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Scheduled daily notifications to keep your workout consistency high"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $CHANNEL_ID")
            }
        }
    }

    /**
     * Schedules a daily exact alarm for workout reminders.
     * Persists the time and enabled status.
     */
    fun scheduleReminder(hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, true)
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()

        scheduleExactAlarm(hour, minute)
        Log.d(TAG, "Reminder scheduled for ${formatTime(hour, minute)}")
    }

    /**
     * Cancels any pending workout reminder alarm and marks it disabled.
     */
    fun cancelReminder() {
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, false)
            .apply()

        val pendingIntent = createAlarmPendingIntent()
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Workout reminder canceled")
    }

    /**
     * Checks if workout reminders are currently enabled in preferences.
     */
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, false)
    }

    /**
     * Returns the configured reminder time (hour, minute).
     */
    fun getReminderTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_REMINDER_HOUR, DEFAULT_HOUR)
        val minute = prefs.getInt(KEY_REMINDER_MINUTE, DEFAULT_MINUTE)
        return Pair(hour, minute)
    }

    /**
     * Formats hour and minute into standard user-friendly 12-hour AM/PM format (e.g., "06:30 AM").
     */
    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
    }

    /**
     * Checks if exact alarm scheduling is granted by the system (Android 12+).
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Re-schedules the alarm after system boot or package update if previously enabled.
     */
    fun rescheduleOnBoot() {
        if (isReminderEnabled()) {
            val (hour, minute) = getReminderTime()
            scheduleExactAlarm(hour, minute)
            Log.d(TAG, "Re-scheduled reminder on device boot for ${formatTime(hour, minute)}")
        }
    }

    /**
     * Schedules the next day's alarm after a reminder trigger has fired.
     */
    fun scheduleNextDailyAlarm() {
        if (isReminderEnabled()) {
            val (hour, minute) = getReminderTime()
            scheduleExactAlarm(hour, minute)
            Log.d(TAG, "Scheduled next daily alarm for ${formatTime(hour, minute)}")
        }
    }

    /**
     * Internal implementation to calculate target time and dispatch to AlarmManager.
     */
    private fun scheduleExactAlarm(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time has already passed today, advance to tomorrow
            if (before(now) || timeInMillis <= now.timeInMillis + 1000) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerMillis = target.timeInMillis
        val pendingIntent = createAlarmPendingIntent()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    // Safe fallback if user has revoked exact alarm permission
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarm set for trigger time: ${target.time}")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException setting exact alarm; falling back to setAndAllowWhileIdle", e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun createAlarmPendingIntent(): PendingIntent {
        val intent = Intent(context, WorkoutReminderReceiver::class.java).apply {
            action = ACTION_WORKOUT_REMINDER
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, intent, flags)
    }

    /**
     * Builds and displays the rich workout reminder notification.
     */
    fun showWorkoutNotification(
        title: String = "Time to Train! 🏋️",
        content: String = "Your scheduled workout is ready. Tap to start!"
    ) {
        createNotificationChannel()

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot post reminder notification.")
                return
            }
        }

        // Tap action: Launch MainActivity
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "workout_schedule")
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            flags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText("$content\nConsistency creates champions. Tap to view today's workout plan and log your sets.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Workout reminder notification displayed successfully.")
    }
}

