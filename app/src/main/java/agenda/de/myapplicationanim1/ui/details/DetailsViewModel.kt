package agenda.de.myapplicationanim1.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import agenda.de.myapplicationanim1.data.remote.RetrofitClient
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repository: AnimeRepository = AnimeRepositoryImpl(RetrofitClient.aniListApiService),
) : ViewModel() {

    private var currentAnimeId: Int? = null

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun loadAnimeDetails(animeId: Int) {
        if (currentAnimeId == animeId && _uiState.value is DetailsUiState.Success) return
        currentAnimeId = animeId
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            repository.getAnimeDetails(animeId).collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> DetailsUiState.Loading
                    is Resource.Success -> DetailsUiState.Success(resource.data)
                    is Resource.Error -> DetailsUiState.Error(resource.message)
                }
            }
        }
    }

    fun retry() {
        currentAnimeId?.let { id ->
            currentAnimeId = null
            loadAnimeDetails(id)
        }
    }
}
