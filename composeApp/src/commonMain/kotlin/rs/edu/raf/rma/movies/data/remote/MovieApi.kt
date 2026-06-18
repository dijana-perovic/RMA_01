package rs.edu.raf.rma.movies.data.remote

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.movies.data.remote.dto.AuthUserDto
import rs.edu.raf.rma.movies.data.remote.dto.CastMemberDto
import rs.edu.raf.rma.movies.data.remote.dto.ConfigEntryDto
import rs.edu.raf.rma.movies.data.remote.dto.GenreDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieDetailDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieImagesResponseDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieVideoDto
import rs.edu.raf.rma.movies.data.remote.dto.PagedResponse

interface MovieApi {

    // Katalog
    @GET("movies")
    suspend fun getMovies(
        @Query("page_size")  pageSize: Int = 30,
        @Query("sort_by")    sortBy: String = "imdb_rating",
        @Query("sort_order") sortOrder: String = "desc",
        @Query("query")      query: String? = null,
        @Query("genre_id")   genreId: Int? = null,
        @Query("min_year")   minYear: Int? = null,
        @Query("max_year")   maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null
    ): PagedResponse<MovieDto>

    @GET("movies/{id}")
    suspend fun getMovieDetail(@Path("id") id: String): MovieDetailDto

    @GET("movies/{id}/cast")
    suspend fun getCast(
        @Path("id") id: String,
        @Query("page_size") pageSize: Int = 10
    ): PagedResponse<CastMemberDto>

    @GET("movies/{id}/images")
    suspend fun getImages(
        @Path("id") id: String,
        @Query("type") type: String = "backdrop"
    ): MovieImagesResponseDto

    @GET("movies/{id}/videos")
    suspend fun getVideos(
        @Path("id") id: String,
        @Query("type") type: String = "Trailer"
    ): List<MovieVideoDto>

    @GET("genres")
    suspend fun getGenres(): List<GenreDto>

    @GET("config")
    suspend fun getConfig(): List<ConfigEntryDto>

    // Favorites
    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieDto>

    @POST("me/favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: String)

    @DELETE("me/favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: String)

    // Watchlist
    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieDto>

    @POST("me/watchlist/{movieId}")
    suspend fun addToWatchlist(@Path("movieId") movieId: String)

    @DELETE("me/watchlist/{movieId}")
    suspend fun removeFromWatchlist(@Path("movieId") movieId: String)

    // Profile
    @GET("me")
    suspend fun getMe(): AuthUserDto
}