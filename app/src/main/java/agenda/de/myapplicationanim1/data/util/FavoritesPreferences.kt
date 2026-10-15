package agenda.de.myapplicationanim1.data.util

import android.content.Context
import android.content.SharedPreferences
import agenda.de.myapplicationanim1.data.model.Anime
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

data class FavoriteAnime(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val score: Double = 0.0,
    val status: String = "",
    val totalEpisodes: Int = 0,
    val addedAtMillis: Long = System.currentTimeMillis()
)

data class FavoriteEpisode(
    val id: String,
    val animeId: Int,
    val animeTitle: String,
    val episodeNumber: Int,
    val episodeTitle: String = "",
    val imageUrl: String = "",
    val comment: String = "",
    val addedAtMillis: Long = System.currentTimeMillis()
)

class FavoritesPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anime_favorites_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val animeListType = Types.newParameterizedType(List::class.java, FavoriteAnime::class.java)
    private val animeAdapter = moshi.adapter<List<FavoriteAnime>>(animeListType)

    private val episodeListType = Types.newParameterizedType(List::class.java, FavoriteEpisode::class.java)
    private val episodeAdapter = moshi.adapter<List<FavoriteEpisode>>(episodeListType)

    private val backupFile = File(context.filesDir, "anime_favorites_backup.json")

    init {
        checkAndRestoreAutoBackup()
    }

    private fun checkAndRestoreAutoBackup() {
        val currentJson = prefs.getString(KEY_FAVORITE_ANIMES, "[]") ?: "[]"
        if (currentJson == "[]" && backupFile.exists()) {
            try {
                val fileContent = backupFile.readText()
                if (fileContent.isNotBlank()) {
                    prefs.edit().putString(KEY_FAVORITE_ANIMES, fileContent).apply()
                }
            } catch (_: Exception) {}
        }
    }

    private fun saveAnimes(current: List<FavoriteAnime>) {
        val json = animeAdapter.toJson(current)
        prefs.edit().putString(KEY_FAVORITE_ANIMES, json).apply()
        try {
            backupFile.writeText(json)
        } catch (_: Exception) {}
    }

    private fun saveEpisodes(current: List<FavoriteEpisode>) {
        val json = episodeAdapter.toJson(current)
        prefs.edit().putString(KEY_FAVORITE_EPISODES, json).apply()
    }

    fun getFavoriteAnimes(): List<FavoriteAnime> {
        val json = prefs.getString(KEY_FAVORITE_ANIMES, "[]") ?: "[]"
        return try {
            animeAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isAnimeFavorite(animeId: Int): Boolean {
        return getFavoriteAnimes().any { it.id == animeId }
    }

    fun toggleAnimeFavorite(anime: Anime): Boolean {
        val current = getFavoriteAnimes().toMutableList()
        val existing = current.find { it.id == anime.id }
        val isNowFavorite: Boolean

        if (existing != null) {
            current.remove(existing)
            isNowFavorite = false
        } else {
            current.add(
                0,
                FavoriteAnime(
                    id = anime.id,
                    title = anime.title,
                    imageUrl = anime.imageUrl.ifEmpty { anime.largeImageUrl },
                    score = anime.score,
                    status = anime.status,
                    totalEpisodes = anime.episodes
                )
            )
            isNowFavorite = true
        }

        saveAnimes(current)
        return isNowFavorite
    }

    fun toggleAnimeFavoriteById(id: Int, title: String, imageUrl: String, score: Double = 0.0): Boolean {
        val current = getFavoriteAnimes().toMutableList()
        val existing = current.find { it.id == id }
        val isNowFavorite: Boolean

        if (existing != null) {
            current.remove(existing)
            isNowFavorite = false
        } else {
            current.add(
                0,
                FavoriteAnime(
                    id = id,
                    title = title,
                    imageUrl = imageUrl,
                    score = score
                )
            )
            isNowFavorite = true
        }

        saveAnimes(current)
        return isNowFavorite
    }

    fun getFavoriteEpisodes(): List<FavoriteEpisode> {
        val json = prefs.getString(KEY_FAVORITE_EPISODES, "[]") ?: "[]"
        return try {
            episodeAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isEpisodeFavorite(animeId: Int, episodeNumber: Int): Boolean {
        val targetId = "${animeId}_ep_${episodeNumber}"
        return getFavoriteEpisodes().any { it.id == targetId }
    }

    fun toggleEpisodeFavorite(
        animeId: Int,
        animeTitle: String,
        episodeNumber: Int,
        episodeTitle: String = "",
        imageUrl: String = ""
    ): Boolean {
        val current = getFavoriteEpisodes().toMutableList()
        val targetId = "${animeId}_ep_${episodeNumber}"
        val existing = current.find { it.id == targetId }
        val isNowFavorite: Boolean

        if (existing != null) {
            current.remove(existing)
            isNowFavorite = false
        } else {
            current.add(
                0,
                FavoriteEpisode(
                    id = targetId,
                    animeId = animeId,
                    animeTitle = animeTitle,
                    episodeNumber = episodeNumber,
                    episodeTitle = episodeTitle.ifEmpty { "Episódio $episodeNumber" },
                    imageUrl = imageUrl
                )
            )
            isNowFavorite = true
        }

        saveEpisodes(current)
        return isNowFavorite
    }

    fun updateEpisodeComment(animeId: Int, episodeNumber: Int, newComment: String) {
        val current = getFavoriteEpisodes().toMutableList()
        val targetId = "${animeId}_ep_${episodeNumber}"
        val index = current.indexOfFirst { it.id == targetId }
        if (index != -1) {
            current[index] = current[index].copy(comment = newComment)
            saveEpisodes(current)
        }
    }

    companion object {
        private const val KEY_FAVORITE_ANIMES = "favorite_animes_json"
        private const val KEY_FAVORITE_EPISODES = "favorite_episodes_json"
    }
}
