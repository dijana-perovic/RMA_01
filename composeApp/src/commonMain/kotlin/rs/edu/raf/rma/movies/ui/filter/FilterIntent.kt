package rs.edu.raf.rma.movies.ui.filter

import rs.edu.raf.rma.movies.domain.model.FilterParams

sealed class FilterIntent {
    data class Initialize(val params: FilterParams) : FilterIntent()
    data class UpdateSearch(val query: String) : FilterIntent()
    data class ToggleGenre(val genreId: Int) : FilterIntent()
    data class SetYearFrom(val year: Int?) : FilterIntent()
    data class SetYearTo(val year: Int?) : FilterIntent()
    data class SetMinRating(val rating: Float?) : FilterIntent()
    object Apply : FilterIntent()
    object ClearAll : FilterIntent()
    object RetryGenres : FilterIntent()
    object LoadGenresIfEmpty : FilterIntent()
}