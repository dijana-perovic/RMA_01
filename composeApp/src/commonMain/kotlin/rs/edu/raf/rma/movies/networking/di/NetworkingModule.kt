package rs.edu.raf.rma.movies.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.core.auth.model.AuthState
import rs.edu.raf.rma.movies.data.remote.createAuthApi
import rs.edu.raf.rma.movies.data.remote.createMovieApi

private val networkingJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

val networkingModule = module {

    // Unauthenticated HttpClient — za login i register
    single<HttpClient>(Qualifiers.Unauthenticated) {
        createHttpClient()
    }

    // Authenticated HttpClient — za sve ostale zahteve, dodaje Bearer token
    single<HttpClient>(Qualifiers.Authenticated) {
        val authStoreLazy: Lazy<AuthStore> = inject()
        createHttpClient {
            installAuthPlugin(authStoreLazy)
        }
    }

    // Unauthenticated Ktorfit — samo za AuthApi
    single<Ktorfit>(Qualifiers.Unauthenticated) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Unauthenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
    }

    // Authenticated Ktorfit — za MovieApi i sve autentikovane endpointe
    single<Ktorfit>(Qualifiers.Authenticated) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
    }

    single { get<Ktorfit>(Qualifiers.Unauthenticated).createAuthApi() }
    single { get<Ktorfit>(Qualifiers.Authenticated).createMovieApi() }
}

private fun createHttpClient(
    block: HttpClientConfig<*>.() -> Unit = {},
): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(json = networkingJson)
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.v(message = message, tag = "HttpClient")
                }
            }
            level = LogLevel.ALL
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    throw ResponseException(
                        response = response,
                        cachedResponseText = "HTTP ${response.status}",
                    )
                }
            }
        }
        block.invoke(this)
    }
}

private fun HttpClientConfig<*>.installAuthPlugin(
    authStoreLazy: Lazy<AuthStore>,
) = install(createClientPlugin("AuthPlugin") {

    // Dodaj Bearer token na svaki request
    on(SetupRequest) { request ->
        when (val state = authStoreLazy.value.authState.value) {
            is AuthState.Authenticated ->
                request.headers.append(
                    HttpHeaders.Authorization,
                    "Bearer ${state.data.accessToken}",
                )
            AuthState.Unauthenticated -> Unit
        }
    }

    // Intercept 401 — briši token, AuthStore emituje Unauthenticated, NavGraph reaguje
    on(Send) { request ->
        val call = proceed(request)
        if (call.response.status == HttpStatusCode.Unauthorized) {
            runBlocking { authStoreLazy.value.clearAuthData() }
        }
        call
    }
})
