package agenda.de.myapplicationanim1.data.util

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class BackupManager(private val context: Context) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val animeListType = Types.newParameterizedType(List::class.java, FavoriteAnime::class.java)
    private val episodeListType = Types.newParameterizedType(List::class.java, FavoriteEpisode::class.java)

    private val animeListAdapter = moshi.adapter<List<FavoriteAnime>>(animeListType)
    private val episodeListAdapter = moshi.adapter<List<FavoriteEpisode>>(episodeListType)

    fun exportBackupJson(): String {
        val favoritesPrefs = FavoritesPreferences(context)
        val animes = favoritesPrefs.getFavoriteAnimes()
        val episodes = favoritesPrefs.getFavoriteEpisodes()

        val data = mapOf(
            "favoriteAnimes" to animes,
            "favoriteEpisodes" to episodes
        )
        val mapAdapter = moshi.adapter(Map::class.java)
        return mapAdapter.toJson(data)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val mapAdapter = moshi.adapter(Map::class.java)
            val data = mapAdapter.fromJson(jsonString) as? Map<*, *> ?: return false

            val animesList = data["favoriteAnimes"] as? List<*> ?: emptyList<Any>()
            val episodesList = data["favoriteEpisodes"] as? List<*> ?: emptyList<Any>()

            val animesJson = animeListAdapter.toJson(animesList.filterIsInstance<FavoriteAnime>())
            val episodesJson = episodeListAdapter.toJson(episodesList.filterIsInstance<FavoriteEpisode>())

            val prefs = context.getSharedPreferences("anime_favorites_prefs", Context.MODE_PRIVATE)
            prefs.edit {
                putString("favorite_animes_json", animesJson)
                putString("favorite_episodes_json", episodesJson)
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
