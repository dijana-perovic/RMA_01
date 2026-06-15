package rs.edu.raf.rma.movies.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    movieId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    // učitaj detalje kada se otvori ekran
    LaunchedEffect(movieId) {
        viewModel.sendIntent(DetailIntent.LoadDetail(movieId))
    }

    // prati trailerUrl — kada ViewModel postavi URL, otvori ga
    LaunchedEffect(state.trailerUrl) {
        state.trailerUrl?.let { url ->
            uriHandler.openUri(url)
            viewModel.clearTrailerUrl()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.movie?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.sendIntent(DetailIntent.Retry)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.isSuccess -> {
                DetailContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onPlayTrailer = {
                        viewModel.sendIntent(DetailIntent.PlayTrailer)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    state: DetailState,
    modifier: Modifier = Modifier,
    onPlayTrailer: () -> Unit
) {
    val movie = state.movie ?: return
    val config = state.imageConfig

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Backdrop sa play dugmetom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            AsyncImage(
                model = config?.let {
                    ImageUrlBuilder.backdrop(it, movie.backdropPath)
                },
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play dugme
            if (state.trailerKey != null) {
                FilledIconButton(
                    onClick = { onPlayTrailer() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play trailer",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Naslov
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Godina i trajanje
            Text(
                text = listOfNotNull(
                    movie.year?.toString(),
                    movie.runtime?.let { "$it min" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rejtinzi
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = movie.imdbRating?.let {
                            "${kotlin.math.round(it * 10) / 10.0} IMDb"
                        } ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                movie.tmdbRating?.let { rating ->
                    Text(
                        text = "${kotlin.math.round(rating * 10) / 10.0} TMDB",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Žanrovi
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(genre.name) }
                    )
                }
            }

            // Overview
            movie.overview?.let { overview ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = overview,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Info bedževi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    movie.budget?.let { budget ->
                        if (budget > 0) InfoBadge(
                            label = "Budget",
                            value = "$${budget / 1_000_000}M"
                        )
                    }
                    movie.revenue?.let { revenue ->
                        if (revenue > 0) InfoBadge(
                            label = "Revenue",
                            value = "$${revenue / 1_000_000}M"
                        )
                    }
                    movie.languageCode?.let { lang ->
                        InfoBadge(label = "Language", value = lang.uppercase())
                    }
                    movie.popularity?.let { pop ->
                        InfoBadge(
                            label = "Popularity",
                            value = "${kotlin.math.round(pop * 10) / 10.0}"
                        )
                    }
                    movie.imdbVotes?.let { votes ->
                        InfoBadge(
                            label = "Votes",
                            value = votes.formatVotes()
                        )
                    }
                }
            }

            // Slike
            if (state.images.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // Glumci
            if (state.cast.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    state.cast.take(10).forEach { member ->
                        CastItem(member = member, config = config)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CastItem(
    member: CastMember,
    config: ImageConfig?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        AsyncImage(
            model = config?.let {
                ImageUrlBuilder.profile(it, member.profilePath)
            },
            contentDescription = member.name,
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Column {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            member.department?.let { dept ->
                Text(
                    text = dept,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}