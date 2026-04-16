package rs.edu.raf.rma.movies.ui.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class MovieListViewModel(
    private val movieRepository: MovieRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MovieListState())
    val state: StateFlow<MovieListState> = _state.asStateFlow()

    init {
        sendIntent(MovieListIntent.LoadMovies)
    }

    fun sendIntent(intent: MovieListIntent) {
        when (intent) {
            is MovieListIntent.LoadMovies -> loadMovies()
            is MovieListIntent.Retry -> loadMovies()
            is MovieListIntent.ChangeSort -> changeSort(intent.sortBy)
            is MovieListIntent.ApplyFilters -> applyFilters(intent.filters)
            is MovieListIntent.OpenFilter -> { }
            is MovieListIntent.OpenDetail -> { }
        }
    }

    fun selectMovie(movieId: String) {
        _state.update { it.copy(selectedMovieId = movieId) }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val movies = movieRepository.getMovies(_state.value.activeFilters)
                val config = _state.value.imageConfig ?: configRepository.getConfig()
                _state.update {
                    it.copy(
                        isLoading = false,
                        movies = movies,
                        totalCount = movies.size,
                        imageConfig = config
                    )
                }
            } catch (e: io.ktor.client.plugins.HttpRequestTimeoutException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Request timed out. Please try again."
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = when {
                            e.message?.contains("Unable to resolve host") == true ->
                                "No internet connection"
                            else -> e.message ?: "Something went wrong"
                        }
                    )
                }
            }
        }
    }

    private fun changeSort(sortBy: String) {
        _state.update {
            it.copy(
                sortBy = sortBy,
                activeFilters = it.activeFilters.copy(sortBy = sortBy)
            )
        }
        loadMovies()
    }

    private fun applyFilters(filters: FilterParams) {
        _state.update {
            it.copy(
                activeFilters = filters.copy(sortBy = it.sortBy)
            )
        }
        loadMovies()
    }
}