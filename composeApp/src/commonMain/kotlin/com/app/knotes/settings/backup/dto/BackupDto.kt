package com.app.knotes.settings.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackupV1Dto(
    val notes: List<NoteV1BackupDto>,
    val tasks: List<TaskV1BackupDto>,
)