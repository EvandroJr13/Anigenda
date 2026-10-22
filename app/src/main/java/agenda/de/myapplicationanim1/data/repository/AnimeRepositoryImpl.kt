package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.GraphQLPayload
import agenda.de.myapplicationanim1.data.model.getCalculatedWeekdayPt
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import java.util.Locale

class AnimeRepositoryImpl(
    private val aniListApiService: AniListApiService = AniListApiService.create()
) : AnimeRepository {

    private fun getStartDateInt3MonthsAgo(): Int {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -90)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d%02d%02d", year, month, day).toInt()
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
            val query = """
                query {
                  Page(page: 1, perPage: 50) {
                    media(status: RELEASING, type: ANIME, sort: POPULARITY_DESC) {
                      id
                      title { english romaji native }
                      coverImage { extraLarge large }
                      averageScore
                      episodes
                      genres
                      description(asHtml: false)
                      trailer { id site }
                      nextAiringEpisode { airingAt episode }
                      startDate { year month day }
                      isAdult
                      externalLinks { site url }
                    }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(query = query)
            val response = aniListApiService.getAnimesDeHoje(payload)

            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                val nonAdult = mediaList.filter { it.isAdult != true }

                val domainList = if (!filter.isNullOrBlank()) {
                    val normFilter = filter.trim().lowercase()
                    val filteredMedia = nonAdult.filter { media ->
                        val dayPt = media.getCalculatedWeekdayPt()?.lowercase()
                        dayPt == null || dayPt == normFilter
                    }
                    if (filteredMedia.isNotEmpty()) {
                        filteredMedia.map { it.toDomain(overrideDay = filter) }
                    } else {
                        nonAdult.map { it.toDomain(overrideDay = filter) }
                    }
                } else {
                    nonAdult.map { it.toDomain() }
                }

                emit(Resource.Success(domainList))
            } else {
                emit(Resource.Error("Erro na API AniList: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar AniList: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val startDateInt = getStartDateInt3MonthsAgo()
            val query = """
                query (${'$'}startDate: Int) {
                  Page(page: 1, perPage: 24) {
                    media(startDate_greater: ${'$'}startDate, type: ANIME, sort: [SCORE_DESC, POPULARITY_DESC]) {
                      id
                      title { english romaji native }
                      coverImage { extraLarge large }
                      averageScore
                      episodes
                      genres
                      description(asHtml: false)
                      trailer { id site }
                      nextAiringEpisode { airingAt episode }
                      startDate { year month day }
                      isAdult
                      externalLinks { site url }
                    }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(
                query = query,
                variables = mapOf("startDate" to startDateInt)
            )

            val response = aniListApiService.getAnimesDeHoje(payload)
            if (response.isSuccessful) {
                var mediaList = response.body()?.data?.page?.media ?: emptyList()

                if (mediaList.isEmpty()) {
                    val fallbackQuery = """
                        query {
                          Page(page: 1, perPage: 24) {
                            media(type: ANIME, sort: [SCORE_DESC, POPULARITY_DESC]) {
                              id
                              title { english romaji native }
                              coverImage { extraLarge large }
                              averageScore
                              episodes
                              genres
                              description(asHtml: false)
                              trailer { id site }
                              nextAiringEpisode { airingAt episode }
                              startDate { year month day }
                              isAdult
                              externalLinks { site url }
                            }
                          }
                        }
                    """.trimIndent()
                    val fallbackResp = aniListApiService.getAnimesDeHoje(GraphQLPayload(query = fallbackQuery))
                    mediaList = fallbackResp.body()?.data?.page?.media ?: emptyList()
                }

                val domainList = mediaList
                    .filter { it.isAdult != true }
                    .map { it.toDomain() }
                emit(Resource.Success(domainList))
            } else {
                emit(Resource.Error("Erro ao carregar animes populares"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro animes populares AniList: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val gqlQuery = """
                query (${'$'}search: String, ${'$'}genre: String) {
                  Page(page: 1, perPage: 28) {
                    media(search: ${'$'}search, genre: ${'$'}genre, type: ANIME, sort: POPULARITY_DESC) {
                      id
                      title { english romaji native }
                      coverImage { extraLarge large }
                      averageScore
                      episodes
                      genres
                      description(asHtml: false)
                      trailer { id site }
                      nextAiringEpisode { airingAt episode }
                      startDate { year month day }
                      isAdult
                      externalLinks { site url }
                    }
                  }
                }
            """.trimIndent()

            val variables = mutableMapOf<String, Any?>()
            if (query.isNotBlank()) {
                variables["search"] = query
            }
            if (!genre.isNullOrBlank() && genre != "Todos") {
                variables["genre"] = genre
            }

            val payload = GraphQLPayload(
                query = gqlQuery,
                variables = variables
            )

            val response = aniListApiService.getAnimesDeHoje(payload)
            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                val domainList = mediaList
                    .filter { it.isAdult != true }
                    .map { it.toDomain() }
                emit(Resource.Success(domainList))
            } else {
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca AniList: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val gqlQuery = """
                query (${'$'}id: Int) {
                  Media(id: ${'$'}id, type: ANIME) {
                    id
                    title { english romaji native }
                    coverImage { extraLarge large }
                    description(asHtml: false)
                    averageScore
                    episodes
                    status
                    genres
                    trailer { id site }
                    startDate { year month day }
                    isAdult
                    externalLinks { site url }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(
                query = gqlQuery,
                variables = mapOf("id" to id)
            )

            val response = aniListApiService.getAnimesDeHoje(payload)
            if (response.isSuccessful) {
                val mediaDto = response.body()?.data?.media
                if (mediaDto != null) {
                    emit(Resource.Success(mediaDto.toDomain()))
                } else {
                    emit(Resource.Error("Detalhes do anime não encontrados"))
                }
            } else {
                emit(Resource.Error("Erro ao carregar detalhes"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes AniList: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
