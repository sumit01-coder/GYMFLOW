package com.gymflow.app

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.data.sync.ExerciseGifPreloadWorker
import com.gymflow.feature.member.exercises.ExerciseVisuals
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class GymFlowApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // 1. Enqueue background WorkManager task to pre-download and cache exercise catalog GIFs
        ExerciseGifPreloadWorker.enqueue(this)

        // 2. Eagerly preheat the core workout routine GIFs in background so today's workout has 0ms load time
        CoroutineScope(Dispatchers.IO).launch {
            val loader = newImageLoader()
            for ((_, gid) in ExerciseVisuals.EXERCISE_GIF_IDS) {
                val url = "https://raw.githubusercontent.com/omercotkd/exercises-gifs/master/assets/$gid.gif"
                val req = ImageRequest.Builder(this@GymFlowApplication)
                    .data(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
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
                } else {
                    add(GifDecoder.Factory())
                }
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
