package rs.edu.raf.rma.movies.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.entity.QuizSessionEntity

@Dao
interface QuizDao {

    @Insert
    suspend fun insertSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions ORDER BY playedAt DESC")
    fun observeSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT MAX(score) FROM quiz_sessions")
    fun observeBestScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    fun observeSessionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movie_details WHERE imagesJson != '[]'")
    suspend fun getEligibleMovieCount(): Int
}