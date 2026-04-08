package com.app.knotes.task.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.currentTimeMillis
import com.app.knotes.task.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class TaskScreenUiState {
    object Loading : TaskScreenUiState()
    data class Error(val message: String) : TaskScreenUiState()
    data class Success(
        val taskList: List<TaskEntity> = emptyList(),
        val isShowAddTaskDialog: Boolean = false,
        val searchQuery: String = ""
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
                        taskList = allTasks.filter { it.title.contains(success.searchQuery, ignoreCase = true) }
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
            (current as? TaskScreenUiState.Success)?.copy(
                searchQuery = query,
                taskList = allTasks.filter { it.title.contains(query, ignoreCase = true) }
            ) ?: current
        }
    }

}
