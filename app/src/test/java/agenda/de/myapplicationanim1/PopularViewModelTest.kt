package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.ui.popular.PopularUiState
import agenda.de.myapplicationanim1.ui.popular.PopularViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PopularViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeAnimeList = listOf(
        Anime(
            id = 101,
            title = "Popular Anime 2025",
            titleJapanese = "人気アニメ",
            imageUrl = "http://example.com/pop.jpg",
            largeImageUrl = "http://example.com/pop_large.jpg",
            synopsis = "Popular synopsis",
            score = 9.1,
            episodes = 12,
            status = "Releasing",
            aired = "2025",
            broadcast = BroadcastInfo("Mondays", "20:00", "JST", "Mondays at 20:00"),
            genres = listOf("Action"),
            studios = listOf("Ufotable"),
            season = "Winter",
            year = 2025,
            rating = "PG-13"
        )
    )

    private class FakePopularRepository(
        var popularResult: Resource<List<Anime>> = Resource.Success(emptyList()),
    ) : AnimeRepository {
        var requestedPopularCount = 0

        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))

        override fun getPopularAnime(): Flow<Resource<List<Anime>>> {
            requestedPopularCount++
            return flowOf(popularResult)
        }

        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flowOf(Resource.Error("Not implemented"))
    }

    @Test
    fun init_loadsPopularAnimeSuccessfully() = runTest {
        val repo = FakePopularRepository(popularResult = Resource.Success(fakeAnimeList))
        val viewModel = PopularViewModel(repository = repo)

        val state = viewModel.uiState.value
        assertTrue(state is PopularUiState.Success)
        assertEquals(fakeAnimeList, (state as PopularUiState.Success).animeList)
        assertEquals(1, repo.requestedPopularCount)
    }

    @Test
    fun errorState_handledCorrectly() = runTest {
        val repo = FakePopularRepository(popularResult = Resource.Error("API Error"))
        val viewModel = PopularViewModel(repository = repo)

        val state = viewModel.uiState.value
        assertTrue(state is PopularUiState.Error)
        assertEquals("API Error", (state as PopularUiState.Error).message)
    }
}
