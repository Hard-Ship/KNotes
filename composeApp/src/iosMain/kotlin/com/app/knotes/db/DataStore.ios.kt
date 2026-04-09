package com.app.knotes.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

private var dataStore: DataStore<Preferences>? = null

@OptIn(ExperimentalForeignApi::class)
actual fun createDataStore(context: Any?): DataStore<Preferences> {
    return dataStore ?: PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val directory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            (directory?.path + "/$DATASTORE_FILE_NAME").toPath()
        }
    ).also { dataStore = it }
}

actual fun platformSettingsModule(): Module = module {
    single<DataStore<Preferences>> { createDataStore() }
}
