package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieImagesResponseDto(
    val posters: List<MovieImageDto> = emptyList(),
    val backdrops: List<MovieImageDto> = emptyList(),
    val logos: List<MovieImageDto> = emptyList()
)
