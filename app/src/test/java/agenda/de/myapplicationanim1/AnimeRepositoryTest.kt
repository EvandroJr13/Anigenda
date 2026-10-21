package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnimeDto
import agenda.de.myapplicationanim1.data.model.BangumiItemDto
import agenda.de.myapplicationanim1.data.model.BroadcastDto
import agenda.de.myapplicationanim1.data.model.JikanListResponse
import agenda.de.myapplicationanim1.data.model.JikanSingleResponse
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.remote.JikanApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun animeDto_toDomain_mapping_isCorrect() {
        val dto = AnimeDto(
            malId = 1,
            title = "Cowboy Bebop",
            titleEnglish = "Cowboy Bebop",
            synopsis = "Space bounty hunters.",
            score = 8.8,
            episodes = 26,
            status = "FINISHED",
            broadcast = BroadcastDto(day = "Monday", time = "12:00")
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Cowboy Bebop", domain.title)
        assertEquals("Space bounty hunters.", domain.synopsis)
        assertEquals(8.8, domain.score, 0.001)
        assertEquals(26, domain.episodes)
        assertEquals("FINISHED", domain.status)
    }

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
    fun getSchedules_fallsBackToJikan_whenBangumiFails() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<List<BangumiItemDto>>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }

        val fakeJikanApi = object : JikanApiService {
            override suspend fun getSchedules(page: Int?, sfw: Boolean) = Response.success(JikanListResponse<AnimeDto>())
            override suspend fun getScheduleByDay(day: String, page: Int?, sfw: Boolean) = Response.success(JikanListResponse<AnimeDto>())
            override suspend fun getSeasonalAnime(year: Int, season: String, page: Int?, sfw: Boolean) = Response.success(JikanListResponse<AnimeDto>())
            override suspend fun getCurrentSeasonAnime(page: Int?, sfw: Boolean) = Response.success(
                JikanListResponse(
                    data = listOf(
                        AnimeDto(
                            malId = 123,
                            title = "Jikan Anime",
                            titleEnglish = "Jikan Anime",
                            status = "RELEASING",
                            broadcast = BroadcastDto(day = "Segunda", time = "12:00")
                        )
                    )
                )
            )
            override suspend fun searchAnime(query: String, page: Int?, sfw: Boolean) = Response.success(JikanListResponse<AnimeDto>())
            override suspend fun getAnimeDetails(id: Int) = Response.success(JikanSingleResponse<AnimeDto>())
        }

        val repository = AnimeRepositoryImpl(jikanApiService = fakeJikanApi, bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Jikan Anime", successRes.data[0].title)
    }

    @Test
    fun getSchedules_fallsBackToEmptyList_whenBothFail() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<List<BangumiItemDto>>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }
        val fakeJikanApi = object : JikanApiService {
            override suspend fun getSchedules(page: Int?, sfw: Boolean) = Response.error<JikanListResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
            override suspend fun getScheduleByDay(day: String, page: Int?, sfw: Boolean) = Response.error<JikanListResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
            override suspend fun getSeasonalAnime(year: Int, season: String, page: Int?, sfw: Boolean) = Response.error<JikanListResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
            override suspend fun getCurrentSeasonAnime(page: Int?, sfw: Boolean) = Response.error<JikanListResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
            override suspend fun searchAnime(query: String, page: Int?, sfw: Boolean) = Response.error<JikanListResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
            override suspend fun getAnimeDetails(id: Int) = Response.error<JikanSingleResponse<AnimeDto>>(500, "Error".toResponseBody("text/plain".toMediaType()))
        }

        val repository = AnimeRepositoryImpl(jikanApiService = fakeJikanApi, bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Error })
    }
}
