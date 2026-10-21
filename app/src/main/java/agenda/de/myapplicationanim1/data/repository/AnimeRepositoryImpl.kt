package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.JikanApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val jikanApiService: JikanApiService = RetrofitClient.jikanApiService,
    private val bangumiApiService: BangumiApiService = RetrofitClient.bangumiApiService,
) : AnimeRepository {

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var animeList: List<Anime>? = null

        // 1. Try Bangumi API CDN
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                val mapped = items.map { it.toDomain() }
                if (mapped.isNotEmpty()) {
                    animeList = mapped
                }
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Bangumi CDN failed or timed out: ${e.message}")
        }

        // 2. Fallback to Jikan API if Bangumi failed or returned empty
        if (animeList.isNullOrEmpty()) {
            try {
                val jikanResponse = jikanApiService.getCurrentSeasonAnime()
                if (jikanResponse.isSuccessful) {
                    val dtoList = jikanResponse.body()?.data ?: emptyList()
                    val mapped = dtoList.map { it.toDomain() }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                Log.w("AnimeRepository", "Jikan API failed or timed out: ${e.message}")
            }
        }

        if (!animeList.isNullOrEmpty()) {
            var list = animeList
            // Strictly filter out animes that are NOT currently releasing
            list = list.filter { anime ->
                anime.status.equals("RELEASING", ignoreCase = true) ||
                        (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L)
            }

            if (!filter.isNullOrBlank()) {
                val normalizedFilter = filter.trim().lowercase()
                list = list.filter { anime ->
                    val airingDay = if (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L) {
                        agenda.de.myapplicationanim1.data.model.getDayOfWeekFromTimestamp(anime.nextAiringEpisode.airingAt)
                    } else {
                        anime.airingDay
                    }
                    airingDay.lowercase() == normalizedFilter
                }
            }
            emit(Resource.Success(list))
        } else {
            emit(Resource.Error("Falha na conexão. Verifique sua internet e tente novamente."))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = jikanApiService.getCurrentSeasonAnime()
            if (response.isSuccessful) {
                val dtoList = response.body()?.data ?: emptyList()
                val animeList = dtoList.map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro ao carregar animes populares: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erro na conexão: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = jikanApiService.searchAnime(query = query)
            if (response.isSuccessful) {
                val dtoList = response.body()?.data ?: emptyList()
                val animeList = dtoList.map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na busca: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = jikanApiService.getAnimeDetails(id)
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    emit(Resource.Success(dto.toDomain()))
                } else {
                    emit(Resource.Error("Detalhes do anime não encontrados"))
                }
            } else {
                emit(Resource.Error("Erro ao carregar detalhes: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
