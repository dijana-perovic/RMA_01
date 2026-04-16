package rs.edu.raf.rma.movies.ui.filter

sealed class FilterIntent {
    data class UpdateSearch(val query: String) : FilterIntent()
    data class ToggleGenre(val genreId: Int) : FilterIntent()
    data class SetYearFrom(val year: Int?) : FilterIntent()
    data class SetYearTo(val year: Int?) : FilterIntent()
    data class SetMinRating(val rating: Float?) : FilterIntent()
    object Apply : FilterIntent()
    object ClearAll : FilterIntent()
}