package com.personal.voicememo.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class VoiceMemoViewModel @Inject constructor(
    private val context: Context,
    private val voiceMemoRepository: VoiceMemoRepository,
    private val audioRecordingService: AudioRecordingService
) : ViewModel() {
    companion object {
        private const val TAG = "VoiceMemoViewModel"
    }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _memos = MutableStateFlow<List<VoiceMemo>>(emptyList())
    val memos: StateFlow<List<VoiceMemo>> = _memos.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    private val _hasRecordPermission = MutableStateFlow(false)
    val hasRecordPermission: StateFlow<Boolean> = _hasRecordPermission.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    sealed class ProcessingState {
        object Idle : ProcessingState()
        object Saving : ProcessingState()
        object Transcribing : ProcessingState()
        object Vectorizing : ProcessingState()
    }

    init {
        loadMemos()
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
                outputFile.createNewFile()
                audioRecordingService.startRecording(outputFile.absolutePath)
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
                val filePath = audioRecordingService.getCurrentFilePath()!!
                audioRecordingService.stopRecording()
                _isRecording.value = false
                saveMemo(File(filePath))
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

    fun onRecordPermissionGranted() {
        _hasRecordPermission.value = true
    }

    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        Log.d(TAG, "Google Sign-In success handled for account: ${account.email}")
        _isSignedIn.value = true
        _error.value = null
        
        if (!_hasRecordPermission.value) {
            Log.d(TAG, "Requesting recording permission after successful sign-in")
        }
    }

    fun setError(message: String?) {
        Log.e(TAG, "Setting error: $message")
        _error.value = message
    }

    fun signOut() {
        Log.d(TAG, "Signing out user")
        _isSignedIn.value = false
        _hasRecordPermission.value = false
        _error.value = null
    }
} 