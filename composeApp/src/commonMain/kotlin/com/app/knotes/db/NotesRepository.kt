package com.app.knotes.db

import com.app.knotes.network.NotesApi
import kotlinx.coroutines.flow.Flow
import com.app.knotes.currentTimeMillis

class NotesRepository(
    private val noteDao: NoteDao,
    private val notesApi: NotesApi
) {

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insert(note: NoteEntity) = noteDao.insert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun refreshNotes(): Result<Unit> {
        return notesApi.getNotes().map { remoteNotes ->
            remoteNotes.forEach { remoteNote ->
                noteDao.insert(remoteNote.copy(timestamp = currentTimeMillis()))
            }
        }
    }
}
