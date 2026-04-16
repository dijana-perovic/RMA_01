package rs.edu.raf.rma.movies.data.repository

import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.data.remote.dto.toDomain
import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.model.MovieImage
import rs.edu.raf.rma.movies.domain.model.MovieVideo
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val api: MovieApi
) : MovieRepository {

    override suspend fun getMovies(filters: FilterParams): List<Movie> {
        return api.getMovies(
            sortBy    = filters.sortBy,
            query     = filters.query.ifBlank { null },
            genreId   = filters.genreId,
            minYear   = filters.yearFrom,
            maxYear   = filters.yearTo,
            minRating = filters.minRating
        ).items.map { it.toDomain() }
    }

    override suspend fun getMovieDetail(id: String): MovieDetail =
        api.getMovieDetail(id).toDomain()

    override suspend fun getCast(id: String): List<CastMember> =
        api.getCast(id).items.map { it.toDomain() }

    override suspend fun getImages(id: String): List<MovieImage> =
        api.getImages(id).backdrops.map { it.toDomain() }

    override suspend fun getVideos(id: String): List<MovieVideo> =
        api.getVideos(id).map { it.toDomain() }
}