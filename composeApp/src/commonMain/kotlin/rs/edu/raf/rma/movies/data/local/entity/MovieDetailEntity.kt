package rs.edu.raf.rma.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetailEntity(
    @PrimaryKey val imdbId: String,
    val tmdbId: Int?,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val tagline: String?,
    val year: Int?,
    val runtime: Int?,
    val budget: Long?,
    val revenue: Long?,
    val languageCode: String?,
    val popularity: Float?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val tmdbRating: Float?,
    val posterPath: String?,
    val backdropPath: String?,
    val genresJson: String,
    val castJson: String,
    val imagesJson: String,
    val videosJson: String
)