package rs.edu.raf.rma.movies.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.domain.model.FilterParams

@Composable
fun FilterScreen(
    viewModel: FilterViewModel = koinViewModel(),
    onBack: () -> Unit,
    onApply: (FilterParams) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setEvent(FilterContract.UiEvent.LoadGenresIfEmpty)
    }

    FilterScreen(
        state = state,
        onBack = onBack,
        onApply = onApply,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterScreen(
    state: FilterContract.UiState,
    onBack: () -> Unit,
    onApply: (FilterParams) -> Unit,
    eventPublisher: (FilterContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Movies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { eventPublisher(FilterContract.UiEvent.ClearAll) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                Button(
                    onClick = { onApply(state.toFilterParams()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { eventPublisher(FilterContract.UiEvent.UpdateSearch(it)) },
                label = { Text("Search by title") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Žanrovi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Genre", style = MaterialTheme.typography.titleSmall)
                when {
                    state.isLoadingGenres -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    state.isEmpty -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error?.message ?: "Could not load genres",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            eventPublisher(FilterContract.UiEvent.RetryGenres)
                        }) {
                            Text("Retry")
                        }
                    }
                    else -> FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedGenreIds.isEmpty(),
                            onClick = { eventPublisher(FilterContract.UiEvent.ClearAll) },
                            label = { Text("All") }
                        )
                        state.genres.forEach { genre ->
                            FilterChip(
                                selected = state.selectedGenreIds.contains(genre.id),
                                onClick = { eventPublisher(FilterContract.UiEvent.ToggleGenre(genre.id)) },
                                label = { Text(genre.name) }
                            )
                        }
                    }
                }
            }

            // Year range
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Year range",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.yearFrom?.toString() ?: "",
                        onValueChange = {
                            eventPublisher(FilterContract.UiEvent.SetYearFrom(it.toIntOrNull()))
                        },
                        label = { Text("From") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = state.yearTo?.toString() ?: "",
                        onValueChange = {
                            eventPublisher(FilterContract.UiEvent.SetYearTo(it.toIntOrNull()))
                        },
                        label = { Text("To") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Min rating
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Minimum rating",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = state.minRating?.let { rating ->
                            val rounded = kotlin.math.round(rating * 10) / 10.0
                            rounded.toString()
                        } ?: "Any",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = state.minRating ?: 0f,
                    onValueChange = {
                        eventPublisher(FilterContract.UiEvent.SetMinRating(if (it == 0f) null else it))
                    },
                    valueRange = 0f..10f,
                    steps = 19
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}