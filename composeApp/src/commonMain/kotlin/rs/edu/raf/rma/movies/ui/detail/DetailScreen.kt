package rs.edu.raf.rma.movies.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.util.ImageUrlBuilder
import rs.edu.raf.rma.movies.util.formatVotes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is DetailContract.SideEffect.OpenUrl ->
                    uriHandler.openUri(effect.url)
                is DetailContract.SideEffect.ShowMessage -> {
                    // TODO: Snackbar
                }
            }
        }
    }

    DetailScreen(
        state = state,
        onBack = onBack,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DetailScreen(
    state: DetailContract.UiState,
    onBack: () -> Unit,
    eventPublisher: (DetailContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.movie?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // Watchlist toggle
                    IconButton(
                        onClick = { eventPublisher(DetailContract.UiEvent.ToggleWatchlist) },
                        enabled = !state.isTogglingWatchlist,
                    ) {
                        Icon(
                            imageVector = if (state.isInWatchlist) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = if (state.isInWatchlist) "Remove from watchlist"
                            else "Add to watchlist",
                            tint = if (state.isInWatchlist) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    // Favorite toggle
                    IconButton(
                        onClick = { eventPublisher(DetailContract.UiEvent.ToggleFavorite) },
                        enabled = !state.isTogglingFavorite,
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = if (state.isFavorite) "Remove from favorites"
                            else "Add to favorites",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.movie == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null && state.movie == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { eventPublisher(DetailContract.UiEvent.Retry) }) {
                        Text("Retry")
                    }
                }
            }

            state.isSuccess -> DetailContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onPlayTrailer = { eventPublisher(DetailContract.UiEvent.PlayTrailer) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    state: DetailContract.UiState,
    modifier: Modifier = Modifier,
    onPlayTrailer: () -> Unit,
) {
    val movie = state.movie ?: return
    val config = state.imageConfig

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp)
        ) {
            AsyncImage(
                model = config?.let { ImageUrlBuilder.backdrop(it, movie.backdropPath) },
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (state.trailerKey != null) {
                FilledIconButton(
                    onClick = onPlayTrailer,
                    modifier = Modifier.align(Alignment.Center).size(56.dp),
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play trailer",
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = listOfNotNull(
                    movie.year?.toString(),
                    movie.runtime?.let { "$it min" },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = movie.imdbRating?.let {
                            "${kotlin.math.round(it * 10) / 10.0} IMDb"
                        } ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                movie.tmdbRating?.let { rating ->
                    Text(
                        text = "${kotlin.math.round(rating * 10) / 10.0} TMDB",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                movie.genres.forEach { genre ->
                    SuggestionChip(onClick = {}, label = { Text(genre.name) })
                }
            }

            movie.overview?.let { overview ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = overview, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    movie.budget?.takeIf { it > 0 }?.let {
                        InfoBadge("Budget", "$${it / 1_000_000}M")
                    }
                    movie.revenue?.takeIf { it > 0 }?.let {
                        InfoBadge("Revenue", "$${it / 1_000_000}M")
                    }
                    movie.languageCode?.let { InfoBadge("Language", it.uppercase()) }
                    movie.popularity?.let {
                        InfoBadge("Popularity", "${kotlin.math.round(it * 10) / 10.0}")
                    }
                    movie.imdbVotes?.let { InfoBadge("Votes", it.formatVotes()) }
                }
            }

            if (state.images.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.images.take(3).forEach { image ->
                            AsyncImage(
                                model = config?.let {
                                    ImageUrlBuilder.backdrop(it, image.filePath)
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .width(240.dp)
                                    .height(135.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }

            if (state.cast.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    state.cast.take(10).forEach { member ->
                        CastItem(member = member, config = config)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun CastItem(member: CastMember, config: ImageConfig?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        AsyncImage(
            model = config?.let { ImageUrlBuilder.profile(it, member.profilePath) },
            contentDescription = member.name,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )
        Column {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            member.department?.let { dept ->
                Text(
                    text = dept,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}