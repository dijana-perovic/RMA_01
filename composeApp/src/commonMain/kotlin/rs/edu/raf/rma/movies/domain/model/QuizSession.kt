package rs.edu.raf.rma.movies.domain.model

data class QuizSession(
    val id: Long,
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val timeUsedSeconds: Int,
    val playedAt: Long,
)