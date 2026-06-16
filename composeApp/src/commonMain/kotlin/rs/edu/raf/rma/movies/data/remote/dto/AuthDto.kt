package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.core.auth.model.AuthData

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val username: String,
    val password: String,
    val fullName: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val username: String,
    val fullName: String,
)

fun AuthResponseDto.toAuthData() = AuthData(
    accessToken = token,
    username = username,
    fullName = fullName,
)
