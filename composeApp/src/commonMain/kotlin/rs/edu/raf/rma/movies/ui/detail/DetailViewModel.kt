package rs.edu.raf.rma.movies.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class DetailViewModel(
    private val movieRepository: MovieRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    fun sendIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadDetail -> loadDetail(intent.movieId)
            is DetailIntent.Retry -> {
                _state.value.movie?.imdbId?.let { loadDetail(it) }
            }
            is DetailIntent.PlayTrailer -> {
                _state.value.trailerKey?.let { key ->
                    _state.update {
                        it.copy(trailerUrl = "https://www.youtube.com/watch?v=$key")
                    }
                }
            }
        }
    }

    fun clearTrailerUrl() {
        _state.update { it.copy(trailerUrl = null) }
    }

    private fun loadDetail(movieId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val movie   = movieRepository.getMovieDetail(movieId)
                val images  = movieRepository.getImages(movieId)
                val cast    = movieRepository.getCast(movieId)
                val videos  = movieRepository.getVideos(movieId)
                val config  = configRepository.getConfig()

                _state.update {
                    it.copy(
                        isLoading   = false,
                        movie       = movie,
                        images      = images,
                        cast        = cast,
                        videos      = videos,
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
}