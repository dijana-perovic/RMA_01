package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.Genre

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

fun GenreDto.toDomain() = Genre(id = id, name = name)