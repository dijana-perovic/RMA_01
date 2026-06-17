package rs.edu.raf.rma.movies.data.local.mapper

import kotlinx.serialization.json.Json
import rs.edu.raf.rma.movies.data.local.entity.FavoriteEntity
import rs.edu.raf.rma.movies.data.local.entity.GenreEntity
import rs.edu.raf.rma.movies.data.local.entity.MovieDetailEntity
import rs.edu.raf.rma.movies.data.local.entity.MovieEntity
import rs.edu.raf.rma.movies.data.local.entity.WatchlistEntity
import rs.edu.raf.rma.movies.data.remote.dto.CastMemberDto
import rs.edu.raf.rma.movies.data.remote.dto.GenreDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieDetailDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieImageDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieVideoDto
import rs.edu.raf.rma.movies.domain.model.CastMember
import rs.edu.raf.rma.movies.domain.model.Genre
import rs.edu.raf.rma.movies.domain.model.Movie
import rs.edu.raf.rma.movies.domain.model.MovieDetail
import rs.edu.raf.rma.movies.domain.model.MovieImage
import rs.edu.raf.rma.movies.domain.model.MovieVideo

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

// DTO → Entity

fun MovieDto.toEntity() = MovieEntity(
    imdbId     = imdbId,
    title      = title,
    year       = year,
    imdbRating = imdbRating?.toDouble(),
    imdbVotes  = imdbVotes,
    posterPath = posterPath,
    genresJson = json.encodeToString(genres)
)

fun MovieDetailDto.toEntity(
    cast: List<CastMemberDto>,
    images: List<MovieImageDto>,
    videos: List<MovieVideoDto>
) = MovieDetailEntity(
    imdbId        = imdbId,
    tmdbId        = tmdbId,
    title         = title,
    originalTitle = originalTitle,
    overview      = overview,
    tagline       = tagline,
    year          = year,
    runtime       = runtime,
    budget        = budget,
    revenue       = revenue,
    languageCode  = languageCode,
    popularity    = popularity,
    imdbRating    = imdbRating,
    imdbVotes     = imdbVotes,
    tmdbRating    = tmdbRating,
    posterPath    = posterPath,
    backdropPath  = backdropPath,
    genresJson    = json.encodeToString(genres),
    castJson      = json.encodeToString(cast),
    imagesJson    = json.encodeToString(images),
    videosJson    = json.encodeToString(videos)
)

fun GenreDto.toEntity() = GenreEntity(id = id, name = name)

// Entity → Domain

fun MovieEntity.toDomain() = Movie(
    imdbId     = imdbId,
    title      = title,
    year       = year,
    imdbRating = imdbRating,
    imdbVotes  = imdbVotes,
    posterPath = posterPath,
    genres     = json.decodeFromString<List<GenreDto>>(genresJson)
        .map { Genre(it.id, it.name) }
)

fun MovieDetailEntity.toDomain() = MovieDetail(
    imdbId        = imdbId,
    tmdbId        = tmdbId,
    title         = title,
    originalTitle = originalTitle,
    overview      = overview,
    tagline       = tagline,
    year          = year,
    runtime       = runtime,
    budget        = budget,
    revenue       = revenue,
    languageCode  = languageCode,
    popularity    = popularity,
    imdbRating    = imdbRating,
    imdbVotes     = imdbVotes,
    tmdbRating    = tmdbRating,
    posterPath    = posterPath,
    backdropPath  = backdropPath,
    genres        = json.decodeFromString<List<GenreDto>>(genresJson)
        .map { Genre(it.id, it.name) },
    cast          = json.decodeFromString<List<CastMemberDto>>(castJson)
        .map { CastMember(it.imdbId, it.name, it.department, it.profilePath) },
    images        = json.decodeFromString<List<MovieImageDto>>(imagesJson)
        .map { MovieImage(it.filePath, it.width, it.height) },
    videos        = json.decodeFromString<List<MovieVideoDto>>(videosJson)
        .map { MovieVideo(it.key, it.site, it.name, it.type) }
)

fun GenreEntity.toDomain() = Genre(id = id, name = name)

// MovieDto → FavoriteEntity / WatchlistEntity

fun MovieDto.toFavoriteEntity() = FavoriteEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating?.toDouble(),
    posterPath = posterPath,
    genresJson = json.encodeToString(genres),
)

fun MovieDto.toWatchlistEntity() = WatchlistEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating?.toDouble(),
    posterPath = posterPath,
    genresJson = json.encodeToString(genres),
)

// FavoriteEntity / WatchlistEntity → Domain

fun FavoriteEntity.toDomain() = Movie(
    imdbId     = imdbId,
    title      = title,
    year       = year,
    imdbRating = imdbRating,
    imdbVotes  = null,
    posterPath = posterPath,
    genres     = json.decodeFromString<List<GenreDto>>(genresJson)
        .map { Genre(it.id, it.name) },
)

fun WatchlistEntity.toDomain() = Movie(
    imdbId     = imdbId,
    title      = title,
    year       = year,
    imdbRating = imdbRating,
    imdbVotes  = null,
    posterPath = posterPath,
    genres     = json.decodeFromString<List<GenreDto>>(genresJson)
        .map { Genre(it.id, it.name) },
)