package rs.edu.raf.rma.movies.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movies.domain.model.QuizQuestion
import rs.edu.raf.rma.movies.domain.model.QuizResult

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = koinViewModel(),
    onNavigateToResult: (QuizResult) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is QuizContract.SideEffect.NavigateToResult ->
                    onNavigateToResult(effect.result)
                is QuizContract.SideEffect.NavigateBack ->
                    onNavigateBack()
            }
        }
    }

    QuizScreen(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun QuizScreen(
    state: QuizContract.UiState,
    eventPublisher: (QuizContract.UiEvent) -> Unit,
) {
    if (state.showAbandonDialog) {
        AbandonDialog(
            onConfirm = { eventPublisher(QuizContract.UiEvent.ConfirmAbandon) },
            onDismiss = { eventPublisher(QuizContract.UiEvent.DismissAbandonDialog) },
        )
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                state.currentQuestion != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        QuizHeader(
                            currentIndex    = state.currentQuestionIndex,
                            totalQuestions  = state.totalQuestions,
                            timeRemaining   = state.timeRemainingSeconds,
                            progress        = state.progress,
                        )

                        AnimatedContent(
                            targetState = state.currentQuestionIndex,
                            transitionSpec = {
                                slideInHorizontally(
                                    animationSpec = tween(300),
                                    initialOffsetX = { it },
                                ) togetherWith slideOutHorizontally(
                                    animationSpec = tween(300),
                                    targetOffsetX = { -it },
                                )
                            },
                            label = "question_transition",
                        ) { _ ->
                            when (val question = state.currentQuestion) {
                                is QuizQuestion.GuessTheMovie ->
                                    GuessTheMovieContent(
                                        question         = question,
                                        selectedAnswer   = state.selectedAnswer,
                                        isAnswerRevealed = state.isAnswerRevealed,
                                        onAnswerSelected = {
                                            eventPublisher(QuizContract.UiEvent.SelectAnswer(it))
                                        },
                                    )
                                is QuizQuestion.GuessTheYear ->
                                    GuessTheYearContent(
                                        question         = question,
                                        selectedAnswer   = state.selectedAnswer,
                                        isAnswerRevealed = state.isAnswerRevealed,
                                        onAnswerSelected = {
                                            eventPublisher(QuizContract.UiEvent.SelectAnswer(it))
                                        },
                                    )
                                is QuizQuestion.GuessTheActor ->
                                    GuessTheActorContent(
                                        question         = question,
                                        selectedAnswer   = state.selectedAnswer,
                                        isAnswerRevealed = state.isAnswerRevealed,
                                        onAnswerSelected = {
                                            eventPublisher(QuizContract.UiEvent.SelectAnswer(it))
                                        },
                                    )
                                null -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizHeader(
    currentIndex: Int,
    totalQuestions: Int,
    timeRemaining: Int,
    progress: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Question ${currentIndex + 1} / $totalQuestions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Timer
            val timerColor = when {
                timeRemaining <= 10 -> MaterialTheme.colorScheme.error
                timeRemaining <= 20 -> MaterialTheme.colorScheme.tertiary
                else                -> MaterialTheme.colorScheme.primary
            }
            Text(
                text = "⏱ ${timeRemaining}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = timerColor,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GuessTheMovieContent(
    question: QuizQuestion.GuessTheMovie,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Which movie is this?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        AsyncImage(
            model = "https://image.tmdb.org/t/p/w780${question.imageUrl}",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )

        AnswerOptions(
            options          = question.options,
            correctAnswer    = question.correctAnswer,
            selectedAnswer   = selectedAnswer,
            isAnswerRevealed = isAnswerRevealed,
            onAnswerSelected = onAnswerSelected,
        )
    }
}

@Composable
private fun GuessTheYearContent(
    question: QuizQuestion.GuessTheYear,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "What year was this movie released?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${question.posterUrl}",
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = question.movieTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        AnswerOptions(
            options          = question.options,
            correctAnswer    = question.correctAnswer,
            selectedAnswer   = selectedAnswer,
            isAnswerRevealed = isAnswerRevealed,
            onAnswerSelected = onAnswerSelected,
        )
    }
}

@Composable
private fun GuessTheActorContent(
    question: QuizQuestion.GuessTheActor,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Who is the lead actor in this movie?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${question.posterUrl}",
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = question.movieTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        AnswerOptions(
            options          = question.options,
            correctAnswer    = question.correctAnswer,
            selectedAnswer   = selectedAnswer,
            isAnswerRevealed = isAnswerRevealed,
            onAnswerSelected = onAnswerSelected,
        )
    }
}

@Composable
private fun AnswerOptions(
    options: List<String>,
    correctAnswer: String,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val containerColor = when {
                !isAnswerRevealed -> MaterialTheme.colorScheme.surfaceVariant
                option == correctAnswer -> Color(0xFF4CAF50)
                option == selectedAnswer -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = when {
                !isAnswerRevealed -> MaterialTheme.colorScheme.onSurfaceVariant
                option == correctAnswer || option == selectedAnswer -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Button(
                onClick = { if (!isAnswerRevealed) onAnswerSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor   = contentColor,
                ),
                enabled = !isAnswerRevealed || option == correctAnswer || option == selectedAnswer,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun AbandonDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abandon quiz?") },
        text = { Text("Your progress will be lost.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Abandon", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        },
    )
}