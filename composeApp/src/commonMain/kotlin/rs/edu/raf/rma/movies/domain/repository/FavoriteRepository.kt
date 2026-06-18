package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.model.Movie

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<Movie>>
    fun observeIsFavorite(imdbId: String): Flow<Boolean>
    suspend fun syncFavorites()
    suspend fun addFavorite(imdbId: String)
    suspend fun removeFavorite(imdbId: String)
    suspend fun clearAll()
}