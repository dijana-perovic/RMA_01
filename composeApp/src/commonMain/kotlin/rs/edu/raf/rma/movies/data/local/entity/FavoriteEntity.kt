package rs.edu.raf.rma.movies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Double?,
    val posterPath: String?,
    val genresJson: String,
    val addedAt: Long = Clock.System.now().toEpochMilliseconds()
)