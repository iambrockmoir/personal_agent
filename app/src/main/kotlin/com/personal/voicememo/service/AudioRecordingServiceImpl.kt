package com.personal.voicememo.service

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class AudioRecordingServiceImpl(
    private val context: Context
) : AudioRecordingService {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    override fun startRecording(filePath: String) {
        if (mediaRecorder != null) {
            throw IllegalStateException("Recording is already in progress")
        }

        mediaRecorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)
            prepare()
            start()
        }
        currentFilePath = filePath
    }

    override fun stopRecording() {
        if (mediaRecorder == null) {
            throw IllegalStateException("No recording in progress")
        }

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        currentFilePath = null
    }

    override fun deleteRecording() {
        currentFilePath?.let { path ->
            File(path).delete()
        }
        currentFilePath = null
    }

    override fun getCurrentFilePath(): String? = currentFilePath

    internal fun createMediaRecorder(): MediaRecorder = MediaRecorder()
} 