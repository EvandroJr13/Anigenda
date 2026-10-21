package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BangumiDataResponse
import agenda.de.myapplicationanim1.data.model.BangumiItemDto
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun bangumiItemDto_toDomain_mapping_isCorrect() {
        val dto = BangumiItemDto(
            id = "test-id",
            title = "Test Anime",
            type = "tv",
            lang = "ja",
            officialSite = "http://example.com",
            begin = "2023-10-01T00:00:00.000Z",
            broadcast = null,
            weekDay = 1,
            sites = emptyList(),
            image = "http://example.com/cover.jpg",
            summary = "Test summary"
        )

        val domain = dto.toDomain()

        assertEquals("Test Anime", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals("Test summary", domain.synopsis)
        assertEquals("Segunda", domain.airingDay)
    }

    @Test
    fun getSchedules_returnsBangumiAnimes() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems(): Response<BangumiDataResponse> {
                return Response.success(
                    BangumiDataResponse(
                        items = listOf(
                            BangumiItemDto(
                                id = "1",
                                title = "Test Anime",
                                titleTranslate = mapOf("en" to listOf("Test Anime")),
                                type = "tv",
                                lang = "ja",
                                officialSite = "http://example.com",
                                begin = "2023-10-01T00:00:00.000Z",
                                broadcast = null,
                                weekDay = 1,
                                sites = emptyList(),
                                image = "http://example.com/cover.jpg",
                                summary = "Test summary"
                            )
                        )
                    )
                )
            }
        }

        val repository = AnimeRepositoryImpl(bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Test Anime", successRes.data[0].title)
    }
}
