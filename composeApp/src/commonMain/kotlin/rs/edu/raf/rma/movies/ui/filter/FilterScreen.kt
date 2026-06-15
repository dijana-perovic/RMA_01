package rs.edu.raf.rma.movies.ui.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.domain.model.FilterParams

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterScreen(
    onBack: () -> Unit,
    onApply: (FilterParams) -> Unit,
    viewModel: FilterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                    onClick = { viewModel.sendIntent(FilterIntent.ClearAll) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                Button(
                    onClick = {
                        viewModel.sendIntent(FilterIntent.Apply)
                        onApply(state.toFilterParams())
                    },
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
                onValueChange = { viewModel.sendIntent(FilterIntent.UpdateSearch(it)) },
                label = { Text("Search by title") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Žanrovi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Genre",
                    style = MaterialTheme.typography.titleSmall
                )
                if (state.isLoadingGenres) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (state.genres.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Could not load genres",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            viewModel.sendIntent(FilterIntent.RetryGenres)
                        }) {
                            Text("Retry")
                        }
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedGenreIds.isEmpty(),
                            onClick = { viewModel.sendIntent(FilterIntent.ClearAll) },
                            label = { Text("All") }
                        )
                        state.genres.forEach { genre ->
                            FilterChip(
                                selected = state.selectedGenreIds.contains(genre.id),
                                onClick = {
                                    viewModel.sendIntent(FilterIntent.ToggleGenre(genre.id))
                                },
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
                            viewModel.sendIntent(FilterIntent.SetYearFrom(it.toIntOrNull()))
                        },
                        label = { Text("From") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = state.yearTo?.toString() ?: "",
                        onValueChange = {
                            viewModel.sendIntent(FilterIntent.SetYearTo(it.toIntOrNull()))
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
                        viewModel.sendIntent(
                            FilterIntent.SetMinRating(if (it == 0f) null else it)
                        )
                    },
                    valueRange = 0f..10f,
                    steps = 19
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}