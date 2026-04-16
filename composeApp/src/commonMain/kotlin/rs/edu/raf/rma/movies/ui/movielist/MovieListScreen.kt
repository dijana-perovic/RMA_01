package rs.edu.raf.rma.movies.ui.movielist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onMovieClick: (String) -> Unit,
    onFilterClick: (FilterParams) -> Unit,
    viewModel: MovieListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                        IconButton(onClick = { onFilterClick(state.activeFilters) }) {
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
                onSortChange = { viewModel.sendIntent(MovieListIntent.ChangeSort(it)) }
            )

            when {
                state.isLoading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.sendIntent(MovieListIntent.Retry) }
                )
                state.isEmpty -> EmptyContent()
                state.isSuccess -> MoviesContent(
                    movies = state.movies,
                    imageConfig = state.imageConfig,
                    onMovieClick = onMovieClick
                )
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
        "popularity"  to "Popularity"
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

@Composable
private fun MoviesContent(
    movies: List<Movie>,
    imageConfig: ImageConfig?,
    onMovieClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        movies.forEach { movie ->
            MovieListItem(
                movie = movie,
                imageConfig = imageConfig,
                onClick = { onMovieClick(movie.imdbId) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No movies found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}