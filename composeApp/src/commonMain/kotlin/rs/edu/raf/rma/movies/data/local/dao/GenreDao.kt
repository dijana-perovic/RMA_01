package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.GenreEntity

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)
}