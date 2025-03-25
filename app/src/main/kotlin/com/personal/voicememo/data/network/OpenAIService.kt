package com.personal.voicememo.data.network

import retrofit2.http.*

interface OpenAIService {
    @POST("embeddings")
    @Headers("accept: application/json", "content-type: application/json")
    suspend fun createEmbedding(
        @Body request: EmbeddingRequest
    ): EmbeddingResponse

    data class EmbeddingRequest(
        val model: String,
        val input: String
    )

    data class EmbeddingResponse(
        val data: List<EmbeddingData>,
        val model: String,
        val usage: Usage
    ) {
        data class EmbeddingData(
            val embedding: List<Float>,
            val index: Int
        )
        
        data class Usage(
            val prompt_tokens: Int,
            val total_tokens: Int
        )
    }
} 