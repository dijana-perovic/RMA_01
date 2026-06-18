package rs.edu.raf.rma.movies.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuizResultScreen(
    viewModel: QuizResultViewModel = koinViewModel(),
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is QuizResultContract.SideEffect.NavigateToQuiz -> onPlayAgain()
                is QuizResultContract.SideEffect.NavigateToHome -> onGoHome()
            }
        }
    }

    QuizResultScreen(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizResultScreen(
    state: QuizResultContract.UiState,
    eventPublisher: (QuizResultContract.UiEvent) -> Unit,
) {
    val result = state.result ?: return

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quiz Result") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Skor
            Text(
                text = "Your Score",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${kotlin.math.round(result.score * 100) / 100.0}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp,
                color = scoreColor(result.score),
            )
            Text(
                text = "out of 100",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            // Statistike
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ResultStatItem(
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    label = "Correct",
                    value = result.correctAnswers.toString(),
                )
                ResultStatItem(
                    icon = {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    label = "Incorrect",
                    value = result.incorrectAnswers.toString(),
                )
                ResultStatItem(
                    icon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    label = "Time used",
                    value = "${result.timeUsedSeconds}s",
                )
            }

            Spacer(Modifier.weight(1f))

            // Dugmad
            Button(
                onClick = { eventPublisher(QuizResultContract.UiEvent.PlayAgain) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Play Again")
            }

            OutlinedButton(
                onClick = { eventPublisher(QuizResultContract.UiEvent.GoHome) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back to Movies")
            }
        }
    }
}

@Composable
private fun ResultStatItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun scoreColor(score: Double): Color = when {
    score >= 80 -> Color(0xFF4CAF50)
    score >= 50 -> Color(0xFFFF9800)
    else        -> Color(0xFFF44336)
}