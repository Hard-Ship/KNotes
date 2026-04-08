package com.app.knotes.di

import com.app.knotes.network.NotesApi
import com.app.knotes.network.HttpClientFactory
import com.app.knotes.NotesVm
import com.app.knotes.db.AppDatabase
import com.app.knotes.db.NotesRepository
import com.app.knotes.db.SettingsRepository
import com.app.knotes.task.core.TaskVm
import com.app.knotes.task.data.TaskRepository
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single { HttpClientFactory.create() }
    single { NotesApi(get()) }

    single { get<AppDatabase>().noteDao() }
    single { NotesRepository(get(), get()) }
    single { SettingsRepository(get()) }
    
    single { get<AppDatabase>().taskDao() }
    single { TaskRepository(get()) }

    viewModelOf(::NotesVm)
    viewModelOf(::TaskVm)
}