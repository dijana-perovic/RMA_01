package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.MovieImage

@Serializable
data class MovieImageDto(
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null
)

fun MovieImageDto.toDomain() = MovieImage(
    filePath = filePath,
    width = width,
    height = height
)