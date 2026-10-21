package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.MalApiService
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val malApiService: MalApiService = MalApiService.create()
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
            val response = malApiService.getAiringAnimeRanking(limit = 50)
            if (response.isSuccessful) {
                val rankingItems = response.body()?.data ?: emptyList()
                var animeList = rankingItems
                    .mapNotNull { it.node }
                    .filter { it.rating != "rx" && (it.status == "currently_airing" || it.status == "RELEASING") }
                    .map { it.toDomain() }

                if (!filter.isNullOrBlank()) {
                    val normalizedFilter = filter.trim().lowercase()
                    animeList = animeList.filter { it.airingDay.lowercase() == normalizedFilter }
                }

                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na API MyAnimeList: ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro ao carregar MyAnimeList API: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar programação: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = malApiService.getAiringAnimeRanking(limit = 24)
            if (response.isSuccessful) {
                val rankingItems = response.body()?.data ?: emptyList()
                val animeList = rankingItems
                    .mapNotNull { it.node }
                    .filter { it.rating != "rx" }
                    .map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na API MyAnimeList: ${response.code()}"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro MAL populares: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar animes populares: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = malApiService.searchAnime(query = query, limit = 50)
            if (response.isSuccessful) {
                val searchItems = response.body()?.data ?: emptyList()
                val animeList = searchItems
                    .mapNotNull { it.node }
                    .filter { it.rating != "rx" }
                    .map { it.toDomain() }
                emit(Resource.Success(animeList))
            } else {
                emit(Resource.Error("Erro na busca"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca MAL: ${e.message}", e)
            emit(Resource.Error("Erro na busca: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val response = malApiService.getAnimeDetails(id = id)
            val malSingle = response.body()
            if (malSingle?.id != null && malSingle.title != null) {
                val node = agenda.de.myapplicationanim1.data.model.MalAnimeNode(
                    id = malSingle.id,
                    title = malSingle.title,
                    mainPicture = malSingle.mainPicture,
                    alternativeTitles = malSingle.alternativeTitles,
                    startDate = malSingle.startDate,
                    synopsis = malSingle.synopsis,
                    mean = malSingle.mean,
                    status = malSingle.status,
                    numEpisodes = malSingle.numEpisodes,
                    broadcast = malSingle.broadcast,
                    rating = malSingle.rating
                )
                emit(Resource.Success(node.toDomain()))
            } else {
                emit(Resource.Error("Detalhes não encontrados no MyAnimeList"))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes MAL: ${e.message}", e)
            emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
