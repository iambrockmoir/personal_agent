package com.personal.voicememo.data.service

import com.personal.voicememo.data.network.WhisperService
import com.personal.voicememo.service.WhisperService as IWhisperService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.util.Log

class WhisperTranscriptionService(
    private val whisperService: WhisperService
) : IWhisperService {
    override suspend fun transcribeAudio(audioFile: File): String {
        Log.d("WhisperTranscriptionService", "Attempting to transcribe file: ${audioFile.absolutePath}, size: ${audioFile.length()} bytes")
        if (!audioFile.exists()) {
            throw IllegalArgumentException("Audio file does not exist: ${audioFile.absolutePath}")
        }

        if (!isValidAudioFormat(audioFile.name)) {
            throw IllegalArgumentException("Invalid audio format. Only M4A files are supported.")
        }

        val requestBody = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("file", audioFile.name, requestBody)
        
        val modelRequestBody = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())
        return try {
            val response = whisperService.transcribeAudio(audioPart, modelRequestBody)
            Log.d("WhisperTranscriptionService", "Received transcription response: ${response.text}")
            response.text
        } catch (e: Exception) {
            Log.e("WhisperTranscriptionService", "Error during transcription: ${e.message}", e)
            throw e
        }
    }

    private fun isValidAudioFormat(fileName: String): Boolean {
        // Allowed audio file extensions per OpenAI documentation
        val allowedExtensions = listOf("mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm")
        return allowedExtensions.any { fileName.lowercase().endsWith("." + it) }
    }
} 