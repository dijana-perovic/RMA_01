package rs.edu.raf.rma.movies.ui.watchlist

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
import rs.edu.raf.rma.movies.domain.repository.WatchlistRepository

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: WatchlistContract.UiState.() -> WatchlistContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<WatchlistContract.UiEvent>()
    fun setEvent(event: WatchlistContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<WatchlistContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeWatchlistFromRoom()
        loadConfig()
        setEvent(WatchlistContract.UiEvent.LoadWatchlist)
    }

    private fun observeWatchlistFromRoom() {
        watchlistRepository.observeWatchlist()
            .onEach { watchlist ->
                setState { copy(watchlist = watchlist, isLoading = false, error = null) }
            }
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
                    is WatchlistContract.UiEvent.LoadWatchlist -> syncWatchlist()
                    is WatchlistContract.UiEvent.Refresh       -> syncWatchlist()
                    is WatchlistContract.UiEvent.RemoveFromWatchlist -> {
                        runCatching { watchlistRepository.removeFromWatchlist(event.imdbId) }
                            .onFailure {
                                _sideEffects.emit(
                                    WatchlistContract.SideEffect.ShowMessage(
                                        "Failed to remove from watchlist"
                                    )
                                )
                            }
                    }
                }
            }
        }
    }

    private fun syncWatchlist() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { watchlistRepository.syncWatchlist() }
                .onFailure { e -> setState { copy(isLoading = false, error = e) } }
            setState { copy(isLoading = false) }
        }
    }
}