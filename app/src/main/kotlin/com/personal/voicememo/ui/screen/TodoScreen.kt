package com.personal.voicememo.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.ui.component.TodoItemCard
import com.personal.voicememo.ui.viewmodel.TodoViewModel
import com.personal.voicememo.ui.viewmodel.TodoUiState

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
    val uiState by viewModel.uiState.collectAsState()
    val todos by viewModel.todos.collectAsState()

    // Extract todos from transcript when it changes
    LaunchedEffect(transcript) {
        transcript?.let { viewModel.extractTodos(it) }
    }

    // Navigate back when todos are successfully saved
    LaunchedEffect(uiState) {
        if (uiState is TodoUiState.Success && todos.isNotEmpty()) {
            onSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todos") },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveTodos() }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Todos")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addTodo(TodoItem("New Todo", "1 hour")) }
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
                                viewModel.updateTodo(index, updatedTodo)
                            },
                            onDeleteTodo = { index ->
                                viewModel.deleteTodo(index)
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
        items(todos) { todo ->
            TodoItemCard(
                todo = todo,
                onUpdate = { updatedTodo ->
                    onUpdateTodo(todos.indexOf(todo), updatedTodo)
                },
                onDelete = {
                    onDeleteTodo(todos.indexOf(todo))
                }
            )
        }
    }
} 