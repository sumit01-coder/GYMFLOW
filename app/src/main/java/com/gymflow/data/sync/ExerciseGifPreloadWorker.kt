package com.gymflow.data.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil.Coil
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.core.database.ExerciseDao
import com.gymflow.feature.member.exercises.ExerciseVisuals
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseGifPreloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "exercise_gif_preload_work"
        private const val TAG = "ExerciseGifPreload"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ExerciseGifPreloadWorker>()
                .setConstraints(constraints)
                .addTag("exercise_gif_preload")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PreloadWorkerEntryPoint {
        fun exerciseDao(): ExerciseDao
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                PreloadWorkerEntryPoint::class.java
            )
            val exerciseDao = entryPoint.exerciseDao()
            val imageLoader = Coil.imageLoader(applicationContext)

            // 1. Priority 1: Preload all 43 Core Workout routine exercise GIFs
            for ((_, gid) in ExerciseVisuals.EXERCISE_GIF_IDS) {
                val url = ExerciseVisuals.BASE_GIF_URL + "$gid.gif"
                try {
                    val req = ImageRequest.Builder(applicationContext)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.execute(req)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed preloading core GIF: $url - ${e.message}")
                }
            }

            // 2. Priority 2: Preload catalog exercises with stored gifUrls
            val exercises = exerciseDao.getExercisesWithGifs()
            for (ex in exercises) {
                val url = ex.gifUrl ?: continue
                try {
                    val req = ImageRequest.Builder(applicationContext)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.execute(req)
                } catch (_: Exception) {
                    // Non-fatal, continue preloading remaining catalog GIFs
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Preload worker failed", e)
            Result.retry()
        }
    }
}

