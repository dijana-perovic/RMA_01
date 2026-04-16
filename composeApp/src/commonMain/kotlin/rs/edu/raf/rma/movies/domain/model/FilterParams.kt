package rs.edu.raf.rma.movies.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FilterParams(
    val query: String = "",
    val selectedGenreIds: Set<Int> = emptySet(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val minRating: Float? = null,
    val sortBy: String = "imdb_rating"
) {
    val genreId: Int? get() = selectedGenreIds.firstOrNull()
}
