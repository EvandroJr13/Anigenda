package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.AiAnimeGenerator
import agenda.de.myapplicationanim1.data.util.Resource
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.time.LocalDate
import java.util.Calendar

class AnimeRepositoryImpl(
    private val apiService: AniListApiService = RetrofitClient.aniListApiService,
    private val bangumiApiService: BangumiApiService = RetrofitClient.bangumiApiService,
) : AnimeRepository {

    private companion object {
        const val MEDIA_FIELDS = """
            id
            title {
              romaji
              english
              native
            }
            coverImage {
              large
            }
            description
            averageScore
            episodes
            status
            startDate {
              year
              month
              day
            }
            nextAiringEpisode {
              airingAt
              episode
            }
            genres
            studios(isMain: true) {
              nodes {
                name
              }
            }
            trailer {
              id
              site
            }
            isAdult
            externalLinks {
              site
              url
            }
        """
    }

    private fun safeLogW(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) Log.w(tag, msg, tr) else Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] WARN: $msg ${tr?.localizedMessage ?: ""}")
        }
    }

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
            val response = bangumiApiService.getItems()

            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                var animeList = items.map { it.toDomain() }

                // Strictly filter out animes that are NOT currently releasing
                animeList = animeList.filter { anime ->
                    anime.status.equals("RELEASING", ignoreCase = true) ||
                            (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L)
                }

                // Filter locally by day of week if filter is provided (e.g., "Segunda", "Terça", etc.)
                if (!filter.isNullOrBlank()) {
                    val normalizedFilter = filter.trim().lowercase()
                    animeList = animeList.filter { anime ->
                        val airingDay = if (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L) {
                            agenda.de.myapplicationanim1.data.model.getDayOfWeekFromTimestamp(anime.nextAiringEpisode.airingAt)
                        } else {
                            anime.airingDay
                        }
                        airingDay.lowercase() == normalizedFilter
                    }
                }

                emit(Resource.Success(animeList))
            } else {
                safeLogW("AnimeRepository", "Bangumi-Data failed with code ${response.code()}, falling back to AniList")
                fetchScheduleFromAniListOrFallback(filter, this)
            }
        } catch (e: Exception) {
            safeLogW("AnimeRepository", "Bangumi-Data exception in getSchedules, falling back to AniList", e)
            try {
                fetchScheduleFromAniListOrFallback(filter, this)
            } catch (fallbackE: Exception) {
                safeLogE("AnimeRepository", "AniList fallback exception in getSchedules", fallbackE)
                emit(Resource.Error("Falha ao carregar programação. Verifique sua conexão.", fallbackE))
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchScheduleFromAniListOrFallback(
        filter: String?,
        collector: kotlinx.coroutines.flow.FlowCollector<Resource<List<Anime>>>
    ) {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val query = """
                query (${'$'}year: Int) {
                  Page(page: 1, perPage: 50) {
                    media(seasonYear: ${'$'}year, type: ANIME, status: RELEASING, sort: [POPULARITY_DESC], isAdult: false) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()

            val variables = mapOf("year" to currentYear)
            val body = mapOf("query" to query, "variables" to variables)
            val response = apiService.graphqlQuery(body)

            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                var animeList = mediaList.filter { it.isAdult != true }.map { it.toDomain() }

                // Strictly filter out animes that are NOT currently releasing
                animeList = animeList.filter { anime ->
                    anime.status.equals("RELEASING", ignoreCase = true) ||
                            (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L)
                }

                if (!filter.isNullOrBlank()) {
                    val normalizedFilter = filter.trim().lowercase()
                    animeList = animeList.filter { anime ->
                        val airingDay = if (anime.nextAiringEpisode != null && anime.nextAiringEpisode.airingAt > 0L) {
                            agenda.de.myapplicationanim1.data.model.getDayOfWeekFromTimestamp(anime.nextAiringEpisode.airingAt)
                        } else {
                            anime.airingDay
                        }
                        airingDay.lowercase() == normalizedFilter
                    }
                }

                collector.emit(Resource.Success(animeList))
            } else {
                safeLogW("AnimeRepository", "AniList fallback failed with code ${response.code()}")
                collector.emit(Resource.Success(emptyList()))
            }
        } catch (e: Exception) {
            safeLogW("AnimeRepository", "AniList fallback exception", e)
            collector.emit(Resource.Success(emptyList()))
        }
    }

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val query = """
                query (${'$'}year: Int) {
                  Page(page: 1, perPage: 24) {
                    media(seasonYear: ${'$'}year, type: ANIME, sort: [POPULARITY_DESC, SCORE_DESC], isAdult: false) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()

            val variables = mapOf("year" to currentYear)
            val body = mapOf("query" to query, "variables" to variables)
            val response = apiService.graphqlQuery(body)

            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                val animeList = mediaList.filter { it.isAdult != true }.map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                val errorMsg = "Erro na API AniList: ${response.code()} ${response.message()}"
                Log.e("AnimeRepository", "getPopularAnime error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: JsonDataException) {
            Log.e("AnimeRepository", "JSON Exception in getPopularAnime", e)
            emit(Resource.Error("Erro ao processar dados (JSON): ${e.localizedMessage}", e))
        } catch (e: IOException) {
            Log.e("AnimeRepository", "IOException in getPopularAnime", e)
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Exception in getPopularAnime", e)
            emit(Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val combinedList = coroutineScope {
                val anilistDeferred = async(Dispatchers.IO) {
                    try {
                        val gqlQuery = """
                            query (${'$'}search: String, ${'$'}genre: String, ${'$'}page: Int, ${'$'}perPage: Int) {
                              Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                                media(search: ${'$'}search, genre: ${'$'}genre, type: ANIME, sort: [POPULARITY_DESC], isAdult: false) {
                                  $MEDIA_FIELDS
                                }
                              }
                            }
                        """.trimIndent()

                        val variables = mutableMapOf<String, Any>("page" to 1, "perPage" to 50)
                        if (query.isNotBlank()) variables["search"] = query
                        if (!genre.isNullOrBlank()) variables["genre"] = genre

                        val body = mapOf("query" to gqlQuery, "variables" to variables)
                        val response = apiService.graphqlQuery(body)
                        if (response.isSuccessful) {
                            response.body()?.data?.page?.media?.filter { it.isAdult != true }?.map { it.toDomain() } ?: emptyList()
                        } else emptyList()
                    } catch (_: Exception) { emptyList() }
                }

                val jikanDeferred = async(Dispatchers.IO) {
                    try {
                        val response = if (query.isBlank()) {
                            RetrofitClient.jikanApiService.getUpcomingAnime()
                        } else {
                            RetrofitClient.jikanApiService.searchAnime(query)
                        }
                        if (response.isSuccessful) {
                            response.body()?.data?.map { it.toDomain() } ?: emptyList()
                        } else emptyList()
                    } catch (_: Exception) { emptyList() }
                }

                val anilistResults = anilistDeferred.await()
                val jikanResults = jikanDeferred.await()

                // Merge and deduplicate by title
                (anilistResults + jikanResults).distinctBy { it.title.lowercase().trim() }
            }

            emit(Resource.Success(combinedList))
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Exception in searchAnime", e)
            emit(Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado", e))
        }
    }.flowOn(Dispatchers.IO)

    private fun getNextSeasonUrl(): String {
        val proximaData = LocalDate.now().plusMonths(3)
        val ano = proximaData.year
        val season = when (proximaData.monthValue) {
            in 1..3 -> "winter"
            in 4..6 -> "spring"
            in 7..9 -> "summer"
            else -> "fall"
        }
        return "https://api.jikan.moe/v4/seasons/$ano/$season"
    }

    override fun getUpcomingAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            // 1. Consulta AniList GraphQL com filtro para animes com lançamento a partir de 2027/01 (startDate_greater: 20261231)
            val gqlQuery = """
                query (${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(startDate_greater: 20261231, type: ANIME, sort: [START_DATE, POPULARITY_DESC], isAdult: false) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()

            val body = mapOf("query" to gqlQuery, "variables" to mapOf("page" to 1, "perPage" to 50))
            val response = apiService.graphqlQuery(body)

            var list = emptyList<Anime>()
            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                list = mediaList.filter { it.isAdult != true }.map { it.toDomain().copy(status = "UPCOMING", airingDay = "2027+") }
            }

            // 2. Se AniList não retornar suficiente, consulta Jikan API MyAnimeList para Winter 2027 (Janeiro de 2027)
            if (list.isEmpty()) {
                val jikanRes = RetrofitClient.jikanApiService.getSeasonalAnime(2027, "winter")
                if (jikanRes.isSuccessful) {
                    list = jikanRes.body()?.data?.map { it.toDomain().copy(status = "UPCOMING", airingDay = "2027+") } ?: emptyList()
                }
            }

            // 3. Garante rigorosamente que apenas animes a partir de 2027/01 sejam incluídos
            val filtered2027List = list.filter { anime ->
                val yr = anime.year ?: 0
                yr >= 2027 || anime.aired.contains("2027") || anime.aired.contains("2028")
            }

            emit(Resource.Success(filtered2027List))
        } catch (e: Exception) {
            safeLogW("AnimeRepository", "Exception in getUpcomingAnime for 2027/01+", e)
            try {
                val jikanRes = RetrofitClient.jikanApiService.getSeasonalAnime(2027, "winter")
                if (jikanRes.isSuccessful) {
                    val jikanList = jikanRes.body()?.data?.map { it.toDomain().copy(status = "UPCOMING", airingDay = "2027+") } ?: emptyList()
                    emit(Resource.Success(jikanList))
                } else {
                    emit(Resource.Success(emptyList()))
                }
            } catch (_: Exception) {
                emit(Resource.Success(emptyList()))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val query = """
                query (${'$'}id: Int) {
                  Media(id: ${'$'}id, type: ANIME) {
                    $MEDIA_FIELDS
                  }
                }
            """.trimIndent()

            val variables = mapOf("id" to id)
            val body = mapOf("query" to query, "variables" to variables)
            val response = apiService.graphqlQuery(body)

            if (response.isSuccessful) {
                val mediaDto = response.body()?.data?.media
                if (mediaDto != null) {
                    if (mediaDto.isAdult == true) {
                        emit(Resource.Error("Conteúdo adulto restrito"))
                    } else {
                        emit(Resource.Success(mediaDto.toDomain()))
                    }
                } else {
                    emit(Resource.Error("Detalhes do anime não encontrados"))
                }
            } else {
                val errorMsg = "Erro na API AniList: ${response.code()} ${response.message()}"
                Log.e("AnimeRepository", "getAnimeDetails error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: JsonDataException) {
            Log.e("AnimeRepository", "JSON Exception in getAnimeDetails", e)
            emit(Resource.Error("Erro ao processar dados (JSON): ${e.localizedMessage}", e))
        } catch (e: IOException) {
            Log.e("AnimeRepository", "IOException in getAnimeDetails", e)
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Exception in getAnimeDetails", e)
            emit(Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado", e))
        }
    }.flowOn(Dispatchers.IO)
}
