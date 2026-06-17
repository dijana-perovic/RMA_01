package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.MovieDetailEntity
import rs.edu.raf.rma.movies.data.local.entity.MovieEntity

@Dao
interface MovieDao {

    @Query("""
        SELECT * FROM movies
        WHERE (:query = '' OR title LIKE '%' || :query || '%')
          AND (:minRating IS NULL OR imdbRating >= :minRating)
          AND (:yearFrom  IS NULL OR year >= :yearFrom)
          AND (:yearTo    IS NULL OR year <= :yearTo)
        ORDER BY
          CASE WHEN :sortBy = 'imdb_rating' THEN imdbRating END DESC,
          CASE WHEN :sortBy = 'year'        THEN year       END DESC,
          CASE WHEN :sortBy = 'title'       THEN title      END ASC,
          imdbRating DESC
    """)
    fun observeFilteredMovies(
        query: String,
        minRating: Float?,
        yearFrom: Int?,
        yearTo: Int?,
        sortBy: String,
    ): Flow<List<MovieEntity>>

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getCount(): Int

    @Query("SELECT * FROM movie_details WHERE imdbId = :id")
    fun observeMovieDetail(id: String): Flow<MovieDetailEntity?>

    @Upsert
    suspend fun upsertMovieDetail(detail: MovieDetailEntity)
}