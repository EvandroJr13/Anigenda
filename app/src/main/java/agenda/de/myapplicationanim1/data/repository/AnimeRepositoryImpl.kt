package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnitakuApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val anitakuApiService: AnitakuApiService = AnitakuApiService.create(),
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
            val response = anitakuApiService.getTopAiring()
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                var animeList = results.map { it.toDomain() }

                if (!filter.isNullOrBlank()) {
                    val normalizedFilter = filter.trim().lowercase()
                    animeList = animeList.filter { it.airingDay.lowercase() == normalizedFilter }
                }

                emit(Resource.Success(animeList))
            } else {
                val recentResponse = anitakuApiService.getRecentEpisodes()
                if (recentResponse.isSuccessful) {
                    val results = recentResponse.body()?.results ?: emptyList()
                    emit(Resource.Success(results.map { it.toDomain() }))
                } else {
                    emit(Resource.Error("Erro ao carregar programação: HTTP ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar programação Anitaku: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = anitakuApiService.getPopular()
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                emit(Resource.Success(results.map { it.toDomain() }))
            } else {
                emit(Resource.Error("Erro ao carregar animes populares"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro populares Anitaku: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar animes populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = anitakuApiService.search(query = query)
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                emit(Resource.Success(results.map { it.toDomain() }))
            } else {
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca Anitaku: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val popularResp = anitakuApiService.getPopular()
            val results = popularResp.body()?.results ?: emptyList()
            val match = results.firstOrNull { it.id.hashCode() == id }
            if (match != null) {
                val infoResp = anitakuApiService.getInfo(match.id)
                val info = infoResp.body()
                if (info != null) {
                    emit(Resource.Success(info.toDomain()))
                } else {
                    emit(Resource.Success(match.toDomain()))
                }
            } else {
                emit(Resource.Error("Detalhes do anime não encontrados"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes Anitaku: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
