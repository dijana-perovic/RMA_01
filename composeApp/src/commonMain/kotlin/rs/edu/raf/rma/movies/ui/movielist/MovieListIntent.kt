package rs.edu.raf.rma.movies.ui.movielist

import rs.edu.raf.rma.movies.domain.model.FilterParams

sealed class MovieListIntent {
    object LoadMovies : MovieListIntent()
    object Retry : MovieListIntent()
    object OpenFilter : MovieListIntent()
    data class OpenDetail(val movieId: String) : MovieListIntent()
    data class ChangeSort(val sortBy: String) : MovieListIntent()
    data class ApplyFilters(val filters: FilterParams) : MovieListIntent()
    data class SelectMovie(val movieId: String) : MovieListIntent()
}