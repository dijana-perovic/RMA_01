package rs.edu.raf.rma.movies.ui.filter

import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Genre

data class FilterState(
    val searchQuery: String = "",
    val selectedGenreIds: Set<Int> = emptySet(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val minRating: Float? = null,
    val genres: List<Genre> = emptyList(),
    val isLoadingGenres: Boolean = false,
    val error: String? = null
) {
    fun toFilterParams() = FilterParams(
        query = searchQuery,
        selectedGenreIds = selectedGenreIds,
        yearFrom = yearFrom,
        yearTo = yearTo,
        minRating = minRating,
        sortBy = "imdb_rating"
    )

    companion object {
        fun fromFilterParams(params: FilterParams) = FilterState(
            searchQuery = params.query,
            selectedGenreIds = params.selectedGenreIds,
            yearFrom = params.yearFrom,
            yearTo = params.yearTo,
            minRating = params.minRating
        )
    }
}