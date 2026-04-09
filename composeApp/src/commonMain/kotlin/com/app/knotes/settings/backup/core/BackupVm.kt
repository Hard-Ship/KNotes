package com.app.knotes.settings.backup.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.settings.backup.data.BackupRepo
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class BackupVm(
    private val backupRepo: BackupRepo,
) : ViewModel() {

    fun generateBackup(file: PlatformFile, onTaskComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                backupRepo.createBackup().onSuccess { backup ->

                    val backupJson = Json.encodeToString(backup)
                    file.write(backupJson.encodeToByteArray())
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

                val bytes = file.readBytes()
                val jsonString = bytes.decodeToString()

                backupRepo.restoreBackup(jsonString).onSuccess {
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