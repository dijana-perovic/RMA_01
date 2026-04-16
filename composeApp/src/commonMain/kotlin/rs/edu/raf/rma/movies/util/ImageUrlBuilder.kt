package rs.edu.raf.rma.movies.util

import rs.edu.raf.rma.movies.domain.model.ImageConfig

object ImageUrlBuilder {
    fun poster(config: ImageConfig, path: String?): String? {
        if (path == null) return null
        return "${config.baseUrl}${config.posterSize}$path"
    }

    fun backdrop(config: ImageConfig, path: String?): String? {
        if (path == null) return null
        return "${config.baseUrl}${config.backdropSize}$path"
    }

    fun profile(config: ImageConfig, path: String?): String? {
        if (path == null) return null
        return "${config.baseUrl}w185$path"
    }
}