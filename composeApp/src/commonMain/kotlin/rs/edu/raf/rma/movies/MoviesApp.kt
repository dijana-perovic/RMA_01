package rs.edu.raf.rma.movies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.navigation.AppNavGraph
import rs.edu.raf.rma.movies.ui.splash.BootState
import rs.edu.raf.rma.movies.ui.splash.SplashScreen
import rs.edu.raf.rma.movies.ui.splash.SplashViewModel

@Composable
fun MoviesApp() {
    val splashViewModel: SplashViewModel = koinViewModel()
    val bootState by splashViewModel.bootState.collectAsState()
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsState()

    when (bootState) {
        BootState.Loading   -> SplashScreen()
        is BootState.Failed -> SplashScreen()
        BootState.Success   -> {
            key(isLoggedIn) {
                AppNavGraph(
                    startDestination = if (isLoggedIn) Screen.MovieList.route
                    else Screen.Auth.route,
                )
            }
        }
    }
}