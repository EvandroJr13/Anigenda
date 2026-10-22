package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.GraphQLPayload
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val aniListApiService: AniListApiService = AniListApiService.create()
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
        try {
            val query = """
                query (${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(type: ANIME, status: RELEASING, sort: [POPULARITY_DESC], isAdult: false) {
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
                      nextAiringEpisode {
                        airingAt
                        episode
                      }
                      genres
                      isAdult
                      externalLinks {
                        site
                        url
                      }
                    }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(
                query = query,
                variables = mapOf("page" to 1, "perPage" to 50)
            )

            val response = aniListApiService.getAnimesDeHoje(payload)
            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
                val domainList = mediaList
                    .filter { it.isAdult != true }
                    .map { it.toDomain(overrideDay = filter) }
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
            val query = """
                query (${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(type: ANIME, sort: [POPULARITY_DESC, SCORE_DESC], isAdult: false) {
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
                      genres
                      isAdult
                      externalLinks {
                        site
                        url
                      }
                    }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(
                query = query,
                variables = mapOf("page" to 1, "perPage" to 24)
            )

            val response = aniListApiService.getAnimesDeHoje(payload)
            if (response.isSuccessful) {
                val mediaList = response.body()?.data?.page?.media ?: emptyList()
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
                query (${'$'}search: String, ${'$'}page: Int, ${'$'}perPage: Int) {
                  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    media(search: ${'$'}search, type: ANIME, sort: [POPULARITY_DESC], isAdult: false) {
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
                      genres
                      isAdult
                      externalLinks {
                        site
                        url
                      }
                    }
                  }
                }
            """.trimIndent()

            val payload = GraphQLPayload(
                query = gqlQuery,
                variables = mapOf("search" to query, "page" to 1, "perPage" to 50)
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
                    genres
                    isAdult
                    externalLinks {
                      site
                      url
                    }
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
