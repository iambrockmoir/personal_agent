package com.personal.voicememo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.personal.voicememo.VoiceMemoApplication
import com.personal.voicememo.ui.screen.MainContent
import com.personal.voicememo.ui.theme.VoiceMemoTheme
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel
import com.personal.voicememo.ui.viewmodel.ViewModelFactory
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: VoiceMemoViewModel by viewModels { 
        viewModelFactory
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            // Show error message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as VoiceMemoApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        setContent {
            VoiceMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordPermission() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
} 