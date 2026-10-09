package agenda.de.myapplicationanim1.data.util

import android.content.Context
import android.content.SharedPreferences
import agenda.de.myapplicationanim1.data.model.Anime

data class NotifiedAnime(
    val id: Int,
    val title: String,
    val imageUrl: String = ""
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
            enabled = enabled
        )
    }

    fun setAnimeNotificationsEnabled(animeId: Int, title: String = "", imageUrl: String = "", enabled: Boolean) {
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
        } else {
            bookmarkedIds.remove(idStr)
            editor.remove(KEY_TITLE_PREFIX + idStr)
            editor.remove(KEY_IMAGE_PREFIX + idStr)
        }
        editor.putStringSet(KEY_BOOKMARKED_ANIME_IDS, bookmarkedIds).apply()
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
    }
}
