package com.personal.voicememo.data.network

import retrofit2.http.POST
import retrofit2.http.Body
import com.personal.voicememo.BuildConfig
import com.personal.voicememo.config.ApiKeys

interface PineconeNetworkService {
    @POST("/vectors/upsert")
    suspend fun upsertVector(
        @Body request: UpsertRequest
    ): UpsertResponse

    @POST("/query")
    suspend fun queryVectors(
        @Body request: QueryRequest
    ): QueryResponse

    @POST("/vectors/delete")
    suspend fun deleteVector(
        @Body request: DeleteRequest
    ): DeleteResponse

    data class UpsertRequest(
        val vectors: List<Vector>,
        val namespace: String = ""  // Default namespace
    )

    data class UpsertResponse(
        val upsertedCount: Int
    )

    data class QueryRequest(
        val vector: List<Float>,
        val topK: Int,
        val includeMetadata: Boolean = true,
        val namespace: String = ""  // Default namespace
    )

    data class QueryResponse(
        val matches: List<Match>
    ) {
        data class Match(
            val id: String,
            val score: Float,
            val values: List<Float>,
            val metadata: Map<String, String>?
        )
    }

    data class DeleteRequest(
        val ids: List<String>,
        val namespace: String = ""  // Default namespace
    )

    data class DeleteResponse(
        val message: String = "Success"
    )

    data class Vector(
        val id: String,
        val values: List<Float>,
        val metadata: Map<String, String>?
    )
} 