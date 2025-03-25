package com.personal.voicememo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMemoScreen(
    viewModel: VoiceMemoViewModel
) {
    val memos by viewModel.memos.observeAsState(emptyList())
    val isRecording by viewModel.isRecording.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val processingState by viewModel.processingState.observeAsState(VoiceMemoViewModel.ProcessingState.Idle)

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
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                enabled = processingState == VoiceMemoViewModel.ProcessingState.Idle
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop recording" else "Start recording"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(memos) { memo ->
                    MemoItem(
                        memo = memo,
                        onDelete = { viewModel.deleteMemo(memo) }
                    )
                }
            }

            // Processing state indicator
            AnimatedVisibility(
                visible = processingState != VoiceMemoViewModel.ProcessingState.Idle,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (processingState) {
                                VoiceMemoViewModel.ProcessingState.Saving -> "Saving recording..."
                                VoiceMemoViewModel.ProcessingState.Transcribing -> "Transcribing audio..."
                                VoiceMemoViewModel.ProcessingState.Vectorizing -> "Processing for search..."
                                else -> ""
                            },
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            error?.let {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { /* Clear error */ }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
fun MemoItem(
    memo: VoiceMemo,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = memo.transcription ?: "Transcribing...",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(memo.createdAt),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete memo")
            }
        }
    }
} 