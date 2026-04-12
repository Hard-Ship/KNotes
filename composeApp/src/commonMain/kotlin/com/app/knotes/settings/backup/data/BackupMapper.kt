package com.app.knotes.settings.backup.data

import com.app.knotes.db.NoteEntity
import com.app.knotes.settings.backup.dto.NoteV1BackupDto
import com.app.knotes.settings.backup.dto.TaskV1BackupDto
import com.app.knotes.task.core.TaskEntity

object BackupMapper {

    // Note
    fun NoteV1BackupDto.toEntity(): NoteEntity =
        NoteEntity(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            color = color,
            isPinned = isPinned
        )


    fun NoteEntity.toV1Backup(): NoteV1BackupDto =
        NoteV1BackupDto(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            color = color,
            isPinned = isPinned
        )


    // Task
    fun TaskV1BackupDto.toEntity(): TaskEntity =
        TaskEntity(
            id = id,
            title = title,
            isCompleted = isCompleted,
            timestamp = timestamp,
        )


    fun TaskEntity.toV1Backup(): TaskV1BackupDto =
        TaskV1BackupDto(
            id = id,
            title = title,
            isCompleted = isCompleted,
            timestamp = timestamp,
        )

}