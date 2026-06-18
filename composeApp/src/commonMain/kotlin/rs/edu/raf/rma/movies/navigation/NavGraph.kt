package rs.edu.raf.rma.movies.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import rs.edu.raf.rma.movies.ui.favorites.FavoritesScreen
import rs.edu.raf.rma.movies.ui.filter.FilterContract
import rs.edu.raf.rma.movies.ui.filter.FilterScreen
import rs.edu.raf.rma.movies.ui.filter.FilterViewModel
import rs.edu.raf.rma.movies.ui.movielist.MovieListContract
import rs.edu.raf.rma.movies.ui.movielist.MovieListScreen
import rs.edu.raf.rma.movies.ui.movielist.MovieListViewModel
import rs.edu.raf.rma.movies.ui.profile.ProfileScreen
import rs.edu.raf.rma.movies.ui.quiz.QuizResultScreen
import rs.edu.raf.rma.movies.ui.quiz.QuizScreen
import rs.edu.raf.rma.movies.ui.watchlist.WatchlistScreen

private val bottomNavRoutes = setOf(
    Screen.MovieList.route,
    Screen.Favorites.route,
    Screen.Watchlist.route,
    Screen.Profile.route,
    Screen.Quiz.route,
)

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.MovieList.route,
                        onClick = {
                            navController.navigate(Screen.MovieList.route) {
                                popUpTo(Screen.MovieList.route) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.Movie, contentDescription = "Movies") },
                        label = { Text("Movies") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Favorites.route,
                        onClick = {
                            navController.navigate(Screen.Favorites.route) {
                                popUpTo(Screen.MovieList.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Watchlist.route,
                        onClick = {
                            navController.navigate(Screen.Watchlist.route) {
                                popUpTo(Screen.MovieList.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watchlist") },
                        label = { Text("Watchlist") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(Screen.MovieList.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Quiz.route,
                        onClick = {
                            navController.navigate(Screen.Quiz.route) {
                                popUpTo(Screen.MovieList.route)
                            }
                        },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz") },
                        label = { Text("Quiz") },
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Auth.route) {
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

            composable(Screen.MovieList.route) {
                val movieListViewModel: MovieListViewModel = koinViewModel()
                val filterViewModel: FilterViewModel = koinViewModel(
                    viewModelStoreOwner = it
                )

                MovieListScreen(
                    viewModel = movieListViewModel,
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    },
                    onFilterClick = {
                        filterViewModel.setEvent(
                            FilterContract.UiEvent.Initialize(
                                movieListViewModel.state.value.activeFilters
                            )
                        )
                        navController.navigate(Screen.Filter.route)
                    },
                )
            }

            composable(Screen.Filter.route) {
                val movieListEntry = remember {
                    navController.getBackStackEntry(Screen.MovieList.route)
                }
                val movieListViewModel: MovieListViewModel = koinViewModel(
                    viewModelStoreOwner = movieListEntry
                )
                val filterViewModel: FilterViewModel = koinViewModel(
                    viewModelStoreOwner = movieListEntry
                )

                FilterScreen(
                    viewModel = filterViewModel,
                    onBack = { navController.popBackStack() },
                    onApply = { filterParams ->
                        movieListViewModel.setEvent(
                            MovieListContract.UiEvent.ApplyFilters(filterParams)
                        )
                        navController.popBackStack()
                    },
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

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    }
                )
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.Quiz.route) {
                QuizScreen(
                    onNavigateToResult = { result ->
                        navController.navigate(
                            Screen.QuizResult.createRoute(
                                score    = result.score,
                                correct  = result.correctAnswers,
                                incorrect = result.incorrectAnswers,
                                time     = result.timeUsedSeconds,
                            )
                        ) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.navigate(Screen.MovieList.route) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument("score")            { type = NavType.FloatType },
                    navArgument("correctAnswers")   { type = NavType.IntType },
                    navArgument("incorrectAnswers") { type = NavType.IntType },
                    navArgument("timeUsedSeconds")  { type = NavType.IntType },
                ),
            ) {
                QuizResultScreen(
                    onPlayAgain = {
                        navController.navigate(Screen.Quiz.route) {
                            popUpTo(Screen.QuizResult.route) { inclusive = true }
                        }
                    },
                    onGoHome = {
                        navController.navigate(Screen.MovieList.route) {
                            popUpTo(Screen.QuizResult.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}