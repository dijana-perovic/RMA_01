package rs.edu.raf.rma.movies.ui.filter

import rs.edu.raf.rma.movies.domain.model.FilterParams
import rs.edu.raf.rma.movies.domain.model.Genre

interface FilterContract {

    data class UiState(
        val searchQuery: String = "",
        val selectedGenreIds: Set<Int> = emptySet(),
        val yearFrom: Int? = null,
        val yearTo: Int? = null,
        val minRating: Float? = null,
        val genres: List<Genre> = emptyList(),
        val isLoadingGenres: Boolean = false,
        val error: Throwable? = null
    ) {
        val isEmpty: Boolean   get() = !isLoadingGenres && error == null && genres.isEmpty()
        val isOffline: Boolean get() = error?.message?.contains("Unable to resolve host") == true

        fun toFilterParams() = FilterParams(
            query            = searchQuery,
            selectedGenreIds = selectedGenreIds,
            yearFrom         = yearFrom,
            yearTo           = yearTo,
            minRating        = minRating,
            sortBy           = "imdb_rating"
        )

        companion object {
            fun fromFilterParams(params: FilterParams) = UiState(
                searchQuery      = params.query,
                selectedGenreIds = params.selectedGenreIds,
                yearFrom         = params.yearFrom,
                yearTo           = params.yearTo,
                minRating        = params.minRating
            )
        }
    }

    sealed class UiEvent {
        data class Initialize(val params: FilterParams)   : UiEvent()
        data class UpdateSearch(val query: String)        : UiEvent()
        data class ToggleGenre(val genreId: Int)          : UiEvent()
        data class SetYearFrom(val year: Int?)            : UiEvent()
        data class SetYearTo(val year: Int?)              : UiEvent()
        data class SetMinRating(val rating: Float?)       : UiEvent()
        data object Apply                                 : UiEvent()
        data object ClearAll                              : UiEvent()
        data object RetryGenres                           : UiEvent()
        data object LoadGenresIfEmpty                     : UiEvent()
    }

    sealed class SideEffect {
        data class ApplyAndClose(val params: FilterParams) : SideEffect()
    }
}