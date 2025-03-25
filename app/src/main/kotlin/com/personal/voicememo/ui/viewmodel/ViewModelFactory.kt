package com.personal.voicememo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val context: Context,
    private val voiceMemoRepository: VoiceMemoRepository,
    private val audioRecordingService: AudioRecordingService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(VoiceMemoViewModel::class.java) -> {
                VoiceMemoViewModel(
                    context = context,
                    voiceMemoRepository = voiceMemoRepository,
                    audioRecordingService = audioRecordingService
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 