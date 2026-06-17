package rs.edu.raf.rma.movies.ui.detail

sealed class DetailSideEffect {
    data class OpenUrl(val url: String) : DetailSideEffect()
}