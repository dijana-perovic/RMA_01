package rs.edu.raf.rma.movies.data.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import rs.edu.raf.rma.movies.data.remote.dto.AuthResponseDto
import rs.edu.raf.rma.movies.data.remote.dto.LoginRequestDto
import rs.edu.raf.rma.movies.data.remote.dto.RegisterRequestDto

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @GET("me")
    suspend fun getMe(): AuthResponseDto
}