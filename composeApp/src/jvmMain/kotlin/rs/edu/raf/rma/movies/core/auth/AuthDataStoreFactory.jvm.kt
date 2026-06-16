package rs.edu.raf.rma.movies.core.auth

import java.io.File

private const val AUTH_DATA_FILE_NAME = "showtime_auth_data.json"

actual fun createAuthDataStorePath(): String {
    val dir = File(System.getProperty("user.home"), ".showtime").also { it.mkdirs() }
    return File(dir, AUTH_DATA_FILE_NAME).absolutePath
}