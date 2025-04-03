package com.personal.voicememo.data.service

import android.util.Log
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.service.OpenAIService as IOpenAIService
import javax.inject.Inject

class OpenAIEmbeddingService @Inject constructor(
    private val api: OpenAIService
) : IOpenAIService {
    override suspend fun generateEmbedding(text: String): List<Float> {
        Log.d("OpenAIEmbeddingService", "Generating embedding for text of length: ${text.length}")
        try {
            val request = OpenAIService.EmbeddingRequest(
                model = "text-embedding-ada-002",
                input = text
            )
            val response = api.createEmbedding(request)
            Log.d("OpenAIEmbeddingService", "Successfully generated embedding with ${response.data[0].embedding.size} dimensions")
            return response.data[0].embedding
        } catch (e: Exception) {
            Log.e("OpenAIEmbeddingService", "Error generating embedding: ${e.message}", e)
            throw e
        }
    }

    override suspend fun extractTodos(transcript: String): TodoList {
        throw UnsupportedOperationException("Todo extraction is not supported by OpenAIEmbeddingService")
    }
} 