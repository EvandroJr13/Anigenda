package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnimeDetailResponse
import agenda.de.myapplicationanim1.data.model.AnimeListResponse
import agenda.de.myapplicationanim1.data.model.AnimeModel
import agenda.de.myapplicationanim1.data.model.ImageWrapper
import agenda.de.myapplicationanim1.data.model.ImageType
import agenda.de.myapplicationanim1.data.model.JikanBroadcastInfo
import agenda.de.myapplicationanim1.data.model.StreamingLinksResponse
import agenda.de.myapplicationanim1.data.model.StreamingPlatform
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnimeApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimeRepositoryTest {

    @Test
    fun animeModel_getDisplayName_and_getImageUrl_isCorrect() {
        val model = AnimeModel(
            id = 1,
            title = "Jap Title",
            titleEnglish = "English Title",
            images = ImageWrapper(jpg = ImageType(imageUrl = "http://example.com/small.jpg", largeImageUrl = "http://example.com/large.jpg")),
            broadcast = JikanBroadcastInfo(day = "Mondays", time = "12:00", timezone = "JST", fullBroadcast = "Mondays at 12:00"),
            rating = "PG-13",
            streaming = listOf(StreamingPlatform(name = "Crunchyroll")),
            airing = true
        )

        assertEquals("English Title", model.getDisplayName())
        assertEquals("http://example.com/large.jpg", model.getImageUrl())

        val domain = model.toDomain(listOf("Crunchyroll"))
        assertEquals("English Title", domain.title)
        assertEquals("Segunda", domain.airingDay)
    }

    @Test
    fun getSchedules_returnsFilteredAnimes() = runTest {
        val fakeApi = object : AnimeApiService {
            override suspend fun getCurrentSeasonAnime(filter: String?, page: Int): AnimeListResponse {
                return AnimeListResponse(
                    data = listOf(
                        AnimeModel(
                            id = 101,
                            title = "Solo Leveling",
                            titleEnglish = "Solo Leveling",
                            images = ImageWrapper(jpg = ImageType(imageUrl = "http://example.com/solo.jpg", largeImageUrl = "http://example.com/solo.jpg")),
                            broadcast = JikanBroadcastInfo(day = "Mondays", time = "12:00", timezone = "JST", fullBroadcast = "Mondays at 12:00"),
                            rating = "PG-13",
                            streaming = listOf(StreamingPlatform(name = "Crunchyroll")),
                            airing = true
                        )
                    )
                )
            }

            override suspend fun getDailySchedule(day: String?, type: String?): AnimeListResponse {
                return getCurrentSeasonAnime("tv", 1)
            }

            override suspend fun getPopularAnimes(filter: String, type: String?, page: Int) = AnimeListResponse()
            override suspend fun getAnimeStreamingPlatforms(animeId: Int) = StreamingLinksResponse()
            override suspend fun getAnimeDetails(animeId: Int) = AnimeDetailResponse()
            override suspend fun searchAnimes(query: String, type: String?) = AnimeListResponse()
        }

        val repository = AnimeRepositoryImpl(animeApiService = fakeApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Solo Leveling", successRes.data[0].title)
    }
}
