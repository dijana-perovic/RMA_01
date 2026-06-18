package rs.edu.raf.rma.movies.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import rs.edu.raf.rma.movies.data.local.dao.MovieDao
import rs.edu.raf.rma.movies.data.local.dao.QuizDao
import rs.edu.raf.rma.movies.data.local.entity.MovieDetailEntity
import rs.edu.raf.rma.movies.data.local.entity.QuizSessionEntity
import rs.edu.raf.rma.movies.data.remote.MovieApi
import rs.edu.raf.rma.movies.data.remote.dto.CastMemberDto
import rs.edu.raf.rma.movies.data.remote.dto.MovieImageDto
import rs.edu.raf.rma.movies.data.remote.dto.PostQuizResultRequestDto
import rs.edu.raf.rma.movies.data.remote.dto.QuizResultDto
import rs.edu.raf.rma.movies.domain.model.QuizQuestion
import rs.edu.raf.rma.movies.domain.model.QuizResult
import rs.edu.raf.rma.movies.domain.model.QuizSession
import rs.edu.raf.rma.movies.domain.repository.QuizRepository

class QuizRepositoryImpl(
    private val api: MovieApi,
    private val movieDao: MovieDao,
    private val quizDao: QuizDao,
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        const val QUESTIONS_PER_SESSION = 10
        const val MAX_SAME_TYPE = 4
        const val MIN_ELIGIBLE_MOVIES = 10
    }

    override suspend fun hasEnoughMovies(): Boolean =
        movieDao.getEligibleMovieCount() >= MIN_ELIGIBLE_MOVIES

    override suspend fun clearAll() {
        quizDao.clearAll()
    }

    override suspend fun generateQuestions(): List<QuizQuestion> {
        val allDetails = movieDao.getAllMovieDetails()
        val eligible = allDetails.filter { detail ->
            val images = decodeImages(detail.imagesJson)
            images.isNotEmpty() && detail.posterPath != null
        }

        if (eligible.size < MIN_ELIGIBLE_MOVIES) return emptyList()

        val questions = mutableListOf<QuizQuestion>()
        val usedImageUrls = mutableSetOf<String>()
        val typeCounts = mutableMapOf(
            QuestionType.GUESS_MOVIE to 0,
            QuestionType.GUESS_YEAR to 0,
            QuestionType.GUESS_ACTOR to 0,
        )

        val shuffled = eligible.shuffled()
        var index = 0

        while (questions.size < QUESTIONS_PER_SESSION && index < shuffled.size) {
            val detail = shuffled[index++]

            val availableTypes = typeCounts.filter { it.value < MAX_SAME_TYPE }.keys.toList()
            if (availableTypes.isEmpty()) break

            val type = availableTypes.random()

            val question = when (type) {
                QuestionType.GUESS_MOVIE ->
                    generateGuessMovieQuestion(detail, eligible, usedImageUrls)
                QuestionType.GUESS_YEAR ->
                    generateGuessYearQuestion(detail, eligible)
                QuestionType.GUESS_ACTOR ->
                    generateGuessActorQuestion(detail, eligible)
            } ?: continue

            questions.add(question)
            typeCounts[type] = (typeCounts[type] ?: 0) + 1

            if (question is QuizQuestion.GuessTheMovie) {
                usedImageUrls.add(question.imageUrl)
            }
        }

        return questions.shuffled()
    }

    private fun generateGuessMovieQuestion(
        detail: MovieDetailEntity,
        allDetails: List<MovieDetailEntity>,
        usedImageUrls: MutableSet<String>,
    ): QuizQuestion.GuessTheMovie? {
        val images = decodeImages(detail.imagesJson)
            .filter { it.filePath !in usedImageUrls }

        if (images.isEmpty()) return null

        val imageUrl = images.random().filePath

        val wrongMovies = allDetails
            .filter { it.imdbId != detail.imdbId }
            .shuffled()
            .take(3)

        if (wrongMovies.size < 3) return null

        val options = (listOf(detail.title) + wrongMovies.map { it.title }).shuffled()

        return QuizQuestion.GuessTheMovie(
            movieId       = detail.imdbId,
            imageUrl      = imageUrl,
            options       = options,
            correctAnswer = detail.title,
        )
    }

    private fun generateGuessYearQuestion(
        detail: MovieDetailEntity,
        allDetails: List<MovieDetailEntity>,
    ): QuizQuestion.GuessTheYear? {
        val correctYear = detail.year ?: return null
        val posterPath  = detail.posterPath ?: return null

        val wrongYears = mutableSetOf<Int>()
        val shuffledOffsets = ((-10..-1).toList() + (1..10).toList()).shuffled()

        for (offset in shuffledOffsets) {
            if (wrongYears.size >= 3) break
            val year = correctYear + offset
            if (year > 1900) wrongYears.add(year)
        }

        if (wrongYears.size < 3) return null

        val options = (listOf(correctYear.toString()) + wrongYears.map { it.toString() })
            .shuffled()

        return QuizQuestion.GuessTheYear(
            movieId       = detail.imdbId,
            posterUrl     = posterPath,
            movieTitle    = detail.title,
            options       = options,
            correctAnswer = correctYear.toString(),
        )
    }

    private fun generateGuessActorQuestion(
        detail: MovieDetailEntity,
        allDetails: List<MovieDetailEntity>,
    ): QuizQuestion.GuessTheActor? {
        val posterPath = detail.posterPath ?: return null

        val cast = decodeCast(detail.castJson)
        if (cast.isEmpty()) return null

        val correctActor = cast.take(3).random()

        val allActors = allDetails
            .flatMap { decodeCast(it.castJson) }
            .filter { it.name != correctActor.name }
            .distinctBy { it.name }
            .shuffled()
            .take(3)

        if (allActors.size < 3) return null

        val options = (listOf(correctActor.name) + allActors.map { it.name }).shuffled()

        return QuizQuestion.GuessTheActor(
            movieId       = detail.imdbId,
            posterUrl     = posterPath,
            movieTitle    = detail.title,
            options       = options,
            correctAnswer = correctActor.name,
        )
    }

    private fun decodeImages(imagesJson: String): List<MovieImageDto> =
        runCatching {
            json.decodeFromString<List<MovieImageDto>>(imagesJson)
        }.getOrDefault(emptyList())

    private fun decodeCast(castJson: String): List<CastMemberDto> =
        runCatching {
            json.decodeFromString<List<CastMemberDto>>(castJson)
        }.getOrDefault(emptyList())

    override suspend fun saveResult(result: QuizResult) {
        val response = api.submitQuizResult(
            PostQuizResultRequestDto(
                score = result.score,
                category = 1
            )
        )

        quizDao.upsertSession(
            QuizSessionEntity(
                id = response.result.id,
                score = response.result.score,
                correctAnswers = result.correctAnswers,
                incorrectAnswers = result.incorrectAnswers,
                timeUsedSeconds = result.timeUsedSeconds,
                playedAt = response.result.playedAt
            )
        )
    }

    override fun observeBestScore(): Flow<Double?> =
        quizDao.observeBestScore()

    override fun observeSessionCount(): Flow<Int> =
        quizDao.observeSessionCount()

    override fun observeSessions(): Flow<List<QuizSession>> =
        quizDao.observeSessions().map { sessions ->
            sessions.map { entity ->
                QuizSession(
                    id               = entity.id,
                    score            = entity.score,
                    correctAnswers   = entity.correctAnswers,
                    incorrectAnswers = entity.incorrectAnswers,
                    timeUsedSeconds  = entity.timeUsedSeconds,
                    playedAt         = entity.playedAt,
                )
            }
        }

    override suspend fun syncQuizResults() {
        val response = api.getQuizResults(
            page = 1,
            pageSize = 100
        )

        quizDao.clearAll()

        response.items.forEach {
            quizDao.upsertSession(
                QuizSessionEntity(
                    id = it.id,
                    score = it.score,
                    correctAnswers = 0,
                    incorrectAnswers = 0,
                    timeUsedSeconds = 0,
                    playedAt = it.playedAt
                )
            )
        }
    }

    private enum class QuestionType {
        GUESS_MOVIE, GUESS_YEAR, GUESS_ACTOR
    }
}