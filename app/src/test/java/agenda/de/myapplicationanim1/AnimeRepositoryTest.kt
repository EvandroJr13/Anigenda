package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.AnitakuAnimeItem
import agenda.de.myapplicationanim1.data.model.AnitakuInfoResponse
import agenda.de.myapplicationanim1.data.model.AnitakuResponse
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AnitakuApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun anitakuAnimeItem_toDomain_mapping_isCorrect() {
        val item = AnitakuAnimeItem(
            id = "naruto",
            title = "Naruto",
            image = "http://example.com/cover.jpg",
            episodeNumber = 220,
            releaseDate = "2002",
            status = "Completed"
        )

        val domain = item.toDomain()

        assertEquals("Naruto", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals(220, domain.episodes)
    }

    @Test
    fun getSchedules_returnsAnitakuAnimes() = runTest {
        val fakeAnitakuApi = object : AnitakuApiService {
            override suspend fun getRecentEpisodes(page: Int) = Response.success(AnitakuResponse())
            override suspend fun getTopAiring(page: Int): Response<AnitakuResponse> {
                return Response.success(
                    AnitakuResponse(
                        results = listOf(
                            AnitakuAnimeItem(
                                id = "naruto",
                                title = "Naruto",
                                image = "http://example.com/cover.jpg",
                                episodeNumber = 220,
                                releaseDate = "2002",
                                status = "RELEASING"
                            )
                        )
                    )
                )
            }
            override suspend fun getPopular(page: Int) = Response.success(AnitakuResponse())
            override suspend fun search(query: String) = Response.success(AnitakuResponse())
            override suspend fun getInfo(id: String) = Response.success(AnitakuInfoResponse(id = id, title = "Naruto"))
        }

        val repository = AnimeRepositoryImpl(anitakuApiService = fakeAnitakuApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Naruto", successRes.data[0].title)
    }
}
