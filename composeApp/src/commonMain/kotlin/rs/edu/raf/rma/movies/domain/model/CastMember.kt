package rs.edu.raf.rma.movies.domain.model

data class CastMember(
    val imdbId: String,
    val name: String,
    val department: String?,
    val profilePath: String?
)
