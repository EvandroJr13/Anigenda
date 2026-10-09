package agenda.de.myapplicationanim1.ui.details

import agenda.de.myapplicationanim1.data.model.Anime

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val anime: Anime) : DetailsUiState
    data class Error(val message: String) : DetailsUiState
}
