package com.app.knotes.settings.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskV1BackupDto(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val timestamp: Long
)
