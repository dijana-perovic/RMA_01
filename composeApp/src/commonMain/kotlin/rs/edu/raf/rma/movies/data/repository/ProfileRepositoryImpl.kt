package rs.edu.raf.rma.movies.data.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.local.dao.FavoriteDao
import rs.edu.raf.rma.movies.data.local.dao.WatchlistDao
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.data.remote.dto.AuthUserDto
import rs.edu.raf.rma.movies.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val movieApi: MovieApi,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
) : ProfileRepository {

    override suspend fun getMe(): AuthUserDto =
        movieApi.getMe()

    override fun observeFavoritesCount(): Flow<Int> =
        favoriteDao.observeCount()

    override fun observeWatchlistCount(): Flow<Int> =
        watchlistDao.observeCount()
}