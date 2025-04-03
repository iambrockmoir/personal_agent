package com.personal.voicememo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VoiceMemoUiState(
    val error: String? = null,
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false
)

@HiltViewModel
class VoiceMemoViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(VoiceMemoUiState())
    val uiState: StateFlow<VoiceMemoUiState> = _uiState.asStateFlow()

    fun onGoogleSignInError(error: Exception) {
        _uiState.update { currentState ->
            currentState.copy(
                error = "Google Sign-In failed: ${error.message}",
                isSignedIn = false
            )
        }
    }

    fun onGoogleSignInSuccess() {
        _uiState.update { currentState ->
            currentState.copy(
                error = null,
                isSignedIn = true
            )
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                error = null
            )
        }
    }
} 