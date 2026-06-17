package rs.edu.raf.rma.movies.ui.detail

import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.model.MovieImage
import rs.edu.raf.rma.movies.domain.model.MovieVideo

interface DetailContract {

    data class UiState(
        val isLoading: Boolean = false,
        val movie: MovieDetail? = null,
        val imageConfig: ImageConfig? = null,
        val isFavorite: Boolean = false,
        val isInWatchlist: Boolean = false,
        val isTogglingFavorite: Boolean = false,
        val isTogglingWatchlist: Boolean = false,
        val error: String? = null
    ) {
        val images: List<MovieImage> get() = movie?.images ?: emptyList()
        val cast: List<CastMember>   get() = movie?.cast   ?: emptyList()
        val videos: List<MovieVideo> get() = movie?.videos ?: emptyList()
        val trailerKey: String?      get() = videos.firstOrNull { it.site == "YouTube" }?.key
        val isSuccess: Boolean       get() = !isLoading && error == null && movie != null
        val isOffline: Boolean       get() = error?.contains("Unable to resolve host") == true
    }

    sealed class UiEvent {
        object LoadDetail      : UiEvent()
        object PlayTrailer     : UiEvent()
        object Retry           : UiEvent()
        object ToggleFavorite  : UiEvent()
        object ToggleWatchlist : UiEvent()
    }

    sealed class SideEffect {
        data class OpenUrl(val url: String)         : SideEffect()
        data class ShowMessage(val message: String) : SideEffect()
    }
}