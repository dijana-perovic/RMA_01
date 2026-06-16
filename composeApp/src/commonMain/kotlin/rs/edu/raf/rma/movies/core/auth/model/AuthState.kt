package rs.edu.raf.rma.movies.core.auth.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val data: AuthData) : AuthState()
}

fun AuthData.asAuthState(): AuthState = when {
    accessToken.isNullOrBlank() -> AuthState.Unauthenticated
    else -> AuthState.Authenticated(data = this)
}
