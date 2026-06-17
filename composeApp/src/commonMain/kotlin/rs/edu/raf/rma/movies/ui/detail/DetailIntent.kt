package rs.edu.raf.rma.movies.ui.detail

sealed class DetailIntent {
    object LoadDetail : DetailIntent()
    object PlayTrailer : DetailIntent()
    object Retry : DetailIntent()
}