package rs.edu.raf.rma.movies.ui.auth

interface AuthContract {

    data class UiState(
        val loginUsername: String = "",
        val loginPassword: String = "",
        val registerFullName: String = "",
        val registerUsername: String = "",
        val registerPassword: String = "",
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    ) {
        val loginFormValid: Boolean
            get() = loginUsername.length >= 3
                    && loginUsername.all { it.isLetterOrDigit() || it == '_' }
                    && loginPassword.length >= 8

        val registerFormValid: Boolean
            get() = registerFullName.isNotBlank()
                    && registerUsername.length >= 3
                    && registerUsername.all { it.isLetterOrDigit() || it == '_' }
                    && registerPassword.length >= 8
    }

    sealed class UiEvent {
        data class UpdateLoginUsername(val value: String)    : UiEvent()
        data class UpdateLoginPassword(val value: String)    : UiEvent()
        data class UpdateRegisterFullName(val value: String) : UiEvent()
        data class UpdateRegisterUsername(val value: String) : UiEvent()
        data class UpdateRegisterPassword(val value: String) : UiEvent()
        data object SubmitLogin                              : UiEvent()
        data object SubmitRegister                           : UiEvent()
        data object ClearError                               : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToHome : SideEffect()
    }
}