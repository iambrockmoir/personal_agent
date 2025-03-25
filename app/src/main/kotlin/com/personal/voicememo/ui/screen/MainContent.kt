package com.personal.voicememo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.personal.voicememo.ui.screen.VoiceMemoScreen
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel

@Composable
fun MainContent(viewModel: VoiceMemoViewModel) {
    Surface {
        VoiceMemoScreen(viewModel = viewModel)
    }
} 