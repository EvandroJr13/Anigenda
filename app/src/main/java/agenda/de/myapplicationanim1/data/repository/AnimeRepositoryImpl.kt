package agenda.de.myapplicationanim1.data.repository

import android.util.Log
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.model.ExternalLinkInfo
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
        var animeList: List<Anime>? = null

        // Try Anitaku API exclusively (English titles from Gogoanime/Anitaku)
        try {
            val response = anitakuApiService.getTopAiring()
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val mapped = results.map { it.toDomain() }
                if (mapped.isNotEmpty()) {
                    animeList = if (!filter.isNullOrBlank()) {
                        val normalizedFilter = filter.trim().lowercase()
                        mapped.filter { it.airingDay.lowercase() == normalizedFilter || it.airingDay.isEmpty() }
                    } else mapped
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Anitaku top-airing failed: ${e.message}", e)
        }

        if (animeList.isNullOrEmpty()) {
            try {
                val response = anitakuApiService.getRecentEpisodes()
                if (response.isSuccessful) {
                    val results = response.body()?.results ?: emptyList()
                    val mapped = results.map { it.toDomain() }
                    if (mapped.isNotEmpty()) {
                        animeList = if (!filter.isNullOrBlank()) {
                            val normalizedFilter = filter.trim().lowercase()
                            mapped.filter { it.airingDay.lowercase() == normalizedFilter || it.airingDay.isEmpty() }
                        } else mapped
                    }
                }
            } catch (e: Exception) {
                safeLogE("AnimeRepository", "Anitaku recent episodes failed: ${e.message}", e)
            }
        }

        // Guaranteed English/Portuguese Curated Fallback list
        val finalList = animeList.takeIf { !it.isNullOrEmpty() } ?: getFallbackAnimeList(filter)
        emit(Resource.Success(finalList))
    }.flowOn(Dispatchers.IO)

    override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        var animeList: List<Anime>? = null

        try {
            val response = anitakuApiService.getPopular()
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val mapped = results.map { it.toDomain() }
                if (mapped.isNotEmpty()) animeList = mapped
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Anitaku popular failed: ${e.message}", e)
        }

        val finalPopular = animeList.takeIf { !it.isNullOrEmpty() } ?: getFallbackAnimeList(null)
        emit(Resource.Success(finalPopular))
    }.flowOn(Dispatchers.IO)

    override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flow {
        emit(Resource.Loading)
        try {
            val response = anitakuApiService.search(query = query)
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                emit(Resource.Success(results.map { it.toDomain() }))
            } else {
                emit(Resource.Success(getFallbackAnimeList(null).filter { it.title.contains(query, ignoreCase = true) }))
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro na busca Anitaku: ${e.message}", e)
            emit(Resource.Success(getFallbackAnimeList(null).filter { it.title.contains(query, ignoreCase = true) }))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flow {
        emit(Resource.Loading)
        try {
            val popularResp = anitakuApiService.getPopular()
            val results = popularResp.body()?.results ?: emptyList()
            val match = results.firstOrNull { it.id.hashCode() == id }
            if (match != null) {
                emit(Resource.Success(match.toDomain()))
            } else {
                val fallback = getFallbackAnimeList(null).firstOrNull { it.id == id }
                if (fallback != null) {
                    emit(Resource.Success(fallback))
                } else {
                    emit(Resource.Error("Detalhes do anime não encontrados"))
                }
            }
        } catch (e: Exception) {
            safeLogE("AnimeRepository", "Erro nos detalhes: ${e.message}", e)
            val fallback = getFallbackAnimeList(null).firstOrNull { it.id == id }
            if (fallback != null) {
                emit(Resource.Success(fallback))
            } else {
                emit(Resource.Error("Erro ao carregar detalhes: ${e.localizedMessage ?: e.message}", e))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun getFallbackAnimeList(filter: String?): List<Anime> {
        val list = listOf(
            Anime(
                id = 1,
                title = "Solo Leveling Season 2",
                titleRomaji = "Solo Leveling Season 2",
                titleEnglish = "Solo Leveling Season 2",
                imageUrl = "https://cdn.myanimelist.net/images/anime/1915/136066.jpg",
                largeImageUrl = "https://cdn.myanimelist.net/images/anime/1915/136066.jpg",
                synopsis = "The weakest hunter of all mankind rises again with a system.",
                score = 8.9,
                episodes = 12,
                status = "RELEASING",
                aired = "2024",
                broadcast = BroadcastInfo(day = "Segunda", time = "13:30", timezone = "JST", stringFormat = "Segunda às 13:30"),
                airingDay = "Segunda",
                externalLinks = listOf(ExternalLinkInfo(site = "Crunchyroll", url = ""))
            ),
            Anime(
                id = 2,
                title = "One Piece",
                titleRomaji = "One Piece",
                titleEnglish = "One Piece",
                imageUrl = "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
                largeImageUrl = "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
                synopsis = "Gol D. Roger was known as the 'Pirate King'.",
                score = 8.7,
                episodes = 1100,
                status = "RELEASING",
                aired = "1999",
                broadcast = BroadcastInfo(day = "Domingo", time = "09:30", timezone = "JST", stringFormat = "Domingo às 09:30"),
                airingDay = "Domingo",
                externalLinks = listOf(ExternalLinkInfo(site = "Crunchyroll", url = ""), ExternalLinkInfo(site = "Netflix", url = ""))
            ),
            Anime(
                id = 3,
                title = "Bleach: Thousand-Year Blood War",
                titleRomaji = "Bleach: Sennen Kessen-hen",
                titleEnglish = "Bleach: Thousand-Year Blood War",
                imageUrl = "https://cdn.myanimelist.net/images/anime/1764/126627.jpg",
                largeImageUrl = "https://cdn.myanimelist.net/images/anime/1764/126627.jpg",
                synopsis = "The final conflict between Soul Reapers and Quincies.",
                score = 9.1,
                episodes = 13,
                status = "RELEASING",
                aired = "2022",
                broadcast = BroadcastInfo(day = "Sábado", time = "23:30", timezone = "JST", stringFormat = "Sábado às 23:30"),
                airingDay = "Sábado",
                externalLinks = listOf(ExternalLinkInfo(site = "Disney+", url = ""))
            ),
            Anime(
                id = 4,
                title = "Blue Lock Season 2",
                titleRomaji = "Blue Lock Season 2",
                titleEnglish = "Blue Lock Season 2",
                imageUrl = "https://cdn.myanimelist.net/images/anime/1287/138988.jpg",
                largeImageUrl = "https://cdn.myanimelist.net/images/anime/1287/138988.jpg",
                synopsis = "The intense soccer striker training continues.",
                score = 8.2,
                episodes = 14,
                status = "RELEASING",
                aired = "2024",
                broadcast = BroadcastInfo(day = "Sábado", time = "00:00", timezone = "JST", stringFormat = "Sábado às 00:00"),
                airingDay = "Sábado",
                externalLinks = listOf(ExternalLinkInfo(site = "Crunchyroll", url = ""))
            ),
            Anime(
                id = 5,
                title = "Re:ZERO - Starting Life in Another World",
                titleRomaji = "Re:Zero kara Hajimeru Isekai Seikatsu",
                titleEnglish = "Re:ZERO - Starting Life in Another World Season 3",
                imageUrl = "https://cdn.myanimelist.net/images/anime/1862/141029.jpg",
                largeImageUrl = "https://cdn.myanimelist.net/images/anime/1862/141029.jpg",
                synopsis = "Subaru Natsuki faces new despair and powerful adversaries in another world.",
                score = 8.6,
                episodes = 16,
                status = "RELEASING",
                aired = "2024",
                broadcast = BroadcastInfo(day = "Quarta", time = "22:30", timezone = "JST", stringFormat = "Quarta às 22:30"),
                airingDay = "Quarta",
                externalLinks = listOf(ExternalLinkInfo(site = "Crunchyroll", url = ""))
            )
        )

        return if (!filter.isNullOrBlank()) {
            val norm = filter.trim().lowercase()
            list.filter { it.airingDay.lowercase() == norm }
        } else {
            list
        }
    }
}
