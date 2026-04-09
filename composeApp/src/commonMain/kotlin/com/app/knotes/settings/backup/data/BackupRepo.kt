package com.app.knotes.settings.backup.data

import com.app.knotes.currentTimeMillis
import com.app.knotes.db.NotesRepository
import com.app.knotes.settings.backup.dto.BackupEnvelopeDto
import com.app.knotes.settings.backup.dto.BackupV1Dto
import com.app.knotes.task.data.TaskRepository
import com.app.knotes.utils.AppConfig
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class BackupRepo(
    private val notesRepo: NotesRepository,
    private val taskRepo: TaskRepository,
    private val json: Json
) {

    suspend fun createBackup(): Result<String> = runCatching {

        val notes = notesRepo.getAllNotes().first().map { BackupMapper.run { it.toV1Backup() } }
        val tasks = taskRepo.getAllTasks().first().map { BackupMapper.run { it.toV1Backup() } }

        if (notes.isEmpty() && tasks.isEmpty()) {
            throw IllegalStateException("Nothing to backup")
        }

        val data = BackupV1Dto(
            notes = notes,
            tasks = tasks
        )

        val envelope = BackupEnvelopeDto(
            app = AppConfig.APP_NAME,
            appVersion = AppConfig.APP_VERSION,
            createdAtEpochMillis = currentTimeMillis(),
            backupVersion = AppConfig.BACKUP_VERSION ,
            data = json.encodeToJsonElement(data)
        )

        json.encodeToString(envelope)
    }


    suspend fun restoreBackup(jsonString: String): Result<Unit> = runCatching {

        val envelope = json.decodeFromString<BackupEnvelopeDto>(jsonString)

        when (envelope.backupVersion) {

            1 -> {
                val v1 = json.decodeFromJsonElement<BackupV1Dto>(envelope.data)

                val notes = v1.notes.map { BackupMapper.run { it.toEntity() } }
                val tasks = v1.tasks.map { BackupMapper.run { it.toEntity() } }

                notesRepo.insert(notes)
                taskRepo.insertTask(tasks)
            }

            else -> throw IllegalStateException("Unsupported Backup version")
        }
    }

}