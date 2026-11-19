package com.gymflow.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.core.recovery.CrashRecoveryActivity
import com.gymflow.core.step.StepSensorManager
import com.gymflow.core.telemetry.BugCategory
import com.gymflow.core.telemetry.BugSeverity
import com.gymflow.core.telemetry.GymFlowBugReporter
import com.gymflow.data.sync.ExerciseGifPreloadWorker
import com.gymflow.feature.member.exercises.ExerciseVisuals
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppStepSensorEntryPoint {
    fun stepSensorManager(): StepSensorManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppSyncEntryPoint {
    fun firestoreConnectionManager(): com.gymflow.core.firebase.FirestoreConnectionManager
}

@HiltAndroidApp
class GymFlowApplication : Application(), ImageLoaderFactory {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Install global crash interception & Firestore telemetry as early as possible
        // so that any exception during Hilt injection or Application.onCreate() is intercepted
        GymFlowBugReporter.initialize(base)
    }

    override fun onCreate() {
        // If running in isolated recovery process, bypass all main-process workers & preheating
        val currentProcessName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            ""
        }
        if (currentProcessName.endsWith(":recovery")) {
            return
        }

        try {
            super.onCreate()
        } catch (e: Throwable) {
            Log.e("GymFlowApplication", "Fatal error during super.onCreate()", e)
            GymFlowBugReporter.reportBug(
                category = BugCategory.UNCAUGHT_CRASH,
                title = "Application Startup Failure: ${e.javaClass.simpleName}",
                error = e,
                severity = BugSeverity.FATAL
            )
            try {
                val recoveryIntent = CrashRecoveryActivity.createIntent(
                    context = this,
                    title = "Startup Failure: ${e.javaClass.simpleName}",
                    message = e.message ?: "An unhandled startup exception occurred.",
                    stackTrace = Log.getStackTraceString(e)
                )
                startActivity(recoveryIntent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            } catch (_: Exception) {}
            return
        }

        // 1. Enqueue background WorkManager task to pre-download and cache exercise catalog GIFs
        ExerciseGifPreloadWorker.enqueue(this)

        // 2. Schedule daily membership expiry scan & renewal notification alerts
        try {
            com.gymflow.core.worker.MembershipExpiryWorker.scheduleDaily(this)
        } catch (e: Exception) {
            Log.w("GymFlowApplication", "Could not schedule daily membership expiry worker", e)
        }

        // 3. Start pedometer sensor tracking immediately so steps are collected seamlessly
        try {
            val stepSensorEntryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this,
                AppStepSensorEntryPoint::class.java
            )
            stepSensorEntryPoint.stepSensorManager().startTracking()
        } catch (e: Exception) {
            Log.w("GymFlowApplication", "Could not start background step sensor tracking at application startup", e)
        }

        // 4. Eagerly start enterprise real-time Firestore synchronization for cross-device updates
        try {
            val syncEntryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                this,
                AppSyncEntryPoint::class.java
            )
            syncEntryPoint.firestoreConnectionManager()
        } catch (e: Exception) {
            Log.w("GymFlowApplication", "Could not initialize FirestoreConnectionManager at application startup", e)
        }

        // 3. Eagerly preheat the core workout routine GIFs in background so today's workout has 0ms load time
        CoroutineScope(Dispatchers.IO).launch {
            val loader = Coil.imageLoader(this@GymFlowApplication)
            for ((_, gid) in ExerciseVisuals.EXERCISE_GIF_IDS) {
                val url = ExerciseVisuals.BASE_GIF_URL + "$gid.gif"
                val req = ImageRequest.Builder(this@GymFlowApplication)
                    .data(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(req)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                }
                add(GifDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("exercise_visuals_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250 MB LRU disk cache for exercise GIFs
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
