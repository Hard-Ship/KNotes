package com.app.knotes.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun createDataStore(context: Any?): DataStore<Preferences> {
    val ctx = context as Context
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            ctx.filesDir.resolve(DATASTORE_FILE_NAME).absolutePath.toPath()
        }
    )
}

actual fun platformSettingsModule(): Module = module {
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
}
