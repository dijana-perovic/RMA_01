package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.QuizSessionEntity

@Dao
interface QuizDao {

    @Upsert
    suspend fun upsertSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions ORDER BY playedAt DESC")
    fun observeSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT MAX(score) FROM quiz_sessions")
    fun observeBestScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    fun observeSessionCount(): Flow<Int>

    @Query("DELETE FROM quiz_sessions")
    suspend fun clearAll()
}