package com.personal.voicememo.data.network

import retrofit2.http.*

/**
 * Service interface for OpenAI API interactions.
 * 
 * This service handles:
 * - Creating embeddings for text
 * - Generating chat completions
 * - Managing API requests and responses
 */
interface OpenAIService {
    /**
     * Creates embeddings for the given text.
     * 
     * @param request The embedding request containing the model and input text
     * @return A response containing the generated embeddings
     */
    @POST("embeddings")
    suspend fun createEmbedding(
        @Body request: EmbeddingRequest
    ): EmbeddingResponse

    /**
     * Generates a chat completion based on the provided messages.
     * 
     * @param request The chat request containing the model and messages
     * @return A response containing the generated completion
     */
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatRequest
    ): ChatResponse

    /**
     * Request body for creating embeddings.
     * 
     * @property model The model to use for embedding generation
     * @property input The text to generate embeddings for
     */
    data class EmbeddingRequest(
        val model: String,
        val input: String
    )

    /**
     * Response structure for embedding generation.
     * 
     * @property data List of generated embeddings
     * @property model The model used for generation
     * @property usage Token usage statistics
     */
    data class EmbeddingResponse(
        val data: List<Embedding>,
        val model: String,
        val usage: Usage
    )

    /**
     * Structure representing a single embedding.
     * 
     * @property embedding List of float values representing the embedding
     */
    data class Embedding(
        val embedding: List<Float>
    )

    /**
     * Request body for chat completion generation.
     * 
     * @property model The model to use for completion generation
     * @property messages List of messages in the conversation
     */
    data class ChatRequest(
        val model: String,
        val messages: List<Message>
    )

    /**
     * Structure representing a message in a chat conversation.
     * 
     * @property role The role of the message sender (e.g., "system", "user", "assistant")
     * @property content The content of the message
     */
    data class Message(
        val role: String,
        val content: String
    )

    /**
     * Response structure for chat completion generation.
     * 
     * @property id Unique identifier for the completion
     * @property choices List of generated completions
     * @property created Timestamp of creation
     * @property model The model used for generation
     * @property usage Token usage statistics
     */
    data class ChatResponse(
        val id: String,
        val choices: List<Choice>,
        val created: Long,
        val model: String,
        val usage: Usage
    ) {
        /**
         * Structure representing a single completion choice.
         * 
         * @property index Index of the choice
         * @property message The generated message
         * @property finish_reason The reason why the generation finished
         */
        data class Choice(
            val index: Int,
            val message: Message,
            val finish_reason: String
        )
    }

    /**
     * Structure representing token usage statistics.
     * 
     * @property prompt_tokens Number of tokens in the prompt
     * @property total_tokens Total number of tokens used
     */
    data class Usage(
        val prompt_tokens: Int,
        val total_tokens: Int
    )
} 