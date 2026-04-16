package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.Movie

@Serializable
data class MovieDto(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreDto> = emptyList()
)

fun MovieDto.toDomain() = Movie(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating?.toDouble(),
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genres = genres.map { it.toDomain() }
)