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
                val nonMatureItems = items.filter { !it.isMatureContent() }
                var animeList = nonMatureItems.map { it.toDomain() }

                // Filter by streaming platforms popular in BR/USA if external links are present
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
            safeLogE("AnimeRepository", "Erro ao carregar programação do Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                val nonMatureItems = items.filter { !it.isMatureContent() }
                val animeList = nonMatureItems.map { it.toDomain() }.take(50)
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro ao carregar animes populares: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro popular do Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                val nonMatureItems = items.filter { !it.isMatureContent() }
                var animeList = nonMatureItems.map { it.toDomain() }

                if (query.isNotBlank()) {
                    val q = query.trim().lowercase()
                    animeList = animeList.filter { 
                        it.title.lowercase().contains(q) || 
                        it.titleEnglish.lowercase().contains(q) ||
                        it.titleRomaji.lowercase().contains(q)
                    }
                }

                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca do Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = bangumiApiService.getItems()
            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                val item = items.firstOrNull { (it.id?.hashCode() ?: it.title?.hashCode()) == id }
                if (item != null) {
                    emit(Resource.Success(item.toDomain()))
                } else {
                    emit(Resource.Error("Anime não encontrado"))
                }
            } else {
                emit(Resource.Error("Erro ao carregar detalhes"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes do Bangumi: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
