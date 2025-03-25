package com.personal.voicememo.service

import java.io.File

interface WhisperService {
    suspend fun transcribeAudio(audioFile: File): String
} 