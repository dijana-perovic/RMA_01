package rs.edu.raf.rma.movies.ui.watchlist

import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.Movie

interface WatchlistContract {

    data class UiState(
        val watchlist: List<Movie> = emptyList(),
        val imageConfig: ImageConfig? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    ) {
        val isEmpty: Boolean   get() = !isLoading && error == null && watchlist.isEmpty()
        val isOffline: Boolean get() = error?.message?.contains("Unable to resolve host") == true
    }

    sealed class UiEvent {
        object LoadWatchlist : UiEvent()
        object Refresh : UiEvent()
        data class RemoveFromWatchlist(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val movieId: String) : SideEffect()
        data class ShowMessage(val message: String) : SideEffect()
    }
}