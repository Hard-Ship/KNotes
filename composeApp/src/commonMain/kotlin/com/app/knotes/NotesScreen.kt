package com.app.knotes


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.app.knotes.db.NoteEntity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.app.knotes.theme.AppTheme
import com.app.knotes.theme.noteColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun NotesScreen(
    onNoteClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val viewModel: NotesVm = koinViewModel()
    val state by viewModel.state.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    when (state) {
        is NotesScreenUiState.Error -> {
            val data = (state as NotesScreenUiState.Error)
            Column(
                modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    data.message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier.height(8.dp))
                OutlinedButton({
                    viewModel.refreshNotes()

                }) {
                    Text("Retry")
                }
            }
        }

        NotesScreenUiState.Loading -> {
            Column(
                modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ContainedLoadingIndicator()
            }
        }

        is NotesScreenUiState.Success -> {

            val data = (state as NotesScreenUiState.Success)

            if (data.noteToDelete != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDeleteDialog() },
                    title = { Text("Delete Note") },
                    text = { Text("Are you sure you want to delete this note?") },
                    confirmButton = {
                        TextButton(onClick = {
                            data.noteToDelete.let { viewModel.deleteNote(it.id) }
                            viewModel.dismissDeleteDialog()
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (data.isShowAddNoteBs) {

                AddNoteBottomSheet(
                    onDismissRequest = { viewModel.isShowAddNoteBs(false) },
                    onAddNote = { title, content, color ->
                        viewModel.addNote(title, content, color)
                        viewModel.isShowAddNoteBs(false)
                    }
                )
            }

            Scaffold(
                modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Notes", style = MaterialTheme.typography.headlineMedium) },
                        actions = {
                            IconButton(onClick = { viewModel.toggleTheme() }) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.isShowAddNoteBs(true) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Add Note", fontWeight = FontWeight.SemiBold)
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = innerPadding,
                    columns = GridCells.Adaptive(300.dp),
                ) {

                    items(data.notesList) { note ->

                        NoteItem(
                            modifier = modifier.fillMaxWidth().padding(16.dp),
                            note = note,
                            onDelete = {
                                viewModel.showDeleteDialog(note)
                            },
                            onClick = { onNoteClick(it.toInt()) }
                        )

                    }

                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoteItem(
    modifier: Modifier,
    note: NoteEntity,
    onDelete: (Long) -> Unit,
    onClick: (Long) -> Unit
) {
    val noteColor = Color(note.color)
    val isLightColor = noteColor == Color.White || noteColor == Color(0xFFE8EAED) || noteColor == Color(0xFFFFFFFF)

    Card(
        modifier = modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = noteColor
        ),
        onClick = {
            onClick(note.id)
        }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLightColor) MaterialTheme.colorScheme.onSurface else Color.Black
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLightColor) MaterialTheme.colorScheme.secondary else Color.Black.copy(alpha = 0.7f),
                    maxLines = 3
                )

            }

            IconButton(onClick = { onDelete(note.id) }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Note",
                    tint = if (isLightColor) MaterialTheme.colorScheme.onSurface.copy(0.3f) else Color.Black.copy(alpha = 0.3f)
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    onDismissRequest: () -> Unit,
    onAddNote: (String, String, Long) -> Unit
) {

    val noteTitle = remember { TextFieldState() }
    val noteContent = remember { TextFieldState() }
    var selectedColor by remember { mutableStateOf(noteColors[0]) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Add New Note",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                state = noteTitle,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                state = noteContent,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Select Tag Color",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(noteColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = if (selectedColor == color) 2.dp else 1.dp,
                                color = if (selectedColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .padding(if (selectedColor == color) 3.dp else 0.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (noteTitle.text.isNotBlank() || noteContent.text.isNotBlank()) {
                        onAddNote(noteTitle.text.toString(), noteContent.text.toString(), selectedColor.toArgb().toLong())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Note")
            }
        }
    }

}
