package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.MalAltTitles
import agenda.de.myapplicationanim1.data.model.MalAnimeListResponse
import agenda.de.myapplicationanim1.data.model.MalAnimeNode
import agenda.de.myapplicationanim1.data.model.MalAnimeSingleResponse
import agenda.de.myapplicationanim1.data.model.MalBroadcast
import agenda.de.myapplicationanim1.data.model.MalPicture
import agenda.de.myapplicationanim1.data.model.MalRankingItem
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.MalApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun malAnimeNode_toDomain_mapping_isCorrect() {
        val node = MalAnimeNode(
            id = 101,
            title = "Solo Leveling",
            alternativeTitles = MalAltTitles(en = "Solo Leveling"),
            mainPicture = MalPicture(large = "http://example.com/cover.jpg"),
            synopsis = "Synopsis",
            mean = 8.5,
            status = "currently_airing",
            broadcast = MalBroadcast(dayOfTheWeek = "monday", startTime = "12:00")
        )

        val domain = node.toDomain()

        assertEquals(101, domain.id)
        assertEquals("Solo Leveling", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals("Segunda", domain.airingDay)
    }

    @Test
    fun getSchedules_returnsMalAnimes() = runTest {
        val fakeMalApi = object : MalApiService {
            override suspend fun getAiringAnimeRanking(rankingType: String, limit: Int, fields: String): Response<MalAnimeListResponse> {
                return Response.success(
                    MalAnimeListResponse(
                        data = listOf(
                            MalRankingItem(
                                node = MalAnimeNode(
                                    id = 101,
                                    title = "Solo Leveling",
                                    alternativeTitles = MalAltTitles(en = "Solo Leveling"),
                                    mainPicture = MalPicture(large = "http://example.com/cover.jpg"),
                                    synopsis = "Synopsis",
                                    mean = 8.5,
                                    status = "currently_airing",
                                    broadcast = MalBroadcast(dayOfTheWeek = "monday", startTime = "12:00")
                                )
                            )
                        )
                    )
                )
            }

            override suspend fun searchAnime(query: String, limit: Int, fields: String) = Response.success(MalAnimeListResponse())
            override suspend fun getAnimeDetails(id: Int, fields: String) = Response.success(MalAnimeSingleResponse())
        }

        val repository = AnimeRepositoryImpl(malApiService = fakeMalApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Solo Leveling", successRes.data[0].title)
    }
}
