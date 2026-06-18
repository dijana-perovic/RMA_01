package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.model.QuizQuestion
import rs.edu.raf.rma.movies.domain.model.QuizResult
import rs.edu.raf.rma.movies.domain.model.QuizSession

interface QuizRepository {
    suspend fun generateQuestions(): List<QuizQuestion>
    suspend fun saveResult(result: QuizResult)
    fun observeBestScore(): Flow<Double?>
    fun observeSessionCount(): Flow<Int>
    fun observeSessions(): Flow<List<QuizSession>>
    suspend fun hasEnoughMovies(): Boolean
}