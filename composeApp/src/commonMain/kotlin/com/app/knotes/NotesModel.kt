package com.app.knotes

import kotlinx.serialization.Serializable

@Serializable
data class NotesModel(
    val id : Int,
    val title : String,
    val note : String,
)
