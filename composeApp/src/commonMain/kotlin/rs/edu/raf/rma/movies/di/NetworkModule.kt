package rs.edu.raf.rma.movies.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rs.edu.raf.rma.movies.data.remote.createMovieApi

val moviesNetworkModule = module {

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis  = 15_000
            }
        }
    }

    single {
        Ktorfit.Builder()
            .baseUrl("https://rma.finlab.rs/")
            .httpClient(get<HttpClient>())
            .build()
    }

    single { get<Ktorfit>().createMovieApi() }
}