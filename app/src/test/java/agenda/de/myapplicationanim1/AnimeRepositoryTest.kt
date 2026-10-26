package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.AniListData
import agenda.de.myapplicationanim1.data.model.AniListResponse
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BangumiItemDto
import agenda.de.myapplicationanim1.data.model.CoverImageDto
import agenda.de.myapplicationanim1.data.model.MediaDto
import agenda.de.myapplicationanim1.data.model.PageDto
import agenda.de.myapplicationanim1.data.model.StartDateDto
import agenda.de.myapplicationanim1.data.model.TitleDto
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.remote.BangumiApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun mediaDto_toDomain_mapping_isCorrect() {
        val dto = MediaDto(
            id = 1,
            title = TitleDto(romaji = "Cowboy Bebop", english = "Cowboy Bebop", native = "カウボーイビバップ"),
            coverImage = CoverImageDto(large = "http://example.com/cover.jpg"),
            description = "Space bounty hunters.<br>Action.",
            averageScore = 88,
            episodes = 26,
            status = "FINISHED",
            startDate = StartDateDto(year = 1998, month = 4, day = 3),
            nextAiringEpisode = null,
            genres = listOf("Action", "Sci-Fi"),
            studios = null,
            trailer = null
        )

        val domain = dto.toDomain()

        assertEquals(1, domain.id)
        assertEquals("Cowboy Bebop", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals("Space bounty hunters.\nAction.", domain.synopsis)
        assertEquals(8.8, domain.score, 0.001)
        assertEquals(26, domain.episodes)
        assertEquals("FINISHED", domain.status)
        assertEquals(listOf("Action", "Sci-Fi"), domain.genres)
        assertEquals(1998, domain.year)
    }

    @Test
    fun mediaDto_externalLinks_and_isAdult_mapping() {
        val dto = MediaDto(
            id = 2,
            title = TitleDto(romaji = "Test", english = "Test", native = null),
            coverImage = null,
            description = null,
            averageScore = 75,
            episodes = 12,
            status = "RELEASING",
            startDate = null,
            nextAiringEpisode = null,
            genres = emptyList(),
            studios = null,
            trailer = null,
            isAdult = false,
            externalLinks = listOf(
                agenda.de.myapplicationanim1.data.model.ExternalLinkDto(site = "Crunchyroll", url = "https://crunchyroll.com/test")
            )
        )

        val domain = dto.toDomain()
        assertEquals(1, domain.externalLinks.size)
        assertEquals("Crunchyroll", domain.externalLinks[0].site)
        assertEquals("https://crunchyroll.com/test", domain.externalLinks[0].url)
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
    fun moshi_deserialization_aniListResponse_isCorrect() {
        val json = """
            {
                "data": {
                    "Page": {
                        "media": [
                            {
                                "id": 5114,
                                "title": {
                                    "romaji": "Fullmetal Alchemist: Brotherhood",
                                    "english": "Fullmetal Alchemist: Brotherhood"
                                },
                                "averageScore": 91,
                                "episodes": 64
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(AniListResponse::class.java)

        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.data?.page?.media)
        assertEquals(1, response?.data?.page?.media?.size)
        val media = response?.data?.page?.media?.first()
        assertEquals(5114, media?.id)
        assertEquals("Fullmetal Alchemist: Brotherhood", media?.title?.english)
        assertEquals(91, media?.averageScore)
        assertEquals(64, media?.episodes)
    }

    @Test
    fun getSchedules_fallsBackToAniList_whenBangumiFails() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<List<BangumiItemDto>>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
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
                                    startDate = StartDateDto(year = 2025, month = 1, day = 1),
                                    nextAiringEpisode = agenda.de.myapplicationanim1.data.model.NextAiringEpisodeDto(
                                        airingAt = mondayTimestamp,
                                        episode = 1
                                    ),
                                    genres = listOf("Action"),
                                    studios = null,
                                    trailer = null
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
            override suspend fun getItems() = Response.error<List<BangumiItemDto>>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }
        val fakeAniListApi = object : AniListApiService {
            override suspend fun graphqlQuery(body: Map<String, Any>): Response<AniListResponse> {
                return Response.error(500, "Error".toResponseBody("text/plain".toMediaType()))
            }
        }

        val repository = AnimeRepositoryImpl(apiService = fakeAniListApi, bangumiApiService = fakeBangumiApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertTrue(successRes.data.isEmpty())
    }

    @Test
    fun getSchedules_filtersByAiringStatusAndDayOfWeek() = runTest {
        val fakeBangumiApi = object : BangumiApiService {
            override suspend fun getItems() = Response.error<List<BangumiItemDto>>(404, "Not Found".toResponseBody("text/plain".toMediaType()))
        }

        val cal = java.util.Calendar.getInstance().apply {
            set(2025, java.util.Calendar.JANUARY, 6, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val mondayTimestamp = cal.timeInMillis / 1000L

        val calWed = java.util.Calendar.getInstance().apply {
            set(2025, java.util.Calendar.JANUARY, 8, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val wednesdayTimestamp = calWed.timeInMillis / 1000L

        val fakeAniListApi = object : AniListApiService {
            override suspend fun graphqlQuery(body: Map<String, Any>): Response<AniListResponse> {
                val aniListResponse = AniListResponse(
                    data = AniListData(
                        page = PageDto(
                            media = listOf(
                                MediaDto(
                                    id = 1,
                                    title = TitleDto(romaji = "Monday Anime", english = "Monday Anime", native = null),
                                    coverImage = null,
                                    description = null,
                                    averageScore = 80,
                                    episodes = 12,
                                    status = "RELEASING",
                                    startDate = null,
                                    nextAiringEpisode = agenda.de.myapplicationanim1.data.model.NextAiringEpisodeDto(
                                        airingAt = mondayTimestamp,
                                        episode = 1
                                    ),
                                    genres = emptyList(),
                                    studios = null,
                                    trailer = null
                                ),
                                MediaDto(
                                    id = 2,
                                    title = TitleDto(romaji = "Wednesday Anime", english = "Wednesday Anime", native = null),
                                    coverImage = null,
                                    description = null,
                                    averageScore = 80,
                                    episodes = 12,
                                    status = "RELEASING",
                                    startDate = null,
                                    nextAiringEpisode = agenda.de.myapplicationanim1.data.model.NextAiringEpisodeDto(
                                        airingAt = wednesdayTimestamp,
                                        episode = 1
                                    ),
                                    genres = emptyList(),
                                    studios = null,
                                    trailer = null
                                ),
                                MediaDto(
                                    id = 3,
                                    title = TitleDto(romaji = "Finished Anime", english = "Finished Anime", native = null),
                                    coverImage = null,
                                    description = null,
                                    averageScore = 80,
                                    episodes = 12,
                                    status = "FINISHED",
                                    startDate = null,
                                    nextAiringEpisode = null,
                                    genres = emptyList(),
                                    studios = null,
                                    trailer = null
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

        val resultsMonday = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { resultsMonday.add(it) }
        val successMonday = resultsMonday.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successMonday.data.size)
        assertEquals("Monday Anime", successMonday.data[0].title)

        val resultsWed = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Quarta").collect { resultsWed.add(it) }
        val successWed = resultsWed.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successWed.data.size)
        assertEquals("Wednesday Anime", successWed.data[0].title)
    }
}
