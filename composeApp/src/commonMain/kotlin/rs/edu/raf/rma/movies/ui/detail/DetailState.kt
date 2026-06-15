package rs.edu.raf.rma.movies.ui.detail

import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.ImageConfig
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.model.MovieImage
import rs.edu.raf.rma.movies.domain.model.MovieVideo

data class DetailState(
    val isLoading: Boolean = false,
    val movie: MovieDetail? = null,
    val images: List<MovieImage> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val videos: List<MovieVideo> = emptyList(),
    val imageConfig: ImageConfig? = null,
    val error: String? = null,
    val trailerUrl: String? = null
) {
    val trailerKey: String? get() = videos.firstOrNull { it.site == "YouTube" }?.key
    val isSuccess: Boolean get() = !isLoading && error == null && movie != null
}