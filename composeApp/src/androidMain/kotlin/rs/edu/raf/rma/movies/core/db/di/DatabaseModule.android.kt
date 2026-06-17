package rs.edu.raf.rma.movies.core.db.di

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module
import rs.edu.raf.rma.movies.core.db.AppDatabase
import rs.edu.raf.rma.movies.core.db.buildAppDatabase

actual fun databaseModule() = module {
    single<AppDatabase> {
        val context: Context = get()
        buildAppDatabase(
            builder = Room.databaseBuilder<AppDatabase>(
                context = context,
                name = context.getDatabasePath("showtime.db").absolutePath,
            )
        )
    }
    single { get<AppDatabase>().movieDao() }
    single { get<AppDatabase>().genreDao() }
}