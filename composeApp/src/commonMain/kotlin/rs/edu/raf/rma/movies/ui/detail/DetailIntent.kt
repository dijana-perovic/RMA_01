package rs.edu.raf.rma.movies.ui.detail

sealed class DetailIntent {
    data class LoadDetail(val movieId: String) : DetailIntent()
    object PlayTrailer : DetailIntent()
    object Retry : DetailIntent()
}