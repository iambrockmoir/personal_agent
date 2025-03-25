package com.personal.voicememo.data.service

import android.util.Log
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.service.OpenAIService as IOpenAIService

class OpenAIEmbeddingService(
    private val openAIService: OpenAIService
) : IOpenAIService {
    override suspend fun generateEmbedding(text: String): List<Float> {
        Log.d("OpenAIEmbeddingService", "Generating embedding for text of length: ${text.length}")
        try {
            val request = OpenAIService.EmbeddingRequest(
                model = "text-embedding-3-small",
                input = text
            )
            val response = openAIService.createEmbedding(request)
            Log.d("OpenAIEmbeddingService", "Successfully generated embedding with ${response.data[0].embedding.size} dimensions")
            return response.data[0].embedding
        } catch (e: Exception) {
            Log.e("OpenAIEmbeddingService", "Error generating embedding: ${e.message}", e)
            throw e
        }
    }
} 