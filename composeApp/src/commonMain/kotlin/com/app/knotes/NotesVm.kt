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
        val notesList: List<NoteEntity> = emptyList(),
        val noteToDelete: NoteEntity? = null,
        val isShowAddNoteBs: Boolean = false
    ) : NotesScreenUiState()
}

sealed class NoteDetailUiState {
    object Loading : NoteDetailUiState()
    data class Success(val note: NoteEntity) : NoteDetailUiState()
    data class Error(val message: String) : NoteDetailUiState()
    object Idle : NoteDetailUiState()
}

class NotesVm(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _noteToDelete = MutableStateFlow<NoteEntity?>(null)
    private val _isShowAddNoteBs = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val state: StateFlow<NotesScreenUiState> = combine(
        notesRepository.getAllNotes(),
        _noteToDelete,
        _isShowAddNoteBs,
        _isLoading,
        _searchQuery
    ) { notes, noteToDelete, isShowAddNoteBs, isLoading, query ->
        if (isLoading && notes.isEmpty()) {
            NotesScreenUiState.Loading
        } else {
            val filteredNotes = if (query.isBlank()) {
                notes
            } else {
                notes.filter {
                    it.title.contains(query, ignoreCase = true) || 
                    it.content.contains(query, ignoreCase = true)
                }
            }
            // Sort by pinned status first, then by timestamp descending
            val sortedNotes = filteredNotes.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenByDescending { it.timestamp }
            )
            NotesScreenUiState.Success(
                notesList = sortedNotes,
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNote(title: String, content: String, color: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                title = title,
                content = content,
                timestamp = currentTimeMillis(),
                color = color,
                isPinned = false
            )
            notesRepository.insert(note)
        }
    }
    
    fun showDeleteDialog(note: NoteEntity) {
        _noteToDelete.value = note
    }

    fun dismissDeleteDialog() {
        _noteToDelete.value = null
    }

    fun isShowAddNoteBs(show: Boolean) {
        _isShowAddNoteBs.value = show
    }
    
    fun deleteNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            notesRepository.deleteNoteById(id)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            notesRepository.update(note.copy(isPinned = !note.isPinned))
        }
    }

    fun getNoteById(id: Long) {
        _noteDetailScreenState.value = NoteDetailUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entity = notesRepository.getNoteById(id)
                if (entity != null) {
                    _noteDetailScreenState.value = NoteDetailUiState.Success(entity)
                } else {
                    _noteDetailScreenState.value = NoteDetailUiState.Error("Note not found")
                }
            } catch (e: Exception) {
                _noteDetailScreenState.value = NoteDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateNote(id: Long, title: String, content: String, color: Long, isPinned: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = NoteEntity(
                id = id,
                title = title,
                content = content,
                timestamp = currentTimeMillis(),
                color = color,
                isPinned = isPinned
            )
            notesRepository.update(entity)
        }
    }
    
}
