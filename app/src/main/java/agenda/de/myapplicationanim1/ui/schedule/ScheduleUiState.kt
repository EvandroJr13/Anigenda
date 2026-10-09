package agenda.de.myapplicationanim1.ui.schedule

import agenda.de.myapplicationanim1.data.model.Anime

sealed interface ScheduleUiState {
    object Loading : ScheduleUiState
    data class Success(val animeList: List<Anime>) : ScheduleUiState
    data class Error(val message: String) : ScheduleUiState
}
