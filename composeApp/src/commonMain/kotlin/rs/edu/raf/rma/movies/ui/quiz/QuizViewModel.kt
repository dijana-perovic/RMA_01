package rs.edu.raf.rma.movies.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.model.QuizResult
import rs.edu.raf.rma.movies.domain.repository.QuizRepository

class QuizViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: QuizContract.UiState.() -> QuizContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<QuizContract.UiEvent>()
    fun setEvent(event: QuizContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<QuizContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    private var timerJob: Job? = null

    init {
        observeEvents()
        setEvent(QuizContract.UiEvent.LoadQuiz)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is QuizContract.UiEvent.LoadQuiz          -> loadQuiz()
                    is QuizContract.UiEvent.SelectAnswer      -> selectAnswer(event.answer)
                    is QuizContract.UiEvent.NextQuestion      -> nextQuestion()
                    is QuizContract.UiEvent.BackPressed       ->
                        setState { copy(showAbandonDialog = true) }
                    is QuizContract.UiEvent.DismissAbandonDialog ->
                        setState { copy(showAbandonDialog = false) }
                    is QuizContract.UiEvent.ConfirmAbandon    -> {
                        timerJob?.cancel()
                        _sideEffects.emit(QuizContract.SideEffect.NavigateBack)
                    }
                }
            }
        }
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { quizRepository.generateQuestions() }
                .fold(
                    onSuccess = { questions ->
                        if (questions.isEmpty()) {
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Browse the catalog first to populate your quiz pool.",
                                )
                            }
                        } else {
                            setState {
                                copy(
                                    isLoading = false,
                                    questions = questions,
                                    currentQuestionIndex = 0,
                                    timeRemainingSeconds = 60,
                                )
                            }
                            startTimer()
                        }
                    },
                    onFailure = { e ->
                        setState { copy(isLoading = false, error = e.message) }
                    }
                )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && !_state.value.isFinished) {
                delay(1000L)
                setState { copy(timeRemainingSeconds = timeRemainingSeconds - 1) }
                if (_state.value.timeRemainingSeconds == 0) {
                    finishQuiz()
                }
            }
        }
    }

    private fun selectAnswer(answer: String) {
        if (_state.value.isAnswerRevealed) return
        val correct = _state.value.currentQuestion?.correctAnswer ?: return

        val isCorrect = answer == correct
        setState {
            copy(
                selectedAnswer   = answer,
                correctAnswer    = correct,
                isAnswerRevealed = true,
                correctCount     = if (isCorrect) correctCount + 1 else correctCount,
                incorrectCount   = if (!isCorrect) incorrectCount + 1 else incorrectCount,
            )
        }

        // Auto-napredovanje na sledeće pitanje nakon kratke pauze
        viewModelScope.launch {
            delay(1200L)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        val state = _state.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex >= state.totalQuestions) {
            finishQuiz()
            return
        }

        setState {
            copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer       = null,
                correctAnswer        = null,
                isAnswerRevealed     = false,
            )
        }
    }

    private fun finishQuiz() {
        if (_state.value.isFinished) return
        timerJob?.cancel()
        setState { copy(isFinished = true) }

        val state = _state.value
        val timeUsed = state.timeUsedSeconds
        val correct = state.correctCount
        val timeRemaining = state.timeRemainingSeconds.toDouble()
        val maxTime = 60.0

        // Formula: UBP = BTO * (9 + PVT / MVT), max 100
        val score = minOf(correct * (9.0 + timeRemaining / maxTime), 100.0)
        val roundedScore = kotlin.math.round(score * 100) / 100.0

        val result = QuizResult(
            score            = roundedScore,
            correctAnswers   = correct,
            incorrectAnswers = state.incorrectCount,
            timeUsedSeconds  = timeUsed,
        )

        viewModelScope.launch {
            runCatching {
                quizRepository.saveResult(result)
            }
            _sideEffects.emit(QuizContract.SideEffect.NavigateToResult(result))
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}