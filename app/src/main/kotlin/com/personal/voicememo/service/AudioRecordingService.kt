package com.personal.voicememo.service

import android.content.Context
import android.media.MediaRecorder
import java.io.File
import android.util.Log

class AudioRecordingService(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var isRecording = false
    private var recordingStartTime: Long = 0

    @Suppress("DEPRECATION") // Using deprecated MediaRecorder for wider device compatibility
    fun startRecording(outputFile: File) {
        if (isRecording) {
            throw IllegalStateException("Already recording")
        }

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            currentFile = outputFile
            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            Log.d("AudioRecordingService", "Recording started. File: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("AudioRecordingService", "Error starting recording: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            throw e
        }
    }

    fun stopRecording() {
        if (!isRecording) {
            throw IllegalStateException("Not recording")
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            val duration = System.currentTimeMillis() - recordingStartTime
            Log.d("AudioRecordingService", "Recording duration: ${duration} ms")
            val fileSize = currentFile?.length() ?: 0
            if (fileSize == 0L) {
                Log.e("AudioRecordingService", "Recorded file is empty: ${currentFile?.absolutePath}")
            } else {
                Log.d("AudioRecordingService", "Recording stopped. File size: ${fileSize} bytes.")
            }
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }

    fun deleteRecording() {
        try {
            stopRecording()
        } catch (e: IllegalStateException) {
            Log.d("AudioRecordingService", "Not recording during deleteRecording: ${e.message}")
        }

        currentFile?.let {
            if (it.exists()) {
                Log.d("AudioRecordingService", "Deleting recording file: ${it.absolutePath}")
                if (it.delete()) {
                    Log.d("AudioRecordingService", "Recording file deleted successfully: ${it.absolutePath}")
                } else {
                    Log.e("AudioRecordingService", "Failed to delete recording file: ${it.absolutePath}")
                }
            } else {
                Log.d("AudioRecordingService", "Recording file does not exist: ${it.absolutePath}")
            }
        }
        currentFile = null
    }

    fun getCurrentFilePath(): String? = currentFile?.absolutePath
} 