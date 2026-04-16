package rs.edu.raf.rma.movies.data.repository

import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.data.remote.dto.toDomain
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.repository.ConfigRepository

class ConfigRepositoryImpl(
    private val api: MovieApi
) : ConfigRepository {

    override suspend fun getConfig(): ImageConfig =
        api.getConfig().toDomain()
}