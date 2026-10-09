package agenda.de.myapplicationanim1

import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.model.BroadcastInfo
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.util.Resource
import agenda.de.myapplicationanim1.ui.schedule.ScheduleDay
import agenda.de.myapplicationanim1.ui.schedule.ScheduleUiState
import agenda.de.myapplicationanim1.ui.schedule.ScheduleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeAnimeList = listOf(
        Anime(
            id = 1,
            title = "Test Anime",
            titleJapanese = "テスト",
            imageUrl = "http://example.com/img.jpg",
            largeImageUrl = "http://example.com/img_large.jpg",
            synopsis = "Test synopsis",
            score = 8.5,
            episodes = 12,
            status = "Airing",
            aired = "2025",
            broadcast = BroadcastInfo("Mondays", "22:00", "JST", "Mondays at 22:00"),
            genres = listOf("Action"),
            studios = listOf("Test Studio"),
            season = "Winter",
            year = 2025,
            rating = "PG-13"
        )
    )

    private class FakeAnimeRepository(
        var scheduleResult: Resource<List<Anime>> = Resource.Success(emptyList()),
    ) : AnimeRepository {
        var requestedFilter: String? = null

        override fun getSchedules(filter: String?): Flow<Resource<List<Anime>>> {
            requestedFilter = filter
            return flowOf(scheduleResult)
        }

        override fun getPopularAnime(): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun searchAnime(query: String, genre: String?): Flow<Resource<List<Anime>>> = flowOf(Resource.Success(emptyList()))
        override fun getAnimeDetails(id: Int): Flow<Resource<Anime>> = flowOf(Resource.Error("Not implemented"))
    }

    @Test
    fun init_loadsScheduleForToday() = runTest {
        val repo = FakeAnimeRepository(scheduleResult = Resource.Success(fakeAnimeList))
        val viewModel = ScheduleViewModel(repository = repo)

        val currentState = viewModel.uiState.value
        assertTrue(currentState is ScheduleUiState.Success)
        assertEquals(fakeAnimeList, (currentState as ScheduleUiState.Success).animeList)
        assertEquals(ScheduleDay.today().displayNamePt, repo.requestedFilter)
    }

    @Test
    fun selectDay_updatesDayAndLoadsNewSchedule() = runTest {
        val repo = FakeAnimeRepository(scheduleResult = Resource.Success(fakeAnimeList))
        val viewModel = ScheduleViewModel(repository = repo)

        viewModel.selectDay(ScheduleDay.FRIDAY)

        assertEquals(ScheduleDay.FRIDAY, viewModel.selectedDay.value)
        assertEquals("Sexta", repo.requestedFilter)
        assertTrue(viewModel.uiState.value is ScheduleUiState.Success)
    }

    @Test
    fun errorState_handledCorrectly() = runTest {
        val repo = FakeAnimeRepository(scheduleResult = Resource.Error("Network error"))
        val viewModel = ScheduleViewModel(repository = repo)

        val currentState = viewModel.uiState.value
        assertTrue(currentState is ScheduleUiState.Error)
        assertEquals("Network error", (currentState as ScheduleUiState.Error).message)
    }
}
