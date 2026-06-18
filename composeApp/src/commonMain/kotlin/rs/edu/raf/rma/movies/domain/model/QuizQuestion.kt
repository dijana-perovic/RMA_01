package rs.edu.raf.rma.movies.domain.model

sealed class QuizQuestion {
    abstract val movieId: String
    abstract val options: List<String>
    abstract val correctAnswer: String

    data class GuessTheMovie(
        override val movieId: String,
        val imageUrl: String,
        override val options: List<String>,  // naslovi filmova
        override val correctAnswer: String,  // tačan naslov
    ) : QuizQuestion()

    data class GuessTheYear(
        override val movieId: String,
        val posterUrl: String,
        val movieTitle: String,
        override val options: List<String>,  // godine kao String
        override val correctAnswer: String,  // tačna godina
    ) : QuizQuestion()

    data class GuessTheActor(
        override val movieId: String,
        val posterUrl: String,
        val movieTitle: String,
        override val options: List<String>,  // imena glumaca
        override val correctAnswer: String,  // tačno ime
    ) : QuizQuestion()
}