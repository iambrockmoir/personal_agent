package com.personal.voicememo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        Log.d("MainViewModel", "Google Sign-In success handled for account: ${account.email}")
        // The ID token will be automatically used by the NetworkModule
    }

    fun onRecordPermissionGranted() {
        Log.d("MainViewModel", "Record permission granted")
    }
} 