package com.app.knotes.settings.backup.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.settings.backup.data.BackupRepo
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupVm(
    private val backupRepo: BackupRepo,
) : ViewModel() {

    fun generateBackup(file: PlatformFile, onTaskComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    backupRepo.createBackup()
                }.onSuccess { backupBytes ->

                    withContext(Dispatchers.Default) {
                        file.write(backupBytes)
                    }
                    onTaskComplete("Backup saved successfully!")

                }.onFailure {
                    onTaskComplete(it.message ?: "Failed to generate backup")
                }

            } catch (e: Exception) {
                onTaskComplete(e.message ?: "Failed to generate backup")
            }
        }
    }

    fun restoreBackup(file: PlatformFile, onTaskComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {

                val bytes = withContext(Dispatchers.Default) {
                    file.readBytes()
                }

                withContext(Dispatchers.Default) {
                    backupRepo.restoreBackup(bytes)
                }.onSuccess {
                    onTaskComplete("Successfully restored.")
                }.onFailure {
                    onTaskComplete(it.message ?: "Failed to restore backup")
                }

            } catch (e: Exception) {
                onTaskComplete(e.message ?: "Failed to restore backup")
            }
        }
    }
}