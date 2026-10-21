package agenda.de.myapplicationanim1.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: AnimeRepository = AnimeRepositoryImpl(),
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(ScheduleDay.today())
    val selectedDay: StateFlow<ScheduleDay> = _selectedDay.asStateFlow()

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule(_selectedDay.value)
    }

    fun selectDay(day: ScheduleDay) {
        if (_selectedDay.value != day) {
            _selectedDay.value = day
            loadSchedule(day)
        }
    }

    fun retry() {
        loadSchedule(_selectedDay.value)
    }

    private fun loadSchedule(day: ScheduleDay) {
        viewModelScope.launch {
            repository.getSchedules(day.displayNamePt).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> ScheduleUiState.Loading
                    is Resource.Success -> ScheduleUiState.Success(resource.data)
                    is Resource.Error -> ScheduleUiState.Error(resource.message)
                }
            }
        }
    }
}
