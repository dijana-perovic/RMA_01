package rs.edu.raf.rma.movies.domain.repository

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.movies.data.remote.dto.AuthUserDto

interface ProfileRepository {
    suspend fun getMe(): AuthUserDto
    fun observeFavoritesCount(): Flow<Int>
    fun observeWatchlistCount(): Flow<Int>
}