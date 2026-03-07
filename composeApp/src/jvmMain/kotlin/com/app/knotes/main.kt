package com.app.knotes

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.app.knotes.di.App
import knotes.composeapp.generated.resources.Res
import knotes.composeapp.generated.resources.notes
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KNotes",
    ) {
        App()
    }
}