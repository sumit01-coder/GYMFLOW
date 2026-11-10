package com.gymflow.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.gymflow.R
import com.gymflow.app.MainActivity
import com.gymflow.core.database.DailyStepsDao
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.security.TokenManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun dailyStepsDao(): DailyStepsDao
    fun planDao(): PlanDao
    fun workoutSessionDao(): WorkoutSessionDao
    fun weightDao(): WeightDao
    fun tokenManager(): TokenManager
}

object GymFlowWidgetUpdater {

    private const val TAG = "GymFlowWidgetUpdater"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    const val EXTRA_WIDGET_ACTION = "extra_widget_action"
    const val ACTION_START_RUN = "ACTION_START_RUN"
    const val ACTION_START_WALK = "ACTION_START_WALK"
    const val ACTION_START_WORKOUT = "ACTION_START_WORKOUT"
    const val ACTION_OPEN_SCHEDULE = "ACTION_OPEN_SCHEDULE"
    const val ACTION_OPEN_HYDRATION = "ACTION_OPEN_HYDRATION"
    const val ACTION_OPEN_STOPWATCH = "ACTION_OPEN_STOPWATCH"

    const val ACTION_LOG_WATER_250 = "com.gymflow.action.LOG_WATER_250"
    const val ACTION_LOG_WATER_500 = "com.gymflow.action.LOG_WATER_500"

    fun updateAllWidgets(context: Context) {
        updateStepsWidget(context)
        updateWorkoutWidget(context)
        updateHydrationWidget(context)
        updateQuickActionWidget(context)
    }

    fun updateStepsWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GymFlowStepsWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val dailyStepsDao = entryPoint.dailyStepsDao()
                val tokenManager = entryPoint.tokenManager()

                val memberId = tokenManager.getUserIdSync() ?: "default_member"
                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entity = dailyStepsDao.getDailyStepsSync(memberId, todayStr)

