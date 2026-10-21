package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.AnOtakuAnimeItem
import agenda.de.myapplicationanim1.data.model.AnOtakuScheduleResponse
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnOtakuApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun anOtakuAnimeItem_toDomain_mapping_isCorrect() {
        val item = AnOtakuAnimeItem(
            id = "101",
            title = "Solo Leveling",
            titleEnglish = "Solo Leveling",
            coverImage = "http://example.com/cover.jpg",
            airingDay = "Monday",
            airingTime = "12:00",
            status = "RELEASING"
        )

        val domain = item.toDomain()

        assertEquals("Solo Leveling", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals("Segunda", domain.airingDay)
    }

    @Test
    fun getSchedules_returnsAnOtakuAnimes() = runTest {
        val fakeAnOtakuApi = object : AnOtakuApiService {
            override suspend fun getSchedule(day: String?): Response<AnOtakuScheduleResponse> {
                return Response.success(
                    AnOtakuScheduleResponse(
                        status = "success",
                        data = listOf(
                            AnOtakuAnimeItem(
                                id = "101",
                                title = "Solo Leveling",
                                titleEnglish = "Solo Leveling",
                                coverImage = "http://example.com/cover.jpg",
                                airingDay = "Monday",
                                airingTime = "12:00",
                                status = "RELEASING"
                            )
                        )
                    )
                )
            }

            override suspend fun getPopularAnime() = Response.success(AnOtakuScheduleResponse())
            override suspend fun searchAnime(query: String) = Response.success(AnOtakuScheduleResponse())
        }

        val repository = AnimeRepositoryImpl(anOtakuApiService = fakeAnOtakuApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Solo Leveling", successRes.data[0].title)
    }
}
