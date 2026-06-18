package rs.edu.raf.rma.movies.ui.quiz

import rs.edu.raf.rma.movies.domain.model.QuizResult

interface QuizResultContract {

    data class UiState(
        val result: QuizResult? = null,
    )

    sealed class UiEvent {
        object PlayAgain : UiEvent()
        object GoHome    : UiEvent()
    }

    sealed class SideEffect {
        object NavigateToQuiz : SideEffect()
        object NavigateToHome : SideEffect()
    }
}