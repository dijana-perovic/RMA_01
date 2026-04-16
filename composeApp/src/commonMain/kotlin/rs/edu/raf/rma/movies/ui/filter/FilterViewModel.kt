package rs.edu.raf.rma.movies.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.repository.GenreRepository

class FilterViewModel(
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FilterState())
    val state: StateFlow<FilterState> = _state.asStateFlow()

    init {
        loadGenres()
    }

    fun initWithFilters(params: FilterParams) {
        _state.update { current ->
            FilterState.fromFilterParams(params).copy(
                genres = current.genres,
                isLoadingGenres = current.isLoadingGenres
            )
        }
    }

    fun sendIntent(intent: FilterIntent) {
        when (intent) {
            is FilterIntent.UpdateSearch -> _state.update {
                it.copy(searchQuery = intent.query)
            }
            is FilterIntent.ToggleGenre -> _state.update { state ->
                val updated = state.selectedGenreIds.toMutableSet().apply {
                    if (contains(intent.genreId)) remove(intent.genreId)
                    else add(intent.genreId)
                }
                state.copy(selectedGenreIds = updated)
            }
            is FilterIntent.SetYearFrom -> _state.update {
                it.copy(yearFrom = intent.year)
            }
            is FilterIntent.SetYearTo -> _state.update {
                it.copy(yearTo = intent.year)
            }
            is FilterIntent.SetMinRating -> _state.update {
                it.copy(minRating = intent.rating)
            }
            is FilterIntent.ClearAll -> _state.update {
                it.copy(
                    searchQuery = "",
                    selectedGenreIds = emptySet(),
                    yearFrom = null,
                    yearTo = null,
                    minRating = null
                )
            }
            is FilterIntent.Apply -> { }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingGenres = true, error = null) }
            try {
                val genres = genreRepository.getGenres()
                _state.update { it.copy(genres = genres, isLoadingGenres = false) }
            } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
                _state.update {
                    it.copy(
                        isLoadingGenres = false,
                        error = "Request timed out. Please try again."
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingGenres = false,
                        error = when {
                            e.message?.contains("Unable to resolve host") == true ->
                                "No internet connection"
                            else -> e.message ?: "Could not load genres"
                        }
                    )
                }
            }
        }
    }

    fun retryLoadGenres() {
        loadGenres()
    }

    fun loadGenresIfEmpty() {
        if (_state.value.genres.isEmpty()) {
            loadGenres()
        }
    }
}