package com.app.knotes.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.knotes.AppNavigation
import com.app.knotes.db.platformDatabaseModule
import com.app.knotes.db.platformSettingsModule
import com.app.knotes.db.SettingsRepository
import com.app.knotes.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.KoinAppDeclaration

@Composable
fun App(platformConfig: KoinAppDeclaration = {}) {
    KoinApplication(application = {
        platformConfig()
        modules(platformDatabaseModule(), platformSettingsModule(), appModule)
    }) {
        val settingsRepository: com.app.knotes.db.SettingsRepository = koinInject()
        val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = false)

        AppTheme(darkTheme = isDarkMode) {
            AppNavigation()
        }
    }
}