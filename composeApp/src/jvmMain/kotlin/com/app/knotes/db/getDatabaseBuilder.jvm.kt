package com.app.knotes.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun platformDatabaseModule(): Module = module {
    single<AppDatabase> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "notes.db")
        val builder = Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
        getRoomDatabase(builder)
    }
}
