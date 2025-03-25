package com.personal.voicememo.data.service

import android.util.Log
import com.personal.voicememo.config.ApiKeys
import com.personal.voicememo.data.models.PineconeVector
import com.personal.voicememo.data.network.PineconeNetworkService
import com.personal.voicememo.service.PineconeService
import javax.inject.Inject

class PineconeStorageService @Inject constructor(
    private val pineconeService: PineconeNetworkService
) : PineconeService {
    companion object {
        private const val TAG = "PineconeStorageService"
    }

    override suspend fun upsertVector(vector: PineconeVector): String {
        try {
            Log.d(TAG, "Starting upsert with Pinecone")
            Log.d(TAG, "Upserting vector with ${vector.values.size} values")
            Log.d(TAG, "Using Pinecone index from ApiKeys: ${ApiKeys.PINECONE_INDEX}")
            val request = PineconeNetworkService.UpsertRequest(
                vectors = listOf(
                    PineconeNetworkService.Vector(
                        id = vector.id,
                        values = vector.values,
                        metadata = vector.metadata
                    )
                )
            )
            pineconeService.upsertVector(request)
            Log.d(TAG, "Successfully upserted vector with id: ${vector.id}")
            return vector.id
        } catch (e: Exception) {
            Log.e(TAG, "Error upserting vector", e)
            throw e
        }
    }

    override suspend fun queryVectors(vector: List<Float>, topK: Int): List<PineconeVector> {
        try {
            Log.d(TAG, "Querying vectors with topK=$topK")
            val request = PineconeNetworkService.QueryRequest(
                vector = vector,
                topK = topK,
                includeMetadata = true
            )
            val response = pineconeService.queryVectors(request)
            Log.d(TAG, "Found ${response.matches.size} matches")
            return response.matches.map { match ->
                PineconeVector(
                    id = match.id,
                    values = match.values,
                    metadata = match.metadata ?: emptyMap()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying vectors", e)
            throw e
        }
    }

    override suspend fun deleteVector(id: String) {
        try {
            Log.d(TAG, "Deleting vector with id: $id")
            val request = PineconeNetworkService.DeleteRequest(ids = listOf(id))
            pineconeService.deleteVector(request)
            Log.d(TAG, "Successfully deleted vector")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting vector", e)
            throw e
        }
    }
} 