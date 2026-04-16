package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<T> = emptyList()
)
