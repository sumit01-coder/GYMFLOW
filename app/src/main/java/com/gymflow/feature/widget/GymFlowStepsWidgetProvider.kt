package com.gymflow.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class GymFlowStepsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GymFlowWidgetUpdater.updateStepsWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        GymFlowWidgetUpdater.updateStepsWidget(context)
    }
}

