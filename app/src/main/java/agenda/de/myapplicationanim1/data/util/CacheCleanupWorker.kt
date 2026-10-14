package agenda.de.myapplicationanim1.data.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import coil.Coil
import okhttp3.Cache
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CacheCleanupWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            // 1. Clear OkHttp network cache
            val okHttpCacheDir = File(applicationContext.cacheDir, "okhttp_cache")
            if (okHttpCacheDir.exists()) {
                try {
                    val cache = Cache(okHttpCacheDir, 50L * 1024L * 1024L)
                    cache.evictAll()
                    cache.close()
                } catch (_: Exception) {}
                okHttpCacheDir.deleteRecursively()
            }

            val networkCacheDir = File(applicationContext.cacheDir, "network_cache")
            if (networkCacheDir.exists()) {
                networkCacheDir.deleteRecursively()
            }

            // 2. Clear Coil image loader cache (memory and disk)
            try {
                val imageLoader = Coil.imageLoader(applicationContext)
                imageLoader.memoryCache?.clear()
                imageLoader.diskCache?.clear()
            } catch (_: Exception) {}

            val imageCacheDir = File(applicationContext.cacheDir, "image_cache")
            if (imageCacheDir.exists()) {
                imageCacheDir.deleteRecursively()
            }

            val coilDir = File(applicationContext.cacheDir, "coil")
            if (coilDir.exists()) {
                coilDir.deleteRecursively()
            }

            // CRITICAL: User data in SharedPreferences ("anime_notifications_prefs", "app_theme_preferences")
            // remains 100% untouched and intact because SharedPreferences are stored in /data/data/.../shared_prefs,
            // which is completely separate from cacheDir.

        } catch (_: Exception) {}

        return Result.success()
    }

    companion object {
        fun scheduleDailyCleanup(context: Context) {
            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 45)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (currentTime.after(targetTime)) {
                targetTime.add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelayMillis = targetTime.timeInMillis - currentTime.timeInMillis

            val periodicRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "CacheCleanupWork",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }
}
