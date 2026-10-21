package agenda.de.myapplicationanim1.ui.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PopularUiState {
    data object Loading : PopularUiState
    data class Success(val animeList: List<Anime>) : PopularUiState
    data class Error(val message: String) : PopularUiState
}

class PopularViewModel(
    private val repository: AnimeRepository = AnimeRepositoryImpl(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<PopularUiState>(PopularUiState.Loading)
    val uiState: StateFlow<PopularUiState> = _uiState.asStateFlow()

    init {
        loadPopularAnime()
    }

    fun loadPopularAnime() {
        viewModelScope.launch {
            repository.getPopularAnime().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { PopularUiState.Loading }
                    }
                    is Resource.Success -> {
                        _uiState.update { PopularUiState.Success(resource.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { PopularUiState.Error(resource.message ?: "Ocorreu um erro desconhecido") }
                    }
                }
            }
        }
    }

    fun refresh() {
        loadPopularAnime()
    }
}
