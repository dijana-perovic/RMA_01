package rs.edu.raf.rma.movies.core.auth

import androidx.datastore.core.okio.OkioSerializer
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import rs.edu.raf.rma.movies.core.auth.model.AuthData

object AuthDataSerializer : OkioSerializer<AuthData> {

    override val defaultValue: AuthData = AuthData.empty()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun readFrom(source: BufferedSource): AuthData {
        return try {
            val jsonString = source.readUtf8()
            if (jsonString.isEmpty()) return defaultValue
            json.decodeFromString<AuthData>(jsonString)
        } catch (e: Exception) {
            Napier.e(e) { "Failed to read AuthData, returning default" }
            defaultValue
        }
    }

    override suspend fun writeTo(t: AuthData, sink: BufferedSink) {
        sink.writeUtf8(
            json.encodeToString(serializer = AuthData.serializer(), value = t)
        )
    }
}
