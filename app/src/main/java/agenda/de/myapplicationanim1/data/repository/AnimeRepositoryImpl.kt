package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
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
            val gqlQuery = """
                query (${'$'}search: String, ${'$'}genre: String, ${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(search: ${'$'}search, genre: ${'$'}genre, type: ANIME, sort: [POPULARITY_DESC], isAdult: false) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()

            val variables = mutableMapOf<String, Any>(
                "page" to 1,
                "perPage" to 50
            )
            if (query.isNotBlank()) {
                variables["search"] = query
            }
            if (!genre.isNullOrBlank()) {
                variables["genre"] = genre
            }

            val body = mapOf("query" to gqlQuery, "variables" to variables)
            val response = apiService.graphqlQuery(body)

            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                val animeList = mediaList.filter { it.isAdult != true }.map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                val errorMsg = "Erro na API AniList: ${response.code()} ${response.message()}"
                Log.e("AnimeRepository", "searchAnime error: $errorMsg")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: JsonDataException) {
            Log.e("AnimeRepository", "JSON Exception in searchAnime", e)
            emit(Resource.Error("Erro ao processar dados (JSON): ${e.localizedMessage}", e))
        } catch (e: IOException) {
            Log.e("AnimeRepository", "IOException in searchAnime", e)
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Exception in searchAnime", e)
            emit(Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado", e))
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
