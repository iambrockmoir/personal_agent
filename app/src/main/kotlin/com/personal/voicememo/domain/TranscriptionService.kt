package com.personal.voicememo.domain

import com.personal.voicememo.data.Result
import java.io.File

/**
 * Service interface for transcribing audio files to text.
 * 
 * This interface abstracts the audio transcription functionality,
 * allowing for different implementations (e.g., OpenAI Whisper, local models).
 * Implementations should handle:
 * - Audio file validation
 * - API communication (if using remote service)
 * - Error handling and retries
 * - Rate limiting considerations
 */
interface TranscriptionService {
    /**
     * Transcribes an audio file to text.
     *
     * @param audioFile The audio file to transcribe
     * @return A [Result] containing either the transcribed text or an error
     * 
     * @throws IllegalArgumentException if the audio file is invalid or unsupported format
     * @throws SecurityException if there are permission issues accessing the file
     */
    suspend fun transcribeAudio(audioFile: File): Result<String>
} 