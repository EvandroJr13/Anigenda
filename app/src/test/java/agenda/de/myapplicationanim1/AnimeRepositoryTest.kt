package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.KitsuAnimeAttributes
import agenda.de.myapplicationanim1.data.model.KitsuAnimeData
import agenda.de.myapplicationanim1.data.model.KitsuAnimeListResponse
import agenda.de.myapplicationanim1.data.model.KitsuAnimeSingleResponse
import agenda.de.myapplicationanim1.data.model.KitsuImageWrapper
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.KitsuApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun kitsuAnimeData_toDomain_mapping_isCorrect() {
        val data = KitsuAnimeData(
            id = "101",
            type = "anime",
            attributes = KitsuAnimeAttributes(
                canonicalTitle = "Solo Leveling",
                titles = mapOf("en" to "Solo Leveling"),
                posterImage = KitsuImageWrapper(large = "http://example.com/cover.jpg"),
                synopsis = "Synopsis",
                averageRating = "85.0",
                status = "current",
                startDate = "2024-01-08"
            )
        )

        val domain = data.toDomain()

        assertEquals(101, domain.id)
        assertEquals("Solo Leveling", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals("Segunda", domain.airingDay)
    }

    @Test
    fun getSchedules_returnsKitsuAnimes() = runTest {
        val fakeKitsuApi = object : KitsuApiService {
            override suspend fun getCurrentlyAiringAnime(status: String, limit: Int, sort: String): Response<KitsuAnimeListResponse> {
                return Response.success(
                    KitsuAnimeListResponse(
                        data = listOf(
                            KitsuAnimeData(
                                id = "101",
                                type = "anime",
                                attributes = KitsuAnimeAttributes(
                                    canonicalTitle = "Solo Leveling",
                                    titles = mapOf("en" to "Solo Leveling"),
                                    posterImage = KitsuImageWrapper(large = "http://example.com/cover.jpg"),
                                    synopsis = "Synopsis",
                                    averageRating = "85.0",
                                    status = "current",
                                    startDate = "2024-01-08"
                                )
                            )
                        )
                    )
                )
            }

            override suspend fun searchAnime(query: String, limit: Int) = Response.success(KitsuAnimeListResponse())
            override suspend fun getAnimeDetails(id: String) = Response.success(KitsuAnimeSingleResponse())
        }

        val repository = AnimeRepositoryImpl(kitsuApiService = fakeKitsuApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Solo Leveling", successRes.data[0].title)
    }
}
