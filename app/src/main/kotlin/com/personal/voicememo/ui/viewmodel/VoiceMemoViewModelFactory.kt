package com.personal.voicememo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import javax.inject.Inject

class VoiceMemoViewModelFactory @Inject constructor(
    private val context: Context,
    private val voiceMemoRepository: VoiceMemoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceMemoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VoiceMemoViewModel(
                context = context,
                voiceMemoRepository = voiceMemoRepository,
                audioRecordingService = AudioRecordingService(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 