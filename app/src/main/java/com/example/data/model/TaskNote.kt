package com.example.data.model

data class TaskNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val photoUri: String? = null
)
