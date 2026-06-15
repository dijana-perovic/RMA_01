package rs.edu.raf.rma.movies.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.ui.detail.DetailScreen
import rs.edu.raf.rma.movies.ui.filter.FilterIntent
import rs.edu.raf.rma.movies.ui.filter.FilterScreen
import rs.edu.raf.rma.movies.ui.filter.FilterViewModel
import rs.edu.raf.rma.movies.ui.movielist.MovieListIntent
import rs.edu.raf.rma.movies.ui.movielist.MovieListScreen
import rs.edu.raf.rma.movies.ui.movielist.MovieListViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val movieListViewModel: MovieListViewModel = koinViewModel()
    val filterViewModel: FilterViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.MovieList.route
    ) {
        composable(Screen.MovieList.route) {
            MovieListScreen(
                onMovieClick = { movieId ->
                    movieListViewModel.sendIntent(MovieListIntent.SelectMovie(movieId))
                    navController.navigate(Screen.Detail.route)
                },
                onFilterClick = {
                    filterViewModel.sendIntent(
                        FilterIntent.Initialize(movieListViewModel.state.value.activeFilters)
                    )
                    filterViewModel.sendIntent(FilterIntent.LoadGenresIfEmpty)
                    navController.navigate(Screen.Filter.route)
                },
                viewModel = movieListViewModel
            )
        }

        composable(Screen.Filter.route) {
            FilterScreen(
                onBack = { navController.popBackStack() },
                onApply = { filterParams ->
                    movieListViewModel.sendIntent(
                        MovieListIntent.ApplyFilters(filterParams)
                    )
                    navController.popBackStack()
                },
                viewModel = filterViewModel
            )
        }

        composable(route = Screen.Detail.route) {
            val movieId = movieListViewModel.state.value.selectedMovieId
                ?: return@composable
            DetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}