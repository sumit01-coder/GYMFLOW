package com.gymflow.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class GymFlowQuickActionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        GymFlowWidgetUpdater.updateQuickActionWidget(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        GymFlowWidgetUpdater.updateQuickActionWidget(context)
    }
}

