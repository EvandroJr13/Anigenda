package agenda.de.myapplicationanim1.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import agenda.de.myapplicationanim1.data.util.FavoriteAnime
import agenda.de.myapplicationanim1.data.util.FavoriteEpisode
import agenda.de.myapplicationanim1.data.util.FavoritesPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = FavoritesPreferences(application)

    private val _favoriteAnimes = MutableStateFlow<List<FavoriteAnime>>(emptyList())
    val favoriteAnimes: StateFlow<List<FavoriteAnime>> = _favoriteAnimes.asStateFlow()

    private val _favoriteEpisodes = MutableStateFlow<List<FavoriteEpisode>>(emptyList())
    val favoriteEpisodes: StateFlow<List<FavoriteEpisode>> = _favoriteEpisodes.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _favoriteAnimes.value = prefs.getFavoriteAnimes()
        _favoriteEpisodes.value = prefs.getFavoriteEpisodes()
    }

    fun removeFavoriteAnime(animeId: Int) {
        val current = prefs.getFavoriteAnimes().find { it.id == animeId }
        if (current != null) {
            prefs.toggleAnimeFavoriteById(current.id, current.title, current.imageUrl, current.score)
            loadFavorites()
        }
    }

    fun removeFavoriteEpisode(animeId: Int, episodeNumber: Int) {
        val current = prefs.getFavoriteEpisodes().find { it.animeId == animeId && it.episodeNumber == episodeNumber }
        if (current != null) {
            prefs.toggleEpisodeFavorite(
                animeId = current.animeId,
                animeTitle = current.animeTitle,
                episodeNumber = current.episodeNumber,
                episodeTitle = current.episodeTitle,
                imageUrl = current.imageUrl
            )
            loadFavorites()
        }
    }

    fun updateEpisodeComment(animeId: Int, episodeNumber: Int, comment: String) {
        prefs.updateEpisodeComment(animeId, episodeNumber, comment)
        loadFavorites()
    }
}
