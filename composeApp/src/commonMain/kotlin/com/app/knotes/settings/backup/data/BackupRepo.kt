package com.app.knotes.settings.backup.data

import com.app.knotes.currentTimeMillis
import com.app.knotes.db.NotesRepository
import com.app.knotes.settings.backup.dto.BackupEnvelopeDto
import com.app.knotes.settings.backup.dto.BackupV1Dto
import com.app.knotes.task.data.TaskRepository
import com.app.knotes.utils.AppConfig
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class BackupRepo(
    private val notesRepo: NotesRepository,
    private val taskRepo: TaskRepository,
    private val json: Json
) {
    // 32 Bytes key (256-bit)
    private val secretKeyBytes = byteArrayOf(
        75, 78, 111, 116, 101, 115, 66, 97, 99, 107, 117, 112, 83, 101, 99, 114,
        101, 116, 75, 101, 121, 50, 48, 50, 54, 33, 64, 35, 36, 37, 94, 38
    )

    private suspend fun encrypt(data: ByteArray): ByteArray {
        val aes = CryptographyProvider.Default.get(AES.GCM)
        val key = aes.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, secretKeyBytes)
        return key.cipher().encrypt(data)
    }

    private suspend fun decrypt(data: ByteArray): ByteArray {
        val aes = CryptographyProvider.Default.get(AES.GCM)
        val key = aes.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, secretKeyBytes)
        return key.cipher().decrypt(data)
    }

    suspend fun createBackup(): Result<ByteArray> = runCatching {

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
            backupVersion = AppConfig.BACKUP_VERSION,
            data = json.encodeToJsonElement(data)
        )

        val jsonString = json.encodeToString(envelope)
        encrypt(jsonString.encodeToByteArray())
    }


    suspend fun restoreBackup(encryptedBytes: ByteArray): Result<Unit> = runCatching {

        val decryptedBytes = decrypt(encryptedBytes)
        val jsonString = decryptedBytes.decodeToString()
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