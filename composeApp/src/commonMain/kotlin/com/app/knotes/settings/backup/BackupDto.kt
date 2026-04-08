package com.app.knotes.settings.backup

import com.app.knotes.db.NoteEntity
import com.app.knotes.task.core.TaskEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupDto(
    val app: String,
    val createdAt: Long,
    val notes: List<NoteEntity>,
    val tasks: List<TaskEntity>,
)
