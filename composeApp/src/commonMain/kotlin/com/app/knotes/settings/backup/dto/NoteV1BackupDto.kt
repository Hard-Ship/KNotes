package com.app.knotes.settings.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteV1BackupDto(
    val id: Long,
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Long,
    val isPinned: Boolean
)
