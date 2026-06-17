package rs.edu.raf.rma.movies.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.movies.data.local.dao.MovieDao
import rs.edu.raf.rma.movies.data.local.mapper.toDomain
import rs.edu.raf.rma.movies.data.local.mapper.toEntity
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val api: MovieApi,
    private val movieDao: MovieDao
) : MovieRepository {

    override fun observeMovies(filters: FilterParams): Flow<List<Movie>> =
        movieDao.observeFilteredMovies(
            query     = filters.query,
            minRating = filters.minRating,
            yearFrom  = filters.yearFrom,
            yearTo    = filters.yearTo,
            sortBy    = filters.sortBy,
        ).map { entities -> entities.map { it.toDomain() } }

    override fun observeMovieDetail(id: String): Flow<MovieDetail?> =
        movieDao.observeMovieDetail(id).map { it?.toDomain() }

    override suspend fun syncMovies(filters: FilterParams) {
        runCatching {
            val response = api.getMovies(
                sortBy    = filters.sortBy,
                query     = filters.query.ifBlank { null },
                genreId   = filters.genreId,
                minYear   = filters.yearFrom,
                maxYear   = filters.yearTo,
                minRating = filters.minRating
            )
            movieDao.upsertMovies(response.items.map { it.toEntity() })
        }.onFailure { Napier.e("syncMovies failed", it) }
    }

    override suspend fun syncMovieDetail(id: String) {
        runCatching {
            val detail = api.getMovieDetail(id)
            val cast   = api.getCast(id).items
            val images = api.getImages(id).backdrops
            val videos = api.getVideos(id)
            movieDao.upsertMovieDetail(detail.toEntity(cast, images, videos))
        }.onFailure { Napier.e("syncMovieDetail($id) failed", it) }
    }
}