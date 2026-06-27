package com.app.knotes.utils

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// ── File Saver ────────────────────────────────────────────────────────────────

data class FileSaveRequest(
    val suggestedName: String,
    val extension: String,
    val onResult: (PlatformFile?) -> Unit
)

object FileSaverController {
    private val _requests = Channel<FileSaveRequest>(Channel.BUFFERED)
    val requests = _requests.receiveAsFlow()

    fun save(
        suggestedName: String,
        extension: String,
        onResult: (PlatformFile?) -> Unit
    ) {
        _requests.trySend(
            FileSaveRequest(
                suggestedName = suggestedName,
                extension = extension,
                onResult = onResult
            )
        )
    }
}

// ── File Picker ───────────────────────────────────────────────────────────────

data class FilePickRequest(
    val extensions: List<String>,
    val onResult: (PlatformFile?) -> Unit
)

object FilePickerController {
    private val _requests = Channel<FilePickRequest>(Channel.BUFFERED)
    val requests = _requests.receiveAsFlow()

    fun pick(
        extensions: List<String>,
        onResult: (PlatformFile?) -> Unit
    ) {
        _requests.trySend(
            FilePickRequest(
                extensions = extensions,
                onResult = onResult
            )
        )
    }
}
