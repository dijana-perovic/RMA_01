package rs.edu.raf.rma.movies.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.core.auth.model.AuthState
import rs.edu.raf.rma.movies.ui.auth.AuthContract
import rs.edu.raf.rma.movies.ui.auth.AuthScreen
import rs.edu.raf.rma.movies.ui.auth.AuthViewModel
import rs.edu.raf.rma.movies.ui.detail.DetailScreen
import rs.edu.raf.rma.movies.ui.filter.FilterContract
import rs.edu.raf.rma.movies.ui.filter.FilterScreen
import rs.edu.raf.rma.movies.ui.filter.FilterViewModel
import rs.edu.raf.rma.movies.ui.movielist.MovieListContract
import rs.edu.raf.rma.movies.ui.movielist.MovieListScreen
import rs.edu.raf.rma.movies.ui.movielist.MovieListViewModel

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    val authStore: AuthStore = koinInject()
    val authState by authStore.authState.collectAsState()

    // Forced logout
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            val current = navController.currentBackStackEntry?.destination?.route
            if (current != null && current != Screen.Auth.route) {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val movieListViewModel: MovieListViewModel = koinViewModel()
    val filterViewModel: FilterViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(route = Screen.Auth.route) {
            val viewModel = koinViewModel<AuthViewModel>()
            LaunchedEffect(viewModel) {
                viewModel.sideEffects.collect { effect ->
                    when (effect) {
                        AuthContract.SideEffect.NavigateToHome ->
                            navController.navigate(Screen.MovieList.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                    }
                }
            }
            AuthScreen(viewModel = viewModel)
        }

        composable(route = Screen.MovieList.route) {
            MovieListScreen(
                onMovieClick = { movieId ->
                    navController.navigate(
                        Screen.Detail.createRoute(movieId)
                    )
                },
                onFilterClick = {
                    filterViewModel.setEvent(
                        FilterContract.UiEvent.Initialize(
                            movieListViewModel.state.value.activeFilters
                        )
                    )
                    navController.navigate(Screen.Filter.route)
                },
                viewModel = movieListViewModel
            )
        }

        composable(route = Screen.Filter.route) {
            LaunchedEffect(filterViewModel) {
                filterViewModel.sideEffects.collect { effect ->
                    when (effect) {
                        is FilterContract.SideEffect.ApplyAndClose -> {
                            movieListViewModel.setEvent(
                                MovieListContract.UiEvent.ApplyFilters(effect.params)
                            )
                            navController.popBackStack()
                        }
                    }
                }
            }
            FilterScreen(
                viewModel = filterViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType }
            )
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}