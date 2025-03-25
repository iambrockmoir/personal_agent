package com.personal.voicememo.domain

import com.personal.voicememo.data.Result
import com.personal.voicememo.data.models.PineconeVector

/**
 * Service interface for storing and retrieving vector embeddings.
 * 
 * This interface abstracts vector database operations,
 * allowing for different vector storage solutions to be used.
 * Implementations should handle:
 * - Vector validation
 * - API communication
 * - Error handling and retries
 * - Connection pooling and resource management
 * - Batch operations when appropriate
 */
interface VectorStorageService {
    /**
     * Stores a vector in the database.
     *
     * @param vector The vector to store, including metadata
     * @return A [Result] containing either the stored vector's ID or an error
     * 
     * @throws IllegalArgumentException if the vector is invalid
     */
    suspend fun storeVector(vector: PineconeVector): Result<String>

    /**
     * Searches for similar vectors.
     *
     * @param vector The query vector to find similar vectors for
     * @param topK The maximum number of similar vectors to return
     * @return A [Result] containing either a list of similar vectors or an error
     * 
     * @throws IllegalArgumentException if the vector is invalid or topK is less than 1
     */
    suspend fun searchSimilar(vector: List<Float>, topK: Int = 10): Result<List<PineconeVector>>

    /**
     * Deletes a vector from storage.
     *
     * @param id The ID of the vector to delete
     * @return A [Result] indicating success or failure
     * 
     * @throws IllegalArgumentException if the ID is invalid
     */
    suspend fun deleteVector(id: String): Result<Unit>
} 