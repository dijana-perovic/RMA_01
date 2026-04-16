package rs.edu.raf.rma.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import rs.edu.raf.rma.movies.di.moviesNetworkModule
import rs.edu.raf.rma.movies.di.moviesRepositoryModule
import rs.edu.raf.rma.movies.di.moviesViewModelModule

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            moviesNetworkModule,
            moviesRepositoryModule,
            moviesViewModelModule,
        )
    }
}
