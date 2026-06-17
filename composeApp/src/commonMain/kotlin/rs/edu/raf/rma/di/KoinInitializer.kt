package rs.edu.raf.rma.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import rs.edu.raf.rma.movies.core.auth.di.authModule
import rs.edu.raf.rma.movies.core.db.di.databaseModule
import rs.edu.raf.rma.movies.networking.di.networkingModule
import rs.edu.raf.rma.movies.di.moviesRepositoryModule
import rs.edu.raf.rma.movies.di.moviesViewModelModule

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            authModule,
            databaseModule(),
            networkingModule,
            moviesRepositoryModule,
            moviesViewModelModule,
        )
    }
}