package com.personal.voicememo.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.ui.component.TodoItemCard
import com.personal.voicememo.ui.viewmodel.TodoViewModel
import com.personal.voicememo.ui.viewmodel.TodoUiState

private const val TAG = "TodoScreen"

/**
 * The screen for creating and managing todos extracted from transcripts.
 * 
 * @param transcript Optional transcript text to extract todos from
 * @param viewModel The ViewModel that manages the state and business logic for todos
 * @param onSaveComplete Callback function that is called when todos are successfully saved
 * @param modifier Modifier to be applied to the layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    transcript: String? = null,
    viewModel: TodoViewModel = hiltViewModel(),
    onSaveComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isActive = remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Handle lifecycle events
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isActive.value = true
                Lifecycle.Event.ON_PAUSE -> isActive.value = false
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val todos by viewModel.todos.collectAsState()

    // Extract todos from transcript when it changes
    LaunchedEffect(transcript) {
        transcript?.let { 
            try {
                viewModel.extractTodos(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting todos from transcript", e)
                hasError = true
            }
        }
    }

    // Navigate back when todos are successfully saved
    LaunchedEffect(uiState) {
        if (uiState is TodoUiState.Success && todos.isEmpty()) {
            try {
                onSaveComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error during save completion callback", e)
                hasError = true
            }
        }
    }

    if (hasError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error displaying todo list",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todos") },
                actions = {
                    IconButton(
                        onClick = { 
                            try {
                                viewModel.saveTodos()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error saving todos", e)
                                hasError = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Todos")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    try {
                        viewModel.addTodo(TodoItem("New Todo", "1 hour"))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding new todo", e)
                        hasError = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState) {
                is TodoUiState.Loading -> {
                    LoadingContent()
                }
                is TodoUiState.Error -> {
                    ErrorContent(message = (uiState as TodoUiState.Error).message)
                }
                else -> {
                    if (todos.isEmpty()) {
                        EmptyContent()
                    } else {
                        TodoList(
                            todos = todos,
                            onUpdateTodo = { index, updatedTodo ->
                                try {
                                    viewModel.updateTodo(index, updatedTodo)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating todo at index $index", e)
                                    hasError = true
                                }
                            },
                            onDeleteTodo = { index ->
                                try {
                                    viewModel.deleteTodo(index)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error deleting todo at index $index", e)
                                    hasError = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays a loading indicator in the center of the screen.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Displays an error message in the center of the screen.
 * 
 * @param message The error message to display
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Displays a message when there are no todos.
 */
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No todos yet. Add one or extract from a transcript.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Displays a list of todos.
 * 
 * @param todos The list of todos to display
 * @param onUpdateTodo Callback function that is called when a todo is updated
 * @param onDeleteTodo Callback function that is called when a todo is deleted
 */
@Composable
private fun TodoList(
    todos: List<TodoItem>,
    onUpdateTodo: (Int, TodoItem) -> Unit,
    onDeleteTodo: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = todos,
            key = { index, todo -> "${todo.item}_${todo.timeEstimate}_$index" }
        ) { index, todo ->
            key(todo.item, todo.timeEstimate, index) {
                TodoItemCard(
                    todo = todo,
                    index = index,
                    onUpdate = onUpdateTodo,
                    onDelete = onDeleteTodo
                )
            }
        }
    }
} 