package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.isMatureContent
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val bangumiApiService: BangumiApiService = RetrofitClient.bangumiApiService
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
        "Bilibili",
        "YouTube"
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
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val dataResponse = response.body()
                val items = dataResponse?.items ?: emptyList()
                var animeList = items
                    .filter { !it.isMatureContent() }
                    // Require strict English title presence in titleTranslate["en"]
                    .filter { item ->
                        val enList = item.titleTranslate?.get("en") ?: item.titleTranslate?.get("en-US")
                        !enList.isNullOrEmpty() && enList.any { it.isNotBlank() }
                    }
                    .map { it.toDomain() }

                // Filter by popular streaming platforms in BR/USA if sites are present
                animeList = animeList.filter { anime ->
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
                    animeList = animeList.filter { it.airingDay.lowercase() == normalizedFilter }
                }

                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro ao carregar programação: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val dataResponse = response.body()
                val items = dataResponse?.items ?: emptyList()
                val animeList = items
                    .filter { !it.isMatureContent() }
                    .filter { item ->
                        val enList = item.titleTranslate?.get("en") ?: item.titleTranslate?.get("en-US")
                        !enList.isNullOrEmpty() && enList.any { it.isNotBlank() }
                    }
                    .map { it.toDomain() }
                    .take(24)
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro ao carregar populares"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro populares Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val dataResponse = response.body()
                val items = dataResponse?.items ?: emptyList()
                val normalizedQuery = query.trim().lowercase()
                val animeList = items
                    .filter { !it.isMatureContent() }
                    .filter { item ->
                        val enList = item.titleTranslate?.get("en") ?: item.titleTranslate?.get("en-US")
                        !enList.isNullOrEmpty() && enList.any { it.isNotBlank() }
                    }
                    .map { it.toDomain() }
                    .filter {
                        it.title.lowercase().contains(normalizedQuery) ||
                                it.titleEnglish.lowercase().contains(normalizedQuery) ||
                                it.titleRomaji.lowercase().contains(normalizedQuery)
                    }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val dataResponse = response.body()
                val items = dataResponse?.items ?: emptyList()
                val item = items
                    .filter { item ->
                        val enList = item.titleTranslate?.get("en") ?: item.titleTranslate?.get("en-US")
                        !enList.isNullOrEmpty() && enList.any { it.isNotBlank() }
                    }
                    .firstOrNull { it.id?.hashCode() == id || it.toDomain().id == id }
                if (item != null) {
                    emit(Resource.Success(item.toDomain()))
                } else {
                    emit(Resource.Error("Detalhes do anime não encontrados"))
                }
            } else {
                emit(Resource.Error("Erro ao carregar detalhes"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
