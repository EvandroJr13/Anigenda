package agenda.de.myapplicationanim1.data.repository

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun getSchedules(filter: String? = null): Flow<Resource<List<Anime>>>
    fun getPopularAnime(): Flow<Resource<List<Anime>>>
    fun searchAnime(query: String, genre: String? = null): Flow<Resource<List<Anime>>>
    fun getAnimeDetails(id: Int): Flow<Resource<Anime>>
}
