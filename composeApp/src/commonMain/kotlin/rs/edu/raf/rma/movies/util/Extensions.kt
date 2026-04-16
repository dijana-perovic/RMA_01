package rs.edu.raf.rma.movies.util

fun Int.formatVotes(): String {
    return when {
        this >= 1_000_000 -> "${kotlin.math.round(this / 100_000.0) / 10.0}M"
        this >= 1_000 -> "${this / 1_000}K"
        else -> this.toString()
    }
}