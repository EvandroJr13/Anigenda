package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.ui.search.SearchUiState
import agenda.de.myapplicationanim1.ui.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeSearchResult = listOf(
        Anime(
            id = 1,
            title = "Naruto Shippuden",
            titleJapanese = "ナルト",
            imageUrl = "http://example.com/naruto.jpg",
            largeImageUrl = "http://example.com/naruto_large.jpg",
            synopsis = "Ninja adventures",
            score = 8.2,
            episodes = 500,
            status = "Finished Airing",
            aired = "2007",
            broadcast = BroadcastInfo("Thursdays", "19:30", "JST", "Thursdays at 19:30"),
            genres = listOf("Action", "Adventure"),
            studios = listOf("Pierrot"),
            season = "Winter",
            year = 2007,
            rating = "PG-13"
        ),
        Anime(
            id = 2,
            title = "One Piece",
            titleJapanese = "ワンピース",
            imageUrl = "http://example.com/onepiece.jpg",
            largeImageUrl = "http://example.com/onepiece_large.jpg",
            synopsis = "Pirate adventures",
            score = 8.7,
            episodes = 1000,
            status = "Currently Airing",
            aired = "1999",
            broadcast = BroadcastInfo("Sundays", "09:30", "JST", "Sundays at 09:30"),
            genres = listOf("Action", "Adventure", "Comedy"),
            studios = listOf("Toei Animation"),
            season = "Fall",
            year = 1999,
            rating = "PG-13"
        )
    )

    private inner class FakeSearchRepository : AnimeRepository {
        var requestedQuery: String? = null
        var shouldReturnError = false

        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flowOf(Resource.Error("Not implemented"))

        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> {
            requestedQuery = query
            return if (shouldReturnError) {
                flowOf(Resource.Error("Search failed"))
            } else {
                flowOf(Resource.Success(fakeSearchResult))
            }
        }
    }

    @Test
    fun initialScreenState_isIdle() {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repository = repo)

        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun onQueryChanged_triggersDebouncedSearch() = runTest {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Naruto")
        advanceTimeBy(500) // Advance past debounce delay

        assertEquals("Naruto", repo.requestedQuery)
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        assertEquals(fakeSearchResult, (state as SearchUiState.Success).animeList)
    }

    @Test
    fun clearQuery_resetsToIdleWhenNoFilterSelected() = runTest {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Naruto")
        advanceTimeBy(500)

        viewModel.clearQuery()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun toggleStatus_filtersResults() = runTest {
        val repo = FakeSearchRepository()
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Anime")
        advanceTimeBy(500)

        viewModel.toggleStatus("Finished")

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        val filteredList = (state as SearchUiState.Success).animeList
        assertEquals(1, filteredList.size)
        assertEquals("Naruto Shippuden", filteredList.first().title)
    }
}
