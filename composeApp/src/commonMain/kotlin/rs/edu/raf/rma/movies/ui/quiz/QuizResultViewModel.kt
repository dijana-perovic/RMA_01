package rs.edu.raf.rma.movies.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.model.QuizResult

class QuizResultViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizResultContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: QuizResultContract.UiState.() -> QuizResultContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<QuizResultContract.UiEvent>()
    fun setEvent(event: QuizResultContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<QuizResultContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        // Dohvati result iz SavedStateHandle
        val score            = savedStateHandle.get<Float>("score")?.toDouble() ?: 0.0
        val correctAnswers   = savedStateHandle.get<Int>("correctAnswers") ?: 0
        val incorrectAnswers = savedStateHandle.get<Int>("incorrectAnswers") ?: 0
        val timeUsed         = savedStateHandle.get<Int>("timeUsedSeconds") ?: 0

        setState {
            copy(
                result = QuizResult(
                    score            = score,
                    correctAnswers   = correctAnswers,
                    incorrectAnswers = incorrectAnswers,
                    timeUsedSeconds  = timeUsed,
                )
            )
        }

        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is QuizResultContract.UiEvent.PlayAgain ->
                        _sideEffects.emit(QuizResultContract.SideEffect.NavigateToQuiz)
                    is QuizResultContract.UiEvent.GoHome ->
                        _sideEffects.emit(QuizResultContract.SideEffect.NavigateToHome)
                }
            }
        }
    }
}