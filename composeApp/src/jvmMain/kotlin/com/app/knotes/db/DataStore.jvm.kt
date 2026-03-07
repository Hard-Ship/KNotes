package com.app.knotes.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun createDataStore(context: Any?): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            File(DATASTORE_FILE_NAME).absolutePath.toPath()
        }
    )
}

actual fun platformSettingsModule(): Module = module {
    single<DataStore<Preferences>> { createDataStore() }
}
