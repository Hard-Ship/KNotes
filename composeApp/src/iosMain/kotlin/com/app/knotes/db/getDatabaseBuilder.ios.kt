package com.app.knotes.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSHomeDirectory

actual fun platformDatabaseModule(): Module = module {
    single<AppDatabase> {
        val dbFile = NSHomeDirectory() + "/notes.db"
        val builder = Room.databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
        getRoomDatabase(builder)
    }
}
