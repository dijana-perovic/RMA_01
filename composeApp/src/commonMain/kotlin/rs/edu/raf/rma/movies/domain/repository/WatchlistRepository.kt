package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.domain.model.Movie

interface WatchlistRepository {
    fun observeWatchlist(): Flow<List<Movie>>
    fun observeIsInWatchlist(imdbId: String): Flow<Boolean>
    suspend fun syncWatchlist()
    suspend fun addToWatchlist(imdbId: String)
    suspend fun removeFromWatchlist(imdbId: String)
    suspend fun clearAll()
}