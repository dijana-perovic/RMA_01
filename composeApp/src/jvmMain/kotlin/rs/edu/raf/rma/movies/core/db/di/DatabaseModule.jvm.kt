package rs.edu.raf.rma.movies.core.db.di

import androidx.room.Room
import org.koin.dsl.module
import rs.edu.raf.rma.movies.core.db.AppDatabase
import rs.edu.raf.rma.movies.core.db.buildAppDatabase
import java.io.File

actual fun databaseModule() = module {
    single<AppDatabase> {
        buildAppDatabase(
            builder = Room.databaseBuilder<AppDatabase>(
                name = File(System.getProperty("user.home"), ".showtime/showtime.db")
                    .also { it.parentFile?.mkdirs() }
                    .absolutePath
            )
        )
    }
    single { get<AppDatabase>().movieDao() }
    single { get<AppDatabase>().genreDao() }
}