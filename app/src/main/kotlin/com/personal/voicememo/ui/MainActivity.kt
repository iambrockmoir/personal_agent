package com.personal.voicememo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.personal.voicememo.BuildConfig
import com.personal.voicememo.R
import com.personal.voicememo.ui.screen.MainContent
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: VoiceMemoViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Sign-in result received: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Sign-in result is OK, getting account from intent")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Log.e(TAG, "Sign-in failed with result code: ${result.resultCode}")
            viewModel.setError("Sign-in failed with result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setupGoogleSignIn()

        setContent {
            MaterialTheme {
                val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle(false)
                val error by viewModel.error.collectAsStateWithLifecycle(null)
                
                Log.d(TAG, "Current state - isSignedIn: $isSignedIn, error: $error")

                if (!isSignedIn) {
                    SignInScreen(
                        onSignInClick = { signIn() },
                        onSignOutClick = { signOut() },
                        error = error
                    )
                } else {
                    MainContent(viewModel = viewModel)
                }
            }
        }
    }

    private fun setupGoogleSignIn() {
        Log.d(TAG, "Setting up Google Sign-In")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets"))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        Log.d(TAG, "Google Sign-In client configured with scopes: ${gso.scopeArray.joinToString()}")

        // Check if user is already signed in
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Log.d(TAG, "User already signed in: ${account.email}")
            viewModel.onGoogleSignInSuccess(account)
        } else {
            Log.d(TAG, "No user currently signed in")
        }
    }

    private fun signIn() {
        Log.d(TAG, "Starting Google Sign-In flow")
        // Clear any previous errors
        viewModel.setError(null)
        
        // Launch sign-in directly
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun signOut() {
        Log.d(TAG, "Signing out")
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Sign-out completed")
            viewModel.signOut()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Log.d(TAG, "Handling sign-in result")
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign in successful: ${account.email}, granted scopes: ${account.grantedScopes?.joinToString()}")
            
            // Update the UI state
            viewModel.onGoogleSignInSuccess(account)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            val message = when (statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign-in cancelled by user"
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Sign-in already in progress"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign-in failed: General error"
                GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during sign-in"
                else -> "Sign-in failed with status code: $statusCode"
            }
            Log.e(TAG, "Sign in failed: $message", e)
            viewModel.setError(message)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(onClick = onSignInClick) {
            Text("Sign in with Google")
        }
    }
} 