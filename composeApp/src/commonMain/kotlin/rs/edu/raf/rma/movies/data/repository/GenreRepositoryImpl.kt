package rs.edu.raf.rma.movies.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.movies.data.local.dao.GenreDao
import rs.edu.raf.rma.movies.data.local.mapper.toDomain
import rs.edu.raf.rma.movies.data.local.mapper.toEntity
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.domain.model.Genre
import rs.edu.raf.rma.movies.domain.repository.GenreRepository

class GenreRepositoryImpl(
    private val api: MovieApi,
    private val genreDao: GenreDao,
) : GenreRepository {

    override fun observeGenres(): Flow<List<Genre>> =
        genreDao.observeGenres().map { it.map { e -> e.toDomain() } }

    override suspend fun syncGenres() {
        runCatching {
            val genres = api.getGenres()
            genreDao.upsertGenres(genres.map { it.toEntity() })
        }.onFailure { Napier.e("syncGenres failed", it) }
    }
}