package rs.edu.raf.rma.movies.data.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.movies.data.local.dao.FavoriteDao
import rs.edu.raf.rma.movies.data.local.mapper.toDomain
import rs.edu.raf.rma.movies.data.local.mapper.toFavoriteEntity
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.repository.FavoriteRepository

class FavoriteRepositoryImpl(
    private val api: MovieApi,
    private val favoriteDao: FavoriteDao,
) : FavoriteRepository {

    // UI posmatra Room — SSOT
    override fun observeFavorites(): Flow<List<Movie>> =
        favoriteDao.observeFavorites().map { it.map { e -> e.toDomain() } }

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(imdbId)

    // Sinhronizacija: GET /me/favorites → Room
    override suspend fun syncFavorites() {
        runCatching {
            val favorites = api.getFavorites()
            favoriteDao.replaceAll(favorites.map { it.toFavoriteEntity() })
        }.onFailure { Napier.e("syncFavorites failed", it) }
    }

    // Optimistički update: odmah menjamo Room, zatim zovemo API
    override suspend fun addFavorite(imdbId: String) {
        // 1. Dohvati podatke o filmu iz movie_details tabele ako postoje
        val detail = runCatching { api.getMovieDetail(imdbId) }.getOrNull()
        val entity = rs.edu.raf.rma.movies.data.local.entity.FavoriteEntity(
            imdbId     = imdbId,
            title      = detail?.title ?: "",
            year       = detail?.year,
            imdbRating = detail?.imdbRating?.toDouble(),
            posterPath = detail?.posterPath,
            genresJson = "[]",
        )

        // 2. Optimistički — dodaj u Room odmah
        favoriteDao.upsert(entity)

        // 3. API poziv — ako pukne, rollback
        runCatching {
            api.addFavorite(imdbId)
        }.onFailure {
            Napier.e("addFavorite API failed, rolling back", it)
            favoriteDao.delete(imdbId)
            throw it
        }
    }

    override suspend fun removeFavorite(imdbId: String) {
        // 1. Optimistički — ukloni iz Room odmah
        favoriteDao.delete(imdbId)

        // 2. API poziv — ako pukne, rollback kroz sync
        runCatching {
            api.removeFavorite(imdbId)
        }.onFailure {
            Napier.e("removeFavorite API failed, rolling back", it)
            syncFavorites()
            throw it
        }
    }
}