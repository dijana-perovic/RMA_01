package rs.edu.raf.rma.movies.domain.repository

import rs.edu.raf.rma.movies.domain.model.ImageConfig

interface ConfigRepository {
    suspend fun getConfig(): ImageConfig
}