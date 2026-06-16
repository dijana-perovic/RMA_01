package rs.edu.raf.rma.movies.core.auth

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import rs.edu.raf.rma.movies.core.auth.model.AuthData
import rs.edu.raf.rma.movies.core.auth.model.AuthState
import rs.edu.raf.rma.movies.core.auth.model.asAuthState

class AuthStore(
    private val persistence: DataStore<AuthData>,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val authState: StateFlow<AuthState> = persistence.data
        .map { it.asAuthState() }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first().asAuthState() },
        )

    suspend fun awaitInitialAuthState(): AuthState =
        persistence.data.map { it.asAuthState() }.first()

    suspend fun setAuthData(data: AuthData) =
        persistence.updateData { data }

    suspend fun clearAuthData() =
        persistence.updateData { AuthData.empty() }
}
