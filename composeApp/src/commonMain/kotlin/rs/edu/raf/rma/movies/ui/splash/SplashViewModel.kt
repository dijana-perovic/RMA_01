package rs.edu.raf.rma.movies.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.core.auth.model.AuthState

class SplashViewModel(
    private val authStore: AuthStore,
) : ViewModel() {

    private val _bootState = MutableStateFlow<BootState>(BootState.Loading)
    val bootState: StateFlow<BootState> = _bootState.asStateFlow()

    // isLoggedIn prati AuthStore flow u realnom vremenu -
    // reaguje i na uspešan login i na forced logout (401)
    val isLoggedIn: StateFlow<Boolean> = authStore.authState
        .map { it is AuthState.Authenticated }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    init {
        boot()
    }

    private fun boot() = viewModelScope.launch {
        try {
            authStore.awaitInitialAuthState()
            _bootState.value = BootState.Success
        } catch (e: Exception) {
            _bootState.value = BootState.Failed(error = e)
        }
    }

    fun retry() {
        _bootState.value = BootState.Loading
        boot()
    }
}