package agenda.de.myapplicationanim1.data.repository

import agenda.de.myapplicationanim1.data.local.LocalAnimeDataProvider
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.isExplicitHentai
import agenda.de.myapplicationanim1.data.model.mapPortugueseDayToEnglishFilter
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnimeApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val apiService: AnimeApiService = AnimeApiService.create()
) : AnimeRepository {

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val dayEnglish = mapPortugueseDayToEnglishFilter(filter)
            val response = if (dayEnglish != null) {
                apiService.getDailySchedule(day = dayEnglish, type = "tv")
            } else {
                apiService.getCurrentSeasonAnime()
            }
            val rawList = response.data ?: emptyList()
            val mapped = rawList
                .filter { !it.isExplicitHentai() }
                .map { it.toDomain(overrideDay = filter) }

            if (mapped.isNotEmpty()) {
                emit(Resource.Success(mapped))
            } else {
                val fallback = LocalAnimeDataProvider.allAnimes.let { list ->
                    if (!filter.isNullOrBlank()) list.filter { it.airingDay.lowercase() == filter.trim().lowercase() } else list
                }
                emit(Resource.Success(fallback))
            }
        } catch (e: Exception) {
            val fallback = LocalAnimeDataProvider.allAnimes.let { list ->
                if (!filter.isNullOrBlank()) list.filter { it.airingDay.lowercase() == filter.trim().lowercase() } else list
            }
            emit(Resource.Success(fallback))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getPopularAnimes(type = "tv")
            val rawList = response.data ?: emptyList()
            val mapped = rawList
                .filter { !it.isExplicitHentai() }
                .map { it.toDomain() }

            if (mapped.isNotEmpty()) {
                emit(Resource.Success(mapped))
            } else {
                emit(Resource.Success(LocalAnimeDataProvider.allAnimes.sortedByDescending { it.score }))
            }
        } catch (e: Exception) {
            emit(Resource.Success(LocalAnimeDataProvider.allAnimes.sortedByDescending { it.score }))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.searchAnimes(query = query, type = "tv")
            val rawList = response.data ?: emptyList()
            val mapped = rawList
                .filter { !it.isExplicitHentai() }
                .map { it.toDomain() }

            val finalResults = if (!genre.isNullOrBlank() && genre != "Todos") {
                val normGenre = genre.trim().lowercase()
                mapped.filter { anime -> anime.genres.any { it.lowercase().contains(normGenre) } }
            } else {
                mapped
            }

            if (finalResults.isNotEmpty()) {
                emit(Resource.Success(finalResults))
            } else {
                var local = LocalAnimeDataProvider.allAnimes
                if (query.isNotBlank()) {
                    val q = query.trim().lowercase()
                    local = local.filter { it.title.lowercase().contains(q) || it.titleEnglish.lowercase().contains(q) }
                }
                emit(Resource.Success(local))
            }
        } catch (e: Exception) {
            var local = LocalAnimeDataProvider.allAnimes
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                local = local.filter { it.title.lowercase().contains(q) || it.titleEnglish.lowercase().contains(q) }
            }
            emit(Resource.Success(local))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAnimeDetails(animeId = id)
            val model = response.data
            if (model != null) {
                emit(Resource.Success(model.toDomain()))
            } else {
                val local = LocalAnimeDataProvider.allAnimes.firstOrNull { it.id == id }
                if (local != null) emit(Resource.Success(local))
                else emit(Resource.Error("Detalhes não encontrados"))
            }
        } catch (e: Exception) {
            val local = LocalAnimeDataProvider.allAnimes.firstOrNull { it.id == id }
            if (local != null) emit(Resource.Success(local))
            else emit(Resource.Error("Detalhes não encontrados: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}
