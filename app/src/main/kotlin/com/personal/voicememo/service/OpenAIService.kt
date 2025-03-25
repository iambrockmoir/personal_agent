package com.personal.voicememo.service

interface OpenAIService {
    suspend fun generateEmbedding(text: String): List<Float>
} 