package com.personal.voicememo.service

import com.personal.voicememo.data.models.PineconeVector

interface PineconeService {
    suspend fun upsertVector(vector: PineconeVector): String
    suspend fun queryVectors(vector: List<Float>, topK: Int): List<PineconeVector>
    suspend fun deleteVector(id: String)
} 