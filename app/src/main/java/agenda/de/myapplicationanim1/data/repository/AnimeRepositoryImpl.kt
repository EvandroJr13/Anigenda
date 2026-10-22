package agenda.de.myapplicationanim1.data.repository

import agenda.de.myapplicationanim1.data.local.LocalAnimeDataProvider
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl : AnimeRepository {

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        val allList = LocalAnimeDataProvider.allAnimes
        val resultList = if (!filter.isNullOrBlank()) {
            val normFilter = filter.trim().lowercase()
            allList.filter { it.airingDay.lowercase() == normFilter }
        } else {
            allList
        }
        emit(Resource.Success(resultList))
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        val popularList = LocalAnimeDataProvider.allAnimes.sortedByDescending { it.score }
        emit(Resource.Success(popularList))
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var resultList = LocalAnimeDataProvider.allAnimes

        if (query.isNotBlank()) {
            val normQuery = query.trim().lowercase()
            resultList = resultList.filter { anime ->
                anime.title.lowercase().contains(normQuery) ||
                        anime.titleEnglish.lowercase().contains(normQuery) ||
                        anime.titleRomaji.lowercase().contains(normQuery) ||
                        anime.genres.any { it.lowercase().contains(normQuery) }
            }
        }

        if (!genre.isNullOrBlank() && genre != "Todos") {
            val normGenre = genre.trim().lowercase()
            resultList = resultList.filter { anime ->
                anime.genres.any { it.lowercase().contains(normGenre) }
            }
        }

        emit(Resource.Success(resultList))
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        val anime = LocalAnimeDataProvider.allAnimes.firstOrNull { it.id == id }
        if (anime != null) {
            emit(Resource.Success(anime))
        } else {
            emit(Resource.Error("Detalhes do anime não encontrados"))
        }
    }.flowOn(Dispatchers.IO)
}
