package rs.edu.raf.rma.movies.domain.model

data class Movie(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val genres: List<Genre>
)
