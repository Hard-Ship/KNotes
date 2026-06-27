package com.app.knotes.utils

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

object SnackbarController {
    private val _snackbarMessages = Channel<SnackbarMessage>(Channel.BUFFERED)
    val snackbarMessages = _snackbarMessages.receiveAsFlow()

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        _snackbarMessages.trySend(
            SnackbarMessage(
                message = message,
                actionLabel = actionLabel,
                onAction = onAction,
                duration = duration
            )
        )
    }
}
