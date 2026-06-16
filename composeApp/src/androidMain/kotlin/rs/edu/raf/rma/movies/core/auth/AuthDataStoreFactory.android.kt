package rs.edu.raf.rma.movies.core.auth

import android.content.Context
import org.koin.mp.KoinPlatform.getKoin

private const val AUTH_DATA_FILE_NAME = "showtime_auth_data.json"

actual fun createAuthDataStorePath(): String {
    val context: Context = getKoin().get()
    return context.filesDir.absolutePath + "/$AUTH_DATA_FILE_NAME"
}
