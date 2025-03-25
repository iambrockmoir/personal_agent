package com.personal.voicememo.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class VoiceMemoViewModel(
    private val context: Context,
    private val voiceMemoRepository: VoiceMemoRepository,
    private val audioRecordingService: AudioRecordingService
) : ViewModel() {
    companion object {
        private const val TAG = "VoiceMemoViewModel"
    }

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val _memos = MutableLiveData<List<VoiceMemo>>()
    val memos: LiveData<List<VoiceMemo>> = _memos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _processingState = MutableLiveData<ProcessingState>()
    val processingState: LiveData<ProcessingState> = _processingState

    sealed class ProcessingState {
        object Idle : ProcessingState()
        object Saving : ProcessingState()
        object Transcribing : ProcessingState()
        object Vectorizing : ProcessingState()
    }

    init {
        loadMemos()
        _processingState.value = ProcessingState.Idle
    }

    private fun loadMemos() {
        viewModelScope.launch {
            try {
                when (val result = voiceMemoRepository.getAllMemos()) {
                    is com.personal.voicememo.data.Result.Success -> {
                        _memos.value = result.data
                    }
                    is com.personal.voicememo.data.Result.Failure -> {
                        _error.value = "Failed to load memos: ${result.exception.message}"
                    }
                    is com.personal.voicememo.data.Result.Loading -> {
                        // Loading state handled by UI
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading memos", e)
                _error.value = "Failed to load memos: ${e.message}"
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                val outputFile = File(context.getExternalFilesDir(null), "recording_${System.currentTimeMillis()}.m4a")
                audioRecordingService.startRecording(outputFile)
                _isRecording.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting recording", e)
                _error.value = "Failed to start recording: ${e.message}"
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                audioRecordingService.stopRecording()
                _isRecording.value = false
                val audioFile = File(audioRecordingService.getCurrentFilePath()!!)
                saveMemo(audioFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recording", e)
                _error.value = "Failed to stop recording: ${e.message}"
            }
        }
    }

    private fun saveMemo(audioFile: File) {
        viewModelScope.launch {
            try {
                _processingState.value = ProcessingState.Saving
                when (val result = voiceMemoRepository.saveMemo(audioFile)) {
                    is com.personal.voicememo.data.Result.Success -> {
                        val memo = result.data
                        _processingState.value = ProcessingState.Transcribing
                        when (val transcriptionResult = voiceMemoRepository.transcribeMemo(memo)) {
                            is com.personal.voicememo.data.Result.Success -> {
                                val transcribedMemo = transcriptionResult.data
                                _processingState.value = ProcessingState.Vectorizing
                                when (val vectorResult = voiceMemoRepository.saveToVectorDB(transcribedMemo)) {
                                    is com.personal.voicememo.data.Result.Success -> {
                                        _processingState.value = ProcessingState.Idle
                                        loadMemos()
                                    }
                                    is com.personal.voicememo.data.Result.Failure -> {
                                        _processingState.value = ProcessingState.Idle
                                        _error.value = "Failed to save to vector DB: ${vectorResult.exception.message}"
                                    }
                                    is com.personal.voicememo.data.Result.Loading -> {
                                        // Loading state handled by UI
                                    }
                                }
                            }
                            is com.personal.voicememo.data.Result.Failure -> {
                                _processingState.value = ProcessingState.Idle
                                _error.value = "Failed to transcribe audio: ${transcriptionResult.exception.message}"
                            }
                            is com.personal.voicememo.data.Result.Loading -> {
                                // Loading state handled by UI
                            }
                        }
                    }
                    is com.personal.voicememo.data.Result.Failure -> {
                        _processingState.value = ProcessingState.Idle
                        _error.value = "Failed to save memo: ${result.exception.message}"
                    }
                    is com.personal.voicememo.data.Result.Loading -> {
                        // Loading state handled by UI
                    }
                }
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Idle
                Log.e(TAG, "Error saving memo", e)
                _error.value = "Failed to save memo: ${e.message}"
            }
        }
    }

    fun deleteMemo(memo: VoiceMemo) {
        viewModelScope.launch {
            try {
                when (val result = voiceMemoRepository.deleteMemo(memo.id)) {
                    is com.personal.voicememo.data.Result.Success -> {
                        loadMemos()
                    }
                    is com.personal.voicememo.data.Result.Failure -> {
                        _error.value = "Failed to delete memo: ${result.exception.message}"
                    }
                    is com.personal.voicememo.data.Result.Loading -> {
                        // Loading state handled by UI
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting memo", e)
                _error.value = "Failed to delete memo: ${e.message}"
            }
        }
    }
} 