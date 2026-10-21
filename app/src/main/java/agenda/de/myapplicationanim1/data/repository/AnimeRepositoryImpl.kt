package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.isMatureContent
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
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

    private companion object {
        const val MEDIA_FIELDS = """
            id
            title {
              english
              romaji
              native
            }
            coverImage {
              extraLarge
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

        // 1. Cross-reference and validate with AniList API (status: RELEASING, isAdult: false)
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
                val mapped = mediaList
                    .filter { it.isAdult != true && it.status.equals("RELEASING", ignoreCase = true) }
                    .map { it.toDomain() }
                if (mapped.isNotEmpty()) {
                    animeList = mapped
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "AniList validation failed: ${e.message}", e)
        }

        // 2. Fallback to Bangumi Data if AniList failed or returned empty
        if (animeList.isNullOrEmpty()) {
            try {
                val response = bangumiApiService.getItems()
                if (response.isSuccessful) {
                    val items = response.body()?.items ?: emptyList()
                    val nonMatureItems = items.filter { !it.isMatureContent() }
                    val mapped = nonMatureItems.map { it.toDomain() }
                    if (mapped.isNotEmpty()) {
                        animeList = mapped
                    }
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "Bangumi fallback failed: ${e.message}", e)
            }
        }

        if (!animeList.isNullOrEmpty()) {
            var list = animeList
            // Filter by streaming platforms popular in BR/USA if external links are present
            list = list.filter { anime ->
                if (anime.externalLinks.isEmpty()) {
                    true
                } else {
                    anime.externalLinks.any { link ->
                        val site = link.site.lowercase()
                        allowedPlatforms.any { allowed -> site.contains(allowed.lowercase()) }
                    }
                }
            }

            if (!filter.isNullOrBlank()) {
                val normalizedFilter = filter.trim().lowercase()
                list = list.filter { it.airingDay.lowercase() == normalizedFilter }
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
                val animeList = mediaList.filter { it.isAdult != true && it.status.equals("RELEASING", ignoreCase = true) }.map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na API AniList: ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro populares: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar animes populares: ${e.localizedMessage ?: e.message}", e))
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
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
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
                emit(Resource.Error("Erro ao carregar detalhes"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
