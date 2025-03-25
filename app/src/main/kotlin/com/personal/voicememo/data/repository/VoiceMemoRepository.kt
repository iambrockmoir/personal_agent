package com.personal.voicememo.data.repository

import com.personal.voicememo.data.Result
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.models.PineconeVector
import com.personal.voicememo.service.OpenAIService
import com.personal.voicememo.service.PineconeService
import com.personal.voicememo.service.WhisperService
import java.io.File
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.util.Log

class VoiceMemoRepository(
    private val voiceMemoDao: VoiceMemoDao,
    private val whisperService: WhisperService,
    private val openAIService: OpenAIService,
    private val pineconeService: PineconeService
) {
    suspend fun saveMemo(audioFile: File): Result<VoiceMemo> {
        Log.d("VoiceMemoRepository", "Attempting to save memo from audio file: ${audioFile.absolutePath}")
        return try {
            val memo = VoiceMemo(
                audioFilePath = audioFile.absolutePath,
                transcription = null,
                createdAt = Date()
            )

            // Save to database
            val id = voiceMemoDao.insertMemo(memo)
            Log.d("VoiceMemoRepository", "Memo saved with id: $id")
            Result.Success(memo.copy(id = id))
        } catch (e: Exception) {
            Log.e("VoiceMemoRepository", "Error saving memo: ${e.message}", e)
            Result.Failure(e)
        }
    }

    suspend fun transcribeMemo(memo: VoiceMemo): Result<VoiceMemo> {
        Log.d("VoiceMemoRepository", "Starting transcription for memo id: ${memo.id}, file: ${memo.audioFilePath}")
        return try {
            val transcription = whisperService.transcribeAudio(File(memo.audioFilePath))
            Log.d("VoiceMemoRepository", "Transcription completed for memo id: ${memo.id}")
            val updatedMemo = memo.copy(transcription = transcription)
            voiceMemoDao.updateMemo(updatedMemo)
            Result.Success(updatedMemo)
        } catch (e: Exception) {
            Log.e("VoiceMemoRepository", "Error transcribing memo id ${memo.id}: ${e.message}", e)
            Result.Failure(e)
        }
    }

    suspend fun saveToVectorDB(memo: VoiceMemo): Result<VoiceMemo> {
        Log.d("VoiceMemoRepository", "Generating embedding for memo id: ${memo.id}")
        return try {
            if (memo.transcription == null || memo.transcription.isEmpty()) {
                throw IllegalArgumentException("Transcription is empty, cannot generate embedding")
            }
            val embedding = openAIService.generateEmbedding(memo.transcription!!)
            Log.d("VoiceMemoRepository", "Embedding generated for memo id: ${memo.id}")
            val pineconeId = pineconeService.upsertVector(
                PineconeVector(
                    id = memo.id.toString(),
                    values = embedding,
                    metadata = mapOf("transcript" to memo.transcription!!)
                )
            )
            Log.d("VoiceMemoRepository", "Vector upsert completed for memo id: ${memo.id}, PineconeId: $pineconeId")
            val updatedMemo = memo.copy(pineconeId = pineconeId)
            voiceMemoDao.updateMemo(updatedMemo)
            Result.Success(updatedMemo)
        } catch (e: Exception) {
            Log.e("VoiceMemoRepository", "Error in vector upsert for memo id ${memo.id}: ${e.message}", e)
            Result.Failure(e)
        }
    }

    suspend fun deleteMemo(memo: VoiceMemo): Result<Unit> {
        return try {
            // Delete from database
            voiceMemoDao.deleteMemo(memo)
            
            // Delete audio file
            File(memo.audioFilePath).delete()
            
            // Delete from Pinecone if vector exists
            memo.pineconeId?.let { pineconeService.deleteVector(it) }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun deleteMemo(id: Long): Result<Unit> {
        return try {
            voiceMemoDao.getMemoById(id)?.let { memo ->
                deleteMemo(memo)
            } ?: Result.Failure(IllegalArgumentException("Memo not found"))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    suspend fun getAllMemos(): Result<List<VoiceMemo>> {
        return try {
            Result.Success(voiceMemoDao.getAllMemos())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
} 