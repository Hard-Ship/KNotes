package com.app.knotes.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.db.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsVm(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun toggleTheme() {
        viewModelScope.launch {
            settingsRepository.toggleTheme()
        }
    }
}
