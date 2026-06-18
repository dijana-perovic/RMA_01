package rs.edu.raf.rma.movies.ui.profile

interface ProfileContract {

    data class UiState(
        val username: String = "",
        val fullName: String = "",
        val favoritesCount: Int = 0,
        val watchlistCount: Int = 0,
        val bestScore: Double? = null,      // placeholder za kviz
        val quizPlayed: Int = 0,            // placeholder za kviz
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    ) {
        val isOffline: Boolean get() = error?.message?.contains("Unable to resolve host") == true
    }

    sealed class UiEvent {
        object LoadProfile : UiEvent()
        object Logout : UiEvent()
    }

    sealed class SideEffect {
        object NavigateToAuth : SideEffect()
        data class ShowMessage(val message: String) : SideEffect()
    }
}