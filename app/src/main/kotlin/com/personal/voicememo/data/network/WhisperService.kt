package com.personal.voicememo.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Service interface for OpenAI's Whisper API.
 * 
 * This service handles:
 * - Transcribing audio files to text
 * - Managing multipart form data for file uploads
 */
interface WhisperService {
    /**
     * Transcribes an audio file using the Whisper API.
     * 
     * @param file The audio file to transcribe
     * @param model The Whisper model to use
     * @return A response containing the transcribed text
     */
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): TranscriptionResponse

    /**
     * Response structure for audio transcription.
     * 
     * @property text The transcribed text from the audio file
     */
    data class TranscriptionResponse(
        val text: String
    )
} 