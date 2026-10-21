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
              extraLarge
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

    override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var animeList: List<Anime>? = null

        // 1. Try Bangumi Data CDN
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

        // 2. Fallback to AniList GraphQL API if Bangumi failed or returned empty
        if (animeList.isNullOrEmpty()) {
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
                    val mapped = mediaList.filter { it.isAdult != true }.map { it.toDomain() }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                Log.w("AnimeRepository", "AniList GraphQL failed: ${e.message}")
            }
        }

        if (!animeList.isNullOrEmpty()) {
            var list = animeList
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
            emit(Resource.Error("Erro ao carregar programação. Verifique sua conexão e tente novamente."))
        }
    }.flowOn(Dispatchers.IO)

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
                emit(Resource.Error("Erro na API AniList: ${response.code()} ${response.message()}"))
            }
        } catch (e: JsonDataException) {
            emit(Resource.Error("Erro ao processar dados: ${e.localizedMessage}", e))
        } catch (e: IOException) {
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
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
                emit(Resource.Error("Erro na busca: ${response.code()} ${response.message()}"))
            }
        } catch (e: JsonDataException) {
            emit(Resource.Error("Erro ao processar dados: ${e.localizedMessage}", e))
        } catch (e: IOException) {
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
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
                emit(Resource.Error("Erro ao carregar detalhes: ${response.code()} ${response.message()}"))
            }
        } catch (e: JsonDataException) {
            emit(Resource.Error("Erro ao processar dados: ${e.localizedMessage}", e))
        } catch (e: IOException) {
            emit(Resource.Error("Erro de conexão. Verifique sua internet.", e))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado", e))
        }
    }.flowOn(Dispatchers.IO)
}
