package com.app.knotes

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
@Serializable
sealed interface NavRoute {
    @Serializable
    data object Home : NavRoute
    
    @Serializable
    data class NoteDetail(val noteId: Int) : NavRoute
}

@Composable
fun AppNavigation() {
    val backstack = remember { mutableStateListOf<NavRoute>(NavRoute.Home) }
    val pop = {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
        }
    }

    NavDisplay(
        backStack = backstack,
        onBack = pop,
        entryProvider = entryProvider {

            entry<NavRoute.Home> {
                NotesScreen(
                    onNoteClick = { id ->
                        backstack.add(NavRoute.NoteDetail(id))
                    }
                )
            }

            entry<NavRoute.NoteDetail> { route ->
                NoteDetailScreen(
                    noteId = route.noteId,
                    onBack = pop
                )
            }
        }
    )
}
