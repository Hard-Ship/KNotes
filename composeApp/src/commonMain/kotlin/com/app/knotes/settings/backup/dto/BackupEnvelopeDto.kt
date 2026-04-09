package com.app.knotes.settings.backup.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BackupEnvelopeDto(
    val app: String,
    val appVersion: String,
    val createdAtEpochMillis: Long,
    val backupVersion : Int,
    val data: JsonElement,
)
