package com.app.knotes.task.core

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.currentTimeMillis
import com.app.knotes.task.data.TaskRepository
import com.app.knotes.utils.FilePickerController
import com.app.knotes.utils.FileSaverController
import com.app.knotes.utils.SnackbarController
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, COMPLETED, PENDING }

sealed class TaskScreenUiState {
    object Loading : TaskScreenUiState()
    data class Error(val message: String) : TaskScreenUiState()
    data class Success(
        val taskList: List<TaskEntity> = emptyList(),
        val isShowAddTaskDialog: Boolean = false,
        val searchQuery: String = "",
        val filter: TaskFilter = TaskFilter.ALL
    ) : TaskScreenUiState()
}

class TaskVm(
    private val taskRepo: TaskRepository
) : ViewModel() {

    private var allTasks: List<TaskEntity> = emptyList()

    private val _TaskScreenUiState =
        MutableStateFlow<TaskScreenUiState>(TaskScreenUiState.Loading)
    val taskScreenUiState = _TaskScreenUiState.asStateFlow()

    fun updateTaskScreenUiState(state: TaskScreenUiState) {
        _TaskScreenUiState.value = state
    }


    fun getAllTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.getAllTasks().collectLatest { list ->
                allTasks = list
                _TaskScreenUiState.update { current ->
                    val success = current as? TaskScreenUiState.Success ?: TaskScreenUiState.Success()
                    success.copy(
                        taskList = allTasks.filter { task ->
                            val matchesSearch = task.title.contains(success.searchQuery, ignoreCase = true)
                            val matchesFilter = when (success.filter) {
                                TaskFilter.ALL -> true
                                TaskFilter.COMPLETED -> task.isCompleted
                                TaskFilter.PENDING -> !task.isCompleted
                            }
                            matchesSearch && matchesFilter
                        }
                    )
                }
            }
        }
    }

    init {
        getAllTasks()
    }
    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.insertTask(
                TaskEntity(
                    title = title.trim(),
                    timestamp = currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepo.deleteTaskById(id)
        }
    }

    fun showAddTaskDialog(show: Boolean) {
        _TaskScreenUiState.update { current ->
            (current as? TaskScreenUiState.Success)?.copy(isShowAddTaskDialog = show)
                ?: current
        }
    }

    fun updateSearchQuery(query: String) {
        _TaskScreenUiState.update { current ->
            val success = current as? TaskScreenUiState.Success ?: return@update current
            success.copy(
                searchQuery = query,
                taskList = allTasks.filter { task ->
                    val matchesSearch = task.title.contains(query, ignoreCase = true)
                    val matchesFilter = when (success.filter) {
                        TaskFilter.ALL -> true
                        TaskFilter.COMPLETED -> task.isCompleted
                        TaskFilter.PENDING -> !task.isCompleted
                    }
                    matchesSearch && matchesFilter
                }
            )
        }
    }

    fun updateFilter(filter: TaskFilter) {
        _TaskScreenUiState.update { current ->
            val success = current as? TaskScreenUiState.Success ?: return@update current
            success.copy(
                filter = filter,
                taskList = allTasks.filter { task ->
                    val matchesSearch = task.title.contains(success.searchQuery, ignoreCase = true)
                    val matchesFilter = when (filter) {
                        TaskFilter.ALL -> true
                        TaskFilter.COMPLETED -> task.isCompleted
                        TaskFilter.PENDING -> !task.isCompleted
                    }
                    matchesSearch && matchesFilter
                }
            )
        }
    }

    fun exportTasksToCsv() {
        FileSaverController.save(
            suggestedName = "tasks_export",
            extension = "csv"
        ) { file ->
            file?.let { generateCsvTemplate(it) }
        }
    }

    private fun generateCsvTemplate(file: PlatformFile) {
        viewModelScope.launch {
            try {
                val csvContent = buildString {
                    appendLine("title,isCompleted")
                    allTasks.forEach { task ->
                        val escapedTitle = task.title.replace("\"", "\"\"")
                        appendLine("\"$escapedTitle\",${task.isCompleted}")
                    }
                }
                withContext(Dispatchers.IO) {
                    file.write(csvContent.encodeToByteArray())
                }
                SnackbarController.showSnackbar(
                    message = "Tasks exported successfully!",
                    duration = SnackbarDuration.Short
                )
            } catch (e: Exception) {
                SnackbarController.showSnackbar(
                    message = e.message ?: "Failed to export tasks",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }


    fun importTasksFromCsv() {
        FilePickerController.pick(
            extensions = listOf("csv")
        ) { file ->
            file?.let { parseCsvAndImport(it) }
        }
    }

    private fun parseCsvAndImport(file: PlatformFile) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    file.readBytes()
                }

                val lines = bytes.decodeToString()
                    .lines()
                    .filter { it.isNotBlank() }

                if (lines.isEmpty()) {
                    showFeedback("CSV file is empty")
                    return@launch
                }

                // Read header
                val headers = splitCsvLine(lines.first())
                    .map { it.trim().lowercase() }

                val titleIndex = headers.indexOf("title")
                if (titleIndex == -1) {
                    showFeedback(
                        "Invalid CSV format: Missing required column 'title'",
                        isLong = true
                    )
                    return@launch
                }

                val isCompletedIndex = headers.indexOf("iscompleted")

                val tasks = mutableListOf<TaskEntity>()
                val errors = mutableListOf<String>()

                // Parse rows
                for (i in 1 until lines.size) {
                    val columns = splitCsvLine(lines[i])

                    if (columns.size <= titleIndex) {
                        errors.add("Row ${i + 1}: Missing title value")
                        continue
                    }

                    val title = columns[titleIndex].trim()

                    if (title.isBlank()) {
                        errors.add("Row ${i + 1}: Title cannot be empty")
                        continue
                    }

                    val isCompleted = try {
                        if (isCompletedIndex != -1 && columns.size > isCompletedIndex) {
                            val value = columns[isCompletedIndex]
                            if (value.isBlank()) {
                                false
                            } else {
                                parseBoolean(value)
                            }
                        } else {
                            false
                        }
                    } catch (e: IllegalArgumentException) {
                        errors.add("Row ${i + 1}: ${e.message}")
                        continue
                    }

                    tasks.add(
                        TaskEntity(
                            title = title,
                            isCompleted = isCompleted,
                            timestamp = currentTimeMillis()
                        )
                    )
                }

                if (errors.isNotEmpty()) {
                    showFeedback(
                        "Import failed with ${errors.size} error(s).\nFirst error: ${errors.first()}",
                        isLong = true
                    )
                    return@launch
                }

                if (tasks.isEmpty()) {
                    showFeedback("No tasks found to import")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    taskRepo.insertTask(tasks)
                }

                showFeedback(
                    "Imported ${tasks.size} task${if (tasks.size == 1) "" else "s"} successfully!"
                )

            } catch (e: Exception) {
                showFeedback(
                    e.message ?: "Failed to import tasks",
                    isLong = true
                )
            }
        }
    }

    private fun parseBoolean(value: String): Boolean {
        return when (value.trim().lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> throw IllegalArgumentException("Invalid boolean value: $value")
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            when (val c = line[i]) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                ',' -> {
                    if (inQuotes) {
                        current.append(c)
                    } else {
                        result.add(current.toString().trim())
                        current.clear()
                    }
                }

                else -> current.append(c)
            }
            i++
        }

        result.add(current.toString().trim())

        return result
    }

    private fun showFeedback(
        message: String,
        isLong: Boolean = false
    ) {
        SnackbarController.showSnackbar(
            message = message,
            duration = if (isLong) SnackbarDuration.Long else SnackbarDuration.Short
        )
    }

}
