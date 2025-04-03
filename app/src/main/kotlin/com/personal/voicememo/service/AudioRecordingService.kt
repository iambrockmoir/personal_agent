package com.personal.voicememo.service

interface AudioRecordingService {
    fun startRecording(filePath: String)
    fun stopRecording()
    fun deleteRecording()
    fun getCurrentFilePath(): String?
} 