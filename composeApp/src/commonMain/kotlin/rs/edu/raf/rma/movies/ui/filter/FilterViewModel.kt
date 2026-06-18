package rs.edu.raf.rma.movies.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.repository.GenreRepository

class FilterViewModel(
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FilterContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: FilterContract.UiState.() -> FilterContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<FilterContract.UiEvent>()
    fun setEvent(event: FilterContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<FilterContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeGenresFromRoom()
        setEvent(FilterContract.UiEvent.LoadGenresIfEmpty)
    }

    private fun observeGenresFromRoom() {
        genreRepository.observeGenres()
            .onEach { genres ->
                setState { copy(genres = genres, isLoadingGenres = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is FilterContract.UiEvent.Initialize ->
                        setState {
                            FilterContract.UiState.fromFilterParams(event.params).copy(
                                genres          = genres,
                                isLoadingGenres = isLoadingGenres
                            )
                        }

                    is FilterContract.UiEvent.UpdateSearch ->
                        setState { copy(searchQuery = event.query) }

                    is FilterContract.UiEvent.ToggleGenre -> {
                        val updated = _state.value.selectedGenreIds.toMutableSet().apply {
                            if (contains(event.genreId)) remove(event.genreId)
                            else add(event.genreId)
                        }
                        setState { copy(selectedGenreIds = updated) }
                    }

                    is FilterContract.UiEvent.SetYearFrom ->
                        setState { copy(yearFrom = event.year) }

                    is FilterContract.UiEvent.SetYearTo ->
                        setState { copy(yearTo = event.year) }

                    is FilterContract.UiEvent.SetMinRating ->
                        setState { copy(minRating = event.rating) }

                    is FilterContract.UiEvent.ClearAll ->
                        setState {
                            copy(
                                searchQuery = "",
                                selectedGenreIds = emptySet(),
                                yearFrom = null,
                                yearTo = null,
                                minRating = null
                            )
                        }

                    is FilterContract.UiEvent.Apply ->
                        _sideEffects.emit(
                            FilterContract.SideEffect.ApplyAndClose(_state.value.toFilterParams())
                        )

                    is FilterContract.UiEvent.RetryGenres -> syncGenres()

                    is FilterContract.UiEvent.LoadGenresIfEmpty -> syncGenres()
                }
            }
        }
    }

    private fun syncGenres() {
        viewModelScope.launch {
            setState { copy(isLoadingGenres = true, error = null) }
            runCatching { genreRepository.syncGenres() }
                .onFailure { e -> setState { copy(isLoadingGenres = false, error = e) } }
        }
    }
}