package com.gymflow.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver responsible for receiving AlarmManager workout reminder triggers
 * and system boot events to maintain persistent daily scheduling across device reboots.
 */
class WorkoutReminderReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "WorkoutReminderReceiver"
        const val ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED
        const val ACTION_QUICKBOOT = "android.intent.action.QUICKBOOT_POWERON"
        const val ACTION_MY_PACKAGE_REPLACED = Intent.ACTION_MY_PACKAGE_REPLACED
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "WorkoutReminderReceiver onReceive triggered with action: $action")

        val reminderManager = WorkoutReminderManager(context.applicationContext)

        when (action) {
            ACTION_BOOT_COMPLETED,
            ACTION_QUICKBOOT,
            ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Reboot/Update event received. Re-scheduling workout reminders...")
                reminderManager.rescheduleOnBoot()
            }
            WorkoutReminderManager.ACTION_WORKOUT_REMINDER -> {
                Log.d(TAG, "Workout reminder alarm fired! Triggering notification...")
                reminderManager.showWorkoutNotification(
                    title = "Time to Train! 🏋️",
                    content = "Your scheduled workout is ready. Tap to start!"
                )
                // Re-arm exact alarm for the same time tomorrow
                reminderManager.scheduleNextDailyAlarm()
            }
            else -> {
                Log.w(TAG, "Unknown action received: $action")
            }
        }
    }
}

