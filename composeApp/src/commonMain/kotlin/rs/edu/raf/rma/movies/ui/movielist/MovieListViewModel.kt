package rs.edu.raf.rma.movies.ui.movielist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class MovieListViewModel(
    private val movieRepository: MovieRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MovieListContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MovieListContract.UiState.() -> MovieListContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<MovieListContract.UiEvent>()
    fun setEvent(event: MovieListContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<MovieListContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    private val filterFlow = MutableStateFlow(FilterParams())

    init {
        observeEvents()
        observeMoviesFromRoom()
        loadConfig()
        syncMovies()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMoviesFromRoom() {
        filterFlow
            .flatMapLatest { filters -> movieRepository.observeMovies(filters) }
            .onEach { movies ->
                setState { copy(movies = movies, isLoading = false, error = null) }
            }
            .catch { e -> setState { copy(isLoading = false, error = e) } }
            .launchIn(viewModelScope)
    }

    private fun loadConfig() {
        viewModelScope.launch {
            runCatching { configRepository.getConfig() }
                .onSuccess { config -> setState { copy(imageConfig = config) } }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is MovieListContract.UiEvent.LoadMovies -> syncMovies()
                    is MovieListContract.UiEvent.Refresh    -> syncMovies()

                    is MovieListContract.UiEvent.ChangeSort -> {
                        val newFilters = filterFlow.value.copy(sortBy = event.sortBy)
                        filterFlow.value = newFilters
                        setState { copy(sortBy = event.sortBy, activeFilters = newFilters) }
                        syncMovies()
                    }

                    is MovieListContract.UiEvent.ApplyFilters -> {
                        val newFilters = event.filters.copy(sortBy = _state.value.sortBy)
                        filterFlow.value = newFilters
                        setState { copy(activeFilters = newFilters) }
                        syncMovies()
                    }
                }
            }
        }
    }

    private fun syncMovies() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            movieRepository.syncMovies(filterFlow.value)
        }
    }
}