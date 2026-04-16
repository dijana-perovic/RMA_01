package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.ImageConfig

@Serializable
data class ConfigEntryDto(
    val key: String,
    val value: String
)

fun List<ConfigEntryDto>.toDomain(): ImageConfig {
    val baseUrl = find { it.key == "image_base_url" }?.value ?: ""
    val posterSizes = find { it.key == "poster_sizes" }?.value?.split(",") ?: emptyList()
    val backdropSizes = find { it.key == "backdrop_sizes" }?.value?.split(",") ?: emptyList()
    return ImageConfig(
        baseUrl = baseUrl,
        posterSize = posterSizes.find { it == "w185" } ?: posterSizes.firstOrNull() ?: "w185",
        backdropSize = backdropSizes.find { it == "w780" } ?: backdropSizes.firstOrNull() ?: "w780"
    )
}
