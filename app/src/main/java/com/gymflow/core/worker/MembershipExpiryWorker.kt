package com.gymflow.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gymflow.R
import com.gymflow.app.MainActivity
import com.gymflow.core.database.GymMemberDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class MembershipExpiryWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface MembershipExpiryWorkerEntryPoint {
        fun gymMemberDao(): GymMemberDao
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                appContext,
                MembershipExpiryWorkerEntryPoint::class.java
            )
            val gymMemberDao = entryPoint.gymMemberDao()

            val now = System.currentTimeMillis()
            val sevenDaysAhead = now + (7L * 24 * 60 * 60 * 1000)

            val expiringMembers = gymMemberDao.getExpiringOrExpiredMembersSync(sevenDaysAhead)
            if (expiringMembers.isNotEmpty()) {
                val expiredCount = expiringMembers.count { it.endDateEpochMs <= now }
                val expiringSoonCount = expiringMembers.size - expiredCount

                showExpirySummaryNotification(expiringMembers.size, expiredCount, expiringSoonCount)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("MembershipExpiryWorker", "Error scanning membership expiry", e)
            Result.retry()
        }
    }

    private fun showExpirySummaryNotification(total: Int, expired: Int, expiringSoon: Int) {
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "gym_membership_expiry_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Gym Membership Renewals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily alerts when gym members' subscriptions are expiring or expired."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "MEMBER_MANAGEMENT")
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when {
            expired > 0 && expiringSoon > 0 -> "$expired memberships expired, $expiringSoon expiring this week. Tap to send WhatsApp reminders."
            expired > 0 -> "$expired gym member memberships have expired. Tap to notify via WhatsApp."
            else -> "$expiringSoon memberships expiring within 7 days. Tap to review & send reminders."
        }

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("GymFlow: $total Members Need Renewal")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "DailyMembershipExpiryWorker"
        private const val NOTIFICATION_ID = 9012

        fun scheduleDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<MembershipExpiryWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
