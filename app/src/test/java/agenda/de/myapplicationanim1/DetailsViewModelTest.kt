package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.ui.details.DetailsUiState
import agenda.de.myapplicationanim1.ui.details.DetailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeAnimeDetails = Anime(
        id = 42,
        title = "Detailed Anime",
        titleJapanese = "詳細アニメ",
        imageUrl = "http://example.com/details.jpg",
        largeImageUrl = "http://example.com/details_large.jpg",
        synopsis = "Deep synopsis",
        score = 8.9,
        episodes = 24,
        status = "Finished Airing",
        aired = "2024",
        broadcast = BroadcastInfo("Fridays", "23:00", "JST", "Fridays at 23:00"),
        genres = listOf("Action", "Sci-Fi"),
        studios = listOf("Bones"),
        season = "Spring",
        year = 2024,
        rating = "PG-13"
    )

    private inner class FakeDetailsRepository : AnimeRepository {
        var requestedId: Int? = null
        var shouldReturnError = false

        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))

        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> {
            requestedId = id
            return if (shouldReturnError) {
                flowOf(Resource.Error("Details Error"))
            } else {
                flowOf(Resource.Success(fakeAnimeDetails))
            }
        }
    }

    @Test
    fun loadAnimeDetails_fetchesDetailsAndEmitsSuccess() = runTest {
        val repo = FakeDetailsRepository()
        val viewModel = DetailsViewModel(repository = repo)

        viewModel.loadAnimeDetails(42)

        assertEquals(42, repo.requestedId)
        val state = viewModel.uiState.value
        assertTrue(state is DetailsUiState.Success)
        assertEquals(fakeAnimeDetails, (state as DetailsUiState.Success).anime)
    }

    @Test
    fun loadAnimeDetails_emitsErrorOnFailure() = runTest {
        val repo = FakeDetailsRepository().apply { shouldReturnError = true }
        val viewModel = DetailsViewModel(repository = repo)

        viewModel.loadAnimeDetails(42)

        val state = viewModel.uiState.value
        assertTrue(state is DetailsUiState.Error)
        assertEquals("Details Error", (state as DetailsUiState.Error).message)
    }
}
