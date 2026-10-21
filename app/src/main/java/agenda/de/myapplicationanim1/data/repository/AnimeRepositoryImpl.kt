package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.KitsuApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val kitsuApiService: KitsuApiService = KitsuApiService.create()
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
            val response = kitsuApiService.getCurrentlyAiringAnime(status = "current", limit = 50)
            if (response.isSuccessful) {
                val kitsuList = response.body()?.data ?: emptyList()
                var animeList = kitsuList
                    .filter { it.attributes?.nsfw != true && it.attributes?.status == "current" }
                    .map { it.toDomain() }

                if (!filter.isNullOrBlank()) {
                    val normalizedFilter = filter.trim().lowercase()
                    animeList = animeList.filter { it.airingDay.lowercase() == normalizedFilter }
                }

                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na API Kitsu: ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar Kitsu API: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = kitsuApiService.getCurrentlyAiringAnime(status = "current", limit = 24)
            if (response.isSuccessful) {
                val kitsuList = response.body()?.data ?: emptyList()
                val animeList = kitsuList
                    .filter { it.attributes?.nsfw != true }
                    .map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na API Kitsu: ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro Kitsu populares: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar animes populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = kitsuApiService.searchAnime(query = query, limit = 50)
            if (response.isSuccessful) {
                val kitsuList = response.body()?.data ?: emptyList()
                val animeList = kitsuList
                    .filter { it.attributes?.nsfw != true }
                    .map { it.toDomain() }
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
            val response = kitsuApiService.getAnimeDetails(id = id.toString())
            val kitsuData = response.body()?.data
            if (kitsuData != null) {
                emit(Resource.Success(kitsuData.toDomain()))
            } else {
                emit(Resource.Error("Detalhes não encontrados no Kitsu"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes Kitsu: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