                val steps = entity?.stepCount ?: 0
                val goal = (entity?.targetGoal ?: 10000).coerceAtLeast(1)
                val distanceKm = (entity?.distanceMeters ?: 0f) / 1000f
                val calories = entity?.caloriesKcal ?: 0
                val percent = ((steps.toFloat() / goal.toFloat()) * 100).toInt().coerceIn(0, 100)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_steps_layout).apply {
                        setTextViewText(R.id.widget_steps_count, "%,d".format(steps))
                        setTextViewText(R.id.widget_steps_goal, "/ %,d steps".format(goal))
                        setTextViewText(R.id.widget_steps_percent, "$percent%")
                        setProgressBar(R.id.widget_steps_progress, 100, percent, false)
                        setTextViewText(R.id.widget_steps_distance, "%.2f km".format(distanceKm))
                        setTextViewText(R.id.widget_steps_calories, "$calories kcal")

                        // Main widget click -> open GymFlow Home
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val mainPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 1,
                            mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_steps_root, mainPendingIntent)

                        // Run Button click -> Launch outdoor run
                        val runIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_WIDGET_ACTION, ACTION_START_RUN)
                        }
                        val runPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 2,
                            runIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_btn_run, runPendingIntent)

                        // Walk Button click -> Launch outdoor walk
                        val walkIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_WIDGET_ACTION, ACTION_START_WALK)
                        }
                        val walkPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 3,
                            walkIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_btn_walk, walkPendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update steps widget", e)
            }
        }
    }

    fun updateWorkoutWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GymFlowWorkoutWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val planDao = entryPoint.planDao()
                val sessionDao = entryPoint.workoutSessionDao()
                val tokenManager = entryPoint.tokenManager()

                val memberId = tokenManager.getUserIdSync() ?: "default_member"
                val today = LocalDate.now()
                val dayOfWeekName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
                val dayOfWeekShort = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
                val startOfDayMs = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val planWithDays = planDao.getActivePlanWithDaysSync(memberId)
                val completedIds = sessionDao.getCompletedExerciseIdsTodaySync(memberId, startOfDayMs).toSet()
                val activeSessions = sessionDao.getAllActiveSessionsSync(memberId)
                val hasActive = activeSessions.isNotEmpty()

                val domainPlan = planWithDays?.toDomain()
                val todayDay = domainPlan?.days?.find {
                    it.dayOfWeek.startsWith(dayOfWeekShort, ignoreCase = true) ||
                            it.dayOfWeek.equals(dayOfWeekName, ignoreCase = true)
                }

                val title: String
                val subtitle: String
                val statusText: String
                val percent: Int
                val buttonText: String

                if (todayDay == null || todayDay.isRestDay) {
                    title = "Rest Day · Recovery"
                    subtitle = "Recharge muscles and stay hydrated"
                    statusText = "Rest & Mobility"
                    percent = 100
                    buttonText = "VIEW WORKOUT SCHEDULE"
                } else {
                    val totalExercises = todayDay.exercises.size
                    val completedCount = todayDay.exercises.count { completedIds.contains(it.exerciseId) }
                    title = if (totalExercises == 0) "${todayDay.title} · Rest Day" else todayDay.title
                    subtitle = "$totalExercises Scheduled Exercises"
                    statusText = "$completedCount of $totalExercises Completed"
                    percent = if (totalExercises > 0) ((completedCount.toFloat() / totalExercises) * 100).toInt() else 0

                    buttonText = when {
                        completedCount >= totalExercises && totalExercises > 0 -> "WORKOUT COMPLETED ✓"
                        hasActive -> "RESUME ACTIVE SESSION"
                        completedCount > 0 -> "CONTINUE WORKOUT"
                        else -> "START TODAY'S WORKOUT"
                    }
                }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_workout_layout).apply {
                        setTextViewText(R.id.widget_workout_day_header, "GYMFLOW · ${dayOfWeekName.uppercase()}")
                        setTextViewText(R.id.widget_workout_title, title)
                        setTextViewText(R.id.widget_workout_subtitle, subtitle)
                        setTextViewText(R.id.widget_workout_status, statusText)
                        setTextViewText(R.id.widget_workout_percent, "$percent%")
                        setProgressBar(R.id.widget_workout_progress, 100, percent, false)
                        setTextViewText(R.id.widget_btn_start_workout, buttonText)

                        // Main widget click -> open GymFlow
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val mainPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 4,
                            mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_workout_root, mainPendingIntent)

                        // Start / Resume Workout click
                        val workoutIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_WIDGET_ACTION, ACTION_START_WORKOUT)
                        }
                        val workoutPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 5,
                            workoutIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_btn_start_workout, workoutPendingIntent)

                        // Schedule badge click
                        val scheduleIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_WIDGET_ACTION, ACTION_OPEN_SCHEDULE)
                        }
                        val schedulePendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 6,
                            scheduleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_workout_badge, schedulePendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update workout widget", e)
            }
        }
    }

    fun updateHydrationWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GymFlowHydrationWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val weightDao = entryPoint.weightDao()
                val tokenManager = entryPoint.tokenManager()

                val memberId = tokenManager.getUserIdSync() ?: "default_member"
                val today = LocalDate.now()
                val startOfDayMs = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val todayTotalMl = weightDao.getTodayHydrationTotalSync(memberId, startOfDayMs) ?: 0
                val goalMl = 2500
                val percent = ((todayTotalMl.toFloat() / goalMl.toFloat()) * 100).toInt().coerceIn(0, 100)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_hydration_layout).apply {
                        setTextViewText(R.id.widget_hydration_count, "%,d".format(todayTotalMl))
                        setTextViewText(R.id.widget_hydration_goal, "/ %,d ml".format(goalMl))
                        setTextViewText(R.id.widget_hydration_percent, "$percent%")
                        setProgressBar(R.id.widget_hydration_progress, 100, percent, false)

                        val status = when {
                            percent >= 100 -> "Daily hydration goal achieved! Keep sipping ✓"
                            percent >= 50 -> "Over halfway to your hydration goal. Stay refreshed!"
                            else -> "Tap +250ml or +500ml below to log water instantly"
                        }
                        setTextViewText(R.id.widget_hydration_status, status)

                        // Main widget click -> open Hydration screen
                        val mainIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_WIDGET_ACTION, ACTION_OPEN_HYDRATION)
                        }
                        val mainPendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId * 10 + 7,
                            mainIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_hydration_root, mainPendingIntent)

                        // +250ml quick-log broadcast
                        val add250Intent = Intent(context, GymFlowHydrationWidgetProvider::class.java).apply {
                            action = ACTION_LOG_WATER_250
                        }
                        val add250PendingIntent = PendingIntent.getBroadcast(
                            context,
                            appWidgetId * 10 + 8,
                            add250Intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_btn_water_250, add250PendingIntent)

                        // +500ml quick-log broadcast
                        val add500Intent = Intent(context, GymFlowHydrationWidgetProvider::class.java).apply {
                            action = ACTION_LOG_WATER_500
                        }
                        val add500PendingIntent = PendingIntent.getBroadcast(
                            context,
                            appWidgetId * 10 + 9,
                            add500Intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        setOnClickPendingIntent(R.id.widget_btn_water_500, add500PendingIntent)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update hydration widget", e)
            }
        }
    }

    fun updateQuickActionWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GymFlowQuickActionWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_action_layout).apply {
                // 1. Run
                val runIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ACTION, ACTION_START_RUN)
                }
                val runPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 20 + 1,
                    runIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_dock_run, runPendingIntent)

                // 2. Walk
                val walkIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ACTION, ACTION_START_WALK)
                }
                val walkPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 20 + 2,
                    walkIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_dock_walk, walkPendingIntent)

                // 3. Workout
                val workoutIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ACTION, ACTION_START_WORKOUT)
                }
                val workoutPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 20 + 3,
                    workoutIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_dock_workout, workoutPendingIntent)

                // 4. Hydration
                val waterIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ACTION, ACTION_OPEN_HYDRATION)
                }
                val waterPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 20 + 4,
                    waterIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_dock_water, waterPendingIntent)

                // 5. Timer / Stopwatch
                val timerIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_WIDGET_ACTION, ACTION_OPEN_STOPWATCH)
                }
                val timerPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 20 + 5,
                    timerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_dock_timer, timerPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
