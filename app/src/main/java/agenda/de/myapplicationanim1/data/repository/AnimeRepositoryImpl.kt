package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnimeModel
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

    private val allowedPlatforms = listOf(
        "Crunchyroll",
        "Netflix",
        "Disney+",
        "Amazon Prime Video",
        "Prime Video",
        "HBO Max",
        "Max",
        "Hulu",
        "Paramount+",
        "Apple TV+",
        "Bilibili"
    )

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.e(tag, msg, tr) else Log.e(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] ERROR: $msg ${tr?.localizedMessage ?: ""}")
        }
    }

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val dayEnglish = mapPortugueseDayToEnglishFilter(filter)
            val response = apiService.getDailySchedule(day = dayEnglish, sfw = true)
            val rawList = response.data ?: emptyList()
            val filteredDomainList = processAndFilterAnimes(rawList, filter)
            emit(Resource.Success(filteredDomainList))
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar programação: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getPopularAnimes(sfw = true)
            val rawList = response.data ?: emptyList()
            val filteredDomainList = processAndFilterAnimes(rawList)
            emit(Resource.Success(filteredDomainList))
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar animes populares: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar animes populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.searchAnimes(query = query, sfw = true)
            val rawList = response.data ?: emptyList()
            val filteredDomainList = processAndFilterAnimes(rawList)
            emit(Resource.Success(filteredDomainList))
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAnimeDetails(animeId = id)
            val model = response.data
            if (model != null) {
                var streamingList = model.streaming?.map { it.name } ?: emptyList()
                if (streamingList.isEmpty()) {
                    try {
                        val streamResponse = apiService.getAnimeStreamingPlatforms(model.id)
                        streamingList = streamResponse.data?.map { it.name } ?: emptyList()
                    } catch (_: Exception) {}
                }
                val matched = streamingList.filter { platform ->
                    allowedPlatforms.any { allowed -> platform.contains(allowed, ignoreCase = true) }
                }
                emit(Resource.Success(model.toDomain(matched)))
            } else {
                emit(Resource.Error("Detalhes do anime não encontrados"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar detalhes: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    private fun processAndFilterAnimes(
        animes: List<AnimeModel>,
        dayFilter: String? = null
    ): List<Anime> {
        val filteredResult = mutableListOf<Anime>()

        for (anime in animes) {
            // 1. Strict validation: must be airing == true or status == "Currently Airing"
            val isCurrentlyAiring = anime.airing == true ||
                    anime.status.equals("Currently Airing", ignoreCase = true) ||
                    anime.status.equals("RELEASING", ignoreCase = true)
            if (!isCurrentlyAiring) continue

            // 2. Secondary age/mature rating filter (Rx / Hentai)
            val rating = anime.rating ?: ""
            val isMature = rating.contains("Rx", ignoreCase = true) ||
                    rating.contains("Hentai", ignoreCase = true)
            if (isMature) continue

            // 3. Map streaming platforms directly from model
            val streamingList = anime.streaming?.map { it.name } ?: emptyList()
            val matchedPlatforms = streamingList.filter { platform ->
                allowedPlatforms.any { allowed -> platform.contains(allowed, ignoreCase = true) }
            }

            if (streamingList.isEmpty() || matchedPlatforms.isNotEmpty()) {
                val domainAnime = anime.toDomain(matchedPlatforms)
                if (dayFilter.isNullOrBlank() || domainAnime.airingDay.lowercase() == dayFilter.trim().lowercase()) {
                    filteredResult.add(domainAnime)
                }
            }
        }

        return filteredResult
    }
}
