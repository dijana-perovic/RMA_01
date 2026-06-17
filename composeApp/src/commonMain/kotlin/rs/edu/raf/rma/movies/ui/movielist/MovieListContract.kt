package rs.edu.raf.rma.movies.ui.movielist

import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.Movie

interface MovieListContract {

    data class UiState(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val error: Throwable? = null,
        val sortBy: String = "imdb_rating",
        val activeFilters: FilterParams = FilterParams(),
        val imageConfig: ImageConfig? = null
    ) {
        val isEmpty: Boolean   get() = !isLoading && error == null && movies.isEmpty()
        val isOffline: Boolean get() = error?.message?.contains("Unable to resolve host") == true
        val isSuccess: Boolean get() = !isLoading && error == null && movies.isNotEmpty()
        val totalCount: Int    get() = movies.size

        val activeFilterCount: Int get() = listOfNotNull(
            activeFilters.selectedGenreIds.takeIf { it.isNotEmpty() },
            activeFilters.yearFrom,
            activeFilters.yearTo,
            activeFilters.minRating,
            activeFilters.query.ifBlank { null }
        ).size
    }

    sealed class UiEvent {
        data object LoadMovies                             : UiEvent()
        data object Refresh                                : UiEvent()
        data class ChangeSort(val sortBy: String)          : UiEvent()
        data class ApplyFilters(val filters: FilterParams) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val movieId: String) : SideEffect()
    }
}