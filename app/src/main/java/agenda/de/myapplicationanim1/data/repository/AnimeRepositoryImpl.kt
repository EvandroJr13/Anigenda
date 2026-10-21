package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnOtakuAnimeItem
import agenda.de.myapplicationanim1.data.model.mapPortugueseDayToEnglishFilter
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnOtakuApiService
import agenda.de.myapplicationanim1.data.remote.AnimeApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val anOtakuApiService: AnOtakuApiService = AnOtakuApiService.create(),
    private val animeApiService: AnimeApiService = AnimeApiService.create(),
    private val bangumiApiService: BangumiApiService = RetrofitClient.bangumiApiService,
) : AnimeRepository {

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] ERROR: $msg ${tr?.localizedMessage ?: ""}")
        }
    }

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var animeList: List<Anime>? = null

        // 1. Primary: AnOtaku API
        try {
            val dayEnglish = mapPortugueseDayToEnglishFilter(filter)
            val response = anOtakuApiService.getSchedule(day = dayEnglish)
            if (response.isSuccessful) {
                val rawList = response.body()?.data ?: emptyList()
                val mapped = rawList
                    .filter { it.isAdult != true }
                    .map { it.toDomain() }

                if (mapped.isNotEmpty()) {
                    animeList = if (!filter.isNullOrBlank()) {
                        val normalizedFilter = filter.trim().lowercase()
                        mapped.filter { it.airingDay.lowercase() == normalizedFilter }
                    } else mapped
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "AnOtaku API schedule failed: ${e.message}", e)
        }

        // 2. Fallback: Jikan API
        if (animeList.isNullOrEmpty()) {
            try {
                val dayEnglish = mapPortugueseDayToEnglishFilter(filter)
                val response = if (dayEnglish != null) {
                    animeApiService.getDailySchedule(day = dayEnglish, type = "tv")
                } else {
                    animeApiService.getCurrentSeasonAnime(filter = "tv")
                }
                val rawList = response.data ?: emptyList()
                val mapped = rawList
                    .filter { (it.airing == true || it.status.equals("Currently Airing", ignoreCase = true)) && !it.rating.orEmpty().contains("Rx", ignoreCase = true) }
                    .map { it.toDomain(overrideDay = filter) }

                if (mapped.isNotEmpty()) {
                    animeList = mapped
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "Jikan API fallback failed: ${e.message}", e)
            }
        }

        if (!animeList.isNullOrEmpty()) {
            emit(Resource.Success(animeList))
        } else {
            emit(Resource.Error("Erro ao carregar programação. Verifique sua conexão e tente novamente."))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var animeList: List<Anime>? = null

        // Primary: AnOtaku API
        try {
            val response = anOtakuApiService.getPopularAnime()
            if (response.isSuccessful) {
                val rawList = response.body()?.data ?: emptyList()
                val mapped = rawList
                    .filter { it.isAdult != true }
                    .map { it.toDomain() }
                if (mapped.isNotEmpty()) {
                    animeList = mapped
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "AnOtaku popular failed: ${e.message}", e)
        }

        // Fallback: Jikan API
        if (animeList.isNullOrEmpty()) {
            try {
                val response = animeApiService.getPopularAnimes(type = "tv")
                val rawList = response.data ?: emptyList()
                val mapped = rawList
                    .filter { !it.rating.orEmpty().contains("Rx", ignoreCase = true) }
                    .map { it.toDomain() }
                if (mapped.isNotEmpty()) {
                    animeList = mapped
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "Jikan popular fallback failed: ${e.message}", e)
            }
        }

        if (!animeList.isNullOrEmpty()) {
            emit(Resource.Success(animeList))
        } else {
            emit(Resource.Error("Erro ao carregar animes populares"))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = anOtakuApiService.searchAnime(query = query)
            if (response.isSuccessful) {
                val rawList = response.body()?.data ?: emptyList()
                val mapped = rawList
                    .filter { it.isAdult != true }
                    .map { it.toDomain() }
                emit(Resource.Success(mapped))
            } else {
                val fallbackResponse = animeApiService.searchAnimes(query = query, type = "tv")
                val rawList = fallbackResponse.data ?: emptyList()
                val mapped = rawList
                    .filter { !it.rating.orEmpty().contains("Rx", ignoreCase = true) }
                    .map { it.toDomain() }
                emit(Resource.Success(mapped))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = anOtakuApiService.getPopularAnime()
            if (response.isSuccessful) {
                val rawList = response.body()?.data ?: emptyList()
                val item = rawList.firstOrNull { it.id.hashCode() == id }
                if (item != null) {
                    emit(Resource.Success(item.toDomain()))
                } else {
                    val jikanResponse = animeApiService.getAnimeDetails(animeId = id)
                    val model = jikanResponse.data
                    if (model != null) {
                        emit(Resource.Success(model.toDomain()))
                    } else {
                        emit(Resource.Error("Detalhes do anime não encontrados"))
                    }
                }
            } else {
                emit(Resource.Error("Erro ao carregar detalhes"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
