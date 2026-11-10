package com.gymflow.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class GymFlowWorkoutWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GymFlowWidgetUpdater.updateWorkoutWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        GymFlowWidgetUpdater.updateWorkoutWidget(context)
    }
}

