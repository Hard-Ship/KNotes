package com.app.knotes.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.currentTimeMillis
import com.app.knotes.db.NotesRepository
import com.app.knotes.task.data.TaskRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class BackupVm(
    private val notesRepository: NotesRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    fun generateBackupJson(file: PlatformFile, onTaskComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val notes = notesRepository.getAllNotes().first()
                val tasks = taskRepository.getAllTasks().first()
                
                val backupDto = BackupDto(
                    notes = notes,
                    tasks = tasks,
                    createdAt = currentTimeMillis(),
                    app = "KNotes"
                )
                
                val backupJson = Json.encodeToString(backupDto)
                file.write(backupJson.encodeToByteArray())
                onTaskComplete("Backup saved successfully!")
            } catch (e: Exception) {
                onTaskComplete("Failed to generate backup: ${e.message}")
            }
        }
    }

    fun restoreFromBackupJson(file: PlatformFile, onTaskComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                val jsonString = bytes.decodeToString()
                val backupDto: BackupDto = Json.decodeFromString(jsonString)
                
                if (backupDto.app != "KNotes") {
                    onTaskComplete("Invalid backup file format.")
                    return@launch
                }

                backupDto.notes.forEach { note ->
                    notesRepository.insert(note.copy(id = 0))
                }
                
                backupDto.tasks.forEach { task ->
                    taskRepository.insertTask(task.copy(id = 0))
                }
                
                onTaskComplete("Successfully restored ${backupDto.notes.size} notes and ${backupDto.tasks.size} tasks.")
            } catch (e: Exception) {
                onTaskComplete("Failed to restore backup: ${e.message}")
            }
        }
    }
}
