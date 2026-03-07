package com.app.knotes.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module

expect fun createDataStore(context: Any? = null): DataStore<Preferences>
expect fun platformSettingsModule(): Module

val DATASTORE_FILE_NAME = "settings.preferences_pb"
