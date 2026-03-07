package com.app.knotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.knotes.db.NoteEntity
import com.app.knotes.db.NotesRepository
import com.app.knotes.db.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.app.knotes.currentTimeMillis

sealed class NotesScreenUiState {
    object Loading : NotesScreenUiState()
    data class Error(val message: String) : NotesScreenUiState()
    data class Success(
        val notesList: List<NotesModel> = emptyList(),
        val noteToDelete: NotesModel? = null,
        val isShowAddNoteBs: Boolean = false
    ) : NotesScreenUiState()
}

sealed class NoteDetailUiState {
    object Loading : NoteDetailUiState()
    data class Success(val note: NotesModel) : NoteDetailUiState()
    data class Error(val message: String) : NoteDetailUiState()
    object Idle : NoteDetailUiState()
}

class NotesVm(
    private val notesRepository: NotesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private val _noteToDelete = MutableStateFlow<NotesModel?>(null)
    private val _isShowAddNoteBs = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val state: StateFlow<NotesScreenUiState> = combine(
        notesRepository.getAllNotes(),
        _noteToDelete,
        _isShowAddNoteBs,
        _isLoading
    ) { notes, noteToDelete, isShowAddNoteBs, isLoading ->
        if (isLoading && notes.isEmpty()) {
            NotesScreenUiState.Loading
        } else {
            NotesScreenUiState.Success(
                notesList = notes.map { NotesModel(it.id.toInt(), it.title, it.content) },
                noteToDelete = noteToDelete,
                isShowAddNoteBs = isShowAddNoteBs
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotesScreenUiState.Loading
    )

    private val _noteDetailScreenState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Idle)
    val noteDetailScreenState: StateFlow<NoteDetailUiState> = _noteDetailScreenState.asStateFlow()

    init {
        refreshNotes()
    }

    fun refreshNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            notesRepository.refreshNotes()
            _isLoading.value = false
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                title = title,
                content = content,
                timestamp = currentTimeMillis()
            )
            notesRepository.insert(note)
        }
    }
    
    fun showDeleteDialog(note: NotesModel) {
        _noteToDelete.value = note
    }

    fun dismissDeleteDialog() {
        _noteToDelete.value = null
    }

    fun isShowAddNoteBs(show: Boolean) {
        _isShowAddNoteBs.value = show
    }
    
    fun deleteNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            notesRepository.deleteNoteById(id.toLong())
        }
    }

    fun getNoteById(id: Int) {
        _noteDetailScreenState.value = NoteDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entity = notesRepository.getNoteById(id.toLong())
                if (entity != null) {
                    _noteDetailScreenState.value = NoteDetailUiState.Success(
                        NotesModel(entity.id.toInt(), entity.title, entity.content)
                    )
                } else {
                    _noteDetailScreenState.value = NoteDetailUiState.Error("Note not found")
                }
            } catch (e: Exception) {
                _noteDetailScreenState.value = NoteDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateNote(id: Int, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = NoteEntity(
                id = id.toLong(),
                title = title,
                content = content,
                timestamp = currentTimeMillis()
            )
            notesRepository.update(entity)
        }
    }
    
    fun toggleTheme() {
        viewModelScope.launch {
            settingsRepository.toggleTheme()
        }
    }
}
