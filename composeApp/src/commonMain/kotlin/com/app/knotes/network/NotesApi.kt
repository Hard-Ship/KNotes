package com.app.knotes.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import com.app.knotes.db.NoteEntity

class NotesApi(private val client: HttpClient) {

    suspend fun getNotes(): Result<List<NoteEntity>> {
        return try {
            val response = client.get("/notes")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}