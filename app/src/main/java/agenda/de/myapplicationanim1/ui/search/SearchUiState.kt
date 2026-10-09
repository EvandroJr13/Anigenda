package agenda.de.myapplicationanim1.ui.search

import agenda.de.myapplicationanim1.data.model.Anime

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val animeList: List<Anime>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
