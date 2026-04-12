package com.app.knotes.task.pres


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.app.knotes.task.core.TaskEntity
import com.app.knotes.task.core.TaskScreenUiState
import com.app.knotes.task.core.TaskVm
import com.app.knotes.utils.convertMillisToDate
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    val viewModel: TaskVm = koinViewModel()
    val uiState by viewModel.taskScreenUiState.collectAsState()

    when (uiState) {
        is TaskScreenUiState.Error -> {
            val data = (uiState as TaskScreenUiState.Error)
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
                    viewModel.getAllTasks()

                }) {
                    Text("Retry")
                }
            }
        }


        TaskScreenUiState.Loading -> {
            Column(
                modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ContainedLoadingIndicator()
            }
        }


        is TaskScreenUiState.Success -> {
            val data = (uiState as TaskScreenUiState.Success)

            // ADD Task Dialog
            if (data.isShowAddTaskDialog) {
                var newTaskTitle by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = {
                        viewModel.showAddTaskDialog(false)
                    },
                    title = { Text("Add Task") },
                    text = {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("What needs to be done?") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.addTask(newTaskTitle)
                            viewModel.showAddTaskDialog(false)
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.showAddTaskDialog(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Scaffold(
                modifier = modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(
                        title = { Text("Tasks", style = MaterialTheme.typography.headlineMedium) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        modifier = modifier.navigationBarsPadding()
                            .padding(bottom = 90.dp),
                        onClick = { viewModel.showAddTaskDialog(true) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Task")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    OutlinedTextField(
                        value = data.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = { Text("Search tasks...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                    )

                    if (data.taskList.isEmpty()) {
                        val isSearching = data.searchQuery.isNotEmpty()
                        Column(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (isSearching) "🔍" else "✅",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (isSearching) "No tasks found" else "All caught up!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isSearching) "Try a different search term" else "Tap '+' to add a task",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 160.dp),
                            columns = GridCells.Adaptive(300.dp),
                        ) {
                            items(data.taskList, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onToggle = { viewModel.toggleTaskCompletion(task) },
                                    onDelete = { viewModel.deleteTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = convertMillisToDate(task.timestamp, "dd MMM yyyy, hh:mm a"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
