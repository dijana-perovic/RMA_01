package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.WatchlistEntity

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun observeWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :imdbId)")
    fun observeIsInWatchlist(imdbId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE imdbId = :imdbId")
    suspend fun delete(imdbId: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(entities: List<WatchlistEntity>) {
        clearAll()
        entities.forEach { upsert(it) }
    }
}