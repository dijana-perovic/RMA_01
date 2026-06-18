package rs.edu.raf.rma.movies.di

import org.koin.dsl.module
import rs.edu.raf.rma.movies.data.repository.ConfigRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.FavoriteRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.GenreRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.MovieRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.ProfileRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.QuizRepositoryImpl
import rs.edu.raf.rma.movies.data.repository.WatchlistRepositoryImpl
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository
import rs.edu.raf.rma.movies.domain.repository.FavoriteRepository
import rs.edu.raf.rma.movies.domain.repository.GenreRepository
import rs.edu.raf.rma.movies.domain.repository.MovieRepository
import rs.edu.raf.rma.movies.domain.repository.ProfileRepository
import rs.edu.raf.rma.movies.domain.repository.QuizRepository
import rs.edu.raf.rma.movies.domain.repository.WatchlistRepository

val moviesRepositoryModule = module {
    single<MovieRepository>    { MovieRepositoryImpl(get(), get()) }
    single<GenreRepository>    { GenreRepositoryImpl(get(), get()) }
    single<ConfigRepository>   { ConfigRepositoryImpl(get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get(), get()) }
    single<WatchlistRepository>{ WatchlistRepositoryImpl(get(), get()) }
    single<ProfileRepository>  { ProfileRepositoryImpl(get(), get(), get()) }
    single<QuizRepository>     { QuizRepositoryImpl(get(), get()) }
}