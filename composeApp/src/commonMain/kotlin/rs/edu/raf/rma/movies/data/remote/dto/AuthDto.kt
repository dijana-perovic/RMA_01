package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.SerialName
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
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class AuthUserDto(
    val id: Int,
    val username: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token") val token: String,
    @SerialName("expires_in") val expiresIn: Int? = null,
    val user: AuthUserDto,
)

fun AuthResponseDto.toAuthData() = AuthData(
    accessToken = token,
    username = user.username,
    fullName = user.fullName,
)