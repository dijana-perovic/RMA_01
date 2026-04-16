package rs.edu.raf.rma.movies.domain.repository

import rs.edu.raf.rma.movies.domain.model.Genre

interface GenreRepository {
    suspend fun getGenres(): List<Genre>
}