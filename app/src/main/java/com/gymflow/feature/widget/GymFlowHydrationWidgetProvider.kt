package com.gymflow.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gymflow.core.database.HydrationLogEntity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class GymFlowHydrationWidgetProvider : AppWidgetProvider() {

    private val providerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GymFlowWidgetUpdater.updateHydrationWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        GymFlowWidgetUpdater.updateHydrationWidget(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == GymFlowWidgetUpdater.ACTION_LOG_WATER_250 || action == GymFlowWidgetUpdater.ACTION_LOG_WATER_500) {
            val amountMl = if (action == GymFlowWidgetUpdater.ACTION_LOG_WATER_250) 250 else 500
            val pendingResult = goAsync()
            providerScope.launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        WidgetEntryPoint::class.java
                    )
                    val weightDao = entryPoint.weightDao()
                    val tokenManager = entryPoint.tokenManager()
                    val memberId = tokenManager.getUserIdSync() ?: "default_member"

                    val entity = HydrationLogEntity(
                        id = UUID.randomUUID().toString(),
                        memberId = memberId,
                        amountMl = amountMl,
                        dailyGoalMl = 2500,
                        recordedAtEpochMs = System.currentTimeMillis(),
                        isSynced = false
                    )
                    weightDao.upsertHydrationLog(entity)
                    GymFlowWidgetUpdater.updateHydrationWidget(context)
                } catch (e: Exception) {
                    Log.e("GymFlowHydrationWidget", "Failed to quick log water from widget", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }
        super.onReceive(context, intent)
    }
}

