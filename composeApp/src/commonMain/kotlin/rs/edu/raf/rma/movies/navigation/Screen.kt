sealed class Screen(val route: String) {
    object Auth        : Screen("auth")
    object MovieList   : Screen("movie_list")
    object Filter      : Screen("filter")
    object Favorites   : Screen("favorites")
    object Watchlist   : Screen("watchlist")
    object Profile     : Screen("profile")
    object Quiz        : Screen("quiz")
    object QuizResult  : Screen("quiz_result/{score}/{correctAnswers}/{incorrectAnswers}/{timeUsedSeconds}") {
        fun createRoute(score: Double, correct: Int, incorrect: Int, time: Int) =
            "quiz_result/$score/$correct/$incorrect/$time"
    }
    object Detail      : Screen("detail/{movieId}") {
        fun createRoute(movieId: String) = "detail/$movieId"
    }
}