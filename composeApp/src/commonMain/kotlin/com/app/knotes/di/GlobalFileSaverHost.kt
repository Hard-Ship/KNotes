package com.app.knotes.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.knotes.utils.FilePickRequest
import com.app.knotes.utils.FilePickerController
import com.app.knotes.utils.FileSaveRequest
import com.app.knotes.utils.FileSaverController
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher

/**
 * Hosts the single global [rememberFileSaverLauncher] and [rememberFilePickerLauncher]
 * for the entire app. Place this once at the app root.
 *
 * Trigger from anywhere via:
 * - [FileSaverController.save] — to open a save dialog
 * - [FilePickerController.pick] — to open an open/import dialog
 */
@Composable
fun GlobalFileSaverHost() {

    // ── File Saver ────────────────────────────────────────────────────────────

    var pendingSaveRequest by remember { mutableStateOf<FileSaveRequest?>(null) }

    val fileSaver = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        pendingSaveRequest?.onResult?.invoke(file)
        pendingSaveRequest = null
    }

    LaunchedEffect(Unit) {
        FileSaverController.requests.collect { request ->
            pendingSaveRequest = request
            fileSaver.launch(
                suggestedName = request.suggestedName,
                extension = request.extension
            )
        }
    }

    // ── File Picker ───────────────────────────────────────────────────────────

    var pendingPickRequest by remember { mutableStateOf<FilePickRequest?>(null) }

    val filePicker = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = pendingPickRequest?.extensions ?: emptyList()),
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        pendingPickRequest?.onResult?.invoke(file)
        pendingPickRequest = null
    }

    LaunchedEffect(Unit) {
        FilePickerController.requests.collect { request ->
            pendingPickRequest = request
            filePicker.launch()
        }
    }
}

