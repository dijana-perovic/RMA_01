package rs.edu.raf.rma.movies.di

import org.koin.dsl.module
import rs.edu.raf.rma.movies.data.repository.ConfigRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.GenreRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.MovieRepositoryImpl
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.GenreRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

val moviesRepositoryModule = module {
    single<MovieRepository> { MovieRepositoryImpl(get()) }
    single<GenreRepository> { GenreRepositoryImpl(get()) }
    single<ConfigRepository> { ConfigRepositoryImpl(get()) }
}