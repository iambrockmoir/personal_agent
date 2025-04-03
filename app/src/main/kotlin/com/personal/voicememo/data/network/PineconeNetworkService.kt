package com.personal.voicememo.data.network

import retrofit2.http.POST
import retrofit2.http.Body
import com.personal.voicememo.BuildConfig
import com.personal.voicememo.config.ApiKeys

/**
 * Service interface for Pinecone vector database operations.
 * 
 * This service handles:
 * - Upserting vectors to the database
 * - Querying similar vectors
 * - Deleting vectors from the database
 */
interface PineconeNetworkService {
    /**
     * Upserts vectors to the Pinecone database.
     * 
     * @param request The upsert request containing vectors and namespace
     * @return A response containing the count of upserted vectors
     */
    @POST("/vectors/upsert")
    suspend fun upsertVector(
        @Body request: UpsertRequest
    ): UpsertResponse

    /**
     * Queries the Pinecone database for similar vectors.
     * 
     * @param request The query request containing the query vector and parameters
     * @return A response containing matching vectors
     */
    @POST("/query")
    suspend fun queryVectors(
        @Body request: QueryRequest
    ): QueryResponse

    /**
     * Deletes vectors from the Pinecone database.
     * 
     * @param request The delete request containing vector IDs and namespace
     * @return A response indicating the success of the operation
     */
    @POST("/vectors/delete")
    suspend fun deleteVector(
        @Body request: DeleteRequest
    ): DeleteResponse

    /**
     * Request structure for upserting vectors.
     * 
     * @property vectors List of vectors to upsert
     * @property namespace The namespace to upsert to (default: "")
     */
    data class UpsertRequest(
        val vectors: List<Vector>,
        val namespace: String = ""  // Default namespace
    )

    /**
     * Response structure for vector upsertion.
     * 
     * @property upsertedCount Number of vectors successfully upserted
     */
    data class UpsertResponse(
        val upsertedCount: Int
    )

    /**
     * Request structure for querying vectors.
     * 
     * @property vector The query vector
     * @property topK Number of top matches to return
     * @property includeMetadata Whether to include metadata in the response
     * @property namespace The namespace to query from (default: "")
     */
    data class QueryRequest(
        val vector: List<Float>,
        val topK: Int,
        val includeMetadata: Boolean = true,
        val namespace: String = ""  // Default namespace
    )

    /**
     * Response structure for vector queries.
     * 
     * @property matches List of matching vectors
     */
    data class QueryResponse(
        val matches: List<Match>
    ) {
        /**
         * Structure representing a matching vector.
         * 
         * @property id Unique identifier of the vector
         * @property score Similarity score
         * @property values Vector values
         * @property metadata Associated metadata
         */
        data class Match(
            val id: String,
            val score: Float,
            val values: List<Float>,
            val metadata: Map<String, String>?
        )
    }

    /**
     * Request structure for deleting vectors.
     * 
     * @property ids List of vector IDs to delete
     * @property namespace The namespace to delete from (default: "")
     */
    data class DeleteRequest(
        val ids: List<String>,
        val namespace: String = ""  // Default namespace
    )

    /**
     * Response structure for vector deletion.
     * 
     * @property message Success message
     */
    data class DeleteResponse(
        val message: String = "Success"
    )

    /**
     * Structure representing a vector in the database.
     * 
     * @property id Unique identifier of the vector
     * @property values Vector values
     * @property metadata Associated metadata
     */
    data class Vector(
        val id: String,
        val values: List<Float>,
        val metadata: Map<String, String>?
    )
} 