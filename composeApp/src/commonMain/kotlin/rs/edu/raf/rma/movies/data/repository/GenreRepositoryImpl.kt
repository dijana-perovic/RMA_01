package rs.edu.raf.rma.movies.data.repository

import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.data.remote.dto.toDomain
import rs.edu.raf.rma.movies.domain.model.Genre
import rs.edu.raf.rma.movies.domain.repository.GenreRepository

class GenreRepositoryImpl(
    private val api: MovieApi
) : GenreRepository {

    override suspend fun getGenres(): List<Genre> =
        api.getGenres().map { it.toDomain() }
}