package rs.edu.raf.rma.movies.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.ui.movielist.MovieListItem

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = koinViewModel(),
    onMovieClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WatchlistContract.SideEffect.NavigateToDetail -> onMovieClick(effect.movieId)
                is WatchlistContract.SideEffect.ShowMessage -> { /* TODO Snackbar */ }
            }
        }
    }

    WatchlistScreen(
        state = state,
        onMovieClick = onMovieClick,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchlistScreen(
    state: WatchlistContract.UiState,
    onMovieClick: (String) -> Unit,
    eventPublisher: (WatchlistContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
                actions = {
                    IconButton(onClick = { eventPublisher(WatchlistContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.isOffline) "No internet connection"
                            else state.error.message ?: "Error",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { eventPublisher(WatchlistContract.UiEvent.Refresh) }) {
                            Text("Retry")
                        }
                    }
                }

                state.isEmpty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Watchlist is empty.\nTap 🔖 on a movie to add it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.watchlist.forEach { movie ->
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        MovieListItem(
                                            movie = movie,
                                            imageConfig = state.imageConfig,
                                            onClick = { onMovieClick(movie.imdbId) },
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            eventPublisher(
                                                WatchlistContract.UiEvent.RemoveFromWatchlist(movie.imdbId)
                                            )
                                        },
                                        modifier = Modifier.padding(end = 8.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove from watchlist",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}