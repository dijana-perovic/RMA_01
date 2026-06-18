package rs.edu.raf.rma.movies.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.data.remote.AuthApi
import rs.edu.raf.rma.movies.data.remote.dto.LoginRequestDto
import rs.edu.raf.rma.movies.data.remote.dto.RegisterRequestDto
import rs.edu.raf.rma.movies.data.remote.dto.toAuthData
import rs.edu.raf.rma.movies.domain.repository.QuizRepository

class AuthViewModel(
    private val authApi: AuthApi,
    private val authStore: AuthStore,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: AuthContract.UiState.() -> AuthContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<AuthContract.UiEvent>()
    fun setEvent(event: AuthContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = MutableSharedFlow<AuthContract.SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is AuthContract.UiEvent.UpdateLoginUsername ->
                        setState { copy(loginUsername = event.value, error = null) }

                    is AuthContract.UiEvent.UpdateLoginPassword ->
                        setState { copy(loginPassword = event.value, error = null) }

                    is AuthContract.UiEvent.UpdateRegisterFullName ->
                        setState { copy(registerFullName = event.value, error = null) }

                    is AuthContract.UiEvent.UpdateRegisterUsername ->
                        setState { copy(registerUsername = event.value, error = null) }

                    is AuthContract.UiEvent.UpdateRegisterPassword ->
                        setState { copy(registerPassword = event.value, error = null) }

                    is AuthContract.UiEvent.ClearError ->
                        setState { copy(error = null) }

                    is AuthContract.UiEvent.SubmitLogin -> login()

                    is AuthContract.UiEvent.SubmitRegister -> register()
                }
            }
        }
    }

    private fun login() {
        if (!_state.value.loginFormValid) return
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching {
                authApi.login(
                    LoginRequestDto(
                        username = _state.value.loginUsername,
                        password = _state.value.loginPassword,
                    )
                )
            }.fold(
                onSuccess = { response ->
                    authStore.setAuthData(response.toAuthData())
                    quizRepository.syncQuizResults()
                    setState { copy(isLoading = false) }
                    _sideEffects.emit(AuthContract.SideEffect.NavigateToHome)
                },
                onFailure = { error ->
                    setState { copy(isLoading = false, error = error.toFriendlyError()) }
                },
            )
        }
    }

    private fun register() {
        if (!_state.value.registerFormValid) return
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching {
                authApi.register(
                    RegisterRequestDto(
                        username = _state.value.registerUsername,
                        password = _state.value.registerPassword,
                        fullName = _state.value.registerFullName,
                    )
                )
            }.fold(
                onSuccess = { response ->
                    authStore.setAuthData(response.toAuthData())
                    setState { copy(isLoading = false) }
                    _sideEffects.emit(AuthContract.SideEffect.NavigateToHome)
                },
                onFailure = { error ->
                    setState { copy(isLoading = false, error = error.toFriendlyError()) }
                },
            )
        }
    }
}

private fun Throwable.toFriendlyError(): Throwable = when {
    this is ResponseException && response.status.value == 409 ->
        Exception("Username is already taken")
    this is ResponseException && response.status.value in 400..401 ->
        Exception("Invalid username or password")
    message?.contains("Unable to resolve host") == true ->
        Exception("No internet connection")
    else -> Exception(message ?: "Something went wrong")
}