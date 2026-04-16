package rs.edu.raf.rma.movies.ui.movielist

import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.Movie

data class MovieListState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val sortBy: String = "imdb_rating",
    val activeFilters: FilterParams = FilterParams(),
    val totalCount: Int = 0,
    val imageConfig: ImageConfig? = null,
    val selectedMovieId: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && error == null && movies.isEmpty()
    val isSuccess: Boolean get() = !isLoading && error == null && movies.isNotEmpty()
    val activeFilterCount: Int get() = listOfNotNull(
        if (activeFilters.selectedGenreIds.isNotEmpty()) true else null,
        activeFilters.yearFrom,
        activeFilters.yearTo,
        activeFilters.minRating,
        activeFilters.query.ifBlank { null }
    ).size
}