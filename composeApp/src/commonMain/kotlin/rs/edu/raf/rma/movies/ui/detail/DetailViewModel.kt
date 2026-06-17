package rs.edu.raf.rma.movies.ui.detail

import androidx.lifecycle.SavedStateHandle
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
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.FavoriteRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository
import rs.edu.raf.rma.movies.domain.repository.WatchlistRepository

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val configRepository: ConfigRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {

    private val movieId: String = savedStateHandle["movieId"]
        ?: throw IllegalStateException("movieId is mandatory")

    private val _state = MutableStateFlow(DetailContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: DetailContract.UiState.() -> DetailContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<DetailContract.UiEvent>()
    fun setEvent(event: DetailContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<DetailContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeMovieDetail()
        observeFavoriteStatus()
        observeWatchlistStatus()
        setEvent(DetailContract.UiEvent.LoadDetail)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is DetailContract.UiEvent.LoadDetail -> loadDetail()
                    is DetailContract.UiEvent.Retry      -> loadDetail()
                    is DetailContract.UiEvent.PlayTrailer -> {
                        _state.value.trailerKey?.let { key ->
                            _sideEffects.emit(
                                DetailContract.SideEffect.OpenUrl(
                                    "https://www.youtube.com/watch?v=$key"
                                )
                            )
                        }
                    }
                    is DetailContract.UiEvent.ToggleFavorite  -> toggleFavorite()
                    is DetailContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
                }
            }
        }
    }

    private fun observeMovieDetail() {
        movieRepository.observeMovieDetail(movieId)
            .onEach { detail ->
                if (detail != null) {
                    setState { copy(isLoading = false, movie = detail, error = null) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFavoriteStatus() {
        favoriteRepository.observeIsFavorite(movieId)
            .onEach { isFavorite -> setState { copy(isFavorite = isFavorite) } }
            .launchIn(viewModelScope)
    }

    private fun observeWatchlistStatus() {
        watchlistRepository.observeIsInWatchlist(movieId)
            .onEach { isInWatchlist -> setState { copy(isInWatchlist = isInWatchlist) } }
            .launchIn(viewModelScope)
    }

    private fun loadDetail() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            movieRepository.syncMovieDetail(movieId)
            if (_state.value.imageConfig == null) {
                runCatching { configRepository.getConfig() }
                    .onSuccess { config -> setState { copy(imageConfig = config) } }
            }
        }
    }

    private fun toggleFavorite() {
        val currentlyFavorite = _state.value.isFavorite
        viewModelScope.launch {
            setState { copy(isTogglingFavorite = true) }
            runCatching {
                if (currentlyFavorite) favoriteRepository.removeFavorite(movieId)
                else favoriteRepository.addFavorite(movieId)
            }.onFailure {
                _sideEffects.emit(
                    DetailContract.SideEffect.ShowMessage(
                        if (currentlyFavorite) "Failed to remove from favorites"
                        else "Failed to add to favorites"
                    )
                )
            }
            setState { copy(isTogglingFavorite = false) }
        }
    }

    private fun toggleWatchlist() {
        val currentlyInWatchlist = _state.value.isInWatchlist
        viewModelScope.launch {
            setState { copy(isTogglingWatchlist = true) }
            runCatching {
                if (currentlyInWatchlist) watchlistRepository.removeFromWatchlist(movieId)
                else watchlistRepository.addToWatchlist(movieId)
            }.onFailure {
                _sideEffects.emit(
                    DetailContract.SideEffect.ShowMessage(
                        if (currentlyInWatchlist) "Failed to remove from watchlist"
                        else "Failed to add to watchlist"
                    )
                )
            }
            setState { copy(isTogglingWatchlist = false) }
        }
    }
}