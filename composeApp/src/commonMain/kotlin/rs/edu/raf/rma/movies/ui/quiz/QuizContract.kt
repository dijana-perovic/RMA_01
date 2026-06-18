package rs.edu.raf.rma.movies.ui.quiz

import rs.edu.raf.rma.movies.domain.model.QuizQuestion
import rs.edu.raf.rma.movies.domain.model.QuizResult

interface QuizContract {

    data class UiState(
        val questions: List<QuizQuestion> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedAnswer: String? = null,
        val correctAnswer: String? = null,
        val isAnswerRevealed: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
        val timeRemainingSeconds: Int = 60,
        val isLoading: Boolean = false,
        val isFinished: Boolean = false,
        val error: String? = null,
        val showAbandonDialog: Boolean = false,
    ) {
        val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentQuestionIndex)
        val totalQuestions: Int             get() = questions.size
        val progress: Float                 get() = if (totalQuestions == 0) 0f
        else currentQuestionIndex.toFloat() / totalQuestions
        val timeUsedSeconds: Int            get() = 60 - timeRemainingSeconds
        val isEmpty: Boolean                get() = !isLoading && error == null && questions.isEmpty()
    }

    sealed class UiEvent {
        object LoadQuiz                              : UiEvent()
        data class SelectAnswer(val answer: String)  : UiEvent()
        object NextQuestion                          : UiEvent()
        object ConfirmAbandon                        : UiEvent()
        object DismissAbandonDialog                  : UiEvent()
        object BackPressed                           : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToResult(val result: QuizResult)  : SideEffect()
        object NavigateBack                                  : SideEffect()
    }
}