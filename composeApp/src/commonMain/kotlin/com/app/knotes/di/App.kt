package com.app.knotes.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.app.knotes.AppNavigation
import com.app.knotes.db.platformDatabaseModule
import com.app.knotes.db.platformSettingsModule
import com.app.knotes.db.SettingsRepository
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.app.knotes.theme.AppTheme
import com.app.knotes.utils.SnackbarController
import kotlinx.coroutines.flow.collectLatest
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
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) {
                SnackbarController.snackbarMessages.collectLatest { event ->
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = event.duration
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
            }
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { paddingValues ->
                GlobalFileSaverHost()
                Box(modifier = Modifier.padding(paddingValues)) {
                    AppNavigation()
                }
            }
        }
    }
}