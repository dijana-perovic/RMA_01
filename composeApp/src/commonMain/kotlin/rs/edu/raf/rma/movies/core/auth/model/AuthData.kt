package rs.edu.raf.rma.movies.core.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val accessToken: String? = null,
    val username: String? = null,
    val fullName: String? = null,
) {
    companion object {
        fun empty() = AuthData(
            accessToken = null,
            username = null,
            fullName = null,
        )
    }
}
