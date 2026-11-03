package com.gymflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
