package rs.edu.raf.rma.movies.ui.profile

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
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.domain.repository.FavoriteRepository
import rs.edu.raf.rma.movies.domain.repository.ProfileRepository
import rs.edu.raf.rma.movies.domain.repository.QuizRepository
import rs.edu.raf.rma.movies.domain.repository.WatchlistRepository

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authStore: AuthStore,
    private val favoriteRepository: FavoriteRepository,
    private val watchlistRepository: WatchlistRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: ProfileContract.UiState.() -> ProfileContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<ProfileContract.UiEvent>()
    fun setEvent(event: ProfileContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<ProfileContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeCounts()
        observeQuizStats()
        setEvent(ProfileContract.UiEvent.LoadProfile)
        syncLists()
        syncQuizResults()
    }

    private fun observeCounts() {
        profileRepository.observeFavoritesCount()
            .onEach { count -> setState { copy(favoritesCount = count) } }
            .launchIn(viewModelScope)

        profileRepository.observeWatchlistCount()
            .onEach { count -> setState { copy(watchlistCount = count) } }
            .launchIn(viewModelScope)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is ProfileContract.UiEvent.LoadProfile -> loadProfile()
                    is ProfileContract.UiEvent.Logout      -> logout()
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { profileRepository.getMe() }
                .fold(
                    onSuccess = { user ->
                        setState {
                            copy(
                                isLoading = false,
                                username  = user.username,
                                fullName  = user.fullName,
                            )
                        }
                    },
                    onFailure = { error ->
                        setState { copy(isLoading = false, error = error) }
                    }
                )
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // Briše lokalne korisničke podatke
            favoriteRepository.clearAll()
            watchlistRepository.clearAll()
            quizRepository.clearAll()
            // Briše token
            authStore.clearAuthData()
        }
    }

    private fun syncLists() {
        viewModelScope.launch {
            favoriteRepository.syncFavorites()
            watchlistRepository.syncWatchlist()
        }
    }

    private fun observeQuizStats() {
        quizRepository.observeBestScore()
            .onEach { score -> setState { copy(bestScore = score) } }
            .launchIn(viewModelScope)

        quizRepository.observeSessionCount()
            .onEach { count -> setState { copy(quizPlayed = count) } }
            .launchIn(viewModelScope)
    }

    private fun syncQuizResults() {
        viewModelScope.launch {
            quizRepository.syncQuizResults()
        }
    }
}