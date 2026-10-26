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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val fakeAnimeList = listOf(
        Anime(
            id = 1,
            title = "Naruto",
            titleJapanese = "ナルト",
            imageUrl = "http://example.com/naruto.jpg",
            largeImageUrl = "http://example.com/naruto_large.jpg",
            synopsis = "Ninja anime",
            score = 8.0,
            episodes = 220,
            status = "Finished Airing",
            aired = "2002",
            broadcast = BroadcastInfo("Thursdays", "19:00", "JST", "Thursdays at 19:00"),
            genres = listOf("Action", "Adventure"),
            studios = listOf("Studio Pierrot"),
            season = "Fall",
            year = 2002,
            rating = "PG-13"
        ),
        Anime(
            id = 2,
            title = "One Piece",
            titleJapanese = "ワンピース",
            imageUrl = "http://example.com/op.jpg",
            largeImageUrl = "http://example.com/op_large.jpg",
            synopsis = "Pirate anime",
            score = 9.0,
            episodes = 1000,
            status = "Currently Airing",
            aired = "1999",
            broadcast = BroadcastInfo("Sundays", "09:30", "JST", "Sundays at 09:30"),
            genres = listOf("Action", "Adventure"),
            studios = listOf("Toei Animation"),
            season = "Fall",
            year = 1999,
            rating = "PG-13"
        )
    )

    private class FakeSearchRepository(
        var searchResult: Resource<List<Anime>> = Resource.Success(emptyList())
    ) : AnimeRepository {
        var requestedQuery: String? = null
        var requestedGenre: String? = null
        var searchCallCount = 0

        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))

        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> {
            requestedQuery = query
            requestedGenre = genre
            searchCallCount++
            return flowOf(searchResult)
        }

        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flowOf(Resource.Error("Not implemented"))
    }

    @Test
    fun initial_stateIsIdle() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(repository = FakeSearchRepository())
        assertEquals("", viewModel.query.value)
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun onQueryChanged_triggersDebouncedSearch() = runTest(testDispatcher) {
        val repo = FakeSearchRepository(searchResult = Resource.Success(fakeAnimeList))
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Naruto")
        // Advance time past the 400ms debounce delay
        testDispatcher.scheduler.advanceTimeBy(450)

        assertEquals("Naruto", viewModel.query.value)
        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        assertEquals(fakeAnimeList, (state as SearchUiState.Success).animeList)
        assertEquals("Naruto", repo.requestedQuery)
    }

    @Test
    fun searchError_emitsErrorState() = runTest(testDispatcher) {
        val repo = FakeSearchRepository(searchResult = Resource.Error("Search failed"))
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Naruto")
        testDispatcher.scheduler.advanceTimeBy(450)

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Error)
        assertEquals("Search failed", (state as SearchUiState.Error).message)
    }

    @Test
    fun toggleStatus_filtersResults() = runTest(testDispatcher) {
        val repo = FakeSearchRepository(searchResult = Resource.Success(fakeAnimeList))
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Anime")
        testDispatcher.scheduler.advanceTimeBy(450)

        // Filter by Airing (One Piece has "Currently Airing", Naruto has "Finished Airing")
        viewModel.toggleStatus("Airing")

        val state = viewModel.uiState.value
        assertTrue(state is SearchUiState.Success)
        val list = (state as SearchUiState.Success).animeList
        assertEquals(1, list.size)
        assertEquals("One Piece", list[0].title)
    }

    @Test
    fun clearQuery_resetsStateWhenNoFiltersActive() = runTest(testDispatcher) {
        val repo = FakeSearchRepository(searchResult = Resource.Success(fakeAnimeList))
        val viewModel = SearchViewModel(repository = repo)

        viewModel.onQueryChanged("Naruto")
        testDispatcher.scheduler.advanceTimeBy(450)

        viewModel.clearQuery()

        assertEquals("", viewModel.query.value)
        assertEquals(SearchUiState.Idle, viewModel.uiState.value)
    }
}
