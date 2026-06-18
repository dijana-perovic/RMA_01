package rs.edu.raf.rma.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostQuizResultRequestDto(
    val score: Double,
    val category: Int,
)

@Serializable
data class PostQuizResultResponseDto(
    val result: QuizResultDto,
    val ranking: Int,
)

@Serializable
data class QuizResultDto(
    val id: Long,
    val category: Int,
    val score: Double,
    @SerialName("played_at")
    val playedAt: Long,
)