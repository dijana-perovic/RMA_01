package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.MovieVideo

@Serializable
data class MovieVideoDto(
    val key: String,
    val site: String,
    val name: String? = null,
    val type: String? = null
)

fun MovieVideoDto.toDomain() = MovieVideo(
    key = key,
    site = site,
    name = name,
    type = type
)
