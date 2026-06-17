package rs.edu.raf.rma.movies.ui.movielist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovieListScreen(
    onMovieClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    viewModel: MovieListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    MovieListScreen(
        state = state,
        onMovieClick = onMovieClick,
        onFilterClick = onFilterClick,
        eventPublisher = viewModel::setEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieListScreen(
    state: MovieListContract.UiState,
    onMovieClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    eventPublisher: (MovieListContract.UiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.activeFilterCount > 0) {
                                Badge { Text(state.activeFilterCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onFilterClick) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SortBar(
                currentSort = state.sortBy,
                totalCount = state.totalCount,
                onSortChange = { eventPublisher(MovieListContract.UiEvent.ChangeSort(it)) }
            )

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.isOffline) "No internet connection"
                            else state.error.message ?: "Error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { eventPublisher(MovieListContract.UiEvent.Refresh) }) {
                            Text("Retry")
                        }
                    }
                }

                state.isEmpty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No movies found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.isSuccess -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    state.movies.forEach { movie ->
                        MovieListItem(
                            movie = movie,
                            imageConfig = state.imageConfig,
                            onClick = { onMovieClick(movie.imdbId) }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SortBar(
    currentSort: String,
    totalCount: Int,
    onSortChange: (String) -> Unit
) {
    val sortOptions = listOf(
        "imdb_rating" to "Rating",
        "year"        to "Year",
        "title"       to "Title",
    )
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$totalCount movies",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = {
                    Text(sortOptions.find { it.first == currentSort }?.second ?: "Rating")
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sortOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onSortChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}