package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :imdbId)")
    fun observeIsFavorite(imdbId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE imdbId = :imdbId")
    suspend fun delete(imdbId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeCount(): Flow<Int>

    @Transaction
    suspend fun replaceAll(entities: List<FavoriteEntity>) {
        clearAll()
        entities.forEach { upsert(it) }
    }
}