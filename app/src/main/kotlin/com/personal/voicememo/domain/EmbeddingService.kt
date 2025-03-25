package com.personal.voicememo.domain

import com.personal.voicememo.data.Result

/**
 * Service interface for converting text into vector embeddings.
 * 
 * This interface abstracts the text-to-vector conversion functionality,
 * allowing for different embedding models or services to be used.
 * Implementations should handle:
 * - Text preprocessing
 * - API communication (if using remote service)
 * - Error handling and retries
 * - Rate limiting and quota management
 * - Caching if appropriate
 */
interface EmbeddingService {
    /**
     * Creates a vector embedding from text.
     *
     * @param text The text to convert into a vector embedding
     * @return A [Result] containing either the vector embedding as a list of floats or an error
     * 
     * @throws IllegalArgumentException if the text is empty or too long
     */
    suspend fun createEmbedding(text: String): Result<List<Float>>
} 