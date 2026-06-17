package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.model.MovieDetail

interface MovieRepository {
    fun observeMovies(filters: FilterParams): Flow<List<Movie>>
    fun observeMovieDetail(id: String): Flow<MovieDetail?>
    suspend fun syncMovies(filters: FilterParams)
    suspend fun syncMovieDetail(id: String)
}