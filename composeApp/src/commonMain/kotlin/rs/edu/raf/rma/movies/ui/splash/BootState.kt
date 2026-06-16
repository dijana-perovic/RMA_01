package rs.edu.raf.rma.movies.ui.splash

sealed interface BootState {
    data object Loading : BootState
    data object Success : BootState
    data class Failed(val error: Throwable) : BootState
}