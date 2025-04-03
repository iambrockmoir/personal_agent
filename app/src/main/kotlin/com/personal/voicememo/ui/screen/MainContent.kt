package com.personal.voicememo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.personal.voicememo.ui.viewmodel.VoiceMemoViewModel

/**
 * Sealed class representing the different screens in the app.
 * Each screen has a unique route identifier.
 */
sealed class Screen(val route: String) {
    /** The main screen for recording and managing voice memos */
    object VoiceMemo : Screen("voice_memo")
    
    /** The screen for creating and managing todos extracted from transcripts */
    object Todo : Screen("todo")
}

/**
 * The main content of the app, which handles navigation between screens.
 * 
 * @param viewModel The ViewModel that manages the state and business logic for voice memos
 */
@Composable
fun MainContent(viewModel: VoiceMemoViewModel) {
    val navController = rememberNavController()
    var currentTranscript by remember { mutableStateOf<String?>(null) }

    Surface {
        NavHost(
            navController = navController,
            startDestination = Screen.VoiceMemo.route
        ) {
            composable(Screen.VoiceMemo.route) {
                VoiceMemoScreen(
                    viewModel = viewModel,
                    onTranscriptComplete = { transcript ->
                        currentTranscript = transcript
                        navController.navigate(Screen.Todo.route)
                    }
                )
            }
            composable(Screen.Todo.route) {
                TodoScreen(
                    transcript = currentTranscript,
                    onSaveComplete = {
                        // Navigate back to the VoiceMemo screen after saving todos
                        navController.navigate(Screen.VoiceMemo.route) {
                            // Pop up to the VoiceMemo screen, removing the Todo screen from the back stack
                            popUpTo(Screen.VoiceMemo.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
} 