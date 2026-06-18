package rs.edu.raf.rma.movies.ui.favorites

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

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: FavoritesContract.UiState.() -> FavoritesContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<FavoritesContract.UiEvent>()
    fun setEvent(event: FavoritesContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<FavoritesContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeFavoritesFromRoom()
        loadConfig()
        setEvent(FavoritesContract.UiEvent.LoadFavorites)
    }

    private fun observeFavoritesFromRoom() {
        favoriteRepository.observeFavorites()
            .onEach { favorites ->
                setState { copy(favorites = favorites, isLoading = false, error = null) }
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
                    is FavoritesContract.UiEvent.LoadFavorites -> syncFavorites()
                    is FavoritesContract.UiEvent.Refresh       -> syncFavorites()
                    is FavoritesContract.UiEvent.RemoveFavorite -> {
                        runCatching { favoriteRepository.removeFavorite(event.imdbId) }
                            .onFailure {
                                _sideEffects.emit(
                                    FavoritesContract.SideEffect.ShowMessage(
                                        "Failed to remove from favorites"
                                    )
                                )
                            }
                    }
                }
            }
        }
    }

    private fun syncFavorites() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { favoriteRepository.syncFavorites() }
                .onFailure { e -> setState { copy(isLoading = false, error = e) } }
            setState { copy(isLoading = false) }
        }
    }
}