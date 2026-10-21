package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnimeModel
import agenda.de.myapplicationanim1.data.model.isMatureContent
import agenda.de.myapplicationanim1.data.model.mapPortugueseDayToEnglishFilter
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnimeApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.MalApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val animeApiService: AnimeApiService = AnimeApiService.create(),
    private val malApiService: MalApiService = MalApiService.create(),
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

        // Tier 1: Try Jikan API (MAL open API)
        try {
            val dayEnglish = mapPortugueseDayToEnglishFilter(filter)
            val response = if (dayEnglish != null) {
                animeApiService.getDailySchedule(day = dayEnglish, sfw = true)
            } else {
                animeApiService.getCurrentSeasonAnime(sfw = true)
            }
            val rawList = response.data ?: emptyList()
            val filtered = processAndFilterAnimes(rawList, filter)
            if (filtered.isNotEmpty()) {
                animeList = filtered
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Jikan API schedule failed: ${e.message}", e)
        }

        // Tier 2: Fallback to MAL API v2 if Jikan failed
        if (animeList.isNullOrEmpty()) {
            try {
                val malResponse = malApiService.getAiringAnimeRanking(limit = 50)
                if (malResponse.isSuccessful) {
                    val rankingItems = malResponse.body()?.data ?: emptyList()
                    var mapped = rankingItems
                        .mapNotNull { it.node }
                        .filter { it.rating != "rx" && (it.status == "currently_airing" || it.status == "RELEASING") }
                        .map { it.toDomain() }

                    if (!filter.isNullOrBlank()) {
                        val normalizedFilter = filter.trim().lowercase()
                        mapped = mapped.filter { it.airingDay.lowercase() == normalizedFilter }
                    }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "MAL API v2 schedule failed: ${e.message}", e)
            }
        }

        // Tier 3: Fallback to Bangumi Data CDN if both MAL options failed
        if (animeList.isNullOrEmpty()) {
            try {
                val bangumiResponse = bangumiApiService.getItems()
                if (bangumiResponse.isSuccessful) {
                    val items = bangumiResponse.body()?.items ?: emptyList()
                    var mapped = items.filter { !it.isMatureContent() }.map { it.toDomain() }
                    if (!filter.isNullOrBlank()) {
                        val normalizedFilter = filter.trim().lowercase()
                        mapped = mapped.filter { it.airingDay.lowercase() == normalizedFilter }
                    }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "Bangumi CDN fallback failed: ${e.message}", e)
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

        // Tier 1: Try Jikan API
        try {
            val response = animeApiService.getPopularAnimes(sfw = true)
            val rawList = response.data ?: emptyList()
            val filtered = processAndFilterAnimes(rawList)
            if (filtered.isNotEmpty()) {
                animeList = filtered
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Jikan popular failed: ${e.message}", e)
        }

        // Tier 2: Fallback to MAL API v2
        if (animeList.isNullOrEmpty()) {
            try {
                val malResponse = malApiService.getAiringAnimeRanking(limit = 24)
                if (malResponse.isSuccessful) {
                    val rankingItems = malResponse.body()?.data ?: emptyList()
                    val mapped = rankingItems
                        .mapNotNull { it.node }
                        .filter { it.rating != "rx" }
                        .map { it.toDomain() }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "MAL popular failed: ${e.message}", e)
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
            val response = animeApiService.searchAnimes(query = query, sfw = true)
            val rawList = response.data ?: emptyList()
            val filteredDomainList = processAndFilterAnimes(rawList)
            emit(Resource.Success(filteredDomainList))
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca Jikan: ${e.message}", e)
            try {
                val malResponse = malApiService.searchAnime(query = query, limit = 50)
                if (malResponse.isSuccessful) {
                    val searchItems = malResponse.body()?.data ?: emptyList()
                    val animeList = searchItems
                        .mapNotNull { it.node }
                        .filter { it.rating != "rx" }
                        .map { it.toDomain() }
                    emit(Resource.Success(animeList))
                } else {
                    emit(Resource.Error("Erro na busca"))
                }
            } catch (ex: Exception) {
                emit(Resource.Error("Erro na busca: ${ex.localizedMessage ?: ex.message}", ex))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = animeApiService.getAnimeDetails(animeId = id)
            val model = response.data
            if (model != null) {
                emit(Resource.Success(model.toDomain()))
            } else {
                emit(Resource.Error("Detalhes do anime não encontrados"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes Jikan: ${e.message}", e)
            try {
                val malResponse = malApiService.getAnimeDetails(id = id)
                val malSingle = malResponse.body()
                if (malSingle?.id != null && malSingle.title != null) {
                    val node = agenda.de.myapplicationanim1.data.model.MalAnimeNode(
                        id = malSingle.id,
                        title = malSingle.title,
                        mainPicture = malSingle.mainPicture,
                        alternativeTitles = malSingle.alternativeTitles,
                        startDate = malSingle.startDate,
                        synopsis = malSingle.synopsis,
                        mean = malSingle.mean,
                        status = malSingle.status,
                        numEpisodes = malSingle.numEpisodes,
                        broadcast = malSingle.broadcast,
                        rating = malSingle.rating
                    )
                    emit(Resource.Success(node.toDomain()))
                } else {
                    emit(Resource.Error("Detalhes não encontrados"))
                }
            } catch (ex: Exception) {
                emit(Resource.Error("Erro ao carregar detalhes: ${ex.localizedMessage ?: ex.message}", ex))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun processAndFilterAnimes(
        animes: List<AnimeModel>,
        dayFilter: String? = null
    ): List<Anime> {
        val filteredResult = mutableListOf<Anime>()

        for (anime in animes) {
            val isCurrentlyAiring = anime.airing == true ||
                    anime.status.equals("Currently Airing", ignoreCase = true) ||
                    anime.status.equals("RELEASING", ignoreCase = true)
            if (!isCurrentlyAiring) continue

            val rating = anime.rating ?: ""
            val isMature = rating.contains("Rx", ignoreCase = true) ||
                    rating.contains("Hentai", ignoreCase = true)
            if (isMature) continue

            val domainAnime = anime.toDomain()
            if (dayFilter.isNullOrBlank() || domainAnime.airingDay.lowercase() == dayFilter.trim().lowercase()) {
                filteredResult.add(domainAnime)
            }
        }

        return filteredResult
    }
}
