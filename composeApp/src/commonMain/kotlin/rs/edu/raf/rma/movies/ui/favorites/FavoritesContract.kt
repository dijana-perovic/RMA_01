package rs.edu.raf.rma.movies.ui.favorites

import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.Movie

interface FavoritesContract {

    data class UiState(
        val favorites: List<Movie> = emptyList(),
        val imageConfig: ImageConfig? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    ) {
        val isEmpty: Boolean   get() = !isLoading && error == null && favorites.isEmpty()
        val isOffline: Boolean get() = error?.message?.contains("Unable to resolve host") == true
    }

    sealed class UiEvent {
        object LoadFavorites                          : UiEvent()
        object Refresh                                : UiEvent()
        data class RemoveFavorite(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val movieId: String) : SideEffect()
        data class ShowMessage(val message: String)      : SideEffect()
    }
}