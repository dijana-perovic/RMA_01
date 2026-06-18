package rs.edu.raf.rma.movies.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.edu.raf.rma.movies.data.local.dao.FavoriteDao
import rs.edu.raf.rma.movies.data.local.dao.GenreDao
import rs.edu.raf.rma.movies.data.local.dao.MovieDao
import rs.edu.raf.rma.movies.data.local.dao.QuizDao
import rs.edu.raf.rma.movies.data.local.dao.WatchlistDao
import rs.edu.raf.rma.movies.data.local.entity.FavoriteEntity
import rs.edu.raf.rma.movies.data.local.entity.GenreEntity
import rs.edu.raf.rma.movies.data.local.entity.MovieDetailEntity
import rs.edu.raf.rma.movies.data.local.entity.MovieEntity
import rs.edu.raf.rma.movies.data.local.entity.QuizSessionEntity
import rs.edu.raf.rma.movies.data.local.entity.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailEntity::class,
        GenreEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizSessionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun genreDao(): GenreDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun quizDao(): QuizDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}