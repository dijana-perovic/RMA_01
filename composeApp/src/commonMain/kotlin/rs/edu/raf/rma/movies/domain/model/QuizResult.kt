package rs.edu.raf.rma.movies.domain.model

data class QuizResult(
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val timeUsedSeconds: Int,
)