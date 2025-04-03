package com.personal.voicememo.service

import com.personal.voicememo.domain.TodoList

interface OpenAIService {
    suspend fun generateEmbedding(text: String): List<Float>
    suspend fun extractTodos(transcript: String): TodoList
} 