package com.app.knotes

import com.app.knotes.db.NoteEntity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.app.knotes.utils.KBackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.app.knotes.theme.noteColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    onBack: () -> Unit
) {
    val viewModel: NotesVm = koinViewModel()
    val detailState by viewModel.noteDetailScreenState.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId.toLong())
    }

    // Title and Content states initialized from the note when loaded
    val titleState = remember { TextFieldState() }
    val contentState = remember { TextFieldState() }
    var selectedColor by remember { mutableStateOf(noteColors[0]) }
    var showColorPicker by remember { mutableStateOf(false) }

    var isInitialized by remember { mutableStateOf(false) }

    // Effect to update local TF states when note is loaded
    LaunchedEffect(detailState) {
        if (detailState is NoteDetailUiState.Success && !isInitialized) {
            val note = (detailState as NoteDetailUiState.Success).note
            titleState.edit {
                replace(0, length, note.title)
            }
            contentState.edit {
                replace(0, length, note.content)
            }
            selectedColor = Color(note.color)
            isInitialized = true
        }
    }

    // Shared save-and-navigate-back logic used by both back arrow and system back gesture
    val saveAndGoBack = {
        if (isInitialized) {
            viewModel.updateNote(
                id = noteId.toLong(),
                title = titleState.text.toString(),
                content = contentState.text.toString(),
                color = selectedColor.toArgb().toLong()
            )
        }
        onBack()
    }

    // Intercept system back gesture (predictive back, nav gesture, HW back button)
    KBackHandler(onBack = saveAndGoBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(selectedColor, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = saveAndGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(
                            imageVector = Icons.Rounded.ColorLens,
                            contentDescription = "Change Color",
                            tint = if (showColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (detailState is NoteDetailUiState.Success) {
                        IconButton(onClick = {
                            viewModel.deleteNote(noteId.toLong())
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (detailState) {
            is NoteDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NoteDetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((detailState as NoteDetailUiState.Error).message)
                }
            }
            is NoteDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (showColorPicker) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(noteColors) { color ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
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
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    ) {
                        // Borderless Title Input
                        TextField(
                            state = titleState,
                            modifier = Modifier.fillMaxWidth(),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            placeholder = {
                                Text(
                                    "Title",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        // Borderless Content Input
                        TextField(
                            state = contentState,
                            modifier = Modifier.fillMaxSize().weight(1f),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
                            ),
                            placeholder = {
                                Text(
                                    "Start writing...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                    }
                }
            }
            NoteDetailUiState.Idle -> {}
        }
    }
}
