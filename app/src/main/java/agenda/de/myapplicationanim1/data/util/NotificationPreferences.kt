package agenda.de.myapplicationanim1.data.util

import android.content.Context
import android.content.SharedPreferences
import agenda.de.myapplicationanim1.data.model.Anime

data class NotifiedAnime(
    val id: Int,
    val title: String,
    val imageUrl: String = ""
)

data class AnimeAlarmInfo(
    val animeId: Int,
    val title: String,
    val imageUrl: String,
    val airingDay: String,
    val broadcastTime: String,
    val nextAiringAtSec: Long?,
    val episodeNumber: Int,
    val triggerAtMillis: Long
)

class NotificationPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anime_notifications_prefs", Context.MODE_PRIVATE)

    var isGlobalNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_GLOBAL_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_GLOBAL_NOTIFICATIONS, value).apply()

    fun isAnimeNotificationsEnabled(animeId: Int): Boolean {
        val bookmarkedIds = prefs.getStringSet(KEY_BOOKMARKED_ANIME_IDS, emptySet()) ?: emptySet()
        return bookmarkedIds.contains(animeId.toString())
    }

    fun setAnimeNotificationsEnabled(animeId: Int, enabled: Boolean) {
        setAnimeNotificationsEnabled(animeId = animeId, title = "", imageUrl = "", enabled = enabled)
    }

    fun setAnimeNotificationsEnabled(anime: Anime, enabled: Boolean) {
        setAnimeNotificationsEnabled(
            animeId = anime.id,
            title = anime.title,
            imageUrl = anime.imageUrl.ifEmpty { anime.largeImageUrl },
            enabled = enabled,
            airingDay = anime.airingDay.ifEmpty { anime.broadcast.day },
            broadcastTime = anime.broadcast.time,
            nextAiringAtSec = anime.nextAiringEpisode?.airingAt,
            episodeNumber = anime.nextAiringEpisode?.episode ?: 1
        )
    }

    fun setAnimeNotificationsEnabled(
        animeId: Int,
        title: String = "",
        imageUrl: String = "",
        enabled: Boolean,
        airingDay: String = "",
        broadcastTime: String = "",
        nextAiringAtSec: Long? = null,
        episodeNumber: Int = 1
    ) {
        val bookmarkedIds = prefs.getStringSet(KEY_BOOKMARKED_ANIME_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val editor = prefs.edit()
        val idStr = animeId.toString()

        if (enabled) {
            bookmarkedIds.add(idStr)
            if (title.isNotBlank()) {
                editor.putString(KEY_TITLE_PREFIX + idStr, title)
            }
            if (imageUrl.isNotBlank()) {
                editor.putString(KEY_IMAGE_PREFIX + idStr, imageUrl)
            }
            if (airingDay.isNotBlank()) {
                editor.putString(KEY_AIRING_DAY_PREFIX + idStr, airingDay)
            }
            if (broadcastTime.isNotBlank()) {
                editor.putString(KEY_TIME_PREFIX + idStr, broadcastTime)
            }
            if (nextAiringAtSec != null) {
                editor.putLong(KEY_AIRING_AT_PREFIX + idStr, nextAiringAtSec)
            }
            editor.putInt(KEY_EPISODE_PREFIX + idStr, episodeNumber)
        } else {
            bookmarkedIds.remove(idStr)
            editor.remove(KEY_TITLE_PREFIX + idStr)
            editor.remove(KEY_IMAGE_PREFIX + idStr)
            editor.remove(KEY_AIRING_DAY_PREFIX + idStr)
            editor.remove(KEY_TIME_PREFIX + idStr)
            editor.remove(KEY_AIRING_AT_PREFIX + idStr)
            editor.remove(KEY_EPISODE_PREFIX + idStr)
            editor.remove(KEY_TRIGGER_MILLIS_PREFIX + idStr)
        }
        editor.putStringSet(KEY_BOOKMARKED_ANIME_IDS, bookmarkedIds).apply()
    }

    fun saveAnimeAlarmInfo(
        animeId: Int,
        title: String,
        imageUrl: String,
        airingDay: String,
        broadcastTime: String,
        nextAiringAtSec: Long?,
        episodeNumber: Int,
        triggerAtMillis: Long
    ) {
        val editor = prefs.edit()
        val idStr = animeId.toString()
        if (title.isNotBlank()) editor.putString(KEY_TITLE_PREFIX + idStr, title)
        if (imageUrl.isNotBlank()) editor.putString(KEY_IMAGE_PREFIX + idStr, imageUrl)
        if (airingDay.isNotBlank()) editor.putString(KEY_AIRING_DAY_PREFIX + idStr, airingDay)
        if (broadcastTime.isNotBlank()) editor.putString(KEY_TIME_PREFIX + idStr, broadcastTime)
        if (nextAiringAtSec != null) editor.putLong(KEY_AIRING_AT_PREFIX + idStr, nextAiringAtSec)
        editor.putInt(KEY_EPISODE_PREFIX + idStr, episodeNumber)
        editor.putLong(KEY_TRIGGER_MILLIS_PREFIX + idStr, triggerAtMillis)
        editor.apply()
    }

    fun getAnimeAlarmInfo(animeId: Int): AnimeAlarmInfo? {
        val idStr = animeId.toString()
        val bookmarkedIds = prefs.getStringSet(KEY_BOOKMARKED_ANIME_IDS, emptySet()) ?: emptySet()
        if (!bookmarkedIds.contains(idStr)) return null

        val title = prefs.getString(KEY_TITLE_PREFIX + idStr, "")?.takeIf { it.isNotBlank() } ?: "Anime #$animeId"
        val imageUrl = prefs.getString(KEY_IMAGE_PREFIX + idStr, "") ?: ""
        val airingDay = prefs.getString(KEY_AIRING_DAY_PREFIX + idStr, "") ?: ""
        val broadcastTime = prefs.getString(KEY_TIME_PREFIX + idStr, "") ?: ""
        val nextAiringAtSec = if (prefs.contains(KEY_AIRING_AT_PREFIX + idStr)) prefs.getLong(KEY_AIRING_AT_PREFIX + idStr, 0L) else null
        val episodeNumber = prefs.getInt(KEY_EPISODE_PREFIX + idStr, 1)
        val triggerAtMillis = prefs.getLong(KEY_TRIGGER_MILLIS_PREFIX + idStr, 0L)

        return AnimeAlarmInfo(
            animeId = animeId,
            title = title,
            imageUrl = imageUrl,
            airingDay = airingDay,
            broadcastTime = broadcastTime,
            nextAiringAtSec = nextAiringAtSec,
            episodeNumber = episodeNumber,
            triggerAtMillis = triggerAtMillis
        )
    }

    fun getBookmarkedAnimeIds(): Set<Int> {
        val bookmarkedIds = prefs.getStringSet(KEY_BOOKMARKED_ANIME_IDS, emptySet()) ?: emptySet()
        return bookmarkedIds.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun getNotifiedAnimes(): List<NotifiedAnime> {
        val ids = getBookmarkedAnimeIds()
        return ids.map { id ->
            val idStr = id.toString()
            val title = prefs.getString(KEY_TITLE_PREFIX + idStr, "")?.takeIf { it.isNotBlank() } ?: "Anime #$id"
            val imageUrl = prefs.getString(KEY_IMAGE_PREFIX + idStr, "") ?: ""
            NotifiedAnime(id = id, title = title, imageUrl = imageUrl)
        }
    }

    companion object {
        private const val KEY_GLOBAL_NOTIFICATIONS = "key_global_notifications"
        private const val KEY_BOOKMARKED_ANIME_IDS = "key_bookmarked_anime_ids"
        private const val KEY_TITLE_PREFIX = "key_title_"
        private const val KEY_IMAGE_PREFIX = "key_image_"
        private const val KEY_AIRING_DAY_PREFIX = "key_airing_day_"
        private const val KEY_TIME_PREFIX = "key_time_"
        private const val KEY_AIRING_AT_PREFIX = "key_airing_at_"
        private const val KEY_EPISODE_PREFIX = "key_episode_"
        private const val KEY_TRIGGER_MILLIS_PREFIX = "key_trigger_millis_"
    }
}
