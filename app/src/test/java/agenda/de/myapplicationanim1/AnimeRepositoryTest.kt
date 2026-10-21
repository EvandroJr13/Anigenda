package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.AniListData
import agenda.de.myapplicationanim1.data.model.AniListResponse
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BangumiDataResponse
import agenda.de.myapplicationanim1.data.model.BangumiItemDto
import agenda.de.myapplicationanim1.data.model.CoverImageDto
import agenda.de.myapplicationanim1.data.model.MediaDto
import agenda.de.myapplicationanim1.data.model.NextAiringEpisodeDto
import agenda.de.myapplicationanim1.data.model.PageDto
import agenda.de.myapplicationanim1.data.model.TitleDto
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
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
    fun getSchedules_fallsBackToAniList_whenBangumiFails() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<BangumiDataResponse>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }

        val cal = java.util.Calendar.getInstance().apply {
            set(2025, java.util.Calendar.JANUARY, 6, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val mondayTimestamp = cal.timeInMillis / 1000L

        val fakeAniListApi = object : AniListApiService {
            override suspend fun graphqlQuery(body: Map<String, Any>): Response<AniListResponse> {
                val aniListResponse = AniListResponse(
                    data = AniListData(
                        page = PageDto(
                            media = listOf(
                                MediaDto(
                                    id = 123,
                                    title = TitleDto(romaji = "AniList Anime", english = "AniList Anime", native = null),
                                    coverImage = CoverImageDto(large = "http://example.com/cover.jpg"),
                                    description = "Description",
                                    averageScore = 80,
                                    episodes = 12,
                                    status = "RELEASING",
                                    nextAiringEpisode = NextAiringEpisodeDto(
                                        airingAt = mondayTimestamp,
                                        episode = 1
                                    )
                                )
                            )
                        ),
                        media = null
                    )
                )
                return Response.success(aniListResponse)
            }
        }

        val repository = AnimeRepositoryImpl(apiService = fakeAniListApi, bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("AniList Anime", successRes.data[0].title)
    }

    @Test
    fun getSchedules_fallsBackToEmptyList_whenBothFail() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<BangumiDataResponse>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }
        val fakeAniListApi = object : AniListApiService {
            override suspend fun graphqlQuery(body: Map<String, Any>): Response<AniListResponse> {
                return Response.error(500, "Error".toResponseBody("text/plain".toMediaType()))
            }
        }

        val repository = AnimeRepositoryImpl(apiService = fakeAniListApi, bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Error })
    }
}
