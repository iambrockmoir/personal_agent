package com.personal.voicememo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * The main screen for recording and managing voice memos.
 * 
 * @param viewModel The ViewModel that manages the state and business logic for voice memos
 * @param onTranscriptComplete Callback function that is called when a transcript is ready for todo extraction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMemoScreen(
    viewModel: VoiceMemoViewModel,
    onTranscriptComplete: (String) -> Unit
) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val memos by viewModel.memos.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Memos") },
                actions = {
                    if (processingState != VoiceMemoViewModel.ProcessingState.Idle) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (processingState == VoiceMemoViewModel.ProcessingState.Idle) {
                FloatingActionButton(
                    onClick = {
                        if (isRecording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Start recording"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            when (processingState) {
                VoiceMemoViewModel.ProcessingState.Idle -> {
                    if (memos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No memos yet. Start recording to create one.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(memos) { memo ->
                                MemoCard(
                                    memo = memo,
                                    onDelete = { viewModel.deleteMemo(memo) },
                                    onTranscriptComplete = onTranscriptComplete
                                )
                            }
                        }
                    }
                }
                VoiceMemoViewModel.ProcessingState.Saving -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Saving memo...")
                    }
                }
                VoiceMemoViewModel.ProcessingState.Transcribing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Transcribing audio...")
                    }
                }
                VoiceMemoViewModel.ProcessingState.Vectorizing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Processing memo...")
                    }
                }
            }
        }
    }
}

/**
 * A card that displays a voice memo with its transcription and actions.
 * 
 * @param memo The voice memo to display
 * @param onDelete Callback function that is called when the delete button is clicked
 * @param onTranscriptComplete Callback function that is called when the create todo button is clicked
 */
@Composable
private fun MemoCard(
    memo: VoiceMemo,
    onDelete: () -> Unit,
    onTranscriptComplete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(memo.createdAt),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (memo.transcription != null) {
                Text(
                    text = memo.transcription!!,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onTranscriptComplete(memo.transcription!!) }
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Todo")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete memo"
                        )
                    }
                }
            } else {
                Text(
                    text = "Transcribing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 