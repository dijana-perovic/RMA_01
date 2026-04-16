package rs.edu.raf.rma.movies.domain.repository

import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.model.MovieImage
import rs.edu.raf.rma.movies.domain.model.MovieVideo

interface MovieRepository {
    suspend fun getMovies(filters: FilterParams): List<Movie>
    suspend fun getMovieDetail(id: String): MovieDetail
    suspend fun getCast(id: String): List<CastMember>
    suspend fun getImages(id: String): List<MovieImage>
    suspend fun getVideos(id: String): List<MovieVideo>
}