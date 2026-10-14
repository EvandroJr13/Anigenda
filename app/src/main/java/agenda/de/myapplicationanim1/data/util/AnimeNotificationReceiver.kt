package agenda.de.myapplicationanim1.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AnimeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val animeId = intent.getIntExtra(EXTRA_ANIME_ID, -1)
        val animeTitle = intent.getStringExtra(EXTRA_ANIME_TITLE) ?: "Anime"
        val episodeNumber = intent.getIntExtra(EXTRA_EPISODE_NUMBER, 1)

        val notifPrefs = NotificationPreferences(context)
        if (!notifPrefs.isGlobalNotificationsEnabled) return
        if (animeId != -1 && !notifPrefs.isAnimeNotificationsEnabled(animeId)) return

        AnimeNotificationManager.showReleaseNotification(
            context = context,
            animeId = if (animeId != -1) animeId else 1000,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber
        )

        if (animeId != -1) {
            val airingDay = intent.getStringExtra(EXTRA_AIRING_DAY) ?: ""
            val broadcastTime = intent.getStringExtra(EXTRA_BROADCAST_TIME) ?: ""
            val nextEpisode = episodeNumber + 1
            AnimeAlarmScheduler.scheduleAlarm(
                context = context,
                animeId = animeId,
                animeTitle = animeTitle,
                airingDay = airingDay,
                broadcastTime = broadcastTime,
                nextAiringAtSec = null,
                episodeNumber = nextEpisode
            )
        }
    }

    companion object {
        const val ACTION_ANIME_RELEASE = "agenda.de.myapplicationanim1.ANIME_RELEASE_NOTIFICATION"
        const val EXTRA_ANIME_ID = "anime_id"
        const val EXTRA_ANIME_TITLE = "anime_title"
        const val EXTRA_EPISODE_NUMBER = "episode_number"
        const val EXTRA_AIRING_DAY = "airing_day"
        const val EXTRA_BROADCAST_TIME = "broadcast_time"
    }
}
