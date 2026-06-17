package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.model.Genre

interface GenreRepository {
    fun observeGenres(): Flow<List<Genre>>
    suspend fun syncGenres()
}