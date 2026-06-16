package rs.edu.raf.rma.movies.core.auth.di

import androidx.datastore.core.DataStore
import org.koin.dsl.module
import rs.edu.raf.rma.movies.core.auth.AuthStore
import rs.edu.raf.rma.movies.core.auth.createAuthDataStore
import rs.edu.raf.rma.movies.core.auth.model.AuthData

val authModule = module {
    single<DataStore<AuthData>> { createAuthDataStore() }
    single<AuthStore> { AuthStore(persistence = get()) }
}