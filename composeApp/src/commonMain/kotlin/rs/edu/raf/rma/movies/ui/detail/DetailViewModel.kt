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
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val movieId: String = savedStateHandle["movieId"]
        ?: throw IllegalStateException("movieId is mandatory")

    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private fun setState(reducer: DetailState.() -> DetailState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<DetailIntent>()
    fun setEvent(event: DetailIntent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<DetailSideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
        observeMovieDetail()
        setEvent(DetailIntent.LoadDetail)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is DetailIntent.LoadDetail -> loadDetail()
                    is DetailIntent.Retry      -> loadDetail()
                    is DetailIntent.PlayTrailer -> {
                        _state.value.trailerKey?.let { key ->
                            _sideEffects.emit(
                                DetailSideEffect.OpenUrl(
                                    "https://www.youtube.com/watch?v=$key"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeMovieDetail() {
        movieRepository.observeMovieDetail(movieId)
            .onEach { detail ->
                if (detail != null) {
                    setState {
                        copy(
                            isLoading = false,
                            movie     = detail,
                            error     = null
                        )
                    }
                }
            }
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
}