package rs.edu.raf.rma.movies.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.movies.ui.auth.AuthViewModel
import rs.edu.raf.rma.movies.ui.detail.DetailViewModel
import rs.edu.raf.rma.movies.ui.filter.FilterViewModel
import rs.edu.raf.rma.movies.ui.movielist.MovieListViewModel
import rs.edu.raf.rma.movies.ui.splash.SplashViewModel

val moviesViewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MovieListViewModel)
    viewModelOf(::FilterViewModel)
    viewModelOf(::DetailViewModel)
}