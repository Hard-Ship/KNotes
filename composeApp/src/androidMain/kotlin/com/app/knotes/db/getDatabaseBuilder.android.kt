package com.app.knotes.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single<AppDatabase> {
        val ctx = androidContext().applicationContext
        val dbFile = ctx.getDatabasePath("notes.db")
        val builder = Room.databaseBuilder<AppDatabase>(
            context = ctx,
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
        getRoomDatabase(builder)
    }
}
