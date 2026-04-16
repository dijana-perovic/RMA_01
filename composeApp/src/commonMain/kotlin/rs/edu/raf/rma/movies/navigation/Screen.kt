package rs.edu.raf.rma.movies.navigation

sealed class Screen(val route: String) {
    object MovieList : Screen("movie_list")
    object Filter : Screen("filter")
    object Detail : Screen("detail")
}