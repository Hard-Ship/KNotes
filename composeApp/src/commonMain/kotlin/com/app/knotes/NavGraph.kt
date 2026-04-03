package com.app.knotes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.app.knotes.task.pres.TasksScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Root : NavRoute

    @Serializable
    data class NoteDetail(val noteId: Int) : NavRoute
}

enum class RootTab(val label: String, val icon: ImageVector) {
    Tasks("Tasks", Icons.Rounded.Description),
    Notes("Notes", Icons.Rounded.CheckCircle)
}

@Composable
fun AppNavigation() {
    val backstack = remember { mutableStateListOf<NavRoute>(NavRoute.Root) }
    val pop = {
        if (backstack.size > 1) {
            backstack.removeAt(backstack.lastIndex)
        }
    }

    NavDisplay(
        backStack = backstack,
        onBack = pop,
        entryProvider = entryProvider {

            entry<NavRoute.Root> {
                RootScreen(
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

@Composable
fun RootScreen(onNoteClick: (Int) -> Unit) {
    var selectedTab by remember { mutableStateOf(RootTab.Tasks) }
    val pillShape = RoundedCornerShape(100)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                NavigationRail(
                    modifier = Modifier.padding(16.dp).navigationBarsPadding()
                        .shadow(8.dp, pillShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            shape = pillShape
                        )
                        .clip(pillShape)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    RootTab.entries.forEach { destinationTab ->
                        NavigationRailItem(
                            selected = selectedTab == destinationTab,
                            onClick = { selectedTab = destinationTab },
                            icon = {
                                Icon(
                                    destinationTab.icon,
                                    contentDescription = destinationTab.label
                                )
                            },
                            label = { Text(destinationTab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        RootTab.Tasks -> TasksScreen()
                        RootTab.Notes -> NotesScreen(onNoteClick = onNoteClick)
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    FloatingDockNavBar(
                        selectedTab = selectedTab,
                        onItemSelected = {
                            selectedTab = it
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    when (selectedTab) {
                        RootTab.Tasks -> TasksScreen()
                        RootTab.Notes -> NotesScreen(onNoteClick = onNoteClick)
                    }
                }
            }
        }
    }

}

@Composable
fun FloatingDockNavBar(
    selectedTab: RootTab,
    onItemSelected: (RootTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        val pillShape = RoundedCornerShape(100)

        NavigationBar(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 48.dp)
                .shadow(12.dp, pillShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = pillShape
                )
                .clip(pillShape)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            RootTab.entries.forEach { destinationTab ->
                NavigationBarItem(
                    selected = selectedTab == destinationTab,
                    onClick = { onItemSelected(destinationTab) },
                    icon = {
                        Icon(
                            destinationTab.icon,
                            contentDescription = destinationTab.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = null,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}