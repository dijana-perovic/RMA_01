package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable
import rs.edu.raf.rma.movies.domain.model.CastMember

@Serializable
data class CastMemberDto(
    val imdbId: String,
    val name: String,
    val department: String? = null,
    val profilePath: String? = null
)

fun CastMemberDto.toDomain() = CastMember(
    imdbId = imdbId,
    name = name,
    department = department,
    profilePath = profilePath
)