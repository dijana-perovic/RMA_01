package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.MovieDetail

@Serializable
data class MovieDetailDto(
    val imdbId: String,
    val tmdbId: Int? = null,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val languageCode: String? = null,
    val popularity: Float? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Float? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val genres: List<GenreDto> = emptyList()
)

fun MovieDetailDto.toDomain() = MovieDetail(
    imdbId = imdbId,
    tmdbId = tmdbId,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    year = year,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    languageCode = languageCode,
    popularity = popularity,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    tmdbRating = tmdbRating,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genres.map { it.toDomain() }
)