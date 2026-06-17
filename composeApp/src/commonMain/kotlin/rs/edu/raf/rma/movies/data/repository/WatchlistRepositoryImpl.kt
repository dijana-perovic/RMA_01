package rs.edu.raf.rma.movies.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.movies.data.local.dao.WatchlistDao
import rs.edu.raf.rma.movies.data.local.entity.WatchlistEntity
import rs.edu.raf.rma.movies.data.local.mapper.toDomain
import rs.edu.raf.rma.movies.data.local.mapper.toWatchlistEntity
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.repository.WatchlistRepository

class WatchlistRepositoryImpl(
    private val api: MovieApi,
    private val watchlistDao: WatchlistDao,
) : WatchlistRepository {

    override fun observeWatchlist(): Flow<List<Movie>> =
        watchlistDao.observeWatchlist().map { it.map { e -> e.toDomain() } }

    override fun observeIsInWatchlist(imdbId: String): Flow<Boolean> =
        watchlistDao.observeIsInWatchlist(imdbId)

    override suspend fun syncWatchlist() {
        runCatching {
            val watchlist = api.getWatchlist()
            watchlistDao.replaceAll(watchlist.map { it.toWatchlistEntity() })
        }.onFailure { Napier.e("syncWatchlist failed", it) }
    }

    override suspend fun addToWatchlist(imdbId: String) {
        val detail = runCatching { api.getMovieDetail(imdbId) }.getOrNull()
        val entity = WatchlistEntity(
            imdbId     = imdbId,
            title      = detail?.title ?: "",
            year       = detail?.year,
            imdbRating = detail?.imdbRating?.toDouble(),
            posterPath = detail?.posterPath,
            genresJson = "[]",
        )

        watchlistDao.upsert(entity)

        runCatching {
            api.addToWatchlist(imdbId)
        }.onFailure {
            Napier.e("addToWatchlist API failed, rolling back", it)
            watchlistDao.delete(imdbId)
            throw it
        }
    }

    override suspend fun removeFromWatchlist(imdbId: String) {
        watchlistDao.delete(imdbId)

        runCatching {
            api.removeFromWatchlist(imdbId)
        }.onFailure {
            Napier.e("removeFromWatchlist API failed, rolling back", it)
            syncWatchlist()
            throw it
        }
    }
}