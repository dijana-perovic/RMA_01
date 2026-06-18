package rs.edu.raf.rma.movies.navigation

sealed class Screen(val route: String) {
    object Auth      : Screen("auth")
    object MovieList : Screen("movie_list")
    object Filter    : Screen("filter")
    object Favorites : Screen("favorites")
    object Watchlist : Screen("watchlist")
    object Profile   : Screen("profile")
    object Detail    : Screen("detail/{movieId}") {
        fun createRoute(movieId: String) = "detail/$movieId"
    }
}