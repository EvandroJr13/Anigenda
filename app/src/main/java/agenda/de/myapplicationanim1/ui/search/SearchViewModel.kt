package agenda.de.myapplicationanim1.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import agenda.de.myapplicationanim1.data.model.Anime
import agenda.de.myapplicationanim1.data.repository.AnimeRepository
import agenda.de.myapplicationanim1.data.repository.AnimeRepositoryImpl
import agenda.de.myapplicationanim1.data.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: AnimeRepository = AnimeRepositoryImpl(),
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val availableGenres = listOf(
        "Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Mecha", "Mystery", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller"
    )

    val availableStatuses = listOf(
        "Airing", "Finished"
    )

    private var searchJob: Job? = null
    private var rawSearchResults: List<Anime> = emptyList()

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.trim().isEmpty() && _selectedGenre.value == null && _selectedStatus.value == null) {
            rawSearchResults = emptyList()
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // 400ms Debounce
            executeSearch()
        }
    }

    fun onSearchClicked() {
        searchJob?.cancel()
        executeSearch()
    }

    fun clearQuery() {
        _query.value = ""
        if (_selectedGenre.value == null && _selectedStatus.value == null) {
            rawSearchResults = emptyList()
            _uiState.value = SearchUiState.Idle
        } else {
            executeSearch()
        }
    }

    fun toggleGenre(genre: String) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
        executeSearch()
    }

    fun toggleStatus(status: String) {
        _selectedStatus.value = if (_selectedStatus.value == status) null else status
        applyFiltersOrSearch()
    }

    fun retry() {
        executeSearch()
    }

    private fun applyFiltersOrSearch() {
        if (_query.value.trim().isEmpty() && _selectedGenre.value == null && _selectedStatus.value == null) {
            rawSearchResults = emptyList()
            _uiState.value = SearchUiState.Idle
        } else if (rawSearchResults.isNotEmpty()) {
            val filtered = filterList(rawSearchResults)
            _uiState.value = SearchUiState.Success(filtered)
        } else {
            executeSearch()
        }
    }

    private fun executeSearch() {
        val q = _query.value.trim()
        val genreFilter = _selectedGenre.value

        if (q.isEmpty() && genreFilter == null && _selectedStatus.value == null) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            repository.searchAnime(query = q, genre = genreFilter).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = SearchUiState.Loading
                    is Resource.Error -> _uiState.value = SearchUiState.Error(resource.message)
                    is Resource.Success -> {
                        rawSearchResults = resource.data
                        val filteredList = filterList(rawSearchResults)
                        _uiState.value = SearchUiState.Success(filteredList)
                    }
                }
            }
        }
    }

    private fun filterList(list: List<Anime>): List<Anime> {
        var result = list
        val selectedStatusVal = _selectedStatus.value
        if (selectedStatusVal != null) {
            result = result.filter { anime ->
                if (selectedStatusVal.equals("Airing", ignoreCase = true)) {
                    anime.status.contains("Airing", ignoreCase = true) || anime.status.contains("RELEASING", ignoreCase = true)
                } else if (selectedStatusVal.equals("Finished", ignoreCase = true)) {
                    anime.status.contains("Finished", ignoreCase = true) || anime.status.contains("FINISHED", ignoreCase = true)
                } else {
                    anime.status.contains(selectedStatusVal, ignoreCase = true)
                }
            }
        }
        return result
    }
}
