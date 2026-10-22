package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.AniListCoverImage
import agenda.de.myapplicationanim1.data.model.AniListData
import agenda.de.myapplicationanim1.data.model.AniListMedia
import agenda.de.myapplicationanim1.data.model.AniListPage
import agenda.de.myapplicationanim1.data.model.AniListResponse
import agenda.de.myapplicationanim1.data.model.AniListTitle
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.GraphQLPayload
import agenda.de.myapplicationanim1.data.model.toDomain
import agenda.de.myapplicationanim1.data.remote.AniListApiService
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AnimeRepositoryTest {

    @Test
    fun aniListMedia_toDomain_mapping_isCorrect() {
        val media = AniListMedia(
            id = 101,
            title = AniListTitle(english = "Solo Leveling", romaji = "Solo Leveling"),
            coverImage = AniListCoverImage(large = "http://example.com/cover.jpg"),
            averageScore = 85.0,
            status = "RELEASING"
        )

        val domain = media.toDomain()

        assertEquals("Solo Leveling", domain.title)
        assertEquals("http://example.com/cover.jpg", domain.imageUrl)
        assertEquals(8.5, domain.score, 0.01)
    }

    @Test
    fun getSchedules_returnsAniListAnimes() = runTest {
        val fakeAniListApi = object : AniListApiService {
            override suspend fun getAnimesDeHoje(payload: GraphQLPayload): Response<AniListResponse> {
                return Response.success(
                    AniListResponse(
                        data = AniListData(
                            page = AniListPage(
                                media = listOf(
                                    AniListMedia(
                                        id = 101,
                                        title = AniListTitle(english = "Solo Leveling"),
                                        coverImage = AniListCoverImage(large = "http://example.com/cover.jpg"),
                                        averageScore = 85.0,
                                        status = "RELEASING"
                                    )
                                )
                            )
                        )
                    )
                )
            }
        }

        val repository = AnimeRepositoryImpl(aniListApiService = fakeAniListApi)
        val results = mutableListOf<Resource<List<Anime>>>()
        repository.getSchedules("Segunda").collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success })
        val successRes = results.first { it is Resource.Success } as Resource.Success
        assertEquals(1, successRes.data.size)
        assertEquals("Solo Leveling", successRes.data[0].title)
    }
}
