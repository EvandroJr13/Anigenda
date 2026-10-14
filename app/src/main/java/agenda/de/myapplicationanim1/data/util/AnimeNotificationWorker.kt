package agenda.de.myapplicationanim1.data.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AnimeNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val animeId = inputData.getInt(EXTRA_ANIME_ID, -1)
        val animeTitle = inputData.getString(EXTRA_ANIME_TITLE) ?: "Anime"
        val episodeNumber = inputData.getInt(EXTRA_EPISODE_NUMBER, 1)
        val airingDay = inputData.getString(EXTRA_AIRING_DAY) ?: ""
        val broadcastTime = inputData.getString(EXTRA_BROADCAST_TIME) ?: ""

        val notifPrefs = NotificationPreferences(applicationContext)
        if (!notifPrefs.isGlobalNotificationsEnabled) return Result.success()
        if (animeId != -1 && !notifPrefs.isAnimeNotificationsEnabled(animeId)) return Result.success()

        AnimeNotificationManager.showReleaseNotification(
            context = applicationContext,
            animeId = if (animeId != -1) animeId else 1000,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber
        )

        if (animeId != -1) {
            val nextEpisode = episodeNumber + 1
            AnimeAlarmScheduler.scheduleAlarm(
                context = applicationContext,
                animeId = animeId,
                animeTitle = animeTitle,
                airingDay = airingDay,
                broadcastTime = broadcastTime,
                nextAiringAtSec = null,
                episodeNumber = nextEpisode
            )
        }
        return Result.success()
    }

    companion object {
        const val EXTRA_ANIME_ID = "anime_id"
        const val EXTRA_ANIME_TITLE = "anime_title"
        const val EXTRA_EPISODE_NUMBER = "episode_number"
        const val EXTRA_AIRING_DAY = "airing_day"
        const val EXTRA_BROADCAST_TIME = "broadcast_time"
    }
}
