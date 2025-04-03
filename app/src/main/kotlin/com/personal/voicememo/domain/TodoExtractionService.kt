package com.personal.voicememo.domain

interface TodoExtractionService {
    suspend fun extractTodos(transcript: String): TodoList
} 